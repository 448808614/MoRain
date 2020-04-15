/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/04/15 09:03:45
 *
 * MiraiPlugins/PluginModeRun/GroupSettings.java
 */

package cn.mcres.karlatemp.mirai.pr;

import cn.mcres.karlatemp.mxlib.util.RAFOutputStream;
import com.google.common.cache.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupSettings {
    public JsonObject data;
    public final long group;
    public static final File settings = new File("group-settings");
    static final LoadingCache<Long, GroupSettings> cached =
            CacheBuilder.newBuilder()
                    .removalListener((RemovalListener<Long, GroupSettings>) notification -> {
                        notification.getValue().save();
                    })
                    .expireAfterAccess(3, TimeUnit.HOURS)
                    .build(new CacheLoader<>() {
                        @Override
                        public GroupSettings load(@NotNull Long key) throws Exception {
                            GroupSettings gs = new GroupSettings(key);
                            loadData(gs, key);
                            return gs;
                        }
                    });

    public GroupSettings(long group) {
        this.group = group;
    }

    private static void loadData(GroupSettings gs, Long key) throws IOException {
        File store = new File(settings, key + ".json");
        if (store.isFile()) {
            try (FileInputStream stream = new FileInputStream(store)) {
                try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    gs.data = JsonParser.parseReader(reader).getAsJsonObject();
                }
            }
        } else {
            gs.data = new JsonObject();
        }
    }

    public static void save(GroupSettings settings) throws IOException {
        File store = new File(GroupSettings.settings, settings.group + ".json");
        if (settings.data == null) {
            store.delete();
        } else {
            GroupSettings.settings.mkdirs();
            try (RAFOutputStream raf = new RAFOutputStream(new RandomAccessFile(store, "rw"))) {
                try (OutputStreamWriter writer = new OutputStreamWriter(raf)) {
                    JsonWriter jsonWriter = new JsonWriter(writer);
                    jsonWriter.setHtmlSafe(false);
                    jsonWriter.setSerializeNulls(false);
                    jsonWriter.setIndent("  ");
                    Streams.write(settings.data, jsonWriter);
                }
            }
        }
    }

    public void save() {
        try {
            save(this);
        } catch (IOException e) {
            Logger.getLogger("GroupSettings").log(Level.SEVERE, "Failed to store " + group + ".json", e);
        }
    }

    public static GroupSettings getSettings(long group) {
        try {
            return cached.get(group);
        } catch (ExecutionException e) {
            GroupSettings settings = new GroupSettings(group);
            cached.put(group, settings);
            return settings;
        }
    }

    public boolean getBoolean(String path) {
        JsonPrimitive p = data.getAsJsonPrimitive(path);
        if (p == null) return false;
        return p.getAsBoolean();
    }

    public String getString(String path) {
        JsonPrimitive p = data.getAsJsonPrimitive(path);
        if (p == null) return null;
        return p.getAsString();
    }
}
