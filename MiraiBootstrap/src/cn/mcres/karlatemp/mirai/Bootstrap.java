/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:31:08
 *
 * MiraiPlugins/MiraiBootstrap/Bootstrap.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mirai.arguments.ArgumentParser;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.event.MemberJoinGroupEvent;
import cn.mcres.karlatemp.mirai.event.MemberLeaveGroupEvent;
import cn.mcres.karlatemp.mirai.event.MessageSendEvent;
import cn.mcres.karlatemp.mirai.permission.Permissible;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import cn.mcres.karlatemp.mirai.plugin.PluginManager;
import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactoryJvm;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.japt.Events;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.MessageEvent;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.SystemDeviceInfoKt;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bootstrap {
    public static final BitSet commandPrefixes = new BitSet() {{
        set('/', true);
        set('#', true);
        set('$', true);
        set('`', true);
        set('·', true);
        set('！', true);
        set('!', true);
        set('%', true);
        set('%', true);
        set('^', true);
        set('*', true);
        set('+', true);
        set('=', true);
        set('>', true);
        set('\\', true);
        set('|', true);
        set('-', true);
        set('、', true); // What the hell??
        set('￥', true); // ?????????
    }};
    public static final String VERSION = "1.0.1";
    private static final Object VOID0 = null;
    @NotNull
    public static final Bot bot = VOID();

    @SuppressWarnings("unchecked")
    public static <T> T VOID() {
        return (T) VOID0;
    }

    public static long getLoginQQ() {
        return LoginData.getLoginQQ();
    }

    public static String getLoginPasswd() {
        return LoginData.getLoginPasswd();
    }

    public static String getVersion() {
        return VERSION;
    }

    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();
        // MiraiWSLogger.install(6765);
        Logging.install();
        Logging.creator = (logger, bot) -> logger.setLevel(Level.ALL);
        // 使用自定义的配置
        final Bot bot = BotFactoryJvm.newBot(getLoginQQ(), getLoginPasswd(), new BotConfiguration() {
            {
                setDeviceInfo(context ->
                        SystemDeviceInfoKt.loadAsDeviceInfo(new File("deviceInfo.json"), context)
                );
                final AtomicReference<MiraiLogger> reference = new AtomicReference<>();
                setBotLoggerSupplier(bot -> {
                    MiraiLogger logger = reference.get();
                    if (logger == null) {
                        logger = Logging.newLogger(bot);
                        reference.set(logger);
                    }
                    return logger;
                });
                setLoginSolver(new LoginSolver() {
                    @Nullable
                    @Override
                    public Object onSolvePicCaptcha(@NotNull Bot bot, @NotNull byte[] bytes, @NotNull Continuation<? super String> continuation) {
                        try {
                            final BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                            return UIHelper.read("Pic Captcha", () -> new JLabel(new ImageIcon(image)));
                        } catch (Exception error) {
                            bot.getLogger().error("Failed to allocate image.", error);
                            return null;
                        }
                    }

                    @Nullable
                    @Override
                    public Object onSolveSliderCaptcha(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                        try {
                            Desktop.getDesktop().browse(URI.create(s));
                            return UIHelper.read("Slider Captcha", null);
                        } catch (Exception ignore) {
                        }
                        return null;
                    }

                    @Nullable
                    @Override
                    public Object onSolveUnsafeDeviceLoginVerify(@NotNull Bot bot, @NotNull String s, @NotNull Continuation<? super String> continuation) {
                        try {
                            return UIHelper.read("SolveUnsafeDeviceLoginVerify", () -> new JTextField(s));
                        } catch (InterruptedException e) {
                            return null;
                        }
                    }
                });
                setReconnectionRetryTimes(5);
                setHeartbeatPeriodMillis(10000L);
                setHeartbeatTimeoutMillis(3000L);
                setReconnectPeriodMillis(3000L);
                setFirstReconnectDelayMillis(5000L);
            }
        });
        initialize(bot);
        Eval.engine.getClass().getClassLoader();
        do {
            bot.login();
            bot.join();
            Logger.getLogger("Bootstrap").log(Level.INFO, "Bot dropped.");
        } while (true);
    }

    public static void accept(MessageEvent event) {
        if (event instanceof GroupMessageEvent) {
            final Iterator<SingleMessage> iterator = event.getMessage().iterator();
            while (iterator.hasNext()) {
                final SingleMessage next = iterator.next();
                if (next instanceof QuoteReply) {
                    final MessageSource source = ((QuoteReply) next).getSource();
                    while (iterator.hasNext()) {
                        final SingleMessage next0 = iterator.next();
                        if (next0 instanceof PlainText) {
                            if (((PlainText) next0).getContent().equalsIgnoreCase("/recall")) {
                                event.getBot().recallAsync(source);
                                event.getBot().recallAsync(event.getMessage());
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (event instanceof GroupMessageEvent) {
            PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(
                    PermissionManager.getPermission(event.getSender().getId(),
                            event.getSubject().getId(),
                            ((GroupMessageEvent) event).getPermission() != MemberPermission.MEMBER)
            );
        } else {
            PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(
                    PermissionManager.getPermission(event.getSender().getId())
            );
        }
        if (Eval.eval(event.getMessage(), event)) return;
        MessageSendEvent ms = new MessageSendEvent(event);
        ms.post();
        if (ms.isCancelled()) return;
        final LinkedList<ArgumentToken> tokens = ArgumentParser.parse(event.getMessage());
        if (!tokens.isEmpty()) {
            final String key = tokens.poll().getAsString().toLowerCase();
            if (key.isEmpty()) return;
            if (!commandPrefixes.get(key.charAt(0))) {
                return;
            }
            final MCommand command = CommandMgr.commands.get(key.substring(1));
            Permissible permissible = PermissionManager.PERMISSIBLE_THREAD_LOCAL.get();
            if (command != null) {
                if (permissible.hasPermission("banned")) {
                    BotSuspendWrap.sendMessage(event.getSubject(), new PlainText("大坏蛋!"));
                    return;
                }
                final String permission = command.permission();
                if (permission != null && !permissible.hasPermission(permission)) {
                    BotSuspendWrap.sendMessage(event.getSubject(), new PlainText("不可以!"));
                    return;
                }
                runCatching(key, event.getSubject(), () -> {
                    PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(permissible);
                    command.$$$$invoke$$(event.getSubject(), event.getSender(), event, tokens, key);
                    return null;
                });
            }
        }
    }

    public static void runCatching(String key, Contact subject, Callable<Void> runnable) {
        try {
            runnable.call();
        } catch (Throwable dump) {
            Logger.getLogger("CommandLogger").log(Level.SEVERE, "Exception in executing command " + key, dump);
            BotSuspendWrap.sendMessage(subject, new PlainText(dump.toString()));
        }
    }

    public static void invokeCommand(String line) {
        if (line == null) return;
        line = line.trim();
        if (line.isEmpty()) return;
        final LinkedList<ArgumentToken> parse = ArgumentParser.parse(line);
        if (parse.isEmpty()) return;
        final MCommand cmd = CommandMgr.commands.get(parse.poll().getAsString().toLowerCase());
        if (cmd == null) {
            System.out.println("Command not found: " + line);
        } else {
            try {
                cmd.$$$$invoke$$(Console.getInstance(), ConsolePacket.INSTANCE.getSender(), ConsolePacket.INSTANCE, parse, "");
            } catch (Throwable dump) {
                Logger.getLogger("CommandLogger").log(Level.SEVERE, "Exception in executing command.", dump);
            }
        }
    }

    public static void initialize(Bot bot) throws NoSuchFieldException {
        Toolkit.Reflection.setObjectValue(null, Bootstrap.class.getField("bot"), bot);
        BootstrapKt.initialize(bot);
        Events.subscribeAlways(MemberLeaveEvent.class, event -> {
            new MemberLeaveGroupEvent(event).post();
        });
        Events.subscribeAlways(MemberJoinEvent.class, event -> {
            new MemberJoinGroupEvent(event).post();
        });
        PluginManager.reload();
        AtomicInteger counter = new AtomicInteger();
        Eval.EvalThreadingManager.pool = new Eval.ThreadPool(task -> {
            Thread t = new Thread(task, "Eval-Thread#" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
        Console.INSTANCE = ConsoleImpl.INSTANCE;
        Thread t = new Thread(() -> {
            PermissionManager.PERMISSIBLE_THREAD_LOCAL.set(new Permissible() {
                @Override
                public Boolean hasPermission0(String permission) {
                    if (permission.equals("banned")) return false;
                    return true;
                }

                @Override
                public boolean hasPermission(String permission) {
                    if (permission.equals("banned")) return false;
                    return true;
                }

                @Override
                public void recalculatePermissions() {
                }

                @Override
                public String getName() {
                    return "CONSOLE";
                }

                @Override
                public Permissible setName(String name) {
                    return null;
                }
            });
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) break;
                    invokeCommand(line);
                } catch (IOException ignore) {
                    break;
                }
            }
        }, "Console Thread");
        t.setDaemon(true);
        t.start();
    }

}
