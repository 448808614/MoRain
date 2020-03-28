/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/18 13:05:27
 *
 * MiraiPlugins/MiraiPlugins/Eval.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mirai.permission.Permissible;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.Global;
import net.mamoe.mirai.message.MessagePacket;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import cn.mcres.karlatemp.mxlib.tools.Unsafe;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.CharBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Eval {
    public static final ScriptEngine engine;
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long ScriptObjectMirror$global$offset;

    static {
        try {
            final Field field = ScriptObjectMirror.class.getDeclaredField("global");
            ScriptObjectMirror$global$offset = unsafe.objectFieldOffset(field);
        } catch (Throwable thr) {
            throw new ExceptionInInitializerError(thr);
        }
    }

    public static Global getGlobal(Object object) {
        if (object instanceof ScriptObjectMirror) {
            return (Global) unsafe.getReference(object, ScriptObjectMirror$global$offset);
        }
        return null;
    }

    public static class ThreadPool {
        @NotNull
        public final ConcurrentLinkedQueue<ThreadInvokingContext> usable = new ConcurrentLinkedQueue<>();
        @NotNull
        public final ThreadFactory factory;

        public ThreadPool(@NotNull ThreadFactory factory) {
            this.factory = factory;
        }

        @NotNull
        public ThreadInvokingContext alloc() {
            final ThreadInvokingContext context = usable.poll();
            if (context != null) {
                context.startTime = System.currentTimeMillis();
                return context;
            }
            return new ThreadInvokingContext(this);
        }

        public void release(@NotNull final ThreadInvokingContext context) {
            final Thread thread = context.thread;
            if (thread != null) {
                if (thread.isAlive()) {
                    context.startTime = System.currentTimeMillis();
                    usable.add(context);
                }
            }
        }
    }

    public static class ThreadInvokingContext implements Runnable {
        public final ThreadPool owner;
        public Thread thread;
        public final AtomicReference<Runnable> task = new AtomicReference<>();
        public volatile boolean shutdown;
        public long startTime;
        private final AtomicBoolean taskStatus = new AtomicBoolean();

        public boolean updateTask(Runnable task) {
            if (this.task.compareAndSet(null, task)) {
                synchronized (this) {
                    notify();
                }
                if (thread == null) {
                    shutdown = false;
                    (thread = owner.factory.newThread(this)).start();
                } else if (!taskStatus.compareAndSet(false, true)) {
                    this.task.set(task);
                    shutdown = false;
                    while (true) {
                        if (thread == null) {
                            (thread = owner.factory.newThread(this)).start();
                            break;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        public ThreadInvokingContext(ThreadPool owner) {
            this.owner = owner;
        }

        @Override
        public void run() {
            synchronized (this) {
                final Thread current = Thread.currentThread();
                if (thread != null && thread != current) return;
                thread = current;
                taskStatus.set(false);
            }
            while (!shutdown) {
                if (task.get() == null)
                    synchronized (this) {
                        try {
                            wait(60000L);
                        } catch (InterruptedException ignore) {
                        }
                    }
                final Runnable currentTask = task.get();
                if (currentTask != null) {
                    startTime = System.currentTimeMillis();
                    currentTask.run();
                    task.set(null);
                    taskStatus.set(false);
                } else if (taskStatus.compareAndSet(false, true)) break;
            }
            thread = null;
        }
    }

    public static class EvalThreadingManager {
        public static ThreadPool pool;

        public static String process(Supplier<String> invoker, long time, TimeUnit unit) {
            final ThreadInvokingContext context = pool.alloc();
            CompletableFuture<String> task = new CompletableFuture<>();
            context.updateTask(() -> task.complete(invoker.get()));
            try {
                String result = task.get(time, unit);
                pool.release(context);
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
                ignore.printStackTrace();
                context.thread.interrupt();
                try {
                    Thread.sleep(500L);
                    if (context.task.get() != null) {
                        context.thread.stop();
                    }
                } catch (InterruptedException ignored) {
                    context.thread.stop();
                }
                return "超时啦!";
            }
        }
    }

    static {
        engine = new NashornScriptEngineFactory().getScriptEngine(
                new String[]{"--no-java", "--no-syntax-extensions", "--no-typed-arrays"},
                Eval.class.getClassLoader(),
                className -> false
        );
    }

    public static BiConsumer<Bindings, ScriptContext> GLOBAL_OVERRIDER;

    public static String invoke(String code, String source, BiConsumer<Bindings, ScriptContext> overrider) {
        final Bindings bindings = engine.createBindings();
        ScriptContext context = new SimpleScriptContext();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        context.setAttribute(ScriptEngine.FILENAME, source, ScriptContext.ENGINE_SCOPE);
        StringWriter sw = new StringWriter();
        context.setWriter(sw);
        context.setErrorWriter(sw);
        if (GLOBAL_OVERRIDER != null) GLOBAL_OVERRIDER.accept(bindings, context);
        if (overrider != null) overrider.accept(bindings, context);
        Object value;
        try {
            value = engine.eval(code, context);
        } catch (Throwable e) {
            return e.toString();
        }
        if (value == null) {
            if (sw.getBuffer().length() > 0) {
                return sw.toString();
            } else {
                return "(....什么都没有....)";
            }
        }
        return String.valueOf(value);
    }

    public static boolean eval(MessageChain chain, MessagePacket<?, ?> event) {
        final String compiled = MessageCoder.coder(chain).toString().trim();
        if (compiled.startsWith("-eval ")) {
            final Permissible permissible = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get();
            if (permissible.hasPermission("banned")) {
                event.getSubject().sendMessageAsync("大坏蛋!");
                return true;
            }
            if (!permissible.hasPermission("magic.eval")) {
                event.getSubject().sendMessageAsync("不可以!");
                return true;
            }
            String code = compiled.substring(5);

            String value = EvalThreadingManager.process(
                    () -> {
                        PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(permissible);
                        return invoke(code, "<eval " + event.getSender().toString() + ">", null);
                    },
                    4000L, TimeUnit.MILLISECONDS);
            if (value.trim().startsWith("/自闭")) {
                value = "不可以!";
            }
            try {
                event.getSubject().sendMessageAsync(MessageCoder.coder(value, event.getSubject()));
            } catch (Throwable e) {
                event.getSubject().sendMessageAsync(e.toString());
                return true;
            }
            return true;
        }
        return false;
    }
}
