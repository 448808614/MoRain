/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 16:06:56
 *
 * MiraiPlugins/PluginModeRun/Main.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.*;
import cn.mcres.karlatemp.mirai.event.MessageSendEvent;
import cn.mcres.karlatemp.mirai.permission.PermissionManager;
import cn.mcres.karlatemp.mirai.plugin.Plugin;
import cn.mcres.karlatemp.mirai.pr.commands.Logging;
import cn.mcres.karlatemp.mirai.pr.commands.*;
import cn.mcres.karlatemp.mirai.pr.listener.MemberJLListener;
import cn.mcres.karlatemp.mirai.pr.magic.Color;
import com.google.gson.JsonParser;
import jdk.nashorn.internal.objects.Global;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.*;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;

public class Main extends Plugin implements CoroutineScope {
    public static final String VERSION = "1.0.4";
    @NotNull
    public static final Main INSTANCE = new Main();
    public static final Random random = new SecureRandom();
    private static final CoroutineScope context = AsyncExecKt.INSTANCE.getNewScope();

    @NotNull
    public static Main getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getDescription() {
        return "MoRain 核心";
    }

    @Override
    public void onDisable() {
        Eval.GLOBAL_OVERRIDER = null;
        GroupSettings.cached.invalidateAll();
        CoreDisableEvent.INSTANCE.post();
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
        CommandMgr.register("raw", new Raw());
        CommandMgr.register("perm", new Perm());
        CommandMgr.register("perm-test", new TestPerm());
        CommandMgr.register("s-logging", new Logging());
        CommandMgr.register("image", new ImageC());
        CommandMgr.register("message", new cn.mcres.karlatemp.mirai.pr.commands.Message());
        CommandMgr.register("wk", new WK());
        CommandMgr.register("group-opt", new GroupOpt());
        TestInitialize.initialize();
        MemberJLListener.register();
        MessageSendEvent.handlers.register(event -> {
            var packet = event.getEvent();
            final String string = packet.getMessage().contentToString().trim();
            if (string.isEmpty()) return;
            if (Bootstrap.commandPrefixes.get(string.charAt(0))) return;
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
            if (event.getEvent() instanceof GroupMessageEvent) {
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
                // Json App Parsing
                {
                    var app = packet.getMessage().first(LightApp.Key);
                    if (app != null) {
                        try {
                            var object = JsonParser.parseString(app.getContent()).getAsJsonObject();
                            if (object.get("app").getAsString().equals("com.tencent.miniapp_01")) {
                                var detail_1 = object.getAsJsonObject("meta").getAsJsonObject("detail_1");
                                System.out.println(detail_1);
                                if (detail_1.get("appid").getAsString().equals("1109937557")) {
                                    if (detail_1.get("qqdocurl") == null) break bilibili_linker;
                                    var link = detail_1.get("qqdocurl").getAsString();
                                    var request = SimpleHttpRequest.copy(new HttpGet(link));
                                    // user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36
                                    // accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
                                    request.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
                                    request.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                                    Http.client.execute(request, new FutureCallback<>() {
                                        @Override
                                        public void completed(SimpleHttpResponse response) {
                                            if (response.getCode() == 200) {
                                                // vikingrMCCache: 137406bv-productionvideoplay-index-av95758070-bv-productionvideoplay-page-av95758070
                                                var header = response.getFirstHeader("vikingrMCCache");
                                                if (header != null) {
                                                    var header0 = header.getValue();
                                                    var splitter = header0.lastIndexOf('-');
                                                    if (splitter == -1) return;
                                                    var av = header0.substring(splitter + 1);
                                                    BiliBili.INSTANCE.invoke(packet.getSubject(), packet.getSender(), packet, BiliBili.build(av));
                                                }
                                            } else {
                                                System.out.println(response.getCode());
                                                System.out.println(response.getBodyText());
                                            }
                                        }

                                        @Override
                                        public void failed(Exception e) {
                                        }

                                        @Override
                                        public void cancelled() {

                                        }
                                    });
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        // {
                        //    "app": "com.tencent.miniapp_01",
                        //    "config": {
                        //        "autoSize": 0,
                        //        "ctime": 1588572964,
                        //        "forward": 1,
                        //        "height": 0,
                        //        "token": "a7f5c1fb7985421f6f3c44db0ad1a45e",
                        //        "type": "normal",
                        //        "width": 0
                        //    },
                        //    "desc": "哔哩哔哩",
                        //    "meta": {
                        //        "detail_1": {
                        //            "appid": "1109937557",
                        //            "desc": "【红石音乐】Battle Against a True Hero【传说之下】",
                        //            "host": {
                        //                "nick": "夏雨",
                        //                "uin": 3390038158
                        //            },
                        //            "icon": "http://miniapp.gtimg.cn/public/appicon/432b76be3a548fc128acaa6c1ec90131_200.jpg",
                        //            "preview": "pubminishare-30161.picsz.qpic.cn/94413313-47f9-4b77-aaef-b8aed251e87e",
                        //            "qqdocurl": "https://b23.tv/6X0xwU?share_medium=android&share_source=qq&bbid=XZ3F75C3685EA465BF2C6A177DD7C8CB3E234&ts=1588572950970",
                        //            "scene": 1036,
                        //            "shareTemplateData": {},
                        //            "shareTemplateId": "8C8E89B49BE609866298ADDFF2DBABA4",
                        //            "title": "哔哩哔哩",
                        //            "url": "m.q.qq.com/a/s/677bd32ad82db58f50f9ad600b7c0b0b"
                        //        }
                        //    },
                        //    "needShareCallBack": false,
                        //    "prompt": "[QQ小程序]哔哩哔哩",
                        //    "ver": "1.0.0.19",
                        //    "view": "view_8C8E89B49BE609866298ADDFF2DBABA4"
                        //}
                    }
                }
            }
            word_key:
            {
                if (groupSettings != null) {
                    if (groupSettings.getBoolean("disable.word-key")) break word_key;
                }
                var first = string.charAt(0);
                if (!Bootstrap.commandPrefixes.get(first)) {
                    var group = 0L;
                    if (packet instanceof GroupMessageEvent) group = ((GroupMessageEvent) packet).getGroup().getId();
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

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return context.getCoroutineContext();
    }
}
