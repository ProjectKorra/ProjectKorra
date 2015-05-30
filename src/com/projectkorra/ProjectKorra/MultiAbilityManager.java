package com.projectkorra.ProjectKorra;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModule;
import com.projectkorra.ProjectKorra.Ability.MultiAbility.MultiAbilityModuleManager;

public class MultiAbilityManager {

	public static ConcurrentHashMap<Player, HashMap<Integer, String>> playerAbilities = new ConcurrentHashMap<Player, HashMap<Integer, String>>();
	public static ConcurrentHashMap<Player, Integer> playerSlot = new ConcurrentHashMap<Player, Integer>();
	public static ConcurrentHashMap<Player, String> playerBoundAbility = new ConcurrentHashMap<Player, String>();

	public MultiAbilityManager() {
		manage();
	}
	
	/**
	 * Binds the "Fake" abilities.
	 * @param player
	 * @param multiAbility
	 */
	public static void bindMultiAbility(Player player, String multiAbility){
		if(playerAbilities.containsKey(player))
			unbindMultiAbility(player);
		playerSlot.put(player, player.getInventory().getHeldItemSlot());
		playerBoundAbility.put(player, multiAbility);
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		HashMap<Integer, String> currAbilities = new HashMap<Integer, String>();
		for(int i : bPlayer.getAbilities().keySet()){
			currAbilities.put(i, bPlayer.getAbilities().get(i));
		}
		playerAbilities.put(player, currAbilities);

		List<String> modes = MultiAbilityModuleManager.multiAbilities.get(multiAbility);

		bPlayer.getAbilities().clear();
		for(int i = 0; i < modes.size(); i++)
			bPlayer.getAbilities().put(i+1, modes.get(i));

		if(player.isOnline()){
			bPlayer.addCooldown("MAM_Setup", 1L); //Support for bending scoreboards.
			player.getInventory().setHeldItemSlot(0);
		}
	}

	/**
	 * Unbinds the "Fake" abilities.
	 * @param player
	 */
	public static void unbindMultiAbility(Player player){
		if(playerAbilities.containsKey(player)){
			HashMap<Integer, String> prevBinds = playerAbilities.get(player);
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
			int lastNonNull = -1;
			for(int i = 1; i < 10; i++){
				if(prevBinds.get(i) != null)
					lastNonNull = i;
				bPlayer.getAbilities().put(i, prevBinds.get(i));
			}
			if(lastNonNull > -1)
				GeneralMethods.saveAbility(bPlayer, lastNonNull, prevBinds.get(lastNonNull));

			if(player.isOnline())
				bPlayer.addCooldown("MAM_Setup", 1L); //Support for bending scoreboards.
			playerAbilities.remove(player);
		}

		if(playerSlot.containsKey(player)){
			if(player.isOnline())
				player.getInventory().setHeldItemSlot(playerSlot.get(player));
			playerSlot.remove(player);
		}else{
			if(player.isOnline())
				player.getInventory().setHeldItemSlot(0);
		}

		if(playerBoundAbility.containsKey(player))
			playerBoundAbility.remove(player);
	}

	/**
	 * MultiAbility equivalent of GeneralMethods.getBoundAbility()
	 * @param player
	 * @param multiAbility
	 * @return
	 */
	public static boolean hasMultiAbilityBound(Player player, String multiAbility){
		if(playerAbilities.containsKey(player)){
			if(!MultiAbilityModuleManager.multiAbilities.get(multiAbility).contains(GeneralMethods.getBoundAbility(player)) && GeneralMethods.getBoundAbility(player) != null)
				return false;
			return true;
		}
		return false;
	}

	public static void manage(){
		new BukkitRunnable() {
			public void run() {
				scrollHotBarSlots();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public static void scrollHotBarSlots(){
		if(!playerAbilities.isEmpty()){
			for(Player player : playerAbilities.keySet()){
				if(playerBoundAbility.containsKey(player)){
					if(GeneralMethods.getBoundAbility(player) == null){
						if(MultiAbilityModuleManager.multiAbilities.containsKey(playerBoundAbility.get(player))){
							if(player.getInventory().getHeldItemSlot() > MultiAbilityModuleManager.multiAbilities.get(playerBoundAbility.get(player)).size()){
								player.getInventory().setHeldItemSlot(MultiAbilityModuleManager.multiAbilities.get(playerBoundAbility.get(player)).size() - 1);
							}else{
								player.getInventory().setHeldItemSlot(0);
							}
						}
					}
				}
			}
		}
	}

	public static void remove(Player player){
		playerAbilities.remove(player);
		playerBoundAbility.remove(player);
		playerSlot.remove(player);
	}
	
	public static void removeAll(){
		List<MultiAbilityModule> abilities = MultiAbilityModuleManager.multiAbility;
		for(MultiAbilityModule mam: abilities)
			mam.stop();
		
		playerAbilities.clear();
		playerSlot.clear();
		playerBoundAbility.clear();
	}
}
