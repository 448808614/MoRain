/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/21 17:15:49
 *
 * MiraiPlugins/MiraiBootstrap/Music.kt
 */

package cn.mcres.karlatemp.mirai.structure

data class Music(
        var app: String = "com.tencent.structmsg",
        var desc: String = "音乐",
        var meta: Meta,
        var prompt: String,
        var ver: String = "version",
        var view: String = "view"
) {
    data class Meta(val music: MusicMeta) {
        data class MusicMeta(
                var action: String = "",
                var android_pkg_name: String = "",
                var app_type: Int = 1,
                var appid: Long = 100495085L,
                var desc: String,
                var jumpUrl: String,
                var musicUrl: String,
                var preview: String,
                var sourceMsgId: String = "0",
                var source_icon: String = "",
                var source_url: String = "",
                var tag: String = "网易云音乐",
                var title: String
        )
    }
}