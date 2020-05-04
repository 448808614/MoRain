/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/19 21:19:16
 *
 * MiraiPlugins/MiraiPlugins/BiliBili.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.Http;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.RichMessage;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class BiliBili implements MCommand {
    public static final BiliBili INSTANCE = new BiliBili();
    public static final Pattern checker = Pattern.compile(
            "^https://(www\\.|)bilibili\\.com/video/([A-Za-z0-9]+)(/.*|\\?.*|)$"
    ), b23 = Pattern.compile("^https://b23\\.tv/([0-9A-Za-z]+)(/.*|\\?.*|)$");

    /*
table='fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF'
tr={}
for i in range(58):
	tr[table[i]]=i
*/
    private static final char[] table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF".toCharArray();
    private static final int[] tr = new int[125];
    /*
    s=[11,10,3,8,4,6,2,9,5,7]
    xor=177451812
    add=100618342136696320
    */
    private static final int[] s = new int[]{11, 10, 3, 8, 4, 6, 2, 9, 5, 7};
    private static final long xor = 177451812L;
    private static final BigInteger xor0 = BigInteger.valueOf(xor);
    private static final long add = 100618342136696320L;
    private static final BigInteger add0 = BigInteger.valueOf(add);
    private static final BigInteger base = BigInteger.valueOf(58);

    static {
        /*
for i in range(58):
	tr[table[i]]=i
	*/
        for (int i = 0; i < 58; i++) {
            tr[table[i]] = i;
        }
    }

    public static long dec(char[] x) {
/*
def dec(x):
	r=0
	for i in range(10):
		r+=tr[x[s[i]]]*58**i
	return (r-add)^xor
*/
        BigInteger ret = BigInteger.valueOf(0);
        for (int i = 0; i < 10; i++) {
            ret = ret.add(base.pow(i).multiply(BigInteger.valueOf(tr[x[s[i]]])));
        }
        return ret.subtract(add0).xor(xor0).longValue();
    }

    public static long dec(String x) {
        return dec(x.toCharArray());
    }

    public static char[] enc(long x) {
        BigInteger integer = BigInteger.valueOf(x);
        integer = integer.xor(xor0).add(add0);
        /*
def enc(x):
	x=(x^xor)+add
	r=list('BV          ')
	for i in range(10):
		r[s[i]]=table[x//58**i%58]
	return ''.join(r)
        */
//        x = (x ^ xor) + add;
        char[] r = new char[12];
        r[0] = 'B';
        r[1] = 'V';
        for (int i = 0; i < 10; i++) {
            // (x / pow(58, i)) % 58
            r[s[i]] = table[
                    integer.divide(base.pow(i)).mod(base).intValue()
//                    (int) ((x / Math.pow(58, i)) % 58)
                    ];
        }
        //r[s[i]] = table[(int) (long) (x / Math.pow(58, i) % 58)];
//            r[s[i]] = table[(int) (long) (x / Math.pow(58, i) % 58)];
        return r;
    }

    /*
    table='fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF'
tr={}
for i in range(58):
	tr[table[i]]=i
s=[11,10,3,8,4,6,2,9,5,7]
xor=177451812
add=100618342136696320

def dec(x):
	r=0
	for i in range(10):
		r+=tr[x[s[i]]]*58**i
	return (r-add)^xor

def enc(x):
	x=(x^xor)+add
	r=list('BV          ')
	for i in range(10):
		r[s[i]]=table[x//58**i%58]
	return ''.join(r)
    */
    public static LinkedList<ArgumentToken> build(String group) {
        LinkedList<ArgumentToken> tok = new LinkedList<>();
        tok.add(new ArgumentToken("vid"));
        tok.add(new ArgumentToken(group));
        return tok;
    }

    private static void cutDesc(String fullDesc, StringBuilder buffer) {
        if (fullDesc.length() > 100) buffer.append(fullDesc, 0, 100).append("...");
        else buffer.append(fullDesc);
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

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        if (args.isEmpty()) {
            contact.sendMessageAsync("" +
                    "BiliBili>\n" +
                    "/bilibili vid <av/bv> 查询视频信息\n" +
                    "/bilibili cov <av/bv> 计算转换");
        } else {
            switch (args.poll().getAsString()) {
                case "vid": {
                    if (args.isEmpty()) {
                        contact.sendMessageAsync("/bilibili vid <av号>");
                        break;
                    }
                    Http.client.execute(SimpleHttpRequest.copy(new HttpGet(make_check(args.poll().getAsString()))), new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(SimpleHttpResponse response) {
                            try {
                                final JsonObject bin = JsonParser.parseString(new String(response.getBodyBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                                switch (bin.get("code").getAsInt()) {
                                    case 0: {
                                        final JsonObject data = bin.get("data").getAsJsonObject();
                                        String title = data.get("title").getAsString(),
                                                image = data.get("pic").getAsString() + "@448w_252h_1c_100q.jpg";
                                        MessageChainBuilder builder = new MessageChainBuilder();
                                        File file = Http.download(String.valueOf(data.get("bvid").getAsString()), image);
                                        if (file == null) builder.add("无法获取图片(下载错误).\n\n");
                                        else {
                                            try {
                                                builder.add(contact.uploadImage(file));
                                            } catch (Throwable ignore) {
                                                builder.add("上传图片失败，请等下再试\n");
                                            }
                                        }
                                        StringBuilder buffer = new StringBuilder();
                                        buffer.append(title).append("\n");
                                        String desc, fullDesc;
                                        {
                                            final JsonElement element = data.get("desc");
                                            if (element == null) fullDesc = desc = "";
                                            else fullDesc = desc = element.getAsString().trim();
                                            if (desc.length() > 15) {
                                                desc = desc.substring(0, 15) + "...";
                                            }
                                            if (desc.isEmpty()) desc = title;
                                        }
                                        final JsonObject stat = data.get("stat").getAsJsonObject();
                                        buffer.append("Up>> ").append(data.get("owner").getAsJsonObject().get("name").getAsString()).append("\n");
                                        buffer.append("Aid>> ").append(data.get("aid").getAsString()).append("\n");
                                        buffer.append("Bid>> ").append(data.get("bvid").getAsString()).append("\n");
                                        buffer.append("播放>> ").append(stat.get("view").getAsString()).append("\n");
                                        buffer.append("弹幕>> ").append(stat.get("danmaku").getAsString()).append("\n");
                                        buffer.append("评论>> ").append(stat.get("reply").getAsString()).append("\n");
                                        buffer.append("硬币>> ").append(stat.get("coin").getAsString()).append("\n");
                                        buffer.append("收藏>> ").append(stat.get("favorite").getAsString()).append("\n");
                                        buffer.append("分享>> ").append(stat.get("share").getAsString()).append("\n");
                                        buffer.append("点赞>> ").append(stat.get("like").getAsString()).append("\n");
                                        buffer.append("不喜欢>> ").append(stat.get("dislike").getAsString());
                                        if (packet instanceof GroupMessage) {
                                            buffer.append("\n=================================\n");
                                            cutDesc(fullDesc, buffer);
                                        }
                                        builder.add(buffer.toString());
                                        contact.sendMessageAsync(
                                                RichMessage.Templates.share("https://www.bilibili.com/video/av" + bin.get("aid"), title, desc, image)
                                        );
                                        try {
                                            contact.sendMessage(builder.asMessageChain());
                                        } catch (Throwable ignore) {
                                            contact.sendMessage(new ExtendedMessageChain(builder.asMessageChain()));
                                        }
                                        break;
                                    }
                                    default:
                                        contact.sendMessageAsync("BiliBili > " + bin.get("message").getAsString());
                                }
                            } catch (Throwable exception) {
                                exception.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Exception e) {
                            contact.sendMessageAsync("BiliBili > 无法获取视频信息！");
                        }

                        @Override
                        public void cancelled() {

                        }
                    });
                    break;
                }
                case "cov": {
                    Throwable error0 = null, error1 = null;
                    ArgumentToken tok = args.poll();
                    if (tok == null) {
                        contact.sendMessageAsync("BiliBili> 错误参数\n" +
                                "/bilibili cov [Aid/Bid]");
                        break;
                    }
                    try {
                        long getAs = tok.getAsLong();
                        if (getAs != 0) {
                            contact.sendMessageAsync(
                                    "BiliBili> Aid 2 Bid > " + getAs + " -> " + String.valueOf(enc(getAs))
                            );
                            break;
                        }
                    } catch (Throwable thr) {
                        error0 = thr;
                    }
                    try {
                        contact.sendMessageAsync(
                                "BiliBili> Bid 2 Aid > " + tok.getAsString() + " -> " + dec(tok.getAsString().toCharArray())
                        );
                        break;
                    } catch (Throwable thr) {
                        error1 = thr;
                    }
                    contact.sendMessageAsync("BiliBili > 转换错误: " + error0 + "\n" + error1);
                    break;
                }
            }
        }
    }
}
