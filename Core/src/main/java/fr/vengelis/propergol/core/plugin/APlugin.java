package fr.vengelis.propergol.core.plugin;

import fr.vengelis.propergol.api.API;
import fr.vengelis.propergol.core.utils.ConsoleLogger;
import fr.vengelis.propergol.core.application.ApplicationType;
import fr.vengelis.propergol.core.configuration.YmlConfiguration;
import fr.vengelis.propergol.core.language.LanguageManager;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class APlugin {

    private String name;
    private ApplicationType applicationType;
    private String version;
    private boolean enabled = false;

    private Map<String, YmlConfiguration> configs = new HashMap<>();

    public void load() {
        ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("plugin-loading"), name));
        onPluginLoad();
    }

    public void enable() {
        ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("plugin-enabling"), name));
        onPluginEnable();
        enabled = true;
    }

    public void disable() {
        ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("plugin-disabling"), name));
        onPluginDisable();
        enabled = false;
    }

    public abstract void onPluginDisable();
    public abstract void onPluginEnable();
    public abstract void onPluginLoad();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void registerConfig(String fileName) {
        configs.put(fileName, new YmlConfiguration(Paths.get(API.WORKING_AREA,"plugins", name, fileName + ".yml")));
    }

    public YmlConfiguration getConfig() {
        return getConfig("config");
    }

    public YmlConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }

    public boolean isEnabled() {
        return enabled;
    }

}
