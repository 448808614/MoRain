/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/19 19:45:47
 *
 * MiraiPlugins/MiraiPlugins/Hitokoto.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.Http;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mxlib.tools.URLEncoder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.MessagePacket;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.function.Consumer;

public class Hitokoto implements MCommand {
    public static void hitokoto(String param, Consumer<String> sender) {
        Http.hitokoto(param, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                //TEMPLATE 『%s』 - 「%s」
                String msg = null;
                String from = null;
                try {
                    JsonObject jo = JsonParser.parseString(new String(response.getBodyBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                    msg = jo.get("hitokoto").getAsString();
                    from = jo.get("from").getAsString();
                } catch (Throwable ignore) {
                }
                if (msg == null || from == null) {
                    msg = "这边没有任何可用数据";
                    from = "Karlatemp";
                }
                sender.accept("『" + msg + "』 - 「" + from + "」");
            }

            @Override
            public void failed(Exception e) {
                if (e instanceof SocketTimeoutException)
                    sender.accept("『连接超时什么的, 才不会发生』 - 「Karlatemp」");
                else if (e instanceof UnknownHostException)
                    sender.accept("『才, 才不是找不到服务器了呢』 - 「Karlatemp」");
                else if (e instanceof ConnectException)
                    sender.accept("『绝，绝对不是连接不到服务器了呢！！哼』 - 「Karlatemp」");
                else sender.accept("『绝对没有发生错误！绝对！』 - 「Karlatemp」");
            }

            @Override
            public void cancelled() {
                sender.accept("『500 Task cancelled』 - 「Karlatemp」");
            }
        });
    }

    @Override
    public void invoke(Contact contact, QQ sender, MessagePacket<?, ?> packet, LinkedList<ArgumentToken> args) {
        StringBuilder param = null;
        if (!args.isEmpty()) {
            param = new StringBuilder("c=" + URLEncoder.encode(args.poll().getAsString(), StandardCharsets.UTF_8));
        }
        while (!args.isEmpty()) {
            param.append("&c=").append(URLEncoder.encode(args.poll().getAsString(), StandardCharsets.UTF_8));
        }
        hitokoto(param == null ? null : param.toString(), contact::sendMessageAsync);
    }
}
