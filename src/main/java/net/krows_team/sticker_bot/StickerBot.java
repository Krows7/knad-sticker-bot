package net.krows_team.sticker_bot;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AddStickerToSet;
import com.pengrad.telegrambot.request.CreateNewStickerSet;
import com.pengrad.telegrambot.request.DeleteStickerFromSet;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetStickerSetResponse;

public class StickerBot {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(StickerBot.class);
	
	private TelegramBot bot;
	
	private Properties properties;
	
	public static final long KROWS_ID = 455071255;
	public static final long BOT_ID = 5499360401L;
	public static final long CHAT_ID = -1001594184195L;
	public static final String BOT_NICKNAME = "knad_sticker_bot";
	public static final String STICKER_SET_NAME = "knad_sticker_pack_by_" + BOT_NICKNAME;
	public static final String STICKER_SET_TITLE = "КНАД Стартер Пак";
	public static final String BOT_COMMAND = "sticker";
	public static final String BOT_PREFIX = "/";
	public static final String PROPERTIES_PATH = "src/main/resources/bot.properties";
	
	public static void main(String[] args) {
		StickerBot bot = new StickerBot();
		bot.start();
	}
	
	public StickerBot() {
		initProperties();
	}
	
	public void initProperties() {
		try {
			File propFile = new File(PROPERTIES_PATH);
			propFile.createNewFile();
			properties = new Properties();
			properties.load(new FileInputStream(propFile));
			checkParameters();
		} catch (Exception e) {
			LOGGER.error("An error occured while reading properties", e);
		}
	}
	
	public void checkParameters() throws Exception {
		List<String> missingParams = new ArrayList<>();
		if(!properties.contains("token")) missingParams.add("token");
		if(!missingParams.isEmpty()) throw new Exception("Missing the following parameters in .properties file: " + missingParams.toString());
	}
	
	public void start() {
		bot = new TelegramBot(properties.getProperty("token"));
		bot.setUpdatesListener(upd -> {
			for (Update u : upd) {
				if (u.message() == null || u.message().text() == null) continue;
				Message msg = u.message();
				String[] msgArr = msg.text().split(" ");
				if(checkForCommand(msgArr[0], BOT_COMMAND)) handleSticker(msg, msgArr);
				else if(checkForCommand(msgArr[0], "help")) handleHelp(msg);
				else if(contains(msgArr, "да")) {
					bot.execute(new SendPhoto(msg.chat().id(), read("src/main/resources/kirkorov.jpg")).replyToMessageId(msg.messageId()));
				}
			}
			return UpdatesListener.CONFIRMED_UPDATES_ALL;
		});
	}
	
	public boolean checkForCommand(String arg, String command) {
		return arg.equals(BOT_PREFIX + command);
	}
	
	public void handleSticker(Message msg, String[] args) {
		if(args.length == 1) sendError(msg.chat().id(), "Too few arguments");
		else if(args.length > 3) sendError(msg.chat().id(), "Too much arguments");
		else if(args[1].equals("add")) {
			if (msg.replyToMessage() == null) sendError(msg.from().id(), "There's no reply message.");
			else {
				try {
					byte[] out = renderSticker(msg);
					GetStickerSetResponse stickerSet = bot.execute(new GetStickerSet(STICKER_SET_NAME));
					String emoji = args.length > 2 ? args[2] : "🤔";
					if(stickerSet.stickerSet() == null) bot.execute(CreateNewStickerSet.pngSticker(KROWS_ID, STICKER_SET_NAME, STICKER_SET_TITLE, emoji, out));
					else bot.execute(AddStickerToSet.pngSticker(KROWS_ID, STICKER_SET_NAME, emoji, out));
					stickerSet = bot.execute(new GetStickerSet(STICKER_SET_NAME));
					Sticker[] stickerArr = stickerSet.stickerSet().stickers();
					bot.execute(new SendSticker(msg.chat().id(), stickerArr[stickerArr.length - 1].fileId()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if(args[1].equals("preview")) {
			if (msg.replyToMessage() == null) sendError(msg.from().id(), "There's no reply message.");
			else {
				try {
					bot.execute(new SendSticker(msg.chat().id(), renderSticker(msg)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (args[1].equals("delete")) {
			GetStickerSetResponse stickerSet = bot.execute(new GetStickerSet(STICKER_SET_NAME));
			if (args[2].equals("all")) {
				for (Sticker sticker : stickerSet.stickerSet().stickers()) bot.execute(new DeleteStickerFromSet(sticker.fileId()));
			} else {
				int index = Integer.valueOf(args[2]);
				BaseResponse response = bot.execute(new DeleteStickerFromSet(stickerSet.stickerSet().stickers()[index].fileId()));
				if(response.isOk()) sendMessage(msg.chat().id(), "Sticker was deleted");
			}
		} else sendError(msg.chat().id(), "Unknown Command: " + args[1]);
	}
	
	private byte[] read(String path) {
		try {
			return Files.readAllBytes(new File(path).toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void handleHelp(Message msg) {
		try {
			Path path = new File("src/main/resources/help.md").toPath();
			String s = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining(System.lineSeparator()));
			SendMessage send = new SendMessage(msg.chat().id(), s);
			send.parseMode(ParseMode.Markdown);
			sendMessage(send);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean contains(String[] a, String match) {
		for (String s : a) if(s.toLowerCase().equals(match)) return true;
		return false;
	}
	
	private byte[] renderSticker(Message msg) throws MalformedURLException, IOException {
		BufferedImage img = new StickerRenderer(bot).renderMessage(msg.replyToMessage());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}
	
	public void sendError(long id, String msg) {
		sendMessage(id, "Error: " + msg);
	}
	
	public void sendMessage(long id, String msg) {
		sendMessage(new SendMessage(id, msg));
	}
	
	public void sendMessage(SendMessage msg) {
		bot.execute(msg);
	}
}

// TODO Work with chats alongside (make debug sticker set)
// TODO Make Admin commands (& Admin Check (me))
// TODO Make console (?)
// TODO Error Handling