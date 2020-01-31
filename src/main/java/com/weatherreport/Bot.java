package com.weatherreport;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.*;

final public class Bot {
    /**
     * Discord bot token
     */
    private static final String token = "NjcxNjMzNzA5ODM3MDU4MDQ4.XjPnIg.NthU5XOMJY1iRpt4D1CXlbI6oGs";

    /**
     * Prefix character the bot should listen to
     */
    private static final String prefix = ";";

    /**
     * A map of all possible commands the bot can interact with
     */
    private static final Map<String, Command> commands = new HashMap<>();

    private static final WeatherReport weatherReport = new WeatherReport();

    /**
     * @param args args
     */
    public static void main(String[] args) {
        Map<String, Command> commandList = new HashMap<>();
        initCommands();

        DiscordClientBuilder builder = new DiscordClientBuilder(token);
        DiscordClient instance = builder.build();
        login(instance);

        instance.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
            final String content = event.getMessage().getContent().orElse("");
            for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                if (content.startsWith(prefix + entry.getKey())) {
                    entry.getValue().execute(event);
                    break;
                }
            }
        });

        instance.login().block();
    }

    /**
     * @param client the discord client
     */
    private static void login(DiscordClient client) {
        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    User self = event.getSelf();
                    System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
                });
    }

    /**
     * Fills the commands Map with keys and their corresponding command.
     */
    private static void initCommands() {
        commands.put("ping", event -> Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Pong!").block());

        commands.put("report", event -> {
            final String content = event.getMessage().getContent().get();
            final List<String> command = Arrays.asList(content.split(" "));
            final Mono<MessageChannel> channel = event.getMessage().getChannel();

            if (command.size() > 1) {
                Objects.requireNonNull(channel.block()).createMessage((weatherReport.getWeatherReport(command.get(1)))).block();
            } else {
                Objects.requireNonNull(channel.block()).createMessage("Please specify a location for me to check the weather at.").block();
            }
        });
    }
}
