package com.projectkorra.projectkorra.region;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.data.MemoryFaction;
import com.massivecraft.factions.perms.Relation;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class FactionsUUID extends RegionProtectionBase {

    protected FactionsUUID() {
        super("Factions");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        FLocation fLoc = new FLocation(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        final Faction faction = com.massivecraft.factions.Board.getInstance().getFactionAt(fLoc);

        //This cast is important as it lets us use the correct implementation class instead of the version-differing interface
        Relation relation = ((MemoryFaction) faction).getRelationTo(fPlayer.getFaction());

        if (!(faction.isWilderness() || fPlayer.getFaction().equals(faction) || relation == Relation.ALLY)) {
            return true;
        }
        return false;
    }
}
