package minicraft.network;

import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.SocketException;
import java.io.IOException;
import minicraft.Game;

public class MinicraftServer {
	
	public ArrayList<MinicraftServerThread> threadList = new ArrayList<MinicraftServerThread>();
	protected ServerThread serverThread;
	
	public MinicraftServer() {
		serverThread = new ServerThread(MinicraftProtocol.PORT);
		serverThread.start();
	}
	
	protected class ServerThread extends Thread {
		int portNumber;
		public boolean listening = true;
		public ServerSocket serverSocket = null;
		
		public ServerThread(int port) {
			super("ServerThread");
			portNumber = port;
		}
		
		public void run() {
			try {
				serverSocket = new ServerSocket(portNumber);
				while (listening) {
					MinicraftServerThread mst = new MinicraftServerThread(serverSocket.accept(), MinicraftServer.this);
					threadList.add(mst);
					checkSockets();
					if(threadList.contains(mst))
						mst.start();
					else
						System.out.println("new connection to server was unsuccessful; not starting thread");
				}
			} catch (SocketException ex) { // this should occur when closing the thread.
			} catch (IOException ex) {
				System.err.println("Could not listen on port " + portNumber);
			}
			
			synchronized ("lock") {
				for(MinicraftServerThread clientThread: threadList)
					clientThread.endConnection();
				
				threadList.clear();
			}
		}
	}
	
	public synchronized void checkSockets() {
		for(MinicraftServerThread thread: threadList.toArray(new MinicraftServerThread[0])) {
			if(thread.socket == null || thread.socket.isClosed())
				threadList.remove(thread);
		}
	}
	
	public void endConnection() {
		serverThread.listening = false;
		try {
			serverThread.serverSocket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
	}
	
	public boolean isConnected() {
		checkSockets();
		return serverThread.serverSocket != null && threadList.size() > 0 && serverThread.listening;
	}
}
