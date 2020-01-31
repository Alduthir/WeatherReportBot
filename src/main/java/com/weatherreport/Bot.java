package com.weatherreport;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class Bot {

    /**
     * Prefix character the bot should listen to
     */
    private static final String prefix = ";";

    /**
     * A map of all possible commands the bot can interact with
     */
    private static final Map<String, Command> commands = new HashMap<>();

    private static final WeatherReport weatherReport = new WeatherReport();

    /* Fills the commands Map with keys and their corresponding command. */
    static {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());

        commands.put("report", event -> {
            final String content = event.getMessage().getContent().get();
            final List<String> command = Arrays.asList(content.split(" "));
            final Mono<MessageChannel> channel = event.getMessage().getChannel();

            if (command.size() > 1) {
                channel.block().createMessage((weatherReport.getWeatherReport(command.get(1)))).block();
            } else {
                channel.block().createMessage("Please specify a location for me to check the weather at.").block();
            }
        });
    }

    /**
     * @param args args
     */
    public static void main(String[] args) {
        DiscordClientBuilder builder = new DiscordClientBuilder(System.getenv("BOT_TOKEN"));
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


}
