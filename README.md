# ProjectKorra | Fork Roku

![Core Icon](https://i.imgur.com/0mWZWFT.png)

## About Fork Roku

Fork Roku is a fork of the popular plugin [ProjectKorra](https://www.spigotmc.org/resources/projectkorra.12071/). It contains a number of features and changes that are not available in the original plugin.

The development team are going in the wrong direction with ProjectKorra. This will continue the plugin with the original code, to support all existing addons, while making changes that will benefit the plugin and its community.

## Changes from ProjectKorra

- Added hex colors for each subelement
- Added a cooldown command to view/set/reset cooldowns for a player
- Made the config generate blocks based on the current server version
- Better supports 1.16
- Removed autoannouncer

### Fixes
- Fixed the server crashing when an ability's range is 0
- Fixed addons registering their listeners twice on /b reload
- Fixed addon ability jars being locked so they couldn't be deleted while the server was on (excluding Linux, which doesn't care if it SHOULD delete it or not)
- Fixed presets halting main thread when being created (caused a lot of lag)
- Fixed presets halting main thread when being deleted
- Fixed preset tabbing not working for binding and deleting
- Fixes BendingBoard IllegalStateException thrown when team is unregistered twice
- Fixed Extraction not working on 1.16 servers
- Fixed cooldowns halting the main thread when saving (and causing a lot of lag)
- Fixed ALL cooldowns being saved to the database
- Fixed bending boards showing up in disabled worlds when you log in
- Fixed bending toggle reminder not being translatable
- Fixed "Proper Usage: xxx" in commands being untranslatable

