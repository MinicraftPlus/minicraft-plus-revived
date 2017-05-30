package minicraft.network;

public class MinicraftProtocol {
	
	public static final int PORT = 4225;
	
	private enum State {
		WAITING, CONNECTED
	}
	
	private State state = State.WAITING;
	
	public String processInput(String theInput) {
		//String theOutput = null;
		/*
		switch(state) {
			case WAITING:
				theOutput = "Waiting";
				state = State.SENTKNOCKKNOCK;
				break;
			
			case SENTKNOCKKNOCK:
				if (theInput.equalsIgnoreCase("Who's there?")) {
					theOutput = clues[currentJoke];
					state = State.SENTCLUE;
				} else {
					theOutput = "You're supposed to say \"Who's there?\"! " +
					"Try again. Knock! Knock!";
				}
				break;
			
			case SENTCLUE:
				if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
					theOutput = answers[currentJoke] + " Want another? (y/n)";
					state = State.ANOTHER;
				} else {
					theOutput = "You're supposed to say \"" +
					clues[currentJoke] +
					" who?\"" +
					"! Try again. Knock! Knock!";
					state = State.SENTKNOCKKNOCK;
				}
				break;
			
			case ANOTHER:
				if (theInput.equalsIgnoreCase("y")) {
					theOutput = "Knock! Knock!";
					if (currentJoke == (NUMJOKES - 1))
						currentJoke = 0;
					else
						currentJoke++;
					state = State.SENTKNOCKKNOCK;
				} else {
					theOutput = "Bye.";
					state = State.WAITING;
				}
				break;
		}*/
		return theInput + ":" + state.name();
	}
}
