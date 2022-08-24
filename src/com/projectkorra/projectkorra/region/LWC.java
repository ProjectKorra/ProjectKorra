package com.projectkorra.projectkorra.region;

import com.griefcraft.model.Protection;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LWC extends RegionProtectionBase {

    public LWC() {
        super("LWC");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final com.griefcraft.lwc.LWC lwc2 = com.griefcraft.lwc.LWC.getInstance();
        final Protection protection = lwc2.getProtectionCache().getProtection(location.getBlock());
        if (protection != null) {
            if (!lwc2.canAccessProtection(player, protection)) {
                return true;
            }
        }
        return false;
    }
}
