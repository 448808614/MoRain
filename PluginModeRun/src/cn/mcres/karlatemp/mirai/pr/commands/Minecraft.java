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
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import io.netty.channel.Channel;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Minecraft implements MCommand {
    public static final Gson g = new Gson();

    public static class MessageSerializer implements JsonDeserializer<BaseComponent[]> {
        private static final Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseComponent.class, new ComponentSerializer())
                .registerTypeAdapter(TextComponent.class, new TextComponentSerializer())
                .registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer())
                .create();

        @Override
        public BaseComponent[] deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonElement.isJsonArray() ? gson.fromJson(jsonElement, BaseComponent[].class) : new BaseComponent[]{gson.fromJson(jsonElement, BaseComponent.class)};
        }
    }

    public static class Desc {
        @JsonAdapter(MessageSerializer.class)
        public BaseComponent[] description;
    }

    @Override
    public void invoke(Contact contact, QQ sender, ContactMessage packet, LinkedList<ArgumentToken> args) {
        if (!args.isEmpty()) {
            switch (args.poll().getAsString()) {
                case "server":
                    if (args.isEmpty()) return;
                    final IPAddress address = MinecraftProtocolHelper.parseMinecraftServerAddress(args.poll().getAsString());
                    MinecraftProtocolHelper.ListPingCallback lp = (byteBuf, l, throwable) -> {
                        if (throwable != null) {
                            contact.sendMessageAsync(throwable.toString());
                        } else if (byteBuf != null) {
                            try {
                                contact.sendMessageAsync(
                                        address + "\n"
                                                + new TextComponent(
                                                g.fromJson(JsonParser.parseString(byteBuf.toString(StandardCharsets.UTF_8)), Desc.class).description
                                        ).toPlainText()
                                );
                            } catch (NullPointerException ignore) {
                                contact.sendMessageAsync("服务器信息无法格式化");
                            }
                        } else {
                            contact.sendMessageAsync("服务器信息获取失败");
                        }
                    };
                    try {
                        final Channel channel = Netty.openChannel(address, lp);
                        if (channel != null)
                            MinecraftProtocolHelper.ping(channel, address.getSourceHost(), address.getSourcePort(), lp.before((result, ms, err) -> channel.close()), false);
                    } catch (InterruptedException ignore) {
                    } catch (Throwable any) {
                        lp.done(null, 0, any);
                    }
                    break;
            }
        }
    }
}
