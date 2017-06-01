package minicraft.entity;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.network.MinicraftServerThread;
import minicraft.gfx.Screen;

/** This is used only by the Server runtime, to represent a client player. */
public class RemotePlayer extends Player {
	
	public MinicraftServerThread connection;
	public Screen screen, lightScreen;
	
	public RemotePlayer(Game game, MinicraftServerThread connection) {
		super(game, new InputHandler(game, false));
		this.connection = connection;
		screen = new Screen(game.screen);
		lightScreen = new Screen(game.lightScreen);
	}
	
	public void tick() {
		if(connection == null || !connection.isConnected()) {
			System.out.println("socket is disconnected from client");
			if(connection != null)
				connection.endConnection();
			else {
				remove();
				Game.server.threadList.remove(connection); // really is just remove(null)
			}
			return;
		}
		
		//input.tick(); // this is the only place that ticks the input; normal player input is a reference to game.input and so doesn't need to be ticked seperately.
		//input.releaseAll();
		synchronized ("lock") {
			//System.out.println("processing unread key presses from client "+connection.getClientName()+", to remote player input");
			for(String keystate: connection.currentInput) {
				String[] parts = keystate.split("=");
				String key = parts[0];
				boolean pressed = Boolean.parseBoolean(parts[1]);
				//System.out.println("toggling key " + key + " on remote player " + connection.getClientName() + " to " + pressed);
				input.pressKey(key, pressed);
			}
			connection.currentInput.clear();
		}
		
		/*java.util.List<String> pressedKeys = input.getAllPressedKeys();
		if(pressedKeys.size() > 0)
			System.out.println("remote player ticked; input keys pressed: " + pressedKeys + ". Ticking super (Player)...");
		*/
		super.tick();
	}
}
