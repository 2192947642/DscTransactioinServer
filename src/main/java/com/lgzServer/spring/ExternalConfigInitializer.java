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
            // 将外部配置添加到环境中，优先级较高
            environment.getPropertySources().addFirst(propertySource);
        }
    }
    private Properties loadExternalProperties() {
        Properties properties = new Properties();
        try {
            // 获取JAR包所在的目录路径
            URL jarLocation = ExternalConfigInitializer.class.getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarLocation.toURI());
            File configFile = new File(jarFile.getParentFile(), EXTERNAL_CONFIG_FILE);

            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlMap = yaml.load(fis);
                    properties.putAll(yamlMap);
                    System.out.println("External configuration file has been loaded: " + configFile.getAbsolutePath());
                }
            } else {
                System.err.println("External configuration file not found: " + configFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to load external configuration file: " + e.getMessage());
        }
        return properties;
    }
}
