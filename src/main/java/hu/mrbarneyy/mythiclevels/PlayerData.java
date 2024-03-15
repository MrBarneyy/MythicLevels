//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package hu.mrbarneyy.mythiclevels;

import org.bukkit.entity.Player;

public class PlayerData {
    private Player player;
    private String playerName;
    private int kills;
    private int deaths;
    private int mKills;
    private int mDeaths;
    private int streak;
    private boolean change = true;

    public PlayerData(Player player) {
        this.player = player;
        this.playerName = player.getName();
        this.kills = 0;
        this.deaths = 0;
        this.mKills = 0;
        this.mDeaths = 0;
        this.streak = 0;
    }

    public PlayerData(String playerName) {
        this.player = null;
        this.playerName = playerName;
        this.kills = 0;
        this.deaths = 0;
        this.mKills = 0;
        this.mDeaths = 0;
        this.streak = 0;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public int getKills() {
        return this.kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public int getMonthlyKills() {
        return this.mKills;
    }

    public int getMonthlyDeaths() {
        return this.mDeaths;
    }

    public void setKills(int k) {
        this.mKills += k - this.kills;
        this.kills = k;
        this.change = true;
    }

    public void setDeaths(int d) {
        this.mDeaths += d - this.deaths;
        this.deaths = d;
        this.change = true;
    }

    public void clearStreak() {
        this.streak = 0;
        this.change = true;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getStreak() {
        return this.streak;
    }

    public void setUnchanged() {
        this.change = false;
    }

    public boolean isChanged() {
        return this.change;
    }

    public void addKill() {
        ++this.mKills;
        ++this.kills;
        ++this.streak;
        this.change = true;
    }

    public void addDeath() {
        ++this.mDeaths;
        ++this.deaths;
        this.streak = 0;
        this.change = true;
    }
}
