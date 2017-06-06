package minicraft.entity;

import minicraft.Game;

/** This is used only by the Server runtime, to represent a client player. */
public class RemotePlayer extends Player {
	
	public RemotePlayer(Game game) {
		super(game, game.input);
	}
}
