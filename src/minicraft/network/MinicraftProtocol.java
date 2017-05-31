package minicraft.network;

public class MinicraftProtocol {
	
	public static final int PORT = 4225;
	
	private enum State {
		WAITING, CONNECTED
	}
	
	private State state = State.WAITING;
}
