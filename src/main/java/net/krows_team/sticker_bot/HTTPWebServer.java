package net.krows_team.sticker_bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

public class HTTPWebServer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HTTPWebServer.class);
	
	private static final String SERVER_PORT = "0.0.0.0";
	private static final String SERVER_URL = "https://knad-sticker-bot.herokuapp.com";
	private static final String WEB_SERVER_THREAD_NAME = "Server update";
	
	private static final long UPDATE_TICK = 1000L * 60 * 25;
	
	private HttpServer server;
	
	public HTTPWebServer() {
		try {
			server = HttpServer.create(new InetSocketAddress(SERVER_PORT, Integer.valueOf(System.getenv("PORT"))), 0);
		} catch (NumberFormatException | IOException e) {
			LOGGER.error("Error occured while creating web server: ", e);
		}
	}
	
	public void start() {
		server.start();
		Timer timer = new Timer(WEB_SERVER_THREAD_NAME);
		TimerTask updateTask = new TimerTask() {
			@Override
			public void run() {
				try {
//					TODO Make it in correct way
					URL url = new URL(SERVER_URL);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					in.close();
			        con.disconnect();
				} catch (IOException e) {
					LOGGER.error("Error occured while handling connection to web server: ", e);
				}
			}
		};
		timer.scheduleAtFixedRate(updateTask, 1000L, UPDATE_TICK);
	}
	
	public void stop() {
		server.stop(0);
	}
}
