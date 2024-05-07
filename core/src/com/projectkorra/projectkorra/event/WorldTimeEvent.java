package com.projectkorra.projectkorra.event;

import com.projectkorra.projectkorra.util.Experimental;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;

@Experimental
public class WorldTimeEvent extends WorldEvent {

    public enum Time {
        DAY, NIGHT
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private Time from;
    private Time to;

    public WorldTimeEvent(@NotNull World world, Time from, Time to) {
        super(world);

        this.from = from;
        this.to = to;
    }

    public Time getFrom() {
        return from;
    }

    public Time getTo() {
        return to;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
