# Minicraft+ Version 2 Changelog

This changelog is related to all the Minicraft+ versions.
All notable changes to this project will be documented in this file.

## [2.2]

### [2.2.0]

#### Additions

+ Added quests and tutorial
+ Added obsidian knight as the second boss
+ Added limitation to inventories
+ Added limitation to stackable items
+ Added 4 new debug arguments
+ Added a toggle for HUD display
+ Added a toggle for simplified effect display
+ Added a new menu for creative mode
+ Added a few creative mode only items for tile placement

#### Changes

* Changed the display of world edit
* Changed the structure of resource pack
* Split the images of textures
* Changed the UI of crash popup
* Changed a better logging
* Improved the localization support
* With optional flag `--savedir`, if present, no longer is appended an extra '.playminicraft/mods/Minecraft Plus' to it
* Improved some textures
* Greatly improved the resource pack system
* Restructured the resources

#### Fixes

* Fixed seed random problem

## [2.1]

### [2.1.3]

#### Removals

- Removed option of changing skin in the options display because of a crash

#### Fixes

* Fixed localization not being loaded on first launch

### [2.1.2]

#### Fixes

* Fixed a critical game crashing cause by loading worlds

### [2.1.1]

#### Fixes

* Fixed air wizard spawner
* Fixed player spawning outside the map

### [2.1.0]

#### Additions

+ Added achievements
+ Added ability to copy, cut, and paste in menus
+ Added credits menu
+ Added the ability to use some ascii characters in world seed
+ Added ornate obsidian and ornate stone tiles
+ Added raw obsidian and stone tiles
+ Added a totem that will spawn the air wizard
+ Added resource packs
+ Added sound when harvesting plants
+ Re-added sparks
+ Re-added damage particles
+ Re-added item shadows
+ Re-added swimming animation
+ Re-added colored spawners

#### Changes

* Made gem tools purple
* Prevented animals and mobs from de-spawning while in view
* Prevented furniture from being pushed over stair tiles
* Shear loses durability when used on sheep
* Resource packs and skins menu now use the standardised menu look
* Pre-releases are now ignored from version check
* Saplings should no longer grow if an entity is on it
* Renamed shear to shears
* Made cloud cactus into an ore you can mine and use

#### Removals

- Removed texture packs
- Removed suits
- Removed second air wizard

#### Fixes

* Fixed crash when inputting certain characters into the inventory searcher
* Fixed crash when deleting world
* Fixed rendering of stair tiles in sky level
* Fixed rendering of cloud cactus in sky level
* Fixed time rendering for potions not following m:ss format
* Fixed some hits not hitting properly (0 attack damage)
* The world list should now update if copying or renaming worlds in-game

> ---------------------------------------------------------------------------------------
>
> Changed versioning system to be in compliance with https://semver.org/spec/v2.0.0.html
>
> ---------------------------------------------------------------------------------------

## [2.0.7]

### Additions

+ Added sheep shearing
+ Added shear tool
+ Added potatoes
+ Added baked potatoes as food
+ Added inventory searcher (F to toggle and Next/Back Page to seek)
+ Added fullscreen mode (default key F11)
+ Added Indonesian localization
+ Added Hungarian localization
+ Added SRV record support; service name is _minicraft (only accessable through code)
+ Added aggregate analytics for various game states and actions
+ Added new skin system with custom skin support
+ Added Built-in Contest Winners' Skins into the Skins Menu

### Changes

* Made zombies drop potatoes
* Sparks (the airwizard bullets) have been optimised
* Fixed picked up dungeon chests
* Tiles now connect to the edge of the world
* Farm tiles will no longer be destroyed if an item is dropped onto it
* Made crops grow slower
* Updated some textures; made wood, apples and gold apples look better
* Made sheep drop beef
* Picking up furniture no longer require stamina
* Mobs no longer target you and attack you when you are in creative mode
* You can only use axes on wood walls, wood doors, and wood floor
* Shears must be used to destroy wool tiles

### Removals

- Disabled multiplayer functionality

### Fixes

* Fixed sound not playing on certain actions

## [2.0.6]

### Additions

+ Added French localization
+ Added more buildings, including abandoned villages
+ Added paths, use a pickaxe on grass to make them
+ You can now craft obsidian walls and doors
+ Resource packs are now possible! Guide is on the wiki

### Changes

* Prevented players from getting stuck in skyholes with a cool animation
* Made the quick-save key configurable
* Let you place floors on clouds
* Fixed small line in the snake sprite that shouldn't be there
* Water and lava now form obsidian, and don't look like sand when they are next to each other
* Totally overhauled fishing!
* Creepers have been buffed
* Villages and spawners can no longer override stair barriers (to the sky or the dungeon)
* TNT can't break hard rock tiles or obsidian walls

### Removals

- Knights and Snakes only have 4 levels instead of 5

### Fixes

* Fixed the creeper's explosion to only hurt mobs if it actually explodes
* Menus that don't have frames don't render the title background

## [2.0.5]

### Additions

+ Added tool durability to all tools
+ Added window popup to display errors
+ Added Spanish localization
+ Added escape potion, which brings you one level closer to the surface from where it's used
+ Added an indicator to tell you what tile in item will be placed on

### Changes

* Updated door sprites and a couple others in minor ways
* Allowed creative player to walk outside cloud tiles in the sky
* Made hoe turn grass into dirt instead of straight to farmland; but with significantly higher chance of seeds
* Made some adjustments to some of the sprites for improved detail
* Made flowers spread grass to dirt like grass spreads grass to dirt, since a flower tile is technically grass too
* Separated directional input into movement and cursor, for player movement and menu selection respectively.
* Made sand behave like dirt when being placed
* Made items face the right way when you attack with them

### Fixes

* Fixed a couple more multiplayer bugs; also, as of dev6, players should stop crashing when changing levels
* Fixed some localization bugs
* Fixed bug with item crafting and reference
* Fixed bug with dungeon chests refilling on load
* Fixed bug with "Click to focus" text rendering
* Fixed bug with armor loading
* Fixed the wheat tile's background
* Fixed bug where creative player couldn't break a tile
* Fixed item data in chests

## [2.0.4]

### Additions

+ Added world seeding! You now have a reliable way of recreating a world... provided you used a seed in the first place, that is... Commands aren't quite here yet...
+ Added a splash screen! So it's pretty while it's loading.
+ Added an official logo! Curtesy of contributors on our discord server.
+ Added a new sprite for the log, so it will stop getting confused with planks.
+ Added an in-game, in-depth storyline guide.
+ Added an FPS slider.
+ Added keys being dropped back into the game... because apparently that feature disappeared somewhere.
+ Added player hunger to world save.
+ Added a version checker, so you know when new updates are available without having to look. It even opens the link for you. ;)

### Changes

* HUGE menu renovation! Menus will be changed forever. You'll see. It's pretty obvious.
* Added/fixed the hints that pop up when you try to place walls/floors/doors.
* Made default game mode Normal instead of Easy.
* Redid and fixed death chests; they now display time left above the chest, and you "pickup" everything in the chest by touching it. It also flashes red when there are < 20 seconds before disappearing.
* Changed beds in multiplayer, so that all players must be sleeping before the game speeds up.

### Removals

- Removed the "home" functionality.

### Fixes

* Fixed multiplayer bug where you could only deal 1 damage to things
* Fixed a long-standing bug that mainly affected dungeon chests and airwizards not appearing when they should, as well as mob spawner dungeons
* Fixed score mode in multiplayer

## [2.0.3]

### Additions

+ Added support for dedicated servers! With a CLI OS, meaning an OS without a GUI. This allows for more compatibility, not to mention more resources being able to be devoted to the game.
+ Added various server options, and a command help system, for dedicated servers.
+ Added feature to save the last ip address you entered when trying to join a multiplayer server
+ Added more error messages when connecting
+ Added the ability to drop an item to the ground with Q and shift-Q (shift-q drops the whole stack)
+ made it so you can transfer single items to and from chests, with the drop key.
+ Created an account system! you must now register on the playminicraft site to use multiplayer. Comes with offline mode.
+ added an fps limiter
+ Added back and fixed knockbacks, after removing them since they didn't work how I wanted.

### Changes

* Allowed torches to be placed on obsidian bricks
* Made various optimizations to the code

### Fixes

* Fixed item entity spawn positions and going through other tiles
* Fixed bugs with creative inventory, so it always has all items.
* Fixed issues with the pickup control in single player and multiplayer
* Fixed multiplayer client saving with the account system.

## [2.0.2]

### Additions

+ made TNT light other TNT
+ Made it so all mobs (not players obviously) despawn after a certain amount of time (will later be fixed so mobs close to the player don't despawn.
+ Made enchanter craftable.
+ Added "unknown" item to make it obvious when something has gone wrong; it has the requested name, but the sprite is a pink square.
+ Added F3 menu for the server instance of the game in multiplayer.

### Changes

* Switched the multiplayer protocol from UDP to TCP.
* edited coal drop amount
* made lots of edits to mob spawn rate, and mob count management

### Removals

- removed enchanter and workbench from starting inventory.
- removed the power glove. It's still used, but you can only use it by pressing the "pickup" key ("v" by default); you can't get it in your inventory.
- removed all the "frozen mobs" which kept appearing in multiplayer

### Fixes

* fixed problems with doors, floors, and walls (item drops, etc.)
* Fixed multiplayer attack rendering across clients; now shows attack, including the item a client is attacking with.
* Fixed creative inventory for clients in multiplayer.
* Fixed multiplayer client saving.
* Patched possible duplicate usernames glitch
* Fixed unlocked score mode times not appearing until you restarted the game; you shouldn't have to restart it now.

## [2.0.1]

### Additions

+ added more super-secret debug powers... mua ha ha...
+ added to F3 window: level mobcount + max mobcount, and name of current tile
+ Arrows now appear in your inventory

### Changes

* Bed spawning should now work; once you sleep in a bed, you will always spawn from the place that you were standing right before you got in bed, unless that spot later becomes rock, or something else a player can't walk through. The bed does not have to remain where it is; you can destroy it, and you will continue to spawn in the spot described above.
* This version should be compatible with 1.8 save files.
* Edited the mob spawning algorithm, so mobs should not spawn at such a crazy speed as before

* Renovated Multiplayer! It should now be much less laggy. Still not quite tolerable, but... better.
  + Added multiplayer feature: when hosting a server, a new window opens up for you to play as any other player, though your settings will be saved so that your should be able to continue as normal if you load the world as single player again. The server window lets you view the players connected, and change world difficulty and such.
  + Added multiplayer feature: the inventory and such data for people connected to the server is saved in a seperate file along with the computer's MAC address, so if they connect with that computer again, their inventory and stats should be restored.

### Fixes

* Creepers shouldn't be able to blow up stairs anymore
* Fixed crafting; correct number of items are used
* Fixed Save folder for non-windows users. You may notice a strange folder called "null" in the same folder as the minicraft jar. Move the ".playminicraft" folder inside to your home directory, and it should recognize it again. It may be hidden due to the "."

* Ensured that most all the normal world features work in multiplayer. However, I have not been able to do much testing with Client Player saves, so that might not work very well. Everything else should work, though; but the connection lag between computers may cause some wierd effects. Fixing this lag is my next goal.

## [2.0.0]

### Additions

+ MULTIPLAYER MODE now supported! (dun, dun-dun-dun!)
  * -*- Well... kind of. I got one way to work, but it's super laggy.

### Changes

* You disappear from the level when you get in bed, as you should
* you can now see the edges of the level
* crafting furniture no longer disappears upon loading a pre-existing world
* Stackable item count decreases as it should

[2.2]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.3...HEAD
[2.2.0]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.3...HEAD
[2.1]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.7...v2.1.3
[2.1.3]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.2...v2.1.3
[2.1.2]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.1...v2.1.2
[2.1.1]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.7...v2.1.0
[2.0.7]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.6...v2.0.7
[2.0.6]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.5...v2.0.6
[2.0.5]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.4...v2.0.5
[2.0.4]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.3...v2.0.4
[2.0.3]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.2...v2.0.3
[2.0.2]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.1...v2.0.2
[2.0.1]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v1.9.4...v2.0.0
