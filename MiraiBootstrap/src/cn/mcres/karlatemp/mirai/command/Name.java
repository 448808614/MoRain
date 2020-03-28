/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/19 19:40:15
 *
 * MiraiPlugins/MiraiPlugins/Name.java
 */

package cn.mcres.karlatemp.mirai.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Name {
    String value();
}
