package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPrevention extends RegionProtectionBase {

    public GriefPrevention() {
        super("GriefPrevention");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final String reason = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.allowBuild(player, location);

        final Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, true, null);

        if (reason != null && claim != null) {
            return true;
        }

        return false;
    }
}
