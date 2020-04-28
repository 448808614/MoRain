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
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import net.mamoe.mirai.message.data.Image;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

        public static class Protocol {
            public String name;
            public int protocol;
        }

        public static class Players {
            public long max, online;
        }

        public Protocol version;
        public String favicon;
        public Players players;
    }

    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
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
                                var desc = g.fromJson(JsonParser.parseString(byteBuf.toString(StandardCharsets.UTF_8)), Desc.class);
                                String msg = address + "  (" + l + "ms)\n"
                                        + desc.version.name + "(" + desc.version.protocol + ")\n"
                                        + "Online: (" + desc.players.online + " / " + desc.players.max + ")\n"
                                        + ChatColor.stripColor(new TextComponent(desc.description).toPlainText());
                                {
                                    final var favicon = desc.favicon;
                                    if (favicon != null) {
                                        var splitter = favicon.indexOf(',');
                                        if (splitter > 0) {
                                            var base64 = favicon.substring(splitter + 1);
                                            final var decode = Base64.getMimeDecoder().decode(base64);
                                            Image image = null;
                                            var counter = 3;
                                            while (counter-- > 0) {
                                                try {
                                                    image = contact.uploadImage(new ByteArrayInputStream(decode));
                                                    break;
                                                } catch (Throwable ignore) {
                                                }
                                            }
                                            if (image != null) {
                                                contact.sendMessageAsync(image.plus(msg));
                                            } else {
                                                contact.sendMessageAsync("Failed to upload server favicon.\n" + msg);
                                            }
                                            return;
                                        }
                                    }
                                }
                                contact.sendMessageAsync(msg);
                            } catch (Exception exception) {
                                contact.sendMessageAsync("服务器信息无法格式化: " + exception);
                            }
                        } else {
                            contact.sendMessageAsync("服务器信息获取失败");
                        }
                    };
                    try {
                        MinecraftProtocolHelper.ping(Netty.group, address, lp, true);
                    } catch (Throwable any) {
                        lp.done(null, 0, any);
                    }
                    break;
            }
        }
    }
}
