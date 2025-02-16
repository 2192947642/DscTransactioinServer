package com.lgzServer.spring;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class ExternalConfigInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String EXTERNAL_CONFIG_FILE = "serverConfig.yaml";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        Properties properties = loadExternalProperties();
        if (!properties.isEmpty()) {
            PropertiesPropertySource propertySource = new PropertiesPropertySource("externalConfig", properties);
            environment.getPropertySources().addFirst(propertySource);
        }
    }

    private Properties loadExternalProperties() {
        Properties properties = new Properties();
        try {
            URL jarLocation = ExternalConfigInitializer.class.getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarLocation.toURI());
            File configFile = new File(jarFile.getParentFile(), EXTERNAL_CONFIG_FILE);

            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlMap = yaml.load(fis);
                    // 展开嵌套的Map结构为扁平属性
                    flattenMap("", yamlMap, properties);
                    System.out.println("External configuration loaded: " + configFile.getAbsolutePath());
                }
            } else {
                System.err.println("External config file not found: " + configFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to load external config: " + e.getMessage());
        }
        return properties;
    }

    private void flattenMap(String prefix, Map<String, Object> source, Properties target) {
        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                // 递归处理子Map
                flattenMap(fullKey, (Map<String, Object>) value, target);
            } else if (value != null) {
                // 将值转换为字符串并存入Properties
                target.put(fullKey, value.toString());
            }
        });
    }
}