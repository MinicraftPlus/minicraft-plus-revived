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
public class MinicraftClient extends MinicraftConnection {
	
	public MinicraftClient(Game game, String hostName) {
		super(game, hostName);
	}
	/*
	public void sendCachedInput(InputHandler input) {
		if(!isConnected()) return;
		
		//presses += input.getCurModifiers().replace("-", ","); // modifier key presses are not tracked normally, only here. Since they don't repeatly trigger a keypress event, this does the repetition.
		if(presses.length() > 0) {
			if (Game.debug) System.out.println("sending key press data to server: " + presses);
			out.println(presses);
			presses = "";
		}
	}*/
}
