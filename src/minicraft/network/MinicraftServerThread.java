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
import minicraft.level.Level;

public class MinicraftServerThread extends MinicraftConnection {
	
	private MinicraftServer serverInstance;
	
	public MinicraftServerThread(Game game, Socket socket, MinicraftServer serverInstance) {
		super(game, socket);
		System.out.println("created server thread");
		this.serverInstance = serverInstance;
	}
	
	public void run() {
		System.out.println("running server thread...");
		if(isConnected()) {
			System.out.println("connected");
			Level level = Game.levels[3];
			out.println("INIT:"+game.tickCount+","+level.w+","+level.h);
			System.out.println("sending tile params");
			for(int i = 0; i < level.tiles.length; i++)
				out.println("TILE:"+i+","+level.tiles[i]+","+level.data[i]);
			
			//System.out.println("sending level...");
			//out.println(levelprint);
			System.out.println("level sent.");
			out.println("START");
			//for(int i = 0; i < level.entities.length; i++) {
			//	out.println("ADD:"+i+","+level.entities.get(i)/*.serialize()*/);
			//}
			super.run();
		} else
			System.out.println("server side socket is not connected to " + getClientName());
	}
	
	public String getClientName() {
		if(socket != null)
			return socket.getInetAddress().toString() + ":" + socket.getPort();
		
		return "NULL SOCKET";
	}
	
	public void endConnection() {
		super.endConnection();
		serverInstance.threadList.remove(this);
		//clientPlayer.remove(); // removes player from level
	}
}
