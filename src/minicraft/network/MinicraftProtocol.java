package minicraft.network;

import java.util.Arrays;
import java.util.List;

public interface MinicraftProtocol {
	
	int PORT = 4225;
	//int packetSize = 2048;
	
	enum InputType {
		INVALID, PING, USERNAMES, LOGIN, GAME, INIT, LOAD, TILES, ENTITIES, TILE, ENTITY, PLAYER, MOVE, ADD, REMOVE, DISCONNECT, SAVE, NOTIFY, INTERACT, PUSH, PICKUP, CHESTIN, CHESTOUT, BED, POTION, HURT, DIE, RESPAWN;
		
		public static final InputType[] values = InputType.values();
		public static final List<InputType> serverOnly = Arrays.asList(new InputType[] {INIT, TILES, ENTITIES, TILE, ENTITY, ADD, REMOVE, PLAYER, HURT, GAME});
		public static final List<InputType> entityUpdates = Arrays.asList(new InputType[] {ENTITY, ADD, REMOVE});
		public static final List<InputType> tileUpdates = Arrays.asList(new InputType[] {TILE});
	}
	
	static InputType getInputType(char idxChar) {
		InputType inType = null;
		int idx = ((int) idxChar) - 1; // the "-1" is because 1 is added originally so it does not make a null character, which is used to seperate requests.
		
		if(idx < InputType.values.length && idx >= 0)
			inType = InputType.values[idx];
		else {
			System.err.println("Communication Error: Socket data has an invalid input type: " + idx);
			return null;
		}
		
		return inType;
	}
	
	void endConnection();
	boolean isConnected();
}
