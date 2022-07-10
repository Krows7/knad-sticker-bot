package net.krows_team.sticker_bot;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

import net.krows_team.sticker_bot.util.ExceptionHandler;

public class HTTPWebServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPWebServer.class);

	private static final String SERVER_IP = "0.0.0.0";
	private static final String SERVER_URL = "https://knad-sticker-bot.herokuapp.com";
	private static final String WEB_SERVER_THREAD_NAME = "Server update";

	private static final long UPDATE_TICK = 1000L * 60 * 25;

	private HttpServer server;

	public HTTPWebServer() {
		ExceptionHandler.create(
				() -> server = HttpServer
						.create(new InetSocketAddress(SERVER_IP, Integer.valueOf(System.getenv("PORT"))), 0),
				e -> LOGGER.error("Error occured while creating web server: ", e)).run();
	}

	public void start() {
		server.start();
		Timer timer = new Timer(WEB_SERVER_THREAD_NAME);
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				ExceptionHandler
						.create(() -> ((HttpURLConnection) new URL(SERVER_URL).openConnection()).getResponseCode(),
								e -> LOGGER.error("Error occured while handling connection to web server: ", e))
						.run();
			}
		};
		timer.scheduleAtFixedRate(updateTask, 0, UPDATE_TICK);
	}

	public void stop() {
		server.stop(0);
	}
}
