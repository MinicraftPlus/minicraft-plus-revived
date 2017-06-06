package minicraft.network;

import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.saveload.Load;
import minicraft.screen.WorldGenMenu;
import minicraft.screen.LoadingMenu;

public class MinicraftConnection extends Thread {
	
	protected static final int PORT = 4225;
	
	protected PrintWriter out;
	protected BufferedReader in;
	
	protected Game game;
	protected Socket socket = null;
	
	private enum InputType {
		INIT, START, TILE, ENTITY, ADD, REMOVE, EXIT, NOTIFY
	}
	
	protected MinicraftConnection(Game gameInstance) {
		super("MinicraftServer");
		this.game = gameInstance;
		start();
	}
	protected MinicraftConnection(Game gameInstance, Socket socket) {
		super("MinicraftServerThread");
		this.game = gameInstance;
		this.socket = socket;
		
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException ex) {
			System.err.println("failed to initalize i/o streams for server-side socket");
			ex.printStackTrace();
		}
		
		start();
	}
	protected MinicraftConnection(Game gameInstance, String hostName) {
		super("MinicraftClient");
		this.game = gameInstance;
		
		(new Thread() {
			public void run() {
				try {
					
					socket = new Socket(hostName, MinicraftConnection.PORT);
					
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream(), true);
					
					MinicraftConnection.this.start();
					
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host " + hostName);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to host " +
						hostName);
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void run() {
		while(isConnected()) {
			try {
				if(in.ready())
					checkInput(in.readLine());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	protected boolean checkInput(String data) {
		//if (Game.debug) System.out.println("checking input...");
		if(data == null || data.length() == 0) return false;
		//if (Game.debug) System.out.println("input is not null");
		/**This is implemented in subclasses, so I thought I'd put the notes about --Input Parsing-- here instead.
			Expected format of input is:
			-a short string of text describing what kind of data it is (what is it for, how should it be read/used, what it represents) that serves as an ID string
			-followed by a colon(':')
			-followed by whatever data.
			
			+++ Defined Data Tags so far: +++
				
				INIT: the first thing sent to the client. This contains pretty much exactly what a level would have: world size, tile ids, and tile data. The format is: "lvlw,lvlh;tileid,data;tileid,data;"...etc.
				
				START: tells the client that it has recieved all necessary data to begin the game.
				
				EXIT: signals recipient to close their side of the socket connection. no data.
				
				TILE: the data is a tile id, its data, and its position in the level. When recieved, The appropriate tile will be updated, for the level that the player is currently on. If recieved by the server, the level is that of the player entity associated with the socket that sent the data. For clients it's simply the player's current level.
				
				ENTITY: data is a serialized entity object. This includes the entity id, which is simply a random integer unique to that entity. The entity object in the level with the same id is **replaced**.
				
				ADD: used to signal that the serialized entity contained in the data should be **added** to the level, rather than replace an existing entity. the entity id is set then to a vaild value.
				
				REMOVE: used to signal that an entity should be **removed** from the level. the data is just the entity id of the entity to remove.
				
				NOTIFY: used to pass a notification to another game. data is the notification text, followed by a comma, followed by the notetick value that Game.notetick should be set to.
		*/
		
		String dataType = data.split(":")[0];
		InputType inType = null;
		try {
			inType = Enum.valueOf(InputType.class, dataType.toUpperCase());
		} catch (IllegalArgumentException ex) {
			System.err.println("Communication Error: Socket input data has an invalid format.");
			if (Game.debug) System.out.println("data: " + data);
			return false;
		}
		
		if(inType == null) {
			// this *should* never execute.
			System.out.println("unexpected null input type of socket data.");
			return false;
		}
		
		// at this point, it has been determined that the data is of a valid type, and that type is stored in inType.
		if(inType == InputType.EXIT) {
			// for this, there is no data, and there might be no semicolon either. So, it will greatly simplify matters if we deal with this first.
			endConnection();
			return true;
		}
		
		if(inType == InputType.START && Game.isValidClient()) {
			game.setMenu(null);
			game.resetGame();
			getLevel().add(game.player);
			Game.readyToRenderGameplay = true;
			out.println("ADD:"+game.player.serialize());
			return true;
		}
		
		int cutidx = data.indexOf(":");
		if(cutidx == -1 || cutidx == data.length()-1) {
			// this means there was no semicolon, or there was a semicolon but no data following it. At this point, that is not valid syntax.
			System.err.println("Communication Error: Socket input data has type " + inType + " but no data.");
			return false;
		}
		
		data = data.substring(cutidx+1); // cut off the data type, because we don't need it.
		
		if(inType == InputType.NOTIFY) {
			int notetime = 0;
			if(data.lastIndexOf(",") == -1 || data.lastIndexOf(",") == data.length()-1)
				System.out.println("note: no note time specified for note: " + data);
			else {
				try {
					notetime = Integer.parseInt(data.substring(data.lastIndexOf(",")+1));
				} catch (NumberFormatException ex) {
					System.err.println("Communication Error: note time specified is not a number. note: " + data);
					return false;
				}
			}
			
			game.notifications.add(data);
			Game.notetick = notetime;
			return true;
		}
		
		/// now begins the actual handling of the input data.
		Level level = getLevel();
		try {
			if(inType == InputType.INIT) {
				if(data.indexOf(",") == -1) {
					System.err.println("Communication error: Invalid init level data; no commas.");
					return false;
				}
				String[] values = data.split(",");
				Game.tickCount = Integer.parseInt(values[0]);
				int lvlw = Integer.parseInt(values[1]);
				int lvlh = Integer.parseInt(values[2]);
				level.w = lvlw;
				level.h = lvlh;
				level.tiles = new byte[lvlw*lvlh];
				level.data = new byte[lvlw*lvlh];
				/*List<String> lvlstrings = Arrays.asList(data.split(";"));
				Game.time = Integer.parseInt(lvlstrings.get(0));
				int[] lvlids = new int[lvlstrings.size()-1];
				int[] lvldata = new int[lvlstrings.size()-1];
				System.out.println("parsing level data into ints...");
				try {
					for(int i = 0; i < lvlstrings.size()-1; i++) {
						String[] parts = lvlstrings.get(i+1).split(",");
						lvlids[i] = Integer.parseInt(parts[0]);
						lvldata[i] = Integer.parseInt(parts[1]);
					}
				} catch (NumberFormatException ex) {
					System.err.println("Communication Error: a value in level init list was not a number.");
					ex.printStackTrace();
					return false;
				} catch (IndexOutOfBoundsException ex) {
					System.err.println("Communication Error: level init list does not contain both id and data.");
					ex.printStackTrace();
					return false;
				}
				
				System.out.println("loading level; array length: " + lvlids.length);
				//game.setMenu(new LoadingMenu());
				Load.loadLevel(level, lvlids, lvldata);
				level = Game.levels[game.currentLevel];
				game.resetGame();
				level.add(game.player);
				*/
				//Game.readyToRenderGameplay = true;
				return true;
			}
			
			if(inType == InputType.TILE) {
				/// replace a tile with another tile.
				String[] parts = data.split(",");
				int pos = Integer.parseInt(parts[0]);
				int id = Integer.parseInt(parts[1]);
				int tdata = Integer.parseInt(parts[2]);
				level.tiles[pos] = (byte) id;
				level.data[pos] = (byte) tdata;
				return true; // finished.
			}
			
			// each of the remaining valid input types all deal with entities in some way.
			int eid = -1;
			if(inType != InputType.ADD) {
				if(!data.contains(";")) {
					System.err.println("Communication Error: data of input type " + inType + " contains no entity id.");
					return false;
				}
				eid = Integer.parseInt(data.split(";")[0]);
			}
			
			if(inType == InputType.REMOVE) {
				/// remove an entity from the level.
				//level.removeEntity(eid); // TODO this method has not been created, nor do entities have ids.
				return true;
			}
			
			/// the remaining input types require a serialized object.
			if(inType == InputType.ENTITY) {
				if(data.indexOf(";") == data.length() - 1) {
					System.err.println("Communication Error: input data of type ENTITY does not contain an entity to deserialize.");
					if (Game.debug) System.out.println("data: " + data);
					return false;
				}
				data = data.substring(data.indexOf(";")+1);
			}
			
			// TODO I cannot really write this line until I know how serialization works.
			//Entity entity = Entity.deserialize(data); // my best guess.
			
			if(inType == InputType.ENTITY) {
				///replace an entity with another entity (usually the same one with a modification)
				//level.replaceEntity(eid, entity); // TODO nonexistent method.
			}
			
			if(inType == InputType.ADD) {
				/// add an entity to the level.
				//level.addEntity(entity); // TODO nonexistent method.
			}
			
		} catch (Exception ex) {
			System.err.println("Communication Error: exception caught while parsing "+inType+"-type input data:");
			if (Game.debug) {
				System.out.println(data);
				System.out.println("exception stack trace:");
			}
			ex.printStackTrace();
			return false;
		}
		
		return true; // this is reached by InputType.ENTITY and InputType.ADD.
	}
	
	/** This method is responsible for sending an updated entity. */
	public void sendEntityUpdate(int eid, Entity entity) {
		out.println("ENTITY:"+"garbage");
	}
	
	/** This method is responsible for removing an existing entity. */
	public void sendEntityUpdate(int eid) {
		out.println("REMOVE:"+eid);
	}
	
	/** This method is responsible for adding a new entity. */
	public void sendEntityUpdate(Entity entity) {
		out.println("ADD:"+"garbage");
	}
	
	/** This method is responsible for sending an updated tile. */
	public void sendTileUpdate(int x, int y) {
		Level level = getLevel();
		int pos = x + level.h * y;
		int id = level.getTile(x, y).id;
		int data = level.getData(x, y);
		out.println("TILE:"+pos+","+id+","+data);
	}
	
	public void sendNotification(String note, int notetick) {
		if(note == null || note.length() == 0) {
			System.out.println("tried to send blank notification");
			return;
		}
		out.println("NOTIFY:"+note+","+notetick);
	}
	
	/** This method is used to get the level that should be updated/used as reference. It is a seperate method so it can be easily overridden, such as for the server threads, which will be updating levels that the host player is not on. */
	protected Level getLevel() {
		int curLvl = game.currentLevel;
		Level level = Game.levels[curLvl];
		if(level == null) {
			int depth = curLvl == 5 ? -4 : curLvl - 3;
			Level parent = depth == -4 ? Game.levels[0] : depth == 1 ? null : Game.levels[curLvl+1];
			int size = WorldGenMenu.getSize();
			level = new Level(game, size, size, (curLvl == 5 ? -4 : curLvl - 3), parent, false);
			Game.levels[curLvl] = level;
		}
		
		return level;
	}
	
	public void endConnection() {
		if (Game.debug) System.out.println("closing socket and ending connection");
		try {
			out.println("EXIT"); // send exit signal
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}
}
