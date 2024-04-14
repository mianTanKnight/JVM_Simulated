package org.weishen.gc_.agent;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class ConfigReader {

    static final InputStream inputStream = ConfigReader.class
            .getClassLoader()
            .getResourceAsStream("config.yaml");

    public static <T extends Config> T readConfig(Class<T> configClass) {
        Yaml yaml = new Yaml(new Constructor(configClass));
        return yaml.load(inputStream);
    }

    public interface Config {
        //Empty ;;
    }

}
