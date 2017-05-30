package minicraft.network;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MinicraftServerThread extends Thread {
	protected Socket socket = null;
	
	private MinicraftServer serverInstance;

	public MinicraftServerThread(Socket socket, MinicraftServer serverInstance) {
		super("MinicraftServerThread");
		this.socket = socket;
		this.serverInstance = serverInstance;
	}
	
	public void run() {
		try (
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
					socket.getInputStream()));
		) {
			String inputLine, outputLine;
			MinicraftProtocol mcp = new MinicraftProtocol();
			outputLine = mcp.processInput(null);
			out.println(outputLine);
			
			while ((inputLine = in.readLine()) != null) {
				outputLine = mcp.processInput(inputLine);
				out.println(outputLine);
				if (outputLine.equals("Bye"))
					break;
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		serverInstance.threadList.remove(this);
	}
}
