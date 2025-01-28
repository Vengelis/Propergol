package fr.vengelis.propergol.core.plugin;

import fr.vengelis.propergol.api.API;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.exception.BrokenPluginException;
import fr.vengelis.propergol.core.utils.ConsoleLogger;
import fr.vengelis.propergol.core.Core;
import fr.vengelis.propergol.core.language.LanguageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class PluginManager {

    private Map<String, APlugin> compatiblePlugins = new HashMap<>();
    private Map<String, APlugin> allPlugins = new HashMap<>();

    public void loadPlugins() {
        loadPlugins(Paths.get(API.WORKING_AREA, "plugins").toString());
    }

    public void loadPlugins(String folderPath) {
        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("plugin-manager-loading"));
        File[] files = new File(folderPath).listFiles((dir, name) -> name.endsWith(".jar"));

        if (files != null) {
            for (File file : files) {
                processFile(file);
            }
            compatiblePlugins.forEach((n, p) -> {
                for (Annotation annotation : p.getClass().getAnnotations()) {
                    if(annotation instanceof PropergolPlugin){
                        if(((PropergolPlugin) annotation).asDefaultConfig())
                            p.registerConfig("config");
                    }
                }
                p.load();
            });
        }
        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("plugin-manager-loading-end"));
    }

    public void enablePlugins() {
        compatiblePlugins.forEach((n, p) -> {
            if(!p.isEnabled()) {
                p.enable();
            }
        });
    }

    public void enablePlugin(String plugin) {
        if(compatiblePlugins.containsKey(plugin)) {
            APlugin p = compatiblePlugins.get(plugin);
            if(!p.isEnabled())
                p.enable();
        }
    }

    public void disablePlugins() {
        compatiblePlugins.forEach((n, p) -> {
            if(p.isEnabled()) {
                p.disable();
            }
        });
    }

    public void disablePlugin(String plugin) {
        if(compatiblePlugins.containsKey(plugin)) {
            APlugin p = compatiblePlugins.get(plugin);
            if(p.isEnabled())
                p.disable();
        }
    }



    private void processFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (isValidClassEntry(entry)) {
                    processClassEntry(entry, classLoader);
                }
                if (isValidYmlEntry(entry)) {
                    extractYmlFile(entry, jarFile, "plugins");
                }
            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenPluginException(e),
                    "The plugin jar file could not be loaded : " + file.getName(),
                    "Please check the file integrity and try again",
                    "The dependencies added to your plugins are not well implemented.",
                    "ClassNotFound errors can occur depending on the circumstances");
        }
    }

    private void processClassEntry(JarEntry entry, URLClassLoader classLoader) {
        String className = entry.getName().replace("/", ".").replace(".class", "");
        try {
            Class<?> loadedClass = classLoader.loadClass(className);
            processAnnotations(loadedClass.getAnnotations(), loadedClass);
        } catch (ClassNotFoundException e) {
            ConsoleLogger.printLine(Level.SEVERE, "The class could not be loaded : " + className);
        }
    }

    private void processAnnotations(Annotation[] annotations, Class<?> loadedClass) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof PropergolPlugin) {
                PropergolPlugin propergolPlugin = (PropergolPlugin) annotation;
                registerPlugin(propergolPlugin, loadedClass);
            }
        }
    }

    private void registerPlugin(PropergolPlugin propergolPluginAnnotation, Class<?> loadedClass) {
        if (!compatiblePlugins.containsKey(propergolPluginAnnotation.name())) {
            if(APlugin.class.isAssignableFrom(loadedClass)) {
                try {
                    APlugin pluginInstance = (APlugin) loadedClass.newInstance();
                    if(isPluginType(pluginInstance, Core.get().getApplicationType())) {
                        compatiblePlugins.put(propergolPluginAnnotation.name(), pluginInstance);
                        ConsoleLogger.printLine(Level.INFO, " - " + propergolPluginAnnotation.name() + " finded (v" + propergolPluginAnnotation.version() + ")");
                    } else {
                        ConsoleLogger.printLine(Level.INFO, " - " + propergolPluginAnnotation.name() + " skipped (ApplicationType is different from PApp)");
                    }
                    allPlugins.put(propergolPluginAnnotation.name(), pluginInstance);
                } catch (InstantiationException | IllegalAccessException e) {
                    ConsoleLogger.printStacktrace(e);
                }
            } else {
                ConsoleLogger.printLine(Level.WARNING, " - " + propergolPluginAnnotation.name() + " is not instance of APlugin. Plugin not loaded.");
            }
        } else {
            ConsoleLogger.printLine(Level.SEVERE, "A plugin with the same name already exists: " + propergolPluginAnnotation.name());
        }
    }

    public static boolean isPluginType(APlugin plugin, ApplicationType type) {
        for (Annotation annotation : plugin.getClass().getAnnotations()) {
            if(annotation instanceof PropergolPlugin) {
                if(((APlugin) annotation).getApplicationType().equals(type))
                    return true;
            }
        }
        return false;
    }

    public static boolean isValidClassEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/");
    }

    public static boolean isValidYmlEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".yml");
    }

    public static void extractYmlFile(JarEntry entry, JarFile jarFile, String zone) {
        String jarName = jarFile.getName().substring(jarFile.getName().lastIndexOf(File.separator) + 1).split("\\.")[0];
        String path = API.WORKING_AREA + File.separator + zone + File.separator + jarName;
        File outputFile = new File(path + File.separator + entry.getName());
        if (!outputFile.getParentFile().exists()) {
            try {
                outputFile.getParentFile().mkdirs();
            } catch (SecurityException e) {
                ConsoleLogger.printStacktrace(e,
                        "Unable to create directory " + outputFile.getParentFile(),
                        "Has the program the right to create directories ?",
                        "Please check the permissions.",
                        "YAML file extraction aborted.");
                return;
            }
        }
        if (!outputFile.exists()) {
            try (InputStream inputStream = jarFile.getInputStream(entry);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                ConsoleLogger.printStacktrace(e,
                        "Unable to extract " + entry.getName() + " from " + jarName,
                        "Has the program the right to write in the file ?",
                        "Please check the permissions.",
                        "YAML file extraction aborted.");
            }
        }
    }

    public APlugin getCompatiblePlugin(String name) {
        return compatiblePlugins.get(name);
    }

    public Map<String, APlugin> getCompatiblePlugins() {
        return compatiblePlugins;
    }

    public APlugin getPlugin(String name) {
        return allPlugins.get(name);
    }

    public Map<String, APlugin> getAllPlugins() {
        return allPlugins;
    }

}
