package com.projectkorra.projectkorra.configuration;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;

public class ConfigManager {

	public static Config presetConfig;
	public static Config defaultConfig;
	public static Config languageConfig;
	
	public ConfigManager() {
		presetConfig = new Config(new File("presets.yml"));
		defaultConfig = new Config(new File("config.yml"));
		languageConfig = new Config(new File("language.yml"));
		configCheck(ConfigType.DEFAULT);
		configCheck(ConfigType.LANGUAGE);
		configCheck(ConfigType.PRESETS);
	}

	public static void configCheck(ConfigType type) {
		FileConfiguration config;
		if (type == ConfigType.PRESETS) {
			config = presetConfig.get();

			ArrayList<String> abilities = new ArrayList<String>();
			abilities.add("FireBlast");
			abilities.add("AirBlast");
			abilities.add("WaterManipulation");
			abilities.add("EarthBlast");
			abilities.add("FireBurst");
			abilities.add("AirBurst");
			abilities.add("Torrent");
			abilities.add("Shockwave");
			abilities.add("AvatarState");
			
			config.addDefault("Example", abilities);

			presetConfig.save();
		} else if (type == ConfigType.LANGUAGE) {
			config = languageConfig.get();
			
			ArrayList<String> helpLines = new ArrayList<String>();
			helpLines.add("&c/bending help [Ability/Command] &eDisplay help.");
			helpLines.add("&c/bending choose [Element] &eChoose an element.");
			helpLines.add("&c/bending bind [Ability] # &eBind an ability.");
			
			config.addDefault("Chat.Enable", true);
			config.addDefault("Chat.Format", "<name>: <message>");
			config.addDefault("Chat.Colors.Avatar", "DARK_PURPLE");
			config.addDefault("Chat.Colors.Air", "GRAY");
			config.addDefault("Chat.Colors.AirSub", "DARK_GRAY");
			config.addDefault("Chat.Colors.Water", "AQUA");
			config.addDefault("Chat.Colors.WaterSub", "DARK_AQUA");
			config.addDefault("Chat.Colors.Earth", "GREEN");
			config.addDefault("Chat.Colors.EarthSub", "DARK_GREEN");
			config.addDefault("Chat.Colors.Fire", "RED");
			config.addDefault("Chat.Colors.FireSub", "DARK_RED");
			config.addDefault("Chat.Colors.Chi", "GOLD");
			
			config.addDefault("Chat.Prefixes.Air", "[Air]");
			config.addDefault("Chat.Prefixes.Earth", "[Earth]");
			config.addDefault("Chat.Prefixes.Fire", "[Fire]");
			config.addDefault("Chat.Prefixes.Water", "[Water]");
			config.addDefault("Chat.Prefixes.Chi", "[Chi]");
			config.addDefault("Chat.Prefixes.Avatar", "[Avatar]");
			config.addDefault("Chat.Prefixes.Nonbender", "[Nonbender]");
			
			config.addDefault("Extras.Water.NightMessage", "Your waterbending has become empowered due to the full moon rising.");
			config.addDefault("Extras.Water.DayMessage", "You feel the empowering of your waterbending subside as the moon sets.");
			config.addDefault("Extras.Fire.NightMessage", "You feel the empowering of your firebending subside as the sun sets.");
			config.addDefault("Extras.Fire.DayMessage", "You feel the strength of the rising sun empowering your firebending.");
			
			config.addDefault("Commands.NoPermission", "You do not have permission to do that.");
			config.addDefault("Commands.MustBePlayer", "You must be a player to perform this action.");
			config.addDefault("Commands.GeneralHelpLines", helpLines);
			
			config.addDefault("Commands.Who.Description", "This command will tell you what element all players that are online are (If you don't specify a player) or give you information about the player that you specify.");
			config.addDefault("Commands.Who.NoPlayersOnline", "There is no one online.");
			config.addDefault("Commands.Who.DatabaseOverload", "The database appears to be overloaded. Please try again later.");
			config.addDefault("Commands.Who.PlayerOffline", "{target} is currently offline. A lookup is currently being done (this might take a few seconds).");
			
			config.addDefault("Commands.Version.Description", "Displays the installed version of ProjectKorra.");
			
			config.addDefault("Commands.Toggle.Description", "This command will toggle a player's own Bending on or off. If toggled off, all abilities should stop working until it is toggled back on. Logging off will automatically toggle your Bending back on. If you run the command /bending toggle all, Bending will be turned off for all players and cannot be turned back on until the command is run again.");
			config.addDefault("Commands.Toggle.ToggledOn", "You have turned your bending back on.");
			config.addDefault("Commands.Toggle.ToggledOff", "Your bending has been toggled off. You will not be able to use most abilities until you toggle it back.");
			config.addDefault("Commands.Toggle.ToggleOnSingleElement", "You have toggled on your {element}.");
			config.addDefault("Commands.Toggle.ToggleOffSingleElement", "You have toggled off your {element}.");
			config.addDefault("Commands.Toggle.WrongElement", "You do not have that element.");
			config.addDefault("Commands.Toggle.All.ToggledOffForAll", "Bending is currently toggled off for all players.");
			config.addDefault("Commands.Toggle.All.ToggleOn", "Bending has been toggled back on for all players.");
			config.addDefault("Commands.Toggle.All.ToggleOff", "Bending has been toggled off for all players.");
			config.addDefault("Commands.Toggle.Other.ToggledOnElementConfirm", "You've toggled on {target}'s {element}");
			config.addDefault("Commands.Toggle.Other.ToggledOffElementConfirm", "You've toggled off {target}'s {element}");
			config.addDefault("Commands.Toggle.Other.ToggledOnElementByOther", "Your {element} has been toggled on by {sender}.");
			config.addDefault("Commands.Toggle.Other.ToggledOffElementByOther", "Your {element} has been toggled off by {sender}.");
			config.addDefault("Commands.Toggle.Other.PlayerNotFound", "Target is not found.");
			config.addDefault("Commands.Toggle.Other.WrongElement", "{target} doesn't have that element.");
			
			config.addDefault("Commands.Remove.Description", "This command will remove the element of the targeted [Player]. The player will be able to re-pick their element after this command is run on them, assuming their Bending was not permaremoved.");
			config.addDefault("Commands.Remove.Other.RemovedAllElements", "Your bending has been removed by {sender}.");
			config.addDefault("Commands.Remove.Other.RemovedAllElementsConfirm", "You've removed {target}'s bending.");
			config.addDefault("Commands.Remove.Other.RemovedElement", "Your {element} has been removed by {sender}.");
			config.addDefault("Commands.Remove.Other.RemovedElementConfirm", "You removed {target}'s {element}.");
			config.addDefault("Commands.Remove.Other.WrongElement", "{target} does not have that element!");
			config.addDefault("Commands.Remove.RemovedElement", "You've removed your {element}.");
			config.addDefault("Commands.Remove.InvalidElement", "That element is invalid!");
			config.addDefault("Commands.Remove.WrongElement", "You do not have that element!");
			config.addDefault("Commands.Remove.PlayerOffline", "That player is offline!");
			
			config.addDefault("Commands.Reload.Description", "This command will reload the Bending config file.");
			config.addDefault("Commands.Reload.SuccessfullyReloaded", "Bending Config reloaded!");
			
			config.addDefault("Commands.Preset.Description", "This command manages Presets, which are saved bindings. Use /bending preset list to view your existing presets, use /bending [create|delete] [name] to manage your presets, and use /bending bind [name] to bind an existing preset.");
			config.addDefault("Commands.Preset.NoPresets", "You do not have any presets.");
			config.addDefault("Commands.Preset.NoPresetName", "You don't have a preset with that name.");
			config.addDefault("Commands.Preset.Created", "Created a new preset named '{name}'.");
			config.addDefault("Commands.Preset.Delete", "You have deleted your '{name}' preset.");
			config.addDefault("Commands.Preset.Removed", "Your bending has been permanently removed.");
			config.addDefault("Commands.Preset.RemovedConfirm", "You have permanently removed {target}'s bending.");
			config.addDefault("Commands.Preset.SuccesfullyBound", "Your binds have been set to match the {name} preset.");
			config.addDefault("Commands.Preset.SuccesfullyCopied", "Your binds have been set to match {target}'s binds.");
			config.addDefault("Commands.Preset.FailedToBindAll", "Some abilities were not bound because you cannot bend the required element.");
			config.addDefault("Commands.Preset.AlreadyExists", "A preset with that name already exists.");
			config.addDefault("Commands.Preset.BendingPermanentlyRemoved", "Your bending was permanently removed.");
			config.addDefault("Commands.Preset.PlayerNotFound", "Player not found.");
			config.addDefault("Commands.Preset.MaxPresets", "You've reached your maximum number of presets.");
			config.addDefault("Commands.Preset.CantEditBinds", "You can't edit your binds right now!");
			config.addDefault("Commands.Preset.Other.BendingPermanentlyRemoved", "That player's bending was permanently removed.");
			config.addDefault("Commands.Preset.Other.SuccesfullyBoundConfirm", "The bound slots of {target} have been set to match the {name} preset.");
			config.addDefault("Commands.Preset.External.NoPresetName", "No external preset found with that name.");
			
			config.addDefault("Commands.PermaRemove.Description", "This command will permanently remove the Bending of the targeted <Player>. Once removed, a player may only receive Bending again if this command is run on them again. This command is typically reserved for administrators.");
			config.addDefault("Commands.PermaRemove.PlayerOffline", "That player is not online.");
			config.addDefault("Commands.PermaRemove.Restored", "Your bending has been restored.");
			config.addDefault("Commands.PermaRemove.RestoredConfirm", "You have restored the bending of {target}.");
			config.addDefault("Commands.PermaRemove.Removed", "Your bending has been permanently removed.");
			config.addDefault("Commands.PermaRemove.RemovedConfirm", "You have removed the bending of {target}.");
			
			config.addDefault("Commands.Invincible.Description", "This command will make you impervious to all Bending damage. Once you use this command, you will stay invincible until you log off or use this command again.");
			config.addDefault("Commands.Invincible.ToggledOn", "You are now invincible to all bending damage and effects. Use this command again to disable this.");
			config.addDefault("Commands.Invincible.ToggledOff", "You are no longer invincible to all bending damage and effects.");
			
			config.addDefault("Commands.Import.Description", "This command will import your old bendingPlayers.yml from the Bending plugin. It will generate a convert.yml file to convert the data to be used with this plugin. You can delete the file once the complete message is displayed. This command should only be used ONCE.");
			config.addDefault("Commands.Import.Disabled", "Import has been disabled in the config!");
			config.addDefault("Commands.Import.PreparingData", "Preparing Data for import.");
			config.addDefault("Commands.Import.ImportStarted", "Import of data started. Do NOT stop / reload your server.");
			config.addDefault("Commands.Import.DebugWarning", "Console will print out all of the players that are imported if debug mode is enabled as they import.");
			config.addDefault("Commands.Import.DataQueuedUp", "All data has been queued up, please allow up to 5 minutes for the data to complete, then reboot your server.");
			
			config.addDefault("Commands.Help.Description", "This command provides information on how to use other commands in ProjectKorra.");
			config.addDefault("Commands.Help.Required", "Required");
			config.addDefault("Commands.Help.Optional", "Optional");
			config.addDefault("Commands.Help.ProperUsage", "Proper Usage: {command1} or {command2}");
			config.addDefault("Commands.Help.Elements.LearnMore", "Learn more: ");
			config.addDefault("Commands.Help.InvalidTopic", "That isn't a valid help topic. Use /bending help for more information.");
			config.addDefault("Commands.Help.Usage", "Usage: ");
			
			config.addDefault("Commands.Display.Description", "This command will show you all of the elements you have bound if you do not specify an element. If you do specify an element (Air, Water, Earth, Fire, or Chi), it will show you all of the available abilities of that element installed on the server.");;
			config.addDefault("Commands.Display.NoCombosAvailable", "There are no {element} combos available.");
			config.addDefault("Commands.Display.NoAbilitiesAvailable", "There are no {element} abilities on this server!");
			config.addDefault("Commands.Display.InvalidArgument", "Not a valid argument.");
			config.addDefault("Commands.Display.PlayersOnly", "This command is only useable by players.");
			config.addDefault("Commands.Display.NoBinds", "You do not have any abilities bound.\nIf you would like to see a list of available abilities, please use the /bending display [Element] command. Use /bending help for more information.");
			
			config.addDefault("Commands.Debug.Description", "Outputs information on the current ProjectKorra installation to /plugins/ProjectKorra/debug.txt");
			config.addDefault("Commands.Debug.SuccessfullyExported", "Debug File Created as debug.txt in the ProjectKorra plugin folder.\nPut contents on pastie.org and create a bug report  on the ProjectKorra forum if you need to.");
			
			config.addDefault("Commands.Copy.Description", "This command will allow the user to copy the binds of another player either for himself or assign them to <Player> if specified.");
			config.addDefault("Commands.Copy.PlayerNotFound", "Couldn't find player.");
			config.addDefault("Commands.Copy.SuccessfullyCopied", "Your binds have been set to match {target}'s!");
			config.addDefault("Commands.Copy.FailedToBindAll", "Not all moves have been bound because you do not have the permission to.");
			config.addDefault("Commands.Copy.Other.SuccessfullyCopied", "{target1}'s binds have been set to match {target2}'s.");
			
			config.addDefault("Commands.Clear.Description", "This command will clear the bound ability from the slot you specify (if you specify one). If you choose not to specify a slot, all of your abilities will be cleared.");
			config.addDefault("Commands.Clear.CantEditBinds", "You can't edit your binds right now!");
			config.addDefault("Commands.Clear.Cleared", "Your bound abilities have been cleared.");
			config.addDefault("Commands.Clear.WrongNumber", "The slot must be an integer between 1 and 9.");
			config.addDefault("Commands.Clear.ClearedSlot", "You have cleared slot #{slot}.");
			config.addDefault("Commands.Clear.AlreadyEmpty", "That slot was is already empty.");
			
			config.addDefault("Commands.Choose.Description", "This command will allow the user to choose a player either for himself or <Player> if specified. This command can only be used once per player unless they have permission to rechoose their element.");
			config.addDefault("Commands.Choose.InvalidElement", "That is not a valid element.");
			config.addDefault("Commands.Choose.PlayerNotFound", "Could not find player.");
			config.addDefault("Commands.Choose.SuccessfullyChosenCFW", "You are now a {element}.");
			config.addDefault("Commands.Choose.SuccessfullyChosenAE", "You are now an {element}.");
			config.addDefault("Commands.Choose.Other.SuccessfullyChosenCFW", "{target} is now a {element}.");
			config.addDefault("Commands.Choose.Other.SuccessfullyChosenAE", "{target} is now an {element}.");
			
			config.addDefault("Commands.Check.Description", "Checks if ProjectKorra is up to date.");
			config.addDefault("Commands.Check.NewVersionAvailable", "There's a new version of ProjectKorra available!");
			config.addDefault("Commands.Check.CurrentVersion", "Current Version: {version}");
			config.addDefault("Commands.Check.LatestVersion", "Latest Version: {version}");
			config.addDefault("Commands.Check.UpToDate", "You have the latest version of ProjectKorra.");
			
			config.addDefault("Commands.Bind.Description", "This command will bind an ability to the slot you specify (if you specify one), or the slot currently selected in your hotbar (If you do not specify a Slot #).");
			config.addDefault("Commands.Bind.AbilityDoesntExist", "{ability} is not a valid ability.");
			config.addDefault("Commands.Bind.WrongNumber", "Slot must be an integer between 1 and 9.");
			config.addDefault("Commands.Bind.ElementToggledOff", "You have that ability's element toggled off currently.");
			config.addDefault("Commands.Bind.SuccessfullyBound", "Succesfully bound {ability} to slot {slot}.");
			config.addDefault("Commands.Bind.NoElement", "You are not a {element}!");
			config.addDefault("Commands.Bind.NoElementAE", "You are not an {element}!");
			config.addDefault("Commands.Bind.NoSubElement", "You don't have access to {subelement}!");
			
			config.addDefault("Commands.Add.Description", "This command will allow the user to add an element to the targeted <Player>, or themselves if the target is not specified. This command is typically reserved for server administrators.");
			config.addDefault("Commands.Add.SuccessfullyAddedCFW", "You are now also a {element}.");
			config.addDefault("Commands.Add.SuccessfullyAddedAE", "You are now also an {element}.");
			config.addDefault("Commands.Add.PlayerNotFound", "That player could not be found.");
			config.addDefault("Commands.Add.InvalidElement", "You must specify a valid element.");
			config.addDefault("Commands.Add.AlreadyHasElement", "You already have that element!");
			config.addDefault("Commands.Add.AlreadyHasSubElement", "You already have that subelement!");
			config.addDefault("Commands.Add.Other.SuccessfullyAddedCFW", "{target} is now also a {element}.");
			config.addDefault("Commands.Add.Other.SuccessfullyAddedAE", "{target} is now also an {element}.");
			config.addDefault("Commands.Add.Other.AlreadyHasElement", "{target} already has that element!");
			config.addDefault("Commands.Add.Other.AlreadyHasSubElement", "{target} already has that subelement!");
			
			config.addDefault("DeathMessages.Enabled", true);
			config.addDefault("DeathMessages.Default", "{victim} was slain by {attacker}'s {ability}");
			
			config.addDefault("Abilities.Avatar.AvatarState.Description", "The signature ability of the Avatar, this is a toggle. Click to activate to become " + "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from " + "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities " + "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able " + "abilities and last until you deactivate them or the Avatar State. Click again with the Avatar " + "State selected to deactivate it.");
			
			config.addDefault("Commands.Help.Elements.Air", "Air is the element of freedom. Airbenders are natural pacifists and great explorers. There is nothing stopping them from scaling the tallest of mountains and walls easily. They specialize in redirection, from blasting things away with gusts of winds, to forming a shield around them to prevent damage. Easy to get across flat terrains, such as oceans, there is practically no terrain off limits to Airbenders. They lack much raw damage output, but make up for it with with their ridiculous amounts of utility and speed.\nAirbenders can chain their abilities into combos, type /b help AirCombos for more information.");
			config.addDefault("Abilities.Air.AirBlast.Description", "AirBlast is the most fundamental bending technique of an airbender." + " To use, simply left-click in a direction. A gust of wind will be" + " created at your fingertips, launching anything in its path harmlessly back." + " A gust of air can extinguish fires on the ground or on a player, can cool lava, and " + "can flip levers and activate buttons. Additionally, tapping sneak will change the " + "origin of your next AirBlast to your targeted location.");
			config.addDefault("Abilities.Air.AirBlast.DeathMessage", "{victim} was flung by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBlast.HorizontalVelocityDeath","{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBubble.Description", "To use, the bender must hold down sneak. All water around the user in a small bubble will vanish, replacing itself once the user either gets too far away or selects a different ability.");
			config.addDefault("Abilities.Air.AirBurst.Description", "AirBurst is one of the most powerful abilities in the airbender's arsenal. " + "To use, press and hold sneak to charge your burst. " + "Once charged, you can either release sneak to release the burst in a sphere around you " + "or click to launch a cone-shaped burst of air in front of you. " + "Additionally, having this ability selected when you land on the ground from a " + "large enough fall will create a burst of air around you.");
			config.addDefault("Abilities.Air.AirBurst.DeathMessage", "{victim} was thrown down by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBurst.HorizontalVelocityDeath", "{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirScooter.Description", "AirScooter is a fast means of transportation. To use, sprint, jump then click with " + "this ability selected. You will hop on a scooter of air and be propelled forward " + "in the direction you're looking (you don't need to press anything). " + "This ability can be used to levitate above liquids, but it cannot go up steep slopes. " + "Any other actions will deactivate this ability.");
			config.addDefault("Abilities.Air.Tornado.Description", "To use, simply sneak (default: shift). " + "This will create a swirling vortex at the targeted location. " + "Any creature or object caught in the vortex will be launched up " + "and out in some random direction. If another player gets caught " + "in the vortex, the launching effect is minimal. Tornado can " + "also be used to transport the user. If the user gets caught in his/her " + "own tornado, his movements are much more manageable. Provided the user doesn't " + "fall out of the vortex, it will take him to a maximum height and move him in " + "the general direction he's looking. Skilled airbenders can scale anything " + "with this ability.");
			config.addDefault("Abilities.Air.AirShield.Description", "Air Shield is one of the most powerful defensive techniques in existence. " + "To use, simply sneak (default: shift). " + "This will create a whirlwind of air around the user, " + "with a small pocket of safe space in the center. " + "This wind will deflect all projectiles and will prevent any creature from " + "entering it for as long as its maintained.");
			config.addDefault("Abilities.Air.AirSpout.Description", "This ability gives the airbender limited sustained levitation. It is a " + "toggle - click to activate and form a whirling spout of air " + "beneath you, lifting you up. You can bend other abilities while using AirSpout. " + "Click again to deactivate this ability.");
			config.addDefault("Abilities.Air.AirSuction.Description", "To use, simply left-click in a direction. A gust of wind will originate as far as it can in that direction and flow towards you, sucking anything in its path harmlessly with it. Skilled benders can use this technique to pull items from precarious locations. Additionally, tapping sneak will change the origin of your next AirSuction to your targeted location.");
			config.addDefault("Abilities.Air.AirSuction.HorizontalVelocityDeath","{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirSwipe.Description", "To use, simply left-click in a direction. An arc of air will flow from you towards that direction, cutting and pushing back anything in its path. Its damage is minimal, but it still sends the message. This ability will extinguish fires, cool lava, and cut things like grass, mushrooms, and flowers. Additionally, you can charge it by holding sneak. Charging before attacking will increase damage and knockback, up to a maximum.");
			config.addDefault("Abilities.Air.AirSwipe.DeathMessage", "{victim} was struck by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.Flight.Description", "Jump in the air, crouch (default: shift) and hold with this ability bound and you will glide around in the direction you look. While flying, click to Hover. Click again to disable Hovering.");
			config.addDefault("Abilities.Air.Suffocate.Description", "This ability is one of the most dangerous abilities an Airbender possesses. To use, simply look at an entity and hold shift. The entity will begin taking damage as you extract the air from their lungs. Any bender caught in this sphere will only be able to use basic moves, such as AirSwipe, WaterManipulation, FireBlast, or EarthBlast. An entity can be knocked out of the sphere by certain bending arts, and your attention will be disrupted if you are hit by bending.");
			config.addDefault("Abilities.Air.Suffocate.DeathMessage", "{victim} was asphyxiated by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.Combo.Twister.Description", "Create a cyclone of air that travels along the ground grabbing nearby entities.");
			config.addDefault("Abilities.Air.Combo.AirStream.Description", "Control a large stream of air that grabs onto enemies allowing you to direct them temporarily.");
			config.addDefault("Abilities.Air.Combo.AirSweep.Description", "Sweep the air in front of you hitting multiple enemies, causing moderate damage and a large knockback. The radius and direction of AirSweep is controlled by moving your mouse in a sweeping motion. For example, if you want to AirSweep upward, then move your mouse upward right after you left click AirBurst");
			config.addDefault("Abilities.Air.Combo.AirSweep.DeathMessage", "{victim} was swept away by {attacker}'s {ability}");
			
			config.addDefault("Commands.Help.Elements.Water", "Water is the element of change. Waterbending focuses on using your opponents own force against them. Using redirection and various dodging tactics, you can be made practically untouchable by an opponent. Waterbending provides agility, along with strong offensive skills while in or near water.\nWaterbenders can chain their abilities into combos, type /b help WaterCombos for more information.");
			config.addDefault("Abilities.Water.Bloodbending.Description", "This ability was made illegal for a reason. With this ability selected, sneak while " + "targetting something and you will bloodbend that target. Bloodbent targets cannot move, " + "bend or attack. You are free to control their actions by looking elsewhere - they will " + "be forced to move in that direction. Additionally, clicking while bloodbending will " + "launch that target off in the direction you're looking. " + "People who are capable of bloodbending are immune to your technique, and you are immune to theirs.");
			config.addDefault("Abilities.Water.Bloodbending.DeathMessage", "{victim} was destroyed by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.Bloodbending.HorizontalVelocityDeath","{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.HealingWaters.Description", "To use, the bender must be at least partially submerged in water. " + "If the user is not sneaking, this ability will automatically begin " + "working provided the user has it selected. If the user is sneaking, " + "he/she is channeling the healing to their target in front of them. " + "In order for this channel to be successful, the user and the target must " + "be at least partially submerged in water.");
			config.addDefault("Abilities.Water.IceBlast.Description", "This ability offers a powerful ice utility for Waterbenders. It can be used to fire an explosive burst of ice at an opponent, spraying ice and snow around it. To use, simply tap sneak (Default: Shift) while targeting a block of ice to select it as a source. From there, you can just left click to send the blast off at your opponent.");
			config.addDefault("Abilities.Water.IceBlast.DeathMessage", "{victim} was shattered by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.IceSpike.Description", "This ability has many functions. Clicking while targetting ice, or an entity over some ice, " + "will raise a spike of ice up, damaging and slowing the target. Tapping sneak (shift) while" + " selecting a water source will select that source that can then be fired with a click. Firing" + " this will launch a spike of ice at your target, dealing a bit of damage and slowing the target. " + "If you sneak (shift) while not selecting a source, many ice spikes will erupt from around you, " + "damaging and slowing those targets.");
			config.addDefault("Abilities.Water.IceSpike.DeathMessage", "{victim} was impaled by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.OctopusForm.Description", "This ability allows the waterbender to manipulate a large quantity of water into a form resembling that of an octopus. " + "To use, click to select a water source. Then, hold sneak to channel this ability. " + "While channeling, the water will form itself around you and has a chance to block incoming attacks. " + "Additionally, you can click while channeling to attack things near you, dealing damage and knocking them back. " + "Releasing shift at any time will dissipate the form.");
			config.addDefault("Abilities.Water.OctopusForm.DeathMessage", "{victim} was slapped by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.PhaseChange.Description", "To use, simply left-click. " + "Any water you are looking at within range will instantly freeze over into solid ice. " + "Provided you stay within range of the ice and do not unbind FreezeMelt, " + "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. " + "If you sneak (default: shift), anything around where you are looking at will instantly melt. " + "Since this is a more favorable state for these things, they will never re-freeze unless they " + "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while " + "targetting water with FreezeMelt, it will evaporate water around that block that is above " + "sea level. ");
			config.addDefault("Abilities.Water.PlantArmor.Description", "PlantArmor is a defensive ability in the arsenal of the plantbender. Clicking on leaves with this ability will temporarily clad you in strong armor made out of plants! You can use this defensively, but you can also use the armor as a source for other plantbending skills.");
			config.addDefault("Abilities.Water.Surge.Description", "This ability has two distinct features. If you sneak to select a source block, you can then click in a direction and a large wave will be launched in that direction. If you sneak again while the wave is en route, the wave will freeze the next target it hits. If, instead, you click to select a source block, you can hold sneak to form a wall of water at your cursor location. Click to shift between a water wall and an ice wall. Release sneak to dissipate it.");
			config.addDefault("Abilities.Water.Torrent.Description", "Torrent is one of the strongest moves in a waterbender's arsenal. To use, first click a source block to select it; then hold shift to begin streaming the water around you. Water flowing around you this way will damage and knock back nearby enemies and projectiles. If you release shift during this, you will create a large wave that expands outwards from you, launching anything in its path back. Instead, if you click you release the water and channel it to flow towards your cursor. Anything caught in the blast will be tossed about violently and take damage. Finally, if you click again when the water is torrenting, it will freeze the area around it when it is obstructed.");
			config.addDefault("Abilities.Water.Torrent.DeathMessage", "{victim} was taken down by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Description", "One of the most diverse moves in a Waterbender's arsenal, this move creates tendrils " + "of water from the players arms to emulate their actual arms. Each water arms mode will be binded to a slot, switch slots to change mode. " + "To deactive the arms, hold Sneak and Double Left-Click." + "\nPull - Use your Arms to pull blocks, items, mobs or even players towards you!" + "\nPunch - An offensive attack, harming players or mobs!" + "\nGrapple - Scale walls and speed across battlefields, using your Arms as a grappling hook!" + "\nGrab - Grab an entity with your arm, and swing them about!" + "\nFreeze - Use your Arms to fire small blasts of ice in any direction!" + "\nSpear - Throw your Arms in any direction, freezing whatever it hits!");
			config.addDefault("Abilities.Water.WaterArms.SneakMessage", "Active Ability:");
			config.addDefault("Abilities.Water.WaterArms.Punch.Description", "{victim} was too slow for {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Freeze.Description", "{victim} was frozen by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Spear.Description", "{victim} was speared to death by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterBubble.Description", "To use, the bender must hold down sneak. All water around the user in a small bubble will vanish, replacing itself once the user either gets too far away or selects a different ability.");
			config.addDefault("Abilities.Water.WaterManipulation.Description", "To use, place your cursor over a waterbendable object and tap sneak (default: shift). Smoke will appear where you've selected, indicating the origin of your ability. After you have selected an origin, simply left-click in any direction and you will see your water spout off in that direction, slicing any creature in its path. If you look towards a creature when you use this ability, it will target that creature. A collision from Water Manipulation both knocks the target back and deals some damage. Alternatively, if you have the source selected and tap shift again, you will be able to control the water more directly.");
			config.addDefault("Abilities.Water.WaterManipulation.DeathMessage", "{victim} was taken down by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterSpout.Description", "This ability provides a Waterbender with a means of transportation. To use, simply left click while in or over water to spout water up beneath you, experiencing controlled levitation. Left clicking again while the spout is active will cause it to disappear. Alternatively, tapping a Waterbendable block while not in Water will select a water block as a source, from there, you can tap sneak (Default:Shift) to channel the Water around you. Releasing the sneak will create a wave allowing you a quick burst of controlled transportation. While riding the wave you may press sneak to cause the wave to disappear.");
			config.addDefault("Abilities.Water.Combo.IceBullet.Description", "Using a large cavern of ice, you can punch ice shards at your opponent causing moderate damage. To rapid fire, you must alternate between Left clicking and right clicking with IceBlast.");
			config.addDefault("Abilities.Water.Combo.IceBullet.DeathMessage", "{victim}'s heart was frozen by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.Combo.IceWave.Description", "PhaseChange your WaterWave into an IceWave that freezes and damages enemies.");
			config.addDefault("Abilities.Water.Combo.IceWave.DeathMessage", "{victim} was frozen solid by {attacker}'s {ability}");
			
			config.addDefault("Commands.Help.Elements.Earth", "Earth is the element of substance. Earthbenders share many of the same fundamental techniques as Waterbenders, but their domain is quite different and more readily accessible. Earthbenders dominate the ground and subterranean, having abilities to pull columns of rock straight up from the earth or drill their way through the mountain. They can also launch themselves through the air using pillars of rock, and will not hurt themselves assuming they land on something they can bend. The more skilled Earthbenders can even bend metal.");
			config.addDefault("Abilities.Earth.Catapult.Description", "To use, left-click while looking in the direction you want to be launched. " + "A pillar of earth will jut up from under you and launch you in that direction - " + "if and only if there is enough earth behind where you're looking to launch you. " + "Skillful use of this ability takes much time and work, and it does result in the " + "death of certain gung-ho earthbenders. If you plan to use this ability, be sure " + "you've read about your passive ability you innately have as an earthbender.");
			config.addDefault("Abilities.Earth.Collapse.Description", " To use, simply left-click on an earthbendable block. " + "That block and the earthbendable blocks above it will be shoved " + "back into the earth below them, if they can. " + "This ability does have the capacity to trap something inside of it, " + "although it is incredibly difficult to do so. " + "Additionally, press sneak with this ability to affect an area around your targetted location - " + "all earth that can be moved downwards will be moved downwards. " + "This ability is especially risky or deadly in caves, depending on the " + "earthbender's goal and technique.");
			config.addDefault("Abilities.Earth.Collapse.DeathMessage", "{victim} was suffocated by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthArmor.Description", "This ability encases the earthbender in temporary armor. To use, click on a block that is earthbendable. If there is another block under it that is earthbendable, the block will fly to you and grant you temporary armor and damage reduction. This ability has a long cooldown.");
			config.addDefault("Abilities.Earth.EarthBlast.Description", "To use, place your cursor over an earthbendable object (dirt, rock, ores, etc) " + "and tap sneak (default: shift). The object will temporarily turn to stone, " + "indicating that you have it focused as the source for your ability. " + "After you have selected an origin (you no longer need to be sneaking), " + "simply left-click in any direction and you will see your object launch " + "off in that direction, smashing into any creature in its path. If you look " + "towards a creature when you use this ability, it will target that creature. " + "A collision from Earth Blast both knocks the target back and deals some damage. " + "You cannot have multiple of these abilities flying at the same time.");
			config.addDefault("Abilities.Earth.EarthBlast.DeathMessage", "{victim} was broken apart by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthGrab.Description", "To use, simply left-click while targeting a creature within range. " + "This ability will erect a circle of earth to trap the creature in.");
			config.addDefault("Abilities.Earth.EarthTunnel.Description", "Earth Tunnel is a completely utility ability for earthbenders. To use, simply sneak (default: shift) in the direction you want to tunnel. You will slowly begin tunneling in the direction you're facing for as long as you sneak or if the tunnel has been dug long enough. This ability will be interrupted if it hits a block that cannot be earthbent.");
			config.addDefault("Abilities.Earth.Extraction.Description", "This ability allows metalbenders to extract the minerals from ore blocks. To use, simply tap sneak while looking at an ore block with metal in it (iron, gold, quartz) and the ore will be extracted and drop in front of you. This ability has a small chance of doubling or tripling the loot. This ability has a short cooldown.");
			config.addDefault("Abilities.Earth.LavaFlow.Description", "This ability allows an Earthbender to create lava using the Earth around them. To use, simply hold sneak (Default: Shift) to create a lava moat that surrounds you, press sneak again to remove the moat. Left click an Earthbendable block to create a pool of lava after a small delay. Additionally, you can left click at any time to turn lava back into its original state -- Earth.");
			config.addDefault("Abilities.Earth.LavaFlow.DeathMessage", "{victim} was caught in by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthSmash.Description", "To raise an EarthSmash hold sneak (default: shift) for approximately 1.5 seconds, " + "then release while aiming at dirt. To grab the EarthSmash aim at the center and hold sneak, " + "the EarthSmash will follow your mouse. You can shoot the EarthSmash by grabbing onto it and left clicking. " + "To ride the EarthSmash simply hop ontop of it and hold sneak while aiming in the direction that you wish to go. " + "Another way to ride an EarthSmash is to grab it with sneak and then right click it. " + "Use EarthSmash as a defensive shield, a powerful attack, or an advanced means of transportation.");
			config.addDefault("Abilities.Earth.EarthSmash.DeathMessage", "{victim} was crushed by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.MetalClips.Description", "MetalClips has the potential to be both an offensive and a utility ability. To start, you must carry smelted Iron Ingots in your inventory. To apply the clips onto an entity, simply click at them. If the entity is a Zombie, a Skeleton, or a Player, the clips will form armor around the entity, giving you some control over them. Each additional clip will give you more control. If you have permission to do so, you may crush the entity with a 4th clip by clicking, hurting them. Without explicit permissions, you will only be able to strap three clips on your target. If the entity is not one of the above, the clip will simply do damage and fall to the ground, to be collected. Another permission requiring action is throwing entities, which also needs to be configured to work. To do so, release sneak while controlling the entity. Throwing entities has varying degrees of power based on how many clips they have on them.");
			config.addDefault("Abilities.Earth.MetalClips.DeathMessage", "{victim} was too slow for {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.RaiseEarth.Description", "To use, simply left-click on an earthbendable block. " + "A column of earth will shoot upwards from that location. " + "Anything in the way of the column will be brought up with it, " + "leaving talented benders the ability to trap brainless entities up there. " + "Additionally, simply sneak (default shift) looking at an earthbendable block. " + "A wall of earth will shoot upwards from that location. " + "Anything in the way of the wall will be brought up with it. ");
			config.addDefault("Abilities.Earth.Shockwave.Description", "This is one of the most powerful moves in the earthbender's arsenal. " + "To use, you must first charge it by holding sneak (default: shift). " + "Once charged, you can release sneak to create an enormous shockwave of earth, " + "disturbing all earth around you and expanding radially outwards. " + "Anything caught in the shockwave will be blasted back and dealt damage. " + "If you instead click while charged, the disruption is focused in a cone in front of you. " + "Lastly, if you fall from a great enough height with this ability selected, you will automatically create a shockwave.");
			config.addDefault("Abilities.Earth.Shockwave.DeathMessage", "{victim} was blown away by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.SandSpout.Description", "SandSpout is a core move for travelling, evasion, and mobility for sandbenders. To use, simply left click while over sand or sandstone, and a column of sand will form at your feet, enabling you to levitate. Any mobs or players that touch your column will receive damage and be blinded. Beware, as the spout will stop working when no longer over sand!");
			config.addDefault("Abilities.Earth.Tremorsense.Description", "This is a pure utility ability for earthbenders. If you are in an area of low-light and are standing on top of an earthbendable block, this ability will automatically turn that block into glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out as you have lost contact with the earth and cannot 'see' until you can touch earth again. Additionally, if you click with this ability selected, smoke will appear above nearby earth with pockets of air beneath them.");
			
			config.addDefault("Commands.Help.Elements.Fire", "Fire is the element of power. Firebenders focus on destruction and incineration. Their abilities are pretty straight forward: set things on fire. They do have a bit of utility however, being able to make themselves un-ignitable, extinguish large areas, cook food in their hands, extinguish large areas, small bursts of flight, and then comes the abilities to shoot fire from your hands.\nFirebenders can chain their abilities into combos, type /b help FireCombos for more information.");
			config.addDefault("Abilities.Fire.Blaze.Description", "To use, simply left-click in any direction. An arc of fire will flow from your location, igniting anything in its path. Additionally, tap sneak to engulf the area around you in roaring flames.");
			config.addDefault("Abilities.Fire.Blaze.DeathMessage", "{victim} was burned alive by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.Combustion.Description", "Combustion is a powerful ability only known by a few skilled Firebenders. It allows the bender to Firebend with their mind, concentrating energy to create a powerful blast. To use, simply tap sneak (Default: Shift) to launch the blast. This technique is highly destructive and very effective, it also comes with a long cooldown.");
			config.addDefault("Abilities.Fire.Combustion.DeathMessage", "{victim} was shot down by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireBlast.Description", "FireBlast is the most fundamental bending technique of a firebender. " + "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. " + "If this blast contacts an enemy, it will dissipate and engulf them in flames, " + "doing additional damage and knocking them back slightly. " + "If the blast hits terrain, it will ignite the nearby area. " + "Additionally, if you hold sneak, you will charge up the fireblast. " + "If you release it when it's charged, it will instead launch a powerful " + "fireball that explodes on contact.");
			config.addDefault("Abilities.Fire.FireBlast.DeathMessage", "{victim} was burnt by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireBurst.Description", "FireBurst is a very powerful firebending ability. " + "To use, press and hold sneak to charge your burst. " + "Once charged, you can either release sneak to release the burst in a sphere around you or " + "click to launch a cone-shaped burst of flames in front of you.");
			config.addDefault("Abilities.Fire.FireBurst.DeathMessage", "{victim} was blown apart by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireJet.Description", "This ability is used for a limited burst of flight for firebenders. Clicking with this " + "ability selected will launch you in the direction you're looking, granting you " + "controlled flight for a short time. This ability can be used mid-air to prevent falling " + "to your death, but on the ground it can only be used if standing on a block that's " + "ignitable (e.g. not snow or water).");
			config.addDefault("Abilities.Fire.FireShield.Description", "FireShield is a basic defensive ability. " + "Clicking with this ability selected will create a " + "small disc of fire in front of you, which will block most " + "attacks and bending. Alternatively, pressing and holding " + "sneak creates a very small shield of fire, blocking most attacks. " + "Creatures that contact this fire are ignited.");
			config.addDefault("Abilities.Fire.FireShield.DeathMessage", "{victim} scorched theirself on {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.HeatControl.Description", "While this ability is selected, the firebender becomes impervious " + "to fire damage and cannot be ignited. " + "If the user left-clicks with this ability, the targeted area will be " + "extinguished, although it will leave any creature burning engulfed in flames. " + "This ability can also cool lava. If this ability is used while targetting ice or snow, it" + " will instead melt blocks in that area. Finally, sneaking with this ability will cook any food in your hand.");
			config.addDefault("Abilities.Fire.Illumination.Description", "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking " + "will create a torch that follows you around. The torch will only appear on objects that are " + "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, " + "it will disappear, but will reappear when you get on another ignitable block. Clicking again " + "dismisses this torch.");
			config.addDefault("Abilities.Fire.Lightning.Description", "Hold sneak while selecting this ability to charge up a lightning strike. Once charged, release sneak to discharge the lightning to the targeted location.");
			config.addDefault("Abilities.Fire.Lightning.DeathMessage", "{victim} was electrocuted by {attacker}'s {ability}");	
			config.addDefault("Abilities.Fire.WallOfFire.Description", "To use this ability, click at a location. A wall of fire will appear at this location, igniting enemies caught in it and blocking projectiles.");
			config.addDefault("Abilities.Fire.WallOfFire.DeathMessage", "{victim} ran into {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.Combo.FireKick.Description", "A short ranged arc of fire launches from the player's feet dealing moderate damage to enemies.");
			config.addDefault("Abilities.Fire.Combo.FireKick.DeathMessage", "{victim} was kicked to the floor with flames by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.Combo.FireSpin.Description", "A circular array of fire that causes damage and massive knockback to nearby enemies.");
			config.addDefault("Abilities.Fire.Combo.FireSpin.DeathMessage", "{victim} was caught in {attacker}'s {ability} inferno");
			config.addDefault("Abilities.Fire.Combo.JetBlaze.Description", "Damages and burns all enemies in the proximity of your FireJet.");
			config.addDefault("Abilities.Fire.Combo.JetBlaze.DeathMessage", "{victim} was blasted away by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.Combo.JetBlast.Description", "Create an explosive blast that propels your FireJet at higher speeds.");
			config.addDefault("Abilities.Fire.Combo.FireWheel.Description", "A high-speed wheel of fire that travels along the ground for long distances dealing high damage.");
			config.addDefault("Abilities.Fire.Combo.FireWheel.DeathMessage", "{victim} was incinerated by {attacker}'s {ability}");
			
			config.addDefault("Commands.Help.Elements.Chi", "Chiblockers focus on bare handed combat, utilizing their agility and speed to stop any bender right in their path. Although they lack the ability to bend any of the other elements, they are great in combat, and a serious threat to any bender. Chiblocking was first shown to be used by Ty Lee in Avatar: The Last Airbender, then later by members of the Equalists in The Legend of Korra.\nChiblockers can chain their abilities into combos, type /b help ChiCombos for more information.");
			config.addDefault("Abilities.Chi.AcrobatStance.Description", "AcrobatStance gives a Chiblocker a higher probability of blocking a Bender's Chi while granting them a Speed and Jump Boost. It also increases the rate at which the hunger bar depletes. To use, simply left click. Left clicking again will de-activate the stance.");
			config.addDefault("Abilities.Chi.HighJump.Description", "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.");
			config.addDefault("Abilities.Chi.Paralyze.Description", "Paralyzes the target, making them unable to do anything for a short " + "period of time. This ability has a long cooldown.");
			config.addDefault("Abilities.Chi.RapidPunch.Description", "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch. This has a short cooldown.");
			config.addDefault("Abilities.Chi.RapidPunch.DeathMessage", "{victim} took all the hits against {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.Smokescreen.Description", "Smokescreen, if used correctly, can serve as a defensive and offensive ability for Chiblockers. To use, simply left click and you will toss out a Smoke Bomb. When the bomb hits the ground, it will explode and give all players within a small radius of the explosion temporary blindness, allowing you to either get away, or move in for the kill. This ability has a long cooldown.");
			config.addDefault("Abilities.Chi.WarriorStance.Description", "WarriorStance gives a Chiblocker increased damage but makes them a tad more vulnerable. To activate, simply left click.");
			config.addDefault("Abilities.Chi.QuickStrike.Description", "QuickStrike enables a chiblocker to quickly strike an enemy, potentially blocking their chi.");
			config.addDefault("Abilities.Chi.QuickStrike.DeathMessage", "{victim} was struck down by {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.SwiftKick.Description", "SwiftKick allows a chiblocker to swiftly kick an enemy, potentially blocking their chi. The chiblocker must be in the air to use this ability.");
			config.addDefault("Abilities.Chi.SwiftKick.DeathMessage", "{victim} was kicked to the floor by {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.Combo.Immobilize.Description", "Immobilizes the opponent for several seconds.");
			
			languageConfig.save();
		} else if (type == ConfigType.DEFAULT) {
			config = defaultConfig.get();

			ArrayList<String> earthBlocks = new ArrayList<String>();
			earthBlocks.add("DIRT");
			earthBlocks.add("MYCEL");
			earthBlocks.add("GRASS");
			earthBlocks.add("STONE");
			earthBlocks.add("GRAVEL");
			earthBlocks.add("CLAY");
			earthBlocks.add("COAL_ORE");
			earthBlocks.add("IRON_ORE");
			earthBlocks.add("GOLD_ORE");
			earthBlocks.add("REDSTONE_ORE");
			earthBlocks.add("LAPIS_ORE");
			earthBlocks.add("DIAMOND_ORE");
			earthBlocks.add("NETHERRACK");
			earthBlocks.add("QUARTZ_ORE");
			earthBlocks.add("COBBLESTONE");
			earthBlocks.add("STEP");

			ArrayList<String> metalBlocks = new ArrayList<String>();
			metalBlocks.add("IRON_BLOCK");
			metalBlocks.add("GOLD_BLOCK");
			metalBlocks.add("QUARTZ_BLOCK");
			
			ArrayList<String> sandBlocks = new ArrayList<String>();
			sandBlocks.add("SAND");
			sandBlocks.add("SANDSTONE");
			sandBlocks.add("RED_SAND");
			sandBlocks.add("RED_SANDSTONE");

			ArrayList<String> iceBlocks = new ArrayList<String>();
			iceBlocks.add("ICE");
			iceBlocks.add("PACKED_ICE");
			
			ArrayList<String> plantBlocks = new ArrayList<String>();
			plantBlocks.add("SAPLING");
			plantBlocks.add("LEAVES");
			plantBlocks.add("LEAVES_2");
			plantBlocks.add("DEAD_BUSH");
			plantBlocks.add("YELLOW_FLOWER");
			plantBlocks.add("RED_ROSE");
			plantBlocks.add("RED_MUSHROOM");
			plantBlocks.add("BROWN_MUSHROOM");
			plantBlocks.add("CACTUS");
			plantBlocks.add("PUMPKIN");
			plantBlocks.add("HUGE_MUSHROOM_1");
			plantBlocks.add("HUGE_MUSHROOM_2");
			plantBlocks.add("MELON_BLOCK");
			plantBlocks.add("VINE");
			plantBlocks.add("WATER_LILY");
			plantBlocks.add("DOUBLE_PLANT");
			plantBlocks.add("CROPS");
			plantBlocks.add("LONG_GRASS");
			plantBlocks.add("SUGAR_CANE_BLOCK");
			plantBlocks.add("PUMPKIN_STEM");
			plantBlocks.add("MELON_STEM");
			
			ArrayList<String> snowBlocks = new ArrayList<>();
			snowBlocks.add("SNOW");

			config.addDefault("Properties.BendingPreview", true);
			config.addDefault("Properties.ImportEnabled", true);
			config.addDefault("Properties.BendingAffectFallingSand.Normal", true);
			config.addDefault("Properties.BendingAffectFallingSand.NormalStrengthMultiplier", 1.0);
			config.addDefault("Properties.BendingAffectFallingSand.TNT", true);
			config.addDefault("Properties.BendingAffectFallingSand.TNTStrengthMultiplier", 1.0);
			config.addDefault("Properties.GlobalCooldown", 500);
			config.addDefault("Properties.TogglePassivesWithAllBending", true);
			config.addDefault("Properties.SeaLevel", 62);

			config.addDefault("Properties.HorizontalCollisionPhysics.Enabled", true);
			config.addDefault("Properties.HorizontalCollisionPhysics.DamageOnBarrierBlock", false);
			config.addDefault("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance", 5.0);
			config.addDefault("Properties.HorizontalCollisionPhysics.WallDamageCap", 5.0);

			config.addDefault("Properties.RegionProtection.AllowHarmlessAbilities", true);
			config.addDefault("Properties.RegionProtection.RespectWorldGuard", true);
			config.addDefault("Properties.RegionProtection.RespectGriefPrevention", true);
			config.addDefault("Properties.RegionProtection.RespectFactions", true);
			config.addDefault("Properties.RegionProtection.RespectTowny", true);
			config.addDefault("Properties.RegionProtection.RespectPreciousStones", true);
			config.addDefault("Properties.RegionProtection.RespectLWC", true);
			config.addDefault("Properties.RegionProtection.Residence.Flag", "bending");
			config.addDefault("Properties.RegionProtection.Residence.Respect", true);
			config.addDefault("Properties.RegionProtection.CacheBlockTime", 5000);

			config.addDefault("Properties.Air.CanBendWithWeapons", false);
			config.addDefault("Properties.Air.Particles", "spell");
			config.addDefault("Properties.Air.PlaySound", true);

			config.addDefault("Properties.Water.CanBendWithWeapons", true);
			config.addDefault("Properties.Water.IceBlocks", iceBlocks);
			config.addDefault("Properties.Water.PlantBlocks", plantBlocks);
			config.addDefault("Properties.Water.SnowBlocks", snowBlocks);
			config.addDefault("Properties.Water.NightFactor", 1.5);
			config.addDefault("Properties.Water.FullMoonFactor", 1.75);
			config.addDefault("Properties.Water.PlaySound", true);

			config.addDefault("Properties.Earth.RevertEarthbending", true);
			config.addDefault("Properties.Earth.SafeRevert", true);
			config.addDefault("Properties.Earth.RevertCheckTime", 300000);
			config.addDefault("Properties.Earth.CanBendWithWeapons", true);
			config.addDefault("Properties.Earth.EarthBlocks", earthBlocks);
			config.addDefault("Properties.Earth.MetalBlocks", metalBlocks);
			config.addDefault("Properties.Earth.SandBlocks", sandBlocks);
			config.addDefault("Properties.Earth.MetalPowerFactor", 1.5);
			config.addDefault("Properties.Earth.PlaySound", true);

			config.addDefault("Properties.Fire.CanBendWithWeapons", true);
			config.addDefault("Properties.Fire.DayFactor", 1.25);
			config.addDefault("Properties.Fire.PlaySound", true);
			config.addDefault("Properties.Fire.FireGriefing", false);
			config.addDefault("Properties.Fire.RevertTicks", 12000L);

			config.addDefault("Properties.Chi.CanBendWithWeapons", true);

			ArrayList<String> disabledWorlds = new ArrayList<String>();
			disabledWorlds.add("TestWorld");
			disabledWorlds.add("TestWorld2");
			config.addDefault("Properties.DisabledWorlds", disabledWorlds);

			config.addDefault("Abilities.Avatar.AvatarState.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.Cooldown", 7200000);
			config.addDefault("Abilities.Avatar.AvatarState.Duration", 480000);
			config.addDefault("Abilities.Avatar.AvatarState.PowerMultiplier", 3.5);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Speed.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Speed.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Power", 3);

			config.addDefault("Abilities.Air.Passive.Factor", 0.3);
			config.addDefault("Abilities.Air.Passive.Speed", 2);
			config.addDefault("Abilities.Air.Passive.Jump", 3);

			config.addDefault("Abilities.Air.AirBlast.Enabled", true);
			config.addDefault("Abilities.Air.AirBlast.Speed", 25);
			config.addDefault("Abilities.Air.AirBlast.Range", 20);
			config.addDefault("Abilities.Air.AirBlast.Radius", 2);
			config.addDefault("Abilities.Air.AirBlast.SelectRange", 10);
			config.addDefault("Abilities.Air.AirBlast.SelectParticles", 4);
			config.addDefault("Abilities.Air.AirBlast.Particles", 6);
			config.addDefault("Abilities.Air.AirBlast.Cooldown", 500);
			config.addDefault("Abilities.Air.AirBlast.Push.Self", 2.5);
			config.addDefault("Abilities.Air.AirBlast.Push.Entities", 3.5);
			config.addDefault("Abilities.Air.AirBlast.CanFlickLevers", true);
			config.addDefault("Abilities.Air.AirBlast.CanOpenDoors", true);
			config.addDefault("Abilities.Air.AirBlast.CanPressButtons", true);
			config.addDefault("Abilities.Air.AirBlast.CanCoolLava", true);

			config.addDefault("Abilities.Air.AirBubble.Enabled", true);
			config.addDefault("Abilities.Air.AirBubble.Radius", 7);

			config.addDefault("Abilities.Air.AirBurst.Enabled", true);
			config.addDefault("Abilities.Air.AirBurst.FallThreshold", 10);
			config.addDefault("Abilities.Air.AirBurst.PushFactor", 1.5);
			config.addDefault("Abilities.Air.AirBurst.ChargeTime", 1750);
			config.addDefault("Abilities.Air.AirBurst.Damage", 0);
			config.addDefault("Abilities.Air.AirBurst.SneakParticles", 10);
			config.addDefault("Abilities.Air.AirBurst.ParticlePercentage", 50);
			config.addDefault("Abilities.Air.AirBurst.AnglePhi", 10);
			config.addDefault("Abilities.Air.AirBurst.AngleTheta", 10);

			config.addDefault("Abilities.Air.AirScooter.Enabled", true);
			config.addDefault("Abilities.Air.AirScooter.Speed", 0.675);
			config.addDefault("Abilities.Air.AirScooter.Interval", 100);
			config.addDefault("Abilities.Air.AirScooter.Radius", 1);
			config.addDefault("Abilities.Air.AirScooter.Cooldown", 7000);
			config.addDefault("Abilities.Air.AirScooter.MaxHeightFromGround", 7);

			config.addDefault("Abilities.Air.AirShield.Enabled", true);
			config.addDefault("Abilities.Air.AirShield.Radius", 7);
			config.addDefault("Abilities.Air.AirShield.Streams", 5);
			config.addDefault("Abilities.Air.AirShield.Speed", 10);
			config.addDefault("Abilities.Air.AirShield.Particles", 5);
			config.addDefault("Abilities.Air.AirShield.IsAvatarStateToggle", true);

			config.addDefault("Abilities.Air.AirSpout.Enabled", true);
			config.addDefault("Abilities.Air.AirSpout.Height", 16);
			config.addDefault("Abilities.Air.AirSpout.Interval", 100);

			config.addDefault("Abilities.Air.AirSuction.Enabled", true);
			config.addDefault("Abilities.Air.AirSuction.Speed", 25);
			config.addDefault("Abilities.Air.AirSuction.Range", 20);
			config.addDefault("Abilities.Air.AirSuction.SelectRange", 10);
			config.addDefault("Abilities.Air.AirSuction.Radius", 2);
			config.addDefault("Abilities.Air.AirSuction.Push", 2.5);
			config.addDefault("Abilities.Air.AirSuction.Cooldown", 500);
			config.addDefault("Abilities.Air.AirSuction.Particles", 6);
			config.addDefault("Abilities.Air.AirSuction.SelectParticles", 6);

			config.addDefault("Abilities.Air.AirSwipe.Enabled", true);
			config.addDefault("Abilities.Air.AirSwipe.Damage", 2);
			config.addDefault("Abilities.Air.AirSwipe.Range", 14);
			config.addDefault("Abilities.Air.AirSwipe.Radius", 2);
			config.addDefault("Abilities.Air.AirSwipe.Push", 0.5);
			config.addDefault("Abilities.Air.AirSwipe.Arc", 16);
			config.addDefault("Abilities.Air.AirSwipe.Speed", 25);
			config.addDefault("Abilities.Air.AirSwipe.Cooldown", 1500);
			config.addDefault("Abilities.Air.AirSwipe.ChargeFactor", 3);
			config.addDefault("Abilities.Air.AirSwipe.MaxChargeTime", 2500);
			config.addDefault("Abilities.Air.AirSwipe.Particles", 3);
			config.addDefault("Abilities.Air.AirSwipe.StepSize", 4);

			config.addDefault("Abilities.Air.Flight.Enabled", true);
			config.addDefault("Abilities.Air.Flight.HoverEnabled", true);
			config.addDefault("Abilities.Air.Flight.Speed", 1);
			config.addDefault("Abilities.Air.Flight.MaxHits", 4);

			config.addDefault("Abilities.Air.Suffocate.Enabled", true);
			config.addDefault("Abilities.Air.Suffocate.ChargeTime", 500);
			config.addDefault("Abilities.Air.Suffocate.Cooldown", 0);
			config.addDefault("Abilities.Air.Suffocate.Range", 20);
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
			config.addDefault("Abilities.Air.Suffocate.AnimationParticleAmount", 1);
			config.addDefault("Abilities.Air.Suffocate.AnimationSpeed", 1.0);
			
			config.addDefault("Abilities.Air.Tornado.Enabled", true);
			config.addDefault("Abilities.Air.Tornado.Radius", 10);
			config.addDefault("Abilities.Air.Tornado.Height", 20);
			config.addDefault("Abilities.Air.Tornado.Range", 25);
			config.addDefault("Abilities.Air.Tornado.Speed", 1);
			config.addDefault("Abilities.Air.Tornado.NpcPushFactor", 1);
			config.addDefault("Abilities.Air.Tornado.PlayerPushFactor", 1);

			config.addDefault("Abilities.Air.AirCombo.Twister.Enabled", true);
			config.addDefault("Abilities.Air.AirCombo.Twister.Speed", 0.35);
			config.addDefault("Abilities.Air.AirCombo.Twister.Range", 16);
			config.addDefault("Abilities.Air.AirCombo.Twister.Height", 8);
			config.addDefault("Abilities.Air.AirCombo.Twister.Radius", 3.5);
			config.addDefault("Abilities.Air.AirCombo.Twister.RemoveDelay", 1500);
			config.addDefault("Abilities.Air.AirCombo.Twister.Cooldown", 10000);
			config.addDefault("Abilities.Air.AirCombo.Twister.DegreesPerParticle", 7);
			config.addDefault("Abilities.Air.AirCombo.Twister.HeightPerParticle", 1.25);
			config.addDefault("Abilities.Air.AirCombo.AirStream.Enabled", true);
			config.addDefault("Abilities.Air.AirCombo.AirStream.Speed", 0.5);
			config.addDefault("Abilities.Air.AirCombo.AirStream.Range", 40);
			config.addDefault("Abilities.Air.AirCombo.AirStream.EntityDuration", 4000);
			config.addDefault("Abilities.Air.AirCombo.AirStream.EntityHeight", 14);
			config.addDefault("Abilities.Air.AirCombo.AirStream.Cooldown", 7000);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Enabled", true);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Speed", 1.4);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Range", 14);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Damage", 3);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Knockback", 3.5);
			config.addDefault("Abilities.Air.AirCombo.AirSweep.Cooldown", 6000);

			config.addDefault("Abilities.Water.Passive.SwimSpeedFactor", 0.7);

			config.addDefault("Abilities.Water.Bloodbending.Enabled", true);
			config.addDefault("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight", true);
			config.addDefault("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs", true);
			config.addDefault("Abilities.Water.Bloodbending.ThrowFactor", 2);
			config.addDefault("Abilities.Water.Bloodbending.Range", 10);
			config.addDefault("Abilities.Water.Bloodbending.HoldTime", 0);
			config.addDefault("Abilities.Water.Bloodbending.Cooldown", 3000);
			config.addDefault("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon", true);
			config.addDefault("Abilities.Water.Bloodbending.CanBloodbendOtherBloodbenders", false);
			
			config.addDefault("Abilities.Water.HealingWaters.Enabled", true);
			config.addDefault("Abilities.Water.HealingWaters.ShiftRequired", true);
			config.addDefault("Abilities.Water.HealingWaters.Radius", 5);
			config.addDefault("Abilities.Water.HealingWaters.Interval", 750);
			config.addDefault("Abilities.Water.HealingWaters.Power", 1);
			config.addDefault("Abilities.Water.HealingWaters.Duration", 70);

			config.addDefault("Abilities.Water.IceBlast.Enabled", true);
			config.addDefault("Abilities.Water.IceBlast.Damage", 3);
			config.addDefault("Abilities.Water.IceBlast.Range", 20);
			config.addDefault("Abilities.Water.IceBlast.DeflectRange", 3);
			config.addDefault("Abilities.Water.IceBlast.CollisionRadius", 2);
			config.addDefault("Abilities.Water.IceBlast.Interval", 20);
			config.addDefault("Abilities.Water.IceBlast.Cooldown", 1500);

			config.addDefault("Abilities.Water.IceSpike.Enabled", true);
			config.addDefault("Abilities.Water.IceSpike.Cooldown", 2000);
			config.addDefault("Abilities.Water.IceSpike.Damage", 2);
			config.addDefault("Abilities.Water.IceSpike.Range", 20);
			config.addDefault("Abilities.Water.IceSpike.Push", 0.7);
			config.addDefault("Abilities.Water.IceSpike.Height", 6);
			config.addDefault("Abilities.Water.IceSpike.Speed", 25);
			config.addDefault("Abilities.Water.IceSpike.SlowCooldown", 5000);
			config.addDefault("Abilities.Water.IceSpike.SlowPower", 2);
			config.addDefault("Abilities.Water.IceSpike.SlowDuration", 70);
			config.addDefault("Abilities.Water.IceSpike.Field.Damage", 2);
			config.addDefault("Abilities.Water.IceSpike.Field.Radius", 6);
			config.addDefault("Abilities.Water.IceSpike.Field.Push", 1);
			config.addDefault("Abilities.Water.IceSpike.Field.Cooldown", 2000);
			config.addDefault("Abilities.Water.IceSpike.Blast.Range", 20);
			config.addDefault("Abilities.Water.IceSpike.Blast.Damage", 1);
			config.addDefault("Abilities.Water.IceSpike.Blast.CollisionRadius", 2);
			config.addDefault("Abilities.Water.IceSpike.Blast.DeflectRange", 3);
			config.addDefault("Abilities.Water.IceSpike.Blast.Cooldown", 500);
			config.addDefault("Abilities.Water.IceSpike.Blast.SlowCooldown", 5000);
			config.addDefault("Abilities.Water.IceSpike.Blast.SlowPower", 2);
			config.addDefault("Abilities.Water.IceSpike.Blast.SlowDuration", 70);
			config.addDefault("Abilities.Water.IceSpike.Blast.Interval", 20);

			config.addDefault("Abilities.Water.OctopusForm.Enabled", true);
			config.addDefault("Abilities.Water.OctopusForm.Range", 10);
			config.addDefault("Abilities.Water.OctopusForm.AttackRange", 2.5);
			config.addDefault("Abilities.Water.OctopusForm.Radius", 3);
			config.addDefault("Abilities.Water.OctopusForm.Damage", 4);
			config.addDefault("Abilities.Water.OctopusForm.Knockback", 1.75);
			config.addDefault("Abilities.Water.OctopusForm.FormDelay", 40);
			config.addDefault("Abilities.Water.OctopusForm.Cooldown", 0);
			config.addDefault("Abilities.Water.OctopusForm.AngleIncrement", 45);

			config.addDefault("Abilities.Water.PhaseChange.Enabled", true);
			config.addDefault("Abilities.Water.PhaseChange.Range", 16);
			config.addDefault("Abilities.Water.PhaseChange.Radius", 4);
			config.addDefault("Abilities.Water.PhaseChange.Freeze.Cooldown", 0);
			config.addDefault("Abilities.Water.PhaseChange.Melt.Cooldown", 0);

			config.addDefault("Abilities.Water.PlantArmor.Enabled", true);
			config.addDefault("Abilities.Water.PlantArmor.Duration", 7500);
			config.addDefault("Abilities.Water.PlantArmor.Resistance", 1);
			config.addDefault("Abilities.Water.PlantArmor.Cooldown", 15000);
			config.addDefault("Abilities.Water.PlantArmor.Range", 10);

			config.addDefault("Abilities.Water.Surge.Enabled", true);
			config.addDefault("Abilities.Water.Surge.Wave.Radius", 3);
			config.addDefault("Abilities.Water.Surge.Wave.Range", 20);
			config.addDefault("Abilities.Water.Surge.Wave.SelectRange", 12);
			config.addDefault("Abilities.Water.Surge.Wave.HorizontalPush", 1);
			config.addDefault("Abilities.Water.Surge.Wave.VerticalPush", 0.2);
			config.addDefault("Abilities.Water.Surge.Wave.MaxFreezeRadius", 7);
			config.addDefault("Abilities.Water.Surge.Wave.Cooldown", 500);
			config.addDefault("Abilities.Water.Surge.Wave.Interval", 30);
			config.addDefault("Abilities.Water.Surge.Wall.Range", 5);
			config.addDefault("Abilities.Water.Surge.Wall.Radius", 2);
			config.addDefault("Abilities.Water.Surge.Wall.Cooldown", 0);
			config.addDefault("Abilities.Water.Surge.Wall.Interval", 30);

			config.addDefault("Abilities.Water.Torrent.Enabled", true);
			config.addDefault("Abilities.Water.Torrent.Range", 25);
			config.addDefault("Abilities.Water.Torrent.SelectRange", 16);
			config.addDefault("Abilities.Water.Torrent.InitialDamage", 3);
			config.addDefault("Abilities.Water.Torrent.DeflectDamage", 1);
			config.addDefault("Abilities.Water.Torrent.SuccessiveDamage", 1);
			config.addDefault("Abilities.Water.Torrent.MaxLayer", 3);
			config.addDefault("Abilities.Water.Torrent.MaxHits", 2);
			config.addDefault("Abilities.Water.Torrent.Push", 1);
			config.addDefault("Abilities.Water.Torrent.Angle", 20);
			config.addDefault("Abilities.Water.Torrent.Radius", 3);
			config.addDefault("Abilities.Water.Torrent.MaxUpwardForce", 0.2);
			config.addDefault("Abilities.Water.Torrent.Interval", 30);
			config.addDefault("Abilities.Water.Torrent.Cooldown", 0);
			config.addDefault("Abilities.Water.Torrent.Wave.Radius", 12);
			config.addDefault("Abilities.Water.Torrent.Wave.Knockback", 1.5);
			config.addDefault("Abilities.Water.Torrent.Wave.Height", 1);
			config.addDefault("Abilities.Water.Torrent.Wave.GrowSpeed", 0.5);
			config.addDefault("Abilities.Water.Torrent.Wave.Interval", 30);
			config.addDefault("Abilities.Water.Torrent.Wave.Cooldown", 0);

			config.addDefault("Abilities.Water.Plantbending.RegrowTime", 180000);

			config.addDefault("Abilities.Water.WaterArms.Enabled", true);
			
			config.addDefault("Abilities.Water.WaterArms.Arms.InitialLength", 4);
			config.addDefault("Abilities.Water.WaterArms.Arms.SourceGrabRange", 12);
			config.addDefault("Abilities.Water.WaterArms.Arms.MaxAttacks", 10);
			config.addDefault("Abilities.Water.WaterArms.Arms.MaxAlternateUsage", 50);
			config.addDefault("Abilities.Water.WaterArms.Arms.MaxIceShots", 8);
			config.addDefault("Abilities.Water.WaterArms.Arms.Cooldown", 20000);
			config.addDefault("Abilities.Water.WaterArms.Arms.AllowPlantSource", true);

			config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.Enabled", true);
			config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.Damage", Double.valueOf(10.0));
			config.addDefault("Abilities.Water.WaterArms.Arms.Lightning.KillUser", false);

			config.addDefault("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled", false);
			config.addDefault("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown", 200);

			config.addDefault("Abilities.Water.WaterArms.Whip.MaxLength", 12);
			config.addDefault("Abilities.Water.WaterArms.Whip.MaxLengthWeak", 8);

			config.addDefault("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.Normal", 16);
			config.addDefault("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.FullMoon", 20);

			config.addDefault("Abilities.Water.WaterArms.Whip.Pull.Multiplier", 0.15);

			config.addDefault("Abilities.Water.WaterArms.Whip.Punch.PunchDamage", 0.5);
			config.addDefault("Abilities.Water.WaterArms.Whip.Punch.MaxLength", 6);
			config.addDefault("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.Normal", 11);
			config.addDefault("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.FullMoon", 13);

			config.addDefault("Abilities.Water.WaterArms.Whip.Grapple.RespectRegions", false);

			config.addDefault("Abilities.Water.WaterArms.Whip.Grab.HoldTime", 3500);

			config.addDefault("Abilities.Water.WaterArms.Freeze.Range", 20);
			config.addDefault("Abilities.Water.WaterArms.Freeze.Damage", 2);

			config.addDefault("Abilities.Water.WaterArms.Spear.Range", 30);
			config.addDefault("Abilities.Water.WaterArms.Spear.Damage", 3);
			config.addDefault("Abilities.Water.WaterArms.Spear.DamageEnabled", true);
			config.addDefault("Abilities.Water.WaterArms.Spear.Sphere", 2);
			config.addDefault("Abilities.Water.WaterArms.Spear.Duration", 4500);
			config.addDefault("Abilities.Water.WaterArms.Spear.Length", 18);

			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Range.Normal", 45);
			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Range.FullMoon", 60);
			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.Normal", 3);
			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.FullMoon", 6);
			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Duration.Normal", 7000);
			config.addDefault("Abilities.Water.WaterArms.Spear.NightAugments.Duration.FullMoon", 12000);

			config.addDefault("Abilities.Water.WaterBubble.Enabled", true);
			config.addDefault("Abilities.Water.WaterBubble.Radius", 6);

			config.addDefault("Abilities.Water.WaterManipulation.Enabled", true);
			config.addDefault("Abilities.Water.WaterManipulation.Damage", 3.0);
			config.addDefault("Abilities.Water.WaterManipulation.Range", 25);
			config.addDefault("Abilities.Water.WaterManipulation.SelectRange", 16);
			config.addDefault("Abilities.Water.WaterManipulation.CollisionRadius", 2);
			config.addDefault("Abilities.Water.WaterManipulation.DeflectRange", 3);
			config.addDefault("Abilities.Water.WaterManipulation.Speed", 35);
			config.addDefault("Abilities.Water.WaterManipulation.Push", 0.3);
			config.addDefault("Abilities.Water.WaterManipulation.Cooldown", 1000);

			config.addDefault("Abilities.Water.WaterSpout.Enabled", true);
			config.addDefault("Abilities.Water.WaterSpout.Height", 16);
			config.addDefault("Abilities.Water.WaterSpout.Interval", 50);
			config.addDefault("Abilities.Water.WaterSpout.BlockSpiral", true);
			config.addDefault("Abilities.Water.WaterSpout.Particles", false);
			config.addDefault("Abilities.Water.WaterSpout.Wave.Particles", false);
			config.addDefault("Abilities.Water.WaterSpout.Wave.Enabled", true);
			config.addDefault("Abilities.Water.WaterSpout.Wave.AllowPlantSource", true);
			config.addDefault("Abilities.Water.WaterSpout.Wave.Radius", 3.8);
			config.addDefault("Abilities.Water.WaterSpout.Wave.WaveRadius", 1.5);
			config.addDefault("Abilities.Water.WaterSpout.Wave.SelectRange", 6);
			config.addDefault("Abilities.Water.WaterSpout.Wave.AnimationSpeed", 1.2);
			config.addDefault("Abilities.Water.WaterSpout.Wave.ChargeTime", 500);
			config.addDefault("Abilities.Water.WaterSpout.Wave.FlightTime", 2500);
			config.addDefault("Abilities.Water.WaterSpout.Wave.Speed", 1.3);
			config.addDefault("Abilities.Water.WaterSpout.Wave.Cooldown", 6000);

			config.addDefault("Abilities.Water.WaterCombo.IceWave.Enabled", true);
			config.addDefault("Abilities.Water.WaterCombo.IceWave.Damage", 3);
			config.addDefault("Abilities.Water.WaterCombo.IceWave.Cooldown", 6000);
			config.addDefault("Abilities.Water.WaterCombo.IceWave.ThawRadius", 10);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.Enabled", true);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.Damage", 2);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.Radius", 2.5);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.Range", 12);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.MaxShots", 30);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.AnimationSpeed", 1);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.ShootTime", 10000);
			config.addDefault("Abilities.Water.WaterCombo.IceBullet.Cooldown", 10000);

			config.addDefault("Abilities.Earth.Passive.Duration", 2500);
			config.addDefault("Abilities.Earth.Passive.SandRunSpeed", 2);
			
			config.addDefault("Abilities.Earth.Catapult.Enabled", true);
			config.addDefault("Abilities.Earth.Catapult.Length", 6);
			config.addDefault("Abilities.Earth.Catapult.Push", 4);
			config.addDefault("Abilities.Earth.Catapult.ShiftModifier", 2);
			config.addDefault("Abilities.Earth.Catapult.Cooldown", 1500);

			config.addDefault("Abilities.Earth.Collapse.Enabled", true);
			config.addDefault("Abilities.Earth.Collapse.SelectRange", 20);
			config.addDefault("Abilities.Earth.Collapse.Radius", 7);
			config.addDefault("Abilities.Earth.Collapse.Speed", 8);
			config.addDefault("Abilities.Earth.Collapse.Column.Height", 6);
			config.addDefault("Abilities.Earth.Collapse.Column.Cooldown", 500);
			config.addDefault("Abilities.Earth.Collapse.Wall.Height", 6);
			config.addDefault("Abilities.Earth.Collapse.Wall.Cooldown", 500);
			
			config.addDefault("Abilities.Earth.EarthArmor.Enabled", true);
			config.addDefault("Abilities.Earth.EarthArmor.SelectRange", 10);
			config.addDefault("Abilities.Earth.EarthArmor.Duration", 10000);
			config.addDefault("Abilities.Earth.EarthArmor.Strength", 2);
			config.addDefault("Abilities.Earth.EarthArmor.Cooldown", 17500);

			config.addDefault("Abilities.Earth.EarthBlast.Enabled", true);
			config.addDefault("Abilities.Earth.EarthBlast.CanHitSelf", false);
			config.addDefault("Abilities.Earth.EarthBlast.SelectRange", 10);
			config.addDefault("Abilities.Earth.EarthBlast.Range", 30);
			config.addDefault("Abilities.Earth.EarthBlast.Speed", 35);
			config.addDefault("Abilities.Earth.EarthBlast.Revert", true);
			config.addDefault("Abilities.Earth.EarthBlast.Damage", 3);
			config.addDefault("Abilities.Earth.EarthBlast.Push", 0.3);
			config.addDefault("Abilities.Earth.EarthBlast.Cooldown", 500);
			config.addDefault("Abilities.Earth.EarthBlast.DeflectRange", 3);
			config.addDefault("Abilities.Earth.EarthBlast.CollisionRadius", 2);

			config.addDefault("Abilities.Earth.EarthGrab.Enabled", true);
			config.addDefault("Abilities.Earth.EarthGrab.SelectRange", 14);
			config.addDefault("Abilities.Earth.EarthGrab.Height", 6);
			config.addDefault("Abilities.Earth.EarthGrab.Cooldown", 500);

			config.addDefault("Abilities.Earth.EarthTunnel.Enabled", true);
			config.addDefault("Abilities.Earth.EarthTunnel.MaxRadius", 1);
			config.addDefault("Abilities.Earth.EarthTunnel.Range", 10);
			config.addDefault("Abilities.Earth.EarthTunnel.Radius", 0.25);
			config.addDefault("Abilities.Earth.EarthTunnel.Revert", true);
			config.addDefault("Abilities.Earth.EarthTunnel.Interval", 30);

			config.addDefault("Abilities.Earth.Extraction.Enabled", true);
			config.addDefault("Abilities.Earth.Extraction.SelectRange", 5);
			config.addDefault("Abilities.Earth.Extraction.Cooldown", 500);
			config.addDefault("Abilities.Earth.Extraction.TripleLootChance", 10);
			config.addDefault("Abilities.Earth.Extraction.DoubleLootChance", 30);

			config.addDefault("Abilities.Earth.LavaFlow.Enabled", true);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftCooldown", 20000);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCooldown", 10000);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLandCooldown", 500);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftCleanupDelay", 10000);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCleanupDelay", 7000);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLandCleanupDelay", 20000);
			config.addDefault("Abilities.Earth.LavaFlow.ClickRange", 10.0);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftRadius", 7.0);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftPlatformRadius", 1.5);
			config.addDefault("Abilities.Earth.LavaFlow.ClickRadius", 5.0);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLavaCreateSpeed", 0.045);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLandCreateSpeed", 0.10);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftFlowSpeed", 0.01);
			config.addDefault("Abilities.Earth.LavaFlow.ShiftRemoveSpeed", 3.0);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLavaStartDelay", 1500);
			config.addDefault("Abilities.Earth.LavaFlow.ClickLandStartDelay", 0);
			config.addDefault("Abilities.Earth.LavaFlow.UpwardFlow", 2);
			config.addDefault("Abilities.Earth.LavaFlow.DownwardFlow", 4);
			config.addDefault("Abilities.Earth.LavaFlow.AllowNaturalFlow", false);
			config.addDefault("Abilities.Earth.LavaFlow.ParticleDensity", 0.11);
			config.addDefault("Abilities.Earth.LavaFlow.RevertMaterial", "STONE");

			config.addDefault("Abilities.Earth.EarthSmash.Enabled", true);
			config.addDefault("Abilities.Earth.EarthSmash.AllowGrab", true);
			config.addDefault("Abilities.Earth.EarthSmash.AllowFlight", true);
			config.addDefault("Abilities.Earth.EarthSmash.GrabRange", 16);
			config.addDefault("Abilities.Earth.EarthSmash.SelectRange", 12);
			config.addDefault("Abilities.Earth.EarthSmash.ChargeTime", 1500);
			config.addDefault("Abilities.Earth.EarthSmash.Cooldown", 3000);
			config.addDefault("Abilities.Earth.EarthSmash.ShootRange", 25);
			config.addDefault("Abilities.Earth.EarthSmash.Damage", 5);
			config.addDefault("Abilities.Earth.EarthSmash.Knockback", 3.5);
			config.addDefault("Abilities.Earth.EarthSmash.Knockup", 0.15);
			config.addDefault("Abilities.Earth.EarthSmash.FlightSpeed", 0.72);
			config.addDefault("Abilities.Earth.EarthSmash.FlightTimer", 3000);
			config.addDefault("Abilities.Earth.EarthSmash.RemoveTimer", 30000);
			config.addDefault("Abilities.Earth.EarthSmash.RequiredBendableBlocks", 11);
			config.addDefault("Abilities.Earth.EarthSmash.MaxBlocksToPassThrough", 3);
			config.addDefault("Abilities.Earth.EarthSmash.ShootAnimationInterval", 25);
			config.addDefault("Abilities.Earth.EarthSmash.FlightAnimationInterval", 0);
			config.addDefault("Abilities.Earth.EarthSmash.LiftAnimationInterval", 30);
			config.addDefault("Abilities.Earth.EarthSmash.GrabDetectionRadius", 2.5);
			config.addDefault("Abilities.Earth.EarthSmash.FlightDetectionRadius", 3.5);

			config.addDefault("Abilities.Earth.MetalClips.Enabled", true);			
			config.addDefault("Abilities.Earth.MetalClips.Damage", 2);
			config.addDefault("Abilities.Earth.MetalClips.CrushDamage", 1);
			config.addDefault("Abilities.Earth.MetalClips.Range", 10);
			config.addDefault("Abilities.Earth.MetalClips.MagnetRange", 20);
			config.addDefault("Abilities.Earth.MetalClips.MagnetPower", 0.6);
			config.addDefault("Abilities.Earth.MetalClips.Cooldown", 6000);
			config.addDefault("Abilities.Earth.MetalClips.CrushCooldown", 2000);
			config.addDefault("Abilities.Earth.MetalClips.ShootCooldown", 0);
			config.addDefault("Abilities.Earth.MetalClips.Duration", 10000);
			config.addDefault("Abilities.Earth.MetalClips.ThrowEnabled", true);

			config.addDefault("Abilities.Earth.RaiseEarth.Enabled", true);
			config.addDefault("Abilities.Earth.RaiseEarth.Speed", 10);
			config.addDefault("Abilities.Earth.RaiseEarth.Column.SelectRange", 20);
			config.addDefault("Abilities.Earth.RaiseEarth.Column.Height", 6);
			config.addDefault("Abilities.Earth.RaiseEarth.Column.Cooldown", 500);
			config.addDefault("Abilities.Earth.RaiseEarth.Wall.SelectRange", 20);
			config.addDefault("Abilities.Earth.RaiseEarth.Wall.Height", 6);
			config.addDefault("Abilities.Earth.RaiseEarth.Wall.Width", 6);
			config.addDefault("Abilities.Earth.RaiseEarth.Wall.Cooldown", 500);

			config.addDefault("Abilities.Earth.Shockwave.Enabled", true);
			config.addDefault("Abilities.Earth.Shockwave.FallThreshold", 12);
			config.addDefault("Abilities.Earth.Shockwave.ChargeTime", 2500);
			config.addDefault("Abilities.Earth.Shockwave.Cooldown", 6000);
			config.addDefault("Abilities.Earth.Shockwave.Damage", 4);
			config.addDefault("Abilities.Earth.Shockwave.Knockback", 1.1);
			config.addDefault("Abilities.Earth.Shockwave.Range", 15);
			config.addDefault("Abilities.Earth.Shockwave.Angle", 40);

			config.addDefault("Abilities.Earth.SandSpout.Enabled", true);
			config.addDefault("Abilities.Earth.SandSpout.Height", 10);
			config.addDefault("Abilities.Earth.SandSpout.BlindnessTime", 10);
			config.addDefault("Abilities.Earth.SandSpout.SpoutDamage", 1);
			config.addDefault("Abilities.Earth.SandSpout.Spiral", false);
			config.addDefault("Abilities.Earth.SandSpout.Interval", 100);

			config.addDefault("Abilities.Earth.Tremorsense.Enabled", true);
			config.addDefault("Abilities.Earth.Tremorsense.MaxDepth", 10);
			config.addDefault("Abilities.Earth.Tremorsense.Radius", 5);
			config.addDefault("Abilities.Earth.Tremorsense.LightThreshold", 7);
			config.addDefault("Abilities.Earth.Tremorsense.Cooldown", 1000);
			
			config.addDefault("Abilities.Fire.Blaze.Enabled", true);
			config.addDefault("Abilities.Fire.Blaze.Arc", 14);
			config.addDefault("Abilities.Fire.Blaze.Range", 7);
			config.addDefault("Abilities.Fire.Blaze.Speed", 15);
			config.addDefault("Abilities.Fire.Blaze.Cooldown", 500);
			config.addDefault("Abilities.Fire.Blaze.Ring.Range", 7);
			config.addDefault("Abilities.Fire.Blaze.Ring.Angle", 10);
			config.addDefault("Abilities.Fire.Blaze.Ring.Cooldown", 1000);

			config.addDefault("Abilities.Fire.Combustion.Enabled", true);
			config.addDefault("Abilities.Fire.Combustion.Cooldown", 10000);
			config.addDefault("Abilities.Fire.Combustion.BreakBlocks", false);
			config.addDefault("Abilities.Fire.Combustion.Power", 1.0);
			config.addDefault("Abilities.Fire.Combustion.Damage", 4);
			config.addDefault("Abilities.Fire.Combustion.Radius", 4);
			config.addDefault("Abilities.Fire.Combustion.Range", 35);
			config.addDefault("Abilities.Fire.Combustion.Speed", 25);

			config.addDefault("Abilities.Fire.FireBlast.Enabled", true);
			config.addDefault("Abilities.Fire.FireBlast.Speed", 20);
			config.addDefault("Abilities.Fire.FireBlast.Range", 20);
			config.addDefault("Abilities.Fire.FireBlast.CollisionRadius", 2);
			config.addDefault("Abilities.Fire.FireBlast.Push", 0.3);
			config.addDefault("Abilities.Fire.FireBlast.Damage", 3);
			config.addDefault("Abilities.Fire.FireBlast.Cooldown", 1500);
			config.addDefault("Abilities.Fire.FireBlast.Dissipate", false);
			config.addDefault("Abilities.Fire.FireBlast.FireTicks", 0);
			config.addDefault("Abilities.Fire.FireBlast.SmokeParticleRadius", 0.3);
			config.addDefault("Abilities.Fire.FireBlast.FlameParticleRadius", 0.275);
			config.addDefault("Abilities.Fire.FireBlast.Charged.ChargeTime", 3000);
			config.addDefault("Abilities.Fire.FireBlast.Charged.CollisionRadius", 2);
			config.addDefault("Abilities.Fire.FireBlast.Charged.Damage", 4);
			config.addDefault("Abilities.Fire.FireBlast.Charged.DamageRadius", 4);
			config.addDefault("Abilities.Fire.FireBlast.Charged.DamageBlocks", true);
			config.addDefault("Abilities.Fire.FireBlast.Charged.ExplosionRadius", 1);
			config.addDefault("Abilities.Fire.FireBlast.Charged.Range", 20);
			config.addDefault("Abilities.Fire.FireBlast.Charged.FireTicks", 0);

			config.addDefault("Abilities.Fire.FireBurst.Enabled", true);
			config.addDefault("Abilities.Fire.FireBurst.Damage", 2);
			config.addDefault("Abilities.Fire.FireBurst.Ignite", true);
			config.addDefault("Abilities.Fire.FireBurst.ChargeTime", 3500);
			config.addDefault("Abilities.Fire.FireBurst.Cooldown", 0);
			config.addDefault("Abilities.Fire.FireBurst.Range", 14);
			config.addDefault("Abilities.Fire.FireBurst.AnglePhi", 10);
			config.addDefault("Abilities.Fire.FireBurst.AngleTheta", 10);
			config.addDefault("Abilities.Fire.FireBurst.ParticlesPercentage", 5);

			config.addDefault("Abilities.Fire.FireJet.Enabled", true);
			config.addDefault("Abilities.Fire.FireJet.Speed", 0.8);
			config.addDefault("Abilities.Fire.FireJet.Duration", 2000);
			config.addDefault("Abilities.Fire.FireJet.Cooldown", 7000);
			config.addDefault("Abilities.Fire.FireJet.IsAvatarStateToggle", true);

			config.addDefault("Abilities.Fire.FireShield.Enabled", true);
			config.addDefault("Abilities.Fire.FireShield.Radius", 3);
			config.addDefault("Abilities.Fire.FireShield.DiscRadius", 1.5);
			config.addDefault("Abilities.Fire.FireShield.Duration", 1000);
			config.addDefault("Abilities.Fire.FireShield.Cooldown", 500);
			config.addDefault("Abilities.Fire.FireShield.Interval", 100);
			config.addDefault("Abilities.Fire.FireShield.FireTicks", 2);

			config.addDefault("Abilities.Fire.HeatControl.Enabled", true);
			config.addDefault("Abilities.Fire.HeatControl.Extinguish.Range", 20);
			config.addDefault("Abilities.Fire.HeatControl.Extinguish.Radius", 7);
			config.addDefault("Abilities.Fire.HeatControl.Extinguish.Cooldown", 500);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.Range", 10);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.Radius", 7);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.RevertTime", 20000);
			config.addDefault("Abilities.Fire.HeatControl.Melt.Range", 15);
			config.addDefault("Abilities.Fire.HeatControl.Melt.Radius", 5);
			config.addDefault("Abilities.Fire.HeatControl.Cook.CookTime", 2000);

			config.addDefault("Abilities.Fire.Illumination.Enabled", true);
			config.addDefault("Abilities.Fire.Illumination.Passive", true);
			config.addDefault("Abilities.Fire.Illumination.Range", 5);
			config.addDefault("Abilities.Fire.Illumination.Cooldown", 500);
			config.addDefault("Abilities.Fire.Illumination.LightThreshold", 7);

			config.addDefault("Abilities.Fire.Lightning.Enabled", true);
			config.addDefault("Abilities.Fire.Lightning.Damage", 4.0);
			config.addDefault("Abilities.Fire.Lightning.Range", 20.0);
			config.addDefault("Abilities.Fire.Lightning.ChargeTime", 2500);
			config.addDefault("Abilities.Fire.Lightning.Cooldown", 500);
			config.addDefault("Abilities.Fire.Lightning.StunChance", 0.20);
			config.addDefault("Abilities.Fire.Lightning.StunDuration", 30.0);
			config.addDefault("Abilities.Fire.Lightning.MaxArcAngle", 2.5);
			config.addDefault("Abilities.Fire.Lightning.SubArcChance", 0.00125);
			config.addDefault("Abilities.Fire.Lightning.ChainArcRange", 6.0);
			config.addDefault("Abilities.Fire.Lightning.ChainArcChance", 0.50);
			config.addDefault("Abilities.Fire.Lightning.MaxChainArcs", 2);
			config.addDefault("Abilities.Fire.Lightning.WaterArcs", 4);
			config.addDefault("Abilities.Fire.Lightning.WaterArcRange", 20.0);
			config.addDefault("Abilities.Fire.Lightning.SelfHitWater", true);
			config.addDefault("Abilities.Fire.Lightning.SelfHitClose", false);
			config.addDefault("Abilities.Fire.Lightning.ArcOnIce", false);

			config.addDefault("Abilities.Fire.WallOfFire.Enabled", true);
			config.addDefault("Abilities.Fire.WallOfFire.Range", 3);
			config.addDefault("Abilities.Fire.WallOfFire.Height", 4);
			config.addDefault("Abilities.Fire.WallOfFire.Width", 4);
			config.addDefault("Abilities.Fire.WallOfFire.Duration", 5000);
			config.addDefault("Abilities.Fire.WallOfFire.Damage", 1);
			config.addDefault("Abilities.Fire.WallOfFire.Cooldown", 11000);
			config.addDefault("Abilities.Fire.WallOfFire.Interval", 250);
			config.addDefault("Abilities.Fire.WallOfFire.DamageInterval", 500);
			config.addDefault("Abilities.Fire.WallOfFire.FireTicks", 0);
			config.addDefault("Abilities.Fire.WallOfFire.MaxAngle", 50);

			config.addDefault("Abilities.Fire.FireCombo.FireKick.Enabled", true);
			config.addDefault("Abilities.Fire.FireCombo.FireKick.Range", 7.0);
			config.addDefault("Abilities.Fire.FireCombo.FireKick.Damage", 3.0);
			config.addDefault("Abilities.Fire.FireCombo.FireKick.Cooldown", 6000);
			config.addDefault("Abilities.Fire.FireCombo.FireSpin.Enabled", true);
			config.addDefault("Abilities.Fire.FireCombo.FireSpin.Range", 7);
			config.addDefault("Abilities.Fire.FireCombo.FireSpin.Damage", 3.0);
			config.addDefault("Abilities.Fire.FireCombo.FireSpin.Knockback", 3.0);
			config.addDefault("Abilities.Fire.FireCombo.FireSpin.Cooldown", 5000);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.Enabled", true);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.Range", 20.0);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.Damage", 4.0);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.Speed", 0.55);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.Cooldown", 6000);
			config.addDefault("Abilities.Fire.FireCombo.FireWheel.FireTicks", 2.5);
			config.addDefault("Abilities.Fire.FireCombo.JetBlast.Enabled", true);
			config.addDefault("Abilities.Fire.FireCombo.JetBlast.Speed", 1.2);
			config.addDefault("Abilities.Fire.FireCombo.JetBlast.Cooldown", 6000);
			config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Enabled", true);
			config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Speed", 1.1);
			config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Damage", 4);
			config.addDefault("Abilities.Fire.FireCombo.JetBlaze.Cooldown", 6000);
			config.addDefault("Abilities.Fire.FireCombo.JetBlaze.FireTicks", 2.5);

			config.addDefault("Abilities.Chi.Passive.ExhaustionFactor", 0.3);
			config.addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
			config.addDefault("Abilities.Chi.Passive.Speed", 1);
			config.addDefault("Abilities.Chi.Passive.Jump", 1);
			config.addDefault("Abilities.Chi.Passive.BlockChi.Chance", 25);
			config.addDefault("Abilities.Chi.Passive.BlockChi.Duration", 1000);

			config.addDefault("Abilities.Chi.ChiCombo.Immobilize.Enabled", true);
			config.addDefault("Abilities.Chi.ChiCombo.Immobilize.ParalyzeDuration", 3500);
			config.addDefault("Abilities.Chi.ChiCombo.Immobilize.Cooldown", 15000);

			config.addDefault("Abilities.Chi.AcrobatStance.Enabled", true);
			config.addDefault("Abilities.Chi.AcrobatStance.ChiBlockBoost", 3);
			config.addDefault("Abilities.Chi.AcrobatStance.Speed", 1);
			config.addDefault("Abilities.Chi.AcrobatStance.Jump", 1);

			config.addDefault("Abilities.Chi.HighJump.Enabled", true);
			config.addDefault("Abilities.Chi.HighJump.Height", 1.3);
			config.addDefault("Abilities.Chi.HighJump.Cooldown", 3000);

			config.addDefault("Abilities.Chi.Paralyze.Enabled", true);
			config.addDefault("Abilities.Chi.Paralyze.Cooldown", 10000);
			config.addDefault("Abilities.Chi.Paralyze.Duration", 1500);

			config.addDefault("Abilities.Chi.RapidPunch.Enabled", true);
			config.addDefault("Abilities.Chi.RapidPunch.Damage", 1);
			config.addDefault("Abilities.Chi.RapidPunch.Distance", 3);
			config.addDefault("Abilities.Chi.RapidPunch.Cooldown", 6000);
			config.addDefault("Abilities.Chi.RapidPunch.Punches", 3);

			config.addDefault("Abilities.Chi.Smokescreen.Enabled", true);
			config.addDefault("Abilities.Chi.Smokescreen.Cooldown", 25000);
			config.addDefault("Abilities.Chi.Smokescreen.Radius", 4);
			config.addDefault("Abilities.Chi.Smokescreen.Duration", 12);

			config.addDefault("Abilities.Chi.WarriorStance.Enabled", true);
			config.addDefault("Abilities.Chi.WarriorStance.Strength", 1);
			config.addDefault("Abilities.Chi.WarriorStance.Resistance", -1);

			config.addDefault("Abilities.Chi.QuickStrike.Enabled", true);
			config.addDefault("Abilities.Chi.QuickStrike.Damage", 1);
			config.addDefault("Abilities.Chi.QuickStrike.ChiBlockChance", 10);

			config.addDefault("Abilities.Chi.SwiftKick.Enabled", true);
			config.addDefault("Abilities.Chi.SwiftKick.Damage", 2);
			config.addDefault("Abilities.Chi.SwiftKick.ChiBlockChance", 15);
			config.addDefault("Abilities.Chi.SwiftKick.Cooldown", 4000);

			config.addDefault("Storage.engine", "sqlite");

			config.addDefault("Storage.MySQL.host", "localhost");
			config.addDefault("Storage.MySQL.port", 3306);
			config.addDefault("Storage.MySQL.pass", "");
			config.addDefault("Storage.MySQL.db", "minecraft");
			config.addDefault("Storage.MySQL.user", "root");

			config.addDefault("debug", false);

			defaultConfig.save();
		}
	}
	
	public static FileConfiguration getConfig() {
		return ConfigManager.defaultConfig.get();
	}
}
