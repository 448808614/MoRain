/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:41:11
 *
 * MiraiPlugins/MiraiBootstrap/Plugin.java
 */

package cn.mcres.karlatemp.mirai.plugin;

import java.util.logging.Logger;

public abstract class Plugin {
    public final Logger logger = Logger.getLogger(getName());

    public abstract String getName();

    public abstract String getVersion();

    public abstract String getDescription();

    public void onEnable() {
    }

    public void onDisable() {
    }
}
