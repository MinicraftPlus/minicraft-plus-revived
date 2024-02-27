[![Nightly build](https://github.com/MinicraftPlus/minicraft-plus-revived/actions/workflows/autobuild.yml/badge.svg?branch=main)](https://github.com/MinicraftPlus/minicraft-plus-revived/actions/workflows/autobuild.yml)
[![CodeQL](https://github.com/MinicraftPlus/minicraft-plus-revived/actions/workflows/codeql-analysis.yml/badge.svg?branch=main)](https://github.com/MinicraftPlus/minicraft-plus-revived/actions/workflows/codeql-analysis.yml)

# Minicraft+

![Minicraft+](https://user-images.githubusercontent.com/37084190/138313821-75ac3112-7044-45c1-bdbb-d89f2333c2c0.png)

Minicraft+ is an overhaul mod of Minicraft, a game made by Markus "Notch" Persson in the Ludum Dare 22 contest. To learn
more about Minicraft take a look at [playminicraft.com](https://www.playminicraft.com), talk to the community at
our [Discord](https://discord.me/minicraft), or check out
our [wiki.gg Wiki](https://minicraft.wiki.gg/wiki/).

Check the [releases](https://github.com/minicraftplus/minicraft-plus-revived/releases) page to download the latest
version, or older versions.

## Major features

* Four new gamemodes
    * Creative
    * Hardcore
    * Score
    * Survival
* Saving and loading
* Multiplayer mode and an account system (Now supported
  by [El-Virus](https://www.github.com/ElVir-Software/minicraft-plus-online))
* More mobs
* Personal crafting menu
* Beds
* Mob spawners with loot
* Ruined structures with loot
* An expanded storyline
* Key binding customization
* Tutorials
* Achievements
* Resource packs
* Better rendering
* Skins
* Quests
* Support for several languages
* and many, many more!

## System Prerequisites

Our game only supports Windows, MacOS and Linux. Furthermore, newer platform versions are required for controllers.

For Java, you may check out [system requirements for Java](https://www.java.com/en/download/help/sysreq.html).

## Current goals and ideas

Take a look at the [ideas](ideas/) folder or
the [issues](https://github.com/minicraftplus/minicraft-plus-revived/issues) page.

## Getting the game and run the game

Head over [releases](https://github.com/minicraftplus/minicraft-plus-revived/releases) and find the latest version of
Minicraft+.
There, you can find an file called `minicraft_plus.jar`. Click the file, and after you have downloaded the file, you
must double-click the file in downloads folder to open it.
You must first confirm that you have [Java](https://www.java.com/en/download/) (at least version 8) installed on your
computer.

## Localization

This project is running with an external localization platform called Lokalise. You can now [head over Lokalise to contribute localization](https://app.lokalise.com/public/42925017655336d50b3007.03067253/)!

## How to build/run in development

Because this project uses a build tool called gradle it is very easy to build or run the project from the source code.

1. Download the source code by clicking the green code button, and download it as a ZIP.
2. Extract the contents of the folder.
3. Open command prompt and enter `cd <folder_location>`, this will open the folder in the command prompt.
4. Type `gradlew run` or `gradlew build` to run or build the program. This might take some time. If on unix, add "./" to
   the front.
    1. If you built the project, the jar file is found in `build/libs`
    2. If you get an error screaming that you're missing java. You need
       to [set up](https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html) your
       JAVA_HOME environment variable, or download a JDK if you haven't already.
