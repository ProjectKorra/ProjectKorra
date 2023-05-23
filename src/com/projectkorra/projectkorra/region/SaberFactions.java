package com.projectkorra.projectkorra.region;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class SaberFactions extends RegionProtectionBase {

    protected SaberFactions() {
        super("Factions");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility) {
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation fLoc = new FLocation(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        final Faction faction = com.massivecraft.factions.Board.getInstance().getFactionAt(fLoc);
        final Relation relation = fPlayer.getRelationTo(faction);

        if (!(faction.isWilderness() || fPlayer.getFaction().equals(faction) || relation == Relation.ALLY)) {
            return true;
        }
        return false;
    }
}
