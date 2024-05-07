package com.projectkorra.projectkorra.versions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface IDamageEventPasser {

    EntityDamageByEntityEvent createEvent(Player player, Entity source, double damage);
}