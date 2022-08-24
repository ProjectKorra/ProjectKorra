package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kingdoms.constants.kingdom.Kingdom;
import org.kingdoms.constants.kingdom.model.KingdomRelation;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.structures.managers.Regulator;
import org.kingdoms.constants.player.DefaultKingdomPermission;
import org.kingdoms.constants.player.KingdomPlayer;

public class Kingdoms extends RegionProtectionBase {

    public Kingdoms() {
        super("Kingdoms", "Kingdoms.Respect");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean harmless, boolean igniteAbility, boolean explosiveAbility) {
        final KingdomPlayer kPlayer = KingdomPlayer.getKingdomPlayer(player);
        final Land land = Land.getLand(location);
        final boolean protectDuringInvasions = ConfigManager.getConfig().getBoolean("Properties.RegionProtection.Kingdoms.ProtectDuringInvasions");
        if (land != null && land.isClaimed()) {
            final Kingdom kingdom = land.getKingdom();
            if (kPlayer.isAdmin()
                    || (!protectDuringInvasions && !land.getInvasions().isEmpty() && land.getInvasions().values().stream().anyMatch(i -> i.getInvader().equals(kPlayer))) // Protection during invasions is off, and player is currently invading; allow
                    || (land.getStructure() != null && land.getStructure() instanceof Regulator && ((Regulator) land.getStructure()).hasAttribute(player, Regulator.Attribute.BUILD))) { // There is a regulator on site which allows the player to build; allow
                return false;
            }
            if (!kPlayer.hasKingdom() // Player has no kingdom; deny
                    || (kPlayer.getKingdom().equals(kingdom) && !kPlayer.hasPermission(DefaultKingdomPermission.BUILD)) // Player is a member of this kingdom but cannot build here; deny
                    || (!kPlayer.getKingdom().equals(kingdom) && !kPlayer.getKingdom().hasAttribute(kingdom, KingdomRelation.Attribute.BUILD))) { // Player is not a member of this kingdom and cannot build here; deny
                return true;
            }
        }

        return false;
    }
}
