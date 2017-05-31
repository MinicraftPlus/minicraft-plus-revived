package minicraft.network;

import java.util.ArrayList;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;

public class MinicraftServerThread {
	
	protected Socket socket = null;
	private MinicraftServer serverInstance;
	private RemotePlayer player;
	private Game game;
	
	private PrintWriter out;
	private BufferedReader in;
	
	public MinicraftServerThread(Socket socket, MinicraftServer serverInstance) {
		this.socket = socket;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		this.serverInstance = serverInstance;
		this.game = serverInstance.game;
		player = new RemotePlayer(game, this);
		game.levels[3].add(player);
	}
	
	public void sendScreenPixels(int[] pixels) {
		if(socket == null) return;
		
		//try {
			for(int i = 0; i < pixels.length; i++)
				out.print(pixels[i]+(i<pixels.length-1?",":"\n"));
			
		/*} catch (IOException ex) {
			System.err.println("Couldn't send pixels to client " + getClientName());
			ex.printStackTrace();
		}*/
		
		checkConnection();
	}
	
	public String[] getInputKeys() {
		if(socket == null) return new String[0];
		
		try {
			String keys = in.readLine();
			if(keys.endsWith(",")) keys = keys.substring(0, keys.length()-1); // remove trailing ",".
			return keys.split(",");
		} catch (IOException ex) {
			System.err.println("Couldn't get input keys from client " + getClientName());
			ex.printStackTrace();
		}
		
		checkConnection();
		
		return new String[0];
	}
	
	public String getClientName() {
		if(socket != null)
			return socket.getInetAddress().toString() + socket.getPort();
		
		checkConnection();
		
		return "NULL SOCKET";
	}
	
	protected void checkConnection() {
		if(socket.isClosed() || !serverInstance.serverThread.listening)
			endConnection();
	}
	
	public void endConnection() {
		System.out.println("ending connection to client " + getClientName());
		try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
		
		serverInstance.threadList.remove(this);
		player.remove(); // removes player from level
	}
}
