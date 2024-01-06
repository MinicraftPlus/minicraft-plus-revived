package minicraft.network;

import kong.unirest.Callback;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.concurrent.Future;

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

	@Nullable
	public Future<HttpResponse<Empty>> ping() {
		return ping(1);
	}

	@Nullable
	public Future<HttpResponse<Empty>> ping(int value) {
		final String url = "https://pingdat.io?t=" + token + "&v=" + value;

		return Unirest.get(url).asEmptyAsync(new Callback<Empty>() {
			@Override
			public void completed(HttpResponse<Empty> response) {
				Logger.tag("Analytics").trace("Ping success for {}, with value {}.", name(), value);
			}

			@Override
			public void failed(UnirestException e) {
				Logger.tag("Analytics").warn("Ping failed for {}, with value {}.", name(), value);
			}

			@Override
			public void cancelled() {
				Logger.tag("Analytics").warn("Ping cancelled for {}, with value {}.", name(), value);
			}
		});
	}
}
