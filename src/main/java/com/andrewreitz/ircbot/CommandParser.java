package com.andrewreitz.ircbot;

import com.andrewreitz.ircbot.antlr.IrcBotBaseListener;
import com.andrewreitz.ircbot.antlr.IrcBotLexer;
import com.andrewreitz.ircbot.antlr.IrcBotParser;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.FlatResponseOperator;

import static com.andrewreitz.ircbot.antlr.IrcBotParser.DictionaryContext;
import static com.andrewreitz.ircbot.antlr.IrcBotParser.DuckDuckGoContext;
import static com.andrewreitz.ircbot.antlr.IrcBotParser.GoogleContext;
import static com.andrewreitz.ircbot.antlr.IrcBotParser.LetMeGoogleThatForYouContext;
import static com.andrewreitz.ircbot.antlr.IrcBotParser.StackOverflowContext;

@Singleton
public class CommandParser {
  private final Logger logger = LoggerFactory.getLogger(CommandParser.class);

  private final Gson gson;

  @Inject CommandParser(Gson gson) {
    this.gson = gson;
  }

  public String parseMessage(String message) {
    // Get our lexer
    IrcBotLexer lexer = new IrcBotLexer(new ANTLRInputStream(message));

    // Get a list of matched tokens
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Pass the tokens to the parser
    IrcBotParser parser = new IrcBotParser(tokens);

    // Ignore Errors
    parser.addErrorListener(new BaseErrorListener());

    final AtomicReference<String> returnValue = new AtomicReference<>();
    parser.addParseListener(new IrcBotBaseListener() {
      @Override public void exitDictionary(@NotNull DictionaryContext ctx) {

      }

      @Override public void exitStackOverflow(@NotNull StackOverflowContext ctx) {

      }

      @Override public void exitDuckDuckGo(@NotNull DuckDuckGoContext ctx) {
        String url = String.format("http://api.duckduckgo.com/?q=%s&format=json",
            ctx.arg().getText().replace("\"", ""));

        try {
          String text = RxNetty.createHttpGet(url)
              .lift(FlatResponseOperator.<ByteBuf>flatResponse())
              .map(holder -> gson.fromJson(holder.getContent().toString(Charset.defaultCharset()),
                  DuckDuckGo.class).abstractText)
              .toBlocking()
              .toFuture()
              .get(1, TimeUnit.MINUTES);

          returnValue.set(text);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          logger.error("Error connecting to duck duck go", e);
        }
      }

      @Override
      public void exitLetMeGoogleThatForYou(@NotNull LetMeGoogleThatForYouContext ctx) {
        String value = "http://lmgtfy.com/?q="
            + ctx.arg()
            .getText()
            .replace("\"", "")
            .replace(" ", "");

        logger.debug("Parsed lmgtfy: returning {}", value);
        returnValue.set(value);
      }

      @Override public void exitGoogle(@NotNull GoogleContext ctx) {

      }
    });
    parser.commandLine();

    return returnValue.get();
  }

  private static final class DuckDuckGo {
    @SerializedName("AbstractText")
    final String abstractText;

    private DuckDuckGo(String abstractText) {
      this.abstractText = abstractText;
    }
  }
}
