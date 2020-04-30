/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:40:42
 *
 * MiraiPlugins/MiraiBootstrap/PluginManager.java
 */

package cn.mcres.karlatemp.mirai.plugin;

import cn.mcres.karlatemp.mirai.AsyncExecKt;
import cn.mcres.karlatemp.mirai.CommandMgr;
import cn.mcres.karlatemp.mirai.command.Reload;
import cn.mcres.karlatemp.mirai.command.Shutdown;
import cn.mcres.karlatemp.mxlib.event.HandlerList;
import cn.mcres.karlatemp.mxlib.tools.Unsafe;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PluginManager {
    private static final Object base;
    private static final long offset;
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    static {
        try {
            Field f = HandlerList.class.getDeclaredField("handlers");
            offset = unsafe.staticFieldOffset(f);
            base = unsafe.staticFieldBase(f);
            unsafe.ensureClassInitialized(HandlerList.class);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentLinkedQueue<WeakReference<HandlerList<?>>> handlers() {
        return (ConcurrentLinkedQueue<WeakReference<HandlerList<?>>>) unsafe.getReference(base, offset);
    }

    public static void reload() {
        for (WeakReference<HandlerList<?>> handler : handlers()) {
            final HandlerList<?> list = handler.get();
            if (list != null) {
                list.clear();
            }
        }
        CommandMgr.commands.clear();
        registerDefaultCommands();
        AsyncExecKt.INSTANCE.stop();
        File pl = new File("plugins");
        pl.mkdirs();
        File libs = new File("libraries");
        libs.mkdirs();
        for (Plugin old : PluginLoaderManager.plugins0) {
            old.onDisable();
        }
        PluginLoaderManager.clear();
        for (File f : libs.listFiles()) {
            if (f.isFile()) {
                if (f.getName().endsWith(".jar")) {
                    PluginLoaderManager.loadLibrary(f);
                }
            }
        }
        for (File f : pl.listFiles()) {
            if (f.isFile()) {
                if (f.getName().endsWith(".jar")) {
                    PluginLoaderManager.loadPlugin(f);
                }
            }
        }
    }

    private static void registerDefaultCommands() {
        CommandMgr.register("reload", new Reload());
        CommandMgr.register("shutdown", new Shutdown());
    }
}
