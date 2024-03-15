//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package hu.mrbarneyy.mythiclevels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicLevels extends JavaPlugin implements Listener {
    private Map<Player, PlayerData> playerDatas = new HashMap();
    private Connection db;
    private List<Map.Entry<String, PlayerData>> topCache;
    private List<Map.Entry<String, PlayerData>> topCacheMonthly;
    private Map<String, String> lastKill;
    private Map<String, Integer> lastKillCount;
    private PapiSupport papiSupport = null;

    public MythicLevels() {
    }

    public void onEnable() {
        this.topCache = new ArrayList<>();
        this.topCacheMonthly = new ArrayList<>();
        this.lastKill = new HashMap<>();
        this.lastKillCount = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.loadDefaultConfig();
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.papiSupport = new PapiSupport(this);
        }

        // Adatbázis létrehozása és táblák létrehozása
        try {
            this.createDatabaseTables();
        } catch (SQLException e) {
            getLogger().severe("Failed to create database tables: " + e.getMessage());
            // Kilépés, ha nem sikerült a táblák létrehozása
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    private void createDatabaseTables() throws SQLException {
        Connection connection = this.getDB();
        Statement statement = connection.createStatement();

        // Adatbázis létrehozása
        String dbName = this.getConfig().getString("mysql.dbname");
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);

        // Tábla létrehozása, ha nem létezik
        String table = this.getConfig().getString("mysql.table.tablename");
        String usernameField = this.getConfig().getString("mysql.table.username");
        String killsField = this.getConfig().getString("mysql.table.kills");
        String deathsField = this.getConfig().getString("mysql.table.deaths");
        String streakField = this.getConfig().getString("mysql.table.streak");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " ("
                + usernameField + " VARCHAR(16) PRIMARY KEY,"
                + killsField + " INT NOT NULL DEFAULT 0,"
                + deathsField + " INT NOT NULL DEFAULT 0,"
                + streakField + " INT NOT NULL DEFAULT 0"
                + ")");

        statement.close();
        connection.close();
    }

    public void onDisable() {
        this.getLogger().info("Save player datas!");
        Iterator var2 = this.getServer().getOnlinePlayers().iterator();

        while(var2.hasNext()) {
            Player player = (Player)var2.next();
            PlayerData playerData = (PlayerData)this.playerDatas.get(player);
            this.savePlayerSync(playerData);
        }

        this.playerDatas.clear();
        if (this.papiSupport != null && this.papiSupport.isRegistered()) {
            this.papiSupport.unregister();
        }

    }

    public void loadDefaultConfig() {
        this.reloadConfig();
        this.setDefaultConfig("language.chatprefix", "&7[MythicLevels] ");
        this.setDefaultConfig("language.noperm", "&cNincs jogod a parancs használatához!");
        this.setDefaultConfig("language.reload", "&aConfig újra betöltve!");
        this.setDefaultConfig("commands.kill.player", Arrays.asList("eco give <player> 100"));
        this.setDefaultConfig("commands.kill.mob", Arrays.asList("eco give <player> 50"));
        this.setDefaultConfig("commands.death.player", Arrays.asList("eco take <player> 20"));
        this.setDefaultConfig("commands.death.mob", Arrays.asList("eco take <player> 10"));
        this.setDefaultConfig("message.kill.global", Arrays.asList("&c<player>[<player_kills>] meghalt <killer>[<killer_kills>] keze által."));
        this.setDefaultConfig("message.kill.player", Arrays.asList("&aMegölted <player> játékost!", "&2Gratulálunk!"));
        this.setDefaultConfig("message.kill.farm", Arrays.asList("&aMegölted <player>[<player_kills>] játékost!", "&cDe ez már killfarmnak minősül!"));
        this.setDefaultConfig("message.kill.mob", Arrays.asList("&aMegölted <player> játékost!", "&2Gratulálunk!"));
        this.setDefaultConfig("message.death.player", Arrays.asList("&4Megölt <killer> játékos!", "&6Sajnáljuk!"));
        this.setDefaultConfig("message.death.other", Arrays.asList("&4Meghaltál!", "&6Sajnáljuk!"));
        this.setDefaultConfig("message.death.global.fall", Arrays.asList("&b<player>[<player_kills>] lezuhant a magasból!"));
        this.setDefaultConfig("message.death.global.void", Arrays.asList("&b<player>[<player_kills>] kizuhant a világból!"));
        this.setDefaultConfig("disabledwords", Arrays.asList("zombifarm", "ittsemenjen"));
        this.setDefaultConfig("mysql.host", "localhost");
        this.setDefaultConfig("mysql.port", 3306);
        this.setDefaultConfig("mysql.dbname", "database");
        this.setDefaultConfig("mysql.user", "username");
        this.setDefaultConfig("mysql.pass", "secretpassword");
        this.setDefaultConfig("mysql.table.tablename", "players");
        this.setDefaultConfig("mysql.table.username", "username");
        this.setDefaultConfig("mysql.table.kills", "kills");
        this.setDefaultConfig("mysql.table.deaths", "deaths");
        this.setDefaultConfig("mysql.table.streak", "level");
        this.saveConfig();
    }

    public void setDefaultConfig(String node, Object value) {
        this.getConfig().set(node, this.getConfig().get(node, value));
    }

    public String getMsg(String node) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("language." + node, "&4Fordítás hiányzik. (" + node + ")"));
    }

    public PlayerData getPlayerData(Player player) {
        return (PlayerData)this.playerDatas.get(player);
    }

    public Map.Entry<String, PlayerData> getTop(int i) {
        return i >= 0 && i < this.topCache.size() ? (Map.Entry)this.topCache.get(i) : null;
    }

    public Map.Entry<String, PlayerData> getTopMonthly(int i) {
        return i >= 0 && i < this.topCacheMonthly.size() ? (Map.Entry)this.topCacheMonthly.get(i) : null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final PlayerData playerData = new PlayerData(e.getPlayer());
        this.playerDatas.put(e.getPlayer(), playerData);
        final String dataTable = this.getConfig().getString("mysql.table.tablename");
        final String usernameField = this.getConfig().getString("mysql.table.username");
        final String killsField = this.getConfig().getString("mysql.table.kills");
        final String deathsField = this.getConfig().getString("mysql.table.deaths");
        final String streakField = this.getConfig().getString("mysql.table.streak");
        this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            public void run() {
                try {
                    PreparedStatement st = MythicLevels.this.getDB().prepareStatement("SELECT * FROM " + dataTable + " WHERE " + usernameField + "=?");
                    st.setString(1, e.getPlayer().getName());
                    ResultSet result = st.executeQuery();
                    if (result.next()) {
                        playerData.setKills(result.getInt(killsField));
                        playerData.setDeaths(result.getInt(deathsField));
                        playerData.setStreak(result.getInt(streakField));
                    } else {
                        st = MythicLevels.this.getDB().prepareStatement("INSERT IGNORE INTO " + dataTable + " SET " + usernameField + "=?,kills=0,deaths=0");
                        st.setString(1, e.getPlayer().getName());
                        st.execute();
                        st.close();
                    }

                    result.close();
                    st.close();
                } catch (SQLException var3) {
                    var3.printStackTrace();
                }

            }
        });
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        if (this.playerDatas.containsKey(e.getPlayer())) {
            final PlayerData data = (PlayerData)this.playerDatas.get(e.getPlayer());
            this.playerDatas.remove(e.getPlayer());
            if (data.isChanged()) {
                this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                    public void run() {
                        MythicLevels.this.savePlayerSync(data);
                    }
                });
            }
        }

    }

    @EventHandler(
            ignoreCancelled = false
    )
    public void onKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer != null && !e.getEntity().getType().equals(EntityType.PLAYER)) {
            if (this.getConfig().getStringList("disabledwords").contains(killer.getWorld().getName())) {
                return;
            }

            List<String> cmd = this.getConfig().getStringList("commands.kill.mob");
            Iterator var5 = cmd.iterator();

            while(var5.hasNext()) {
                String c = (String)var5.next();
                this.getServer().dispatchCommand(this.getServer().getConsoleSender(), c.replace("<player>", killer.getName()));
            }

            List<String> msgs = this.getConfig().getStringList("message.kill.mob");
            Iterator var6 = msgs.iterator();

            while(var6.hasNext()) {
                String msg = (String)var6.next();
                msg = msg.replace("<player>", e.getEntity().getName()).replace("<killer>", killer.getName());
                killer.sendMessage(PlaceholderAPI.setPlaceholders(killer, msg));
            }
        }

    }

    @EventHandler(
            ignoreCancelled = false
    )
    public void onDeath(PlayerDeathEvent e) {
        if (!this.getConfig().getStringList("disabledwords").contains(e.getEntity().getWorld().getName())) {
            Player killer = e.getEntity().getKiller();
            if (!e.getEntity().equals(killer)) {
                PlayerData playerData = this.getPlayerData(e.getEntity());
                PlayerData killerData = null;
                String msg;
                Iterator var8;
                List msgs;
                Iterator var13;
                if (killer == null) {
                    if (e.getEntity().getLastDamageCause() != null && e.getEntity().getLastDamageCause().getCause().equals(DamageCause.VOID)) {
                        msgs = this.getConfig().getStringList("message.death.global.void");
                        var13 = msgs.iterator();

                        while(var13.hasNext()) {
                            msg = (String)var13.next();
                            msg = msg.replace("<player>", e.getEntity().getName());
                            msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills()));
                            this.getServer().broadcastMessage(PlaceholderAPI.setPlaceholders(e.getEntity(), msg));
                        }
                    } else if (e.getEntity().getLastDamageCause() != null && e.getEntity().getLastDamageCause().getCause().equals(DamageCause.FALL)) {
                        msgs = this.getConfig().getStringList("message.death.global.fall");
                        var13 = msgs.iterator();

                        while(var13.hasNext()) {
                            msg = (String)var13.next();
                            msg = msg.replace("<player>", e.getEntity().getName());
                            msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills()));
                            this.getServer().broadcastMessage(PlaceholderAPI.setPlaceholders(e.getEntity(), msg));
                        }
                    }
                } else {
                    killerData = this.getPlayerData(killer);
                    Iterator var9;
                    if (this.lastKill.containsKey(killer.getName()) && ((String)this.lastKill.get(killer.getName())).equals(e.getEntity().getName()) && (Integer)this.lastKillCount.get(killer.getName()) >= 2) {
                        msgs = this.getConfig().getStringList("message.kill.farm");
                        var13 = msgs.iterator();

                        while(var13.hasNext()) {
                            msg = (String)var13.next();
                            msg = msg.replace("<player>", e.getEntity().getName()).replace("<killer>", killer.getName());
                            msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills())).replace("<killer_kills>", String.valueOf(killerData.getKills()));
                            killer.sendMessage(PlaceholderAPI.setPlaceholders(killer, msg));
                        }
                    } else {
                        killerData.addKill();
                        Event event = new AddKillPlayerEvent(e.getEntity(), killer);
                        this.getServer().getPluginManager().callEvent(event);
                        if (this.lastKill.containsKey(killer.getName()) && ((String)this.lastKill.get(killer.getName())).equals(e.getEntity().getName())) {
                            this.lastKillCount.put(killer.getName(), (Integer)this.lastKillCount.get(killer.getName()) + 1);
                        } else {
                            this.lastKill.put(killer.getName(), e.getEntity().getName());
                            this.lastKillCount.put(killer.getName(), 1);
                        }

                        msgs = this.getConfig().getStringList("commands.kill.player");
                        var8 = msgs.iterator();

                        while(var8.hasNext()) {
                            msg = (String)var8.next();
                            this.getServer().dispatchCommand(this.getServer().getConsoleSender(), msg.replace("<player>", killer.getName()));
                        }

                        var9 = msgs.iterator();

                        while(var9.hasNext()) {
                            msg = (String)var9.next();
                            msg = msg.replace("<player>", e.getEntity().getName()).replace("<killer>", killer.getName());
                            msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills())).replace("<killer_kills>", String.valueOf(killerData.getKills()));
                            killer.sendMessage(PlaceholderAPI.setPlaceholders(killer, msg));
                        }
                    }

                    msgs = this.getConfig().getStringList("message.kill.global");
                    var13 = msgs.iterator();

                    while(var13.hasNext()) {
                        msg = (String)var13.next();
                        msg = msg.replace("<player>", e.getEntity().getName()).replace("<killer>", killer.getName());
                        msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills())).replace("<killer_kills>", String.valueOf(killerData.getKills()));
                        var9 = this.getServer().getOnlinePlayers().iterator();

                        while(var9.hasNext()) {
                            Player player = (Player)var9.next();
                            if (player.hasPermission("mythiclevels.globalmessages")) {
                                player.sendMessage(PlaceholderAPI.setPlaceholders(killer, msg));
                            }
                        }
                    }
                }

                ((PlayerData)this.playerDatas.get(e.getEntity())).addDeath();
                msgs = this.getConfig().getStringList("commands.death." + (killer == null ? "mob" : "player"));
                var13 = msgs.iterator();

                while(var13.hasNext()) {
                    msg = (String)var13.next();
                    this.getServer().dispatchCommand(this.getServer().getConsoleSender(), msg.replace("<player>", e.getEntity().getName()));
                }

                msgs = this.getConfig().getStringList("message.death." + (killer == null ? "other" : "player"));

                for(var8 = msgs.iterator(); var8.hasNext(); e.getEntity().sendMessage(PlaceholderAPI.setPlaceholders(e.getEntity(), msg))) {
                    msg = (String)var8.next();
                    msg = msg.replace("<player>", e.getEntity().getName());
                    if (killer != null) {
                        msg = msg.replace("<killer>", killer.getName());
                    }

                    msg = msg.replace("<player_kills>", String.valueOf(playerData.getKills()));
                    if (killer != null) {
                        msg = msg.replace("<killer_kills>", String.valueOf(killerData.getKills()));
                    }
                }
            }
        }

    }



    public Connection getDB() throws SQLException {
        if (this.db == null || this.db.isClosed()) {
            String mysql_host = this.getConfig().getString("mysql.host");
            String mysql_port = this.getConfig().getString("mysql.port");
            String mysql_dbname = this.getConfig().getString("mysql.dbname");
            String mysql_user = this.getConfig().getString("mysql.user");
            String mysql_pass = this.getConfig().getString("mysql.pass");

            try {
                this.db = DriverManager.getConnection("jdbc:mysql://" + mysql_host + ":" + mysql_port + "/" + mysql_dbname + "?autoReconnect=true&useSSL=false", mysql_user, mysql_pass);
            } catch (SQLException var7) {
                var7.printStackTrace();
            }
        }

        return this.db;
    }

    private void savePlayerSync(PlayerData playerData) {
        String dataTable = this.getConfig().getString("mysql.table.tablename");
        String usernameField = this.getConfig().getString("mysql.table.username");
        String killsField = this.getConfig().getString("mysql.table.kills");
        String deathsField = this.getConfig().getString("mysql.table.deaths");
        String streakField = this.getConfig().getString("mysql.table.streak");

        try {
            PreparedStatement st = this.getDB().prepareStatement("UPDATE " + dataTable + " SET `" + killsField + "`=?," + deathsField + "=?,`" + streakField + "`=? WHERE " + usernameField + "=? LIMIT 1");
            st.setInt(1, playerData.getKills());
            st.setInt(2, playerData.getDeaths());
            st.setInt(3, playerData.getStreak());
            st.setString(4, playerData.getPlayer().getName());
            st.execute();
            st.close();
            playerData.setUnchanged();
        } catch (SQLException var8) {
            var8.printStackTrace();
        }

    }
}