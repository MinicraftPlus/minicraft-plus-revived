package minicraft.entity;

import minicraft.ClientGame;

/** This is used only by the Server runtime, to represent a client player. */
public class RemotePlayer extends Player {
	
	public RemotePlayer(ClientGame game) {
		super(game, game.input);
	}
}
