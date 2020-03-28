/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/17 19:01:35
 *
 * MiraiPlugins/MiraiPlugins/ArgumentImageToken.java
 */

package cn.mcres.karlatemp.mirai.arguments;

import net.mamoe.mirai.message.data.Image;

public class ArgumentImageToken extends ArgumentToken {
    public final Image image;

    public ArgumentImageToken(Image image) {
        super(image);
        this.image = image;
    }

    @Override
    public String toString() {
        return "Image{" + image + '}';
    }
}
