package minicraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Locale;
import minicraft.saveload.Save;
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
				
				Command query = ConsoleReader.getCommandByName(args[1]); // prints its own error message if the command wasn't found.
				if(query != null)
					System.out.println(query.getDetailedHelp());
			}
		},
		
		STATUS
		(null, "display some server stats.", "displays server fps, and number of players connected.") {
			public void run(String[] args, Game game) {
				System.out.println("fps: " + game.fra);
				if(Game.isValidServer()) {
					System.out.println("players connected: " + Game.server.getThreads().length);
					for(String info: Game.server.getClientInfo())
						System.out.println("\t"+info);
				} else {
					System.out.println("server is not running; no players connected.");
				}
			}
		},
		
		CONFIG
		("[option_name [value]]", "change various server settings.", "no arguments: displays all config options and their current values", "option_name: displays the current value of that option", "option_name value:, will set the option to the specified value, provided it is a valid value for that option.") {
			
			public void run(String[] args, Game game) {
				if(!Game.isValidServer()) {
					System.out.println("no server running. cannot fetch config options.");
					return;
				}
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
		
		QUIT
		(null, "close the server.") {
			public void run(String[] args, Game game) {
				if(Game.isValidServer()) {
					System.out.println("shutting down server...");
					Game.server.endConnection();
				} else
					System.out.println("no server running.");
			}
		},
		
		RESTART
		(null, "restart the server.", "closes the server, then starts it back up again.") {
			public void run(String[] args, Game game) {
				Command.QUIT.run(null, game); // shuts down the server.
				try {
					Thread.sleep(500); // give the computer some time to, uh, recuperate? idk, I think it's a good idea.
				} catch(InterruptedException ex) {}
				game.startMultiplayerServer(); // start the server back up.
			}
		};
		
		private String generalHelp, detailedHelp;
		
		private Command(String usage, String general, String... specific) {
			String name = this.name().toLowerCase();
			String sep = " - ";
			
			generalHelp = name + sep + general;
			
			if(usage != null) usage = sep+"Usage: "+name+" "+usage;
			else usage = "";
			
			detailedHelp = name + usage + sep + general;
			if(specific != null && specific.length > 0)
				detailedHelp += System.lineSeparator()+String.join(System.lineSeparator()+"\t", specific);
		}
		
		public abstract void run(String[] args, Game game);
		
		public String getGeneralHelp() { return generalHelp; }
		public String getDetailedHelp() { return detailedHelp; }
		
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
			String command = stdin.next();
			List<String> parsed = new ArrayList<String>();
			parsed.addAll(Arrays.asList(command.split(" ")));
			//if (Game.debug) System.out.println("parsed command: " + parsed.toString());
			Command cmd = getCommandByName(parsed.remove(0)); // will print its own error message if not found.
			if(cmd == null)
				Command.HELP.run(new String[0], game);
			else
				cmd.run(parsed.toArray(new String[parsed.size()]), game);
			
			if(cmd == Command.QUIT) shouldRun = false;
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
