//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package hu.mrbarneyy.mythiclevels;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AddKillPlayerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player victim;
    private Player killer;

    public AddKillPlayerEvent(Player victim, Player killer) {
        this.victim = victim;
        this.killer = killer;
    }

    public Player getVictim() {
        return this.victim;
    }

    public Player getKiller() {
        return this.killer;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
