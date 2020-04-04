/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 01:58:20
 *
 * MiraiPlugins/KarPermManager/ExceptionBox.java
 */

package cn.mcres.karlatemp.mirai.eval;

public class ExceptionBox extends RuntimeException {
    public final Object exception;

    public ExceptionBox(Object exception) {
        super(null, null, false, false);
        this.exception = exception;
    }
}
