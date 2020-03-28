/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 18:54:30
 *
 * MiraiPlugins/MiraiPlugins/ArgumentAtToken.java
 */

package cn.mcres.karlatemp.mirai.arguments;

import net.mamoe.mirai.message.data.At;

public class ArgumentAtToken extends ArgumentToken {
    public final At qq;

    public ArgumentAtToken(At qq) {
        super(String.valueOf(qq));
        this.qq = qq;
    }

    @Override
    public int getAsInt() {
        return (int) qq.getTarget();
    }

    @Override
    public long getAsLong() {
        return qq.getTarget();
    }

    @Override
    public String toString() {
        return "At{" + qq + '}';
    }
}
