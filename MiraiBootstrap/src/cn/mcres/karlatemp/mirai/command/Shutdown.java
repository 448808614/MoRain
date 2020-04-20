/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/19 18:29:55
 *
 * MiraiPlugins/MiraiPlugins/Shutdown.java
 */

package cn.mcres.karlatemp.mirai.command;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

public class Shutdown implements MCommand {
    @Override
    public String permission() {
        return "command.shutdown";
    }

    private long invokeTime = 0;

    @Override
    public void invoke(@NotNull Contact contact, @NotNull QQ sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) {
        long time = System.currentTimeMillis();
        if (time - invokeTime > 1000 * 30) {
            invokeTime = time;
            contact.sendMessageAsync("Shutdown system?");
        } else {
            PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out));
            ps.println();
            ps.println("==============================");
            ps.println("= System shutdown by " + sender + " in " + contact);
            ps.println("==============================");
            ps.flush();
            ps.close();
            System.exit(0);
        }
    }
}
