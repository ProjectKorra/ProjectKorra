package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class PlayerThreadMonitor implements Runnable {

    private Player player;
    private Location oldLocation;

    public PlayerThreadMonitor(Player player) {
        this.player = player;
    }

    @Override
    public void run() {

        if (!player.isOnline()) {
            ProjectKorra.log.info("Player is no longer online! Player: " + player.getName());
            return;
        }

        if (this.oldLocation != null && !Bukkit.isOwnedByCurrentRegion(oldLocation)) {
            ProjectKorra.log.info(player.getName() + " changed regions. Restarting passives.");
            onChangeRegion();
        }

        this.oldLocation = player.getLocation();
    }

    public void onChangeRegion() {
        PassiveManager.registerPassives(this.player);
    }
}
