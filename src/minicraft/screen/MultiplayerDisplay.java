package minicraft.screen;

import java.io.InputStream;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Ellipsis;
import minicraft.gfx.Ellipsis.SequentialEllipsis;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Save;
import minicraft.screen.entry.RangeEntry;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

public class MultiplayerDisplay extends Display {
	
	private static String domain = "https://playminicraft.com";
	private static String apiDomain = domain+"/api";
	
	public static String savedIP = "";
	public static String savedUUID = "";
	public static String savedUsername = "";
	private String email = "";
	
	private String waitingMessage = "waiting...";
	private String loadingMessage = "nothing";
	private String errorMessage = "";
	
	private String typing = email;
	private boolean inputIsValid = false;
	
	private final RangeEntry connectTimeout = new RangeEntry("Timeout (sec) (0=none)", 0, 120, (int)Math.ceil(MinicraftClient.DEFAULT_CONNECT_TIMEOUT/1000f));
	
	private boolean online = false;
	private boolean typingEmail = true;
	
	private Ellipsis ellipsis = new SequentialEllipsis();
	
	private enum State {
		WAITING, LOGIN, ENTERIP, LOADING, ERROR
	}
	
	private State curState;
	
	public MultiplayerDisplay() { this(true); }
	public MultiplayerDisplay(boolean pingSite) {
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if(savedUUID == null) savedUUID = "";
		if(email == null) email = "";
		if(savedUsername == null) savedUsername = "";
		
		if(pingSite)
			contactAccountServer(() -> {});
	}
	
	// this automatically sets the ipAddress.
	public MultiplayerDisplay(String ipAddress) {
		this(false);
		contactAccountServer(() -> {
			if(curState == State.ENTERIP) { // login was automatic
				setWaitMessage("connecting to server");
				Game.client = new MinicraftClient(savedUsername,this, ipAddress);
			} else
				savedIP = ipAddress; // must login manually, so the ip address is saved for now.
		});
	}
	
	private void contactAccountServer(Action sitePingCallback) {
		setWaitMessage("testing connection");
		
		Unirest.get(domain).asBinaryAsync(new Callback<InputStream>() {
			@Override
			public void completed(HttpResponse<InputStream> httpResponse) {
				if(httpResponse.getStatus() == 200)
					online = true;
				else
					System.err.println("warning: minicraft site ping returned status code " + httpResponse.getStatus());
				
				if(savedUUID.length() > 0) {
					// there is a previous login that can be used; check that it's valid
					setWaitMessage("attempting log in");
					//if (Game.debug) System.out.println("fetching username for uuid");
					fetchName(savedUUID);
				}
				
				if(curState == State.ERROR)
					return;
				
				// at this point, the game is online, and either the player could log in automatically, or has to enter their
				// email and password.
				
				if(savedUsername.length() == 0 || savedUUID.length() == 0)
					curState = State.LOGIN; // the player must log in manually.
				else {
					typing = savedIP;
					curState = State.ENTERIP; // the user has sufficient credentials; skip login phase
				}
				
				sitePingCallback.act();
			}
			
			@Override
			public void failed(UnirestException e) {
				System.err.println("website ping failed: "+e.getMessage());
				if(!e.getMessage().equalsIgnoreCase("connection reset by peer"))
					e.printStackTrace();
				cancelled();
			}
			
			@Override
			public void cancelled() {
				System.err.println("website ping cancelled.");
				if(savedUsername.length() == 0 || savedUUID.length() == 0) {
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
		switch(curState) {
			case LOGIN:
				typing = input.addKeyTyped(typing, null);
				
				inputIsValid = typing.length() != 0;
				
				if(input.getKey("select").clicked && inputIsValid) {
					if(typingEmail) {
						typingEmail = false;
						email = typing;
						//new Save(game);
						typing = "";
					} else {
						login(email, typing); // typing = password
						typing = "";
					}
					return;
				}
				break;
			
			case ENTERIP:
				typing = input.addKeyTyped(typing, null);
				connectTimeout.tick(input);
				if(input.getKey("select").clicked) {
					setWaitMessage("connecting to server");
					savedIP = typing;
					new Thread(() -> {
						Game.client = new MinicraftClient(savedUsername, this, typing, connectTimeout.getValue()*1000); // typing = ipAddress
						if(Game.client.isConnected())
							new Save(); // write the saved ip to file
						typing = "";
					}).start();
					return;
				} else if(input.getKey("shift-escape").clicked) {
					// logout
					savedUUID = "";
					savedUsername = "";
					new Save(); // so the next time they start up the game to log in, it won't try to log in automatically.
					typing = "";
					curState = State.LOGIN;
				}
				break;
			
			case WAITING:
				/// this is just in case something gets set too early or something and the error state is overridden.
				if(errorMessage.length() > 0)
					curState = State.ERROR;
				break;
		}
		
		if(input.getKey("exit").clicked && !Game.ISHOST) {
			Game.setMenu(new TitleDisplay());
		}
	}
	
	private void login(String email, String password) {
		setWaitMessage("logging in");
		
		/// HTTP REQUEST - send username and password to server via HTTPS, expecting a UUID in return.
		Unirest.post(apiDomain+"/login")
			.field("email", email)
			.field("password", password)
			.asJsonAsync(new Callback<JsonNode>() {
				@Override
				public void completed(HttpResponse<JsonNode> response) {
					JSONObject json = response.getBody().getObject();
					if(Game.debug) System.out.println("received json from login attempt: " + json.toString());
					switch(json.getString("status")) {
						case "error":
							setError(json.getString("message"), false); // in case the user abandoned the menu, don't drag them back.
							break;
						
						case "success":
							savedUUID = json.getString("uuid");
							savedUsername = json.getString("name");
							setWaitMessage("saving credentials");
							new Save();
							typing = savedIP;
							curState = State.ENTERIP;
							break;
					}
				}
				
				@Override
				public void failed(UnirestException e) {
					e.printStackTrace();
					cancelled();
				}
				
				@Override
				public void cancelled() {
					setError("login failed.", false);
				}
			});
	}
	
	private void fetchName(String uuid) {
		/// HTTP REQUEST - ATTEMPT TO SEND UUID TO SERVER AND UPDATE USERNAME
		HttpResponse<JsonNode> response = null;
		
		try {
			response = Unirest.post(apiDomain+"/fetch-name")
				.field("uuid", savedUUID)
				.asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		
		if(response != null) {
			JSONObject json = response.getBody().getObject();
			//if(Game.debug) System.out.println("received json from username request: " + json.toString());
			switch(json.getString("status")) {
				case "error":
					setError("problem with saved login data; please exit and login again.", false);
					savedUUID = "";
					break;
				
				case "success":
					if(Game.debug) System.out.println("successfully received username from playminicraft server");
					savedUsername = json.getString("name");
					break;
			}
		} else// if(Game.debug)
			setError("Internal server error: Couldn't fetch username from uuid");
		//System.out.println("response to username fetch was null");
	}
	
	
	public void setWaitMessage(String message) {
		waitingMessage = message;
		curState = State.WAITING;
	}
	
	public void setLoadingMessage(String msg) {
		curState = State.LOADING;
		loadingMessage = msg;
	}
	
	public void setError(String msg) { setError(msg, true); }
	private void setError(String msg, boolean overrideMenu) {
		if(curState == State.ERROR) return; // keep original message
		this.curState = State.ERROR;
		errorMessage = msg;
		if(overrideMenu && Game.getMenu() != this && Game.isValidClient())
			Game.setMenu(this); // if you don't override the menu, then you'd have to already be viewing the multiplayer menu.
	}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
		
		switch(curState) {
			case ENTERIP:
				Font.drawCentered("logged in as: " + savedUsername, screen, 6, Color.get(-1, 252));
				
				if(!online)
					Font.drawCentered("offline mode: local servers only", screen, Screen.h/2 - Font.textHeight()*6, Color.get(-1, 335));
				
				Font.drawCentered("Enter ip address to connect to:", screen, Screen.h/2-Font.textHeight()*2-2, Color.get(-1, 555));
				Font.drawCentered(typing, screen, Screen.h/2-Font.textHeight(), Color.get(-1, 552));
				
				connectTimeout.render(screen, (Screen.w-connectTimeout.getWidth()) / 2, Screen.h/2+Font.textHeight()*2, true);
				
				Font.drawCentered("Press Shift-Escape to logout", screen, Screen.h-Font.textHeight()*7, Color.get(-1, 444));
				break;
			
			case LOGIN:
				String msg = "Enter email:";
				if(!typingEmail)
					msg = "Enter password:";
				Font.drawCentered(msg, screen, Screen.h/2-6, Color.WHITE);
				
				msg = typing;
				if(!typingEmail)
					//noinspection ReplaceAllDot
					msg = msg.replaceAll(".", ".");
				Font.drawCentered(msg, screen, Screen.h/2+6, (inputIsValid?Color.get(-1, 444):Color.RED));
				if(!inputIsValid) {
					Font.drawCentered("field is blank", screen, Screen.h/2+20, Color.RED);
				}
				
				Font.drawCentered("get an account at:", screen, Font.textHeight()/2-1, Color.get(-1, 345));
				Font.drawCentered(domain.substring(domain.indexOf("://")+3)+"/register", screen, Font.textHeight()*3/2, Color.get(-1, 345));
				
				break;
			
			case WAITING:
				Font.drawCentered(waitingMessage+ ellipsis.updateAndGet(), screen, Screen.h/2, Color.WHITE);
				break;
			
			case LOADING:
				Font.drawCentered("Loading "+loadingMessage+" from server"+ ellipsis.updateAndGet(), screen, Screen.h/2, Color.WHITE);
				//Font.drawCentered(transferPercent+"%", screen, Screen.h/2+6, Color.WHITE);
				break;
			
			case ERROR:
				//if(Updater.tickCount % 10 == 0) System.out.println("error message: " + errorMessage);
				Font.drawCentered("Could not connect to server:", screen, Screen.h/2-6, Color.RED);
				FontStyle style = new FontStyle(Color.get(-1, 511)).setYPos(Screen.h/2+6);
				Font.drawParagraph(errorMessage, screen, style, 1);
				//Font.drawCentered(errorMessage, screen, Screen.h/2+6, Color.get(-1, 511));
				break;
		}
		
		if(curState == State.ENTERIP || curState == State.ERROR) {
			Font.drawCentered("Press "+Game.input.getMapping("exit")+" to return", screen, Screen.h-Font.textHeight()*2, Color.GRAY);
		}
	}
}
