package minicraft.network;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.entity.Chest;
import minicraft.entity.particle.Particle;
import minicraft.saveload.Save;
import minicraft.saveload.Load;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;
import minicraft.screen.WorldSelectMenu;
import minicraft.item.Item;
import minicraft.item.Items;

public class MinicraftServer extends Thread implements MinicraftConnection {
	
	private List<RemotePlayer> clientList = java.util.Collections.<RemotePlayer>synchronizedList(new ArrayList<RemotePlayer>());
	/// these two are to make sure all clients recieve all entity additions and removals. They keep track of what entities the client has confirmed that they have added, or removed, by eid.
	//private HashMap<RemotePlayer, ArrayList<Integer>> unconfirmedAdditions = new HashMap<RemotePlayer, ArrayList<Integer>>();
	//private HashMap<RemotePlayer, ArrayList<Integer>> unconfirmedRemovals = new HashMap<RemotePlayer, ArrayList<Integer>>();
	//private long entityCheckTime
	//private long lastTileInteractTime
	
	private Game game;
	private DatagramSocket socket = null;
	
	private String worldPath;
	
	public MinicraftServer(Game game) {
		super("MinicraftServer");
		this.game = game;
		
		game.ISONLINE = true;
		game.ISHOST = true; // just in case.
		//game.player.remove(); // the server has no player...
		
		worldPath = Game.gameDir + "/saves/" + WorldSelectMenu.worldname;
		
		try {
			System.out.println("opening server socket...");
			socket = new DatagramSocket(MinicraftProtocol.PORT);
			start();
		} catch (SocketException ex) {
			System.err.println("failed to open server socket.");
			ex.printStackTrace();
		}
	}
	
	private long timeSinceCheck = 0;
	public void run() {
		if(Game.debug) System.out.println("server started.");
		try {
			socket.setSoTimeout(1500);
		} catch (SocketException ex) {
			System.err.println("SERVER couldn't set socket timeout:");
			ex.printStackTrace();
		}
		while(isConnected()) {
			byte[] data = new byte[MinicraftProtocol.packetSize];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			boolean recieved = false;
			long startTime = System.nanoTime();
			try {
				socket.receive(packet);
				recieved = true;
				timeSinceCheck += System.nanoTime() - startTime;
				if(timeSinceCheck / 1E8 > 15L)
					checkDeltaRecipts();
			} catch (SocketTimeoutException ex) {
				checkDeltaRecipts(); // checks the players for what entity additions and removals they haven't confirmed yet.
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			if(recieved)
				parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
		
		System.out.println("closing server socket");
		
		endConnection();
	}
	
	private void checkDeltaRecipts() {
		timeSinceCheck = 0;
		for(RemotePlayer client: getClientList().toArray(new RemotePlayer[0])) {
			for(Integer eid: client.unconfirmedAdditions.keySet().toArray(new Integer[0])) {
				if((System.nanoTime() - client.unconfirmedAdditions.get(eid)) / 1E9 > 2L) {
					Entity e = Game.getEntity(eid);
					if(e == null) // this could happen if an entity was removed before a client ever realized it was there. In that case, we may as well not tell them.
						client.unconfirmedAdditions.remove(eid);
					else
						sendEntityAddition(e, client, true);
				}
			}
			
			for(Integer eid: client.unconfirmedRemovals.keySet().toArray(new Integer[0])) {
				if((System.nanoTime() - client.unconfirmedRemovals.get(eid)) / 1E9 > 2L)
					sendEntityRemoval(eid, client, true);
			}
			
			/*for(Byte[] tiledata: client.unconfirmedTiles.keySet().toArray(new Byte[0][])) {
				if((System.nanoTime() - client.unconfirmedTiles.get(tiledata)) / 1E9 > 2L) {
					
				}
			}*/
		}
	}
	
	public synchronized List<RemotePlayer> getClientList() {
		return clientList;
	}
	
	private RemotePlayer getIfPlayer(Entity e) {
		//if (Game.debug) System.out.println("SERVER: sending entity update for: " + e.getClass());
		if(e instanceof RemotePlayer) {
			RemotePlayer given = (RemotePlayer) e;
			RemotePlayer filed = getClientPlayer(given.ipAddress, given.port);
			if(filed == null) {
				System.err.println("SERVER encountered a RemotePlayer not in the client list: " + given);
				return null;
			}
			return filed;
		}
		else
			return null;
	}
	
	public void sendEntityUpdate(Entity e) {sendEntityUpdate(e, getIfPlayer(e));}
	/// this is like the above, but it won't do any autodetection, so so can choose who not to send it to.
	public void sendEntityUpdate(Entity e, RemotePlayer sender) {
		byte[] data = prependType(MinicraftProtocol.InputType.ENTITY, (e.eid+";"+e.getUpdates()).getBytes());
		//System.out.println("server sending entity update");
		broadcastData(data, sender);
	}
	
	public void sendTileUpdate(int depth, int x, int y) {
		byte lvlidx = (byte) Game.lvlIdx(depth);
		Level curLevel = Game.levels[lvlidx];
		int pos = x + curLevel.w * y;
		byte[] posbytes = String.valueOf(pos).getBytes();
		byte[] tiledata = new byte[posbytes.length+3];
		tiledata[0] = lvlidx;
		tiledata[1] = curLevel.tiles[pos];
		tiledata[2] = curLevel.data[pos];
		for(int i = 0; i < posbytes.length; i++)
			tiledata[i+3] = posbytes[i];
		
		broadcastData(prependType(MinicraftProtocol.InputType.TILE, tiledata));
	}
	
	public void sendEntityAddition(Entity e) { sendEntityAddition(e, getIfPlayer(e)); }
	public void sendEntityAddition(Entity e, RemotePlayer sender) { sendEntityAddition(e, sender, false); }
	public void sendEntityAddition(Entity e, RemotePlayer sender, boolean giveToSender) {
		String entity = Save.writeEntity(e, false);
		if(entity.length() == 0) return; // this entity is not worth sending across; at this point though, this is not the case with anything.
		byte[] fulledata = new byte[entity.getBytes().length+1];
		fulledata[0] = (byte) Game.lvlIdx(e.level.depth);
		byte[] edata = entity.getBytes();
		for(int i = 0; i < edata.length; i++)
			fulledata[i+1] = edata[i];
		if(Game.debug && !(e instanceof Particle)) System.out.println("SERVER: sending entity addition: " + entity);
		if(!giveToSender) {
			broadcastData(prependType(MinicraftProtocol.InputType.ADD, fulledata), sender);
			for(RemotePlayer client: getClientList().toArray(new RemotePlayer[0]))
				if(client != sender)
					client.unconfirmedAdditions.put(e.eid, System.nanoTime());
		} else if(sender != null) {
			sendData(prependType(MinicraftProtocol.InputType.ADD, fulledata), sender.ipAddress, sender.port);
			sender.unconfirmedAdditions.put(e.eid, System.nanoTime());
		}
		//for(RemotePlayer client: unconfirmedAdditions.keySet().toArray(new RemotePlayer[0])) {
	}
	
	public void sendEntityRemoval(Entity e) { sendEntityRemoval(e, getIfPlayer(e)); }
	public void sendEntityRemoval(Entity e, RemotePlayer sender) { sendEntityRemoval(e.eid, sender, false); }
	public void sendEntityRemoval(int eid, RemotePlayer sender, boolean giveToSender) {
		if(!giveToSender) {
			broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, String.valueOf(eid).getBytes()), sender);
			for(RemotePlayer client: getClientList().toArray(new RemotePlayer[0]))
				if(client != sender)
					client.unconfirmedRemovals.put(eid, System.nanoTime());
		} else if(sender != null) {
			sendData(prependType(MinicraftProtocol.InputType.REMOVE, String.valueOf(eid).getBytes()), sender.ipAddress, sender.port);
			sender.unconfirmedRemovals.put(eid, System.nanoTime());
		}
		//for(RemotePlayer client: unconfirmedRemovals.keySet().toArray(new RemotePlayer[0])) {
	}
	
	public void sendNotification(String note, int notetime) {
		String data = notetime + ";" + note;
		broadcastData(prependType(MinicraftProtocol.InputType.NOTIFY, data.getBytes()));
	}
	
	public void sendPlayerHurt(Player player, int damage, int attackDir) {
		if(!(player instanceof RemotePlayer)) {
			System.out.println("SERVER: encountered regular player, cannot determine ip. can't send hurt packet.");
			return;
		}
		RemotePlayer rp = (RemotePlayer)player;
		if(!getClientList().contains(rp)) {
			System.out.println("SERVER encountered remote player not in client list: " + rp + "; not sending hurt packet.");
			return;
		}
		
		if(damage >= 128) {
			System.out.println("SERVER: damage too high to send across: " + damage + ". lowering to 127.");
			damage = 127;
		}
		
		byte[] data = {(byte)damage, (byte)attackDir};
		//if (Game.debug) System.out.println("SERVER: sending hurt ");
		sendData(prependType(MinicraftProtocol.InputType.HURT, data), rp.ipAddress, rp.port);
	}
	
	public void updateMode(int mode) {
		broadcastData(prependType(MinicraftProtocol.InputType.MODE, (new byte[] {(byte)mode})));
	}
	
	public ArrayList<String> getClientNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(RemotePlayer client: getClientList()) {
			names.add(client.username + ": " + client.ipAddress.getHostAddress() + (Game.debug?" ("+(client.x>>4)+","+(client.y>>4)+")":""));
		}
		
		return names;
	}
	
	public File[] getRemotePlayerFiles() {
		File saveFolder = new File(worldPath);
		
		File[] clientSaves = saveFolder.listFiles(new FilenameFilter() {
			public boolean accept(File file, String name) {
				return name.startsWith("RemotePlayer");
			}
		});
		
		if(clientSaves == null)
			clientSaves = new File[0];
		
		return clientSaves;
	}
	
	public File getRemotePlayerFile(NetworkInterface computer) {
		File[] clientFiles = getRemotePlayerFiles();
		
		for(File file: clientFiles) {
			byte[] mac;
			String macString = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					macString = br.readLine().trim();
				} catch(IOException ex) {
					System.err.println("failed to read line from file.");
					ex.printStackTrace();
				}
			} catch(FileNotFoundException ex) {
				System.err.println("couldn't find remote player file: " + file);
				ex.printStackTrace();
			}
			
			mac = new byte[macString.length()/2];
			for(int i = 0; i < mac.length; i++) {
				mac[i] = Byte.parseByte(macString.substring(i*2, i*2+2), 16);
			}
			
			try {
				if(Arrays.equals(mac, computer.getHardwareAddress())) {
					/// this player has been here before.
					if (Game.debug) System.out.println("remote player file found; returning file " + file.getName());
					return file;
				}
			} catch(SocketException ex) {
				System.err.println("problem fetching mac address.");
				ex.printStackTrace();
			}
		}
		
		return null;
	}
	
	public String getRemotePlayerFileData(NetworkInterface computer) {
		File rpFile = getRemotePlayerFile(computer);
		
		String playerdata = "";
		if(rpFile != null && rpFile.exists()) {
			try {
				String content = Load.loadFromFile(rpFile.getPath(), false); //Files.readAllLines(rpFile.toPath(), StandardCharsets.UTF_8);
				playerdata = content.substring(content.indexOf("\n")+1);
				/*if(lines.size() > 3) {
					System.err.println("remote player file has more lines than expected; has " + lines.size() + ", expected 3");
				}*/
			} catch(IOException ex) {
				System.err.println("failed to read remote player file: " + rpFile);
				ex.printStackTrace();
				return "";
			}
			/*try {
				//BufferedReader br = new BufferedReader(new FileReader(rpFile));
				try {
					String mac = br.readLine(); // this is the mac address.
					String line = "";
					while((line = br.readLine()) != null)
						playerdata += line + "\n"; // should get the right data.
					playerdata = playerdata.substring(0, playerdata.length()-1);
				} catch(IOException ex) {
					System.err.println("failed to read line from file.");
					ex.printStackTrace();
				}
			} catch(FileNotFoundException ex) {
				System.err.println("couldn't find remote player file: " + rpFile);
				ex.printStackTrace();
			}*/
		}
		
		return playerdata;
	}
	
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		if(alldata == null || alldata.length == 0) return false;
		
		//if (Game.debug) System.out.println("SERVER: recieved packet");
		
		MinicraftProtocol.InputType inType = MinicraftProtocol.getInputType(alldata[0]);
		
		if(inType == null) {
			System.err.println("SERVER: invalid packet recieved; input type is not valid.");
			return false;
		}
		
		byte[] data = Arrays.copyOfRange(alldata, 1, alldata.length);
		NetworkInterface computer = null;
		try {
			computer = NetworkInterface.getByInetAddress(address);
		} catch(SocketException ex) {
			System.err.println("couldn't get network interface from address.");
			ex.printStackTrace();
		}
		/*
			wanted behavior for each input type:
			
			-INVALID: print message to console.
			
			-LOGIN
				expected data: username and game version
				behavior: check version to match game version
					-if match:
						+create remote player
						+find good start pos from game instance's surface level
						+send back INIT_W packet to given address and port
						+send ADD packet to all clients *besides* the one that sent the packet, with remote player data
					-if no match:
						+send back INVALID packet with message that versions didn't match; need ____(current) version
			
			-INIT_W: ignore or send back INVALID: not allowed to set data
			
			-INIT_T
				expected data: level number
				behavior: send back list of tiles for specified level
			
			-INIT_E
				expected data: level number
				behavior: send back list of entities on specified level, enough data to construct them anew
			
			-DISCONNECT
				expected data: none
				behavior: send REMOVE packet to the other clients, data being the id of the remote player matching the address from which the DISCONNECT packet was recieved.
			
			-TILE, ENTITY, ADD, REMOVE, NOTIFY: broadcast the packet to all the other clients
			
			recieving ENTITY is a request to have that entity sent in an ADD packet.
		*/
		
		// handle reports of type INVALID
		if(inType == MinicraftProtocol.InputType.INVALID) {
			if (Game.debug) System.out.println("SERVER: recieved error");
			String msg = new String(data).trim();
			System.err.println(msg);
			return false;
		}
		
		/// identify the client who sent this packet:
		RemotePlayer sender = getClientPlayer(address, port);
		// the sender should always be null for LOGIN packets, but not for any other. maybe invalid, but that returns above.
		if(sender == null && inType != MinicraftProtocol.InputType.LOGIN && inType != MinicraftProtocol.InputType.USERNAMES) {
			System.err.println("SERVER error: cannot identify sender of packet; type="+inType.name());
			sendError("You must login first.", address, port);
			return false;
		}
		
		if(MinicraftProtocol.InputType.serverOnly.contains(inType)) {
			/// these are ALL illegal for a client to send.
			System.err.println("SERVER warning: client " + sender + " sent illegal packet type " + inType + " to server.");
			//sendError("You cannot set the world variables.", address, port);
			return false;
		}
		
		switch(inType) {
			case USERNAMES:
				if (Game.debug) System.out.println("SERVER: recieved usernames request");
				String names = "";
				for(RemotePlayer rp: getClientList())
					names += rp.username + "\n";
				
				sendData(prependType(MinicraftProtocol.InputType.USERNAMES, names.getBytes()), address, port);
				return true;
			
			case LOGIN:
				if (Game.debug) System.out.println("SERVER: recieved login request");
				String[] info = new String(data).trim().split(";");
				if (Game.debug) System.out.println("SERVER: login data: " + Arrays.toString(info));
				String username = info[0];
				Load.Version clientVersion = new Load.Version(info[1]);
				// check version; send back invalid if they don't match.
				if(clientVersion.compareTo(new Load.Version(Game.VERSION)) != 0) {
					sendError("wrong game version; need " + Game.VERSION, address, port);
					return false;
				}
				
				/// versions match; make client player
				RemotePlayer clientPlayer = new RemotePlayer(game, username, address, port);
				
				/// now, we need to check if this player has played in this world before. If they have, then all previoeus settings and items and such will be restored.
				String playerdata = ""; // this stores the data fetched from the files.
				
				if(clientPlayer.ipAddress.isLoopbackAddress() && !(game.player instanceof RemotePlayer)) {
					/// this is the first person on localhost. I believe that a second person will be saved as a loopback address (later: jk the first one actually will), but a third will simply overwrite the second.
					if (Game.debug) System.out.println("SERVER: host player found");
					List<String> datalist = new ArrayList<String>();
					Save.writePlayer(game.player, datalist);
					for(String str: datalist)
						if(str.length() > 0)
							playerdata += str + ",";
					playerdata = playerdata.substring(0, playerdata.length()-1) + "\n";
					Save.writeInventory(game.player, datalist);
					//if(Game.debug) System.out.println("SERVER main player inv: " + datalist);
					for(String str: datalist)
						if(str.length() > 0)
							playerdata += str + ",";
					if(datalist.size() == 0)
						playerdata += "null";
					else
						playerdata = playerdata.substring(0, playerdata.length()-1);
					
					game.player = clientPlayer; // all the important data has been saved.
					//(new Load()).loadPlayer("Player", game.player);
				}
				else {
					playerdata = getRemotePlayerFileData(computer);
				}
				
				if(playerdata.length() > 0) {
					/// if a save file was found, then send the data to the client so they can resume where they left off.
					//if (Game.debug) System.out.println("SERVER: sending saved player data: " + playerdata);
					sendData(prependType(MinicraftProtocol.InputType.PLAYER, playerdata.getBytes()), address, port);
					// and now, initialize the RemotePlayer instance with the data.
					(new Load()).loadPlayer(clientPlayer, Arrays.asList(playerdata.split("\\n")[0].split(",")));
					// we really don't need to load the inventory.
				} else
					clientPlayer.findStartPos(Game.levels[Game.lvlIdx(0)]); // find a new start pos
				
				// now, we send the INIT_W packet and notify the others clients.
				
				int playerlvl = Game.lvlIdx(clientPlayer.level != null ? clientPlayer.level.depth : 0);
				if(!Arrays.asList(Game.levels[playerlvl].getEntityArray()).contains(clientPlayer)) // this will be true if their file was already found, since they are added in Load.loadPlayer().
					Game.levels[playerlvl].add(clientPlayer); // add to level (**id is generated here**) and also, maybe, broadcasted to other players?
				//making INIT_W packet
				int[] toSend = {
					clientPlayer.eid,
					Game.levels[playerlvl].w,
					Game.levels[playerlvl].h,
					playerlvl,
					clientPlayer.x,
					clientPlayer.y,
					Game.tickCount,
					Game.pastDay1?1:0,
					ModeMenu.mode,
					game.scoreTime // this will only be used if in score mode.
				};
				String sendString = "";
				for(int val: toSend)
					sendString += val+",";
				/// send client world info
				if (Game.debug) System.out.println("SERVER: sending INIT_W packet");
				sendData(prependType(MinicraftProtocol.InputType.INIT_W, sendString.getBytes()), address, port);
				getClientList().add(clientPlayer); /// file client player
				//unconfirmedAdditions.put(clientPlayer, new ArrayList<Integer>());
				//unconfirmedRemovals.put(clientPlayer, new ArrayList<Integer>());
				/// tell others of client joining
				//if (Game.debug) System.out.println("SERVER: broadcasting player addition");
				//sendEntityAddition(clientPlayer, clientPlayer);
				return true;
			
			case INIT_T:
				//if (Game.debug) System.out.println("SERVER: recieved tiles request");
				// send back the tiles in the level specified.
				int levelidx = (int) data[0];
				if(levelidx >= 0 && levelidx < Game.levels.length) {
					byte[] tiledata = new byte[Game.levels[levelidx].tiles.length*2 + 2];
					//tiledata[0] = (byte) MinicraftProtocol.InputType.INIT_T.ordinal();
					//tiledata[1] = 0; // the index to start on.
					for(int i = 0; i < tiledata.length/2 - 3; i++) {
						tiledata[i*2 + 2] = Game.levels[levelidx].tiles[i];
						tiledata[i*2+1 + 2] = Game.levels[levelidx].data[i];
					}
					int pkSize = MinicraftProtocol.packetSize - 2;
					for(int i = 0; i < tiledata.length; i += pkSize) {
						int endidx = i+1+pkSize;
						byte[] curData = Arrays.copyOfRange(tiledata, i, (endidx>=tiledata.length?tiledata.length-1:endidx));
						curData[0] = (byte) MinicraftProtocol.InputType.INIT_T.ordinal();
						curData[1] = (byte) (i/pkSize); // this value tells the client what index to start filling on.
						if(Game.debug) System.out.println("SERVER: sending tiles from " + (i/2) + " to " + ((i+curData.length-2)/2) + " to client; passed value: " + curData[1]);
						sendData(curData, address, port);
					}
					return true;
				} else {
					System.err.println("SERVER warning: Client " + sender + " tried to request tiles from nonexistent level " + levelidx);
					sendError("requested level ("+levelidx+") does not exist.", address, port);
					return false;
				}
			
			case INIT_E:
				if (Game.debug) System.out.println("SERVER: recieved entities request");
				// send back the entities in the level specified.
				int lvlidx = (int) data[0];
				if(lvlidx >= 0 && lvlidx < Game.levels.length) {
					//System.out.println("SERVER requesting access to entities...");
					Entity[] entities = Game.levels[lvlidx].getEntityArray();
					//System.out.println("access granted.");
					int i = 0;
					while(i < entities.length) {
						String edata = "";
						for(i = i; i < entities.length; i++) {
							String curEntityData = "";
							if(entities[i] != sender) {
								curEntityData = Save.writeEntity(entities[i], false) + ",";
								if(Game.debug && entities[i] instanceof Player) System.out.println("SERVER: sending player in INIT_E packet: " + entities[i]);
							}
							if(curEntityData.getBytes().length + edata.getBytes().length + "END".getBytes().length > MinicraftProtocol.packetSize-1 && curEntityData.length() > 1) {
								// there are too many entities to send in one packet.
								break;
							}
							// there is enough space.
							if(curEntityData.length() > 1) // 1 b/c of the "," added; this prevents entities that aren't saved from causing ",," to appear.
								edata += curEntityData;
						}
						if(i >= entities.length)
							edata += "END"; // tell the client there are no more entities to send.
						else
							edata = edata.substring(0, edata.length()-1); // cut off trailing comma
						sendData(prependType(MinicraftProtocol.InputType.INIT_E, edata.getBytes()), address, port);
					}
					
					return true;
				} else {
					System.err.println("SERVER warning: Client " + sender + " tried to request entities from nonexistent level " + lvlidx);
					sendError("requested level does not exist.", address, port);
					return false;
				}
			
			case DISCONNECT:
				if (Game.debug) System.out.println("SERVER: recieved disconnect request");
				/// tell the other clients to remove this client from their games
				broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, String.valueOf(sender.eid).getBytes()), sender);
				getClientList().remove(sender); // remove from the server list
				sender.remove(); // removes the remote player from the level.
				return true;
			
			case ADD:
				// addition confirmed; data should be id.
				sender.unconfirmedAdditions.remove(Integer.parseInt(new String(data).trim()));
				return true;
			
			case REMOVE:
				// removal confirmed; data should be id.
				sender.unconfirmedRemovals.remove(Integer.parseInt(new String(data).trim()));
				return true;
			
			case ENTITY:
				// client wants the specified entity sent in an ADD packet, becuase it couldn't find that entity upon recieving an ENTITY packet from the server.
				Entity entityToSend = Game.getEntity(Integer.parseInt((new String(data)).trim()));
				if(entityToSend == null) {
					/// well THIS would be a problem, I think. Though... Actually, not really. It just means that an entity was removed between the time of sending an update for it, and the client then asking for it to be added. But since it would be useless to add it at this point, we'll just ignore the request.
					return true;
				}
				if(sender.unconfirmedAdditions.containsKey(entityToSend.eid))
					return false; /// becuase the client will be recieving a lot of the updates, it will ask for the entity a lot. So only send the data manually once, and then ignore all future requests as long as the first still stands.
				else
					sendEntityAddition(entityToSend, sender, true);
				return true;
			
			case SAVE:
				if (Game.debug) System.out.println("SERVER: recieved player save");
				String filedata = new String(data).trim();
				/// save this client's data to a file.
				String filename = ""; // this will hold the path to the file that will be saved to.
				/// first, determine if this is the main player. if not, determine if a file already exists for this client. if not, find an available file name. for simplicity, we will just count the number of remote player files saved.
				
				if(sender == game.player) {
					if (Game.debug) System.out.println("SERVER: identified SAVE packet sender as host");
					String[] parts = filedata.split("\\n");
					List<String> datastrs = new ArrayList<String>();
					
					Save save = new Save(sender);
					for(String str: parts[0].split(","))
						datastrs.add(str);
					save.writeToFile(save.location+"Player"+Save.extention, datastrs);
					datastrs.clear();
					for(String str: parts[1].split(","))
						datastrs.add(str);
					save.writeToFile(save.location+"Inventory"+Save.extention, datastrs);
					
					return true;
				}
				
				File rpFile = getRemotePlayerFile(computer);
				if(rpFile != null && rpFile.exists()) // check if this remote player already has a file.
					filename = rpFile.getName();
				else {
					File[] clientSaves = getRemotePlayerFiles();
					int numFiles = clientSaves.length;
					filename = "RemotePlayer"+numFiles+Save.extention;
				}
				
				byte[] macAddress = null;
				try {
					macAddress = computer.getHardwareAddress();
				} catch(SocketException ex) {
					System.err.println("couldn't get mac address.");
					ex.printStackTrace();
				}
				if(macAddress == null) {
					System.err.println("SERVER: error saving player file; couldn't get client MAC address.");
					return false;
				}
				
				String macString = "";
				for(byte b: macAddress) {
					String hexInt = Integer.toHexString((int)b);
					if (Game.debug) System.out.println("mac byte as hex int: " + hexInt);
					macString += hexInt.substring(hexInt.length()-2);
				}
				if(Game.debug) System.out.println("mac as hex: " + macString);
				filedata = macString + "\n" + filedata;
				
				String filepath = worldPath+"/"+filename;
				//java.nio.file.Path theFile = (new File(filepath)).toPath();
				try {
					Save.writeToFile(filepath, filedata.split("\\n"), false);
					//Files.write(theFile, Arrays.asList(filedata.split("\\n")), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					//Files.setAttribute(theFile, "isRegularFile", (new Boolean(true)), (java.nio.file.LinkOption[])null);
				} catch(IOException ex) {
					System.err.println("problem writing remote player to file: " + filepath);
					ex.printStackTrace();
				}
				// the above will hopefully write the data to file.
				return true;
			
			case CHESTIN: case CHESTOUT:
				if (Game.debug) System.out.println("SERVER: recieved chest request");
				String[] contents = new String(data).trim().split(";");
				int eid = Integer.parseInt(contents[0]);
				Entity e = Game.getEntity(eid);
				if(e == null || !(e instanceof Chest)) {
					System.err.println("SERVER error with CHESTOUT request: Specified chest entity did not exist or was not a chest.");
					return false;
				}
				Chest chest = (Chest) e;
				
				if(inType == MinicraftProtocol.InputType.CHESTIN) {
					Item item = Items.get(contents[1]);
					if(item == null) {
						System.err.println("SERVER error with CHESTIN request: specified item could not be found from string: " + contents[1]);
						return false;
					}
					chest.inventory.add(item);
				}
				else if(inType == MinicraftProtocol.InputType.CHESTOUT) {
					int index = Integer.parseInt(contents[1]);
					if(index >= chest.inventory.invSize() || index < 0) {
						System.err.println("SERVER error with CHESTOUT request: specified chest inv index is out of bounds: "+index+"; inv size:"+chest.inventory.invSize());
						return false;
					}
					// was a valid index
					Item removed = chest.inventory.remove(index);
					
					sendData(prependType(MinicraftProtocol.InputType.CHESTOUT, removed.getData().getBytes()), address, port); // send back the *exact* same packet as was sent; the client will handle it accordingly, by changing their inventory.
				}
				
				sendEntityUpdate(chest, sender);
				return true;
			
			case PICKUP:
				//if (Game.debug) System.out.println("SERVER: recieved itementity pickup request");
				int ieid = Integer.parseInt(new String(data).trim());
				Entity entity = Game.getEntity(ieid);
				if(entity == null || !(entity instanceof ItemEntity) || entity.removed) {
					System.err.println("SERVER could not find item entity in PICKUP request: " + ieid + ". Telling client to remove...");
					sendEntityRemoval(ieid, sender, true); // will happen when another guy gets to it first, so the this client shouldn't have it on the level anymore. It could also happen if the client didn't recieve the packet telling them to pick it up... in which case it will be lost, but oh well.
					return false;
				}
				
				entity.remove();
				if(Game.debug) System.out.println("SERVER: item entity pickup approved: " + entity);
				sendData(alldata, address, port);
				broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, String.valueOf(entity.eid).getBytes()), sender);
				return true;
			
			case INTERACT:
				if (Game.debug) System.out.println("SERVER: recieved interaction request");
				//x, y, dir, item
				String[] pinfo = new String(data).trim().split(";");
				// since this should be the most up-to-date data, just update the remote player coords with them.
				int ox = sender.x>>4, oy = sender.y>>4;
				sender.x = Integer.parseInt(pinfo[0]);
				sender.y = Integer.parseInt(pinfo[1]);
				sender.dir = Integer.parseInt(pinfo[2]);
				sender.activeItem = Items.get(pinfo[3]); // this can be null; and that's fine, it means a fist. ;)
				int arrowCount = Integer.parseInt(pinfo[4]);
				int curArrows = sender.inventory.count(Items.get("arrow"));
				if(curArrows < arrowCount)
					sender.inventory.add(Items.get("arrow"), arrowCount-curArrows);
				if(curArrows > arrowCount)
					sender.inventory.removeItems(Items.get("arrow"), curArrows-arrowCount);
				sender.attack(); /// NOTE the player may fire an arrow, but we won't tell because that player will update it theirself.
			
			case MOVE:
				/// the player moved.
				//if (Game.debug) System.out.println("SERVER: recieved move packet");
				String[] movedata = new String(data).trim().split(";");
				sender.x = Integer.parseInt(movedata[0]);
				sender.y = Integer.parseInt(movedata[1]);
				sender.dir = Integer.parseInt(movedata[2]);
				sender.walkDist += 8; // hopefully will make walking animations work.
				sendEntityUpdate(sender);
				return true;
			
			/// I'm thinking this should end up never being used... oh, well maybe for notifications.
			default:
				System.out.println("SERVER used default behavior for input type " + inType);
				broadcastData(alldata, sender);
				return true;
		}
	}
	
	public void broadcastData(byte[] data) {
		for(int i = 0; i < getClientList().size(); i++) {
			RemotePlayer client = getClientList().get(i);
			sendData(data, client.ipAddress, client.port);
		}
	}
	
	public void broadcastData(byte[] data, RemotePlayer sender) {
		if(sender == null) {
			broadcastData(data);
			return;
		}
		for(int i = 0; i < getClientList().size(); i++) {
			RemotePlayer client = getClientList().get(i);
			if(client != sender) // send this packet to all EXCEPT the specified one.
				sendData(data, client.ipAddress, client.port);
		}
	}
	
	//static int sends = 0;
	public void sendData(byte[] data, InetAddress ip, int port) {
		//sends++;
		//if(sends > 50) System.exit(0);
		DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
		String intype = MinicraftProtocol.getInputType(data[0]).name();
		if (Game.debug && !intype.equalsIgnoreCase("entity") && !intype.equalsIgnoreCase("add")) System.out.println("SERVER: sending "+intype+" data to: " + ip);
		try {
			socket.send(packet);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void sendError(String message, InetAddress ip, int port) {
		if (Game.debug) System.out.println("SERVER: sending error to " + ip + ": \"" + message + "\"");
		sendData(prependType(MinicraftProtocol.InputType.INVALID, message.getBytes()), ip, port);
	}
	
	public RemotePlayer getClientPlayer(InetAddress ip, int port) {
		for(RemotePlayer client: getClientList())
			if(client.ipAddress.equals(ip) && client.port == port)
				return client;
		
		return null;
	}
	
	public void endConnection() {
		if (Game.debug) System.out.println("SERVER: ending connection");
		synchronized ("lock") {
			for(RemotePlayer client: getClientList()) {
				broadcastData(prependType(MinicraftProtocol.InputType.DISCONNECT, (new byte[0])));
			}
			
			getClientList().clear();
		}
		
		try {
			socket.close();
		} catch (NullPointerException ex) {}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed();
	}
}
