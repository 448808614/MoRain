/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/18 22:43:40
 *
 * MiraiPlugins/MiraiPlugins/CommandMgr.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mirai.command.MCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandMgr {
    public static final Map<String, MCommand> commands = new HashMap<>();

    public static void register(Collection<String> names, MCommand cmd) {
        for (String s : names) register(s, cmd);
    }

    public static void register(String name, MCommand cmd) {
        commands.put(name.toLowerCase(), cmd);
    }

}
