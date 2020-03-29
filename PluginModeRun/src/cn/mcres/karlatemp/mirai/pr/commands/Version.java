/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/29 02:28:42
 *
 * MiraiPlugins/PluginModeRun/Version.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.Bootstrap;
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import cn.mcres.karlatemp.mirai.pr.Main;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.MessagePacket;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements MCommand {

    public static String a(String x) {
        return '\\' + x;
    }

    public static final Pattern pt = Pattern.compile(
            a(File.pathSeparator)
    );
    public static final Pattern p2 = Pattern.compile(
            "^mirai-([A-Za-z0-9.\\-_]+)-(([0-9.]+)(|-[A-Za-z0-9_]+))\\.jar$"
    );

    @Override
    public void invoke(Contact contact, QQ sender, MessagePacket<?, ?> packet, LinkedList<ArgumentToken> args) {
        StringBuilder builder = new StringBuilder().append("墨雨橙 Power by Mirai.\n");
        final String[] split = pt.split(ManagementFactory.getRuntimeMXBean().getClassPath());
        for (String pt : split) {
            final int i = pt.indexOf(File.separatorChar);
            if (i != -1) {
                pt = pt.substring(i + 1);
            }
            final Matcher matcher = p2.matcher(pt);
            if (matcher.find()) {
                builder.append("Mirai-").append(matcher.group(1)).append(": ").append(matcher.group(2)).append('\n');
            }
        }
        builder.append("Java: ").append(System.getProperty("java.version")).append('\n')
                .append("Bootstrap: ").append(Bootstrap.getVersion()).append('\n')
                .append("Main Core: ").append(Main.VERSION);
        contact.sendMessageAsync(builder.toString());
    }
}
