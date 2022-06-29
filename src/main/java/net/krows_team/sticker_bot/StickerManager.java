package net.krows_team.sticker_bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class StickerManager {
	
	public static final String STICKERS_NAME = "Stickers";
	public static final int STICKERS_ID = 429000;
	
	private TelegramBot bot;
	
	public StickerManager(TelegramBot bot) {
		this.bot = bot;
	}
	
//	public void sendCommand(String cmd) {
//		bot.
//		SendMessage request = new SendMessage(cmd, cmd)
//		bot.execute(new )
//	}
//	
//	public void start() {
//		
//	}
//	
//	public void createPack()
}
