package net.krows_team.sticker_bot.text;

import java.awt.Graphics2D;

public class TextNode extends Node {

	private String text;
	
	public TextNode(String text) {
		this.text = text;
	}
	
	@Override
	public void render(Graphics2D g, int x, int y) {
		g.drawString(text, x, y);
	}
	
	public int getWidth(Graphics2D g) {
		return g.getFontMetrics().stringWidth(text);
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		return String.format("TextNode[%s]", text);
	}
}
