package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.angeschossen.lands.api.flags.Flag;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.flags.types.LandFlag;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Lands extends RegionProtectionBase {

    protected LandsIntegration landsIntegration;

    protected Lands() {
        super("Lands");

        this.landsIntegration = new LandsIntegration(ProjectKorra.plugin);
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final Area area = landsIntegration.getAreaByLoc(location);
        final boolean isClaimed = landsIntegration.isClaimed(location);


        if (isClaimed) {
            if (igniteAbility && !area.hasFlag(player.getUniqueId(), Flags.BLOCK_IGNITE)) return true;
            if (explosiveAbility && !area.hasFlag(Flags.TNT_GRIEFING)) return true;
            return !area.hasFlag(player.getUniqueId(), Flags.BLOCK_BREAK);
        }

        return false;
    }
}
