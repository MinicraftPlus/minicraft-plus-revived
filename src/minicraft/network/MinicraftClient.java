package minicraft.network;

import java.util.ArrayList;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import minicraft.Game;
import minicraft.InputHandler;

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient {
	
	public static final String hostName = "localhost";
	
	public Socket socket = null;
	public boolean done = false;
	private Game game;
	
	private PrintWriter out;
	private BufferedReader in;
	
	public MinicraftClient(Game game) {
		this.game = game;
		(new Thread() {
			public void run() {
				try {
					socket = new Socket(hostName, MinicraftProtocol.PORT);
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host " + hostName);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to " +
						hostName);
				}
				
				try {
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (IOException ex) {
					//System.out.println("socket found closed from client while trying to send input: " + socket.isClosed());
					ex.printStackTrace();
					//if(socket.isClosed())
						//endConnection();
				}/* catch (IOException ex) {
					System.err.println("failed sending input to host " + hostName + ":");
					ex.printStackTrace();
				}*/
				done = true;
				System.out.println("socket is open at socket creation: " + !socket.isClosed());
			}
		}).start();
	}
	
	public int[] getScreenPixels() {
		if(!done) return new int[0];
		try {
			String[] pixelStrings = in.readLine().split(",");
			int[] pixels = new int[pixelStrings.length];
			for(int i = 0; i < pixelStrings.length; i++)
				pixels[i] = Integer.parseInt(pixelStrings[i]);
			
			return pixels;
			
		} catch (SocketException ex) {
			if(socket.isClosed())
				endConnection();
			else ex.printStackTrace();
		} catch (IOException ex) {
			System.err.println("Couldn't get pixels from the connection to " +
				hostName);
			System.err.println(ex.getMessage()); // I'm testing this out.
		}
		
		return null;
	}
	
	/// TODO this is repeatedly called after closing socket; I need a way to find out, from one side of the socket, if the other side is closed. I think...
	public void sendInput(InputHandler input) {
		if(!done) return;
		//try {
			String importantkeys = "attack,up,down,left,right,menu";
			for(String key: importantkeys.split(","))
				if(input.getKey(key).clicked)
					out.print(key+",");
			
		//} catch
	}
	
	public void endConnection() {
		System.out.println("closing client socket and ending connection");
		try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
		
		Game.client = null;
		Game.ISONLINE = false;
	}
	
	public boolean isConnected() {
		return done && socket != null && !socket.isClosed();// && socket.isConnected();
	}
}
