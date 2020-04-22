/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 16:06:56
 *
 * MiraiPlugins/PluginModeRun/Main.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.AsyncExec;
import cn.mcres.karlatemp.mirai.AsyncExecKt;
import cn.mcres.karlatemp.mirai.CommandMgr;
import cn.mcres.karlatemp.mirai.Eval;
import cn.mcres.karlatemp.mirai.event.MessageSendEvent;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import cn.mcres.karlatemp.mirai.plugin.Plugin;
import cn.mcres.karlatemp.mirai.pr.commands.*;
import cn.mcres.karlatemp.mirai.pr.listener.MemberJLListener;
import cn.mcres.karlatemp.mirai.pr.magic.Color;
import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import jdk.nashorn.internal.objects.Global;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;

public class Main extends Plugin implements CoroutineScope {
    public static final String VERSION = "1.0.4";
    @NotNull
    public static final Main INSTANCE = new Main();

    @NotNull
    public static Main getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Main";
    }

    public static final Random random = new SecureRandom();

    @Override
    public void onDisable() {
        Eval.GLOBAL_OVERRIDER = null;
        GroupSettings.cached.invalidateAll();
        try {
            kotlinx.coroutines.CoroutineScopeKt.cancel(this, null);
        } catch (Throwable ignore) {
        }
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
            bindings.put("Control", EvalContext.CONTROL);
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
        CommandMgr.register("image", new ImageC());
        CommandMgr.register("message", new cn.mcres.karlatemp.mirai.pr.commands.Message());
        CommandMgr.register("wk", new WK());
        CommandMgr.register("gc", new Gc());
        CommandMgr.register("group-opt", new GroupOpt());
        CommandMgr.register("marketing", new Marketing());
        TestInitialize.initialize();
        MemberJLListener.register();
        MessageSendEvent.handlers.register(event -> {
            final ContactMessage packet = event.getEvent();
            final String string = packet.getMessage().contentToString().trim();
            if (string.startsWith("/")) return;
            if (string.equals("jd")) {
                if (PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("banned")) {
                    packet.getSubject().sendMessageAsync("不可以!");
                    event.setCancelled(true);
                    return;
                }
                packet.getSubject().sendMessageAsync(JavaDocKt.getJd());
                event.setCancelled(true);
            }
            if (PermissionManager.PERMISSIBLE_THREAD_LOCAL.get().hasPermission("banned")) return;
            GroupSettings groupSettings;
            if (event.getEvent() instanceof GroupMessage) {
                groupSettings = GroupSettings.getSettings(event.getEvent().getSubject().getId());
            } else {
                groupSettings = null;
            }
            color_picker:
            {
                if (groupSettings != null) {
                    if (groupSettings.getBoolean("disable.color-picker")) break color_picker;
                }
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
            bilibili_linker:
            {
                if (groupSettings != null) {
                    if (groupSettings.getBoolean("disable.bilibili-linker")) break bilibili_linker;
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
            }
            word_key:
            {
                if (groupSettings != null) {
                    if (groupSettings.getBoolean("disable.word-key")) break word_key;
                }
                if (!string.isEmpty()) {
                    var first = string.charAt(0);
                    if (first != '/' && first != '#' && first != '$' && first != '>') {
                        var group = 0L;
                        if (packet instanceof GroupMessage) group = ((GroupMessage) packet).getGroup().getId();
                        for (var w : WordKey.allWords.values()) {
                            if (w.group != 0) {
                                if (w.group != group) continue;
                            }
                            if (w.match(string)) {
                                w.send(
                                        packet.getSubject(), packet.getSender()
                                );
                            }
                        }
                    }
                }
            }
            for (Message mg : packet.getMessage()) {
                if (mg instanceof FlashImage) {
                    packet.getSubject().sendMessageAsync(
                            MessageUtils.quote(packet.getMessage())
                                    .plus(MessageUtils.newImage(((FlashImage) mg).getImage().getImageId()))
                    );
                }
            }
        });
        KotlinInitializerKt.initialize();
    }

    private static final CoroutineScope context = AsyncExecKt.INSTANCE.getNewScope();

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return context.getCoroutineContext();
    }
}
