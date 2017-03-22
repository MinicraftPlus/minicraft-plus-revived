# minicraft-plus-revived
Minicraft+, code taken from version 1.6 originally, with the goal of adding back all the features currently in 1.8.

The idea is to sort of merge the decompiled code of 1.8 with this code, since this code is better-looking, but the other has more features and it's further ahead.


### The plan:

1. Finish reintegrating the 1.8 features back into the 1.6 code; and clean it up at the same time.
2. Add new features! Use ideas from:
  * other minicraft versions
  * minecraft
  * my / other people's imagination

#### Current Bugs / Needed Fixes:

* Sound effect don't always play when they should.
* I have a system in place to save custom key controls, but lack the menu to edit them... I need to get that fixed.
* Clean up and simplify the code in various java files to make them both easier to follow and easier to develop.

### Long-term Goals:

* Add commenting work done in [this repository](https://github.com/shylor/miniventure) to applicable code.
  * Has now been done for many files; but there are still more to go...

* Add back:
  * Spawners
  * Colored Clothes
  * Experience (from 1.4)

* Tool and armor durability
  * Make armor cut a percent of damage, rather than act as extra hearts

* Chest item transfer
  * I personally can't stand how with chests, you always have to transfer all of a particular item, and you can't choose how much. I plan to change that.

#### Ideas for future additions:

* Auto-update system
* Multiplayer Servers
  * Custom usernames
* Custom Skins
* Redstone
* Fire
* Chickens
* Minecarts

  It's worth note that these are simply **ideas**, and have no garuntee of being implemented any time soon, or even at all. If they become more likely to implement, they will be move to "Long Term Goals".


##### Random Note: LevelGen.java has it's own main method; use it to generate an image of a randomly generated map.