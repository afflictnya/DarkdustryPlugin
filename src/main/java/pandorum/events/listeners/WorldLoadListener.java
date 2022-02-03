package pandorum.events.listeners;

import arc.util.Timer;
import pandorum.struct.CacheSeq;
import pandorum.struct.Seqs;

import java.time.Duration;

import static mindustry.Vars.world;
import static pandorum.PluginVars.*;
import static pandorum.util.Utils.sendToChat;

public class WorldLoadListener {

    @SuppressWarnings("unchecked")
    public static void call() {
        if (config.historyEnabled()) {
            history = new CacheSeq[world.width()][world.height()];
            world.tiles.eachTile(tile -> history[tile.x][tile.y] = Seqs.seqBuilder().maximumSize(historyLimit).expireAfterWrite(Duration.ofMillis(expireDelay)).build());
        }

        votesSurrender.clear();
        votesRtv.clear();
        votesVnw.clear();

        mapRateVotes.clear();

        mapPlayTime = 0;
        canVote = true;

        if (worldLoadTask != null) worldLoadTask.cancel();
        worldLoadTask = Timer.schedule(() -> sendToChat("events.world-loaded"), 10f);
    }
}
