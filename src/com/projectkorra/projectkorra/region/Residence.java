package com.projectkorra.projectkorra.region;

import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class Residence extends RegionProtectionBase {

    private String flag;

    protected Residence() {
        super("Residence", "Residence.Respect");

        this.flag = ConfigManager.defaultConfig.get().getString("Properties.RegionProtection.Residence.Flag", "bending");
        if (this.flag.equals("")) this.flag = "bending";
        FlagPermissions.addFlag(this.flag);
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final ResidenceInterface res = com.bekvon.bukkit.residence.Residence.getInstance().getResidenceManagerAPI();
        final ClaimedResidence claim = res.getByLoc(location);
        if (claim != null) {
            final ResidencePermissions perms = claim.getPermissions();
            if (!perms.hasApplicableFlag(player.getName(), this.flag)) {
                return true;
            }
        }

        return false;
    }
}
