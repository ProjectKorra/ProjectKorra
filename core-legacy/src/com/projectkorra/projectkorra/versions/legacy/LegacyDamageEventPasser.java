package com.projectkorra.projectkorra.versions.legacy;

import com.projectkorra.projectkorra.versions.IDamageEventPasser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LegacyDamageEventPasser implements IDamageEventPasser {
    @Override
    public EntityDamageByEntityEvent createEvent(Player player, Entity source, double damage) {
        return new EntityDamageByEntityEvent(source, player, EntityDamageByEntityEvent.DamageCause.CUSTOM, damage);
    }
}