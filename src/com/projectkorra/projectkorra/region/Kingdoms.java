package com.projectkorra.projectkorra.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.StandardRelationAttribute;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.structures.objects.Regulator;
import org.kingdoms.constants.player.KingdomPlayer;
import org.kingdoms.constants.player.StandardKingdomPermission;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

class Kingdoms extends RegionProtectionBase {

    protected Kingdoms() {
        super("Kingdoms", "Kingdoms.Respect");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player);
        final Land land = Land.getLand(location);
        
        // If land is not claimed, no region protection is in place.
        if (land == null || !land.isClaimed()) {
            return false;
        }
        
        // Allow bending if regulator allows building on land
        if (land.getStructure(Regulator.class) != null && land.getStructure(Regulator.class).hasAttribute(player, Regulator.Attribute.BUILD)) {
            return false;
        }
        
        // Check conditions where player has kingdom
        if (kPlayer.hasKingdom()) {
            final Kingdom kingdom = land.getKingdom();
            final Kingdom pKingdom = kPlayer.getKingdom();
            
            // If player in own land, only need to check if they have build perms
            if (pKingdom.equals(kingdom)) {
                if (kPlayer.hasPermission(StandardKingdomPermission.BUILD)) {
                    return false;
                }
            } else {
                // If player is in a land with a build relation attribute
                if (kingdom.hasAttribute(pKingdom, StandardRelationAttribute.BUILD)) {
                    return false;
                }
                
                // If player is invading a land where protect during invasions is turned off
                final boolean protectDuringInvasions = ConfigManager.getConfig().getBoolean("Properties.RegionProtection.Kingdoms.ProtectDuringInvasions");
                if (!protectDuringInvasions && !land.getInvasions().isEmpty() && land.getInvasions().values().stream().anyMatch(i -> i.getAttacker().equals(pKingdom))) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
