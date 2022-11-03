package darkdustry.components;

import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import darkdustry.DarkdustryPlugin;
import darkdustry.features.Ranks;
import darkdustry.features.Ranks.Rank;
import mindustry.gen.Player;
import reactor.core.publisher.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static darkdustry.PluginVars.config;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

public class Database {

    public static MongoClient client;
    public static MongoCollection<PlayerData> collection;

    public static void connect() {
        try {
            client = MongoClients.create(config.mongoUrl);
            collection = client.getDatabase("darkdustry")
                    .withCodecRegistry(fromRegistries(getDefaultCodecRegistry(), fromProviders(builder().automatic(true).build())))
                    .getCollection("players", PlayerData.class);

            DarkdustryPlugin.info("Database connected.");
        } catch (Exception e) {
            DarkdustryPlugin.error("Failed to connect to the database: @", e);
        }
    }

    public static void exit() {
        client.close();
    }

    public static Mono<PlayerData> getPlayerData(Player player) {
        return getPlayerData(player.uuid());
    }

    public static Mono<PlayerData> getPlayerData(String uuid) {
        return Mono.from(collection.find(eq("uuid", uuid))).defaultIfEmpty(new PlayerData(uuid));
    }

    public static Flux<PlayerData> getPlayersData(Iterable<Player> players) {
        return Flux.fromIterable(players).flatMap(Database::getPlayerData);
    }

    public static Mono<UpdateResult> setPlayerData(PlayerData data) {
        return Mono.from(collection.replaceOne(eq("uuid", data.uuid), data, new ReplaceOptions().upsert(true)));
    }

    public static Mono<Void> setPlayersData(Iterable<PlayerData> datas) {
        return Flux.fromIterable(datas).flatMap(Database::setPlayerData).then();
    }

    public static class PlayerData {
        public String uuid;
        public String language = "off";

        public boolean alerts = true;
        public boolean effects = true;
        public boolean doubleTapHistory = false;
        public boolean welcomeMessage = true;

        public int playTime = 0;
        public int buildingsBuilt = 0;
        public int gamesPlayed = 0;

        public int rank = 0;

        @SuppressWarnings("unused")
        public PlayerData() {}

        public PlayerData(String uuid) {
            this.uuid = uuid;
        }

        public Rank rank() {
            return Ranks.all.get(rank);
        }

        public void rank(Rank rank) {
            this.rank = rank.id;
        }
    }
}