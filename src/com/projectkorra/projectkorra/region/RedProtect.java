package com.projectkorra.projectkorra.region;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class RedProtect extends RegionProtectionBase {

    protected RedProtect() {
        super("RedProtect");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final RedProtectAPI api = br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect.get().getAPI();
        final Region region = api.getRegion(location);
        if (!(region != null && region.canBuild(player))) {
            return true;
        }

        return false;
    }
}
