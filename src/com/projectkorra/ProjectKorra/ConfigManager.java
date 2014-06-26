package com.projectkorra.ProjectKorra;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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

		plugin.getConfig().addDefault("Properties.GlobalCooldown", 500);
		plugin.getConfig().addDefault("Properties.SeaLevel", 62);

		plugin.getConfig().addDefault("Properties.Air.CanBendWithWeapons", false);

		plugin.getConfig().addDefault("Properties.Water.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Water.NightFactor", 1.5);

		plugin.getConfig().addDefault("Properties.Earth.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Earth.EarthbendableBlocks", earthbendable);

		plugin.getConfig().addDefault("Properties.Fire.CanBendWithWeapons", true);
		plugin.getConfig().addDefault("Properties.Fire.DayFactor", 1.5);

		plugin.getConfig().addDefault("Properties.Chi.CanBendWithWeapons", true);

		plugin.getConfig().addDefault("Abilities.AvatarState.Enabled", true);
		plugin.getConfig().addDefault("Abilities.AvatarState.Description", "The signature ability of the Avatar, this is a toggle. Click to activate to become "
				+ "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from "
				+ "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities "
				+ "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able "
				+ "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar "
				+ "State selected to deactivate it.");

		plugin.getConfig().addDefault("Abilities.Air.Passive.Factor", 0.3);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Speed", 2);
		plugin.getConfig().addDefault("Abilities.Air.Passive.Jump", 3);

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

		config.addDefault("Abilities.Air.AirScooter.Enabled", true);
		config.addDefault("Abilities.Air.AirScooter.Description", "AirScooter is a fast means of transportation. To use, sprint, jump then click with "
				+ "this ability selected. You will hop on a scooter of air and be propelled forward "
				+ "in the direction you're looking (you don't need to press anything). "
				+ "This ability can be used to levitate above liquids, but it cannot go up steep slopes. "
				+ "Any other actions will deactivate this ability.");
		config.addDefault("Abilities.Air.AirScooter.Speed", .675);

		plugin.getConfig().addDefault("Abilities.Air.Tornado.Enabled", true);
		plugin.getConfig().addDefault("Abilities.Air.Tornado.Description", "To use, simply sneak (default: shift). "
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

		config.addDefault("Abilities.Air.Tornado.Radius", 10);
		config.addDefault("Abilities.Air.Tornado.Height", 25);
		config.addDefault("Abilities.Air.Tornado.Range", 25);
		config.addDefault("Abilities.Air.Tornado.MobPushFactor", 1);
		config.addDefault("Abilities.Air.Tornado.PlayerPushFactor", 1);

		plugin.getConfig().addDefault("Abilities.Water.Passive.SwimSpeedFactor", 0.7);

		config.addDefault("Abilities.Water.Bloodbending.Enabled", true);
		config.addDefault("Abilities.Water.Bloodbending.Description", "This ability was made illegal for a reason. With this ability selected, sneak while "
				+ "targetting something and you will bloodbend that target. Bloodbent targets cannot move, "
				+ "bend or attack. You are free to control their actions by looking elsewhere - they will "
				+ "be forced to move in that direction. Additionally, clicking while bloodbending will "
				+ "launch that target off in the direction you're looking. "
				+ "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.");
		config.addDefault("Abilities.Water.Bloodbending.ThrowFactor", 2);
		config.addDefault("Abilities.Water.Bloodbending.Range", 10);

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

		config.addDefault("Abilities.Water.OctopusForm.Enabled", true);
		config.addDefault("Abilities.Water.OctopusForm.Description", "This ability allows the waterbender to manipulate a large quantity of water into a form resembling that of an octopus. "
				+ "To use, click to select a water source. Then, hold sneak to channel this ability. "
				+ "While channeling, the water will form itself around you and has a chance to block incoming attacks. "
				+ "Additionally, you can click while channeling to attack things near you, dealing damage and knocking them back. "
				+ "Releasing shift at any time will dissipate the form.");

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
		config.addDefault("Abilities.Water.Surge.Wave.HorizontalPush", 1);
		config.addDefault("Abilities.Water.Surge.VerticalPush", 0.2);
		config.addDefault("Abilities.Water.Surge.Wall.Range", 5);
		config.addDefault("Abilities.Water.Surge.Wall.Radius", 2);

		config.addDefault("Abilities.Water.Torrent.Enabled", true);
		config.addDefault("Abilities.Water.Torrent.Description", "Torrent is one of the strongest moves in a waterbender's arsenal. To use, first click a source block to select it; then hold shift to begin streaming the water around you. Water flowing around you this way will damage and knock back nearby enemies and projectiles. If you release shift during this, you will create a large wave that expands outwards from you, launching anything in its path back. Instead, if you click you release the water and channel it to flow towards your cursor. Anything caught in the blast will be tossed about violently and take damage. Finally, if you click again when the water is torrenting, it will freeze the area around it when it is obstructed.");

		plugin.getConfig().addDefault("Abilities.Water.Plantbending.RegrowTime", 180000);

		config.addDefault("Abilities.Water.WaterBubble.Enabled", true);
		config.addDefault("Abilities.Water.WaterBubble.Description","To use, the bender must merely have the ability selected. All water around the user in a small bubble will vanish, replacing itself once the user either gets too far away or selects a different ability.");
		config.addDefault("Abilities.Water.WaterBubble.Radius", 7);

		config.addDefault("Abilities.Water.WaterSpout.Enabled", true);
		config.addDefault("Abilities.Water.WaterSpout.Description", "To use this ability, click while over or in water. "
				+ "You will spout water up from beneath you to experience controlled levitation. "
				+ "This ability is a toggle, so you can activate it then use other abilities and it "
				+ "will remain on. If you try to spout over an area with no water, snow or ice, "
				+ "the spout will dissipate and you will fall. Click again with this ability selected to deactivate it.");
		config.addDefault("Abilities.Water.WaterSpout.Height", 20);

		plugin.getConfig().addDefault("Abilities.Earth.Passive.Duration", 2500);

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
		config.addDefault("Abilities.fire.FireBlast.Dissipate", false);

		config.addDefault("Abilities.Fire.FireJet.Enabled", true);
		config.addDefault("Abilities.Fire.FireJet.Description", "This ability is used for a limited burst of flight for firebenders. Clicking with this "
				+ "ability selected will launch you in the direction you're looking, granting you "
				+ "controlled flight for a short time. This ability can be used mid-air to prevent falling "
				+ "to your death, but on the ground it can only be used if standing on a block that's "
				+ "ignitable (e.g. not snow or water).");
		config.addDefault("Abilities.Fire.FireJet.Speed", 0.7);
		config.addDefault("Abilities.Fire.FireJet.Duration", 1500);
		config.addDefault("Abilities.Fire.FireJet.Cooldown", 6000);

		config.addDefault("Abilities.Fire.HeatControl.Enabled", true);
		config.addDefault("Abilities.Fire.HeatControl.Description", "While this ability is selected, the firebender becomes impervious "
				+ "to fire damage and cannot be ignited. "
				+ "If the user left-clicks with this ability, the targeted area will be "
				+ "extinguished, although it will leave any creature burning engulfed in flames. "
				+ "This ability can also cool lava. If this ability is used while targetting ice or snow, it"
				+ " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.");
		config.addDefault("Abilities.Fire.HeatControl.Extinguish.Range", 20);
		config.addDefault("Abilities.Fire.HeatControl.Extinguish.Radius", 7);
		config.addDefault("Abilities.Fire.HeatControl.Melt.Range", 15);
		config.addDefault("Abilities.Fire.HeatControl.Melt.Radius", 5);

		config.addDefault("Abilities.Fire.Illumination.Enabled", true);
		config.addDefault("Abilities.Fire.Illumination.Description", "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
				+ "will create a torch that follows you around. The torch will only appear on objects that are "
				+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
				+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
				+ "dismisses this torch.");
		config.addDefault("Abilities.Fire.Illumination.Range", 5);

		plugin.getConfig().addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Speed", 1);
		plugin.getConfig().addDefault("Abilities.Chi.Passive.Jump", 2);

		config.addDefault("Abilities.Chi.Paralyze.Enabled", true);
		config.addDefault("Abilities.Chi.Paralyze.Description", "Paralyzes the target, making them unable to do anything for a short "
				+ "period of time. This ability has a long cooldown.");
		config.addDefault("Abilities.Chi.Paralyze.Cooldown", 15000);
		config.addDefault("Abilities.Chi.Paralyze.Duration", 2000);

		plugin.getConfig().addDefault("Storage.engine", "sqlite");

		plugin.getConfig().addDefault("Storage.MySQL.host", "localhost");
		plugin.getConfig().addDefault("Storage.MySQL.port", 3306);
		plugin.getConfig().addDefault("Storage.MySQL.pass", "");
		plugin.getConfig().addDefault("Storage.MySQL.db", "minecraft");
		plugin.getConfig().addDefault("Storage.MySQL.user", "root");

		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}
}
