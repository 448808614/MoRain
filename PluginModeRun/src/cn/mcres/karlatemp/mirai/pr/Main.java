/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 16:06:56
 *
 * MiraiPlugins/PluginModeRun/Main.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.AsyncExec;
import cn.mcres.karlatemp.mirai.CommandMgr;
import cn.mcres.karlatemp.mirai.Eval;
import cn.mcres.karlatemp.mirai.event.MessageSendEvent;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import cn.mcres.karlatemp.mirai.plugin.Plugin;
import cn.mcres.karlatemp.mirai.pr.commands.*;
import cn.mcres.karlatemp.mirai.pr.magic.Color;
import cn.mcres.karlatemp.mxlib.share.$MXBukkitLibConfiguration;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.ScriptObject;
import net.mamoe.mirai.message.MessagePacket;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;

import java.awt.image.BufferedImage;
import java.io.File;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;

public class Main extends Plugin {
    public static final String VERSION = "1.0.3";

    @Override
    public String getName() {
        return "Main";
    }

    public static final Random random = new SecureRandom();

    @Override
    public void onDisable() {
        Eval.GLOBAL_OVERRIDER = null;
    }

    @Override
    public void onEnable() {
        System.out.println("Plugin loading!");
        Eval.GLOBAL_OVERRIDER = (bindings, scriptContext) -> {
            final Global global = Eval.getGlobal(bindings);
            if (global != null) {
                global.delete("load", false);
                global.delete("loadWithNewGlobal", false);
                global.delete("exit", false);
                global.delete("quit", false);
            }
            bindings.put("load", null);
            bindings.put("loadWithNewGlobal", null);
            bindings.put("exit", null);
            bindings.put("quit", null);
            if (PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("magic.eval.unsafe")) {
                bindings.put("GetField", EvalContext.GET_FIELD);
                bindings.put("GetDeclaredField", EvalContext.GET_DECLARED_FIELD);
                bindings.put("ForName", EvalContext.FOR_NAME);
                bindings.put("GetUnsafe", EvalContext.GET_UNSAFE);
                bindings.put("ToJs", EvalContext.TO_JS);
                bindings.put("GetValue", EvalContext.GET_VALUE);
                bindings.put("SetValue", EvalContext.SET_VALUE);
            }
        };
        CommandMgr.register("bilibili", BiliBili.INSTANCE);
        CommandMgr.register("gravatar", new Gravatar());
        CommandMgr.register("hitokoto", new Hitokoto());
        CommandMgr.register("mc", new Minecraft());
        CommandMgr.register("raw", new Raw());
        CommandMgr.register("perm", new Perm());
        CommandMgr.register("perm-test", new TestPerm());
        CommandMgr.register("s-logging", new Logging());
        CommandMgr.register("about", new Version());
        TestInitialize.initialize();
        MessageSendEvent.handlers.register(event -> {
            final MessagePacket<?, ?> packet = event.getEvent();
            final String string = packet.getMessage().toString().trim();
            if (packet.getMessage().toString().startsWith("/")) return;
            if (packet.getMessage().eq("jd")) {
                if (PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("banned")) {
                    packet.getSubject().sendMessageAsync("不可以!");
                    event.setCancelled(true);
                    return;
                }
                packet.getSubject().sendMessageAsync("" +
                        "Java8: https://docs.oracle.com/javase/8/docs/api/overview-summary.html\n" +
                        "BukkitAPI - Javadoc:\n" +
                        "    1.7.10版(已过时):https://jd.bukkit.org/\n" +
                        "    Chinese_Bukkit: \n" +
                        "        1.12.2版:http://docs.zoyn.top/bukkitapi/1.12.2/\n" +
                        "        1.13+版:https://bukkit.windit.net/javadoc/\n" +
                        "    Spigot: https://hub.spigotmc.org/javadocs/spigot/\n" +
                        "    Paper: https://papermc.io/javadocs/paper/\n" +
                        "    Sponge(不推荐): https://docs.spongepowered.org/stable/zh-CN/\n" +
                        "    BungeeCord:\n" +
                        "        API: https://ci.md-5.net/job/BungeeCord/ws/api/target/apidocs/overview-summary.html\n" +
                        "        API-Chat: https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/overview-summary.html\n" +
                        "    MCP Query: https://mcp.exz.me/"
                );
                event.setCancelled(true);
            }
            if (PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("banned")) return;
            {
                final java.awt.Color color = Color.match(string);
                if (color != null) {
                    AsyncExec.service.execute(() -> {
                        int count = 3;
                        final BufferedImage bufferedImage = Color.build(color);
                        Image image = null;
                        do {
                            try {
                                image = packet.getSubject().uploadImage(
                                        bufferedImage
                                );
                                break;
                            } catch (Exception ignore) {
                            }
                        } while (count-- > 0);
                        if (image == null)
                            packet.getSubject().sendMessageAsync("无法发送图片 " + string.substring(1));
                        else
                            packet.getSubject().sendMessageAsync(
                                    new PlainText(string.substring(1)).plus(image)
                            );
                    });
                    event.setCancelled(true);
                }
            }
            {
                final Matcher matcher = BiliBili.checker.matcher(string);
                if (matcher.find()) {
                    BiliBili.INSTANCE.invoke(packet.getSubject(), packet.getSender(), packet, BiliBili.build(matcher.group(2)));
                    event.setCancelled(true);
                }
            }
            {
                final Matcher matcher = BiliBili.b23.matcher(string);
                if (matcher.find()) {
                    BiliBili.INSTANCE.invoke(packet.getSubject(), packet.getSender(), packet, BiliBili.build(matcher.group(1)));
                    event.setCancelled(true);
                }
            }
            if (packet.getMessage().contains("开车")) {
                packet.getSubject().sendMessageAsync(
                        packet.getSubject().uploadImage(new File("x509/Dr.jpg"))
                );
                event.setCancelled(true);
            }
            if (packet.getMessage().contains("颜色")) {
                packet.getSubject().sendMessageAsync(
                        packet.getSubject().uploadImage(new File("x509/Color.jpg"))
                );
                event.setCancelled(true);
            }
            if (packet.getMessage().contains("上图")) {
                packet.getSubject().sendMessageAsync(
                        packet.getSubject().uploadImage(new File("x509/NoJavaWithBukkit.jpg"))
                );
                event.setCancelled(true);
            }
            if (packet.getMessage().contains("运行错误")) {
                packet.getSubject().sendMessageAsync(
                        packet.getSubject().uploadImage(new File("x509/RuntimeError.jpg"))
                );
                event.setCancelled(true);
            }
            if (packet.getMessage().contains("怎么") || packet.getMessage().contains("为什么")) {
                packet.getSubject().sendMessageAsync(
                        packet.getSubject().uploadImage(new File(
                                random.nextDouble() < 0.1 ? "x509/UnknownEgg.jpg" : "x509/Unknown.jpg"
                        ))
                );
                event.setCancelled(true);
            }
        });
    }
}
