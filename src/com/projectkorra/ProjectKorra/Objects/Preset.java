package com.projectkorra.ProjectKorra.Objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.DBConnection;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Preset {
    
    public static ConcurrentHashMap<UUID, List<Preset>> presets = new ConcurrentHashMap<UUID, List<Preset>>();
    
    UUID uuid;
    HashMap<Integer, String> abilities;
    String name;
    
    public Preset(UUID uuid, String name, HashMap<Integer, String> abilities) {
        this.uuid = uuid;
        this.name = name;
        this.abilities = abilities;
        if (!presets.containsKey(uuid)) {
            presets.put(uuid, new ArrayList<Preset>());
        }
        presets.get(uuid).add(this);
    }
    
    public static void unloadPreset(Player player) {
        UUID uuid = player.getUniqueId();
        presets.remove(uuid);
    }
    
    public static void loadPresets(final Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid = player.getUniqueId();
                if (uuid == null)
                    return;
                ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM pk_presets WHERE uuid = '" + uuid.toString() + "'");
                try {
                    if (rs2.next()) { // Presets exist.
                        int i = 0;
                        do {
                            HashMap<Integer, String> moves = new HashMap<Integer, String>();
                            for (int total = 1; total <= 9; total++) {
                                String slot = rs2.getString("slot" + total);
                                if (slot != null)
                                    moves.put(total, slot);
                            }
                            new Preset(uuid, rs2.getString("name"), moves);
                            i++;
                        } while (rs2.next());
                        ProjectKorra.log.info("Loaded " + i + " presets for " + player.getName());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(ProjectKorra.plugin);
    }
    
    @SuppressWarnings("unchecked")
    public static void bindPreset(Player player, String name) {
        BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
        if (bPlayer == null)
            return;
        if (!presets.containsKey(player.getUniqueId()))
            return;
        for (Preset preset : presets.get(player.getUniqueId())) {
            if (preset.name.equalsIgnoreCase(name)) { // We found it
                bPlayer.setAbilities((HashMap<Integer, String>) preset.abilities.clone());
            }
        }
    }
    
    public static boolean presetExists(Player player, String name) {
        if (!presets.containsKey(player.getUniqueId()))
            return false;
        boolean exists = false;
        for (Preset preset : presets.get(player.getUniqueId())) {
            if (preset.name.equalsIgnoreCase(name))
                exists = true;
        }
        return exists;
    }
    
    public static Preset getPreset(Player player, String name) {
        if (!presets.containsKey(player.getUniqueId()))
            return null;
        for (Preset preset : presets.get(player.getUniqueId())) {
            if (preset.name.equalsIgnoreCase(name))
                return preset;
        }
        return null;
    }
    
    public static HashMap<Integer, String> getPresetContents(Player player, String name) {
    	if (!presets.containsKey(player.getUniqueId()))
    		return null;
    	for (Preset preset : presets.get(player.getUniqueId())) {
    		if (preset.name.equalsIgnoreCase(name)) {
    			return preset.abilities;
    		}
    	}
    	return null;
    }
    
    public void delete() {
        DBConnection.sql.modifyQuery("DELETE FROM pk_presets WHERE uuid = '" + uuid.toString() + "' AND name = '" + name + "'");
        presets.get(uuid).remove(this);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void save() {
    	if (ProjectKorra.plugin.getConfig().getString("Storage.engine").equalsIgnoreCase("mysql")) {
    		DBConnection.sql.modifyQuery("INSERT INTO pk_presets (uuid, name) VALUES ('" + uuid.toString() + "', '" + name + "') "
    				+ "ON DUPLICATE KEY UPDATE name=VALUES(name)");
    	} else {
//    		DBConnection.sql.modifyQuery("INSERT OR IGNORE INTO pk_presets (uuid, name) VALUES ('" + uuid.toString() + "', '" + name + "')");
//    		DBConnection.sql.modifyQuery("UPDATE pk_presets SET uuid = '" + uuid.toString() + "', name = '" + name + "'");
    		DBConnection.sql.modifyQuery("INSERT OR REPLACE INTO pk_presets (uuid, name) VALUES ('" + uuid.toString() + "', '" + name + "')");
    	}
        
        /*
         * Now we know the preset exists in the SQL table, so we can manipulate
         * it normally.
         */
        for (int i = 1; i <= 9; i++) {
        	DBConnection.sql.modifyQuery("UPDATE pk_presets SET slot" + i + " = '" + (abilities.get(i) == null ? null: abilities.get(i)) + "' WHERE uuid = '" + uuid.toString() + "' AND name = '" + name + "'");
        }
    }
    
}
