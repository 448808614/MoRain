/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/30 12:46:45
 *
 * MiraiPlugins/MiraiBootstrap/Templates.kt
 */

package cn.mcres.karlatemp.mirai

import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain

// {"app":"com.tencent.structmsg","desc":"音乐","meta":{"music":{
// "action":"","android_pkg_name":"","app_type":1,"appid":100495085,"desc":"hokchi",
// "jumpUrl":"http://music.163.com/song/1396981253","musicUrl":
// "http://music.163.com/song/media/outer/url?id=1396981253.mp3","preview":
// "http://p2.music.126.net/fzxMwtZMDod37ecRAkKiGQ==/109951164427637790.jpg",
// "sourceMsgId":"0","source_icon":"","source_url":"","tag":"网易云音乐","title":"WDNMD"}},
// "prompt":"[分享] WDNMD","ver":"0.0.0.1","view":"music"}
// [分享]WDNMD\nhokchi\nhttp://music.163.com/song/1396981253\n来自: 网易云音乐

fun build163Music(
        jumpURL: String,
        musicURL: String,
        preview: String,
        title: String,
        prompt: (title: String) -> String = { "[分享] $it" },
        author: String = "<unknown>",
        tag: String = "网易云音乐",
        tail: (title: String, author: String, jumpURL: String) -> String = { a, b, c ->
            // [分享]Release Me \nTwo Steps From Hell\nhttp://music.163.com/song/31654461\n来自: 网易云音乐
            "[分享]$a\n$b\n$c\n来自: 网易云音乐"
        }
): MessageChain = LightApp(json {
    // {
    //    "app": "com.tencent.structmsg",
    //    "desc": "音乐",
    //    "meta": {
    //        "music": {
    //            "action": "",
    //            "android_pkg_name": "",
    //            "app_type": 1,
    //            "appid": 100495085,
    //            "desc": "Two Steps From Hell",
    //            "jumpUrl": "http://music.163.com/song/31654461",
    //            "musicUrl": "http://music.163.com/song/media/outer/url?id=31654461.mp3",
    //            "preview": "http://p2.music.126.net/n41bSTrQwG_lQzkXz7cygg==/109951163892182787.jpg",
    //            "sourceMsgId": "0",
    //            "source_icon": "",
    //            "source_url": "",
    //            "tag": "网易云音乐",
    //            "title": "Release Me "
    //        }
    //    },
    //    "prompt": "[分享] Release Me ",
    //    "ver": "0.0.0.1",
    //    "view": "music"
    //}
    obj {
        "app" value "com.tencent.structmsg"
        "desc" value "音乐"
        "meta" obj {
            "music" obj {
                "action" value ""
                "android_pkg_name" value ""
                "app_type" value 1
                "appid" value 100495085
                "desc" value author
                "jumpUrl" value jumpURL
                "musicUrl" value musicURL
                "preview" value preview
                "sourceMsgId" value "0"
                "source_icon" value ""
                "source_url" value ""
                "tag" value tag
                "title" value title
            }
        }
        "prompt" value prompt(title)
        "ver" value "0.0.0.1"
        "view" value "music"
    }
}) + tail(title, author, jumpURL)