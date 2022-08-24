package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldGuard extends RegionProtectionBase {

    public WorldGuard() {
        super("WorldGuard");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, org.bukkit.Location reallocation, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        World world = reallocation.getWorld();

        if (player.hasPermission("worldguard.region.bypass." + world.getName())) return false;

        final com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
        final Location location = BukkitAdapter.adapt(reallocation);
        if (igniteAbility) {
            if (!player.hasPermission("worldguard.override.lighter")) {
                if (wg.getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world)).blockLighter) {
                    return true;
                }
            }
        }

        if (explosiveAbility) {
            if (wg.getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world)).blockTNTExplosions) {
                return true;
            }
            final StateFlag.State tntflag = wg.getPlatform().getRegionContainer().createQuery().queryState(location, WorldGuardPlugin.inst().wrapPlayer(player), Flags.TNT);
            if (tntflag != null && tntflag.equals(StateFlag.State.DENY)) {
                return true;
            }
        }
        final StateFlag bendingflag = (StateFlag) com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry().get("bending");
        if (bendingflag != null) {
            final StateFlag.State bendingflagstate = wg.getPlatform().getRegionContainer().createQuery().queryState(location, WorldGuardPlugin.inst().wrapPlayer(player), bendingflag);
            if (bendingflagstate == null && !wg.getPlatform().getRegionContainer().createQuery().testState(location, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)) {
                return true;
            }
            if (bendingflagstate != null && bendingflagstate.equals(StateFlag.State.DENY)) {
                return true;
            }
        } else {
            if (!wg.getPlatform().getRegionContainer().createQuery().testState(location, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD)) {
                return true;
            }
        }

        return false;
    }
}
