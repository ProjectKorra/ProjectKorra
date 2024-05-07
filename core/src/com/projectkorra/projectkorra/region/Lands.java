package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Lands extends RegionProtectionBase {

    protected LandsIntegration landsIntegration;

    protected Lands() {
        super("Lands");

        this.landsIntegration = LandsIntegration.of(ProjectKorra.plugin);
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility) {
        final Area area = this.landsIntegration.getArea(location);
        final boolean isClaimed = area != null;

        if (isClaimed) {
            if (igniteAbility && !area.hasFlag(player.getUniqueId(), Flags.BLOCK_IGNITE)) return true;
            if (explosiveAbility && !area.hasNaturalFlag(Flags.TNT_GRIEFING)) return true;
            return !area.hasFlag(player.getUniqueId(), Flags.BLOCK_BREAK);
        }

        return false;
    }
}
