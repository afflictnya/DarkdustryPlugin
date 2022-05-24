package pandorum.features;

import arc.struct.Seq;
import pandorum.data.PlayerData;

import static pandorum.data.Database.getPlayerData;
import static pandorum.data.Database.setPlayerData;

public class Ranks {

    public static Rank player;
    public static Rank active;
    public static Rank activePlus;
    public static Rank veteran;
    public static Rank contributor;
    public static Rank admin;

    public static void load() {
        player = new Rank() {{
            tag = "";
            name = "player";
            displayName = "[accent]Player";

            next = active = new Rank() {{
                tag = "[#ffd37f]<[white]\uE800[#ffd37f]> ";
                name = "active";
                displayName = "[sky]Active";
                req = new Requirements(300 * 60, 25000, 20);

                next = activePlus = new Rank() {{
                    tag = "[#ffd37f]<[white]\uE813[#ffd37f]> ";
                    name = "active+";
                    displayName = "[cyan]Active+";
                    req = new Requirements(750 * 60, 50000, 40);

                    next = veteran = new Rank() {{
                        tag = "[#ffd37f]<[gold]\uE809[#ffd37f]> ";
                        name = "veteran";
                        displayName = "[gold]Veteran";
                        req = new Requirements(1500 * 60, 100000, 100);
                    }};
                }};
            }};
        }};

        contributor = new Rank() {{
            tag = "[#ffd37f]<[yellow]\uE80F[#ffd37f]> ";
            name = "contributor";
            displayName = "[lime]Contributor";
        }};

        admin = new Rank() {{
            tag = "[#ffd37f]<[scarlet]\uE817[#ffd37f]> ";
            name = "admin";
            displayName = "[scarlet]Admin";
        }};
    }

    public static Rank getRank(int id) {
        return Rank.ranks.get(id);
    }

    public static void setRank(String uuid, Rank rank) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) return;

        data.rank = rank.id;

        if (rank.req != null) {
            data.playTime = rank.req.playTime;
            data.buildingsBuilt = rank.req.buildingsBuilt;
            data.gamesPlayed = rank.req.gamesPlayed;
        }

        setPlayerData(uuid, data);
    }

    public static class Rank {
        public static final Seq<Rank> ranks = new Seq<>();

        public String tag = "";
        public String name = "";
        public String displayName = "";

        public int id;

        public Requirements req = null;
        public Rank next = null;

        public Rank() {
            this.id = ranks.size;
            ranks.add(this);
        }

        public boolean hasNext() {
            return next != null && next.req != null;
        }
    }

    public static class Requirements {
        public final int playTime;
        public final int buildingsBuilt;
        public final int gamesPlayed;

        public Requirements(int playTime, int buildingsBuilt, int gamesPlayed) {
            this.playTime = playTime;
            this.buildingsBuilt = buildingsBuilt;
            this.gamesPlayed = gamesPlayed;
        }

        public boolean check(int playTime, int buildingsBuilt, int gamesPlayed) {
            return playTime >= this.playTime && buildingsBuilt >= this.buildingsBuilt && gamesPlayed >= this.gamesPlayed;
        }
    }
}
