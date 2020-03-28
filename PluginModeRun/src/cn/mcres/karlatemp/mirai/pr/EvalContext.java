/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/23 22:28:50
 *
 * MiraiPlugins/PluginModeRun/EvalContext.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import cn.mcres.karlatemp.mxlib.tools.Unsafe;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.ECMAException;
import jdk.nashorn.internal.runtime.Undefined;

import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class EvalContext {
    public static class ExceptionBox extends Throwable {
        public final Object exception;

        public ExceptionBox(Object exception) {
            super(null, null, false, false);
            this.exception = exception;
        }
    }

    public abstract static class NativeFunction implements JSObject {
        protected String name;

        @Override
        public Object call(Object o, Object... objects) {
            Object result;
            try {
                result = call0(o, objects);
            } catch (Throwable throwable) {
                if (throwable instanceof ExceptionBox) {
                    throw ECMAException.create(((ExceptionBox) throwable).exception, getClassName() + ".native", 4, 4);
                }
                throw ECMAException.create(throwable, getClassName() + ".native", 4, 4);
            }
            if (result instanceof ExceptionBox) {
                throw ECMAException.create(((ExceptionBox) result).exception, getClassName() + ".native", 4, 4);
            }
            return result;
        }

        protected abstract Object call0(Object o, Object... objects) throws Throwable;

        private static final NativeFunction TO_STRING_TO_STRING = new NativeFunction((Void) null) {
            {
                this.TO_STRING = this;
            }

            @Override
            protected Object call0(Object o, Object... objects) throws Throwable {
                return call(o, objects);
            }

            @Override
            public Object call(Object o, Object... objects) {
                return "function toString(){ [native code] }";
            }
        };

        NativeFunction(Void ignored) {
            TO_STRING = TO_STRING_TO_STRING;
        }

        protected NativeFunction() {
            this((String) null);
        }

        protected NativeFunction(String name) {
            if (name == null) name = "NativeMethod";
            TO_STRING = new NativeFunction((Void) null) {
                NativeFunction thiz;

                NativeFunction init(NativeFunction thiz) {
                    this.thiz = thiz;
                    name = "toString";
                    return this;
                }

                @Override
                protected Object call0(Object o, Object... objects) throws Throwable {
                    return call(o, objects);
                }

                public String toString() {
                    return "function toString(){ [native code] }";
                }

                @Override
                public Object call(Object o, Object... objects) {
                    return thiz.toString();
                }
            }.init(this);
            this.name = name;
        }

        NativeFunction TO_STRING;

        @Override
        public Object newObject(Object... objects) {
            throw new UnsupportedOperationException("No newInstace yet.");
        }

        @Override
        public Object eval(String s) {
            throw new UnsupportedOperationException("No EvalContext Here");
        }

        @Override
        public Object getMember(String s) {
            if ("toString".equals(s)) {
                return TO_STRING;
            }
            return null;
        }

        @Override
        public Object getSlot(int i) {
            return null;
        }

        @Override
        public boolean hasMember(String s) {
            return false;
        }

        @Override
        public boolean hasSlot(int i) {
            return false;
        }

        @Override
        public void removeMember(String s) {
        }

        @Override
        public void setMember(String s, Object o) {
        }

        @Override
        public void setSlot(int i, Object o) {
        }

        @Override
        public Set<String> keySet() {
            return Collections.emptySet();
        }

        @Override
        public Collection<Object> values() {
            return Collections.emptySet();
        }

        @Override
        public boolean isInstance(Object o) {
            return false;
        }

        @Override
        public boolean isInstanceOf(Object o) {
            return false;
        }

        @Override
        public String getClassName() {
            return name;
        }

        @Override
        public boolean isFunction() {
            return true;
        }

        @Override
        public boolean isStrictFunction() {
            return true;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public double toNumber() {
            return 0;
        }

        @Override
        public String toString() {
            return "function " + getClassName() + "(){ [native code] }";
        }
    }

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
