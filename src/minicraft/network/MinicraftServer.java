package minicraft.network;

import java.util.ArrayList;
import java.net.ServerSocket;
import java.io.IOException;

public class MinicraftServer {
	
	public ArrayList<MinicraftServerThread> threadList = new ArrayList<MinicraftServerThread>();
	
	public MinicraftServer() {
		new ServerThread(MinicraftProtocol.PORT).start();
	}
	
	private class ServerThread extends Thread {
		int portNumber;
		
		public ServerThread(int port) {
			super("ServerThread");
			portNumber = port;
		}
		
		public void run() {
			boolean listening = true;
			
			try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
				while (listening) {
					MinicraftServerThread mst = new MinicraftServerThread(serverSocket.accept(), MinicraftServer.this);
					threadList.add(mst);
					mst.start();
				}
			} catch (IOException ex) {
				System.err.println("Could not listen on port " + portNumber);
			}
		}
	}
}
