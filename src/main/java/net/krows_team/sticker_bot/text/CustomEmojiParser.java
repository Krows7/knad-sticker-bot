package net.krows_team.sticker_bot.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiLoader;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import com.vdurmont.emoji.EmojiTrie;

import net.krows_team.sticker_bot.util.Utils;

public class CustomEmojiParser extends EmojiParser {
	
	public static final String PATH = Utils.getResourcePath("emoji/emojis.json");

	static {
		init();
	}
	
	public static List<Node> splitIntoNodes(String input) {
		List<Node> nodes = new ArrayList<>();
		List<UnicodeCandidate> c = getUnicodeCandidates(input);
		int prev = 0;
		for(UnicodeCandidate can : c) {
			String s = input.substring(prev, can.getEmojiStartIndex());
			if(!s.isEmpty()) nodes.add(new TextNode(s));
			nodes.add(new EmojiNode(can.getEmoji()));
			prev = can.getFitzpatrickEndIndex();
		}
		String s = input.substring(prev, input.length());
		if(!s.isEmpty()) nodes.add(new TextNode(s));
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public static void init() {
		try {
		  var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
		  VarHandle modifiersField = lookup.findVarHandle(Field.class, "modifiers", int.class);
		  Field ALL_EMOJIS = EmojiManager.class.getDeclaredField("ALL_EMOJIS");
		  ALL_EMOJIS.setAccessible(true);
		  modifiersField.set(ALL_EMOJIS, ALL_EMOJIS.getModifiers() & ~Modifier.FINAL);
		  Field EMOJIS_BY_TAG = EmojiManager.class.getDeclaredField("EMOJIS_BY_TAG");
		  EMOJIS_BY_TAG.setAccessible(true);
		  modifiersField.set(EMOJIS_BY_TAG, EMOJIS_BY_TAG.getModifiers() & ~Modifier.FINAL);
		  Field EMOJIS_BY_ALIAS = EmojiManager.class.getDeclaredField("EMOJIS_BY_ALIAS");
		  EMOJIS_BY_ALIAS.setAccessible(true);
		  modifiersField.set(EMOJIS_BY_ALIAS, EMOJIS_BY_ALIAS.getModifiers() & ~Modifier.FINAL);
		  Field EMOJI_TRIE = EmojiManager.class.getDeclaredField("EMOJI_TRIE");
		  EMOJI_TRIE.setAccessible(true);
		  modifiersField.set(EMOJI_TRIE, EMOJI_TRIE.getModifiers() & ~Modifier.FINAL);
		  InputStream stream = new FileInputStream(new File(PATH));
		  List<Emoji> emojis = EmojiLoader.loadEmojis(stream);
		  ALL_EMOJIS.set(null, emojis);
		  for (Emoji emoji : emojis) {
		    for (String tag : emoji.getTags()) {
		      if (((Map<String, Set<Emoji>>) EMOJIS_BY_TAG.get(null)).get(tag) == null) {
		        ((Map<String, Set<Emoji>>) EMOJIS_BY_TAG.get(null)).put(tag, new HashSet<Emoji>());
		      }
		      ((Map<String, Set<Emoji>>) EMOJIS_BY_TAG.get(null)).get(tag).add(emoji);
		    }
		    for (String alias : emoji.getAliases()) {
		      ((Map<String, Emoji>) EMOJIS_BY_ALIAS.get(null)).put(alias, emoji);
		    }
		  }
		
		  EMOJI_TRIE.set(null, new EmojiTrie(emojis));
		  Collections.sort((List<Emoji>) ALL_EMOJIS.get(null), new Comparator<Emoji>() {
		    public int compare(Emoji e1, Emoji e2) {
		      return e2.getUnicode().length() - e1.getUnicode().length();
		    }
		  });
		  stream.close();
		} catch (IOException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			  throw new RuntimeException(e);
	}
	}
	
	public static List<Emoji> extract(String input) {
		return getUnicodeCandidates(input).stream().map(u -> u.getEmoji()).collect(Collectors.toList());
	}
}
