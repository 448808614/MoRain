/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 19:06:36
 *
 * MiraiPlugins/MiraiPlugins/ArgumentParser.java
 */

package cn.mcres.karlatemp.mirai.arguments;

import net.mamoe.mirai.message.data.*;

import java.util.LinkedList;

public class ArgumentParser {
    public static LinkedList<ArgumentToken> parse(MessageChain messages) {
        LinkedList<ArgumentToken> tokens = new LinkedList<>();
        for (SingleMessage msg : messages) {
            if (msg instanceof Image) {
                tokens.add(new ArgumentImageToken((Image) msg));
            } else if (msg instanceof At) {
                tokens.add(new ArgumentAtToken((At) msg));
            } else if (msg instanceof PlainText) {
                final String value = ((PlainText) msg).getStringValue().trim();
                int start = 0;
                do {
                    int index = value.indexOf(' ', start);
                    if (index == -1) {
                        tokens.add(new ArgumentToken(value.substring(start)));
                        break;
                    } else {
                        tokens.add(new ArgumentToken(value.substring(start, index)));
                        start = index + 1;
                    }
                } while (true);
            }
        }
        return tokens;
    }

    public static LinkedList<ArgumentToken> parse(String line) {
        LinkedList<ArgumentToken> tokens = new LinkedList<>();
        int start = 0;
        do {
            int index = line.indexOf(' ', start);
            if (index == -1) {
                tokens.add(new ArgumentToken(line.substring(start)));
                break;
            } else {
                tokens.add(new ArgumentToken(line.substring(start, index)));
                start = index + 1;
            }
        } while (true);
        return tokens;
    }
}
