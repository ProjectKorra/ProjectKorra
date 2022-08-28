package com.projectkorra.projectkorra.region;

import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Residence extends RegionProtectionBase {

    protected Residence() {
        super("Residence", "Residence.Respect");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final ResidenceInterface res = com.bekvon.bukkit.residence.Residence.getInstance().getResidenceManagerAPI();
        final ClaimedResidence claim = res.getByLoc(location);
        if (claim != null) {
            final ResidencePermissions perms = claim.getPermissions();
            if (!perms.hasApplicableFlag(player.getName(), ConfigManager.getConfig().getString("Properties.RegionProtection.Residence.Flag"))) {
                return true;
            }
        }

        return false;
    }
}
