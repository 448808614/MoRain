/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/16 11:22:59
 *
 * MiraiPlugins/PluginModeRun/Marketing.java
 */

package cn.mcres.karlatemp.mirai.pr.commands;

import cn.mcres.karlatemp.mirai.arguments.ArgumentToken;
import cn.mcres.karlatemp.mirai.command.MCommand;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.ContactMessage;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class Marketing implements MCommand {
    @Override
    public void invoke(@NotNull Contact contact, @NotNull User sender, @NotNull ContactMessage packet, @NotNull LinkedList<ArgumentToken> args) throws Exception {
        if (args.size() < 3) {
            contact.sendMessageAsync("" +
                    "营销号生成器vUnknown:\n" +
                    "/marketing [主题] [事件] [另一种说法]"
            );
            return;
        }
        String bb = args.poll().getAsString();
        assert !args.isEmpty();
        String cc = args.poll().getAsString();
        assert !args.isEmpty();
        String dd = args.poll().getAsString();
        contact.sendMessageAsync(bb + cc + "是怎么一回事呢？" + bb + "相信大家都很熟悉，但是" + bb + cc + "是怎么回事呢，下面就让小编带大家一起了解吧。\n"
                + bb + cc + "，其实就是" + dd + "，大家可能会很惊讶" + bb + "怎么会" + cc + "呢？但事实就是这样，小编也感到非常 惊讶。\n"
                + "这就是关于" + bb + cc + "的事情了，大家有什么想法呢，欢迎在评论区告诉小编一起讨论哦！"
        );
    }
}
