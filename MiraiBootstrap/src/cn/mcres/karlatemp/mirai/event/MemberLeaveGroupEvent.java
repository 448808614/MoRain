/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/04 01:52:36
 *
 * MiraiPlugins/MiraiBootstrap/MemberLeaveGroupEvent.java
 */

package cn.mcres.karlatemp.mirai.event;

import cn.mcres.karlatemp.mxlib.event.Event;
import cn.mcres.karlatemp.mxlib.event.HandlerList;
import net.mamoe.mirai.event.events.MemberLeaveEvent;

public class MemberLeaveGroupEvent extends Event {
    public static final HandlerList<MemberLeaveGroupEvent> handlers = new HandlerList<>();
    public final MemberLeaveEvent packet;

    public MemberLeaveGroupEvent(MemberLeaveEvent packet) {
        this.packet = packet;
    }

    @Override
    public HandlerList<MemberLeaveGroupEvent> getHandlerList() {
        return handlers;
    }
}
