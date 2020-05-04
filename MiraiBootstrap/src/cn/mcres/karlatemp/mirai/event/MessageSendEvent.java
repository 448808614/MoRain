/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:37:14
 *
 * MiraiPlugins/MiraiBootstrap/MessageSendEvent.java
 */

package cn.mcres.karlatemp.mirai.event;

import cn.mcres.karlatemp.mxlib.event.Cancellable;
import cn.mcres.karlatemp.mxlib.event.Event;
import cn.mcres.karlatemp.mxlib.event.HandlerList;
import net.mamoe.mirai.message.ContactMessage;

public class MessageSendEvent extends Event implements Cancellable {
    public static final HandlerList<MessageSendEvent> handlers = new HandlerList<>();
    private final ContactMessage event;
    private boolean c;

    public MessageSendEvent(ContactMessage event) {
        this.event = event;
    }

    public ContactMessage getEvent() {
        return event;
    }

    @Override
    public HandlerList<MessageSendEvent> getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return c;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        c = isCancelled;
    }
}
