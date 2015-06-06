package com.projectkorra.ProjectKorra;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	static ProjectKorra plugin;

	public ConfigManager(ProjectKorra plugin) {
		ConfigManager.plugin = plugin;
		configCheck();
	}

	public static void configCheck() {

		FileConfiguration config = ProjectKorra.plugin.getConfig();

		ArrayList<String> earthbendable = new ArrayList<String>();
		earthbendable.add("STONE");
		earthbendable.add("CLAY");
		earthbendable.add("COAL_ORE");
		earthbendable.add("DIAMOND_ORE");
		earthbendable.add("DIRT");
		earthbendable.add("GOLD_ORE");
		earthbendable.add("GRASS");
		earthbendable.add("GRAVEL");
		earthbendable.add("IRON_ORE");
		earthbendable.add("LAPIS_ORE");
		earthbendable.add("NETHERRACK");
		earthbendable.add("REDSTONE_ORE");
		earthbendable.add("SAND");
		earthbendable.add("SANDSTONE");
		earthbendable.add("MYCEL");
		
		ArrayList<String> metals = new ArrayList<String>();
		metals.add("IRON_BLOCK");
		metals.add("GOLD_BLOCK");
		metals.add("QUARTZ_BLOCK");

		config.addDefault("Properties.Chat.Enable", true);
		config.addDefault("Properties.Chat.Format", "<name>: <message>");
		config.addDefault("Properties.Chat.Prefixes.Air", "[Airbender]");
		config.addDefault("Properties.Chat.Prefixes.Water", "[Waterbender]");
		config.addDefault("Properties.Chat.Prefixes.Earth", "[Earthbender]");
		config.addDefault("Properties.Chat.Prefixes.Fire", "[Firebender]");
		config.addDefault("Properties.Chat.Prefixes.Chi", "[Chiblocker]");
		config.addDefault("Properties.Chat.Prefixes.Avatar", "[Avatar]");
		config.addDefault("Properties.Chat.Colors.Avatar", "DARK_PURPLE");
		config.addDefault("Properties.Chat.Colors.Air", "GRAY");
		config.addDefault("Properties.Chat.Colors.AirSub", "DARK_GRAY");
		config.addDefault("Properties.Chat.Colors.Water", "AQUA");
		config.addDefault("Properties.Chat.Colors.WaterSub", "DARK_AQUA");
		config.addDefault("Properties.Chat.Colors.Earth", "GREEN");
		config.addDefault("Properties.Chat.Colors.EarthSub", "DARK_GREEN");
		config.addDefault("Properties.Chat.Colors.Fire", "RED");
		config.addDefault("Properties.Chat.Colors.FireSub", "DARK_RED");
		config.addDefault("Properties.Chat.Colors.Chi", "GOLD");

		config.addDefault("Properties.ImportEnabled", true);
		config.addDefault("Properties.BendingAffectFallingSand.Normal", true);
		config.addDefault("Properties.BendingAffectFallingSand.NormalStrengthMultiplier", 1.0);
		config.addDefault("Properties.BendingAffectFallingSand.TNT", true);
		config.addDefault("Properties.BendingAffectFallingSand.TNTStrengthMultiplier", 1.0);
		config.addDefault("Properties.GlobalCooldown", 500);
		config.addDefault("Properties.SeaLevel", 62);

		config.addDefault("Properties.HorizontalCollisionPhysics.Enabled", true);
		config.addDefault("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance", 5.0);

		config.addDefault("Properties.CustomItems.GrapplingHook.Enable", true);
		config.addDefault("Properties.CustomItems.GrapplingHook.IronUses", 25);
		config.addDefault("Properties.CustomItems.GrapplingHook.GoldUses", 50);

		config.addDefault("Properties.RegionProtection.AllowHarmlessAbilities", true);
		config.addDefault("Properties.RegionProtection.RespectWorldGuard", true);
		config.addDefault("Properties.RegionProtection.RespectGriefPrevention", true);
		config.addDefault("Properties.RegionProtection.RespectFactions", true);
		config.addDefault("Properties.RegionProtection.RespectTowny", true);
		config.addDefault("Properties.RegionProtection.RespectPreciousStones", true);
		config.addDefault("Properties.RegionProtection.RespectLWC", true);
		config.addDefault("Properties.RegionProtection.CacheBlockTime", 5000);

		config.addDefault("Properties.TagAPI.Enabled", true);

		config.addDefault("Properties.Air.CanBendWithWeapons", false);
		config.addDefault("Properties.Air.Particles", "smoke");
		config.addDefault("Properties.Air.PlaySound", true);

		config.addDefault("Properties.Water.CanBendWithWeapons", true);
		config.addDefault("Properties.Water.NightFactor", 1.5);
		config.addDefault("Properties.Water.FullMoonFactor", 3.0);
		config.addDefault("Properties.Water.CanBendPackedIce", true);
		config.addDefault("Properties.Water.PlaySound", true);

		config.addDefault("Properties.Earth.RevertEarthbending", true);
		config.addDefault("Properties.Earth.SafeRevert", true);
		config.addDefault("Properties.Earth.RevertCheckTime", 300000);
		config.addDefault("Properties.Earth.CanBendWithWeapons", true);
		config.addDefault("Properties.Earth.EarthbendableBlocks", earthbendable);
		config.addDefault("Properties.Earth.MetalBlocks", metals);
		config.addDefault("Properties.Earth.MetalPowerFactor", 1.5);
		config.addDefault("Properties.Earth.PlaySound", true);

		config.addDefault("Properties.Fire.CanBendWithWeapons", true);
		config.addDefault("Properties.Fire.DayFactor", 1.5);
		config.addDefault("Properties.Fire.PlaySound", true);

		config.addDefault("Properties.Chi.CanBendWithWeapons", true);

		ArrayList<String> disabledWorlds = new ArrayList<String>();
		disabledWorlds.add("TestWorld");
		disabledWorlds.add("TestWorld2");
		config.addDefault("Properties.DisabledWorlds", disabledWorlds);

		config.addDefault("Abilities.AvatarState.Enabled", true);
		config.addDefault("Abilities.AvatarState.Description", "The signature ability of the Avatar, this is a toggle. Click to activate to become "
				+ "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from "
				+ "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities "
				+ "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able "
				+ "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar "
				+ "State selected to deactivate it.");
		config.addDefault("Abilities.AvatarState.Cooldown", 7200000);
		config.addDefault("Abilities.AvatarState.Duration", 480000);
		config.addDefault("Abilities.AvatarState.PowerMultiplier", 5);
		config.addDefault("Abilities.AvatarState.PotionEffects.Regeneration.Enabled", true);
		config.addDefault("Abilities.AvatarState.PotionEffects.Regeneration.Power", 3);
		config.addDefault("Abilities.AvatarState.PotionEffects.Speed.Enabled", true);
		config.addDefault("Abilities.AvatarState.PotionEffects.Speed.Power", 3);
		config.addDefault("Abilities.AvatarState.PotionEffects.DamageResistance.Enabled", true);
		config.addDefault("Abilities.AvatarState.PotionEffects.FireResistance.Enabled", true);
		config.addDefault("Abilities.AvatarState.PotionEffects.FireResistance.Power", 3);

		config.addDefault("Abilities.Air.Passive.Factor", 0.3);
		config.addDefault("Abilities.Air.Passive.Speed", 2);
		config.addDefault("Abilities.Air.Passive.Jump", 3);

		config.addDefault("Abilities.Air.AirBlast.Enabled", true);
		config.addDefault("Abilities.Air.AirBlast.Description", "AirBlast is the most fundamental bending technique of an airbender."
				+ " To use, simply left-click in a direction. A gust of wind will be"
				+ " created at your fingertips, launching anything in its path harmlessly back."
				+ " A gust of air can extinguish fires on the ground or on a player, can cool lava, and "
				+ "can flip levers and activate buttons. Additionally, tapping sneak will change the "
				+ "origin of your next AirBlast to your targeted location.");
		config.addDefault("Abilities.Air.AirBlast.Speed", 25);
		config.addDefault("Abilities.Air.AirBlast.Range", 20);
		config.addDefault("Abilities.Air.AirBlast.Radius", 2);
		config.addDefault("Abilities.Air.AirBlast.Push", 3.5);

		config.addDefault("Abilities.Air.AirBubble.Enabled", true);
		config.addDefault("Abilities.Air.AirBubble.Description", "To use, the bender must merely have the ability selected. All water around the user in a small bubble will vanish, replacing itself once the user either gets too far away or selects a different ability.");
		config.addDefault("Abilities.Air.AirBubble.Radius", 7);

		config.addDefault("Abilities.Air.AirBurst.Enabled", true);
		config.addDefault("Abilities.Air.AirBurst.Description", "AirBurst is one of the most powerful abilities in the airbender's arsenal. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of air in front of you, or click to release the burst in a sphere around you. "
				+ "Additionally, having this ability selected when you land on the ground from a "
				+ "large enough fall will create a burst of air around you.");
		config.addDefault("Abilities.Air.AirBurst.FallThreshold", 10);
		config.addDefault("Abilities.Air.AirBurst.PushFactor", 1.5);
		config.addDefault("Abilities.Air.AirBurst.ChargeTime", 1750);
		config.addDefault("Abilities.Air.AirBurst.Damage", 0);

		config.addDefault("Abilities.Air.AirScooter.Enabled", true);
		config.addDefault("Abilities.Air.AirScooter.Description", "AirScooter is a fast means of transportation. To use, sprint, jump then click with "
				+ "this ability selected. You will hop on a scooter of air and be propelled forward "
				+ "in the direction you're looking (you don't need to press anything). "
				+ "This ability can be used to levitate above liquids, but it cannot go up steep slopes. "
				+ "Any other actions will deactivate this ability.");
		config.addDefault("Abilities.Air.AirScooter.Speed", .675);

		config.addDefault("Abilities.Air.Tornado.Enabled", true);
		config.addDefault("Abilities.Air.Tornado.Description", "To use, simply sneak (default: shift). "
				+ "This will create a swirling vortex at the targeted location. "
				+ "Any creature or object caught in the vortex will be launched up "
				+ "and out in some random direction. If another player gets caught "
				+ "in the vortex, the launching effect is minimal. Tornado can "
				+ "also be used to transport the user. If the user gets caught in his/her "
				+ "own tornado, his movements are much more manageable. Provided the user doesn't "
				+ "fall out of the vortex, it will take him to a maximum height and move him in "
				+ "the general direction he's looking. Skilled airbenders can scale anything "
				+ "with this ability.");

		config.addDefault("Abilities.Air.AirShield.Enabled", true);
		config.addDefault("Abilities.Air.AirShield.Description", "Air Shield is one of the most powerful defensive techniques in existence. "
				+ "To use, simply sneak (default: shift). "
				+ "This will create a whirlwind of air around the user, "
				+ "with a small pocket of safe space in the center. "
				+ "This wind will deflect all projectiles and will prevent any creature from "
				+ "entering it for as long as its maintained.");
		config.addDefault("Abilities.Air.AirShield.Radius", 7);
		config.addDefault("Abilities.Air.AirShield.IsAvatarStateToggle", true);

		config.addDefault("Abilities.Air.AirSpout.Enabled", true);
		config.addDefault("Abilities.Air.AirSpout.Description", "This ability gives the airbender limited sustained levitation. It is a "
				+ "toggle - click to activate and form a whirling spout of air "
				+ "beneath you, lifting you up. You can bend other abilities while using AirSpout. "
				+ "Click again to deactivate this ability.");
		config.addDefault("Abilities.Air.AirSpout.Height", 20);

		config.addDefault("Abilities.Air.AirSuction.Enabled", true);
		config.addDefault("Abilities.Air.AirSuction.Description", "To use, simply left-click in a direction. A gust of wind will originate as far as it can in that direction and flow towards you, sucking anything in its path harmlessly with it. Skilled benders can use this technique to pull items from precarious locations. Additionally, tapping sneak will change the origin of your next AirSuction to your targeted location.");
		config.addDefault("Abilities.Air.AirSuction.Speed", 25);
		config.addDefault("Abilities.Air.AirSuction.Range", 20);
		config.addDefault("Abilities.Air.AirSuction.Radius", 2);
		config.addDefault("Abilities.Air.AirSuction.Push", 3.5);

		config.addDefault("Abilities.Air.AirSwipe.Enabled", true);
		config.addDefault("Abilities.Air.AirSwipe.Description", "To use, simply left-click in a direction. An arc of air will flow from you towards that direction, cutting and pushing back anything in its path. Its damage is minimal, but it still sends the message. This ability will extinguish fires, cool lava, and cut things like grass, mushrooms, and flowers. Additionally, you can charge it by holding sneak. Charging before attacking will increase damage and knockback, up to a maximum.");
		config.addDefault("Abilities.Air.AirSwipe.Damage", 2);
		config.addDefault("Abilities.Air.AirSwipe.Range", 16);
		config.addDefault("Abilities.Air.AirSwipe.Radius", 2);
		config.addDefault("Abilities.Air.AirSwipe.Push", 1);
		config.addDefault("Abilities.Air.AirSwipe.Arc", 20);
		config.addDefault("Abilities.Air.AirSwipe.Speed", 25);
		config.addDefault("Abilities.Air.AirSwipe.Cooldown", 1500);
		config.addDefault("Abilities.Air.AirSwipe.ChargeFactor", 3);
		config.addDefault("Abilities.Air.AirSwipe.MaxChargeTime", 3000);
		
		config.addDefault("Abilities.Air.Flight.Enabled", true);
		config.addDefault("Abilities.Air.Flight.Description", "Jump in the air, crouch (default: shift) and hold with this ability bound and you will glide around in the direction you look. While flying, click to Hover. Click again to disable Hovering.");
		config.addDefault("Abilities.Air.Flight.HoverEnabled", true);
		
		config.addDefault("Abilities.Air.Suffocate.Enabled", true);
		config.addDefault("Abilities.Air.Suffocate.Description", "This ability is one of the most dangerous abilities an Airbender possesses. To use, simply look at an entity and hold shift. The entity will begin taking damage as you extract the air from their lungs. Any bender caught in this sphere will only be able to use basic moves, such as AirSwipe, WaterManipulation, FireBlast, or EarthBlast. An entity can be knocked out of the sphere by certain bending arts, and your attention will be disrupted if you are hit by bending.");
		config.addDefault("Abilities.Air.Suffocate.ChargeTime", 1000);
		config.addDefault("Abilities.Air.Suffocate.Cooldown", 0);
		config.addDefault("Abilities.Air.Suffocate.Range", 15);
		config.addDefault("Abilities.Air.Suffocate.Damage", 2);
		config.addDefault("Abilities.Air.Suffocate.DamageInitialDelay", 2);
		config.addDefault("Abilities.Air.Suffocate.DamageInterval", 1);
		config.addDefault("Abilities.Air.Suffocate.SlowPotency", 1);
		config.addDefault("Abilities.Air.Suffocate.SlowDelay", 0.5);
		config.addDefault("Abilities.Air.Suffocate.SlowInterval", 1.25);
		config.addDefault("Abilities.Air.Suffocate.BlindPotentcy", 30);
		config.addDefault("Abilities.Air.Suffocate.BlindDelay", 2);
		config.addDefault("Abilities.Air.Suffocate.BlindInterval", 1.5);
		config.addDefault("Abilities.Air.Suffocate.CanBeUsedOnUndeadMobs", true);
		config.addDefault("Abilities.Air.Suffocate.RequireConstantAim", true);
		config.addDefault("Abilities.Air.Suffocate.RequireConstantAimRadius", 5);
		config.addDefault("Abilities.Air.Suffocate.AnimationRadius", 2.0);
		config.addDefault("Abilities.Air.Suffocate.AnimationParticleAmount", 2);
		config.addDefault("Abilities.Air.Suffocate.AnimationSpeed", 1.0);

		config.addDefault("Abilities.Air.Tornado.Radius", 10);
		config.addDefault("Abilities.Air.Tornado.Height", 25);
		config.addDefault("Abilities.Air.Tornado.Range", 25);
		config.addDefault("Abilities.Air.Tornado.MobPushFactor", 1);
		config.addDefault("Abilities.Air.Tornado.PlayerPushFactor", 1);
		
		config.addDefault("Abilities.Air.AirCombo.Enabled", true);
		config.addDefault("Abilities.Air.AirCombo.Twister.Speed", 0.35);
		config.addDefault("Abilities.Air.AirCombo.Twister.Range", 16);
		config.addDefault("Abilities.Air.AirCombo.Twister.Height", 8);
		config.addDefault("Abilities.Air.AirCombo.Twister.Radius", 3.5);
		config.addDefault("Abilities.Air.AirCombo.Twister.RemoveDelay", 1500);
		config.addDefault("Abilities.Air.AirCombo.Twister.Cooldown", 10000);
		config.addDefault("Abilities.Air.AirCombo.Twister.DegreesPerParticle", 7);
		config.addDefault("Abilities.Air.AirCombo.Twister.HeightPerParticle", 1.25);
		config.addDefault("Abilities.Air.AirCombo.AirStream.Speed", 0.5);
		config.addDefault("Abilities.Air.AirCombo.AirStream.Range", 40);
		config.addDefault("Abilities.Air.AirCombo.AirStream.EntityDuration", 4000);
		config.addDefault("Abilities.Air.AirCombo.AirStream.EntityHeight", 14);
		config.addDefault("Abilities.Air.AirCombo.AirStream.Cooldown", 6000);
		config.addDefault("Abilities.Air.AirCombo.AirSweep.Speed", 1.4);
		config.addDefault("Abilities.Air.AirCombo.AirSweep.Range", 14);
		config.addDefault("Abilities.Air.AirCombo.AirSweep.Damage", 4);
		config.addDefault("Abilities.Air.AirCombo.AirSweep.Knockback", 3.5);
		config.addDefault("Abilities.Air.AirCombo.AirSweep.Cooldown", 5000);
		
		config.addDefault("Abilities.Water.Passive.SwimSpeedFactor", 0.7);

		config.addDefault("Abilities.Water.Bloodbending.Enabled", true);
		config.addDefault("Abilities.Water.Bloodbending.Description", "This ability was made illegal for a reason. With this ability selected, sneak while "
				+ "targetting something and you will bloodbend that target. Bloodbent targets cannot move, "
				+ "bend or attack. You are free to control their actions by looking elsewhere - they will "
				+ "be forced to move in that direction. Additionally, clicking while bloodbending will "
				+ "launch that target off in the direction you're looking. "
				+ "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.");
		config.addDefault("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight", false);
		config.addDefault("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs", true);
		config.addDefault("Abilities.Water.Bloodbending.ThrowFactor", 2);
		config.addDefault("Abilities.Water.Bloodbending.Range", 10);
		config.addDefault("Abilities.Water.Bloodbending.HoldTime", 0);
		config.addDefault("Abilities.Water.Bloodbending.Cooldown", 0);
		config.addDefault("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon", false);

		config.addDefault("Abilities.Water.HealingWaters.Enabled", true);
		config.addDefault("Abilities.Water.HealingWaters.Description", "To use, the bender must be at least partially submerged in water. "
				+ "If the user is not sneaking, this ability will automatically begin "
				+ "working provided the user has it selected. If the user is sneaking, "
				+ "he/she is channeling the healing to their target in front of them. "
				+ "In order for this channel to be successful, the user and the target must "
				+ "be at least partially submerged in water.");
		config.addDefault("Abilities.Water.HealingWaters.Radius", 5);
		config.addDefault("Abilities.Water.HealingWaters.Interval", 750);
		config.addDefault("Abilities.Water.HealingWaters.Power", 1);

		config.addDefault("Abilities.Water.IceBlast.Enabled", true);
		config.addDefault("Abilities.Water.IceBlast.Damage", 3);
		config.addDefault("Abilities.Water.IceBlast.Range", 20);
		config.addDefault("Abilities.Water.IceBlast.Description", "This ability offers a powerful ice utility for Waterbenders. It can be used to fire an explosive burst of ice at an opponent, spraying ice and snow around it. To use, simply tap sneak (Default: Shift) while targeting a block of ice to select it as a source. From there, you can just left click to send the blast off at your opponent.");

		config.addDefault("Abilities.Water.IceSpike.Enabled", true);
		config.addDefault("Abilities.Water.IceSpike.Description", "This ability has many functions. Clicking while targetting ice, or an entity over some ice, "
				+ "will raise a spike of ice up, damaging and slowing the target. Tapping sneak (shift) while"
				+ " selecting a water source will select that source that can then be fired with a click. Firing"
				+ " this will launch a spike of ice at your target, dealing a bit of damage and slowing the target. "
				+ "If you sneak (shift) while not selecting a source, many ice spikes will erupt from around you, "
				+ "damaging and slowing those targets.");
		config.addDefault("Abilities.Water.IceSpike.Cooldown", 2000);
		config.addDefault("Abilities.Water.IceSpike.Damage", 2);
		config.addDefault("Abilities.Water.IceSpike.Range", 20);
		config.addDefault("Abilities.Water.IceSpike.ThrowingMult", 0.7);
		config.addDefault("Abilities.Water.IceSpike.Height", 6);
		config.addDefault("Abilities.Water.IceSpike.Projectile.Range", 20);
		config.addDefault("Abilities.Water.IceSpike.Projectile.Damage", 1);

		config.addDefault("Abilities.Water.OctopusForm.Enabled", true);
		config.addDefault("Abilities.Water.OctopusForm.Description", "This ability allows the waterbender to manipulate a large quantity of water into a form resembling that of an octopus. "
				+ "To use, click to select a water source. Then, hold sneak to channel this ability. "
				+ "While channeling, the water will form itself around you and has a chance to block incoming attacks. "
				+ "Additionally, you can click while channeling to attack things near you, dealing damage and knocking them back. "
				+ "Releasing shift at any time will dissipate the form.");
		config.addDefault("Abilities.Water.OctopusForm.Range", 10);
		config.addDefault("Abilities.Water.OctopusForm.AttackRange", 2.5);
		config.addDefault("Abilities.Water.OctopusForm.Radius", 3);
		config.addDefault("Abilities.Water.OctopusForm.Damage", 3);
		config.addDefault("Abilities.Water.OctopusForm.Knockback", 1.75);
		config.addDefault("Abilities.Water.OctopusForm.FormDelay", 50);

		config.addDefault("Abilities.Water.PhaseChange.Enabled", true);
		config.addDefault("Abilities.Water.PhaseChange.Description", "To use, simply left-click. "
				+ "Any water you are looking at within range will instantly freeze over into solid ice. "
				+ "Provided you stay within range of the ice and do not unbind FreezeMelt, "
				+ "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. "
				+ "If you sneak (default: shift), anything around where you are looking at will instantly melt. "
				+ "Since this is a more favorable state for these things, they will never re-freeze unless they "
				+ "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while "
				+ "targetting water with FreezeMelt, it will evaporate water around that block that is above "
				+ "sea level. ");
		config.addDefault("Abilities.Water.PhaseChange.Range", 20);
		config.addDefault("Abilities.Water.PhaseChange.Radius", 5);

		config.addDefault("Abilities.Water.Surge.Enabled", true);
		config.addDefault("Abilities.Water.Surge.Description", "This ability has two distinct features. If you sneak to select a source block, you can then click in a direction and a large wave will be launched in that direction. If you sneak again while the wave is en route, the wave will freeze the next target it hits. If, instead, you click to select a source block, you can hold sneak to form a wall of water at your cursor location. Click to shift between a water wall and an ice wall. Release sneak to dissipate it.");
		config.addDefault("Abilities.Water.Surge.Wave.Radius", 3);
		config.addDefault("Abilities.Water.Surge.Wave.Range", 20);
		config.addDefault("Abilities.Water.Surge.Wave.HorizontalPush", 1);
		config.addDefault("Abilities.Water.Surge.VerticalPush", 0.2);
		config.addDefault("Abilities.Water.Surge.Wall.Range", 5);
		config.addDefault("Abilities.Water.Surge.Wall.Radius", 2);

		config.addDefault("Abilities.Water.Torrent.Enabled", true);
		config.addDefault("Abilities.Water.Torrent.Description", "Torrent is one of the strongest moves in a waterbender's arsenal. To use, first click a source block to select it; then hold shift to begin streaming the water around you. Water flowing around you this way will damage and knock back nearby enemies and projectiles. If you release shift during this, you will create a large wave that expands outwards from you, launching anything in its path back. Instead, if you click you release the water and channel it to flow towards your cursor. Anything caught in the blast will be tossed about violently and take damage. Finally, if you click again when the water is torrenting, it will freeze the area around it when it is obstructed.");
		config.addDefault("Abilities.Water.Torrent.Range", 25);
		config.addDefault("Abilities.Water.Torrent.DeflectDamage", 1);
		config.addDefault("Abilities.Water.Torrent.Damage", 2);
		config.addDefault("Abilities.Water.Torrent.Wave.Radius", 15);
		config.addDefault("Abilities.Water.Torrent.Wave.Knockback", 1.5);
		config.addDefault("Abilities.Water.Torrent.Wave.Height", 1);

		config.addDefault("Abilities.Water.Plantbending.RegrowTime", 180000);
		
		config.addDefault("Abilities.Water.WaterArms.Enabled", true);
		config.addDefault("Abilities.Water.WaterArms.Description", "One of the most diverse moves in a Waterbender's arsenal, this move creates tendrils "
				+ "of water from the players arms to emulate their actual arms. Each water arms mode will be binded to a slot, switch slots to change mode. "
				+ "To deactive the arms, hold Sneak and Double Left-Click."
				+ "\nPull - Use your Arms to pull blocks, items, mobs or even players towards you!"
				+ "\nPunch - An offensive attack, harming players or mobs!"
				+ "\nGrapple - Scale walls and speed across battlefields, using your Arms as a grappling hook!"
				+ "\nGrab - Grab an entity with your arm, and swing them about!"
				+ "\nFreeze - Use your Arms to fire small blasts of ice in any direction!"
				+ "\nSpear - Throw your Arms in any direction, freezing whatever it hits!");
		config.addDefault("Abilities.Water.WaterArms.SneakMessage", "Active Ability:");
		
		config.addDefault("Abilities.Water.WaterArms.Arms.InitialLength", 4);
		config.addDefault("Abilities.Water.WaterArms.Arms.SourceGrabRange", 4);
		config.addDefault("Abilities.Water.WaterArms.Arms.MaxAttacks", 10);
		config.addDefault("Abilities.Water.WaterArms.Arms.MaxAlternateUsage", 50);
		config.addDefault("Abilities.Water.WaterArms.Arms.MaxIceShots", 5);
		config.addDefault("Abilities.Water.WaterArms.Arms.Cooldown", 20000);
		config.addDefault("Abilities.Water.WaterArms.Arms.AllowPlantSource", true);
		
		config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.Enabled", true);
		config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.Damage", Double.valueOf(10.0));
		config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.KillUser", false);
		
		config.addDefault("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled", false);
		config.addDefault("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown", 200);
		
		config.addDefault("Abilities.Water.WaterArms.Whip.MaxLength", 20);
		config.addDefault("Abilities.Water.WaterArms.Whip.MaxLengthWeak", 12);
		
		config.addDefault("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.Normal", 24);
		config.addDefault("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.FullMoon", 30);
		
		config.addDefault("Abilities.Water.WaterArms.Whip.Pull.Multiplier", Double.valueOf(0.15));
		
		config.addDefault("Abilities.Water.WaterArms.Whip.Punch.PunchDamage", Double.valueOf(3.0));
		config.addDefault("Abilities.Water.WaterArms.Whip.Punch.MaxLength", 10);
		config.addDefault("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.Normal", 11);
		config.addDefault("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.FullMoon", 13);
		
		config.addDefault("Abilities.Water.WaterArms.Whip.Grapple.RespectRegions", false);
		
		config.addDefault("Abilities.Water.WaterArms.Whip.Grab.HoldTime", 10000);
		
		config.addDefault("Abilities.Water.WaterArms.Freeze.Range", 20);
		config.addDefault("Abilities.Water.WaterArms.Freeze.Damage", Double.valueOf(2.0));
		
		config.addDefault("Abilities.Water.WaterArms.Spear.Range", 40);
		config.addDefault("Abilities.Water.WaterArms.Spear.Damage", Double.valueOf(4.0));
		config.addDefault("Abilities.Water.WaterArms.Spear.DamageEnabled", true);
		config.addDefault("Abilities.Water.WaterArms.Spear.Sphere", 2);
		config.addDefault("Abilities.Water.WaterArms.Spear.Duration", 6000);
		config.addDefault("Abilities.Water.WaterArms.Spear.Length", 18);
		
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Range.Normal", 45);
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Range.FullMoon", 60);
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.Normal", 3);
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.FullMoon", 6);
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Duration.Normal", 7000);
		config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Duration.FullMoon", 12000);

		config.addDefault("Abilities.Water.WaterBubble.Enabled", true);
		config.addDefault("Abilities.Water.WaterBubble.Description","To use, the bender must merely have the ability selected. All water around the user in a small bubble will vanish, replacing itself once the user either gets too far away or selects a different ability.");
		config.addDefault("Abilities.Water.WaterBubble.Radius", 7);

		config.addDefault("Abilities.Water.WaterManipulation.Enabled", true);
		config.addDefault("Abilities.Water.WaterManipulation.Description", "To use, place your cursor over a waterbendable object and tap sneak (default: shift). Smoke will appear where you've selected, indicating the origin of your ability. After you have selected an origin, simply left-click in any direction and you will see your water spout off in that direction, slicing any creature in its path. If you look towards a creature when you use this ability, it will target that creature. A collision from Water Manipulation both knocks the target back and deals some damage. Alternatively, if you have the source selected and tap shift again, you will be able to control the water more directly.");
		config.addDefault("Abilities.Water.WaterManipulation.Damage", 3.0);
		config.addDefault("Abilities.Water.WaterManipulation.Range", 20);
		config.addDefault("Abilities.Water.WaterManipulation.Speed", 35);
		config.addDefault("Abilities.Water.WaterManipulation.Push", 0.3);
		config.addDefault("Abilities.Water.WaterManipulation.Cooldown", 1000);

		config.addDefault("Abilities.Water.WaterSpout.Enabled", true);
		config.addDefault("Abilities.Water.WaterSpout.Description", "This ability provides a Waterbender with a means of transportation. To use, simply left click while in or over water to spout water up beneath you, experiencing controlled levitation. Left clicking again while the spout is active will cause it to disappear. Alternatively, tapping a Waterbendable block while not in Water will select a water block as a source, from there, you can tap sneak (Default:Shift) to channel the Water around you. Releasing the sneak will create a wave allowing you a quick burst of controlled transportation. While riding the wave you may press sneak to cause the wave to disappear.");
		config.addDefault("Abilities.Water.WaterSpout.Height", 20);
		config.addDefault("Abilities.Water.WaterSpout.BlockSpiral", true);
		config.addDefault("Abilities.Water.WaterSpout.Particles", false);
		config.addDefault("Abilities.Water.WaterSpout.Wave.Particles", false);
		config.addDefault("Abilities.Water.WaterSpout.Wave.Enabled", true);
		config.addDefault("Abilities.Water.WaterSpout.Wave.Range", 6);
		config.addDefault("Abilities.Water.WaterSpout.Wave.ChargeTime", 1000);
		config.addDefault("Abilities.Water.WaterSpout.Wave.FlightTime", 2000);
		config.addDefault("Abilities.Water.WaterSpout.Wave.Speed", 1.2);
		
		config.addDefault("Abilities.Water.WaterCombo.Enabled", true);
		config.addDefault("Abilities.Water.WaterCombo.IceWave.Damage", 3);
		config.addDefault("Abilities.Water.WaterCombo.IceWave.Cooldown", 6000);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.Damage", 1.5);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.Radius", 2.5);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.Range", 12);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.MaxShots", 30);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.AnimationSpeed", 1);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.ShootTime", 5000);
		config.addDefault("Abilities.Water.WaterCombo.IceBullet.Cooldown", 10000);

		config.addDefault("Abilities.Earth.Passive.Duration", 2500);

		config.addDefault("Abilities.Earth.Catapult.Enabled", true);
		config.addDefault("Abilities.Earth.Catapult.Description", "To use, left-click while looking in the direction you want to be launched. "
				+ "A pillar of earth will jut up from under you and launch you in that direction - "
				+ "if and only if there is enough earth behind where you're looking to launch you. "
				+ "Skillful use of this ability takes much time and work, and it does result in the "
				+ "death of certain gung-ho earthbenders. If you plan to use this ability, be sure "
				+ "you've read about your passive ability you innately have as an earthbender.");
		config.addDefault("Abilities.Earth.Catapult.Length", 7);
		config.addDefault("Abilities.Earth.Catapult.Speed", 12);
		config.addDefault("Abilities.Earth.Catapult.Push", 5);

		config.addDefault("Abilities.Earth.Collapse.Enabled", true);
		config.addDefault("Abilities.Earth.Collapse.Description", " To use, simply left-click on an earthbendable block. "
				+ "That block and the earthbendable blocks above it will be shoved "
				+ "back into the earth below them, if they can. "
				+ "This ability does have the capacity to trap something inside of it, "
				+ "although it is incredibly difficult to do so. "
				+ "Additionally, press sneak with this ability to affect an area around your targetted location - "
				+ "all earth that can be moved downwards will be moved downwards. "
				+ "This ability is especially risky or deadly in caves, depending on the "
				+ "earthbender's goal and technique.");
		config.addDefault("Abilities.Earth.Collapse.Range", 20);
		config.addDefault("Abilities.Earth.Collapse.Radius", 7);
		config.addDefault("Abilities.Earth.Collapse.Speed", 8);

		config.addDefault("Abilities.Earth.EarthArmor.Enabled", true);
		config.addDefault("Abilities.Earth.EarthArmor.Description", "This ability encases the earthbender in temporary armor. To use, click on a block that is earthbendable. If there is another block under it that is earthbendable, the block will fly to you and grant you temporary armor and damage reduction. This ability has a long cooldown.");
		config.addDefault("Abilities.Earth.EarthArmor.Duration", 10000);
		config.addDefault("Abilities.Earth.EarthArmor.Strength", 2);
		config.addDefault("Abilities.Earth.EarthArmor.Duration", 17500);

		config.addDefault("Abilities.Earth.EarthBlast.Enabled", true);
		config.addDefault("Abilities.Earth.EarthBlast.Description", "To use, place your cursor over an earthbendable object (dirt, rock, ores, etc) "
				+ "and tap sneak (default: shift). The object will temporarily turn to stone, "
				+ "indicating that you have it focused as the source for your ability. "
				+ "After you have selected an origin (you no longer need to be sneaking), "
				+ "simply left-click in any direction and you will see your object launch "
				+ "off in that direction, smashing into any creature in its path. If you look "
				+ "towards a creature when you use this ability, it will target that creature. "
				+ "A collision from Earth Blast both knocks the target back and deals some damage. "
				+ "You cannot have multiple of these abilities flying at the same time.");
		config.addDefault("Abilities.Earth.EarthBlast.CanHitSelf", false);
		config.addDefault("Abilities.Earth.EarthBlast.PrepareRange", 7);
		config.addDefault("Abilities.Earth.EarthBlast.Range", 20);
		config.addDefault("Abilities.Earth.EarthBlast.Speed", 35);
		config.addDefault("Abilities.Earth.EarthBlast.Revert", true);
		config.addDefault("Abilities.Earth.Earthblast.Damage", 4);
		config.addDefault("Abilities.Earth.EarthBlast.Push", 0.3);

		config.addDefault("Abilities.Earth.EarthGrab.Enabled", true);
		config.addDefault("Abilities.Earth.EarthGrab.Description", "To use, simply left-click while targeting a creature within range. "
				+ "This ability will erect a circle of earth to trap the creature in.");
		config.addDefault("Abilities.Earth.EarthGrab.Range", 15);

		config.addDefault("Abilities.Earth.EarthTunnel.Enabled", true);
		config.addDefault("Abilities.Earth.EarthTunnel.Description", "Earth Tunnel is a completely utility ability for earthbenders. To use, simply sneak (default: shift) in the direction you want to tunnel. You will slowly begin tunneling in the direction you're facing for as long as you sneak or if the tunnel has been dug long enough. This ability will be interrupted if it hits a block that cannot be earthbent.");
		config.addDefault("Abilities.Earth.EarthTunnel.MaxRadius", 1);
		config.addDefault("Abilities.Earth.EarthTunnel.Range", 10);
		config.addDefault("Abilities.Earth.EarthTunnel.Radius", 0.25);
		config.addDefault("Abilities.Earth.EarthTunnel.Revert", true);
		config.addDefault("Abilities.Earth.EarthTunnel.Interval", 30);

		config.addDefault("Abilities.Earth.Extraction.Enabled", true);
		config.addDefault("Abilities.Earth.Extraction.Description", "This ability allows metalbenders to extract the minerals from ore blocks. To use, simply tap sneak while looking at an ore block with metal in it (iron, gold, quartz) and the ore will be extracted and drop in front of you. This ability has a small chance of doubling or tripling the loot. This ability has a short cooldown.");
		config.addDefault("Abilities.Earth.Extraction.Cooldown", 10000);
		config.addDefault("Abilities.Earth.Extraction.TripleLootChance", 15);
		config.addDefault("Abilities.Earth.Extraction.DoubleLootChance", 40);

		config.addDefault("Abilities.Earth.LavaFlow.Enabled", true);
		config.addDefault("Abilities.Earth.LavaFlow.Description", "This ability allows an Earthbender to create lava using the Earth around them. To use, simply hold sneak (Default: Shift) to create a lava moat that surrounds you, press sneak again to remove the moat. Left click an Earthbendable block to create a pool of lava after a small delay. Additionally, you can left click at any time to turn lava back into its original state -- Earth.");
		config.addDefault("Abilities.Earth.LavaFlow.ShiftCooldown", 16000);
		config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCooldown", 10000);
		config.addDefault("Abilities.Earth.LavaFlow.ClickLandCooldown", 500);
		config.addDefault("Abilities.Earth.LavaFlow.ShiftCleanupDelay", 10000);
		config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCleanupDelay", 7000);
		config.addDefault("Abilities.Earth.LavaFlow.ClickLandCleanupDelay", 20000);
		config.addDefault("Abilities.Earth.LavaFlow.ClickRange", 10.0);
		config.addDefault("Abilities.Earth.LavaFlow.ShiftRadius", 8.0);
		config.addDefault("Abilities.Earth.LavaFlow.ShiftPlatformRadius", 1.5);
		config.addDefault("Abilities.Earth.LavaFlow.ClickRadius", 5.0);
        config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCreateSpeed", 0.05);
        config.addDefault("Abilities.Earth.LavaFlow.ClickLandCreateSpeed", 0.10);
        config.addDefault("Abilities.Earth.LavaFlow.ShiftFlowSpeed", 0.1);
        config.addDefault("Abilities.Earth.LavaFlow.ShiftRemoveSpeed", 3.0);
        config.addDefault("Abilities.Earth.LavaFlow.ClickLavaStartDelay", 1500);
        config.addDefault("Abilities.Earth.LavaFlow.ClickLandStartDelay", 0);
        config.addDefault("Abilities.Earth.LavaFlow.UpwardFlow", 2);
        config.addDefault("Abilities.Earth.LavaFlow.DownwardFlow", 4);
        config.addDefault("Abilities.Earth.LavaFlow.AllowNaturalFlow", false);
        config.addDefault("Abilities.Earth.LavaFlow.ParticleDensity", 0.33);
        
		config.addDefault("Abilities.Earth.EarthSmash.Enabled", true);
		config.addDefault("Abilities.Earth.EarthSmash.Description", "To raise an EarthSmash hold sneak (default: shift) for approximately 1.5 seconds, " +
				"then release while aiming at dirt. To grab the EarthSmash aim at the center and hold sneak, " +
				"the EarthSmash will follow your mouse. You can shoot the EarthSmash by grabbing onto it and left clicking. " +
				"To ride the EarthSmash simply hop ontop of it and hold sneak while aiming in the direction that you wish to go. " +
				"Another way to ride an EarthSmash is to grab it with sneak and then right click it. " + 
				"Use EarthSmash as a defensive shield, a powerful attack, or an advanced means of transportation.");
		config.addDefault("Abilities.Earth.EarthSmash.AllowGrab", true);
		config.addDefault("Abilities.Earth.EarthSmash.AllowShooting", true);
		config.addDefault("Abilities.Earth.EarthSmash.AllowFlight", true);
		config.addDefault("Abilities.Earth.EarthSmash.GrabRange", 10);
		config.addDefault("Abilities.Earth.EarthSmash.ChargeTime", 1200);
		config.addDefault("Abilities.Earth.EarthSmash.Cooldown", 0);
		config.addDefault("Abilities.Earth.EarthSmash.ShotRange", 30);
		config.addDefault("Abilities.Earth.EarthSmash.Damage", 6);
		config.addDefault("Abilities.Earth.EarthSmash.Knockback", 3.5);
		config.addDefault("Abilities.Earth.EarthSmash.Knockup", 0.15);
		config.addDefault("Abilities.Earth.EarthSmash.FlightSpeed", 0.72);
		config.addDefault("Abilities.Earth.EarthSmash.FlightTimer", 3000);
		config.addDefault("Abilities.Earth.EarthSmash.RemoveTimer", 30000);
		
//		config.addDefault("Abilities.Earth.LavaSurge.Enabled", true);
//	    config.addDefault("Abilities.Earth.LavaSurge.Description", "LavaSurge is a fundamental move for any Lavabender out there. To use, simply sneak (Default: Shift) while looking at a source of Earth or Lava, then click in a direction. A surge of lava will swiftly travel towards the target you were pointing at, dealing moderate damage, a large knockback, and setting them on fire.");
//	    config.addDefault("Abilities.Earth.LavaSurge.Damage", 4);
//	    config.addDefault("Abilities.Earth.LavaSurge.Cooldown", 1000);
//		config.addDefault("Abilities.Earth.LavaSurge.FractureRadius", 1);
//		config.addDefault("Abilities.Earth.LavaSurge.PrepareRange", 7);
//		config.addDefault("Abilities.Earth.LavaSurge.TravelRange", 15);
//		config.addDefault("Abilities.Earth.LavaSurge.MaxLavaWaves", 10);
//		config.addDefault("Abilities.Earth.LavaSurge.SourceCanBeEarth", true);

		config.addDefault("Abilities.Earth.MetalClips.Enabled", true);
		config.addDefault("Abilities.Earth.MetalClips.Description", "MetalClips has the potential to be both an offensive and a utility ability. To start, you must carry smelted Iron Ingots in your inventory. To apply the clips onto an entity, simply click at them. If the entity is a Zombie, a Skeleton, or a Player, the clips will form armor around the entity, giving you some control over them. Each additional clip will give you more control. If you have permission to do so, you may crush the entity against a wall with a 4th clip, hurting them. Without explicit permissions, you will only be able to strap three clips on your target. If the entity is not one of the above, the clip will simply do damage and fall to the ground, to be collected.");
		config.addDefault("Abilities.Earth.MetalClips.Damage", 2);
		config.addDefault("Abilities.Earth.MetalClips.DamageInterval", 500);
		config.addDefault("Abilities.Earth.MetalClips.MagnetRange", 20);
		config.addDefault("Abilities.Earth.MetalClips.MagnetPower", 0.6);
		config.addDefault("Abilities.Earth.MetalClips.Cooldown", 1000);
		config.addDefault("Abilities.Earth.MetalClips.Duration", 10000);

		
		config.addDefault("Abilities.Earth.RaiseEarth.Enabled", true);
		config.addDefault("Abilities.Earth.RaiseEarth.Description", "To use, simply left-click on an earthbendable block. "
				+ "A column of earth will shoot upwards from that location. "
				+ "Anything in the way of the column will be brought up with it, "
				+ "leaving talented benders the ability to trap brainless entities up there. "
				+ "Additionally, simply sneak (default shift) looking at an earthbendable block. "
				+ "A wall of earth will shoot upwards from that location. "
				+ "Anything in the way of the wall will be brought up with it. ");
		config.addDefault("Abilities.Earth.RaiseEarth.Column.Height", 6);
		config.addDefault("Abilities.Earth.RaiseEarth.Wall.Range", 15);
		config.addDefault("Abilities.Earth.RaiseEarth.Wall.Height", 6);
		config.addDefault("Abilities.Earth.RaiseEarth.Wall.Width", 6);

		config.addDefault("Abilities.Earth.Shockwave.Enabled", true);
		config.addDefault("Abilities.Earth.Shockwave.Description", "This is one of the most powerful moves in the earthbender's arsenal. "
				+ "To use, you must first charge it by holding sneak (default: shift). "
				+ "Once charged, you can release sneak to create an enormous shockwave of earth, "
				+ "disturbing all earth around you and expanding radially outwards. "
				+ "Anything caught in the shockwave will be blasted back and dealt damage. "
				+ "If you instead click while charged, the disruption is focused in a cone in front of you. "
				+ "Lastly, if you fall from a great enough height with this ability selected, you will automatically create a shockwave.");
		config.addDefault("Abilities.Earth.Shockwave.FallThreshold", 10);
		config.addDefault("Abilities.Earth.Shockwave.ChargeTime", 2500);
		config.addDefault("Abilities.Earth.Shockwave.Damage", 5);
		config.addDefault("Abilities.Earth.Shockwave.Knockback", 1.1);
		config.addDefault("Abilities.Earth.Shockwave.Range", 15);
		
		config.addDefault("Abilities.Earth.SandSpout.Enabled", true);
		config.addDefault("Abilities.Earth.SandSpout.Description", "This ability provides a Sandbenders with a means of transportation. To use, simply left click while over sand or sandstone to raise the sand up beneath you, experiencing controlled levitation. Left clicking again while the spout is active will cause it to disappear.");
		config.addDefault("Abilities.Earth.SandSpout.Height", 7);
		config.addDefault("Abilities.Earth.SandSpout.BlindnessTime", 10);
		config.addDefault("Abilities.Earth.SandSpout.SpoutDamage", 1);

		config.addDefault("Abilities.Earth.Tremorsense.Enabled", true);
		config.addDefault("Abilities.Earth.Tremorsense.Description", "This is a pure utility ability for earthbenders. If you are in an area of low-light and are standing on top of an earthbendable block, this ability will automatically turn that block into glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out as you have lost contact with the earth and cannot 'see' until you can touch earth again. Additionally, if you click with this ability selected, smoke will appear above nearby earth with pockets of air beneath them.");
		config.addDefault("Abilities.Earth.Tremorsense.MaxDepth", 10);
		config.addDefault("Abilities.Earth.Tremorsense.Radius", 5);
		config.addDefault("Abilities.Earth.Tremorsense.LightThreshold", 7);
		config.addDefault("Abilities.Earth.Tremorsense.Cooldown", 1000);

		config.addDefault("Abilities.Fire.Blaze.Enabled", true);
		config.addDefault("Abilities.Fire.Blaze.Description", "To use, simply left-click in any direction. An arc of fire will flow from your location, igniting anything in its path. Additionally, tap sneak to engulf the area around you in roaring flames.");
		config.addDefault("Abilities.Fire.Blaze.ArcOfFire.Arc", 20);
		config.addDefault("Abilities.Fire.Blaze.ArcOfFire.Range", 9);
		config.addDefault("Abilities.Fire.Blaze.RingOfFire.Range", 7);

		config.addDefault("Abilities.Fire.Combustion.Enabled", true);
		config.addDefault("Abilities.Fire.Combustion.Description", "Combustion is a powerful ability only known by a few skilled Firebenders. It allows the bender to Firebend with their mind, concentrating energy to create a powerful blast. To use, simply tap sneak (Default: Shift) to launch the blast. This technique is highly destructive and very effective, it also comes with a long cooldown.");
		config.addDefault("Abilities.Fire.Combustion.Cooldown", 15000);
		//		config.addDefault("Abilities.Fire.Combustion.ChargeTime", 5000);
		config.addDefault("Abilities.Fire.Combustion.BreakBlocks", false);
		config.addDefault("Abilities.Fire.Combustion.Power", 1.0);
		config.addDefault("Abilities.Fire.Combustion.Damage", 5);
		config.addDefault("Abilities.Fire.Combustion.Radius", 4);
		config.addDefault("Abilities.Fire.Combustion.Range", 20);
		config.addDefault("Abilities.Fire.Combustion.Speed", 25);

		config.addDefault("Abilities.Fire.FireBlast.Enabled", true);
		config.addDefault("Abilities.Fire.FireBlast.Description","FireBlast is the most fundamental bending technique of a firebender. "
				+ "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. "
				+ "If this blast contacts an enemy, it will dissipate and engulf them in flames, "
				+ "doing additional damage and knocking them back slightly. "
				+ "If the blast hits terrain, it will ignite the nearby area. "
				+ "Additionally, if you hold sneak, you will charge up the fireblast. "
				+ "If you release it when it's charged, it will instead launch a powerful "
				+ "fireball that explodes on contact.");
		config.addDefault("Abilities.Fire.FireBlast.Speed", 15);
		config.addDefault("Abilities.Fire.FireBlast.Range", 15);
		config.addDefault("Abilities.Fire.FireBlast.Radius", 2);
		config.addDefault("Abilities.Fire.FireBlast.Push", 0.3);
		config.addDefault("Abilities.Fire.FireBlast.Damage", 2);
		config.addDefault("Abilities.Fire.FireBlast.Cooldown", 1500);
		config.addDefault("Abilities.Fire.FireBlast.Dissipate", false);
		config.addDefault("Abilities.Fire.FireBlast.Charged.ChargeTime", 2000);
		config.addDefault("Abilities.Fire.FireBlast.Charged.Damage", 4);
		config.addDefault("Abilities.Fire.FireBlast.Charged.DamageRadius", 6);
		config.addDefault("Abilities.Fire.FireBlast.Charged.Power", 1);
		config.addDefault("Abilities.Fire.FireBlast.Charged.Range", 20);

		config.addDefault("Abilities.Fire.FireBurst.Enabled", true);
		config.addDefault("Abilities.Fire.FireBurst.Description", "FireBurst is a very powerful firebending ability. "
				+ "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of flames in front of you, or click to release the burst in a sphere around you. ");
		config.addDefault("Abilities.Fire.FireBurst.Damage", 3);
		config.addDefault("Abilities.Fire.FireBurst.ChargeTime", 2500);
		config.addDefault("Abilities.Fire.FireBurst.Range", 15);

		config.addDefault("Abilities.Fire.FireJet.Enabled", true);
		config.addDefault("Abilities.Fire.FireJet.Description", "This ability is used for a limited burst of flight for firebenders. Clicking with this "
				+ "ability selected will launch you in the direction you're looking, granting you "
				+ "controlled flight for a short time. This ability can be used mid-air to prevent falling "
				+ "to your death, but on the ground it can only be used if standing on a block that's "
				+ "ignitable (e.g. not snow or water).");
		config.addDefault("Abilities.Fire.FireJet.Speed", 0.7);
		config.addDefault("Abilities.Fire.FireJet.Duration", 1500);
		config.addDefault("Abilities.Fire.FireJet.Cooldown", 6000);
		config.addDefault("Abilities.Fire.FireJet.IsAvatarStateToggle", true);

		config.addDefault("Abilities.Fire.FireShield.Enabled", true);
		config.addDefault("Abilities.Fire.FireShield.Description", "FireShield is a basic defensive ability. "
				+ "Clicking with this ability selected will create a "
				+ "small disc of fire in front of you, which will block most "
				+ "attacks and bending. Alternatively, pressing and holding "
				+ "sneak creates a very small shield of fire, blocking most attacks. "
				+ "Creatures that contact this fire are ignited.");
		config.addDefault("Abilities.Fire.FireShield.Radius", 3);
		config.addDefault("Abilities.Fire.FireShield.DiscRadius", 1.5);
		config.addDefault("Abilities.Fire.FireShield.Duration", 1000);

		config.addDefault("Abilities.Fire.HeatControl.Enabled", true);
		config.addDefault("Abilities.Fire.HeatControl.Description", "While this ability is selected, the firebender becomes impervious "
				+ "to fire damage and cannot be ignited. "
				+ "If the user left-clicks with this ability, the targeted area will be "
				+ "extinguished, although it will leave any creature burning engulfed in flames. "
				+ "This ability can also cool lava. If this ability is used while targetting ice or snow, it"
				+ " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.");
		config.addDefault("Abilities.Fire.HeatControl.Extinguish.Range", 20);
		config.addDefault("Abilities.Fire.HeatControl.Extinguish.Radius", 7);
		config.addDefault("Abilities.Fire.HeatControl.Solidify.Range", 10);
		config.addDefault("Abilities.Fire.HeatControl.Solidify.Radius", 5);
		config.addDefault("Abilities.Fire.HeatControl.Solidify.RevertTime", 20000);
		config.addDefault("Abilities.Fire.HeatControl.Melt.Range", 15);
		config.addDefault("Abilities.Fire.HeatControl.Melt.Radius", 5);

		config.addDefault("Abilities.Fire.Illumination.Enabled", true);
		config.addDefault("Abilities.Fire.Illumination.Description", "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
				+ "will create a torch that follows you around. The torch will only appear on objects that are "
				+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
				+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
				+ "dismisses this torch.");
		config.addDefault("Abilities.Fire.Illumination.Range", 5);

		config.addDefault("Abilities.Fire.Lightning.Enabled", true);
		config.addDefault("Abilities.Fire.Lightning.Description", "Hold sneak while selecting this ability to charge up a lightning strike. Once charged, release sneak to discharge the lightning to the targeted location.");
		config.addDefault("Abilities.Fire.Lightning.Damage", 6.0);
		config.addDefault("Abilities.Fire.Lightning.Range", 13.0);
		config.addDefault("Abilities.Fire.Lightning.ChargeTime", 3500);
		config.addDefault("Abilities.Fire.Lightning.Cooldown", 0);
		config.addDefault("Abilities.Fire.Lightning.StunChance", 0.20);
		config.addDefault("Abilities.Fire.Lightning.StunDuration", 30.0);
		config.addDefault("Abilities.Fire.Lightning.MaxArcAngle", 30);
		config.addDefault("Abilities.Fire.Lightning.SubArcChance", 0.025);
		config.addDefault("Abilities.Fire.Lightning.ChainArcRange", 6.0);
		config.addDefault("Abilities.Fire.Lightning.ChainArcChance", 0.50);
		config.addDefault("Abilities.Fire.Lightning.MaxChainArcs", 2);
		config.addDefault("Abilities.Fire.Lightning.WaterArcs", 4);
		config.addDefault("Abilities.Fire.Lightning.WaterArcRange", 12.0);
		config.addDefault("Abilities.Fire.Lightning.SelfHitWater", true);
		config.addDefault("Abilities.Fire.Lightning.SelfHitClose", true);
		config.addDefault("Abilities.Fire.Lightning.ArcOnIce", false);
		

		config.addDefault("Abilities.Fire.WallOfFire.Enabled", true);
		config.addDefault("Abilities.Fire.WallOfFire.Description", "To use this ability, click at a location. A wall of fire will appear at this location, igniting enemies caught in it and blocking projectiles.");
		config.addDefault("Abilities.Fire.WallOfFire.Range", 4);
		config.addDefault("Abilities.Fire.WallOfFire.Height", 4);
		config.addDefault("Abilities.Fire.WallOfFire.Width", 4);
		config.addDefault("Abilities.Fire.WallOfFire.Duration", 5000);
		config.addDefault("Abilities.Fire.WallOfFire.Damage", 2);
		config.addDefault("Abilities.Fire.WallOfFire.Cooldown", 7500);
		config.addDefault("Abilities.Fire.WallOfFire.Interval", 500);

		config.addDefault("Abilities.Fire.FireCombo.Enabled", true);
		config.addDefault("Abilities.Fire.FireCombo.FireKick.Range", 7.0);
		config.addDefault("Abilities.Fire.FireCombo.FireKick.Damage", 3.0);
		config.addDefault("Abilities.Fire.FireCombo.FireKick.Cooldown", 2000);
		config.addDefault("Abilities.Fire.FireCombo.FireSpin.Range", 7);
		config.addDefault("Abilities.Fire.FireCombo.FireSpin.Damage", 3.0);
		config.addDefault("Abilities.Fire.FireCombo.FireSpin.Knockback", 3.2);
		config.addDefault("Abilities.Fire.FireCombo.FireSpin.Cooldown", 2000);
		config.addDefault("Abilities.Fire.FireCombo.FireWheel.Range", 20.0);
		config.addDefault("Abilities.Fire.FireCombo.FireWheel.Damage", 4.0);
		config.addDefault("Abilities.Fire.FireCombo.FireWheel.Speed", 0.55);
		config.addDefault("Abilities.Fire.FireCombo.FireWheel.Cooldown", 200);
		config.addDefault("Abilities.Fire.FireCombo.JetBlast.Speed", 1.2);
		config.addDefault("Abilities.Fire.FireCombo.JetBlast.Cooldown", 6000);
		config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Speed", 1.1);
		config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Damage", 3);
		config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Cooldown", 6000);

		config.addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
		config.addDefault("Abilities.Chi.Passive.Speed", 1);
		config.addDefault("Abilities.Chi.Passive.Jump", 2);
		config.addDefault("Abilities.Chi.Passive.BlockChi.Duration", 2500);
		config.addDefault("Abilities.Chi.Passive.DodgeChange", 25);
		      
		config.addDefault("Abilities.Chi.AcrobatStance.Enabled", true);
		config.addDefault("Abilities.Chi.AcrobatStance.Description", "AcrobatStance gives a Chiblocker a higher probability of blocking a Bender's Chi while granting them a Speed and Jump Boost. It also increases the rate at which the hunger bar depletes. To use, simply left click. Left clicking again will de-activate the stance.");
		config.addDefault("Abilities.Chi.ChiBlockBoost", 0.1);
		
		config.addDefault("Abilities.Chi.HighJump.Enabled", true);
		config.addDefault("Abilities.Chi.HighJump.Description", "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.");
		config.addDefault("Abilities.Chi.HighJump.Height", 1);
		config.addDefault("Abilities.Chi.HighJump.Cooldown", 10000);

		config.addDefault("Abilities.Chi.Paralyze.Enabled", true);
		config.addDefault("Abilities.Chi.Paralyze.Description", "Paralyzes the target, making them unable to do anything for a short "
				+ "period of time. This ability has a long cooldown.");
		config.addDefault("Abilities.Chi.Paralyze.Cooldown", 15000);
		config.addDefault("Abilities.Chi.Paralyze.Duration", 2000);

		config.addDefault("Abilities.Chi.RapidPunch.Enabled", true);
		config.addDefault("Abilities.Chi.RapidPunch.Description", "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch. This has a short cooldown.");
		config.addDefault("Abilities.Chi.RapidPunch.Damage", 1);
		config.addDefault("Abilities.Chi.RapidPunch.Distance", 4);
		config.addDefault("Abilities.Chi.RapidPunch.Cooldown", 15000);
		config.addDefault("Abilities.Chi.RapidPunch.Punches", 4);

		config.addDefault("Abilities.Chi.Smokescreen.Enabled", true);
		config.addDefault("Abilities.Chi.Smokescreen.Description", "Smokescren, if used correctly, can serve as a defensive and offensive ability for Chiblockers. To use, simply left click and you will toss out a Smoke Bomb. When the bomb hits the ground, it will explode and give all players within a small radius of the explosion temporary blindness, allowing you to either get away, or move in for the kill. This ability has a long cooldown.");
		config.addDefault("Abilities.Chi.Smokescreen.Cooldown", 50000);
		config.addDefault("Abilities.Chi.Smokescreen.Radius", 4);
		config.addDefault("Abilities.Chi.Smokescreen.Duration", 15);

		config.addDefault("Abilities.Chi.WarriorStance.Enabled", true);
		config.addDefault("Abilities.Chi.WarriorStance.Description", "WarriorStance gives a Chiblocker increased damage but makes them a tad more vulnerable. To activate, simply left click.");
		config.addDefault("Abilities.Chi.WarriorStance.Strength", 1);
		config.addDefault("Abilities.Chi.WarriorStance.Resistance", -1);
		
		config.addDefault("Abilities.Chi.QuickStrike.Enabled", true);
		config.addDefault("Abilities.Chi.QuickStrike.Description", "QuickStrike enables a chiblocker to quickly strike an enemy, potentially blocking their chi.");
		config.addDefault("Abilities.Chi.QuickStrike.Damage", 2);
		config.addDefault("Abilities.Chi.QuickStrike.ChiBlockChance", 20);
		
		config.addDefault("Abilities.Chi.SwiftKick.Enabled", true);
		config.addDefault("Abilities.Chi.SwiftKick.Description", "SwiftKick allows a chiblocker to swiftly kick an enemy, potentially blocking their chi. The chiblocker must be in the air to use this ability.");
		config.addDefault("Abilities.Chi.SwiftKick.Damage", 4);
		config.addDefault("Abilities.Chi.SwiftKick.ChiBlockChance", 30);
		
		config.addDefault("Abilities.Chi.ChiCombo.ParalyzeDuration", 10000);
		
		config.addDefault("Storage.engine", "sqlite");

		config.addDefault("Storage.MySQL.host", "localhost");
		config.addDefault("Storage.MySQL.port", 3306);
		config.addDefault("Storage.MySQL.pass", "");
		config.addDefault("Storage.MySQL.db", "minecraft");
		config.addDefault("Storage.MySQL.user", "root");

		config.addDefault("debug", false);
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
}
