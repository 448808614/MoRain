/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 18:51:49
 *
 * MiraiPlugins/MiraiPlugins/ArgumentToken.java
 */

package cn.mcres.karlatemp.mirai.arguments;

public class ArgumentToken {
    private final Object value;

    public ArgumentToken(Object value) {
        this.value = value;
    }

    public String getAsString() {
        return String.valueOf(value);
    }

    public long getAsLong() {
        try {
            return Long.parseLong(getAsString());
        } catch (NumberFormatException ignore) {
        }
        return 0L;
    }

    public int getAsInt() {
        try {
            return Integer.parseInt(getAsString());
        } catch (NumberFormatException ignore) {
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Token{" + value + '}';
    }
}
