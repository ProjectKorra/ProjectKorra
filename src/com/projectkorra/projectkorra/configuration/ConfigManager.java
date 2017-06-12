package com.projectkorra.projectkorra.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

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
			config.addDefault("Chat.Branding.JoinMessage.Enabled", true);
			config.addDefault("Chat.Branding.AutoAnnouncer.Enabled", true);
			config.addDefault("Chat.Branding.AutoAnnouncer.Interval", 30);
			config.addDefault("Chat.Branding.Color", "GOLD");
			config.addDefault("Chat.Branding.Borders.TopBorder", "");
			config.addDefault("Chat.Branding.Borders.BottomBorder", "");
			config.addDefault("Chat.Branding.ChatPrefix.Prefix", "");
			config.addDefault("Chat.Branding.ChatPrefix.Suffix", " ");

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

			config.addDefault("Commands.Display.Description", "This command will show you all of the elements you have bound if you do not specify an element. If you do specify an element (Air, Water, Earth, Fire, or Chi), it will show you all of the available abilities of that element installed on the server.");

			config.addDefault("Commands.Display.NoCombosAvailable", "There are no {element} combos available.");
			config.addDefault("Commands.Display.NoPassivesAvailable", "There are no {element} passives available.");
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
			config.addDefault("Commands.Add.SuccessfullyAddedAll", "You now also have: ");
			config.addDefault("Commands.Add.PlayerNotFound", "That player could not be found.");
			config.addDefault("Commands.Add.InvalidElement", "You must specify a valid element.");
			config.addDefault("Commands.Add.AlreadyHasElement", "You already have that element!");
			config.addDefault("Commands.Add.AlreadyHasSubElement", "You already have that subelement!");
			config.addDefault("Commands.Add.AlreadyHasAllElements", "You already have all elements!");
			config.addDefault("Commands.Add.Other.SuccessfullyAddedCFW", "{target} is now also a {element}.");
			config.addDefault("Commands.Add.Other.SuccessfullyAddedAE", "{target} is now also an {element}.");
			config.addDefault("Commands.Add.Other.SuccessfullyAddedAll", "{target} now also has: ");
			config.addDefault("Commands.Add.Other.AlreadyHasElement", "{target} already has that element!");
			config.addDefault("Commands.Add.Other.AlreadyHasSubElement", "{target} already has that subelement!");
			config.addDefault("Commands.Add.Other.AlreadyHasAllElements", "{target} already has all elements!");
			
			config.addDefault("DeathMessages.Enabled", true);
			config.addDefault("DeathMessages.Default", "{victim} was slain by {attacker}'s {ability}");

			config.addDefault("Abilities.Avatar.AvatarState.Description", "The signature ability of the Avatar, this is a toggle. Left click to activate to become " + "nearly unstoppable. While in the Avatar State, the user takes severely reduced damage from " + "all sources, regenerates health rapidly, and is granted extreme speed. Nearly all abilities " + "are incredibly amplified in this state. Additionally, AirShield and FireJet become toggle-able " + "abilities and last until you deactivate them or the Avatar State. Left click again with the Avatar " + "State selected to deactivate it.");

			config.addDefault("Commands.Help.Elements.Air", "Air is the element of freedom. Airbenders are natural pacifists and great explorers. There is nothing stopping them from scaling the tallest of mountains and walls easily. They specialize in redirection, from blasting things away with gusts of winds, to forming a shield around them to prevent damage. Easy to get across flat terrains, such as oceans, there is practically no terrain off limits to Airbenders. They lack much raw damage output, but make up for it with with their ridiculous amounts of utility and speed.\nAirbenders can chain their abilities into combos, type /b help AirCombos for more information.");
			config.addDefault("Abilities.Air.AirBlast.Description", "AirBlast is the most fundamental bending technique of an airbender. It allows the bender to be extremely agile and possess great mobility, but also has many utility options, such as cooling lava, opening doors and flicking levers.");
			config.addDefault("Abilities.Air.AirBlast.Instructions", "\n" + "(Push) " + "Left click while aiming at an entity to push them back." + "\n" + "(Throw) " + "Tap sneak to select a location and left click in a direction to throw entities away from the selected location.");
			config.addDefault("Abilities.Air.AirBlast.DeathMessage", "{victim} was flung by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBlast.HorizontalVelocityDeath", "{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBurst.Description", "AirBurst is one of the most powerful abilities in the airbender's arsenal. It allows the bender to create space between them and whoever is close to them. AirBurst is extremely useful when you're surrounded by mobs, of if you're low in health and need to escape. It can also be useful for confusing your target also.");
			config.addDefault("Abilities.Air.AirBurst.Instructions", "\n" + "(Sphere) Hold sneak until particles appear and then release shift to create air that expands outwards, pushing entities back. If you fall from great height while you are on this slot, the burst will automatically activate." + "\n" + "(Cone) While charging the move with shift, click to send the burst in a cone only going in one direction.");
			config.addDefault("Abilities.Air.AirBurst.DeathMessage", "{victim} was thrown down by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirBurst.HorizontalVelocityDeath", "{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirScooter.Description", "AirScooter is a fast means of transportation. It can be used to escape from enemies or confuse them by using air scooter around them.");
			config.addDefault("Abilities.Air.AirScooter.Instructions", "Sprint, jump, and left click while in the air to activate air scooter. You will then move forward in the direction you're looking.");
			config.addDefault("Abilities.Air.Tornado.Description", "Tornado is one of the most powerful and advanced abilities that an Airbender knows. If the tornado meets a player or mob, it will push them around. Tornado can also be used to push back projectiles and used for mobility. Use a tornado directly under you to propel yourself upwards.");
			config.addDefault("Abilities.Air.Tornado.Instructions", "Hold sneak and a tornado will form gradually wherever you look.");
			config.addDefault("Abilities.Air.AirShield.Description", "Air Shield is one of the most powerful defensive techniques in existence. This ability is mainly used when you are low health and need protection. It's also useful when you're surrounded by mobs.");
			config.addDefault("Abilities.Air.AirShield.Instructions", "Hold sneak and a shield of air will form around you, blocking projectiles and pushing entities back.");
			config.addDefault("Abilities.Air.AirSpout.Description", "This ability gives the airbender limited sustained levitation. It allows an airbender to gain a height advantage to escape from mobs, players or just to dodge from attacks. This ability is also useful for building as it allows you to reach great heights.");
			config.addDefault("Abilities.Air.AirSpout.Instructions", "Left click to activate a spout beneath you and hold spacebar to go higher. If you wish to go lower, simply hold sneak. To disable this ability, left click once again.");
			config.addDefault("Abilities.Air.AirSuction.Description", "AirSuction is a basic ability that allows you to manipulation an entity's movement. It can be used to bring someone back to you when they're running away, or even to get yourself to great heights.");
			config.addDefault("Abilities.Air.AirSuction.Instructions", "\n" + "(Pull) Left click while aiming at a target to pull them towards you." + "\n" + "(Manipulation) Sneak to select a point and then left click at a target or yourself to send you or your target to the point that you selected.");
			config.addDefault("Abilities.Air.AirSuction.HorizontalVelocityDeath", "{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.AirSwipe.Description", "AirSwipe is the most commonly used damage ability in an airbender's arsenal. An arc of air will flow from you towards the direction you're facing, cutting and pushing back anything in its path. This ability will extinguish fires, cool lava, and cut things like grass, mushrooms, and flowers.");
			config.addDefault("Abilities.Air.AirSwipe.Instructions", "\n" + "(Uncharged) Simply left click to send an air swipe out that will damage targets that it comes into contact with." + "\n" + "(Charged) Hold sneak until particles appear, then release sneak to send a more powerful air swipe out that damages entity's that it comes into contact with.");
			config.addDefault("Abilities.Air.AirSwipe.DeathMessage", "{victim} was struck by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.Flight.Description", "Flight is one of the most advanced airbending abilities there is. It's used to escape from players or mobs or to confuse your enemy by flying around them, making you extremely hard to hit.");
			config.addDefault("Abilities.Air.Flight.Instructions", "\n" + "(Fly) Jump in the air and hold sneak to fly in the direction that you're looking." + "\n" + "(Hover) While flying with this ability, left click to hover in the air. Left click again to disable hovering");
			config.addDefault("Abilities.Air.Suffocate.Description", "This ability is one of the most dangerous abilities an Airbender possesses. Although it is difficult to perform, it's extremely deadly once the ability starts, making it difficult for enemies to escape.");
			config.addDefault("Abilities.Air.Suffocate.Instructions", "Hold sneak while looking at a target to begin suffocating them. If the target goes out of range, you get damaged, or you release sneak, the ability will cancel.");
			config.addDefault("Abilities.Air.Suffocate.DeathMessage", "{victim} was asphyxiated by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.Combo.Twister.Description", "Create a cyclone of air that travels along the ground grabbing nearby entities.");
			config.addDefault("Abilities.Air.Combo.AirStream.Description", "Control a large stream of air that grabs onto enemies allowing you to direct them temporarily.");
			config.addDefault("Abilities.Air.Combo.AirSweep.Description", "Sweep the air in front of you hitting multiple enemies, causing moderate damage and a large knockback. The radius and direction of AirSweep is controlled by moving your mouse in a sweeping motion. For example, if you want to AirSweep upward, then move your mouse upward right after you left click AirBurst");
			config.addDefault("Abilities.Air.Combo.AirSweep.DeathMessage", "{victim} was swept away by {attacker}'s {ability}");
			config.addDefault("Abilities.Air.Passive.AirAgility.Description", "AirAgility is a passive ability which enables airbenders to run faster and jump higher.");
			config.addDefault("Abilities.Air.Passive.AirSaturation.Description", "AirSaturation is a passive ability which causes airbenders' hunger to deplete at a slower rate.");
			config.addDefault("Abilities.Air.Passive.GracefulDescent.Description", "GracefulDescent is a passive ability which allows airbenders to make a gentle landing, negating all fall damage on any surface.");

			config.addDefault("Commands.Help.Elements.Water", "Water is the element of change. Waterbending focuses on using your opponents own force against them. Using redirection and various dodging tactics, you can be made practically untouchable by an opponent. Waterbending provides agility, along with strong offensive skills while in or near water.\nWaterbenders can chain their abilities into combos, type /b help WaterCombos for more information.");
			config.addDefault("Abilities.Water.Bloodbending.Description", "Bloodbending is one of the most unique bending abilities that existed and it has immense power, which is why it was made illegal in the Avatar universe. People who are capable of bloodbending are immune to your technique, and you are immune to theirs.");
			config.addDefault("Abilities.Water.Bloodbending.Instructions", "\n" + "(Control) Hold sneak while looking at an entity to bloodbend them. You will then be controlling the entity, making them move wherever you look." + "\n" + "(Throw) While bloodbending an entity, left click to throw that entity in the direction you're looking.");
			config.addDefault("Abilities.Water.Bloodbending.DeathMessage", "{victim} was destroyed by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.Bloodbending.HorizontalVelocityDeath", "{victim} experienced kinetic damage by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.HealingWaters.Description", "HealingWaters is an advanced waterbender skill that allows the player to heal themselves or others from the damage they've taken. If healing another player, you must continue to look at them to channel the ability.");
			config.addDefault("Abilities.Water.HealingWaters.Instructions", "Hold sneak to begin healing yourself or right click while sneaking to begin healing another player. You or the player must be in water and damaged for this ability to work, or you need to have water bottles in your inventory.");
			config.addDefault("Abilities.Water.IceBlast.Description", "IceBlast is a powerful ability that deals damage to entities it comes into contact with. Because IceBlast's travel time is pretty quick, it's increddibly useful for finishing off low health targets.");
			config.addDefault("Abilities.Water.IceBlast.Instructions", "Tap sneak while looking at an ice block and then click in a direction to send an ice blast in that direction.");
			config.addDefault("Abilities.Water.IceBlast.DeathMessage", "{victim} was shattered by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.IceSpike.Description", "This ability offers a powerful ice utility for Waterbenders. It can be used to fire an ice blast or raise an ice spike. If the ice blast or ice spike comes into contact with another entity, it will give them slowness and deal some damage to them..");
			config.addDefault("Abilities.Water.IceSpike.Instructions", "\n" + "(Blast) Tap sneak on a water source and then left click in a direction to fire an ice blast in a direction. Additionally, you can left click to manipulate the ice blast while it's in the air to change the direction of the blast." + "\n" + "(Spike) While in range of ice, tap sneak to raise ice pillars from the ice. If a player is caught in these ice pillars they will be propelled into the air. You cannot be looking at ice or water or this feature will not activate. Alternatively, you can left click an ice block to raise a single pilar of ice.");
			config.addDefault("Abilities.Water.IceSpike.DeathMessage", "{victim} was impaled by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.OctopusForm.Description", "OctopusForm is one of the most advanced abilities in a waterbender's aresenal. It has the possibility of doing high damage to anyone it comes into contact with.");
			config.addDefault("Abilities.Water.OctopusForm.Instructions", "Left click a water source and then hold sneak to form a set of water tentacles. This ability will channel as long as you are holding sneak. Additionally, if you left click this ability will whip targets you're facing dealing damage and knockback, if they're in range.");
			config.addDefault("Abilities.Water.OctopusForm.DeathMessage", "{victim} was slapped by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.PhaseChange.Description", "PhaseChange is one of the most useful utility moves that a waterbender possess. This ability is better used when fighting, allowing you to create a platform on water that you can fight on and being territorial by manipulating your environment. It's also useful for travelling across seas.");
			config.addDefault("Abilities.Water.PhaseChange.Instructions", "\n" + "(Melt) To melt ice, hold sneak while looking at an ice block." + "\n" + "(Freeze) To freeze water and turn it into ice, simply left click at water. This ice will stay so long as you are in range, otherwise it will revert back to water. This only freezes the top layer of ice.");
			config.addDefault("Abilities.Water.Surge.Description", "Surge offers great utility and is one of the most important defence abilities for waterbender's. It can be used to push entities back, used to push yourself in a direction, trap entities and protect yourself with a shield.");
			config.addDefault("Abilities.Water.Surge.Instructions", "\n" + "(Shield) Left click on a water source and then hold sneak while looking up to create a water shield that will move wherever you look. Additionally, you can left click to turn this shield into ice. If you let go of sneak at any point, this ability will cancel." + "\n" + "(Surge) Tap sneak at a water source and click in a direction to fire a surge of water that will knock entities back. Additionally, if you tap sneak again before the surge reaches an entity, when it hits them it will encase them in ice.");
			config.addDefault("Abilities.Water.Torrent.Description", "Torrent is one of the strongest moves in a waterbender's arsenal. It has the potential to do immense damage and to be comboed with other abilities to perform a deal a large damage burst. Torrent is fundamental for waterbender's. ");
			config.addDefault("Abilities.Water.Torrent.Instructions", "\n" + "(Torrent) Left click at a water source and hold sneak to form the torrent. Then, left click and the torrent will shoot out, moving in the direction you're looking. If the torrent hits an entity, it can drag them and deal damage. Additionally, if you left click before the torrent hits a surface or entity it will freeze on impact." + "\n" + "(Wave) Left click a water source and hold sneak to form a torrent around you. Then, release sneak to send a wave of water expanding outwards every direction that will push entities back.");
			config.addDefault("Abilities.Water.Torrent.DeathMessage", "{victim} was taken down by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Description", "One of the most diverse moves in a Waterbender's arsenal, this move creates tendrils " + "of water from the players arms to emulate their actual arms. It has the potential to do a variety of things that can either do mass amounts of damage, or used for mobility.");
			config.addDefault("Abilities.Water.WaterArms.Instructions", "To activate this ability, tap sneak at a water source. Additionally, to de-activate this ability, hold sneak and left click." + "\n" + "(Pull) Left click at a target and your arms will expand outwards, pulling entities towards you if they're in range." + "\n" + "(Punch) Left click and one arm will expand outwards, punching anyone it hits and dealing damage." + "\n" + "(Grapple) Left click to send your arms forward, pulling you to whatever surface they land on." + "\n" + "(Grab) Left click to grab an entity that's in range. They will then be controlled and moved in whatever direction you look. Additionally, if you left click again you can throw the target that you're controlling." + "\n" + "(Freeze) Left click to rapidly fire ice blasts at a target, damaging the target and giving them slowness." + "\n" + "(Spear) Left click to send an ice spear out, damaging and freezing whoever it hits in ice blocks.");
			config.addDefault("Abilities.Water.WaterArms.SneakMessage", "Active Ability:");
			config.addDefault("Abilities.Water.WaterArms.Punch.DeathMessage", "{victim} was too slow for {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Freeze.DeathMessage", "{victim} was frozen by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterArms.Spear.DeathMessage", "{victim} was speared to death by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterBubble.Description", "WaterBubble is a basic waterbending ability that allows the bender to create air pockets under water. This is increddibly useful for building under water.");
			config.addDefault("Abilities.Water.WaterBubble.Instructions", "Hold sneak when in range of water to push the water back and create a water bubble. Alternatively, you can click to create a bubble for a short amount of time.");
			config.addDefault("Abilities.Water.WaterManipulation.Description", "WaterManipulation is a fundamental ability for waterbenders. Although it is a basic move, it allows for fast damage due to its rapid fire nature, which is incredibly useful when wanting to finish off low health targets.");
			config.addDefault("Abilities.Water.WaterManipulation.Instructions", "Tap sneak on a water source and left click to send a water manipulation to the point that you clicked. Additionally, you can left click again to change the direction of this move. This includes other players' WaterManipulations.");
			config.addDefault("Abilities.Water.WaterManipulation.DeathMessage", "{victim} was taken down by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.WaterSpout.Description", "This ability provides a Waterbender with a means of transportation. It's the most useful mobility move that a waterbender possesses and is great for chasing down targets or escaping.");
			config.addDefault("Abilities.Water.WaterSpout.Instructions", "\n" + "(Spout) Left click to activate a spout beneath you and hold spacebar to go higher. If you wish to go lower, simply hold sneak. To disable this ability, left click once again." + "\n" + "(Wave) Left click a water source and hold sneak until water has formed around you. Then, release sneak to ride a water wave that transports you in the direction you're looking. To cancel this water wave, left click with WaterSpout.");
			config.addDefault("Abilities.Water.Combo.IceBullet.Description", "Using a large cavern of ice, you can punch ice shards at your opponent causing moderate damage. To rapid fire, you must alternate between Left clicking and right clicking with IceBlast.");
			config.addDefault("Abilities.Water.Combo.IceBullet.DeathMessage", "{victim}'s heart was frozen by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.Combo.IceWave.Description", "PhaseChange your WaterWave into an IceWave that freezes and damages enemies.");
			config.addDefault("Abilities.Water.Combo.IceWave.DeathMessage", "{victim} was frozen solid by {attacker}'s {ability}");
			config.addDefault("Abilities.Water.Passive.FastSwim.Description", "FastSwim is a passive ability for waterbenders allowing them to travel quickly through the water. Simple hold shift while underwater to propel yourself forward.");
			config.addDefault("Abilities.Water.Passive.Hydrosink.Description", "Hydrosink is a passive ability for waterbenders enabling them to softly land on any waterbendable surface, cancelling all damage.");

			config.addDefault("Commands.Help.Elements.Earth", "Earth is the element of substance. Earthbenders share many of the same fundamental techniques as Waterbenders, but their domain is quite different and more readily accessible. Earthbenders dominate the ground and subterranean, having abilities to pull columns of rock straight up from the earth or drill their way through the mountain. They can also launch themselves through the air using pillars of rock, and will not hurt themselves assuming they land on something they can bend. The more skilled Earthbenders can even bend metal.");
			config.addDefault("Abilities.Earth.Catapult.Description", "Catapult is an advanced earthbending ability that allows you to forcefully push yourself using earth, reaching great heights. This technique is best used when travelling, but it can also be used to quickly escape a battle.");
			config.addDefault("Abilities.Earth.Catapult.Instructions", "Hold sneak until you see particles and hear a sound and then release to be propelled in the direction you're looking. Additionally, you can left-click to be propelled with less power.");
			config.addDefault("Abilities.Earth.Collapse.Description", "This ability is a basic earthbending ability that allows the earthbender great utility. It allows them to control earth blocks by compressing earth. Players and mobs can be trapped and killed if earth is collapsed and they're stuck inside it, meaning this move is deadly when in cave systems.");
			config.addDefault("Abilities.Earth.Collapse.Instructions", "Left click an earthbendable block. If there's space under that block, it will be collapsed. Alternatively, you can tap sneak to collapse multiple blocks at a time.");
			config.addDefault("Abilities.Earth.Collapse.DeathMessage", "{victim} was suffocated by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthArmor.Description", "This ability encases the Earthbender in armor, giving them protection. It is a fundamental earthbending technique that's used to survive longer in battles.");
			config.addDefault("Abilities.Earth.EarthArmor.Instructions", "Tap sneak while looking at an earthbendable block to bring those blocks towards you, forming earth armor. This ability will give you extra hearts and will be removed once those extra hearts are gone. You can disable this ability by holding sneak and left clicking with EarthArmor.");
			config.addDefault("Abilities.Earth.EarthBlast.Description", "EarthBlast is a basic yet fundamental earthbending ability. It allows you to deal rapid fire damage to your target to finish low health targets off or deal burst damage to them. Although it can be used at long range, it's potential is greater in close ranged comat.");
			config.addDefault("Abilities.Earth.EarthBlast.Instructions", "Tap sneak at an earthbendable block and then left click in a direction to send an earthblast. Additionally, you can left click again to change the direction of the earthblast. You can also redirect other earthbender's earth blast by left clicking. If the earth blast hits an entity it will deal damage and knockback.");
			config.addDefault("Abilities.Earth.EarthBlast.DeathMessage", "{victim} was broken apart by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthGrab.Description", "EarthGrab is one of the best defence abilities in an earthbender's aresenal. It allows you to trap someone who is running away so that you can catch up to someone, or quickly create a dome to protect yourself from incomming attacks. Although this ability is basic, it requires fast reactions to reach its full potential.");
			config.addDefault("Abilities.Earth.EarthGrab.Instructions", "\n" + "(Grab) To grab an entity, hold sneak and left click in the direction of the target. Earth will expand out and create an earth dome around the target, trapping them." + "\n" + "(Dome) To encase yourself in an earth dome, simply left click at an earthbendable block under you.");
			config.addDefault("Abilities.Earth.EarthTunnel.Description", "Earth Tunnel is a completely utility ability for earthbenders. It allows you to dig a hole that lowers players down while you continue the ability, create fast escape routes or just great for making your own cave systems.");
			config.addDefault("Abilities.Earth.EarthTunnel.Instructions", "Hold sneak while looking at an earthbendable block to tunnel the blocks away. If you release sneak or look at a block that isn't earthbendable, the ability will cancel.");
			config.addDefault("Abilities.Earth.Extraction.Description", "This ability allows metalbenders to extract the minerals from ore blocks. This ability is extremely useful for gathering materials as it has a chance to extract double or tripple the ores.");
			config.addDefault("Abilities.Earth.Extraction.Instructions", "Tap sneak while looking at an earthbendable ore to extract the ore.");
			config.addDefault("Abilities.Earth.LavaFlow.Description", "LavaFlow is an extremely advanced, and dangerous ability. It allows the earthbender to create pools of lava around them, or to solidify existing lava. This ability can be deadly when comboed with EarthGrab.");
			config.addDefault("Abilities.Earth.LavaFlow.Instructions", "\n" + "(Flow) Hold sneak and lava will begin expanding outwards. Once the lava has stopped expanding, you can release sneak. Additionally, if you tap sneak the lava you created will revert back to the earthbendable block." + "\n" + "(Lava Pool) Left click to slowly transform earthbendable blocks into a pool of lava." + "\n" + "(Solidify) Left click on lava to solidify it, turning it to stone.");
			config.addDefault("Abilities.Earth.LavaFlow.DeathMessage", "{victim} was caught in by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.EarthSmash.Description", "EarthSmash is an advanced earthbending technique that has lots of utility. It can be comboed with abilities such as Shockwave, but also be used for mobility and to produce high damage. EarthSmash is great for escaping when at low health.");
			config.addDefault("Abilities.Earth.EarthSmash.Instructions", "\n" + "(Smash) Hold sneak until particles appear, then release sneak while looking at an earthbendable block which will raise an earth boulder. Then, hold sneak while looking at this boulder to control it. Left click to send the bounder in the direction you're facing, damanging entities and knocking them back." + "\n" + "(Ride) After you have created an earth boulder, hold sneak and right click on the boulder to ride it. You will now ride the boulder in whatever direction you look. Additionally, you can ride the boulder by going on top of it and holding sneak. If you come into contact with an entity while riding the boulder, it will drag them along with you. If you left go of sneak, the ability will cancel.");
			config.addDefault("Abilities.Earth.EarthSmash.DeathMessage", "{victim} was crushed by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.MetalClips.Description", "MetalClips is an advanced metalbending ability that allows you to take control of a fight. It gives the metalbender the ability to control an entity, create space between them and a player and even added utility.");
			config.addDefault("Abilities.Earth.MetalClips.Instructions", "\n" + "(Clips) This ability requires iron ingots in your inventory. Left click to throw an ingot at an entity, dealing damage to them. This ingot will form into armor, wrapping itself around the entity. Once enough armor pieces are around the entity, you can then control them. To control them, hold sneak while looking at them and then they will be moved in the direction you look. Additionally, you can release sneak to throw them in the direction you're looking." + "\n" + "(Magnet) Hold sneak with this ability to pull iron ingots towards you.");
			config.addDefault("Abilities.Earth.MetalClips.DeathMessage", "{victim} was too slow for {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.RaiseEarth.Description", "RaiseEarth is a basic yet useful utility move. It has the potential to allow the earthbender to create great escape routes by raising earth underneath them to propell themselves upwards. It also offers synergy with other moves, such as shockwave. RaiseEarth is often used to block incoming abilities.");
			config.addDefault("Abilities.Earth.RaiseEarth.Instructions", "\n" + "(Pillar) To raise a pillar of earth, left click on an earthbendable block." + "\n" + "(Wall) To raise a wall of earth, tap sneak on an earthbendable block.");
			config.addDefault("Abilities.Earth.Shockwave.Description", "Shockwave is one of the most powerful earthbending abilities. It allows the earthbender to deal mass damage to everyone around them and knock them back. It's extremely useful when fighting more than one target or if you're surrounded by mobs.");
			config.addDefault("Abilities.Earth.Shockwave.Instructions", "Hold sneak until you see particles and then release sneak to send a wave of earth outwards, damaging and knocking entities back that it collides with. Additionally, instead of releasing sneak you can send a cone of earth forwards by left clicking. If you are on the Shockwave slot and you fall from a great height, your Shockwave will automatically activate.");
			config.addDefault("Abilities.Earth.Shockwave.DeathMessage", "{victim} was blown away by {attacker}'s {ability}");
			config.addDefault("Abilities.Earth.SandSpout.Description", "SandSpout is a core move for travelling, evasion, and mobility for sandbenders. It's extremely useful to gain a height advantage.");
			config.addDefault("Abilities.Earth.Sandspout.Instructions", "This ability will only work while you are on a sand block. Simply left click to create a sand spout underneath you. Then, hold spacebar to raise yourself upwards or hold sneak to go downwards. Left click again to disable this ability.");
			config.addDefault("Abilities.Earth.Tremorsense.Description", "This is a pure utility ability for earthbenders. If you are in an area of low-light and are standing on top of an earthbendable block, this ability will automatically turn that block into glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out as you have lost contact with the earth and cannot 'see' until you can touch earth again. Additionally, if you click with this ability selected, smoke will appear above nearby earth with pockets of air beneath them.");
			config.addDefault("Abilities.Earth.Tremorsense.Instructions", "Simply left click while on an earthbendable block.");
			config.addDefault("Abilities.Earth.Passive.DensityShift.Description", "DensityShift is a passive ability which allows earthbenders to make a firm landing negating all fall damage on any earthbendable surface.");
			config.addDefault("Abilities.Earth.Passive.FerroControl.Description", "FerroControl is a passive ability which allows metalbenders to simply open and close iron doors by sneaking.");

			config.addDefault("Commands.Help.Elements.Fire", "Fire is the element of power. Firebenders focus on destruction and incineration. Their abilities are pretty straight forward: set things on fire. They do have a bit of utility however, being able to make themselves un-ignitable, extinguish large areas, cook food in their hands, extinguish large areas, small bursts of flight, and then comes the abilities to shoot fire from your hands.\nFirebenders can chain their abilities into combos, type /b help FireCombos for more information.");
			config.addDefault("Abilities.Fire.Blaze.Description", "Blaze is a basic firebending technique that can be extremely deadly if used right. It's useful to stop people from chasing you or to create space between you and other players..");
			config.addDefault("Abilities.Fire.Blaze.Instructions", "Left click to send an arc of fire in the direction you're facing that will burn entities in its path. Additionally, you can tap sneak to send a blaze all around you.");
			config.addDefault("Abilities.Fire.Blaze.DeathMessage", "{victim} was burned alive by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.Combustion.Description", "Combustion is a special firebending technique that's extremely deadly. It allows you to create a powerful blast to deal immense damage to players at long range.");
			config.addDefault("Abilities.Fire.Combustion.Instructions", "Tap sneak to send a combustion out in the direction you're looking. It will explode on impact, or you can left click to manually expload it. This deals damage to players who are in radius of the blast.");
			config.addDefault("Abilities.Fire.Combustion.DeathMessage", "{victim} was shot down by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireBlast.Description", "FireBlast is the most fundamental bending technique of a firebender. It allows the firebender to create mass amounts of fire blasts to constantly keep damaging an entity. It's great for rapid fire successions to deal immense damage.");
			config.addDefault("Abilities.Fire.FireBlast.Instructions", "\n" + "(Ball) Left click to send out a ball of fire that will deal damage and knockback entities it hits. Additionally, this ability can refuel furnace power if the blast connects with a furnace." + "\n" + "(Blast) Hold sneak until you see particles and then release sneak to send out a powerful fire blast outwards. This deals damage and knocks back anyone it hits, while exploding on impact.");
			config.addDefault("Abilities.Fire.FireBlast.DeathMessage", "{victim} was burnt by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireBurst.Description", "FireBurst is a very powerful firebending ability. " + "FireBurst is an advanced firebending technique that has a large range and the potential to deal immense damage. It's incredibly useful when surrounded by lots of mobs, to damage them all at once.");
			config.addDefault("Abilities.Fire.FireBurst.Instructions", "Hold sneak until you see particles and then release sneak to send out a sphere of fire expanding outwards, damaging anything it hits. Additionally, you can left click instead of releasing sneak to send the fire burst into one direction only.");
			config.addDefault("Abilities.Fire.FireBurst.DeathMessage", "{victim} was blown apart by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireJet.Description", "FireJet is a fundamental utility move for firebenders. It allows the firebender to blast fire behind them to propel them forward, which can prevent them from taking fall damage or to escape from deadly situations.");
			config.addDefault("Abilities.Fire.FireJet.Instructions", "Left click to propel yourself in the direction you're looking. Additionally, left click while flying to cancel the jet.");
			config.addDefault("Abilities.Fire.FireShield.Description", "FireShield is a basic defensive ability that allows a firebender to block projectiles or other bending abilities. It's useful while fighting off skeletons, or while trying to block bending abilities at low health.");
			config.addDefault("Abilities.Fire.FireShield.Instructions", "Hold sneak to create a fire shield around you that will block projectiles and other bending abilities. Additionally, left click to create a temporary fire shield. If entities step inside this fire shield, they will be ignited.");
			config.addDefault("Abilities.Fire.FireShield.DeathMessage", "{victim} scorched theirself on {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.FireManipulation.Description", "FireManipulation is an extremely advanced and unique Firebending technique that allows the bender to create fire and manipulate it to block incoming attacks. You can also manipulate the fire you create to be used as an offence ability.");
			config.addDefault("Abilities.Fire.FireManipulation.Instructions", "Stream: Hold sneak and move your cursor around to create a fire where you look, blocking incoming attacks. Once you've created enough fire, left click to send the fire stream outwards, damaging anything it comes into contact with.");
			config.addDefault("Abilities.Fire.FireManipulation.DeathMessage", "{victim} scorched theirself on {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.HeatControl.Description", "HeatControl is a fundamental firebending technique that allows the firebender to control and manipulate heat. This ability is extremely useful for ensuring that you're protected from your own fire and fire from that of other firebenders. It's also offers utility by melting ice or cooking food.");
			config.addDefault("Abilities.Fire.HeatControl.Instructions", "\n" + "(Melt) To melt ice, simply left click while looking at ice." + "\n" + "(Solidify) To solidify lava, hold sneak while looking at lava while standing still and it will start to solidify the lava pool you're looking at." + "\n" + "(Extinguish) To extinguish nearby fire or yourself, simply tap sneak." + "\n" + "(Cook) To cook food, place the raw food on your HeatControl slot and hold sneak. The food will then begin to cook.");
			config.addDefault("Abilities.Fire.Illumination.Description", "Illumination is a basic firebending technique that allows firebenders to manipulate their fire to create a light source. This ability will automatically activate when you're in low light.");
			config.addDefault("Abilities.Fire.Illumination.Instructions", "Left click to enable. Additionally, left click to disable.");
			config.addDefault("Abilities.Fire.Lightning.Description", "Lightning is an advanced firebending technique. It allows you to create lightning and manipulate it towards a target to deal immense damage.");
			config.addDefault("Abilities.Fire.Lightning.Instructions", "\n" + "(Lightning) Hold sneak to create lightning until particles appear, then release sneak to send lightning in the direction you're looking. This deals damage to entities that it hits and has a chance to stun them for a short duration." + "\n" + "(Redirection) When someone has fired a lightning strike at you, you can hold sneak to absorb this lightning and then release sneak to fire it back.");
			config.addDefault("Abilities.Fire.Lightning.DeathMessage", "{victim} was electrocuted by {attacker}'s {ability}");
			config.addDefault("Abilities.Fire.WallOfFire.Description", "WallOfFire is an advanced firebending technique that can be used aggressively or defensively. It's incredibly useful when trying to block off opponents from chasing you or to back them into corners.");
			config.addDefault("Abilities.Fire.WallOfFire.Instructions", "Left click to create a fire wall at the location you clicked. This fire wall will damage entities that run into it and deal knockback.");
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
			config.addDefault("Abilities.Chi.AcrobatStance.Description", "AcrobatStance gives a Chiblocker a higher probability of blocking a Bender's Chi while granting them a Speed and Jump Boost. It also increases the rate at which the hunger bar depletes.");
			config.addDefault("Abilities.Chi.AcrobatStance.Instructions",  "To use, simply left click to activate this stance. Left click once more to deactivate it.");
			config.addDefault("Abilities.Chi.HighJump.Description", "HighJump gives the Chiblocker the ability to leap into the air. This ability is used for mobility, and is often used to dodge incoming attacks.");
			config.addDefault("Abilities.Chi.HighJump.Instructions", "To use, simply left click while standing on the ground.");
			config.addDefault("Abilities.Chi.Smokescreen.Description", "Smokescreen, if used correctly, can serve as a defensive and offensive ability for Chiblockers. When used, a smoke bomb is fired which will blind anyone within a small radius of the explosion, allowing you to either get away, or move in for the kill.");
			config.addDefault("Abiltiies.Chi.Smokescreen.Instructions", "Left click and a smoke bomb will be fired in the direction you're looking.");
			config.addDefault("Abilities.Chi.WarriorStance.Description", "WariorStance is an advanced chiblocker technique that gives the chiblocker increased damage but makes them a tad more vulnerable. This ability is useful when finishing off weak targets.");
			config.addDefault("Abilities.Chi.WarriorStance.Instructions", "Left click to activate the warrior stance mode. Additionally, left click to disable it.");
			config.addDefault("Abilities.Chi.Paralyze.Description", "Paralyzes the target, making them unable to do anything for a short period of time as they will be paralyzed where they're stood. ");
			config.addDefault("Abilities.Chi.Paralyze.Instructions", "Punch a player to paralyze them.");
			config.addDefault("Abilities.Chi.RapidPunch.Description", "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch. This has a short cooldown.");
			config.addDefault("Abilities.Chi.RapidPunch.Instructions", "Punch a player to deal massive damage.");
			config.addDefault("Abilities.Chi.RapidPunch.DeathMessage", "{victim} took all the hits against {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.QuickStrike.Description", "QuickStrike enables a chiblocker to quickly strike an enemy, potentially blocking their chi.");
			config.addDefault("Abilities.Chi.QuickStrike.Instructions", "Left click on a player to quick strike them.");
			config.addDefault("Abilities.Chi.QuickStrike.DeathMessage", "{victim} was struck down by {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.SwiftKick.Description", "SwiftKick allows a chiblocker to swiftly kick an enemy, potentially blocking their chi.");
			config.addDefault("Abilities.Chi.SwiftKick.Instructions", "Jump and left click on a player to swift kick them.");
			config.addDefault("Abilities.Chi.SwiftKick.DeathMessage", "{victim} was kicked to the floor by {attacker}'s {ability}");
			config.addDefault("Abilities.Chi.Combo.Immobilize.Description", "Immobilizes the opponent for several seconds.");
			config.addDefault("Abilities.Chi.Passive.ChiAgility.Description", "ChiAgility is a passive ability which enables chiblockers to run faster and jump higher.");
			config.addDefault("Abilities.Chi.Passive.ChiSaturation.Description", "ChiSaturation is a passive ability which causes chiblockers' hunger to deplete at a slower rate.");
			config.addDefault("Abilities.Chi.Passive.Acrobatics.Description", "Acrobatics is a passive ability which negates all fall damage based on a percent chance.");

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
			earthBlocks.add("GRASS_PATH");

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
			config.addDefault("Properties.RegionProtection.Kingdoms", true);
			config.addDefault("Properties.RegionProtection.CacheBlockTime", 5000);

			config.addDefault("Properties.Air.CanBendWithWeapons", false);
			config.addDefault("Properties.Air.Particles", "spell");
			config.addDefault("Properties.Air.PlaySound", true);
			config.addDefault("Properties.Air.Sound", "ENTITY_CREEPER_HURT");

			config.addDefault("Properties.Water.CanBendWithWeapons", true);
			config.addDefault("Properties.Water.IceBlocks", iceBlocks);
			config.addDefault("Properties.Water.PlantBlocks", plantBlocks);
			config.addDefault("Properties.Water.SnowBlocks", snowBlocks);
			config.addDefault("Properties.Water.NightFactor", 1.5);
			config.addDefault("Properties.Water.FullMoonFactor", 1.75);
			config.addDefault("Properties.Water.PlaySound", true);
			config.addDefault("Properties.Water.WaterSound", "BLOCK_WATER_AMBIENT");
			config.addDefault("Properties.Water.IceSound", "ITEM_FLINTANDSTEEL_USE");
			config.addDefault("Properties.Water.PlantSound", "BLOCK_GRASS_STEP");

			config.addDefault("Properties.Earth.RevertEarthbending", true);
			config.addDefault("Properties.Earth.SafeRevert", true);
			config.addDefault("Properties.Earth.RevertCheckTime", 300000);
			config.addDefault("Properties.Earth.CanBendWithWeapons", true);
			config.addDefault("Properties.Earth.EarthBlocks", earthBlocks);
			config.addDefault("Properties.Earth.MetalBlocks", metalBlocks);
			config.addDefault("Properties.Earth.SandBlocks", sandBlocks);
			config.addDefault("Properties.Earth.MetalPowerFactor", 1.5);
			config.addDefault("Properties.Earth.PlaySound", true);
			config.addDefault("Properties.Earth.EarthSound", "ENTITY_GHAST_SHOOT");
			config.addDefault("Properties.Earth.MetalSound", "ENTITY_IRONGOLEM_HURT");
			config.addDefault("Properties.Earth.SandSound", "BLOCK_SAND_BREAK");

			config.addDefault("Properties.Fire.CanBendWithWeapons", true);
			config.addDefault("Properties.Fire.DayFactor", 1.25);
			config.addDefault("Properties.Fire.PlaySound", true);
			config.addDefault("Properties.Fire.FireGriefing", false);
			config.addDefault("Properties.Fire.RevertTicks", 12000L);
			config.addDefault("Properties.Fire.FireSound", "BLOCK_FIRE_AMBIENT");
			config.addDefault("Properties.Fire.CombustionSound", "ENTITY_FIREWORK_BLAST");

			config.addDefault("Properties.Chi.CanBendWithWeapons", true);

			ArrayList<String> disabledWorlds = new ArrayList<String>();
			disabledWorlds.add("TestWorld");
			disabledWorlds.add("TestWorld2");
			config.addDefault("Properties.DisabledWorlds", disabledWorlds);

			config.addDefault("Abilities.Avatar.AvatarState.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.Cooldown", 7200000);
			config.addDefault("Abilities.Avatar.AvatarState.Duration", 480000);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Speed.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.Speed.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Enabled", true);
			config.addDefault("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Power", 3);
			config.addDefault("Abilities.Avatar.AvatarState.PowerMultiplier", 2);
			config.addDefault("Abilities.Avatar.AvatarState.Sound", "BLOCK_ANVIL_LAND");

			config.addDefault("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Entities", 4.5);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirBlast.Push.Self", 4.0);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirSpout.Height", 26);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirSuction.Push", 3.5);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirSwipe.Cooldown", 1000);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirSwipe.Damage", 4.5);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirSwipe.Push", 1.0);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirBurst.ChargeTime", 1000);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirBurst.Damage", 3);
			config.addDefault("Abilities.Avatar.AvatarState.Air.AirShield.IsAvatarStateToggle", true);
			config.addDefault("Abilities.Avatar.AvatarState.Air.Suffocate.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Air.Suffocate.ChargeTime", 1000);
			config.addDefault("Abilities.Avatar.AvatarState.Air.Suffocate.Damage", 3);
			config.addDefault("Abilities.Avatar.AvatarState.Air.Suffocate.Range", 16);

			config.addDefault("Abilities.Avatar.AvatarState.Earth.Catapult.MaxDistance", 80);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Catapult.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftCooldown", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickLavaCooldown", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickLandCooldown", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftPlatformRadius", 2);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ClickRadius", 10);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.LavaFlow.ShiftRadius", 12);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.MetalClips.Cooldown", 2000);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.MetalClips.Range", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.MetalClips.CrushDamage", 3);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Column.Height", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Wall.Height", 16);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Wall.Width", 16);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Collapse.Column.Height", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Collapse.Wall.Height", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthArmor.Cooldown", 2000);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthArmor.GoldHearts", 6);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthBlast.Cooldown", 500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthBlast.Damage", 5);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthGrab.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthGrab.Height", 10);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Shockwave.Range", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Shockwave.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Shockwave.ChargeTime", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Shockwave.Damage", 5);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.Shockwave.Knockback", 2);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.SelectRange", 16);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.GrabRange", 16);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.ChargeTime", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.Damage", 7);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.Knockback", 4.5);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.FlightSpeed", 1.0);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.FlightTimer", 10000);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthSmash.ShootRange", 30);
			config.addDefault("Abilities.Avatar.AvatarState.Earth.EarthTunnel.Radius", 0.5);

			config.addDefault("Abilities.Avatar.AvatarState.Fire.Blaze.Ring.Range", 14);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireJet.IsAvatarStateToggle", true);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.Lightning.ChargeTime", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.Lightning.Damage", 6);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.Lightning.Cooldown", 1000);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireBurst.ChargeTime", 1);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireBurst.Damage", 3);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireBurst.Cooldown", 0);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireBlast.Charged.ChargeTime", 1500);
			config.addDefault("Abilities.Avatar.AvatarState.Fire.FireBlast.Charged.Damage", 5);

			config.addDefault("Abilities.Avatar.AvatarState.Water.Surge.Wall.Radius", 4);
			config.addDefault("Abilities.Avatar.AvatarState.Water.Surge.Wave.Radius", 20);
			config.addDefault("Abilities.Avatar.AvatarState.Water.WaterManipulation.Damage", 5);

			config.addDefault("Abilities.Air.Passive.Factor", 0.3);
			config.addDefault("Abilities.Air.Passive.AirAgility.Enabled", true);
			config.addDefault("Abilities.Air.Passive.AirAgility.JumpPower", 3);
			config.addDefault("Abilities.Air.Passive.AirAgility.SpeedPower", 2);
			config.addDefault("Abilities.Air.Passive.AirSaturation.Enabled", true);
			config.addDefault("Abilities.Air.Passive.GracefulDescent.Enabled", true);

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
			config.addDefault("Abilities.Air.Flight.Cooldown", 5000);
			config.addDefault("Abilities.Air.Flight.Speed", 1);
			config.addDefault("Abilities.Air.Flight.MaxHits", 4);
			config.addDefault("Abilities.Air.Flight.MaxDuration", 0);

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

			config.addDefault("Abilities.Water.Passive.FastSwim.Enabled", true);
			config.addDefault("Abilities.Water.Passive.FastSwim.SpeedFactor", 0.7);
			config.addDefault("Abilities.Water.Passive.Hydrosink.Enabled", true);

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
			config.addDefault("Abilities.Water.HealingWaters.Cooldown", 0);
			config.addDefault("Abilities.Water.HealingWaters.Range", 5);
			config.addDefault("Abilities.Water.HealingWaters.Interval", 750);
			config.addDefault("Abilities.Water.HealingWaters.ChargeTime", 1000);
			config.addDefault("Abilities.Water.HealingWaters.Power", 1);
			config.addDefault("Abilities.Water.HealingWaters.Duration", 70);
			config.addDefault("Abilities.Water.HealingWaters.EnableParticles", true);

			config.addDefault("Abilities.Water.IceBlast.Enabled", true);
			config.addDefault("Abilities.Water.IceBlast.Damage", 3);
			config.addDefault("Abilities.Water.IceBlast.Range", 20);
			config.addDefault("Abilities.Water.IceBlast.DeflectRange", 3);
			config.addDefault("Abilities.Water.IceBlast.CollisionRadius", 1.5);
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
			config.addDefault("Abilities.Water.IceSpike.Blast.CollisionRadius", 1.5);
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
			config.addDefault("Abilities.Water.PhaseChange.SourceRange", 7);
			config.addDefault("Abilities.Water.PhaseChange.Freeze.Cooldown", 500);
			config.addDefault("Abilities.Water.PhaseChange.Freeze.Radius", 3);
			config.addDefault("Abilities.Water.PhaseChange.Freeze.Depth", 1);
			config.addDefault("Abilities.Water.PhaseChange.Freeze.ControlRadius", 25);
			config.addDefault("Abilities.Water.PhaseChange.Melt.Cooldown", 2000);
			config.addDefault("Abilities.Water.PhaseChange.Melt.Speed", 8.0);
			config.addDefault("Abilities.Water.PhaseChange.Melt.Radius", 7);
			config.addDefault("Abilities.Water.PhaseChange.Melt.AllowFlow", true);

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
			config.addDefault("Abilities.Water.Surge.Wave.IceRevertTime", 60000);

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
			config.addDefault("Abilities.Water.Torrent.Revert", true);
			config.addDefault("Abilities.Water.Torrent.RevertTime", 60000);
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
			config.addDefault("Abilities.Water.WaterBubble.Radius", 4.0);
			config.addDefault("Abilities.Water.WaterBubble.Speed", 0.5);
			config.addDefault("Abilities.Water.WaterBubble.ClickDuration", 2000L);
			config.addDefault("Abilities.Water.WaterBubble.MustStartAboveWater", false);
			
			config.addDefault("Abilities.Water.WaterManipulation.Enabled", true);
			config.addDefault("Abilities.Water.WaterManipulation.Damage", 3.0);
			config.addDefault("Abilities.Water.WaterManipulation.Range", 25);
			config.addDefault("Abilities.Water.WaterManipulation.SelectRange", 16);
			config.addDefault("Abilities.Water.WaterManipulation.CollisionRadius", 1.5);
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
			config.addDefault("Abilities.Water.WaterCombo.IceWave.RevertSphere", true);
			config.addDefault("Abilities.Water.WaterCombo.IceWave.RevertSphereTime", 30000L);

			config.addDefault("Abilities.Earth.Passive.Duration", 2500);
			config.addDefault("Abilities.Earth.Passive.DensityShift.Enabled", true);
			config.addDefault("Abilities.Earth.Passive.FerroControl.Enabled", true);

			config.addDefault("Abilities.Earth.Catapult.Enabled", true);
			config.addDefault("Abilities.Earth.Catapult.Cooldown", 7000);
			config.addDefault("Abilities.Earth.Catapult.StageTimeMult", 2.0);
			config.addDefault("Abilities.Earth.Catapult.Angle", 45);
			config.addDefault("Abilities.Earth.Catapult.CancelWithAngle", false);

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
			config.addDefault("Abilities.Earth.EarthArmor.GoldHearts", 4);
			config.addDefault("Abilities.Earth.EarthArmor.Cooldown", 17500);
			config.addDefault("Abilities.Earth.EarthArmor.MaxDuration", 20000);

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
			config.addDefault("Abilities.Earth.EarthBlast.CollisionRadius", 1.5);

			config.addDefault("Abilities.Earth.EarthGrab.Enabled", true);
			config.addDefault("Abilities.Earth.EarthGrab.SelectRange", 20);
			config.addDefault("Abilities.Earth.EarthGrab.Height", 6);
			config.addDefault("Abilities.Earth.EarthGrab.Cooldown", 2000);

			config.addDefault("Abilities.Earth.EarthTunnel.Enabled", true);
			config.addDefault("Abilities.Earth.EarthTunnel.MaxRadius", 1);
			config.addDefault("Abilities.Earth.EarthTunnel.Range", 10);
			config.addDefault("Abilities.Earth.EarthTunnel.Radius", 0.25);
			config.addDefault("Abilities.Earth.EarthTunnel.Revert", true);
			config.addDefault("Abilities.Earth.EarthTunnel.DropLootIfNotRevert", false);
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
			config.addDefault("Abilities.Fire.FireBlast.CollisionRadius", 1.5);
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

			config.addDefault("Abilities.Fire.FireManipulation.Stream.Cooldown", 12000);
			config.addDefault("Abilities.Fire.FireManipulation.Stream.Range", 50);
			config.addDefault("Abilities.Fire.FireManipulation.Stream.Damage", 2);
			config.addDefault("Abilities.Fire.FireManipulation.Stream.Speed", 0.75);
			config.addDefault("Abilities.Fire.FireManipulation.Stream.Particles", 50);
			
			config.addDefault("Abilities.Fire.FireManipulation.Shield.Cooldown", 6000);
			config.addDefault("Abilities.Fire.FireManipulation.Shield.Range", 4);
			config.addDefault("Abilities.Fire.FireManipulation.Shield.Damage", 1);
			config.addDefault("Abilities.Fire.FireManipulation.Shield.MaxDuration", 5000L);
			config.addDefault("Abilities.Fire.FireManipulation.Shield.Particles", 12);

			config.addDefault("Abilities.Fire.FireShield.Enabled", true);
			config.addDefault("Abilities.Fire.FireShield.Radius", 3);
			config.addDefault("Abilities.Fire.FireShield.DiscRadius", 1.5);
			config.addDefault("Abilities.Fire.FireShield.Duration", 1000);
			config.addDefault("Abilities.Fire.FireShield.Cooldown", 500);
			config.addDefault("Abilities.Fire.FireShield.Interval", 100);
			config.addDefault("Abilities.Fire.FireShield.FireTicks", 2);

			config.addDefault("Abilities.Fire.HeatControl.Enabled", true);
			config.addDefault("Abilities.Fire.HeatControl.Cook.Interval", 1000);
			config.addDefault("Abilities.Fire.HeatControl.Extinguish.Cooldown", 5000);
			config.addDefault("Abilities.Fire.HeatControl.Extinguish.Radius", 6);
			config.addDefault("Abilities.Fire.HeatControl.Melt.Range", 15);
			config.addDefault("Abilities.Fire.HeatControl.Melt.Radius", 5);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.MaxRadius", 10);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.Range", 7);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.Revert", true);
			config.addDefault("Abilities.Fire.HeatControl.Solidify.RevertTime", 120000);

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

			config.addDefault("Abilities.Chi.Passive.Acrobatics.Enabled", true);
			config.addDefault("Abilities.Chi.Passive.Acrobatics.FallReductionFactor", 0.5);
			config.addDefault("Abilities.Chi.Passive.FallReductionFactor", 0.5);
			config.addDefault("Abilities.Chi.Passive.ChiAgility.Enabled", true);
			config.addDefault("Abilities.Chi.Passive.ChiAgility.JumpPower", 1);
			config.addDefault("Abilities.Chi.Passive.ChiAgility.SpeedPower", 1);
			config.addDefault("Abilities.Chi.Passive.ChiSaturation.Enabled", true);
			config.addDefault("Abilities.Chi.Passive.ChiSaturation.ExhaustionFactor", 0.3);
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
			config.addDefault("Abilities.Chi.QuickStrike.Damage", 2);
			config.addDefault("Abilities.Chi.QuickStrike.Cooldown", 3000);
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
