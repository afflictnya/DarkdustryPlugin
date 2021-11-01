package pandorum.events;

import arc.util.CommandHandler.Command;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Strings;
import mindustry.gen.Player;
import pandorum.Misc;
import pandorum.comp.Bundle;

import static mindustry.Vars.netServer;

public class InvalidCommandResponse {
    public static String response(Player player, CommandResponse response) {
        if (response.type == ResponseType.manyArguments) {
            return Bundle.format("commands.unknown.many-arguments", Misc.findLocale(player.locale), response.command.text, response.command.paramText);
        } else if (response.type == ResponseType.fewArguments) {
            return Bundle.format("commands.unknown.few-arguments", Misc.findLocale(player.locale), response.command.text, response.command.paramText);
        } else {
            int minDst = 0;
            Command closest = null;

            for (Command command : netServer.clientCommands.getCommandList()) {
                int dst = Strings.levenshtein(command.text, response.runCommand);
                if (dst < 3 && (closest == null || dst < minDst)) {
                    minDst = dst;
                    closest = command;
                }
            }

            if (closest != null) {
                return Bundle.format("commands.unknown.closest", Misc.findLocale(player.locale), closest.text);
            } else {
                return Bundle.format("commands.unknown", Misc.findLocale(player.locale));
            }
        }
    }
}
