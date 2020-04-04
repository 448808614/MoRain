/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 02:17:40
 *
 * MiraiPlugins/MiraiBootstrap/ResultMethod.java
 */

package cn.mcres.karlatemp.mirai.eval;

public class ResultMethod extends NativeFunction {
    public Object result;

    public ResultMethod() {
        super("Result");
    }

    @Override
    protected Object call0(Object o, Object... objects) throws Throwable {
        return result = objects[0];
    }
}
