/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/30 18:40:09
 *
 * MiraiPlugins/BiliBiliLinker/BiliBiliLinker.java
 */

package cn.mcres.karlatemp.mirai.bbl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.event.internal.EventInternalJvmKt;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.FriendMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.RichMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiliBiliLinker extends PluginBase {
    static BiliBiliLinker INSTANCE;
    static File tempFolder;

    public BiliBiliLinker() {
        INSTANCE = this;
    }

    public static final Collection<Predicate<ContactMessage>> testers = new LinkedList<>();

    public static boolean allow(ContactMessage packet) {
        synchronized (testers) {
            for (Predicate<ContactMessage> p : testers) {
                if (!p.test(packet)) return false;
            }
        }
        return true;
    }


    public static void callback(ByteArrayOutputStream stream, ContactMessage event) throws Throwable {
        final JsonObject bin = JsonParser
                .parseString(stream.toString(StandardCharsets.UTF_8)).getAsJsonObject();
        switch (bin.get("code").getAsInt()) {
            case 0: {
                final JsonObject data = bin.get("data").getAsJsonObject();
                String title = data.get("title").getAsString(),
                        image = data.get("pic").getAsString() + "@448w_252h_1c_100q.jpg";
                MessageChainBuilder builder = new MessageChainBuilder();
                File file = AsyncHttp.download(String.valueOf(data.get("bvid").getAsString()), image);
                if (file == null) builder.add("无法获取图片(下载错误).\n\n");
                else {
                    try {
                        builder.add(event.getSubject().uploadImage(file));
                    } catch (Throwable ignore) {
                        builder.add("上传图片失败，请等下再试\n");
                    }
                }
                builder.add(title + "\n");
                String desc;
                {
                    final JsonElement element = data.get("desc");
                    if (element == null) desc = "";
                    else desc = element.getAsString().trim();
                    if (desc.length() > 15) {
                        desc = desc.substring(0, 15) + "...";
                    }
                    if (desc.isEmpty()) desc = title;
                }
                final JsonObject stat = data.get("stat").getAsJsonObject();
                builder.add("Up>> " + data.get("owner").getAsJsonObject().get("name").getAsString() + "\n");
                builder.add("Aid>> " + data.get("aid") + "\n");
                builder.add("Bid>> " + data.get("bvid").getAsString() + "\n");
                builder.add("弹幕>> " + stat.get("danmaku") + "\n");
                builder.add("评论>> " + stat.get("reply") + "\n");
                builder.add("硬币>> " + stat.get("coin") + "\n");
                builder.add("收藏>> " + stat.get("favorite") + "\n");
                builder.add("分享>> " + stat.get("share") + "\n");
                builder.add("点赞>> " + stat.get("like") + "\n");
                builder.add("不喜欢>> " + stat.get("dislike"));
                event.getSubject().sendMessageAsync(builder.asMessageChain());
                event.getSubject().sendMessageAsync(
                        RichMessage.Templates.share("https://www.bilibili.com/video/" + data.get("bvid").getAsString(), title, desc, image)
                );
                break;
            }
            default:
                event.getSubject().sendMessageAsync("BiliBili > " + bin.get("message").getAsString());
        }
    }

    public static String make_check(String id) {
        if (id.startsWith("BV")) {
            return "https://api.bilibili.com/x/web-interface/view?bvid=" + id;
        }
        if (id.startsWith("av")) {
            return "https://api.bilibili.com/x/web-interface/view?aid=" + id.substring(2);
        }
        return "https://api.bilibili.com/x/web-interface/view?aid=" + id;
    }

    private static final Pattern p1 = Pattern.compile(
            "^https://(www\\.|)bilibili\\.com/video/([A-Za-z0-9]+)(/.*|\\?.*|)$"
    );
    private static final Pattern p2 = Pattern.compile(
            "^https://b23\\.tv/([0-9A-Za-z]+)(/.*|\\?.*|)$"
    );

    private void handle(ContactMessage event) {
        if (!allow(event)) return;
        final String match = event.getMessage().contentToString();
        {
            final Matcher matcher = p1.matcher(match);
            if (matcher.find()) {
                AsyncHttp.run(make_check(matcher.group(2)), stream -> callback(stream, event));
            }
        }
        {
            final Matcher matcher = p2.matcher(match);
            if (matcher.find()) {
                AsyncHttp.run(make_check(matcher.group(1)), stream -> callback(stream, event));
            }
        }
    }

    @SuppressWarnings("KotlinInternalInJava")
    @Override
    public void onEnable() {
        tempFolder = new File(getDataFolder(), "temp");
        ConfigurationLoader.reload();
        EventInternalJvmKt._subscribeEventForJaptOnly(GroupMessage.class, this, this::handle);
        EventInternalJvmKt._subscribeEventForJaptOnly(FriendMessage.class, this, this::handle);
    }

    @Override
    public void onDisable() {
        AsyncHttp.service.shutdownNow();
    }
}
