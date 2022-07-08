package net.krows_team.sticker_bot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;

public class Utils {
	
	public static String getURLById(TelegramBot api, String id) {
		GetFile request = new GetFile(id);
		GetFileResponse response = api.execute(request);
		return api.getFullFilePath(response.file());
	}
	
	public static String getResourcePath(String file) {
		return "src/main/resources/" + file;
	}
	
	public static String getTestResourcePath(String file) {
		return "src/test/resources/" + file;
	}
	
	public static boolean contains(String[] a, String v) {
		for(String s : a) if(s.equals(v)) return true;
		return false;
	}
	
	public static byte[] readFile(String path) throws IOException {
		return Files.readAllBytes(new File(path).toPath());
	}
	
	public static <T> T before(T t, Runnable run) {
		run.run();
		return t;
	}
}
