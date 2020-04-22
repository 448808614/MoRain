/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * Reserved.FileName: Loggin.java@author: karlatemp@vip.qq.com: 2020/1/5 上午1:16@version: 2.0
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mxlib.MXBukkitLib;
import cn.mcres.karlatemp.mxlib.logging.*;
import cn.mcres.karlatemp.mxlib.tools.InlinePrintStream;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;
import net.mamoe.mirai.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logging {
    public static ILogger logger;
    public static MLoggerHandler handler;
    public static Creator creator;
    public static boolean openFileLogging = true;

    public interface Creator {
        void initialize(Logger logger, Bot bot);
    }

    public static class SyncLogging extends InlinePrintStream {
        private final RandomAccessFile raf;

        public SyncLogging(RandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public synchronized void print(String s) {
            try {
                raf.seek(raf.length());
                raf.write(s.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignore) {
            }
        }

        @Override
        public synchronized void println() {
            try {
                raf.seek(raf.length());
                raf.write('\r');
                raf.write('\n');
            } catch (IOException ignore) {
            }

        }

        @Override
        public synchronized void println(String x) {
            print(x);
            println();
        }
    }

    public synchronized static void install() {
        if (logger != null) return;
        System.setProperty("log4j2.loggerContextFactory", Logging.class.getName());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        PrintStream printOut = System.out;
        try {
            RandomAccessFile raf = new RandomAccessFile("logging.log", "rw");
            Logging.SyncLogging log = new SyncLogging(raf);
            PrintStream oo = printOut;
            printOut = new InlinePrintStream() {
                @Override
                public void print(String s) {
                    if (openFileLogging)
                        log.print(s);
                    oo.print(s);
                }

                @Override
                public void println() {
                    if (openFileLogging)
                        log.println();
                    oo.println();
                }

                @Override
                public void println(String x) {
                    if (openFileLogging)
                        log.println(x);
                    oo.println(x);
                }
            };
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        MXBukkitLib.setLogger(logger = new AsyncLogger(new PrintStreamLogger(
                printOut, new MessageFactoryImpl(),
                new AlignmentPrefixSupplier(
                        (error, line, level, record) -> {
                            if (record != null) return record.getLoggerName();
                            return "null";
                        }
                ) {

                    @NotNull
                    @Override
                    public String get(boolean error, @Nullable String line, @Nullable Level level, @Nullable LogRecord record) {
                        String date = format.format(new Date());
                        do {
                            int p = prln.get();
                            if (p < 20) break;
                            if (prln.compareAndSet(p, 19)) break;
                        } while (true);
                        return '[' + date + "] " + super.get(error, line, level, record);
                    }
                }, printOut, printOut
        ) {
            @Override
            protected void writeLine(String pre, String message, boolean error) {
                var trim = message.trim();
                if (trim.equals("Send done: Heartbeat.Alive") || trim.equals("Event: Heartbeat.Alive.Response") || trim.equals("Packet: Heartbeat.Alive.Response"))
                    return;
                super.writeLine(pre, message, error);
            }
        }, Executors.newSingleThreadExecutor(task -> {
            Thread t = new Thread(task, "Logger writer");
            t.setDaemon(true);
            return t;
        })));
        final Logger rt = Logger.getGlobal().getParent();
        for (Handler h : rt.getHandlers()) {
            rt.removeHandler(h);
        }
        rt.addHandler(handler = new MLoggerHandler(new AbstractBaseLogger(new MessageFactoryImpl()) {
            @Override
            protected void writeLine(String pre, String message, boolean error) {

            }

            @Override
            protected @NotNull
            String getPrefix(boolean error, String line, Level level, LogRecord lr) {
                return "";
            }

            @Override
            public @NotNull
            ILogger publish(LogRecord record, Handler handler) {
                MXBukkitLib.getLogger().publish(record, handler);
                return this;
            }
        }));
        Utils.setDefaultLogger(name -> newLogger(Logger.getLogger(name), true));
        handler.setLevel(Level.ALL);
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                Logger.getLogger("Thread#" + t.getName())
                        .log(Level.SEVERE, "An unknown error caused the thread to stop", e));
    }

    public static MiraiLogger newLogger(Logger logger, boolean initialize) {
        if (initialize) {
            if (creator != null)
                creator.initialize(logger, null);
        }
        return new MiraiLogger() {
            public boolean enabled = true;

            @Nullable
            @Override
            public String getIdentity() {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            private MiraiLogger follower;

            @Nullable
            @Override
            public MiraiLogger getFollower() {
                return follower;
            }

            @Override
            public void setFollower(@Nullable MiraiLogger miraiLogger) {
                follower = miraiLogger;
            }

            @Override
            public void verbose(@Nullable String s) {
                if (!enabled) return;
                if (follower != null) follower.verbose(s);
                logger.log(Level.FINE, s);
            }

            @Override
            public void verbose(@Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.verbose(throwable);
                logger.log(Level.FINE, null, throwable);
            }

            @Override
            public void verbose(@Nullable String s, @Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.verbose(s, throwable);
                logger.log(Level.FINE, s, throwable);
            }

            @Override
            public void debug(@Nullable String s) {
                if (!enabled) return;
                if (follower != null) follower.debug(s);
                logger.finer(s);
            }

            @Override
            public void debug(@Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.debug(throwable);
                logger.log(Level.FINER, null, throwable);
            }

            @Override
            public void debug(@Nullable String s, @Nullable Throwable throwable) {
                if (follower != null) follower.debug(s, throwable);
                logger.log(Level.FINER, s, throwable);
            }

            @Override
            public void info(@Nullable String s) {
                if (!enabled) return;
                if (follower != null) follower.info(s);
                logger.log(Level.INFO, s);
            }

            @Override
            public void info(@Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.info(throwable);
                logger.log(Level.INFO, null, throwable);
            }

            @Override
            public void info(@Nullable String s, @Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.info(s, throwable);
                logger.log(Level.INFO, s, throwable);
            }

            @Override
            public void warning(@Nullable String s) {
                if (!enabled) return;
                if (follower != null) follower.warning(s);
                logger.log(Level.WARNING, s);
            }

            @Override
            public void warning(@Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.warning(throwable);
                logger.log(Level.WARNING, null, throwable);
            }

            @Override
            public void warning(@Nullable String s, @Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.warning(s, throwable);
                logger.log(Level.WARNING, s, throwable);
            }

            @Override
            public void error(@Nullable String s) {
                if (!enabled) return;
                if (follower != null) follower.error(s);
                logger.log(Level.SEVERE, s);
            }

            @Override
            public void error(@Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.error(throwable);
                logger.log(Level.SEVERE, null, throwable);
            }

            @Override
            public void error(@Nullable String s, @Nullable Throwable throwable) {
                if (!enabled) return;
                if (follower != null) follower.error(s, throwable);
                logger.log(Level.SEVERE, s, throwable);
            }

            @NotNull
            @Override
            public <T extends MiraiLogger> T plus(@NotNull T t) {
                setFollower(t);
                return t;
            }

            @Override
            public void plusAssign(@NotNull MiraiLogger miraiLogger) {
                if (follower == null) follower = miraiLogger;
                else follower.plusAssign(miraiLogger);
            }
        };
    }

    public static MiraiLogger newLogger(Bot bot) {
        Logger logger = Logger.getLogger("bot." + bot.getSelfQQ());
        if (creator != null) creator.initialize(logger, bot);
        return newLogger(logger, false);
    }
}
