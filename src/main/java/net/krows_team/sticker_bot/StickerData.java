package net.krows_team.sticker_bot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.UserProfilePhotos;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;

import net.krows_team.sticker_bot.util.OptionalPair;
import net.krows_team.sticker_bot.util.Utils;

public class StickerData {
	
	long timestamp;
	
	String renderName;
	String text;
	
	Color color;
	
	OptionalPair<BufferedImage, String> avatar;
	
	public StickerData(Message msg, TelegramBot api) {
		this(loadTimestamp(msg), loadRenderName(msg), msg.text(), Colors.getFixed(loadNickname(msg)).createColor(), loadAvatar(msg, api));
	}
	
	StickerData(long timestamp, String renderName, String text, Color color, OptionalPair<BufferedImage, String> avatar) {
		this.timestamp = timestamp;
		this.renderName = renderName;
		this.text = text;
		this.color = color;
		this.avatar = avatar;
	}
	
	private static OptionalPair<BufferedImage, String> loadAvatar(Message msg, TelegramBot api) {
		UserProfilePhotos photo = getProfilePhoto(api, msg.from().id());
		if(msg.forwardSenderName() != null || msg.forwardFrom() != null) {
			if(msg.forwardFrom() == null) return OptionalPair.of(getCapitals(msg.forwardSenderName(), null));
			else {
				photo = getProfilePhoto(api, msg.forwardFrom().id());
				if(photo.totalCount() == 0) return OptionalPair.of(getCapitals(msg.forwardFrom().firstName(), msg.forwardFrom().lastName()));
				else return OptionalPair.nonNull(loadFromURL(api, photo));
			}
		} else if(photo.totalCount() == 0) return OptionalPair.of(getCapitals(msg.from().firstName(), msg.from().lastName()));
		else return OptionalPair.nonNull(loadFromURL(api, photo));
	}
	
	private static BufferedImage loadFromURL(TelegramBot api, UserProfilePhotos photo) {
		try {
			return ImageIO.read(new URL(Utils.getURLById(api, photo.photos()[0][0].fileId())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static UserProfilePhotos getProfilePhoto(TelegramBot api, long id) {
		return api.execute(new GetUserProfilePhotos(id)).photos();
	}
	
	private static long loadTimestamp(Message msg) {
		return (msg.forwardDate() == null ? msg.date() : msg.forwardDate()) * 1000L;
	}
	
	private static String loadRenderName(Message msg) {
		if(msg.forwardFrom() != null) {
			return msg.forwardFrom().firstName() + (msg.forwardFrom().lastName() == null ? "" : " " + msg.forwardFrom().lastName());
		} else if(msg.forwardSenderName() != null) return msg.forwardSenderName();
		else return msg.from().firstName() + (msg.from().lastName() == null ? "" : " " + msg.from().lastName());
	}
	
	private static String loadNickname(Message msg) {
		return msg.forwardFrom() != null ? msg.forwardFrom().username() : msg.forwardSenderName() != null ? msg.forwardSenderName() : msg.from().username();
	}
	
	private static String getCapitals(String firstName, String lastName) {
		return "" + Character.toUpperCase(firstName.charAt(0)) + (lastName == null ? "" : Character.toUpperCase(lastName.charAt(0)));
	}
}
