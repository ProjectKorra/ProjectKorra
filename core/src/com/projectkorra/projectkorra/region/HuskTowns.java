package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;

import net.william278.husktowns.libraries.cloplib.operation.OperationType;
import net.william278.husktowns.api.BukkitHuskTownsAPI;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.user.OnlineUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class HuskTowns extends RegionProtectionBase {

    protected BukkitHuskTownsAPI huskTownsAPI;

    protected HuskTowns() {
        super("HuskTowns");
        this.huskTownsAPI = BukkitHuskTownsAPI.getInstance();
    }


    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility) {
        final OnlineUser user = huskTownsAPI.getOnlineUser(player);
        final Position position = huskTownsAPI.getPosition(location);
        return !huskTownsAPI.isOperationAllowed(user, OperationType.PLAYER_DAMAGE_PLAYER, position);
    }
}