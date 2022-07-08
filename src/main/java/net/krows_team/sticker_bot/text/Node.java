package net.krows_team.sticker_bot.text;

import java.awt.Graphics2D;

public abstract class Node {
	
	public abstract void render(Graphics2D g, int x, int y);
	
	public abstract int getWidth(Graphics2D g);
}
