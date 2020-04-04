/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/20 11:09:59
 *
 * MiraiPlugins/MiraiPlugins/Minecraft.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.Netty;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mxlib.network.IPAddress;
import cn.mcres.karlatemp.mxlib.network.minecraft.MinecraftProtocolHelper;
import com.google.gson.JsonParser;
import io.netty.channel.Channel;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Minecraft implements MCommand {
    @Override
    public void invoke(Contact contact, QQ sender, ContactMessage packet, LinkedList<ArgumentToken> args) {
        if (!args.isEmpty()) {
            switch (args.poll().getAsString()) {
                case "server":
                    if (args.isEmpty()) return;
                    try {
                        final IPAddress address = MinecraftProtocolHelper.parseMinecraftServerAddress(args.poll().getAsString());
                        MinecraftProtocolHelper.ListPingCallback lp = (byteBuf, l, throwable) -> {
                            if (throwable != null) {
                                System.out.println(throwable.toString());
                            } else if (byteBuf != null) {
                                try {
                                    System.out.println(
                                            address + "\n"
                                                    + new TextComponent(ComponentSerializer.parse(new JsonParser().parse(byteBuf.toString(StandardCharsets.UTF_8)).getAsJsonObject().get("description").toString())).toPlainText()
                                    );
                                } catch (NullPointerException ignore) {
                                    System.out.println("服务器信息无法格式化");
                                }
                            } else {
                                System.out.println("服务器信息获取失败");
                            }
                        };
                        final Channel channel = Netty.openChannel(address, lp);
                        if (channel != null)
                            MinecraftProtocolHelper.ping(channel, address.getSourceHost(), address.getSourcePort(), lp, false);
                    } catch (InterruptedException ignore) {
                    }
                    break;
            }
        }
    }
}
