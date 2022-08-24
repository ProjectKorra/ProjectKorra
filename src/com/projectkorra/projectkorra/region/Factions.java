package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import me.markeh.factionsframework.entities.FPlayer;
import me.markeh.factionsframework.entities.FPlayers;
import me.markeh.factionsframework.entities.Faction;
import me.markeh.factionsframework.enums.Rel;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Factions extends RegionProtectionBase {

    protected Factions() {
        super("Factions");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final FPlayer fPlayer = FPlayers.getBySender(player);
        final Faction faction = me.markeh.factionsframework.entities.Factions.getFactionAt(location);
        final Rel relation = fPlayer.getRelationTo(faction);

        if (!(faction.isNone() || fPlayer.getFaction().equals(faction) || relation == Rel.ALLY)) {
            return true;
        }

        return false;
    }
}
