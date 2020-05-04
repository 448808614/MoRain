/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/28 22:11:14
 *
 * MiraiPlugins/PluginModeRun/Covid.kt
 */

package cn.mcres.karlatemp.mirai.pr.kotlin.commands

import cn.mcres.karlatemp.mirai.*
import cn.mcres.karlatemp.mirai.arguments.ArgumentToken
import cn.mcres.karlatemp.mirai.command.KCAlias
import cn.mcres.karlatemp.mirai.command.KCommand
import cn.mcres.karlatemp.mirai.command.KotlinCommand
import cn.mcres.karlatemp.mxlib.tools.URLEncoder
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.toMessage
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.core5.concurrent.FutureCallback
import java.time.Instant
import java.time.ZoneId
import java.util.*

// {
//      "locationId": 955015,
//      "continentName": "亚洲",
//      "continentEnglishName": "Asia",
//      "countryName": "卡塔尔",
//      "countryEnglishName": "Qatar",
//      "provinceName": "卡塔尔",
//      "provinceEnglishName": "Qatar",
//      "provinceShortName": "卡塔尔",
//      "currentConfirmedCount": 10777,
//      "confirmedCount": 11921,
//      "suspectedCount": 0,
//      "curedCount": 1134,
//      "deadCount": 10,
//      "comment": "",
//      "cities": [
//         {
//          "cityName": "通州区",
//          "currentConfirmedCount": 18,
//          "confirmedCount": 19,
//          "suspectedCount": 0,
//          "curedCount": 1,
//          "deadCount": 0,
//          "locationId": 110112,
//          "cityEnglishName": "Tongzhou District"
//        }],
//      "updateTime": 1588079376688
//    }
@Suppress("SpellCheckingInspection")
@KCommand("covid")
@KCAlias("疫情")
object Covid : KotlinCommand() {
    data class Result(
            val results: List<OneOfResult>?,
            val success: Boolean
    )

    data class City(
            val cityName: String,
            val currentConfirmedCount: Long,
            val confirmedCount: Long,
            val curedCount: Long,
            val deadCount: Long,
            val locationId: Long,
            val cityEnglishName: String
    )

    data class OneOfResult(
            val locationId: Int,
            val continentName: String,
            val continentEnglishName: String,
            val countryName: String,
            val countryEnglishName: String,
            val provinceName: String,
            val provinceEnglishName: String,
            val provinceShortName: String,
            val currentConfirmedCount: Long,
            val confirmedCount: Long,
            val suspectedCount: Long,
            val curedCount: Long,
            val deadCount: Long,
            val commit: String,
            val cities: List<City>?,
            val updateTime: Long
    )

    override suspend fun invoke0(contact: Contact, sender: User, packet: ContactMessage, args: LinkedList<ArgumentToken>) {
        val url = "https://lab.isaaclin.cn/nCoV/api/area" + args.let {
            if (!it.isEmpty()) {
                return@let "?province=" + URLEncoder.encode(it.poll().asString, Charsets.UTF_8)
            }
            "请输入一个国家、省份或直辖市".toMessage() sendTo contact
            return@invoke0
        }
        Http.client.execute(SimpleHttpRequest.copy(HttpGet(url)), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() {
            }

            override fun completed(p0: SimpleHttpResponse) {
                kotlin.runCatching {
                    val result = p0.bodyBytes.inputStream().reader(Charsets.UTF_8).use { globalGson.fromJson(it, Result::class.java) }
                            ?: error("Internal Error: No RESULT")
                    AsyncExecKt.newScope.launch {
                        if (!result.success || result.results == null || result.results.isEmpty()) {
                            "获取数据失败".toMessage() sendTo contact
                            return@launch
                        }
                        val r = result.results.getOrNull(0) ?: run {
                            "未查到目标地区的数据".toMessage() sendTo contact
                            return@launch
                        }
                        if (r.cities == null || r.cities.isEmpty()) {
                            ("${r.provinceName}疫情 ->\n" +
                                    "现存确诊>> ${r.currentConfirmedCount}\n" +
                                    "累计确诊>> ${r.confirmedCount}\n" +
                                    "累计治愈>> ${r.curedCount}\n" +
                                    "累计死亡>> ${r.deadCount}\n" +
                                    "更新时间 -> ${
                                    Instant.ofEpochMilli(r.updateTime).atZone(ZoneId.systemDefault())
                                            .toString().replace('T', ' ')
                                    } (${r.updateTime})").toMessage() sendTo contact
                            return@launch
                        }
                        buildForwardMessage(contact) {
                            contact.lucky() asNode this says
                                    "${r.provinceName}疫情 ->\n" +
                                    "现存确诊>> ${r.currentConfirmedCount}\n" +
                                    "累计确诊>> ${r.confirmedCount}\n" +
                                    "累计治愈>> ${r.curedCount}\n" +
                                    "累计死亡>> ${r.deadCount}"
                            r.cities.forEach {
                                contact.lucky() asNode this says
                                        "${it.cityName}疫情 ->\n" +
                                        "现存确诊>> ${it.currentConfirmedCount}\n" +
                                        "累计确诊>> ${it.confirmedCount}\n" +
                                        "累计治愈>> ${it.curedCount}\n" +
                                        "累计死亡>> ${it.deadCount}"
                            }
                            contact.lucky() asNode this says
                                    "更新时间 -> ${
                                    Instant.ofEpochMilli(r.updateTime).atZone(ZoneId.systemDefault())
                                            .toString().replace('T', ' ')
                                    } (${r.updateTime})"
                        } sendTo contact
                    }
                }.exceptionOrNull()?.apply { failed(this) }
            }

            override fun failed(p0: Exception) = failed(p0 as Throwable)

            fun failed(p0: Throwable) {
                AsyncExecKt.newScope.launch {
                    p0.toString().toMessage() sendTo contact
                }
            }

        })
    }
}