package pandorum.commands.discord;

import discord4j.core.object.entity.Message;
import mindustry.maps.Map;

import static pandorum.Misc.findMap;
import static pandorum.discord.Bot.err;
import static pandorum.discord.Bot.sendFile;

public class GetMapCommand {
    public static void run(final String[] args, final Message message) {
        Map map = findMap(args[0]);
        if (map == null) {
            err(message, "Карта не найдена.", "Проверьте правильность ввода.");
            return;
        }

        try {
            sendFile(message, map.file);
        } catch (Exception e) {
            err(message, "Ошибка.", "Получить карту с сервера не удалось.");
        }
    }
}
