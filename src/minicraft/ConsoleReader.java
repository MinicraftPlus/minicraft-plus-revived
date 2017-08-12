package minicraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Locale;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.network.MinicraftServerThread;
import minicraft.level.Level;
import minicraft.saveload.Save;
import minicraft.screen.OptionsMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.WorldSelectMenu;

class ConsoleReader extends Thread {
	
	private Game game;
	
	private static enum Config {
		PLAYERCAP {
			public String getValue() {
				return String.valueOf(Game.server.getPlayerCap());
			}
			
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
			public String getValue() {
				return String.valueOf(OptionsMenu.autosave);
			}
			
			public boolean setValue(String val) {
				OptionsMenu.autosave = Boolean.parseBoolean(val);
				return true;
			}
		};
		
		public abstract String getValue();
		public abstract boolean setValue(String val);
		
		public static final Config[] values = Config.values();
	}
	
	private static enum Command {
		HELP
		("--all | [cmd]", "describes the function of each command. Specify a command name to read more about how to use it.", "no arguments: prints a list of all available commands, with a short description of each.", "cmd: a command name. will print the short description of that command, along with usage details such as what parameters/arguments it uses, and what function each argument has, and what the defualt behavior is if a given argument is ommitted.", "--all: prints the long description of all the commands.", "Usage symbol meanings:", "\t| = OR; specifies two possible choices for a given argument.", "\t[] = Optional; the arguments within may be specified, but they are not required.", "\t<> = Required; you must include the arguments within for the command to work.", "Note that the usage symbols may be nested, so a <> inside a [] is only required if you do whatever else is inside the [].") {
			public void run(String[] args, Game game) {
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
		(null, "display some server stats.", "displays server fps, and number of players connected.") {
			public void run(String[] args, Game game) {
				System.out.println("fps: " + game.fra);
				System.out.println("players connected: " + Game.server.getThreads().length);
				for(String info: Game.server.getClientInfo())
					System.out.println("\t"+info);
			}
		},
		
		CONFIG
		("[option_name [value]]", "change various server settings.", "no arguments: displays all config options and their current values", "option_name: displays the current value of that option", "option_name value:, will set the option to the specified value, provided it is a valid value for that option.") {
			
			public void run(String[] args, Game game) {
				if(args.length == 0) {
					for(Config c: Config.values)
						System.out.println("\t"+c.name() + " = " + c.getValue());
				} else {
					Config configOption = null;
					try {
						configOption = Enum.valueOf(Config.class, args[0].toUpperCase(Locale.ENGLISH));
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
							new Save(game, WorldSelectMenu.worldname, Game.server);
						} else
							System.out.println("failed to set " + configOption.name());
					}
				}
			}
		},
		
		STOP
		(null, "close the server.") {
			public void run(String[] args, Game game) {
				System.out.println("shutting down server...");
				Game.server.endConnection();
			}
		},
		
		RESTART
		(null, "restart the server.", "closes the server, then starts it back up again.") {
			public void run(String[] args, Game game) {
				Command.STOP.run(null, game); // shuts down the server.
				try {
					Thread.sleep(500); // give the computer some time to, uh, recuperate? idk, I think it's a good idea.
				} catch(InterruptedException ex) {}
				game.startMultiplayerServer(); // start the server back up.
			}
		},
		
		SAVE
		(null, "Save the world to file.") {
			public void run(String[] args, Game game) {
				Game.server.saveWorld();
				System.out.println("World Saved.");
			}
		},
		
		GAMEMODE
		("<mode>", "change the server gamemode.", "mode: one of the following: c(reative), su(rvivial), t(imed) / score, h(ardcore)") {
			public void run(String[] args, Game game) {
				if(args.length != 1) {
					System.out.println("incorrent number of arguments. Please specify the game mode in one word:");
					printHelp(this, game);
					return;
				}
				
				switch(args[0].toLowerCase()) {
					case "s": case "survival":
						ModeMenu.updateModeBools("Survival");
						break;
					
					case "c": case "creative":
						ModeMenu.updateModeBools("Creative");
						break;
					
					case "h": case "hardcore":
						ModeMenu.updateModeBools("Hardcore");
						break;
					
					case "t": case "timed": case "score":
						ModeMenu.updateModeBools("Score");
						break;
					
					default:
						System.out.println(args[0] + " is not a valid game mode.");
						printHelp(this, game);
						break;
				}
			}
		},
		
		TIME
		("[timeString]", "sets or prints the time of day." , "no arguments: prints the current time of day, in ticks.", "timeString: sets the time of day to the given value; it can be a number, in which case it is a tick count from 0 to 64000 or so, or one of the following strings: Morning, Day, Evening, Night. the time of day will be set to the beginning of the given time period.") {
			public void run(String[] args, Game game) {
				if(args.length == 0) {
					System.out.println("time of day is: " + game.tickCount + " ("+game.getTime()+")");
					return;
				}
				
				int targetTicks = -1;
				
				if(args[0].length() > 0) {
					try {
						String firstLetter = String.valueOf(args[0].charAt(0)).toUpperCase();
						String remainder = args[0].substring(1).toLowerCase();
						Game.Time time = Enum.valueOf(Game.Time.class, firstLetter+remainder);
						targetTicks = time.tickTime;
					} catch(IllegalArgumentException iaex) {
						try {
							targetTicks = Integer.parseInt(args[0]);
						} catch(NumberFormatException nfex) {
						}
					}
				}
				
				if(targetTicks >= 0) {
					game.setTime(targetTicks);
					Game.server.updateGameVars();
				} else {
					System.out.println("time specified is in an invalid format.");
					Command.printHelp(this, game);
				}
			}
		},
		
		MSG
		("[username] <message>", "make a message appear on other players' screens.", "w/o username: sends to all players,", "with username: sends to that player only.") {
			public void run(String[] args, Game game) {
				if(args.length == 0) {
					System.out.println("please specify a message to send.");
					return;
				}
				List<String> usernames = new ArrayList<String>();
				if(args.length > 1) {
					for(int i = 0; i < args.length-1; i++)
						usernames.add(args[i]);
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
			public void run(String[] args, Game game) {
				if(args.length == 0) {
					System.out.println("you must specify a username, and coordinates or another username to teleport to.");
					printHelp(this, game);
					return;
				}
				MinicraftServerThread clientThread = Game.server.getAssociatedThread(args[0]);
				if(clientThread == null) {
					System.out.println("could not find player with username \"" + args[0] + "\"");
					return;
				}
				
				int xt = -1;
				int yt = -1;
				Level level = clientThread.getClient().getLevel();
				
				if(args.length > 2) {
					try {
						xt = getCoordinate(args[1], clientThread.getClient().x >> 4);
						yt = getCoordinate(args[2], clientThread.getClient().y >> 4);
						
						if(args.length == 4) {
							try {
								int lvl = getCoordinate(args[3], (level != null ? level.depth : 0));
								level = Game.levels[Game.lvlIdx(lvl)];
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
						printHelp(this, game);
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
					clientThread.setClientPos(level.depth, xt << 4, yt << 4);
					
					System.out.println("teleported player " + playerToMove.getUsername() + " to tile coordinates " + xt+","+yt+", on level " + level.depth);
				} else {
					System.out.println("could not perform teleport; coordinates are not valid.");
					return;
				}
			}
		};
		
		private String generalHelp, detailedHelp, usage;
		
		private Command(String usage, String general, String... specific) {
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
		
		public abstract void run(String[] args, Game game);
		
		public String getUsage() { return usage; }
		public String getGeneralHelp() { return generalHelp; }
		public String getDetailedHelp() { return detailedHelp; }
		
		public static void printHelp(Command cmd, Game game) {
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
		
		public static final Command[] values = Command.values();
	}
	
	private boolean shouldRun;
	
	public ConsoleReader(Game game) {
		super("ConsoleReader");
		this.game = game;
		shouldRun = true;
	}
	
	public void run() {
		Scanner stdin = new Scanner(System.in).useDelimiter(System.lineSeparator());
		try {
			Thread.sleep(500); // this is to let it get past the debug statements at world load, and any others, maybe, if not in debug mode.
		} catch(InterruptedException ex) {}
		System.out.println("type \"help\" for a list of commands...");
		
		while(shouldRun/* && stdin.hasNext()*/) {
			System.out.print("Enter a command: ");
			String command = stdin.next().trim();
			if(command.length() == 0) continue;
			List<String> parsed = new ArrayList<String>();
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
				Command.HELP.run(new String[0], game);
			else if(Game.isValidServer() || cmd == Command.HELP)
				cmd.run(parsed.toArray(new String[parsed.size()]), game);
			else
				System.out.println("no server running.");
			
			if(cmd == Command.STOP) shouldRun = false;
		}
		
		game.quit();
	}
	
	private static Command getCommandByName(String name) {
		Command cmd = null;
		try {
			cmd = Enum.valueOf(Command.class, name.toUpperCase(Locale.ENGLISH));
		} catch(IllegalArgumentException ex) {
			System.out.println("unknown command: \"" + name + "\"");
		}
		
		return cmd;
	}
}
