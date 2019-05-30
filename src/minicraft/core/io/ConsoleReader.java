package minicraft.core.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import minicraft.core.Game;
import minicraft.core.Initializer;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.network.MinicraftServerThread;
import minicraft.saveload.Save;
import minicraft.screen.WorldSelectDisplay;

import org.jetbrains.annotations.Nullable;

public class ConsoleReader extends Thread {
	
	private enum Config {
		PLAYERCAP {
			public String getValue()  { return String.valueOf(Game.server.getPlayerCap()); }
			
			public boolean setValue(String val) {
				try {
					Game.server.setPlayerCap(Integer.parseInt(val));
					return true;
				} catch(NumberFormatException ex) {
					System.out.println("\""+val+"\" is not a valid number.");
				}
				return false;
			}
		},
		
		AUTOSAVE {
			public String getValue()  { return String.valueOf(Settings.get("autosave")); }
			
			public boolean setValue(String val) {
				Settings.set("autosave", Boolean.parseBoolean(val));
				return true;
			}
		};
		
		public abstract String getValue();
		public abstract boolean setValue(String val);
		
		public static final Config[] values = Config.values();
	}
	
	private enum Command {
		HELP
		("--all | [cmd]", "describes the function of each command. Specify a command name to read more about how to use it.", "no arguments: prints a list of all available commands, with a short description of each.", "cmd: a command name. will print the short description of that command, along with usage details such as what parameters/arguments it uses, and what function each argument has, and what the defualt behavior is if a given argument is ommitted.", "--all: prints the long description of all the commands.", "Usage symbol meanings:", "\t| = OR; specifies two possible choices for a given argument.", "\t[] = Optional; the arguments within may be specified, but they are not required.", "\t<> = Required; you must include the arguments within for the command to work.", "Note that the usage symbols may be nested, so a <> inside a [] is only required if you do whatever else is inside the [].") {
			public void run(String[] args) {
				if(args.length == 0) {
					System.out.println("available commands:");
					for(Command cmd: Command.values)
						System.out.println(cmd.getGeneralHelp());
					
					return;
				}
				
				Command query = ConsoleReader.getCommandByName(args[0]); // prints its own error message if the command wasn't found.
				if(query != null)
					System.out.println(query.getDetailedHelp());
			}
		},
		
		STATUS
		(null, "display some server stats.", "displays game version, server fps, and number of players connected.") {
			public void run(String[] args) {
				System.out.println("running "+Game.NAME+' '+Game.VERSION+(Game.debug?" (debug mode)":""));
				System.out.println("fps: " + Initializer.getCurFps());
				System.out.println("players connected: " + Game.server.getNumPlayers());
				for(String info: Game.server.getClientInfo())
					System.out.println("\t"+info);
			}
		},
		
		CONFIG
		("[option_name [value]]", "change various server settings.", "no arguments: displays all config options and their current values", "option_name: displays the current value of that option", "option_name value:, will set the option to the specified value, provided it is a valid value for that option.") {
			
			public void run(String[] args) {
				if(args.length == 0) {
					for(Config c: Config.values)
						System.out.println("\t"+c.name() + " = " + c.getValue());
				} else {
					Config configOption = null;
					try {
						configOption = Enum.valueOf(Config.class, args[0].toUpperCase(Localization.getSelectedLocale()));
					} catch(IllegalArgumentException ex) {
						System.out.println("\""+args[0]+"\" is not a valid config option. run \"config\" for a list of the available config options.");
					}
					if(configOption == null) return;
					if(args.length > 1) { // we want to set the config option.
						if(args.length > 2) System.out.println("note: additional arguments (more than two) will be ignored.");
						boolean set = configOption.setValue(args[1]);
						if(set) {
							System.out.println(configOption.name()+" set successfully.");
							/// HERE is where we save the modified config options.
							new Save(WorldSelectDisplay.getWorldName(), Game.server);
							new Save(); 	
						} else
							System.out.println("failed to set " + configOption.name());
					}
				}
			}
		},
		
		STOP
		(null, "close the server.") {
			public void run(String[] args) {
				System.out.println("shutting down server...");
				Game.server.endConnection();
			}
		},
		
		RESTART
		(null, "restart the server.", "closes the server, then starts it back up again.") {
			public void run(String[] args) {
				Command.STOP.run(null); // shuts down the server.
				try {
					Thread.sleep(500); // give the computer some time to, uh, recuperate? idk, I think it's a good idea.
				} catch(InterruptedException ignored) {}
				Network.startMultiplayerServer(); // start the server back up.
			}
		},
		
		SAVE
		(null, "Save the world to file.") {
			public void run(String[] args) {
				Game.server.saveWorld();
				System.out.println("World Saved.");
			}
		},
		
		GAMEMODE
		("<mode>", "change the server gamemode.", "mode: one of the following: c(reative), su(rvivial), t(imed) / score, h(ardcore)") {
			public void run(String[] args) {
				if(args.length != 1) {
					System.out.println("incorrect number of arguments. Please specify the game mode in one word:");
					printHelp(this);
					return;
				}
				
				switch(args[0].toLowerCase()) {
					case "s": case "survival":
						Settings.set("mode", "Survival");
						break;
					
					case "c": case "creative":
						Settings.set("mode", "Creative");
						break;
					
					case "h": case "hardcore":
						Settings.set("mode", "Hardcore");
						break;
					
					case "t": case "timed": case "score":
						Settings.set("mode", "Score");
						break;
					
					default:
						System.out.println(args[0] + " is not a valid game mode.");
						printHelp(this);
						break;
				}
				
				Game.server.updateGameVars();
			}
		},
		
		TIME
		("[timeString]", "sets or prints the time of day." , "no arguments: prints the current time of day, in ticks.", "timeString: sets the time of day to the given value; it can be a number, in which case it is a tick count from 0 to 64000 or so, or one of the following strings: Morning, Day, Evening, Night. the time of day will be set to the beginning of the given time period.") {
			public void run(String[] args) {
				if(args.length == 0) {
					System.out.println("time of day is: " + Updater.tickCount + " ("+ Updater.getTime()+")");
					return;
				}
				
				int targetTicks = -1;
				
				if(args[0].length() > 0) {
					try {
						String firstLetter = String.valueOf(args[0].charAt(0)).toUpperCase();
						String remainder = args[0].substring(1).toLowerCase();
						Updater.Time time = Enum.valueOf(Updater.Time.class, firstLetter+remainder);
						targetTicks = time.tickTime;
					} catch(IllegalArgumentException iaex) {
						try {
							targetTicks = Integer.parseInt(args[0]);
						} catch(NumberFormatException ignored) {
						}
					}
				}
				
				if(targetTicks >= 0) {
					Updater.setTime(targetTicks);
					Game.server.updateGameVars();
				} else {
					System.out.println("time specified is in an invalid format.");
					Command.printHelp(this);
				}
			}
		},
		
		MSG
		("[username] <message>", "make a message appear on other players' screens.", "w/o username: sends to all players,", "with username: sends to that player only.") {
			public void run(String[] args) {
				if(args.length == 0) {
					System.out.println("please specify a message to send.");
					return;
				}
				List<String> usernames = new ArrayList<>();
				if(args.length > 1) {
					usernames.addAll(Arrays.asList(args).subList(0, args.length - 1));
				} else {
					Game.server.broadcastNotification(args[0], 50);
					return;
				}
				
				String message = args[args.length-1];
				for(MinicraftServerThread clientThread: Game.server.getAssociatedThreads(usernames.toArray(new String[usernames.size()]), true))
					clientThread.sendNotification(message, 50);
			}
		},
		
		TP
		("<playername> <x y [level] | playername>", "teleports a player to a given location in the world.", "the first player name is the player that will be teleported. the second argument can be either another player, or a set of world coordinates.", "if the second argument is a player name, then the first player will be teleported to the second player, possibly traversing different levels.", "if world coordinates are specified, an x and y coordinate are required. A level depth may optionally be specified to go to a different level; if not specified, the current level is assumed.", "the symbol \"~\" may be used in place of an x or y coordinate, or a level, to mean the current player position on that axis. additionally, an offset may be specified by writing it like so: \"~-3 ~\". this means 3 tiles to the left of the current player position.") { /// future usage: "<x> <y> | "
			public void run(String[] args) {
				if(args.length == 0) {
					System.out.println("you must specify a username, and coordinates or another username to teleport to.");
					printHelp(this);
					return;
				}
				MinicraftServerThread clientThread = Game.server.getAssociatedThread(args[0]);
				if(clientThread == null) {
					System.out.println("could not find player with username \"" + args[0] + "\"");
					return;
				}
				
				int xt, yt;
				Level level = clientThread.getClient().getLevel();
				
				if(args.length > 2) {
					try {
						xt = getCoordinate(args[1], clientThread.getClient().x >> 4);
						yt = getCoordinate(args[2], clientThread.getClient().y >> 4);
						
						if(args.length == 4) {
							try {
								int lvl = getCoordinate(args[3], (level != null ? level.depth : 0));
								level = World.levels[World.lvlIdx(lvl)];
							} catch (NumberFormatException ex) {
								System.out.println("specified level index is not a number: " + args[3]);
								return;
							} catch(IndexOutOfBoundsException ex) {
								System.out.println("invalid level index: " + args[3]);
								return;
							}
						}
					} catch(NumberFormatException ex) {
						System.out.println("invalid command syntax; specify a player or world coordinates for tp destination.");
						printHelp(this);
						return;
					}
				} else {
					// user specified the username of another player to tp to.
					MinicraftServerThread destClientThread = Game.server.getAssociatedThread(args[1]);
					if(destClientThread == null) {
						System.out.println("could not find player with username \"" + args[0] + "\" for tp destination.");
						return;
					}
					
					RemotePlayer rp = destClientThread.getClient();
					if(rp == null) {
						System.out.println("client no longer exists...");
						return;
					}
					xt = rp.x >> 4;
					yt = rp.y >> 4;
					level = rp.getLevel();
				}
				
				if(xt >= 0 && yt >= 0 && level != null && xt < level.w && yt < level.h) {
					// perform teleport
					RemotePlayer playerToMove = clientThread.getClient();
					if(playerToMove == null) {
						System.out.println("can't perform teleport; client no longer exists...");
						return;
					}
					if(!level.getTile(xt, yt).mayPass(level, xt, yt, playerToMove)) {
						System.out.println("specified tile is solid and cannot be moved though.");
						return;
					}
					Level pLevel = playerToMove.getLevel();
					int nx = xt*16+8;
					int ny = yt*16+8;
					if(pLevel == null || pLevel.depth != level.depth) {
						playerToMove.remove();
						level.add(playerToMove, nx, ny);
					}
					else {
						int oldxt = playerToMove.x >> 4;
						int oldyt = playerToMove.y >> 4;
						playerToMove.x = nx;
						playerToMove.y = ny;
						Game.server.broadcastEntityUpdate(playerToMove, true);
						playerToMove.updatePlayers(oldxt, oldyt);
						playerToMove.updateSyncArea(oldxt, oldyt);
					}
					
					System.out.println("teleported player " + playerToMove.getUsername() + " to tile coordinates " + xt+","+yt+", on level " + level.depth);
				} else {
					System.out.println("could not perform teleport; coordinates are not valid.");
				}
			}
		},
		
		PING ("", "Pings all the clients, and prints a message when each responds.") {
			@Override
			public void run(String[] args)  { Game.server.pingClients(); }
		},
		
		KILL ("<playername> | @[!]<all|entity|player|mob> <level | <playername> <radius>>", "Kills the specified entities.", "Specifying only a playername will kill that player.", "In the second form, use @all to refer to all entities, @entity to refer to all non-mob entities, @mob to refer to only mob entities, and @player to refer to all players.", "the \"!\" reverses the effect.", "@_ level will target all matching entities for that level.", "using a playername and radius will target all matching entities within the given radius of the player, the radius being a number of tiles.") {
			@Override
			public void run(String[] args) {
				List<Entity> entities = targetEntities(args);
				if(entities == null) {
					printHelp(this);
					return;
				}
				
				int count = entities.size();
				for(Entity e: entities)
					e.die();
				
				System.out.println("removed " + count + " entities.");
			}
		};
		
		private String generalHelp, detailedHelp, usage;
		
		Command(String usage, String general, String... specific) {
			String name = this.name().toLowerCase();
			String sep = " - ";
			
			generalHelp = name + sep + general;
			
			this.usage = usage == null ? name : name + " " + usage;
			if(usage != null) usage = sep+"Usage: "+name+" "+usage;
			else usage = "";
			
			detailedHelp = name + usage + sep + general;
			if(specific != null && specific.length > 0)
				detailedHelp += System.lineSeparator()+"\t"+String.join(System.lineSeparator()+"\t", specific);
		}
		
		public abstract void run(String[] args);
		
		public String getUsage() { return usage; }
		public String getGeneralHelp() { return generalHelp; }
		public String getDetailedHelp() { return detailedHelp; }
		
		public static void printHelp(Command cmd) {
			System.out.println("Usage: " + cmd.getUsage());
			System.out.println("type \"help " + cmd + "\" for more info.");
		}
		
		private static int getCoordinate(String coord, int baseline) throws NumberFormatException {
			if(coord.contains("~")) {
				if(coord.equals("~")) return baseline;
				else return Integer.parseInt(coord.replace("~", "")) + baseline;
			} else
				return Integer.parseInt(coord);
		}
		
		@Nullable
		private static List<Entity> targetEntities(String[] args) {
			List<Entity> matches = new ArrayList<>();
			
			if(args.length == 0) {
				System.out.println("cannot target entities without arguments.");
				return null;
			}
			
			if(args.length == 1) {
				// must be player name
				MinicraftServerThread thread = Game.server.getAssociatedThread(args[0]);
				if(thread != null)
					matches.add(thread.getClient());
				return matches;
			}
			
			// must specify @_ as first argument
			
			if(!args[0].startsWith("@")) {
				System.out.println("invalid entity targeting format. Please read help.");
				return null;
			}
			
			String target = args[0].substring(1).toLowerCase(Localization.getSelectedLocale()); // cut off "@"
			List<Entity> allEntities = new ArrayList<>();
			
			if(args.length == 2) {
				// specified @_ level
				try {
					allEntities.addAll(Arrays.asList(Game.levels[new Integer(args[1])].getEntityArray()));
				} catch(NumberFormatException ex) {
					System.out.println("invalid entity targeting format: specified level is not an integer: " + args[1]);
					return null;
				} catch (IndexOutOfBoundsException ex) {
					System.out.println("invalid entity targeting format: specified level does not exist: " + args[1]);
					return null;
				}
			}
			
			if(args.length == 3) {
				// @_ playername radius
				MinicraftServerThread thread = Game.server.getAssociatedThread(args[1]);
				RemotePlayer rp = thread == null ? null : thread.getClient();
				if(rp == null) {
					System.out.println("invalid entity targeting format: remote player does not exist: " + args[1]);
					return null;
				}
				
				try {
					int radius = new Integer(args[2]);
					allEntities.addAll(rp.getLevel().getEntitiesInRect(new Rectangle(rp.x, rp.y, radius*2, radius*2, Rectangle.CENTER_DIMS)));
					allEntities.remove(rp);
				} catch(NumberFormatException ex) {
					System.out.println("invalid entity targeting format: specified radius is not an integer: " + args[2]);
					return null;
				}
			}
			
			boolean invert = false;
			if(target.startsWith("!")) {
				invert = true;
				target = target.substring(1);
			}
			
			List<Entity> remainingEntities = new ArrayList<>(allEntities);
			switch(target) {
				case "all": break; // target all entities
				
				case "entity": // target only non-mobs
					allEntities.removeIf(entity -> entity instanceof Mob);
				break;
				
				case "mob": // target only mobs
					allEntities.removeIf(entity -> !(entity instanceof Mob));
				break;
				
				case "player": // target only players
					allEntities.removeIf(entity -> !(entity instanceof Player));
				break;
				
				default:
					System.out.println("invalid entity targeting format: @_ argument is not valid: @" + target);
					return null;
			}
			
			remainingEntities.removeAll(allEntities);
			
			if(invert)
				return remainingEntities;
			
			return allEntities;
		}
		
		public static final Command[] values = Command.values();
	}
	
	private boolean shouldRun;
	
	public ConsoleReader() {
		super("ConsoleReader");
		shouldRun = true;
	}
	
	public void run() {
		Scanner stdin = new Scanner(System.in);
		try {
			Thread.sleep(500); // this is to let it get past the debug statements at world load, and any others, maybe, if not in debug mode.
		} catch(InterruptedException ignored) {}
		System.out.println("type \"help\" for a list of commands...");
		
		while(shouldRun/* && stdin.hasNext()*/) {
			System.out.println();
			System.out.print("Enter a command: ");
			String command = stdin.nextLine().trim();
			if(command.length() == 0) continue;
			List<String> parsed = new ArrayList<>();
			parsed.addAll(Arrays.asList(command.split(" ")));
			int lastIdx = -1;
			for(int i = 0; i < parsed.size(); i++) {
				if(parsed.get(i).contains("\"")) {
					if(lastIdx >= 0) { // closing a quoted String
						while(i > lastIdx) { // join the words together
							parsed.set(lastIdx, parsed.get(lastIdx) + " " + parsed.remove(lastIdx+1));
							i--;
						}
						lastIdx = -1; // reset the "last quote" variable.
					} else // start the quoted String
						lastIdx = i; // set the "last quote" variable.
					
					parsed.set(i, parsed.get(i).replaceFirst("\"", "")); // remove the parsed quote character from the string.
					i--; // so that this string can be parsed again, in case there is another quote.
				}
			}
			//if (Game.debug) System.out.println("parsed command: " + parsed.toString());
			
			Command cmd = getCommandByName(parsed.remove(0)); // will print its own error message if not found.
			if(cmd == null)
				Command.HELP.run(new String[0]);
			else if(Game.isValidServer() || cmd == Command.HELP)
				cmd.run(parsed.toArray(new String[parsed.size()]));
			else
				System.out.println("no server running.");
			
			if(cmd == Command.STOP) shouldRun = false;
		}
		
		Game.quit();
	}
	
	private static Command getCommandByName(String name) {
		Command cmd = null;
		try {
			cmd = Enum.valueOf(Command.class, name.toUpperCase(Localization.getSelectedLocale()));
		} catch(IllegalArgumentException ex) {
			System.out.println("unknown command: \"" + name + "\"");
		}
		
		return cmd;
	}
}
