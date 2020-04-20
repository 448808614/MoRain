/*
 * Copyright (c) 2018-2020 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 * @create 2020/03/21 15:46:47
 *
 * MiraiPlugins/MiraiBootstrap/PluginLoaderManager.java
 */

package cn.mcres.karlatemp.mirai.plugin;

import kotlin.jvm.internal.Reflection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoaderManager {
    private static final ClassLoader parent = PluginLoaderManager.class.getClassLoader();
    public static final LinkedList<URLClassLoader> libraries = new LinkedList<>();
    public static final LinkedList<PluginClassLoader> plugins = new LinkedList<>();
    public static final LinkedList<Plugin> plugins0 = new LinkedList<>();
    public static final ThreadLocal<JarFile> scanning = new ThreadLocal<>();

    public static class PluginClassLoader extends URLClassLoader {
        public PluginClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        protected Class<?> findSuper(String klass) throws ClassNotFoundException {
            return super.findClass(klass);
        }

        protected Class<?> findClass(String klass) throws ClassNotFoundException {
            try {
                return super.findClass(klass);
            } catch (ClassNotFoundException notFound) {
                for (URLClassLoader loader : libraries) {
                    try {
                        return loader.loadClass(klass);
                    } catch (ClassNotFoundException ignore) {
                    }
                }
                for (PluginClassLoader loader : plugins) {
                    try {
                        return loader.findSuper(klass);
                    } catch (ClassNotFoundException ignore) {
                    }
                }
                throw notFound;
            }
        }
    }

    public static void clear() {
        libraries.clear();
        plugins.clear();
        plugins0.clear();
    }

    public static void loadLibrary(File f) {
        System.out.println("Loading library: " + f);
        try {
            libraries.add(new URLClassLoader(new URL[]{f.toURI().toURL()}, parent));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> klass) throws IllegalAccessException, InstantiationException {
        var obj = Reflection.getOrCreateKotlinClass(klass).getObjectInstance();
        if (obj == null) return (T) klass.newInstance();
        return (T) obj;
    }

    public static void loadPlugin(File f) {
        System.out.println("Loading plugin: " + f);
        try {
            PluginClassLoader loader = new PluginClassLoader(new URL[]{f.toURI().toURL()}, parent);
            plugins.add(loader);
            try (final JarFile file = new JarFile(f)) {
                scanning.set(file);
                final Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        ClassNode node = new ClassNode();
                        try (InputStream is = file.getInputStream(entry)) {
                            new ClassReader(is).accept(node, 0);
                        }
                        if (node.superName.equals("cn/mcres/karlatemp/mirai/plugin/Plugin")) {
                            System.out.println("Find instance: " + node.name);
                            Class<?> c = loader.loadClass(node.name.replace('/', '.'));
                            register(getInstance(c.asSubclass(Plugin.class))).onEnable();
                        }
                        loader.loadClass(node.name.replace('/', '.'));
                    }
                }
                loader.close();
            } finally {
                scanning.remove();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static Plugin register(Plugin plugin) {
        plugins0.add(plugin);
        return plugin;
    }
}
