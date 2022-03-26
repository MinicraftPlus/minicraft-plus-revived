# Minicraft Plus Revived
![Minicraft+](https://user-images.githubusercontent.com/37084190/138313821-75ac3112-7044-45c1-bdbb-d89f2333c2c0.png)

Minicraft+ is a modded version of Minicraft that adds many more features to the original version. The original Minicraft game was made by Markus "Notch" Persson in the Ludum Dare 22 contest.  To learn more about Minicraft take a look at [playminicraft.com](https://www.playminicraft.com), or talk to the community at the [Discord](https://discord.me/minicraft).

Check the [releases](https://github.com/minicraftplus/minicraft-plus-revived/releases) page to download the latest version, or older versions.

## Major features
* More game modes, including:
  * Creative
  * Hardcore
  * Score
  * Survival
* Saving and loadings
* Respawn
* World creation and management options, such as name, size, and terrain type of world, and rename, delete, and copy world
* Multiplayer mode and an account system (Now supported by [El-Virus](https://www.github.com/ElVir-Software/minicraft-plus-online))
* More mobs
* More tiles
* More tools, such as claymores and torches
* More items, such as potions and buckets
* Ability to drop items
* Personal crafting menu
* Beds
* Mob spawners with loot
* Ruined structures with loot
* An expanded storyline: 4th dungeon level, and second boss
* Resizable screen size
* Key binding customization
* Tutorials
* Texture Packs
* Much more gradual lighting
* 4 Selectable built-in skins and custom skin support
* Support for several languages

## Current goals and ideas
Take a look at the [ideas](ideas/) folder or the [issues](https://github.com/minicraftplus/minicraft-plus-revived/issues) page.

## How to build/run
Because this project uses a build tool called gradle it is very easy to build or run the project from the source code.

1. Download the source code by clicking the green code button, and download it as a ZIP.
2. Extract the contents of the folder.
3. Open command prompt and enter `cd [folder_location]`, this will open the folder in the command prompt. 
4. Type `gradlew run` or `gradlew build` to run or build the program. This might take some time. If on unix, add "./" to the front.
   1. If you built the project, the jar file is found in `build/libs`
   2. If you get an error screaming that you're missing java. You need to [set up](https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html) your JAVA_HOME environment variable, or download a JDK if you haven't already.
