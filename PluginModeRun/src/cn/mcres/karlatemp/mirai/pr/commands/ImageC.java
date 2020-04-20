/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/03 19:08:26
 *
 * MiraiPlugins/PluginModeRun/ImageC.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.AsyncExec;
import cn.mcres.karlatemp.mirai.Http;
import cn.mcres.karlatemp.mirai.arguments.ArgumentImageToken;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import kotlinx.coroutines.GlobalScope;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.internal.EventInternalJvmKt;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.FriendMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.OnlineImage;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ImageC implements MCommand {
    public static BufferedImage read(Image i) throws IOException {
        if (i instanceof OnlineImage) {
            File f = Http.download("img/image-" + Base64.getEncoder().encodeToString(i.getImageId().getBytes(StandardCharsets.UTF_8)), ((OnlineImage) i).getOriginUrl());
            if (f == null) return null;
            return ImageIO.read(f);
        }
        return null;
    }

    @SuppressWarnings("KotlinInternalInJava")
    public static void waitImage(LinkedList<ArgumentToken> tokens, ContactMessage packet, BiConsumer<BufferedImage, Throwable> image) {
        if (tokens != null) {
            if (!tokens.isEmpty()) {
                final ArgumentToken token = tokens.poll();
                if (token instanceof ArgumentImageToken) {
                    try {
                        image.accept(read(((ArgumentImageToken) token).image), null);
                    } catch (Throwable any) {
                        image.accept(null, any);
                    }
                    return;
                }
            }
        }
        AtomicReference<Listener<?>> listener = new AtomicReference<>();
        long current = System.currentTimeMillis();
        listener.set(EventInternalJvmKt._subscribeEventForJaptOnly(ContactMessage.class, GlobalScope.INSTANCE, c -> {
            if (System.currentTimeMillis() - current > 60000) {
                listener.get().complete();
                return;
            }
            if (packet instanceof GroupMessage) {
                if (c instanceof GroupMessage) {
                    if (c.getSubject().getId() != packet.getSubject().getId()) {
                        return;
                    }
                } else return;
            } else if (packet instanceof FriendMessage) {
                if (!(c instanceof FriendMessage)) {
                    return;
                }
            } else return;
            if (c.getSender().getId() == packet.getSender().getId()) {
                Image image0 = null;
                for (Message m : c.getMessage()) {
                    if (m instanceof Image) {
                        image0 = (Image) m;
                        break;
                    }
                }
                if (image0 != null) {
                    listener.get().complete();
                    try {
                        image.accept(read(image0), null);
                    } catch (Throwable any) {
                        image.accept(null, any);
                    }
                }
            }
        }));
        packet.getSubject().sendMessageAsync("请在1min内发送一张图片..");
    }

    public static BufferedImage toDark(BufferedImage image) {
        BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D graphics = target.createGraphics();
        graphics.drawImage(image, null, 0, 0);
        graphics.dispose();
        return target;
    }

    public static BiConsumer<BufferedImage, Throwable> runProcessing(Contact contact, Function<BufferedImage, BufferedImage> function) {
        return (image, e) -> {
            if (e != null) {
                contact.sendMessageAsync(e.toString());
                return;
            }
            if (image == null) {
                contact.sendMessageAsync("无效图片.");
                return;
            }
            image = function.apply(image);
            if (image == null) {
                contact.sendMessageAsync("处理图片失败");
                return;
            }
            int counter = 3;
            Image image0 = null;
            do {
                try {
                    image0 = contact.uploadImage(image);
                    break;
                } catch (Exception ignore) {
                }
            } while (counter-- > 0);
            if (image0 == null) {
                contact.sendMessageAsync("上传图片失败");
            } else {
                contact.sendMessageAsync(image0);
            }
        };
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull QQ sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) return;
        switch (args.poll().getAsString()) {
            case "gray": {
                waitImage(args, packet, runProcessing(contact, ImageC::toDark));
                break;
            }
            case "gb": {
                if (!PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("command.image.gb")) {
                    contact.sendMessageAsync("不可以!");
                    return;
                }
                waitImage(args, packet, runProcessing(contact, image -> GaussianBlur(image, 16, AsyncExec.service, 50)));
                break;
            }
        }
    }

    public static BufferedImage GaussianBlur(BufferedImage image) {
        return GaussianBlur(image, 20, AsyncExec.service, 10);
    }

    public static BufferedImage GaussianBlur(BufferedImage image, int radix, Executor executor, int nThread) {
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        int width = image.getWidth();
        int height = image.getHeight();
        Color[] colors = new Color[width * height], finish = new Color[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // System.out.println("Invoking x=" + x + ", y=" + y + ", " + ((x * width) + y));
                colors[(x * height) + y] = new Color(image.getRGB(x, y));
            }
        }
        AtomicInteger xAt = new AtomicInteger();
        CompletableFuture<Void> done = new CompletableFuture<>();
        Runnable task = () -> {
            while (true) {
                int x = xAt.getAndIncrement();
                if (x >= width) return;
                for (int y = 0; y < height; y++) {
                    int tot = 0;
                    int r = 0, g = 0, b = 0;
                    for (int xOffset = -radix; xOffset <= radix; xOffset++) {
                        int rx = x + xOffset;
                        if (rx < 0) continue;
                        if (rx >= width) break;
                        int yEnd = radix - Math.abs(xOffset);
                        int xMove = rx * height;
                        for (int yOffset = -yEnd; yOffset <= yEnd; yOffset++) {
                            int ry = y + yOffset;
                            if (ry < 0) continue;
                            if (ry >= height) break;
                            tot++;
                            Color c = colors[xMove + ry];
                            r += c.getRed();
                            g += c.getGreen();
                            b += c.getBlue();
                        }
                    }
                    if (tot > 0) {
                        finish[(x * height) + y] = new Color(r / tot, g / tot, b / tot);
                    }
                }
                if (x + 1 == width) {
                    done.complete(null);
                    return;
                }
            }
        };
        nThread = Math.min(bi.getWidth(), Math.max(1, nThread));
        if (nThread == 1) {
            task.run();
        } else {
            while (nThread-- > 0) executor.execute(task);
        }
        try {
            done.get();
        } catch (InterruptedException | ExecutionException ignore) {
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final Color color = finish[(x * height) + y];
                if (color != null) bi.setRGB(x, y, color.getRGB());
            }
        }
        return bi;
    }
}
