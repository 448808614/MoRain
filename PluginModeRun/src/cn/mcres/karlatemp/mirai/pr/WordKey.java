/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/05 03:16:18
 *
 * MiraiPlugins/PluginModeRun/WordKey.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.pr.commands.ExtendedMessageChain;
import cn.mcres.karlatemp.mxlib.util.RAFOutputStream;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WordKey implements Serializable {
    public static final Map<String, WordKey> allWords = new ConcurrentHashMap<>();
    public static final File words = new File("words");
    public static final WordKey UNLOADED;
    public static final Logger LOGGER = Logger.getLogger("WordKey");
    private static final long serialVersionUID = 1810648626663744907L;

    static {
        var l = UNLOADED = new WordKey();
        l.eq = new LinkedList<>();
        l.contains = new LinkedList<>();
        l.messages = new MessageLink();

    }

    static {
        var pattern = Pattern.compile("^(.+)\\.bin$");
        var files = words.listFiles();
        if (files != null)
            for (var f : files) {
                var mt = pattern.matcher(f.getName());
                if (mt.find()) {
                    allocate(mt.group(1)).load();
                }
            }
    }

    public transient String uniqueId;
    public Collection<String> eq;
    public Collection<String> contains;
    public MessageLink messages;
    public long group;

    @NotNull
    public static WordKey allocateV(@NotNull String name) {
        final WordKey wordKey = allWords.get(name);
        if (wordKey != null) return wordKey;
        return UNLOADED;
    }

    @NotNull
    public static WordKey allocate(@NotNull String name) {
        var we = allocateV(name);
        if (we == UNLOADED) return new WordKey().register(name).load();
        return we;
    }

    public WordKey load() {
        if (uniqueId != null) {
            File f = new File(words, uniqueId + ".bin");
            if (f.isFile()) {
                try (var stream = new ObjectInputStream(new FileInputStream(f))) {
                    var key = (WordKey) stream.readObject();
                    eq = key.eq;
                    contains = key.contains;
                    messages = key.messages;
                    group = key.group;
                } catch (Exception ioe) {
                    LOGGER.log(Level.SEVERE, "Error in loading " + uniqueId, ioe);
                }
            }
        }
        return this;
    }

    public void save() {
        if (uniqueId != null) {
            File f = new File(words, uniqueId + ".bin");
            f.getParentFile().mkdirs();
            try (ObjectOutputStream writer = new ObjectOutputStream(new RAFOutputStream(new RandomAccessFile(f, "rw")))) {
                writer.writeObject(this);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in saving " + uniqueId, e);
            }
        }
    }

    private WordKey register(String name) {
        if (uniqueId == null) {
            uniqueId = name;
            allWords.put(uniqueId, this);
            eq = new LinkedList<>();
            contains = new LinkedList<>();
            messages = new MessageLink().initialize();
        }
        return this;
    }

    public boolean match(String stl) {
        for (var e : eq) {
            if (stl.equals(e)) return true;
        }
        for (var c : contains) {
            if (stl.contains(c)) return true;
        }
        return false;
    }

    public void remove() {
        if (uniqueId != null) {
            allWords.remove(uniqueId, this);
            new File(words, uniqueId + ".bin").delete();
        }
    }

    public void send(Contact contact, User sender) {
        final MessageChain build = messages.build(contact, sender);
        if (build.getSize() == 1) {
            final SingleMessage first = build.iterator().next();
            try {
                if (first instanceof PlainText) {
                    contact.sendMessage(((PlainText) first).getContent());
                } else {
                    contact.sendMessage(first);
                }
            } catch (Throwable throwable) {
                if (contact instanceof Group) {
                    contact.sendMessageAsync(new ExtendedMessageChain(build));
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to send message:" + build, throwable);
                }
            }
        } else {
            if (contact instanceof Group) {
                try {
                    contact.sendMessage(build);
                } catch (Throwable throwable) {
                    contact.sendMessageAsync(new ExtendedMessageChain(build));
                }
            } else {
                contact.sendMessageAsync(build);
            }
        }
    }
}
