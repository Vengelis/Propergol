package fr.vengelis.propergol.core;

import fr.vengelis.propergol.api.API;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.utils.ConsoleLogger;
import fr.vengelis.propergol.core.application.booter.ArgumentManager;
import fr.vengelis.propergol.core.application.events.EventManager;
import fr.vengelis.propergol.core.application.handler.HandlerRecorder;
import fr.vengelis.propergol.core.application.handler.impl.BootHandler;
import fr.vengelis.propergol.core.configuration.YmlConfiguration;
import fr.vengelis.propergol.core.language.LanguageManager;
import fr.vengelis.propergol.core.plugin.PluginManager;
import fr.vengelis.propergol.core.utils.ResourceExporter;
import fr.vengelis.propergol.core.utils.VersionChecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

public class Core {

    private static Core instance;

    private final ApplicationType applicationType;
    private final API api = new API();
    private final PluginManager pluginManager = new PluginManager();
    private final ArgumentManager argumentManager = new ArgumentManager();
    private final ResourceExporter resourceExporter = new ResourceExporter();
    private final EventManager eventManager = new EventManager();
    private YmlConfiguration systemConfig;

    public Core(ApplicationType applicationType) {
        instance = this;
        this.applicationType = applicationType;
    }

    public void boot() {

        ConsoleLogger.printLine(Level.INFO, "#--------------------------------------------------------------------------------------------------------#");
        ConsoleLogger.printLine(Level.INFO, "|     ____                                                         _                                     |");
        ConsoleLogger.printLine(Level.INFO, "|    |  _ \\   _ __    ___    _ __     ___   _ __    __ _    ___   | |                                    |");
        ConsoleLogger.printLine(Level.INFO, "|    | |_) | | '__|  / _ \\  | '_ \\   / _ \\ | '__|  / _` |  / _ \\  | |                                    |");
        ConsoleLogger.printLine(Level.INFO, "|    |  __/  | |    | (_) | | |_) | |  __/ | |    | (_| | | (_) | | |                                    |");
        ConsoleLogger.printLine(Level.INFO, "|    |_|     |_|     \\___/  | .__/   \\___| |_|     \\__, |  \\___/  |_|                                    |");
        ConsoleLogger.printLine(Level.INFO, "|                           |_|                    |___/                                                 |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                                        |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                   By Vengelis_  - " + getVersion());
        ConsoleLogger.printLine(Level.INFO, "#--------------------------------------------------------------------------------------------------------#");
        ConsoleLogger.printLineBox(Level.INFO, "Application type : " + applicationType.name());

        new HandlerRecorder();
        HandlerRecorder.get().executeBoot(BootHandler.Step.BEFORE_API);

        api.boot();
        VersionChecker.check();

        resourceExporter.createFolder(API.WORKING_AREA + File.separator + "languages");
        resourceExporter.createFolder(API.WORKING_AREA + File.separator + "configs");
        resourceExporter.createFolder(API.WORKING_AREA + File.separator + "plugins");

        try {
            resourceExporter.saveResource(new File(API.WORKING_AREA), "/languages/en_US.yml", false);
            resourceExporter.saveResource(new File(API.WORKING_AREA), "/configs/system.yml", true);
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }

        this.systemConfig = new YmlConfiguration(Paths.get(API.WORKING_AREA,"configs", "system.yml"));
        ConsoleLogger.VERBOSE = (Boolean) this.systemConfig.get("system.verbose");
        if(ConsoleLogger.VERBOSE)
            ConsoleLogger.printLine(Level.FINEST, "Verbose mode enabled !");

        LanguageManager.loadLanguagesFromPath(API.WORKING_AREA + File.separator + "languages");
        LanguageManager.setCurrentLanguage(this.systemConfig.get("system.lang").toString());

        HandlerRecorder.get().executeBoot(BootHandler.Step.BEFORE_PLUGINS);
        pluginManager.loadPlugins();
        HandlerRecorder.get().executeBoot(BootHandler.Step.AFTER_PLUGINS);
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public static Core get() {
        return instance;
    }

    public static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = Core.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                ConsoleLogger.printLine(Level.SEVERE,"Sorry, unable to find version.properties");
                return null;
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            return null;
        }
    }

    public static void main(String[] args) {
        (new Core(ApplicationType.CORE)).boot();
    }
}
