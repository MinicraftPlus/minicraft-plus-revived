package minicraft.network;

import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import minicraft.Game;
import minicraft.entity.Entity;

public class MinicraftServer extends MinicraftConnection {
	
	public ArrayList<MinicraftServerThread> threadList = new ArrayList<MinicraftServerThread>();
	protected ServerSocket serverSocket;
	
	public MinicraftServer(Game game) {
		super(game);
	}
	
	public void run() {
		System.out.println("opening server socket");
		try {
			serverSocket = new ServerSocket(MinicraftConnection.PORT);
			while (serverSocket != null) {
				MinicraftServerThread mst = new MinicraftServerThread(game, serverSocket.accept(), this);
				threadList.add(mst);
			}
		} catch (SocketException ex) { // this should occur when closing the thread.
			ex.printStackTrace();
		} catch (IOException ex) {
			System.err.println("Could not listen on port " + MinicraftConnection.PORT);
		}
		
		System.out.println("closing server socket");
		
		endConnection();
	}
	
	public boolean checkInput(String data) {
		boolean success = true;
		for(MinicraftServerThread thread: threadList) {
			success = success && thread.checkInput(data);
		}
		return success;
	}
	
	public void sendEntityUpdate(int eid, Entity e) {
		for(MinicraftServerThread thread: threadList) {
			thread.sendEntityUpdate(eid, e);
		}
	}
	
	public void sendTileUpdate(int x, int y) {
		for(MinicraftServerThread thread: threadList) {
			thread.sendTileUpdate(x, y);
		}
	}
	
	public void sendNotification(String note, int ntime) {
		for(MinicraftServerThread thread: threadList) {
			thread.sendNotification(note, ntime);
		}
	}
	
	public ArrayList<String> getClientNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(MinicraftServerThread thread: threadList) {
			names.add(thread.getClientName());
		}
		
		return names;
	}
	
	public void endConnection() {
		synchronized ("lock") {
			for(MinicraftServerThread clientThread: threadList)
				clientThread.endConnection();
			
			threadList.clear();
		}
		
		try {
			serverSocket.close();
		} catch (SocketException ex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
		}
	}
	
	public boolean isConnected() {
		return serverSocket != null && threadList.size() > 0;
	}
}
