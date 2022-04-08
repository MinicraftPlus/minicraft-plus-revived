package minicraft.network;

import java.io.InputStream;
import java.util.concurrent.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import minicraft.core.Game;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public enum Analytics {
	
	GameStartup("4ZBgx4TBdNvBBVBB"),
	
	SaveFileUpdate("NCFgx4TBXrv46fpZ"),
	
	SinglePlayerGame("kSPgx4TBXGn6Kvw5"),
	
	MultiplayerGame("HwLgx4TBV1pvmsdV"),
	
	LocalSession("sLQgx4TBrWSP1Q7m"),
	
	WorldCreation("mQbgx4TB7DC5XqMP"),
	
	LoginAttempt("5fWgx4TBXwbq7vKc"),
	
	LoginSuccess("Czggx4TB27KrgQxN"),
	
	SessionAttempt("P5ggx4TBbrq3NTvt"),
	
	SessionJoin("Kvjgx4TBH2TZ33Sf"),
	
	FirstAirWizardDeath("k50gx4TBjnB7bzp3"),
	FirstAirWizardIIDeath("XFngx4TB1tFF0jZ5"),
	AirWizardDeath("WRGgx4TB5NkmZt5m"),
	AirWizardIIDeath("3v3gx4TBMf730gZl"),
	
	Crashes("Fr1gx4TB2tZVBkcK"),
	
	SinglePlayerDeath("n8ggx4TBqnWw2hfT"),
	
	MultiplayerDeath("T7Cgx4TB6n2rPzBB");
	
	
	private final String token;
	
	Analytics(String token) {
		this.token = token;
	}
	
	@Nullable public Future<HttpResponse<InputStream>> ping() { return ping(1); }
	@Nullable public Future<HttpResponse<InputStream>> ping(int value) {
		if (Game.debug) return null;
		final String url = "https://pingdat.io?t="+token+"&v="+value;
		
		return Unirest.get(url).asBinaryAsync(new Callback<InputStream>() {
			@Override
			public void completed(HttpResponse<InputStream> response) {
				Logger.trace("Ping success for {}, with value {}.", name(), value);
			}
			
			@Override
			public void failed(UnirestException e) {
				Logger.warn("Ping failed for {}, with value {}.", name(), value);
			}
			
			@Override
			public void cancelled() {
				Logger.warn("Ping cancelled for {}, with value {}.", name(), value);
			}
		});
	}
}
