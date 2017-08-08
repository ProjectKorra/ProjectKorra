package com.projectkorra.projectkorra.waterbending.passive;

public class WaterPassive {

	/*
	 * Hydro Sink is now managed in HydroSink.java
	 * Fast Swim is now managed in FastSwim.java
	 */
	/*
	 * public static void handlePassive() { if (Commands.isToggledForAll &&
	 * ConfigManager.defaultConfig.get().getBoolean(
	 * "Properties.TogglePassivesWithAllBending")) { return; }
	 * 
	 * double swimSpeed = getSwimSpeed();
	 * 
	 * for (Player player : Bukkit.getServer().getOnlinePlayers()) {
	 * BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player); if
	 * (bPlayer == null) { continue; }
	 * 
	 * String ability = bPlayer.getBoundAbilityName(); CoreAbility coreAbil =
	 * CoreAbility.getAbility(ability); if
	 * (bPlayer.canBendPassive(Element.WATER)) { if
	 * (CoreAbility.hasAbility(player, WaterSpout.class) ||
	 * CoreAbility.hasAbility(player, EarthArmor.class)) { continue; } else if
	 * (CoreAbility.getAbility(player, WaterArms.class) != null) { continue; }
	 * else if (coreAbil == null || (coreAbil != null &&
	 * !coreAbil.isSneakAbility())) { if (player.isSneaking() &&
	 * WaterAbility.isWater(player.getLocation().getBlock())) {
	 * player.setVelocity(player.getEyeLocation().getDirection().clone().
	 * normalize().multiply(swimSpeed)); } } } } }
	 */
}
