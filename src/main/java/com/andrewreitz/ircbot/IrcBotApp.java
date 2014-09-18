package com.andrewreitz.ircbot;

import com.andrewreitz.ircbot.config.Config;
import com.andrewreitz.ircbot.config.ConfigLoader;
import com.esotericsoftware.yamlbeans.YamlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.ObjectGraph;

public class IrcBotApp {
  private final Logger logger = LoggerFactory.getLogger(IrcBotApp.class);

  @Inject ConfigLoader configLoader;
  @Inject CommandParser commandParser;

  public void start() {
    Config config = null;
    try {
      config = configLoader.loadConfig();
    } catch (YamlException e) {
      logger.error("Error loading configurations", e);
      System.exit(1);
    }

    new IrcClient(config, commandParser).start();
  }

  public static void main(String[] args) {
    ObjectGraph objectGraph = ObjectGraph.create(IrcBotModule.class);
    IrcBotApp app = objectGraph.get(IrcBotApp.class);
    app.start();
  }
}
