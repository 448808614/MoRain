/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 15:50:26
 *
 * MiraiPlugins/MiraiPlugins/MessageCoder.java
 */

package cn.mcres.karlatemp.mirai;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.*;

import java.nio.CharBuffer;
import java.util.regex.Pattern;

public class MessageCoder {
    private static final Pattern pt = Pattern.compile("\\[mirai:(.+?)]");

    public static StringBuilder control(CharBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        boolean noNext = false;
        int insert = 0;
        while (buffer.hasRemaining()) {
            char next = buffer.get();
            switch (next) {
                case '\u202e': {
                    noNext = true;
                    break;
                }
                case '\u202d':
                case '\u202c': {
                    noNext = false;
                    break;
                }
                case '\n': {
                    builder.append('\n');
                    insert = buffer.length();
                    break;
                }
                default: {
                    if (noNext) {
                        switch (next) {
                            case '(':
                                next = ')';
                                break;
                            case ')':
                                next = '(';
                                break;
                            case '[':
                                next = ']';
                                break;
                            case ']':
                                next = '[';
                                break;
                            case '{':
                                next = '}';
                                break;
                            case '}':
                                next = '{';
                                break;
                            case '\\':
                                next = '/';
                                break;
                            case '【':
                                next = '】';
                                break;
                            case '】':
                                next = '【';
                                break;
                            case '（':
                                next = '）';
                                break;
                            case '）':
                                next = '（';
                                break;
                        }
                    }
                    builder.insert(insert, next);
                    if (!noNext) insert++;
                    break;
                }
            }
        }
        return builder;
    }

    public static MessageChain coder(String buffer, Contact contact) {
        MessageChainBuilder builder = new MessageChainBuilder(buffer.length() / 2);
        int start = -1;
        String find = "[mirai:";
        while (true) {
            final int begin = buffer.indexOf(find, start);
            if (begin == -1) break;
            final int end = buffer.indexOf(']', begin);
            if (end == -1) break;
            builder.add(buffer.substring(Math.max(0, start), begin));
            String chunk = buffer.substring(begin + find.length(), end);
            start = end + 1;
            int split = chunk.indexOf('=');
            String type, param;
            if (split == -1) {
                type = chunk;
                param = null;
            } else {
                type = chunk.substring(0, split);
                param = chunk.substring(split + 1);
            }
            switch (type) {
                case "at": {
                    if (param == null) break;
                    try {
                        long qq = Long.parseLong(param);
                        if (contact instanceof Group) {
                            try {
                                builder.add(new At(((Group) contact).get(qq)));
                                break;
                            } catch (Exception ignore) {
                            }
                        }
                        builder.add(At._lowLevelConstructAtInstance(qq, "<unknown>"));
                    } catch (Throwable ignore) {
                    }
                    break;
                }
                case "image": {
                    if (param == null) break;
                    builder.add(MessageUtils.newImage(param));
                    break;
                }
            }
        }
        if (start + 1 != buffer.length()) {
            builder.add(buffer.substring(Math.max(start, 0)));
        }
        return builder.asMessageChain();
    }

    public static StringBuilder coder(MessageChain chain) {
        StringBuilder builder = new StringBuilder();
        for (Message m : chain) {
            if (m instanceof At) {
                builder.append("[mirai:at=").append(((At) m).getTarget()).append(']');
            } else if (m instanceof QuoteReply) {
            } else if (m instanceof MessageSource) {
            } else if (m instanceof Image) {
                builder.append("[mirai:image=");
                encode(builder, ((Image) m).getImageId());
                builder.append(']');
            } else {
                encode(builder, m.toString());
            }
        }
        return builder;
    }

    private static void encode(StringBuilder builder, String value) {
        if (value == null || value.isEmpty()) return;
        builder.append(value);
    }
}
