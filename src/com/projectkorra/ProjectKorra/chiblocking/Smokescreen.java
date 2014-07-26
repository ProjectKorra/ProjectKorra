package com.projectkorra.ProjectKorra.chiblocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class Smokescreen {

	public static HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	public static List<Integer> snowballs = new ArrayList<Integer>();
	/*
	 * TODO: Make stuff configurable
	 */
	
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Smokescreen.Cooldown");
	public static int duration = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.Smokescreen.Duration");
	public static double radius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.Smokescreen.Radius");
	
	public Smokescreen(Player player) {
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}
		
		snowballs.add(player.launchProjectile(Snowball.class).getEntityId());
		cooldowns.put(player.getName(), System.currentTimeMillis());
	}
	
	
	public static void playEffect(Location loc) {
        int z = -2;
        int x = -2;
        int y = 0;
        for(int i = 0; i < 125;i++)
        {
            Location newLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
            for(int direction = 0; direction < 8; direction++)
            {
                loc.getWorld().playEffect(newLoc, Effect.SMOKE, direction);
            }
            if(z == 2)
            {
                z = -2;
//                y++;
            }
            if(x == 2)
            {
                x = -2;
                z++;
            }
            x++;
        }
	}
	
	public static void applyBlindness(Entity entity) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration * 20, 2));
		}
	}
}
