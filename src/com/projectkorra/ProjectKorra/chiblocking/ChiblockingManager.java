package com.projectkorra.ProjectKorra.chiblocking;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class ChiblockingManager implements Runnable {
    
    public ProjectKorra plugin;
    
    public ChiblockingManager(ProjectKorra plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        ChiPassive.handlePassive();
        WarriorStance.progressAll();
        AcrobatStance.progressAll();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Smokescreen.removeFromHashMap(player);
        }
    }
    
}