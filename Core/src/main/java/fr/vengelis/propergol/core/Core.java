package fr.vengelis.propergol.core;

import fr.vengelis.propergol.api.API;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.communication.postgres.PostgreCommunicationSystem;
import fr.vengelis.propergol.core.communication.redis.RedisCommunicationSystem;
import fr.vengelis.propergol.core.communication.redis.RedisConnection;
import fr.vengelis.propergol.core.communication.redis.RedisResult;
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
    private final RedisCommunicationSystem redisCommunicationSystem = new RedisCommunicationSystem();
    private PostgreCommunicationSystem postgreCommunicationSystem;

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
            resourceExporter.saveResource(new File(API.WORKING_AREA), "/languages/en_US.yml", true);
            resourceExporter.saveResource(new File(API.WORKING_AREA), "/configs/system.yml", true);
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }
        this.systemConfig = new YmlConfiguration(Paths.get(API.WORKING_AREA,"configs", "system.yml"));

        LanguageManager.loadLanguagesFromPath(API.WORKING_AREA + File.separator + "languages");
        LanguageManager.setCurrentLanguage(this.systemConfig.get("system.lang").toString());

        ConsoleLogger.VERBOSE = (Boolean) this.systemConfig.get("system.verbose");
        if(ConsoleLogger.VERBOSE)
            ConsoleLogger.printLine(Level.FINEST, "Verbose mode enabled !");

        if((Boolean) this.systemConfig.get("system.communication.redis.auth.enabled")) {
            RedisConnection.create(
                    this.systemConfig.get("system.communication.redis.host").toString(),
                    this.systemConfig.get("system.communication.redis.auth.user").toString(),
                    (boolean) this.systemConfig.get("system.communication.redis.auth.has-password") ?
                            this.systemConfig.get("system.communication.redis.auth.password").toString() :
                            null,
                    (int) this.systemConfig.get("system.communication.redis.port"),
                    (int) this.systemConfig.get("system.communication.redis.database"),
                    (int) this.systemConfig.get("system.communication.redis.timeout")
            );
        } else {
            RedisConnection.create(
                    this.systemConfig.get("system.communication.redis.host").toString(),
                    null,
                    null,
                    (int) this.systemConfig.get("system.communication.redis.port"),
                    (int) this.systemConfig.get("system.communication.redis.database"),
                    (int) this.systemConfig.get("system.communication.redis.timeout")
            );
        }
        redisCommunicationSystem.boot();
        if(redisCommunicationSystem.getPubSubAPI().tryHelloWorld().getType().equals(RedisResult.Type.SUCCESS)) {
            ConsoleLogger.printLine(Level.FINER, LanguageManager.translate("redis-op"));
        } else {
            ConsoleLogger.printLine(Level.FINER, LanguageManager.translate("redis-not-op"));
        }

        if(applicationType.equals(ApplicationType.REPOSITORY)) {
            try {
                postgreCommunicationSystem = PostgreCommunicationSystem.create(
                        this.systemConfig.get("system.communication.postgre.host").toString(),
                        this.systemConfig.get("system.communication.postgre.user").toString(),
                        this.systemConfig.get("system.communication.postgre.password").toString(),
                        this.systemConfig.get("system.communication.postgre.database").toString(),
                        (int) this.systemConfig.get("system.communication.postgre.port")
                );
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e);
                ConsoleLogger.printLineBox(Level.SEVERE, "");
            }
        }
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

    public YmlConfiguration getSystemConfig() {
        return systemConfig;
    }

    public RedisCommunicationSystem getRedisCommunicationSystem() {
        return redisCommunicationSystem;
    }

    public PostgreCommunicationSystem getPostgreCommunicationSystem() {
        return postgreCommunicationSystem;
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

}
