/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 02:02:22
 *
 * MiraiPlugins/PluginModeRun/MemberJLListener.java
 */

package cn.mcres.karlatemp.mirai.pr.listener;

import cn.mcres.karlatemp.mirai.Eval;
import cn.mcres.karlatemp.mirai.Http;
import cn.mcres.karlatemp.mirai.eval.NativeFunction;
import cn.mcres.karlatemp.mirai.eval.ResultMethod;
import cn.mcres.karlatemp.mirai.event.MemberJoinGroupEvent;
import cn.mcres.karlatemp.mirai.event.MemberLeaveGroupEvent;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.message.data.*;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MemberJLListener {
    public static class EmptyScript extends CompiledScript {
        public static final EmptyScript INSTANCE = new EmptyScript();

        @Override
        public Object eval() throws ScriptException {
            return null;
        }

        @Override
        public Object eval(Bindings bindings) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(ScriptContext context) throws ScriptException {
            return null;
        }

        @Override
        public ScriptEngine getEngine() {
            return null;
        }
    }

    public static final NativeFunction UploadImage = new NativeFunction("UploadImage") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            File file = new File("images", String.valueOf(objects[1]));
            Contact c = (Contact) objects[0];
            int counter = 5;
            do {
                try {
                    return c.uploadImage(file);
                } catch (Throwable ignore) {
                }
            } while (counter-- > 0);
            return null;
        }
    }, At = new NativeFunction("At") {
        @Override
        protected Object call0(Object o, Object... objects) throws Throwable {
            return new At((Member) objects[0]);
        }
    };

    public static MessageChain build(Object o) {
        if (o instanceof MessageChain) return (MessageChain) o;
        if (o instanceof MessageChainBuilder) return ((MessageChainBuilder) o).asMessageChain();
        if (o instanceof JSObject) {
            JSObject jo = (JSObject) o;
            if (jo.isArray()) {
                return MessageUtils.newChain((Collection<? extends Message>)
                        jo.values().stream().map(v -> {
                            if (v instanceof Message) return (Message) v;
                            if (v != null) return new PlainText(String.valueOf(v));
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedList::new))
                );
            }
        }
        return null;
    }

    public static final Map<Long, CompiledScript> joins = new ConcurrentHashMap<>(), leaves = new ConcurrentHashMap<>();
    public static final Function<Long, CompiledScript>
            JoinLoader = id -> load(id, true),
            LeaveLoader = id -> load(id, false);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void remove(long group, boolean isJoin) {
        new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + ".txt").delete();
        new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + ".js").delete();
    }

    public static void override(long group, boolean isJoin, boolean isScript, MessageChain chain) {
        File f = new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + (isScript ? ".js" : ".txt"));
        //noinspection ResultOfMethodCallIgnored
        new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + (isScript ? ".txt" : ".js")).delete();
        //noinspection ResultOfMethodCallIgnored
        f.getParentFile().mkdirs();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            if (isScript) {
                writer.append(chain.toString());
                writer.flush();
            } else {
                for (Message m : chain) {
                    if (m instanceof OnlineImage) {
                        OnlineImage image = (OnlineImage) m;
                        String id = Base64.getEncoder().encodeToString(image.getImageId().getBytes(StandardCharsets.UTF_8));
                        if (Http.download("../image/" + group + "/" + id, image.getOriginUrl()) != null) {
                            writer.append("[image]").append(String.valueOf(group)).append('/').append(id).append('\n');
                        }
                    } else {
                        String s = m.toString().replace("[image]", "");
                        writer.append(s);
                    }
                }
            }
        } catch (IOException ignore) {
        }
    }

    public static CompiledScript load(long group, boolean isJoin) {
        File normal = new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + ".txt");
        File script = new File(groupScript, group + "-" + (isJoin ? "join" : "leave") + ".js");
        if (normal.isFile()) {
            try (Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(normal), StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                LinkedList<BiFunction<Contact, Member, Message>> link = new LinkedList<>();
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine();
                    if (line.startsWith("[image]")) {
                        if (builder.length() > 0) {
                            String txt = builder.toString();
                            link.add((c, m) -> new PlainText(txt));
                            builder.setLength(0);
                        }
                        String img = group + "/" + line.substring(7);
                        link.add((contact, member) -> (Image) UploadImage.call(null, contact, img));
                    } else if (line.equals("[at]")) {
                        if (builder.length() > 0) {
                            String txt = builder.toString();
                            link.add((c, m) -> new PlainText(txt));
                            builder.setLength(0);
                        }
                        link.add((contact, member) -> new At(member));
                    } else builder.append(line);
                }
                if (builder.length() > 0) {
                    String txt = builder.toString();
                    link.add((c, m) -> new PlainText(txt));
                }
                return new CompiledScript() {
                    @Override
                    public Object eval() throws ScriptException {
                        throw new ScriptException("No Impl");
                    }

                    @Override
                    public Object eval(Bindings bindings) throws ScriptException {
                        ScriptContext context = new SimpleScriptContext();
                        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                        return eval(context);
                    }

                    @Override
                    public Object eval(ScriptContext context) throws ScriptException {
                        LinkedList<Message> messages = new LinkedList<>();
                        Contact t = (Contact) context.getBindings(ScriptContext.ENGINE_SCOPE).get("group");
                        Member u = (Member) context.getBindings(ScriptContext.ENGINE_SCOPE).get("member");
                        for (BiFunction<Contact, Member, Message> mgs : link) {
                            messages.add(mgs.apply(t, u));
                        }
                        return MessageUtils.newChain(messages);
                    }

                    @Override
                    public ScriptEngine getEngine() {
                        return null;
                    }
                };
            } catch (IOException ignore) {
            }
        }
        if (script.isFile()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(script), StandardCharsets.UTF_8)) {
                return engine.compile(reader);
            } catch (Exception ignore) {
            }
        }
        return EmptyScript.INSTANCE;
    }

    public static final File groupScript = new File("scripts/groups");
    public static final NashornScriptEngine engine = (NashornScriptEngine) Eval.engine;

    public static void process(CompiledScript script, String name, Group group, Member member) {
        if (script == null) return;
        ScriptContext context = new SimpleScriptContext();
        context.setAttribute(ScriptEngine.FILENAME, name, ScriptContext.ENGINE_SCOPE);
        Bindings bindings = engine.createBindings();
        bindings.put("group", group);
        bindings.put("member", member);
        bindings.put("UploadImage", UploadImage);
        bindings.put("At", At);
        ResultMethod result = new ResultMethod();
        bindings.put("result", result);
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        Object value;
        try {
            value = script.eval(context);
        } catch (Throwable ignore) {
            return;
        }
        if (value == null) value = result.result;
        if (value != null) {
            final MessageChain build = build(value);
            if (build != null) group.sendMessageAsync(build);
        }
    }

    public static void register() {
        MemberJoinGroupEvent.handlers.register(event -> {
            System.out.println("J " + event.packet);
            final MemberJoinEvent packet = event.packet;
            final CompiledScript script = joins.computeIfAbsent(packet.getGroup().getId(), JoinLoader);
            if (script instanceof EmptyScript) return;
            process(script, "Join/" + packet.getGroup().getId(), packet.getGroup(), packet.getMember());
        });
        MemberLeaveGroupEvent.handlers.register(event -> {
            System.out.println("E " + event.packet);
            final MemberLeaveEvent packet = event.packet;
            final CompiledScript script = leaves.computeIfAbsent(packet.getGroup().getId(), LeaveLoader);
            if (script instanceof EmptyScript) return;
            process(script, "Leave/" + packet.getGroup().getId(), packet.getGroup(), packet.getMember());
        });
    }
}
