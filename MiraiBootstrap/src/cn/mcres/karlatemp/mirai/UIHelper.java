/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/18 02:32:30
 *
 * MiraiPlugins/MiraiPlugins/UIHelper.java
 */

package cn.mcres.karlatemp.mirai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class UIHelper {
    public static String read(String title, Supplier<Component> component) throws InterruptedException {
        JFrame jframe = new JFrame(title);
        JTextField value = new JTextField();
        final AtomicBoolean invoke = new AtomicBoolean(true);
        Runnable postClose = () -> {
            synchronized (invoke) {
                invoke.set(false);
                invoke.notify();
                jframe.dispose();
            }
        };
        value.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case 27:
                    case 10:
                        jframe.dispose();
                        postClose.run();
                }
            }
        });
        jframe.setLayout(new BorderLayout(10, 5));
        jframe.add(value, BorderLayout.SOUTH);
        if (component != null)
            jframe.add(component.get(), BorderLayout.NORTH);
        jframe.pack();
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                postClose.run();
            }
        });
        jframe.setVisible(true);
        while (invoke.get()) {
            synchronized (invoke) {
                if (invoke.get()) {
                    invoke.wait();
                }
            }
        }
        return value.getText();
    }
}
