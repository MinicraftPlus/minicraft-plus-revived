package minicraft.network;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MinicraftClient {
	
	public static final String hostName = "localhost";
	
	public Socket socket = null;
	public boolean done = false;
	
	public MinicraftClient() {
		(new Thread() {
			public void run() {
				try {
					socket = new Socket(hostName, MinicraftProtocol.PORT);
					System.out.println(connect());
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host " + hostName);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to " +
						hostName);
				}
				done = true;
			}
		}).start();
	}
	
	public boolean connect() throws IOException {
		try (
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		) {
			BufferedReader stdIn =
				new BufferedReader(new InputStreamReader(System.in));
			
			String fromServer;
			String fromUser;
			
			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server: " + fromServer);
				if (fromServer.contains("end"))
					break;
				
				fromUser = stdIn.readLine();
				if (fromUser != null) {
					System.out.println("Client: " + fromUser);
					out.println(fromUser);
				}
			}
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
				hostName);
			return false;
		}
		
		return true;
	}
}
