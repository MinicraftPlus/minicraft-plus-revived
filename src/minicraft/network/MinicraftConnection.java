package minicraft.network;

import java.util.List;
import java.util.Arrays;
import java.net.InetAddress;

public interface MinicraftConnection {
	
	int PORT = 4225;
	int packetSize = 2048;
	
	enum InputType {
		INVALID, USERNAMES, LOGIN, GAME, INIT, TILES, ENTITIES, TILE, ENTITY, PLAYER, MOVE, ADD, REMOVE, DISCONNECT, SAVE, NOTIFY, INTERACT, PICKUP, CHESTIN, CHESTOUT, BED, HURT, DIE, RESPAWN;
		
		public static final InputType[] values = InputType.values();
		public static final List<InputType> serverOnly = Arrays.asList(new InputType[] {INIT, TILE, PLAYER, HURT, GAME});
	}
	
	static InputType getInputType(byte idx) {
		InputType inType = null;
		if(idx < InputType.values.length)
			inType = InputType.values[idx];
		else {
			System.err.println("Communication Error: Socket data has an invalid input type: " + idx);
			return null;
		}
		
		return inType;
	}
	
	boolean parsePacket(byte[] data, InetAddress address, int port);
	
	void endConnection();
	boolean isConnected();
	
	default byte[] prependType(InputType type, byte[] data) {
		byte[] fulldata = new byte[data.length+1];
		fulldata[0] = (byte) type.ordinal();
		for(int i = 1; i < fulldata.length; i++)
			fulldata[i] = data[i-1];
		return fulldata;
	}
}
