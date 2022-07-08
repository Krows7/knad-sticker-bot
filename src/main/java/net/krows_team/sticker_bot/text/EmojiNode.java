package net.krows_team.sticker_bot.text;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.vdurmont.emoji.Emoji;

import net.krows_team.sticker_bot.StickerRenderer;

public class EmojiNode extends Node {

	private Emoji emoji;
	
	public EmojiNode(Emoji emoji) {
		this.emoji = emoji;
	}
	
	@Override
	public void render(Graphics2D g, int x, int y) {
		BufferedImage img = getEmoji(g);
		int height = g.getFontMetrics().getDescent();
		g.drawImage(img, x, y - img.getHeight() + (img.getHeight() - height) / 4, null);
	}
	
	@Override
	public int getWidth(Graphics2D g) {
		return StickerRenderer.getXHeight(g) * 2;
	}
	
	private BufferedImage getEmoji(Graphics2D g) {
		BufferedImage img = EmojiCache.getEmojiImage(emoji.getUnicode());
		int height = StickerRenderer.getXHeight(g) * 2;
		return StickerRenderer.resizeImage(img, height, height);
	}
}
