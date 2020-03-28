/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/20 11:06:04
 *
 * MiraiPlugins/MiraiPlugins/Netty.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mxlib.network.IPAddress;
import cn.mcres.karlatemp.mxlib.network.PipelineUtils;
import cn.mcres.karlatemp.mxlib.network.minecraft.MinecraftProtocolHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Netty {
    public static final EventLoopGroup group = PipelineUtils.newEventLoopGroup(50);

    public static Channel openChannel(IPAddress address, MinecraftProtocolHelper.ListPingCallback callback) throws InterruptedException {
        CompletableFuture<Channel> task = new CompletableFuture<>();
        AtomicReference<Channel> reference = new AtomicReference<>();
        reference.set(new Bootstrap().group(group)
                .channel(PipelineUtils.getChannel())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ReadTimeoutHandler(10000L, TimeUnit.MILLISECONDS))
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                        callback.done(null, 0, cause);
                                        ctx.channel().close();
                                    }
                                });
                    }

                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .connect(address.getHost(), address.getPort())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        task.complete(reference.get());
                    } else {
                        callback.done(null, 0, future.cause());
                        task.complete(null);
                    }
                }).channel());
        try {
            return task.get();
        } catch (ExecutionException e) {
            return null;
        }
    }
}
