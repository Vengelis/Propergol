package fr.vengelis.propergol.core.configuration;

import fr.vengelis.propergol.core.utils.ConsoleLogger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class YmlConfiguration {

    private Map<String, Object> config;
    private final Path filePath;

    public YmlConfiguration(Path filePath){
        this.filePath = filePath;
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(filePath.toFile())) {
            config = yaml.load(inputStream);
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public Object get(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = config;
        Object value = null;

        for (String key : keys) {
            value = currentMap.get(key);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                break;
            }
        }

        return value;
    }

    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = config;

        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!currentMap.containsKey(key) || !(currentMap.get(key) instanceof Map)) {
                currentMap.put(key, new java.util.LinkedHashMap<>());
            }
            currentMap = (Map<String, Object>) currentMap.get(key);
        }

        currentMap.put(keys[keys.length - 1], value);
    }

    public void save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Representer representer = new Representer(options);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(representer, options);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(filePath.toFile().toPath()), StandardCharsets.UTF_8)) {
            yaml.dump(config, writer);
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }
}
