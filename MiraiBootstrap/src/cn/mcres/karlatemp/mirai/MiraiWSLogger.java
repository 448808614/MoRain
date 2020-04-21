/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/23 21:12:11
 *
 * MiraiPlugins/MiraiBootstrap/MiraiWSLogger.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mxlib.network.PipelineUtils;
import cn.mcres.karlatemp.mxlib.tools.InlinePrintStream;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class MiraiWSLogger {
    public Deque<String> logs = new ConcurrentLinkedDeque<>();
    public boolean removeMode = false;
    public final AtomicInteger log_counter = new AtomicInteger();
    public final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void install(int i) {
        MiraiWSLogger logger = new MiraiWSLogger(i);
        PrintStream oldPs = System.out;
        PrintStream ps = new InlinePrintStream() {
            @Override
            public void print(String s) {
                oldPs.println(s);
                logger.newMessage(s);
            }

            @Override
            public void println() {
                oldPs.println();
                logger.newMessage("");
            }

            @Override
            public void println(String x) {
                oldPs.println(x);
                logger.newMessage(x);
            }
        };
        System.setOut(ps);
        System.setErr(ps);
    }

    public void newMessage(String message) {
        logs.add(message);
        if (!removeMode) {
            if (log_counter.getAndIncrement() == 300) {
                removeMode = true;
            }
        } else {
            logs.removeFirst();
        }
        for (Channel c : channels) {
            c.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    public MiraiWSLogger(int port) {
        System.out.println("Mirai Logger Initialized on port " + port);
        AtomicInteger LoggerMiraiCounter = new AtomicInteger();
        new ServerBootstrap().channel(PipelineUtils.getServerChannel())
                .group(PipelineUtils.newEventLoopGroup(60, task -> {
                    Thread t = new Thread(task, "Mirai Logger#" + port + "#" + LoggerMiraiCounter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                                .addLast("http-codec", new HttpServerCodec())
                                .addLast("http-chunked", new ChunkedWriteHandler())
                                .addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 1024))
                                .addLast("ws-protocol", new WebSocketServerProtocolHandler("/logging", null, true, 65535))
                                .addLast("main-handler", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        channels.remove(ctx.channel());
                                    }

                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if (msg instanceof TextWebSocketFrame) {
                                            final String text = ((TextWebSocketFrame) msg).text();
                                            switch (text.charAt(0)) {
                                                case 'l': {
                                                    for (String s : logs) {
                                                        ctx.writeAndFlush(new TextWebSocketFrame(s));
                                                    }
                                                    channels.add(ctx.channel());
                                                    break;
                                                }
                                                case 'i': {
                                                    String command = text.substring(1);
                                                    // Bootstrap.invokeCommand(command);
                                                    break;
                                                }
                                            }
                                        }
                                        if (msg instanceof PingWebSocketFrame) {
                                            ctx.writeAndFlush(new PongWebSocketFrame());
                                        }
                                    }
                                });
                    }
                })
                .bind(port);
    }
}
