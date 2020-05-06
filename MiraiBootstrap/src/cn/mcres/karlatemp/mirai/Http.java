/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/19 19:10:50
 *
 * MiraiPlugins/MiraiPlugins/Http.java
 */

package cn.mcres.karlatemp.mirai;

import cn.mcres.karlatemp.mxlib.tools.Toolkit;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Future;

@SuppressWarnings("SpellCheckingInspection")
public class Http {
    public static final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
            .disableCookieManagement()
            .setUserAgent("Java/" + System.getProperty("java.version"))
            .setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(500)
                    .build())
//            .setConnectionManager()
            .build();

    private static final File temp = new File("temp");

    static {
        client.start();
        temp.mkdirs();
    }


    public static File download(String name, String url) {
        File file = new File(temp, name + ".jpg");
        if (file.isFile()) return file;
        file.getParentFile().mkdirs();
        try {
            URL url0 = new URL(url);
            final URLConnection connection = url0.openConnection();
            try (FileOutputStream stream = new FileOutputStream(file)) {
                Toolkit.IO.writeTo(connection.getInputStream(), stream);
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
            return file;
        } catch (Throwable any) {
            any.printStackTrace();
            return null;
        }
    }

    public static Future<SimpleHttpResponse> hitokoto(String params, @NotNull FutureCallback<SimpleHttpResponse> callback) {
        if (params == null) {
            params = "https://v1.hitokoto.cn";
        } else {
            params = "https://v1.hitokoto.cn/?" + params;
        }
        return client.execute(SimpleHttpRequest.copy(new HttpGet(params)), callback);
    }
}
