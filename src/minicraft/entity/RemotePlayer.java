package minicraft.entity;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.network.MinicraftServerThread;
import minicraft.gfx.Screen;

/** This is used only by the Server runtime, to represent a client player. */
public class RemotePlayer extends Player {
	
	public MinicraftServerThread connection;
	
	public RemotePlayer(Game game, MinicraftServerThread connection) {
		super(game, new InputHandler(game));
		this.connection = connection;
	}
	
	public void tick() {
		for(String key: connection.getInputKeys())
			input.getKey(key).toggle(true);
		
		super.tick();
	}
}
