package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.World;
import org.bukkit.entity.Player;

class WorldGuard extends RegionProtectionBase {

    protected WorldGuard() {
        super("WorldGuard");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, org.bukkit.Location reallocation, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility) {
        World world = reallocation.getWorld();

        // Check for bypass permissions
        if (player.hasPermission("worldguard.region.bypass." + world.getName())) return false;

        final com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
        final Location location = BukkitAdapter.adapt(reallocation);

        // Cache common objects
        var platform = wg.getPlatform();
        var regionContainer = platform.getRegionContainer();
        var query = regionContainer.createQuery();
        var wrappedPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        // Check bending-all-off flag
        if (isFlagDenied(location, wrappedPlayer, "bending-all-off", query)) {
            return true; // Completely block all abilities in this region
        }

        // Check ignite abilities
        if (igniteAbility && !player.hasPermission("worldguard.override.lighter")) {
            if (platform.getGlobalStateManager().get(BukkitAdapter.adapt(world)).blockLighter) {
                return true;
            }
        }

        // Check explosive abilities
        if (explosiveAbility) {
            if (platform.getGlobalStateManager().get(BukkitAdapter.adapt(world)).blockTNTExplosions) {
                return true;
            }
            StateFlag.State tntFlag = query.queryState(location, wrappedPlayer, Flags.TNT);
            if (tntFlag != null && tntFlag.equals(StateFlag.State.DENY)) {
                return true;
            }
        }

        // Check bending flag
        //if (ability != null) {
        //    player.sendMessage(ability.getName() + " is it harmless? " + ability.isHarmlessAbility());
        //}
        if (isBendingFlagDenied(location, wrappedPlayer, "bending", query, ability)) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to check if a specific flag is denied in the region.
     */
    private boolean isFlagDenied(Location location, LocalPlayer wrappedPlayer, String flagName,
                                 RegionQuery query) {
        StateFlag flag = (StateFlag) com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (flag != null) {
            StateFlag.State flagState = query.queryState(location, wrappedPlayer, flag);
            if (flagState == StateFlag.State.DENY) {
                return true;
            }
        } else {
            if (!query.testState(location, wrappedPlayer, Flags.BUILD)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Specific handling for the bending flag.
     * Allows harmless abilities if allowHarmless is true, blocks others.
     */
    private boolean isBendingFlagDenied(Location location, LocalPlayer wrappedPlayer, String flagName,
                                        RegionQuery query, CoreAbility ability) {
        boolean allowHarmless = ConfigManager.getConfig().getBoolean("Properties.RegionProtection.AllowHarmlessAbilities");
        StateFlag flag = (StateFlag) com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry().get(flagName);

        if (flag != null) {
            StateFlag.State flagState = query.queryState(location, wrappedPlayer, flag);
            if (flagState == StateFlag.State.DENY) {
                if ((ability == null || ability.isHarmlessAbility()) && allowHarmless) {
                    return false; // Allow harmless abilities when configured
                }
                return true; // Block all other abilities
            }
        } else {
            if (!query.testState(location, wrappedPlayer, Flags.BUILD)) {
                return true;
            }
        }
        return false; // Default to allowing abilities if the flag isn't set
    }
}