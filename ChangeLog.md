# Changelog

This changelog is related to all the Minicraft+ versions.
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html),
but some sections are changed to compliant this project.

## [2.3.0]

### Additions

* Added ornate wood tiles
* Added dye vat 
* Added 16 new flowers
* Added 16 dye colors
* Added 15 bed colors
* Added 10 wool colors
* Added a backup feature whenever using a development build

### Changes

* Disabled hardware acceleration by default
* Hardware acceleration is now a toggle in settings
* Screenshots will now be the size of the rendered view within the window
* Updated textures of wool items
* Updated textures of wool tiles

### Removals

* Removed argument flag `--no-hardware-acceleration`
* Removed screenshot size setting

### Fixes


## [2.2.1]

### Additions
* Added inventory capacity counter

### Fixes
* Prevent creepers from blowing up stairs
* Updated localisation
* Fixed Russian font
* Fixed performance issues
* Fixed corner rendering of some tiles
* Fixed overworld rendering as totally black at night
* Fixed some keys not bound to cursor mappings
* Fixed a wrong texture
* Fixed problems with screenshot feature

## [2.2.0]

### Additions

* Added quest system
* Added tutorials
* Added obsidian knight as the second boss (Air Wizard II replacement)
* Added limitation to inventories
* Added limitation to stackable items
* Added seven new debug arguments:
   `--debug-log-time`, `--debug-log-thread`, `--debug-log-trace`, `--debug-filelog-full`, `--debug-level`, `--debug-locale`, `--debug-unloc-tracing`
* Added a new argument for disabling hardware acceleration - `--no-hardware-acceleration`
* Added a toggle for HUD display
* Added a toggle for simplified effect display
* Added a new menu for creative mode
* Added a few creative mode only items for tile placement
* Added a standard PopupDisplay for all popups
* Added a crash handler and a better look of dialog.
* Added a simple ability for screenshots
* Added dynamic for sounds
* ReAdded sand footprint particle for sand
* Added characters to the font (`ÀÂÄÈÎÌÏÒÙÛÝ*«»£$&€§ªº`)
* Added world-wide seed
* Added controller support
* Added on-screen keyboard
* Added glass bottle and made potions return glass bottles when used
* Added a trigger to auto-enable hardware acceleration
* Added support for lower-cased letters
* Added item description menu
* Added fences
* Added editable signs

### Changes

* Changed the folder structure of resource packs
* Changed the UI of crash popup
* Changed the height requirement on skin images from 32 pixels to 16 pixels
* Move each texture in the sprite sheets to its own file
* If the application flag `--savedir` is present, '.playminicraft/mods/Minecraft Plus' will no longer be appended
* Updated all ore-related and metal-related textures
* Made the parent display render in the background
* Made recipes unlockable
* Made you reobtain your old clothes when putting on new clothing
* Made you reobtain empty bottles when a bottle of potion is consumed
* Made languages fallback to English
* Improved the tile place indicator
* Overhauled debugging actions
* Overhauled the farming system
* Changed the languaging setting menu
* Optimized CPU usage
* Reduced food stamina cost
* Made some strings lower-cased
* Updated spawning and despawning conditions
* Iron and gold now require 3 ores instead of 4 for crafting
* Renamed `assets/books/story_guide.txt` to `assets/books/game_guide.txt`
* Updated `assets/books/game_guide.txt` and `assets/books/instructions.txt`
* Sand footprints are now more accurately placed
* Optimized rendering
* Increased death chest despawn timer as follows:
  Easy: 450 secs, Normal: 300 secs, Hard: 150 secs

### Removals

* None (compared to 2.1.3)

### Fixes

* Fixed seed random problem
* Fixed rendering positioning problem of color code styled texts
* Fixed lights disappearing when out of screen
* Fixed animals and items destroying plants
* Fixed various old world loading crashes, including migration to the new destination
* Fixed possible crash when Air Wizard is not loaded
* Fixed minor loading bugs to dungeon chests
* Fixed performance issue in WaterTile ticking
* Fixed majority of audio issues

## [2.1.3]

### Removals

- Removed option of changing skin in the options display because of a crash

### Fixes

* Fixed localization not being loaded on first launch

## [2.1.2]

### Fixes

* Fixed a critical game crashing cause by loading worlds

## [2.1.1]

### Fixes

* Fixed air wizard spawner
* Fixed player spawning outside the map

## [2.1.0]

### Additions

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

### Changes

* Made gem tools purple
* Prevented animals and mobs from de-spawning while in view
* Prevented furniture from being pushed over stair tiles
* Shear loses durability when used on sheep
* Resource packs and skins menu now use the standardised menu look
* Pre-releases are now ignored from version check
* Saplings should no longer grow if an entity is on it
* Renamed shear to shears
* Made cloud cactus into an ore you can mine and use

### Removals

- Removed texture packs
- Removed suits
- Removed second air wizard

### Fixes

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

>     Minicraft+ version 2.0.7
>
>     + Added sheep shearing
>     + Added shear tool
>     + Added potatoes
>     + Added baked potatoes as food
>     + Made zombies drop potatoes
>     + Added inventory searcher (F to toggle and Next/Back Page to seek)
>     + Added fullscreen mode (default key F11)
>     + Added Indonesian localization
>     + Added Hungarian localization
>     + Added SRV record support; service name is _minicraft (only accessable through code)
>     + Added aggregate analytics for various game states and actions
>     + Added new skin system with custom skin support
>     + Added Built-in Contest Winners' Skins into the Skins Menu
>     * Sparks (the airwizard bullets) have been optimised
>     * Fixed picked up dungeon chests
>     * Tiles now connect to the edge of the world
>     * Farm tiles will no longer be destroyed if an item is dropped onto it
>     * Made crops grow slower
>     * Updated some textures; made wood, apples and gold apples look better
>     * Made sheep drop beef
>     * Picking up furniture no longer require stamina
>     * Mobs no longer target you and attack you when you are in creative mode
>     * Fixed sound not playing on certain actions
>     * You can only use axes on wood walls, wood doors, and wood floor
>     * Shears must be used to destroy wool tiles
>     - Disabled multiplayer functionality
>
>     Minicraft+ version 2.0.6
>
>     + Prevented players from getting stuck in skyholes with a cool animation
>     * Fixed the creeper's explosion to only hurt mobs if it actually explodes
>     * Made the quick-save key configurable
>     + Added French localization
>     + Added more buildings, including abandoned villages
>     * Let you place floors on clouds
>     * Fixed small line in the snake sprite that shouldn't be there
>     * Water and lava now form obsidian, and don't look like sand when they are next to each other
>     * Totally overhauled fishing!
>     + Added paths, use a pickaxe on grass to make them
>     + You can now craft obsidian walls and doors
>     + Resource packs are now possible! Guide is on the wiki
>     - Knights and Snakes only have 4 levels instead of 5
>     * Creepers have been buffed
>     * Villages and spawners can no longer override stair barriers (to the sky or the dungeon)
>     * TNT can't break hard rock tiles or obsidian walls
>     * Menus that don't have frames don't render the title background
>
>
>     Minicraft+ version 2.0.5
>
>     + Added tool durability to all tools
>     * Updated door sprites and a couple others in minor ways
>     * Fixed a couple more multiplayer bugs; also, as of dev6, players should stop crashing when changing levels
>     + Added window popup to display errors
>     + Added Spanish localization
>     * Fixed some localization bugs
>     * Fixed bug with item crafting and reference
>     * Fixed bug with dungeon chests refilling on load
>     * Allowed creative player to walk outside cloud tiles in the sky
>     * Made hoe turn grass into dirt instead of straight to farmland; but with significantly higher chance of seeds
>     * Fixed bug with "Click to focus" text rendering
>     * Fixed bug with armor loading
>     + Added escape potion, which brings you one level closer to the surface from where it's used
>     * Made some adjustments to some of the sprites for improved detail
>     * Made flowers spread grass to dirt like grass spreads grass to dirt, since a flower tile is technically grass too
>     + Added an indicator to tell you what tile in item will be placed on
>     + Separated directional input into movement and cursor, for player movement and menu selection respectively.
>     * Fixed the wheat tile's background
>     * Fixed bug where creative player couldn't break a tile
>     * Made sand behave like dirt when being placed
>     * Fixed item data in chests
>     * Made items face the right way when you attack with them
>
>
>     Minicraft+ version 2.0.4
>
>     * HUGE menu renovation! Menus will be changed forever. You'll see. It's pretty obvious.
>     + Added world seeding! You now have a reliable way of recreating a world... provided you used a seed in the first place, that is... Commands aren't quite here yet...
>     + Added a splash screen! So it's pretty while it's loading.
>     + Added an official logo! Curtesy of contributors on our discord server.
>     + Added a new sprite for the log, so it will stop getting confused with planks.
>     * Added/fixed the hints that pop up when you try to place walls/floors/doors.
>     + Added an in-game, in-depth storyline guide.
>     + Added an FPS slider.
>     * Made default game mode Normal instead of Easy.
>     * Redid and fixed death chests; they now display time left above the chest, and you "pickup" everything in the chest by touching it. It also flashes red when there are < 20 seconds before disappearing.
>     * Fixed multiplayer bug where you could only deal 1 damage to things
>     * Fixed a long-standing bug that mainly affected dungeon chests and airwizards not appearing when they should, as well as mob spawner dungeons
>     + Added keys being dropped back into the game... because apparently that feature disappeared somewhere.
>     + Added player hunger to world save.
>     - Removed the "home" functionality.
>     * Changed beds in multiplayer, so that all players must be sleeping before the game speeds up.
>     * Fixed score mode in multiplayer
>     + Added a version checker, so you know when new updates are available without having to look. It even opens the link for you. ;)
>
>
>     Minicraft+ version 2.0.3
>
>     + Added support for dedicated servers! With a CLI OS, meaning an OS without a GUI. This allows for more compatibility, not to mention more resources being able to be devoted to the game.
>     + Added various server options, and a command help system, for dedicated servers.
>     + Added feature to save the last ip address you entered when trying to join a multiplayer server
>     + Added more error messages when connecting
>     * Fixed item entity spawn positions and going through other tiles
>     + Added the ability to drop an item to the ground with Q and shift-Q (shift-q drops the whole stack)
>     + made it so you can transfer single items to and from chests, with the drop key.
>     * Fixed bugs with creative inventory, so it always has all items.
>     * Fixed issues with the pickup control in single player and multiplayer
>     + Created an account system! you must now register on the playminicraft site to use multiplayer. Comes with offline mode.
>     * Fixed multiplayer client saving with the account system.
>     + added an fps limiter
>     * Made various optimizations to the code
>     + Added back and fixed knockbacks, after removing them since they didn't work how I wanted.
>     + Allowed torches to be placed on obsidian bricks
>
>
>     Minicraft+ version 2.0.2
>
>     * Switched the multiplayer protocol from UDP to TCP.
>     * fixed problems with doors, floors, and walls (item drops, etc.)
>     + made TNT light other TNT
>     * edited coal drop amount
>     * made lots of edits to mob spawn rate, and mob count management
>     + Made it so all mobs (not players obviously) despawn after a certain amount of time (will later be fixed so mobs close to the player don't despawn.
>     + Made enchanter craftable.
>     - removed enchanter and workbench from starting inventory.
>     - removed the power glove. It's still used, but you can only use it by pressing the "pickup" key ("v" by default); you can't get it in your inventory.
>     + Added "unknown" item to make it obvious when something has gone wrong; it has the requested name, but the sprite is a pink square.
>     * Fixed multiplayer attack rendering across clients; now shows attack, including the item a client is attacking with.
>     * Fixed creative inventory for clients in multiplayer.
>     * Fixed multiplayer client saving.
>     - removed all the "frozen mobs" which kept appearing in multiplayer
>     * Fixed unlocked score mode times not appearing until you restarted the game; you shouldn't have to restart it now.
>     + Added F3 menu for the server instance of the game in multiplayer.
>     * Patched possible duplicate usernames glitch
>
>
>     Minicraft+ version 2.0.1
>
>     +* added more super-secret debug powers... mua ha ha...
>     + added to F3 window: level mobcount + max mobcount, and name of current tile
>     + Arrows now appear in your inventory
>     * Fixed crafting; correct number of items are used
>     * Creepers shouldn't be able to blow up stairs anymore
>     * Bed spawning should now work; once you sleep in a bed, you will always spawn from the place that you were standing right before you got in bed, unless that spot later becomes rock, or something else a player can't walk through. The bed does not have to remain where it is; you can destroy it, and you will continue to spawn in the spot described above.
>     * Fixed Save folder for non-windows users. You may notice a strange folder called "null" in the same folder as the minicraft jar. Move the ".playminicraft" folder inside to your home directory, and it should recognize it again. It may be hidden due to the "."
>     * This version should be compatible with 1.8 save files.
>     * Edited the mob spawning algorithm, so mobs should not spawn at such a crazy speed as before
>
>     * Renovated Multiplayer! It should now be much less laggy. Still not quite tolerable, but... better.
>     + Added multiplayer feature: when hosting a server, a new window opens up for you to play as any other player, though your settings will be saved so that your should be able to continue as normal if you load the world as single player again. The server window lets you view the players connected, and change world difficulty and such.
>     + Added multiplayer feature: the inventory and such data for people connected to the server is saved in a seperate file along with the computer's MAC address, so if they connect with that computer again, their inventory and stats should be restored.
>
>     * Ensured that most all the normal world features work in multiplayer. However, I have not been able to do much testing with Client Player saves, so that might not work very well. Everything else should work, though; but the connection lag between computers may cause some wierd effects. Fixing this lag is my next goal.
>
>
>     Minicraft+ version 2.0.0
>
>     +++ MULTIPLAYER MODE now supported! (dun, dun-dun-dun!)
>     -*- Well... kind of. I got one way to work, but it's super laggy.
>
>     *** mostly, just lots and lots of important bug fixes, such as:
>     * You disappear from the level when you get in bed, as you should
>     * you can now see the edges of the level
>     * crafting furniture no longer disappears upon loading a pre-existing world
>     * Stackable item count decreases as it should
>
>
>     ---------------------------------------------------------
>
>     Minicraft+ version 1.9.4
>
>     * Almost total revision of lighting; surface level now has lighting overlay sometimes, and the colors all around may be slightly different.
>     + Daylight now increases and decreases more smoothly; steadily goes from dark to light in the morning, and light to dark in the evening.
>
>     + Added Instant Health Potion! Instantly refills up to 5 hearts.
>     * changed death chest expire times; easy:30min, norm:10min, hard:2min
>     * Sped up sleeping game speed.
>     - Prevented Airwizard from being removed during daily entity clear.
>     * Increased Torch light radius; also, torches now work the same way on the surface as they do in the caves.
>     + Added missingTexture sprite.
>     * Changed the name of a couple items (was abbreviated).
>     + Wool slows down all mobs except you.
>     + Added some more help messages when trying do things that you might not know you can't do yet. (destroying hardrock, obsidian stair on lava level).
>     + Imposed a limit to the number of mobs that can spawn on a level, dependent on the level and the game difficulty.
>
>     + Added small tweak for creative mode: empty buckets will remain empty, so you can empty the ocean as much as you like. Full buckets remain full, as well.
>     + Additional creative mode tweak: "attacking" mob spawner with hand changes the level of mob it spawns. Also, you can pick up spawners in creative mode.
>     + Creative mode inventory remains full.
>
>     + Key controls displayed on (hopefully) all menus will always be correct. ;)
>     * Revised some key binding details to hopefully prevent inability to select anything. Also, added feature to reset all key bindings to default.
>
>     + You can now copy world saves.
>     + Added another confirm dialog before deleting worlds.
>     + Player score shown on-screen in score mode. TIP: as you probably don't know... you can unlock new time limits for score mode. unlock 10min by getting 1000 points in a 20min game, and unlock 2Hour by getting 100,000 points in a 1Hour game. (Hint: defeating the AirWizard gives you 100,000 points...)
>
>     Minicraft+ version 1.9.3
>
>     + added command line argument to change the save directory of the world files.
>     * revised input system to (hopefully) work on any language keyboard/computer.
>     + added back feature where mobs stay away during the first night.
>     * Reduced world load time from 2 seconds to about half a second, for me). It used to generate new levels needlessly, but now it doesn't.
>     + Loading has a progress bar. Though honestly, it doesn't need one.
>
>
>     Minicraft+ version 1.9.2
>
>     * HUGE renovations to the code; might not see many changes to functionality, but entirely new bugs are bound to crop up...
>     * renovated entities
>     * renovated menus, a bit
>     + added Key Binding Menu, finally! You can now customize key controls, and save them. Accessible from the title screen, and the pause menu.
>     + added spanish keyboard(/computer?) support... for the enter (and backspace) key... but that's all you need to access and change the key binding menu.
>     + Key bindings, sound, and autosave options are loaded on startup, in a special file that applies to all saves; like beating the second airwizard.
>     + world difficulty is saved for each world
>
>
>     Minicraft+ version 1.9.1
>
>     = Armor now functions as a damage buffer rather than extra hearts; you take damage after only a certain number of damage points accumulate.
>     + Armor lasts longer.
>       = Rate of hunger bar healing health depends on hunger; still happens when less than full health.
>     - removed player's restriction on movement while being hurt
>     + Added version number to save file
>     + Hunger bar now decreases in a better way (in my opinion)
>     # fixed all the bugs I could find!
>
>
>     Minicraft+ version 1.9
>
>     =Everything reimplemented
>     =stuff works
>     +Game window is resizable
>     +more entries in debug display
>     +added debug features
>     +key preferences... well, kinda. You can't really change them...
>     +airwizard suit status and key prefs saved with game file
>     +modifier keys supported (shift, alt, ctrl)
>     +added obsidian brick and door to creative inventory
>
>
>     ----- Noteable additions in v1.8 -----
>     * Spawners & underground mini-dungeons
>     * Airwizard's health appears above his head
>     * Added colored clothes to change the player's outfit. They are craftable in the loom, or dropped rarely by zombies.
>     * You can set the timer for score mode: 20m (default), 30m, 40m, or 1h
>     * you can unlock new score mode times with high enough scores: 10 min and 2 hours.
>     * "Airwizard II" boss shows up on the surface after you unlock all the locked chests
>     * Once you defeat the Air Wizard, you can wear his costume by going into the settings menu. This suit allows the player to walk on air tiles.
>
>     ----- Noteable additions in v1.7 -----
>     * Save/Load worlds
>     * Updated pause menu
>     * Potions
>     * Added locked chests in purple dungeon.
>     * Keys to open the chests are dropped by knights
>     * A death chest will appear if you die, allowing you to retrieve your stuff.
>     * Added an Iron Lantern at the entrance of the purple dungeon

[2.2.0]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.3...HEAD

[2.1.3]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.2...v2.1.3

[2.1.2]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.1...v2.1.2

[2.1.1]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.1.0...v2.1.1

[2.1.0]: https://github.com/MinicraftPlus/minicraft-plus-revived/compare/v2.0.7...v2.1.0
