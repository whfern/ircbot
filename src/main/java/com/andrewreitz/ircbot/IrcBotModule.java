package com.andrewreitz.ircbot;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    injects = IrcBotApp.class
)
public class IrcBotModule {
  @Provides @Singleton Gson provideGson() {
    return new Gson();
  }
}
