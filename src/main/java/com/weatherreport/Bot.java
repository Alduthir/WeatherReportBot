package com.weatherreport;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
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

    static {
        addPingPongEvent();
        addLocateEvent();
        addReportEvent();
        addHelpEvent();
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

    private static void addReportEvent() {
        commands.put("report", event -> {
            String content = event.getMessage().getContent().get();
            List<String> command = Arrays.asList(content.split(" "));
            Mono<MessageChannel> channel = event.getMessage().getChannel();
            GetWeatherReportService weatherReportService = new GetWeatherReportService();
            if (command.size() > 1) {
                String response = "";
                String place = command.get(1);
                try {
                    System.out.println("Attempting to locate " + place);
                    if (command.size() == 2) {
                        response = weatherReportService.getWeatherReport(place);
                    }
                    if (command.size() > 2) {
                        response = weatherReportService.getWeatherReport(place, command.get(2));
                    }
                    channel.block().createMessage(response).block();
                } catch (Exception e) {
                    System.out.println("Exception Thrown:" + e);
                }
            } else {
                channel.block().createMessage("Please specify a location for me to report the weather at").block();
            }
        });
    }


    private static void addLocateEvent() {
        commands.put("locate", event -> {
            String content = event.getMessage().getContent().get();
            List<String> command = Arrays.asList(content.split(" "));
            Mono<MessageChannel> channel = event.getMessage().getChannel();
            FindPlaceService findPlaceService = new FindPlaceService();

            if (command.size() > 1) {
                String place = command.get(1);
                try {
                    System.out.println("Attempting to locate " + place);
                    String response = findPlaceService.getGeoLocation(place);
                    channel.block().createMessage(response).block();
                } catch (Exception e) {
                    System.out.println("Exception Thrown:" + e);
                }
            } else {
                channel.block().createMessage("Please specify a location for me to locate").block();
            }
        });
    }

    private static void addPingPongEvent() {
        commands.put("ping", event -> event.getMessage()
                .getChannel().block()
                .createMessage("Pong!").block());
    }


    private static void addHelpEvent() {
        commands.put("help", event -> {
            Mono<Member> author = event.getMessage().getAuthorAsMember();
            Mono<PrivateChannel> privateChannel = author.block().getPrivateChannel();

            privateChannel.block().createMessage(
                    "Hello and thanks for using WeatherReport. You can request the position of a city using " +
                            ";locate <Cityname> or request a weather report using ;report <CityName>" +
                            "When using ;report you may also specify either metric or imperial to retrieve" +
                            " temperatures in either Fahrenheit or Celsius. example: ;report <CityName> metric"
            ).block();
        });
    }
}
