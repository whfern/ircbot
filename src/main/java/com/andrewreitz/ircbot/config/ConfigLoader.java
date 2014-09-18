package com.andrewreitz.ircbot.config;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class ConfigLoader {
  private final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

  @Inject public ConfigLoader() { }

  public Config loadConfig() throws YamlException {
    InputStream configStream = ConfigLoader.class
        .getClassLoader()
        .getResourceAsStream("config.yml");
    InputStreamReader configStreamReader = new InputStreamReader(configStream);
    YamlReader yamlReader = new YamlReader(configStreamReader);
    Config config = yamlReader.read(Config.class);
    logger.debug("Config Loaded: {}", config);
    return config;
  }
}
