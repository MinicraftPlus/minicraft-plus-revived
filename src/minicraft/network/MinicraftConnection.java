package minicraft.network;

import java.net.InetAddress;

public interface MinicraftConnection {
	boolean parsePacket(byte[] data, InetAddress address, int port);
	
	void endConnection();
	boolean isConnected();
	
	default byte[] prependType(MinicraftProtocol.InputType type, byte[] data) {
		byte[] fulldata = new byte[data.length+1];
		fulldata[0] = (byte) type.ordinal();
		for(int i = 1; i < fulldata.length; i++)
			fulldata[i] = data[i-1];
		return fulldata;
	}
}
