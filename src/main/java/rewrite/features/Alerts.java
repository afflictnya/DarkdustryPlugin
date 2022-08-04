package rewrite.features;

import arc.func.Boolp;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.game.EventType.*;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import rewrite.components.Icons;
import rewrite.utils.Cooldowns;

import static mindustry.Vars.*;
import static rewrite.PluginVars.*;
import static rewrite.components.Bundle.*;
import static rewrite.components.Database.*;

public class Alerts {

    /** Блоки, которые опасно строить рядом с ядром. */
    public static final ObjectMap<Block, Boolp> dangerousBuildBlocks = new ObjectMap<>();
    /** Блоки, в которые опасно переносить конкретные ресурсы. */
    public static final ObjectMap<Block, Item> dangerousDepositBlocks = new ObjectMap<>();

    public static boolean enabled() {
        return config.mode.isDefault();
    }

    public static void load() {
        dangerousBuildBlocks.put(Blocks.incinerator,   () -> !state.rules.infiniteResources);
        dangerousBuildBlocks.put(Blocks.thoriumReactor, () -> state.rules.reactorExplosions);

        dangerousDepositBlocks.put(Blocks.combustionGenerator, Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.steamGenerator,      Items.blastCompound);
        dangerousDepositBlocks.put(Blocks.thoriumReactor,      Items.thorium);
    }

    public static void buildAlert(BuildSelectEvent event) {
        if (!enabled() || !isDangerous(event.builder.buildPlan().block, event.team, event.tile) || !Cooldowns.runnable("", "alerts")) return;
        Cooldowns.runned("", "alerts");

        event.team.data().players.each(Alerts::isAlertsEnabled, player -> {
            bundled(player, "alerts.dangerous-building", event.builder.getPlayer().name, Icons.get(event.builder.buildPlan().block.name), event.tile.x, event.tile.y);
        });
    }

    public static void depositAlert(DepositEvent event) {
        if (!enabled() || !isDangerous(event.tile, event.tile.team, event.item)) return;

        event.player.team().data().players.each(Alerts::isAlertsEnabled, player -> {
            bundled(player, "alerts.dangerous-deposit", event.player.name, Icons.get(event.item.name), Icons.get(event.tile.block.name), event.tile.tileX(), event.tile.tileY());
        });
    }

    private static boolean isDangerous(Block block, Team team, Tile tile) {
        return dangerousBuildBlocks.containsKey(block) && dangerousBuildBlocks.get(block).get() && isNearCore(team, tile);
    }

    private static boolean isDangerous(Building build, Team team, Item item) {
        return dangerousDepositBlocks.containsKey(build.block) && dangerousDepositBlocks.get(build.block) == item && isNearCore(team, build);
    }

    private static boolean isNearCore(Team team, Position position) {
        return team.cores().contains(core -> core.dst(position) < alertsDistance);
    }

    private static boolean isAlertsEnabled(Player player) {
        return getPlayerData(player.uuid()).alertsEnabled;
    }
}