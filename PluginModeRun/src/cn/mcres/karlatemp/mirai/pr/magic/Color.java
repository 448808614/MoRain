/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/30 15:49:54
 *
 * MiraiPlugins/PluginModeRun/Color.java
 */

package cn.mcres.karlatemp.mirai.pr.magic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {
    public static final Pattern reg = Pattern.compile(
            "^C#([A-Fa-f0-9]{6})$"
    );
    public static final Pattern rgb = Pattern.compile(
            "^C#rgb\\s*\\(([0-9]{1,3}),\\s*([0-9]{1,3}),\\s*([0-9]{1,3})\\)$"
    );

    public static java.awt.Color match(String str) {
        final Matcher matcher = reg.matcher(str);
        if (matcher.find()) {
            return build(matcher.group(1));
        }
        final Matcher matcher1 = rgb.matcher(str);
        if (matcher1.find()) {
            try {
                return new java.awt.Color(
                        Integer.parseInt(matcher1.group(1)) & 0xFF,
                        Integer.parseInt(matcher1.group(2)) & 0xFF,
                        Integer.parseInt(matcher1.group(3)) & 0xFF
                );
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    public static java.awt.Color build(String hex) {
        return new java.awt.Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        );
    }

    public static BufferedImage build(java.awt.Color color) {
        BufferedImage image = new BufferedImage(150, 100, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 150, 100);
        graphics.dispose();
        return image;
    }
}
