/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/05 03:24:19
 *
 * MiraiPlugins/PluginModeRun/MessageLink.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mirai.Http;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;

public class MessageLink implements Serializable {
    public static final long serialVersionUID = 0xF478787879511L;
    public Collection<Action> actions;

    public MessageLink initialize() {
        if (actions == null) {
            actions = new LinkedList<>();
        }
        return this;
    }

    public static abstract class Action implements Serializable {
        public static final long serialVersionUID = -9007870469836068169L;

        public abstract void append(Contact contact, User sender, Collection<Message> messages);
    }

    public static class ActionAtIt extends Action {
        public static final long serialVersionUID = 0x123975eddaffL;
        public static final ActionAtIt INSTANCE = new ActionAtIt();

        private ActionAtIt() {
        }

        private Object readResolve() {
            return INSTANCE;
        }

        private void readObject(ObjectInputStream stream) {
        }

        private void writeObject(ObjectOutputStream stream) {
        }

        @Override
        public void append(Contact contact, User sender, Collection<Message> messages) {
            if (sender instanceof Member) {
                messages.add(new At((Member) sender));
            }
        }
    }

    public static class ActionPlant extends Action {
        public static final long serialVersionUID = 8647684171544548745L;

        private void readObject(ObjectInputStream stream) throws IOException {
            string = stream.readUTF();
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeUTF(string);
        }

        public String string;

        @Override
        public void append(Contact contact, User sender, Collection<Message> messages) {
            messages.add(new PlainText(string));
        }

        public ActionPlant value(String substring) {
            string = substring;
            return this;
        }
    }

    public static class ActionImage extends Action {
        public static final long serialVersionUID = 998784464875457L;
        public File file;

        private void readObject(ObjectInputStream stream) throws IOException {
            file = new File(stream.readUTF());
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeUTF(file.getPath());
        }

        @Override
        public void append(Contact contact, User sender, Collection<Message> messages) {
            int counter = 5;
            while (counter-- > 0) {
                try {
                    messages.add(contact.uploadImage(file));
                    break;
                } catch (Exception ignore) {
                }
            }
        }

        public ActionImage file(File file) {
            this.file = file;
            return this;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int counter = stream.readUnsignedShort();
        actions = new LinkedList<>();
        while (counter-- > 0) {
            actions.add((Action) stream.readObject());
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeShort(actions.size());
        for (Action a : actions) stream.writeObject(a);
    }

    public MessageChain build(Contact contact, User sender) {
        var mg = new LinkedList<Message>();
        for (var a : actions) {
            a.append(contact, sender, mg);
        }
        return MessageUtils.newChain(mg);
    }

    public void override(MessageChain chain) {
        actions.clear();
        for (var m : chain) {
            if (m instanceof OnlineImage) {
                var image = (OnlineImage) m;
                File file = Http.download(
                        "imgs/" + Base64.getEncoder().encodeToString(
                                image.getImageId().getBytes(StandardCharsets.UTF_8)
                        ), image.getOriginUrl());
                if (file != null) {
                    actions.add(new ActionImage().file(file));
                }
            } else if (m instanceof MessageSource) {
            } else if (m instanceof QuoteReply) {
            } else {
                var msg = String.valueOf(m);
                var split = "[at]";
                var start = 0;
                do {
                    var index = msg.indexOf(split, start);
                    if (index == -1) {
                        var tail = msg.substring(start);
                        if (tail.isEmpty()) break;
                        actions.add(new ActionPlant().value(tail));
                        break;
                    } else {
                        actions.add(new ActionPlant().value(msg.substring(start, index)));
                        actions.add(ActionAtIt.INSTANCE);
                        start = index + split.length();
                    }
                } while (true);
            }
        }
    }
}
