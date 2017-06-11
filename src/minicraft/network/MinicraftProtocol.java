package minicraft.network;

import java.util.Arrays;
import java.util.List;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.saveload.Load;
import minicraft.screen.WorldGenMenu;
import minicraft.screen.LoadingMenu;

public class MinicraftProtocol {
	
	protected static final int PORT = 4225;
	
	public static enum InputType {
		INVALID, LOGIN, INIT_W, INIT_T, INIT_E, TILE, ENTITY, ADD, REMOVE, DISCONNECT, NOTIFY;
		
		public static InputType[] values = InputType.values();
	}
	
	public static InputType getInputType(byte idx) {
		InputType inType = null;
		if(idx < InputType.values.length)
			inType = InputType.values[idx];
		else {
			System.err.println("Communication Error: Socket data has an invalid input type: " + idx);
			return null;
		}
		
		return inType;
	}
}
