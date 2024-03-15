//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package hu.mrbarneyy.mythiclevels;

import java.util.Map;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PapiSupport extends PlaceholderExpansion {
    private MythicLevels plugin;

    public PapiSupport(MythicLevels instance) {
        this.plugin = instance;
        this.register();
    }

    public String getVersion() {
        return "1.0";
    }

    public String getIdentifier() {
        return "mythiclevels";
    }

    public String getAuthor() {
        return "MrBarneyy";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        String[] args;
        int i;
        Map.Entry top;
        if (identifier.toLowerCase().startsWith("top_")) {
            try {
                args = identifier.split("_");
                i = Integer.parseInt(args[1]);
                top = this.plugin.getTop(i - 1);
                if (args[2].equalsIgnoreCase("name")) {
                    return (String)top.getKey();
                }

                if (args[2].equalsIgnoreCase("kills")) {
                    return String.valueOf(((PlayerData)top.getValue()).getKills());
                }

                if (args[2].equalsIgnoreCase("deaths")) {
                    return String.valueOf(((PlayerData)top.getValue()).getDeaths());
                }

                return args[2] + "???";
            } catch (Exception var8) {
                (new RuntimeException("Failed to parse \"%mythicguild_" + identifier + "%\"", var8)).printStackTrace();
            }
        } else if (identifier.toLowerCase().startsWith("mtop_")) {
            try {
                args = identifier.split("_");
                i = Integer.parseInt(args[1]);
                top = this.plugin.getTopMonthly(i - 1);
                if (args[2].equalsIgnoreCase("name")) {
                    return (String)top.getKey();
                }

                if (args[2].equalsIgnoreCase("kills")) {
                    return String.valueOf(((PlayerData)top.getValue()).getKills());
                }

                if (args[2].equalsIgnoreCase("deaths")) {
                    return String.valueOf(((PlayerData)top.getValue()).getDeaths());
                }

                return args[2] + "???";
            } catch (Exception var7) {
                (new RuntimeException("Failed to parse \"%mythicguild_" + identifier + "%\"", var7)).printStackTrace();
            }
        }

        if (player == null) {
            return null;
        } else if (identifier.equalsIgnoreCase("kills")) {
            return String.valueOf(this.plugin.getPlayerData(player) == null ? 0 : this.plugin.getPlayerData(player).getKills());
        } else {
            return identifier.equalsIgnoreCase("deaths") ? String.valueOf(this.plugin.getPlayerData(player) == null ? 0 : this.plugin.getPlayerData(player).getDeaths()) : null;
        }
    }

    public String format(long val) {
        if (val < 10000L) {
            return String.valueOf(val);
        } else {
            return val < 1000000L ? String.valueOf(val / 1000L) + "K" : String.valueOf(Math.floor((double)val / 10000.0) / 100.0) + "M";
        }
    }
}
