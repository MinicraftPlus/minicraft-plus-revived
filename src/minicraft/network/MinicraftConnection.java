package minicraft.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import minicraft.Game;
import minicraft.item.PotionType;

public abstract class MinicraftConnection extends Thread implements MinicraftProtocol {
	
	private PrintWriter out;
	private BufferedReader in;
	private Socket socket = null;
	
	protected MinicraftConnection(String threadName, Socket socket) {
		super(threadName);
		this.socket = socket;
		
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException ex) {
			System.err.println("failed to initalize i/o streams for socket:");
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			System.err.println("CONNECTION ERROR: null socket, cannot initialize i/o streams...");
			ex.printStackTrace();
		}
	}
	
	public void run() {
		if (Game.debug) System.out.println("starting " + this);
		
		while(isConnected()) {
			if(!checkInput()) {
				// in order to avoid looping through the array as fast as computerly possible, we will wait a short time whenever nothing is read (aka checkInput returns false), before attempting to read again.
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {}
			}
		}
		
		if (Game.debug) System.out.println("run loop ended for " + this + "; ending connection.");
		
		endConnection();
	}
	
	private String currentData = "";
	
	protected final boolean checkInput() {
		int read = -1;
		try {
			if(in.ready()) {
				read = in.read();
				//if (Game.debug) System.out.println(this + " read character from stream: " + ((char)read));
			}
		} catch (IOException ex) {
			System.err.println(this + " had a problem reading its input stream (will continue trying): " + ex.getMessage());
			ex.printStackTrace();
		}
		
		if(read == -1) return false;
		
		while(read != -1) {
			
			if(read > 0) { // if it is valid character that is not the null character, then add it to the string.
				currentData += (char)read;
				//if (Game.debug) System.out.println(this + " successfully read character from input stream: " + ((char)read) );
			}
			else if(currentData.length() > 0) { // read MUST equal 0 at this point, aka a null character; the if statement makes it ignore sequential null characters.
				
				if (Game.debug) System.out.println(this + " completed data packet; parsing...");
				
				InputType inType = MinicraftProtocol.getInputType(currentData.charAt(0));
				//if (Game.debug && inType != InputType.MOVE) System.out.println("SERVER: recieved "+inType+" packet");
				
				if(inType == null)
					System.err.println("SERVER: invalid packet recieved; input type is not valid.");
				else
					parsePacket(inType, currentData.substring(1));
				
				currentData = "";
			}
			
			try {
				if(in.ready())
					read = in.read();
				else
					read = -1;
			} catch (IOException ex) {
				System.err.println(this + " had a problem reading its input stream (will continue trying): " + ex.getMessage());
				ex.printStackTrace();
			}
		}
		
		return true;
	}
	
	protected abstract boolean parsePacket(InputType inType, String data);
	
	protected synchronized void sendData(InputType inType, String data) {
		if (Game.debug) System.out.println(this + ": printing " + inType + " data...");
		out.print(((char)inType.ordinal()+1) + data + "\0");
	}
	
	/// there are a couple methods that are identical in both a server thread, and the client, so I'll just put them here.
	
	public void sendNotification(String note, int notetime) {
		sendData(InputType.NOTIFY, notetime+";"+note);
	}
	
	public void sendPotionEffect(PotionType type, boolean addEffect) {
		sendData(InputType.POTION, addEffect+";"+type.ordinal());
	}
	
	public void endConnection() {
		if(!socket.isClosed()) {
			if (Game.debug) System.out.println("closing socket and ending connection for: " + this);
			
			sendData(InputType.DISCONNECT, "");
			
			try {
				socket.close();
			} catch (IOException ex) {
			}
		}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}
}
