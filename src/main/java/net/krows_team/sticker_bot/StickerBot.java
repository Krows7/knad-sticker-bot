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
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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

import net.krows_team.sticker_bot.util.Utils;

public class StickerBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(StickerBot.class);

	private static final Predicate<String> WORD_PATTERN = Pattern.compile("\\b(\\w*(Д|д)(А|а|a)+\\w*)\\b")
			.asPredicate();

	private static final long STICKER_USER_ID = getStickerUserId();
	private static final long TEST_CHAT_ID = getTestChatId();

	private static final String KIRKOROV_FILE_NAME = "kirkorov.jpg";
	private static final String BOT_PREFIX = "knad_sticker";
	private static final String BOT_NICKNAME = BOT_PREFIX + "_bot";
	private static final String STICKER_SET_NAME = BOT_PREFIX + "_pack_by_" + BOT_NICKNAME;
	private static final String STICKER_SET_TITLE = "КНАД Стартер Пак";
	private static final String STICKER_COMMAND = "sticker";
	private static final String HELP_COMMAND = "help";
	private static final String HALT_COMMAND = "halt";
	private static final String RESUME_COMMAND = "resume";
	private static final String STOP_COMMAND = "stop";
	private static final String BOT_COMMAND_PREFIX = "/";
	private static final String PROPERTIES_PATH = Utils.getResourcePath("bot.properties");

	private HTTPWebServer webServer;

	private TelegramBot bot;

	private Properties properties;

	private String token;

	private boolean started = false;
	private boolean isLocal = false;
	private boolean isTesting = false;

	public static void main(String[] args) {
		StickerBot bot = new StickerBot(args);
		bot.start(args);
	}

	public StickerBot(String[] args) {
		isLocal = Utils.contains(args, "local");
		isTesting = Utils.contains(args, "test");
		if (!isLocal)
			applyHerokuHacks();
		initProperties();
		loadToken();
	}

	private void loadToken() {
		token = Optional.ofNullable(System.getenv("bot_token"))
				.orElseThrow(() -> new NullPointerException("Telegram bot token is null"));
	}

	private void applyHerokuHacks() {
		webServer = new HTTPWebServer();
		webServer.start();
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
		if (!missingParams.isEmpty())
			throw new Exception("Missing the following parameters in .properties file: " + missingParams.toString());
	}

	public void start(String[] args) {
		bot = new TelegramBot(token);
		bot.setUpdatesListener(upd -> {
			try {
				for (Update u : upd) {
//					TODO Make it better
					if (u.message() == null || u.message().text() == null)
						continue;
					Message msg = u.message();
					if (isTesting && msg.chat().id() != TEST_CHAT_ID)
						continue;
					String[] msgArr = msg.text().split(" ");
					if (isCommandAvailable(msgArr[0], STICKER_COMMAND))
						handleSticker(msg, msgArr);
					else if (isCommandAvailable(msgArr[0], HELP_COMMAND))
						handleHelp(msg);
					else if (isCommandAvailable(msgArr[0], HALT_COMMAND))
						handleHalt();
					else if (checkForCommand(msgArr[0], RESUME_COMMAND))
						started = true;
					else if (checkForCommand(msgArr[0], STOP_COMMAND))
						started = false;
					else if (started && WORD_PATTERN.test(msg.text())) {
						bot.execute(new SendPhoto(msg.chat().id(),
								Utils.readFile(Utils.getResourcePath(KIRKOROV_FILE_NAME)))
										.replyToMessageId(msg.messageId()));
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error occured to bot shutdown: ", e);
				if (Utils.contains(args, "error-halt"))
					handleHalt();
				else
					start(args);
			}
			return UpdatesListener.CONFIRMED_UPDATES_ALL;
		});
	}

	public boolean isCommandAvailable(String arg, String command) {
		return started && checkForCommand(arg, command);
	}

	public boolean checkForCommand(String arg, String command) {
		return arg.equals(BOT_COMMAND_PREFIX + command);
	}

	public void handleHalt() {
		if (!isLocal)
			webServer.stop();
		bot.shutdown();
		System.exit(666);
	}

	public void handleSticker(Message msg, String[] args) {
//		TODO Make it better
		if (args.length == 1)
			sendError(msg.chat().id(), "Too few arguments");
		else if (args.length > 3)
			sendError(msg.chat().id(), "Too much arguments");
		else if (args[1].equals("add")) {
			if (msg.replyToMessage() == null)
				sendError(msg.from().id(), "There's no reply message");
			else {
				try {
					byte[] out = renderSticker(msg);
					GetStickerSetResponse stickerSet = bot.execute(new GetStickerSet(STICKER_SET_NAME));
					String emoji = args.length > 2 ? args[2] : "🤔";
					if (stickerSet.stickerSet() == null)
						bot.execute(CreateNewStickerSet.pngSticker(STICKER_USER_ID, STICKER_SET_NAME, STICKER_SET_TITLE,
								emoji, out));
					else
						bot.execute(AddStickerToSet.pngSticker(STICKER_USER_ID, STICKER_SET_NAME, emoji, out));
					stickerSet = bot.execute(new GetStickerSet(STICKER_SET_NAME));
					Sticker[] stickerArr = stickerSet.stickerSet().stickers();
					bot.execute(new SendSticker(msg.chat().id(), stickerArr[stickerArr.length - 1].fileId()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (args[1].equals("preview")) {
			if (msg.replyToMessage() == null)
				sendError(msg.from().id(), "There's no reply message");
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
				for (Sticker sticker : stickerSet.stickerSet().stickers())
					bot.execute(new DeleteStickerFromSet(sticker.fileId()));
			} else {
				int index = Integer.valueOf(args[2]);
				BaseResponse response = bot
						.execute(new DeleteStickerFromSet(stickerSet.stickerSet().stickers()[index].fileId()));
				if (response.isOk())
					sendMessage(msg.chat().id(), "Sticker was deleted");
			}
		} else
			sendError(msg.chat().id(), "Unknown Command: " + args[1]);
	}

	private static long getStickerUserId() {
		return Long.valueOf(System.getenv("sticker_user_id"));
	}

	private static long getTestChatId() {
		return Long.valueOf(System.getenv("test_chat_id"));
	}

	public void handleHelp(Message msg) {
		try {
			Path path = new File(Utils.getResourcePath("help.md")).toPath();
			String s = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining(System.lineSeparator()));
			SendMessage send = new SendMessage(msg.chat().id(), s);
			send.parseMode(ParseMode.Markdown);
			sendMessage(send);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] renderSticker(Message msg) throws MalformedURLException, IOException {
		BufferedImage img = new StickerRenderer(new StickerData(msg.replyToMessage(), bot)).renderMessage();
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