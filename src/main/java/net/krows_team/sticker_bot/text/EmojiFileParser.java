package net.krows_team.sticker_bot.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.krows_team.sticker_bot.util.Utils;

public class EmojiFileParser {
	
	public static final EmojiFileParser INSTANCE = load();
	
	Map<String, Integer> emojiMap;
	
	private static EmojiFileParser load() {
		return new EmojiFileParser();
	}
	
	public EmojiFileParser() {
		List<String> lines = null;
		emojiMap = new HashMap<>();
		try {
			lines = Files.readAllLines(Paths.get(Utils.getResourcePath("emoji/emoji.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int index = 0;
		for(int i = 0; i < 324; i++) {
			String[] emojis = lines.get(i).split(",");
			for(String em : emojis) {
				if(em.isEmpty()) continue;
				String emoji = em.substring(1, em.length() - 1);
				emojiMap.put(emoji, index++);
			}
		}
		String[] last = lines.get(653).split(",");
		emojiMap.put(last[0].substring(1, last[0].length() - 1), index++);
		emojiMap.put(last[1].substring(1, last[1].length() - 1), index++);
	}
}
