package net.krows_team.sticker_bot.text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.krows_team.sticker_bot.util.Utils;

public class EmojiCache {
	
	private static final int EMOJI_PAGE_COUNT = 7;
	private static final int EMOJI_IMAGE_SIZE = 72;
	private static final int EMOJI_PAGE_COLUMNS = 32;
	private static final int EMOJI_PAGE_ROWS = 16;
	private static final int LAST_EMOJI_PAGE_ROWS = 13;
	private static final int EMOJIS_IN_PAGE = EMOJI_PAGE_COLUMNS * EMOJI_PAGE_ROWS;
	
	private static final BufferedImage[] EMOJIS = new BufferedImage[EMOJI_PAGE_COUNT * EMOJIS_IN_PAGE];
	
	private static boolean cached = false;
	
	private static void loadCache() {
		for(int i = 0; i < EMOJI_PAGE_COUNT; i++) {
			BufferedImage page = loadPage(i + 1);
			int height = i == EMOJI_PAGE_COUNT - 1 ? LAST_EMOJI_PAGE_ROWS : EMOJI_PAGE_ROWS;
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < EMOJI_PAGE_COLUMNS; x++) {
					EMOJIS[EMOJIS_IN_PAGE * i + y * EMOJI_PAGE_COLUMNS + x] = page.getSubimage(x * EMOJI_IMAGE_SIZE, y * EMOJI_IMAGE_SIZE, EMOJI_IMAGE_SIZE, EMOJI_IMAGE_SIZE);
				}
			}
		}
		cached = true;
	}
	
	public static BufferedImage getEmojiImage(String emoji) {
		if(!cached) loadCache();
		int index = EmojiFileParser.INSTANCE.emojiMap.get(emoji);
		return EMOJIS[index];
	}
	
	private static BufferedImage loadPage(int index) {
		try {
			return ImageIO.read(new File(Utils.getResourcePath(String.format("emoji/emoji_%s.png", index))));
		} catch (IOException e) {
			// TODO Error Handling
			e.printStackTrace();
		}
		return null;
	}
}
