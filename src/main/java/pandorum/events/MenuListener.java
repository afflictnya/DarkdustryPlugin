package pandorum.events;

import arc.Events;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.ui.Menus;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bson.Document;
import pandorum.PandorumPlugin;
import pandorum.discord.BotHandler;
import pandorum.discord.BotMain;

import static pandorum.Misc.bundled;
import static pandorum.Misc.sendToChat;

public class MenuListener {
    public static void init() {
        // Приветственное сообщение (0)
        Menus.registerMenu((player, option) -> {
            if (option == 1) {
                Document playerInfo = PandorumPlugin.createInfo(player);
                playerInfo.replace("hellomsg", false);
                PandorumPlugin.savePlayerStats(player.uuid());
                bundled(player, "events.hellomsg.disabled");
            }
        });

        // Команда /despw (1)
        Menus.registerMenu((player, option) -> {
            if (option == 1) return;
            int amount = 0;

            switch (option) {
                case 0 -> {
                    amount = Groups.unit.size();
                    Groups.unit.each(Unitc::kill);
                }
                case 2 -> {
                    amount = Groups.unit.count(Unitc::isPlayer);
                    Groups.unit.each(Unitc::isPlayer, Unitc::kill);
                }
                case 3 -> {
                    amount = Groups.unit.count(u -> u.team == Team.sharded);
                    Groups.unit.each(u -> u.team == Team.sharded, Unitc::kill);
                }
                case 4 -> {
                    amount = Groups.unit.count(u -> u.team == Team.crux);
                    Groups.unit.each(u -> u.team == Team.crux, Unitc::kill);
                }
                case 5 -> {
                    player.clearUnit();
                    bundled(player, "commands.admin.despw.suicide");
                    return;
                }
            }

            bundled(player, "commands.admin.despw.success", amount);
        });

        // Команда /artv (2)
        Menus.registerMenu((player, option) -> {
            if (option == 0) {
                Events.fire(new EventType.GameOverEvent(Team.crux));
                sendToChat("commands.admin.artv.info");

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(BotMain.normalColor)
                        .setAuthor(Strings.stripColors(player.name))
                        .setTitle("Админ принудительно завершил игру.");

                BotHandler.botChannel.sendMessageEmbeds(embed.build()).queue();
            }
        });

        //Информация о игроке (3)
        Menus.registerMenu((player, option) -> {
            //Пока что не делает ничего
        });
    }
}
