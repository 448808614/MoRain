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
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase;
import net.mamoe.mirai.utils.Utils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Logging {
    public static ILogger logger;
    public static MLoggerHandler handler;
    public static Creator creator;
    public static boolean openFileLogging = true;
    private static final Pattern dropper = Pattern.compile("\\033\\[[0-9;]*?m");
    private static final Map<Level, String> lv_ansi = Map.of(
            Level.SEVERE, new Ansi().fg(Ansi.Color.RED).a("SEVERE").reset().toString(),
            Level.INFO, new Ansi().fg(Ansi.Color.CYAN).a("INFO").reset().toString(),
            Level.WARNING, new Ansi().fg(Ansi.Color.YELLOW).a("WARNING").reset().toString(),
            Level.FINER, new Ansi().fg(Ansi.Color.MAGENTA).a("FINER").reset().toString(),
            Level.FINE, new Ansi().fg(Ansi.Color.MAGENTA).a("FINE").reset().toString(),
            Level.FINEST, new Ansi().fg(Ansi.Color.MAGENTA).a("FINEST").reset().toString()
    );

    private static boolean filter(String message) {
        String trim = message.trim();
        return trim.equals("Send done: Heartbeat.Alive")
                || trim.contains("Heartbeat.Alive")
                ;
    }

    public synchronized static void install() throws FileNotFoundException {
        if (logger != null) return;
        System.setProperty("log4j2.loggerContextFactory", Logging.class.getName());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
        RandomAccessFile raf = new RandomAccessFile("logging.log", "rw");
        Logging.SyncLogging log = new SyncLogging(raf);
        var logging_logger = new PrintStreamLogger(log, new MessageFactoryImpl(), new AlignmentPrefixSupplier(
                (error, line, level, record) -> {
                    if (record != null) {
                        return record.getLoggerName();
                    }
                    return "null";
                }
        ) {
            @NotNull
            @Override
            public String get(boolean error, @Nullable String line, @Nullable Level level, @Nullable LogRecord record) {
                var date = new Date();
                do {
                    int p = prln.get();
                    if (p < 20) break;
                    if (prln.compareAndSet(p, 19)) break;
                } while (true);
                return '[' + format1.format(date) + ' ' + format.format(date) + "] " + super.get(error, line, level, record);
            }
        }, log, log) {
            @Override
            protected void writeLine(String pre, String message, boolean error) {
                if (!Logging.openFileLogging) return;
                if (filter(message)) {
                    return;
                }
                super.writeLine(pre, message, error);
            }
        };
        var console_logger = new PrintStreamLogger(System.out, new MessageFactoryAnsi(), new AlignmentPrefixSupplier(
                (error, line, level, record) -> {
                    if (record != null) {
                        return record.getLoggerName();
                    }
                    return "null";
                }
        ) {
            @Override
            protected int getCharsFontWidth(@NotNull String chars) {
                return super.getCharsFontWidth(dropper.matcher(chars).replaceAll(""));
            }

            @NotNull
            @Override
            protected String valueOf(@Nullable Level lv) {
                final var s = lv_ansi.get(lv);
                if (s != null) return s;
                return super.valueOf(lv);
            }

            @NotNull
            @Override
            public String get(boolean error, @Nullable String line, @Nullable Level level, @Nullable LogRecord record) {
                var date = new Date();
                do {
                    int p = prln.get();
                    if (p < 20) break;
                    if (prln.compareAndSet(p, 19)) break;
                } while (true);
                return new Ansi().reset()
                        .a('[').fgBrightYellow().a(format1.format(date)).a(' ')
                        .fgBrightCyan().a(format.format(date)).reset().a("] ")
                        .a(super.get(error, line, level, record)).reset().toString();
            }
        }, System.out, System.out) {
            @Override
            protected void writeLine(String pre, String message, boolean error) {
                if (filter(dropper.matcher(message).replaceAll(""))) {
                    return;
                }
                super.writeLine(pre, message, error);
            }
        };

        MXBukkitLib.setLogger(logger = new AsyncLogger(new MLogger(console_logger, logging_logger), Executors.newSingleThreadExecutor(task -> {
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
        return new MiraiLoggerPlatformBase() {

            @Nullable
            @Override
            public String getIdentity() {
                return null;
            }

            @Override
            public void verbose0(@Nullable String s) {
                logger.log(Level.FINE, s);
            }

            @Override
            public void verbose(@Nullable Throwable throwable) {
                logger.log(Level.FINE, null, throwable);
            }

            @Override
            public void verbose0(@Nullable String s, @Nullable Throwable throwable) {
                logger.log(Level.FINE, s, throwable);
            }

            @Override
            public void debug0(@Nullable String s) {
                logger.finer(s);
            }

            @Override
            public void debug(@Nullable Throwable throwable) {
                logger.log(Level.FINER, null, throwable);
            }

            @Override
            public void debug0(@Nullable String s, @Nullable Throwable throwable) {
                logger.log(Level.FINER, s, throwable);
            }

            @Override
            public void info0(@Nullable String s) {
                logger.log(Level.INFO, s);
            }

            @Override
            public void info0(@Nullable String s, @Nullable Throwable throwable) {
                logger.log(Level.INFO, s, throwable);
            }

            @Override
            public void warning0(@Nullable String s) {
                logger.log(Level.WARNING, s);
            }

            @Override
            public void warning0(@Nullable String s, @Nullable Throwable throwable) {
                logger.log(Level.WARNING, s, throwable);
            }

            @Override
            public void error0(@Nullable String s) {
                logger.log(Level.SEVERE, s);
            }

            @Override
            public void error0(@Nullable String s, @Nullable Throwable throwable) {
                logger.log(Level.SEVERE, s, throwable);
            }
        };
    }

    public static MiraiLogger newLogger(Bot bot) {
        Logger logger = Logger.getLogger("bot." + bot.getSelfQQ().getId());
        if (creator != null) creator.initialize(logger, bot);
        return newLogger(logger, false);
    }

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
}
