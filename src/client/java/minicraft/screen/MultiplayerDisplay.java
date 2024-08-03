package minicraft.screen;

import kong.unirest.Callback;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.SequentialEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.network.Analytics;
import minicraft.util.Logging;

/**
 * @deprecated As multiplayer mode removed. This class is not localized.
 */
@Deprecated
public class MultiplayerDisplay extends Display {

	private static final String domain = "https://playminicraft.com";
	private static final String apiDomain = domain + "/api";

	public static String savedIP = "";
	public static String savedUUID = "";
	public static String savedUsername = "";
	private String email = "";

	private String waitingMessage = "waiting...";
	private String loadingMessage = "nothing";
	private String errorMessage = "";

	private String typing = email;
	private boolean inputIsValid = false;

	private boolean online = false;
	private boolean typingEmail = true;

	private Ellipsis ellipsis = new SequentialEllipsis();

	private enum State {
		WAITING, LOGIN, ENTERIP, LOADING, ERROR
	}

	private State curState;

	public MultiplayerDisplay() {
		this(true);
	}

	public MultiplayerDisplay(boolean pingSite) {

		if (savedUUID == null) savedUUID = "";
		if (email == null) email = "";
		if (savedUsername == null) savedUsername = "";

		if (pingSite)
			contactAccountServer(() -> {
			});
	}

	private void contactAccountServer(Action sitePingCallback) {
		setWaitMessage("testing connection");

		Unirest.get(domain).asEmptyAsync(new Callback<Empty>() {
			@Override
			public void completed(HttpResponse<Empty> response) {
				if (response.getStatus() == 200)
					online = true;
				else
					System.err.println("Warning: Minicraft site ping returned status code " + response.getStatus());

				if (savedUUID.length() > 0) {
					setWaitMessage("attempting log in");
					fetchName(savedUUID);
				}

				if (curState == State.ERROR)
					return;

				// at this point, the game is online, and either the player could log in automatically, or has to enter their
				// email and password.

				if (savedUsername.length() == 0 || savedUUID.length() == 0)
					curState = State.LOGIN; // the player must log in manually.
				else {
					typing = savedIP;
					curState = State.ENTERIP; // the user has sufficient credentials; skip login phase
				}

				sitePingCallback.act();
			}

			@Override
			public void failed(UnirestException e) {
				System.err.println("Website ping failed: " + e.getMessage());
				if (!e.getMessage().equalsIgnoreCase("connection reset by peer"))
					e.printStackTrace();
				cancelled();
			}

			@Override
			public void cancelled() {
				System.err.println("Website ping cancelled.");
				if (savedUsername.length() == 0 || savedUUID.length() == 0) {
					// couldn't validate username, and can't enter offline mode b/c there is no username
					setError("could not connect to playminicraft account server, but no login data saved; cannot enter offline mode.", false);
					return;
				}

				// there is a saved copy of the uuid and username of the last player; use it for offline mode.
				curState = State.ENTERIP;

				sitePingCallback.act();
			}
		});
	}

	@Override
	public void tick(InputHandler input) {
	}

	private void fetchName(String uuid) {
		Analytics.LoginAttempt.ping();
		/// HTTP REQUEST - ATTEMPT TO SEND UUID TO SERVER AND UPDATE USERNAME
		HttpResponse<JsonNode> response = null;

		try {
			response = Unirest.post(apiDomain + "/fetch-name")
				.field("uuid", savedUUID)
				.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		if (response != null) {
			kong.unirest.json.JSONObject json = response.getBody().getObject();
			switch (json.getString("status")) {
				case "error":
					setError("problem with saved login data; please exit and login again.", false);
					savedUUID = "";
					break;

				case "success":
					Logging.NETWORK.debug("Successfully received username from playminicraft server.");
					savedUsername = json.getString("name");
					Analytics.LoginSuccess.ping();
					break;
			}
		} else
			setError("Internal server error: Couldn't fetch username from uuid");
	}


	public void setWaitMessage(String message) {
		waitingMessage = message;
		curState = State.WAITING;
	}

	public void setLoadingMessage(String msg) {
		curState = State.LOADING;
		loadingMessage = msg;
	}

	public void setError(String msg) {
		setError(msg, true);
	}

	private void setError(String msg, boolean overrideMenu) {
		if (curState == State.ERROR) return; // keep original message
		this.curState = State.ERROR;
		errorMessage = msg;
	}

	@Override
	public void render(Screen screen) {
		screen.clear(0);

		switch (curState) {
			case ENTERIP:
				Font.drawCentered("Logged in as: " + savedUsername, screen, 6, Color.get(1, 102, 255, 102));

				if (!online)
					Font.drawCentered("Offline mode: local servers only", screen, Screen.h / 2 - Font.textHeight() * 6, Color.get(1, 153, 153, 255));

				Font.drawCentered("Enter ip address to connect to:", screen, Screen.h / 2 - Font.textHeight() * 2 - 2, Color.get(1, 255));
				Font.drawCentered(typing, screen, Screen.h / 2 - Font.textHeight(), Color.get(1, 255, 255, 102));

				Font.drawCentered("Press Shift-Escape to logout", screen, Screen.h - Font.textHeight() * 7, Color.get(1, 204));
				break;

			case LOGIN:
				String msg = "Enter email:";
				if (!typingEmail)
					msg = "Enter password:";
				Font.drawCentered(msg, screen, Screen.h / 2 - 6, Color.WHITE);

				msg = typing;
				if (!typingEmail)
					//noinspection ReplaceAllDot
					msg = msg.replaceAll(".", ".");
				Font.drawCentered(msg, screen, Screen.h / 2 + 6, (inputIsValid ? Color.get(1, 204) : Color.RED));
				if (!inputIsValid) {
					Font.drawCentered("field is blank", screen, Screen.h / 2 + 20, Color.RED);
				}

				Font.drawCentered("get an account at:", screen, Font.textHeight() / 2 - 1, Color.get(1, 153, 204, 255));
				Font.drawCentered(domain.substring(domain.indexOf("://") + 3) + "/register", screen, Font.textHeight() * 3 / 2, Color.get(1, 153, 204, 255));

				break;

			case WAITING:
				Font.drawCentered(waitingMessage + ellipsis.updateAndGet(), screen, Screen.h / 2, Color.WHITE);
				break;

			case LOADING:
				Font.drawCentered("Loading " + loadingMessage + " from server" + ellipsis.updateAndGet(), screen, Screen.h / 2, Color.WHITE);
				//Font.drawCentered(transferPercent + "%", screen, Screen.h / 2 + 6, Color.WHITE);
				break;

			case ERROR:
				//if(Updater.tickCount % 10 == 0) System.out.println("error message: " + errorMessage);
				Font.drawCentered("Could not connect to server:", screen, Screen.h / 2 - 6, Color.RED);
				FontStyle style = new FontStyle(Color.get(1, 255, 51, 51)).setYPos(Screen.h / 2 + 6);
				Font.drawParagraph(errorMessage, screen, style, 1);
				//Font.drawCentered(errorMessage, screen, Screen.h/2+6, Color.get(1, 255, 51, 51));
				break;
		}

		if (curState == State.ENTERIP || curState == State.ERROR) {
			Font.drawCentered("Press " + Game.input.getMapping("exit") + " to return", screen, Screen.h - Font.textHeight() * 2, Color.GRAY);
		}
	}
}
