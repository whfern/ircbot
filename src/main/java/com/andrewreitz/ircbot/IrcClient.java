package com.andrewreitz.ircbot;

import com.andrewreitz.ircbot.config.Config;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.andrewreitz.ircbot.IrcClient.Command.JOIN;
import static com.andrewreitz.ircbot.IrcClient.Command.NICK;
import static com.andrewreitz.ircbot.IrcClient.Command.PING;
import static com.andrewreitz.ircbot.IrcClient.Command.PONG;
import static com.andrewreitz.ircbot.IrcClient.Command.PRIVMSG;
import static com.andrewreitz.ircbot.IrcClient.Command.USER;

public class IrcClient {
  enum Command {
    NICK("NICK %s\n\r"),
    USER("USER %s 8 * :%s\r\n"),
    JOIN("JOIN #%s\n\r"),
    PING(""),
    PONG("PONG %s\n\r"),
    PRIVMSG("PRIVMSG #%s :%s\n\r");

    final String command;

    private Command(String command) {
      this.command = command;
    }
  }

  private final Logger logger = LoggerFactory.getLogger(IrcClient.class);

  private final Config config;
  private final CommandParser commandParser;
  private final RxClient<String, String> client;

  public IrcClient(Config config, CommandParser commandParser) {
    this.config = config;
    this.commandParser = commandParser;

    client = RxNetty.createTcpClient(config.getServer(), config.getPort(),
        PipelineConfigurators.textOnlyConfigurator());
  }

  public void start() {
    logger.debug("Connecting to {}:{}", config.getServer(), config.getPort());

    Iterable<String> messages = client.connect()
        .flatMap(connection -> {

          Observable<String> nickCommand = connection.writeAndFlush(createMessage(NICK, config.getNick()))
              .map(aVoid -> {
                logger.debug("Setting NICK to {}", config.getNick());
                return "";
              });

          Observable<String> userCommand = connection.writeAndFlush(
              createMessage(USER, config.getUsername(), config.getFullname()))
              .map(aVoid -> {
                logger.debug("Setting user");
                return "";
              });

          Observable<String> joinCommand = connection.writeAndFlush(
              createMessage(JOIN, config.getChannel()))
              .map(aVoid -> {
                logger.debug("Joining channel {}", config.getChannel());
                return "";
              });

          Observable<String> fromServer = connection.getInput()
              .map(s -> {
                s = s.trim();
                if (s.startsWith(PING.name())) {
                  String[] messageChunks = s.replace(":", "").split(" ");
                  String server = messageChunks[messageChunks.length - 1];
                  connection.writeAndFlush(createMessage(PONG, server))
                      .subscribe(aVoid -> logger.debug("Sending PONG to server"));
                }

                if (s.contains(PRIVMSG.name())) {
                  String message = s.substring(s.lastIndexOf(":") + 1);
                  String response = commandParser.parseMessage(message);
                  if (!Strings.isNullOrEmpty(response)) {
                    connection.writeAndFlush(createMessage(PRIVMSG, config.getChannel(), response))
                        .subscribe(aVoid -> logger.debug("Sending message {}", response));
                  }
                }

                return s;
              });

          return Observable.merge(Observable.concat(nickCommand, userCommand, joinCommand), fromServer);
        })
        .toBlocking()
        .toIterable();

    for (String message : messages) {
      System.out.println(message);
    }
  }

  private static String createMessage(Command command, Object... args) {
    return String.format(command.command, args);
  }
}
