package com.andrewreitz.ircbot.config;

public final class Config {
  private String nick;
  private String username;
  private String fullname;
  private String server;
  private int port;
  private boolean ssl;
  private String channel;

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isSsl() {
    return ssl;
  }

  public void setSsl(boolean ssl) {
    this.ssl = ssl;
  }

  public String getChannel() {
    return channel.replace("#", "");
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  @Override public String toString() {
    return "Config{"
        + "nick='" + nick + '\''
        + ", server='" + server + '\''
        + ", port='" + port + '\''
        + ", ssl=" + ssl
        + '}';
  }
}
