/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/30 16:20:53
 *
 * MiraiPlugins/MiraiBootstrap/AsyncExec.java
 */

package cn.mcres.karlatemp.mirai;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncExec {
    private static final AtomicInteger counter = new AtomicInteger();
    public static final ExecutorService service = Executors.newScheduledThreadPool(10,
            task -> {
                Thread t = new Thread(task, "Async Exec#" + counter.getAndIncrement());
                t.setDaemon(true);
                Logger logger = Logger.getLogger(t.getName());
                t.setUncaughtExceptionHandler((t1, e) -> logger.log(Level.SEVERE, "Exception in " + t.getName(), e));
                return t;
            }
    );
}
