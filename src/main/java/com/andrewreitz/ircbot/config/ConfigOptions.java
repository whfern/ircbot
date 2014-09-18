package com.andrewreitz.ircbot.config;

import org.kohsuke.args4j.Option;

public final class ConfigOptions {

  @Option(name = "-n", aliases = "--name", usage = "set the name of the ircbot")
  private String name;

  @Option(name = "-s", aliases = "--server", usage = "set the server to connect to")
  private String server;

  @Option(name = "-p", aliases = "--port", usage = "set the port to connect on")
  private String port;

  @Option(name = "-l", aliases = "--ssl", usage = "use ssl")
  private boolean ssl;
}
