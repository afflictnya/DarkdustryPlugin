package darkdustry.utils;

import arc.struct.ObjectMap;
import arc.util.Time;

public class Cooldowns {

    public static final ObjectMap<String, ObjectMap<String, Long>> cooldowns = new ObjectMap<>();
    public static final ObjectMap<String, Long> defaults = ObjectMap.of(
            "sync", 15L,
            "login", 900L,
            "votekick", 300L,
            "rtv", 60L,
            "vnw", 60L,
            "savemap", 180L,
            "loadsave", 180L
    );

    public static boolean canRun(String uuid, String command) {
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(command)) return true;
        return cooldowns.get(uuid).get(command) <= Time.millis();
    }

    public static void run(String uuid, String command) {
        cooldowns.get(uuid, ObjectMap::new).put(command, Time.millis() + defaults.get(command) * 1000L);
    }
}