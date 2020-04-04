/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 01:38:44
 *
 * MiraiPlugins/MiraiBootstrap/MemberJoinGroupEvent.java
 */

package cn.mcres.karlatemp.mirai.event;

import cn.mcres.karlatemp.mxlib.event.Event;
import cn.mcres.karlatemp.mxlib.event.HandlerList;
import net.mamoe.mirai.event.events.MemberJoinEvent;

public class MemberJoinGroupEvent extends Event {
    public static final HandlerList<MemberJoinGroupEvent> handlers = new HandlerList<>();
    public final MemberJoinEvent packet;

    public MemberJoinGroupEvent(MemberJoinEvent packet) {
        this.packet = packet;
    }

    @Override
    public HandlerList<MemberJoinGroupEvent> getHandlerList() {
        return handlers;
    }
}
