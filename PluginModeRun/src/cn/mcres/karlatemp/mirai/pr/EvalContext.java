/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/23 22:28:50
 *
 * MiraiPlugins/PluginModeRun/EvalContext.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.MessageCoder;
import cn.mcres.karlatemp.mirai.eval.ExceptionBox;
import cn.mcres.karlatemp.mirai.eval.NativeFunction;
import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import cn.mcres.karlatemp.mxlib.tools.Unsafe;
import jdk.dynalink.beans.StaticClass;
import jdk.nashorn.internal.runtime.Undefined;

import java.lang.reflect.Field;
import java.nio.CharBuffer;

public class EvalContext {

    public static final NativeFunction GET_FIELD = new NativeFunction("GetField") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return ((Class<?>) objects[0]).getField((String) objects[1]);
        }
    };
    public static final NativeFunction GET_DECLARED_FIELD = new NativeFunction("GetField") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return ((Class<?>) objects[0]).getDeclaredField((String) objects[1]);
        }
    };
    public static final NativeFunction FOR_NAME = new NativeFunction("ForName") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return Class.forName((String) objects[0]);
        }
    };
    public static final NativeFunction CONTROL = new NativeFunction("Control") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            String str = String.valueOf(objects[0]);
            if (str.length() > 150) {
                throw new IllegalAccessException("Control String To Large");
            }
            return MessageCoder.control(CharBuffer.wrap(str));
        }
    };
    public static final NativeFunction TO_JS = new NativeFunction("ToJs") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            Object obj = objects[0];
            if (obj instanceof Class<?>) return StaticClass.forClass((Class<?>) obj);
            if (obj instanceof StaticClass) return obj;
            return new ExceptionBox("No Class found.");
        }
    };
    public static final NativeFunction GET_VALUE = new NativeFunction("GetValue") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return Toolkit.Reflection.getObjectValue(objects[0], (Field) objects[1]);
        }
    };
    public static final NativeFunction SET_VALUE = new NativeFunction("SetValue") {
        @Override
        protected Object call0(Object o, Object... objects) {
            Toolkit.Reflection.setObjectValue(objects[0], (Field) objects[1], objects[2]);
            return Undefined.getUndefined();
        }
    };
    public static final NativeFunction GET_UNSAFE = new NativeFunction("GetUnsafe") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return Unsafe.getUnsafe();
        }
    };
}
