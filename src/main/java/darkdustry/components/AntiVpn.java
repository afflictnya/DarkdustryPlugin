package darkdustry.components;

import arc.util.*;
import darkdustry.components.Database.VpnData;
import reactor.core.publisher.Mono;

import static darkdustry.PluginVars.*;

public class AntiVpn {

    public static Mono<VpnData> checkIp(String ip) {
        return Mono.create(sink -> Http.get(antiVpnApiUrl + ip)
                .header("X-RapidAPI-Key", config.rapidApiKey)
                .header("X-RapidAPI-Host", antiVpnApiHost)
                .error(sink::error)
                .submit(response -> {
                    var detection = reader.parse(response.getResultAsString()).get("detection");

                    boolean isVpn = detection.getBoolean("cloud") ||
                            detection.getBoolean("hosting") ||
                            detection.getBoolean("proxy") ||
                            detection.getBoolean("spamhaus") ||
                            detection.getBoolean("tor");

                    sink.success(new VpnData(ip, isVpn));
                }));
    }
}