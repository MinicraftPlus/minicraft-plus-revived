package minicraft.network;

public class MinicraftConnection extends Thread {
	
	protected static final int PORT = 4225;
	
	protected PrintWriter out;
	protected BufferedReader in;
	
	protected Game game;
	protected Socket socket = null;
	
	private enum InputType {
		TILE, ENTITY, ADD, REMOVE, EXIT
	}
	
	protected MinicraftConnection(Game gameInstance, Socket socket) {
		super("MinicraftServerThread");
		this.game = gameInstance;
		this.socket = socket;
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
				}
			}
		}).start();
	}
	
	public void run() {
		while(isConnected()) {
			if(in.ready())
				checkInput(in.readLine());
		}
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	protected boolean checkInput(String data) {
		if(data == null || data.length() == 0) return false;
		/**This is implemented in subclasses, so I thought I'd put the notes about --Input Parsing-- here instead.
			Expected format of input is:
			-a short string of text describing what kind of data it is (what is it for, how should it be read/used, what it represents) that serves as an ID string
			-followed by a colon(':')
			-followed by whatever data.
			
			+++ Defined Data Tags so far: +++
				
				EXIT: signals recipient to close their side of the socket connection. no data.
				
				TILE: the data is a tile id, its data, and its position in the level. When recieved, The appropriate tile will be updated, for the level that the player is currently on. If recieved by the server, the level is that of the player entity associated with the socket that sent the data. For clients it's simply the player's current level.
				
				ENTITY: data is a serialized entity object. This includes the entity id, which is simply a random integer unique to that entity. The entity object in the level with the same id is **replaced**.
				
				ADD: used to signal that the serialized entity contained in the data should be **added** to the level, rather than replace an existing entity. the entity id is set then to a vaild value.
				
				REMOVE: used to signal that an entity should be **removed** from the level. the data is just the entity id of the entity to remove.
		*/
		
		String dataType = data.split(":")[0];
		InputType inType = null;
		try {
			inType = Enum.valueOf(InputType, dataType.toUpperCase());
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
		
		int cutidx = data.indexOf(":");
		if(cutidx == -1 || cutidx == data.length-1) {
			// this means there was no semicolon, or there was a semicolon but no data following it. At this point, that is not valid syntax.
			System.err.println("Communication Error: Socket input data has type " + inType + " but no data.");
			return false;
		}
		
		data = data.substring(cutidx+1); // cut off the data type, because we don't need it.
		
		/// now begins the actual handling of the input data.
		Level level = getLevel();
		try {
			
			if(inType == InputType.TILE) {
				/// replace a tile with another tile.
				String[] parts = data.split(",");
				int pos = Integer.parseInt(parts[0]);
				int id = Integer.parseInt(parts[1]);
				int data = Integer.parseInt(parts[2]);
				level.tiles[pos] = (byte) id;
				level.data[pos] = (byte) data;
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
				data.indexOf(";") == data.length() - 1) {
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
		
	}
	
	/** This method is responsible for sending an updated tile. */
	public void sendTileUpdate(int x, int y) {
		Level level = getLevel();
		//int pos =
	}
	
	/** This method is used to get the level that should be updated/used as reference. It is a seperate method so it can be easily overridden, such as for the server threads, which will be updating levels that the host player is not on. */
	protected Level getLevel() {
		return Game.levels[game.currentLevel];
	}
	
	public void endConnection() {
		if (Game.debug) System.out.println("closing socket and ending connection");
		out.println("EXIT"); // send exit signal
		try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}
}
