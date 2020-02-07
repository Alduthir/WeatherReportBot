package com.weatherreport;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import org.json.JSONObject;
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
//        addReportEvent();
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

//    private static void addReportEvent() {
//        commands.put("report", event -> {
//            String content = event.getMessage().getContent().get();
//            List<String> command = Arrays.asList(content.split(" "));
//            Mono<MessageChannel> channel = event.getMessage().getChannel();
//            Location location = new Location();
//
//            if (command.size() > 1) {
//                try {
//                    LatLng response = location.getGeoLocation(context, command.get(1));
//                    channel.block().createMessage(String.format("City located at lat %f, lng %f", response.lat, response.lng));
//                } catch (Exception e) {
//                    if (e instanceof RequestDeniedException) {
//                        channel.block().createMessage("Request for location was denied");
//                    } else if (e instanceof InvalidRequestException) {
//                        channel.block().createMessage("I was unable to proces your request for location, it was deemed invalid.");
//                    } else if (e instanceof OverDailyLimitException) {
//                        channel.block().createMessage("Daily request limit exceeded. Try again tomorrow.");
//                    } else {
//                        channel.block().createMessage("Something went wrong. Please try again.");
//                    }
//                }
//            } else {
//                channel.block().createMessage("Please specify a location for me to check the weather at.").block();
//            }
//        });
//    }

    private static void addLocateEvent() {
        commands.put("locate", event -> {
            String content = event.getMessage().getContent().get();
            List<String> command = Arrays.asList(content.split(" "));
            Mono<MessageChannel> channel = event.getMessage().getChannel();
            FindPlaceService findPlaceService = new FindPlaceService();
            String place = command.get(1);

            if (command.size() > 1) {
                try {
                    System.out.println("Attempting to locate " + place);
                    JSONObject location = findPlaceService.getGeoLocation(place);
                    if (location.isEmpty()) {
                        channel.block().createMessage("I was unable to locate " + place);
                    } else {
                        channel.block().createMessage(
                                String.format(
                                        "%s found at lat: %8.5f lng: %8.5f",
                                        command.get(1),
                                        location.getDouble("lat"),
                                        location.getDouble("lng")
                                )
                        ).block();
                    }
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
}
