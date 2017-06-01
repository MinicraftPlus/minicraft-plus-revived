package minicraft.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.RemotePlayer;

public class MinicraftServerThread extends Thread {
	
	private MinicraftServer serverInstance;
	private Game game;
	
	public RemotePlayer player;
	public ArrayList<String> currentInput = new ArrayList<String>();
	
	protected Socket socket = null;
	private PrintWriter out;
	private BufferedReader in;
	
	public MinicraftServerThread(Socket socket, MinicraftServer serverInstance) {
		super("MinicraftServerThread");
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
		//if(socket == null) return;
		//System.out.println("sending screen pixels to client...");
		for(int i = 0; i < pixels.length; i++)
			out.print(pixels[i]+(i<pixels.length-1?",":"\n"));
		
		checkConnection();
	}
	
	public void run() {
		// this indefinitely checks for key input sent until disconnected.
		System.out.println("starting server thread connected to " + getClientName());
		//System.out.println("reading key inputs sent from client...");
		while(isConnected()) {
			try {
				if(in.ready()) {
					//System.out.println("reading key input from client...");
					String keys = in.readLine();
					if(keys == null || keys.length() == 0) continue;
					if(keys.endsWith(",")) keys = keys.substring(0, keys.length()-1); // remove trailing ",".
					//System.out.println("adding " + keys + " to input list for client");
					synchronized ("lock") {
						currentInput.addAll(Arrays.asList(keys.split(",")));
					}
				}// else System.out.println("reader not ready");
			} catch (IOException ex) {
				System.out.println("Couldn't get input keys from client " + getClientName());
				ex.printStackTrace();
			}
		}
		
		System.out.println("server connection with " + getClientName() + " is not connected; terminating thread");
		endConnection();
	}
	
	public String getClientName() {
		if(socket != null)
			return socket.getInetAddress().toString() + ":" + socket.getPort();
		
		checkConnection();
		
		return "NULL SOCKET";
	}
	
	protected void checkConnection() {
		if(!isConnected()) endConnection();
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
	
	public boolean isConnected() {
		return serverInstance.isConnected() && serverInstance.threadList.contains(this) && socket != null && socket.isConnected();
	}
}
