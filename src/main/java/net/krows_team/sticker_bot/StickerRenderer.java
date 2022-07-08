package net.krows_team.sticker_bot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.krows_team.sticker_bot.text.TextRenderer;

public class StickerRenderer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StickerRenderer.class);
	
	private static final int MAX_WIDTH = 512;
	private static final int BACKGROUND_COLOR = 0x1c2028;
	private static final int TIME_COLOR = 0x787D87;
	private static final int AVATAR_DIAMETER = 53;
	private static final int TIME_HEIGHT = 15;
	private static final int TIME_BASELINE = 12;
	private static final int TIME_X = 59;
	private static final int MAX_TEXT_TIME_GAP = 13;
	private static final int MAX_TEXT_GAP = 20;
	private static final int PROFILE_LETTERS_BASELINE = 16;
	private static final int TIME_10_OFFSET = 14;
	private static final int DEFAULT_SCREEN_RESOLUTION = 120;
	private static final int SCREEN_RESOLUTION = getScreenResolution();
	private static final float FONT_SIZE = 12.25F;
	private static final float FONT_NAME_SIZE = 12F;
	private static final float FONT_PROFILE_SIZE = 17F;
	private static final float MESSAGE_BLOCK_X = AVATAR_DIAMETER + 15;
	private static final float MESSAGE_BLOCK_ROUND_RADIUS = 17;
	private static final float MESSAGE_BLOCK_BEGIN_RADIUS = 12;
	private static final float MESSAGE_TEXT_X = MESSAGE_BLOCK_X + 21;
	private static final float MESSAGE_NAME_BASELINE = 35;
	private static final float MESSAGE_TEXT_BASELINE = 63;
	
	private static final String FONT_NAME = "Open Sans Medium";
	
	private static Font NAME_FONT = new Font(FONT_NAME, Font.BOLD, 0).deriveFont(fitSize(FONT_NAME_SIZE));
	public static Font TEXT_FONT = new Font(FONT_NAME, Font.PLAIN, 0).deriveFont(fitSize(FONT_SIZE));

	private StickerData data;
	
	private BufferedImage img;
	
	private Graphics2D g;
	
	private boolean timeAdjust = true;
	private boolean timeMore10 = false;
	
	private int height = 1;
	private int width = MAX_WIDTH;
	
	public StickerRenderer(StickerData data) {
		this.data = data;
	}
	
	private void init() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(data.timestamp));
		
		timeMore10 = calendar.get(Calendar.HOUR_OF_DAY) > 9;
	}
	
	public BufferedImage fitImage() {
		if (img.getWidth() != 512) return resizeImage(img, 512, (int) (img.getHeight() * (512.0F / img.getWidth())));
		return img;
	}
	
	private void fixSize(String text) {
		LOGGER.debug("fixSize1()");
		float[] trueSize = TextRenderer.getBounds(g, text, (int) (MAX_WIDTH - MESSAGE_TEXT_X - MAX_TEXT_GAP));
		LOGGER.debug(String.format("Message Height: %s", trueSize[1]));
		trueSize[1] += MESSAGE_NAME_BASELINE + 14;
		if(TIME_X + MAX_TEXT_TIME_GAP + trueSize[0] > MAX_WIDTH - MESSAGE_TEXT_X - (timeMore10 ? TIME_10_OFFSET : 0)) timeAdjust = false;
		LOGGER.debug(String.format("Time Adjust: %s", timeAdjust));
		LOGGER.debug(String.format("Last line width: %s", trueSize[0]));
		if(timeAdjust) trueSize[1] += TIME_BASELINE / 2;
		else trueSize[1] += TIME_HEIGHT + TIME_BASELINE;
		height = (int) trueSize[1];
		
		int nameWidth = getNameWidth(img, g, data.renderName);
		float mx = Math.max(MESSAGE_TEXT_X + nameWidth + TIME_X + (timeMore10 ? TIME_10_OFFSET : 0) + MESSAGE_TEXT_X - MESSAGE_BLOCK_X, (MESSAGE_TEXT_X + trueSize[0] + MAX_TEXT_TIME_GAP + TIME_X + (timeMore10 ? TIME_10_OFFSET : 0)));
		if(timeAdjust && (int) trueSize[2] == 1) width = (int) Math.min(MAX_WIDTH, mx);
	}
	
	private BufferedImage createImage() {
		return (img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
	}
	
	private void createRender() {
		createImage();
		initGraphics(img);
	}
	
	public BufferedImage renderMessage() {
		init();
		createRender();
		fixSize(data.text);
		createRender();
		fillTransparent(g, width, height);
		drawAvatar(data.avatar.orElse(this::renderDefaultProfilePicture), g, height);
		drawMessageBlock(g, width, height);
		renderName(g, data.renderName);
		renderText(g, data.text);
		renderTime(g, data.timestamp, width, height);
		// TODO Fix if Height > 512
		return fitImage();
	}
	
	public int getNameWidth(BufferedImage img, Graphics2D g, String name) {
		g.setFont(NAME_FONT);
		return g.getFontMetrics().stringWidth(name);
	}
	
	public void renderText(Graphics2D g, String text) {
		g.setColor(new Color(0xEBEBEB));
		g.setFont(TEXT_FONT);
		TextRenderer.renderText(g, text, (int) MESSAGE_TEXT_X, (int) MESSAGE_TEXT_BASELINE, (int) (width - MESSAGE_TEXT_X - MAX_TEXT_GAP));
	}
	
	private void initGraphics(BufferedImage img) {
		g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setFont(TEXT_FONT);
	}
	
	private void fillTransparent(Graphics2D g, int width, int height) {
		g.setColor(new Color(0, true));
		g.fillRect(0, 0, width, height);
	}
	
	private void drawAvatar(BufferedImage avatar, Graphics2D g, int height) {
		float diameter = AVATAR_DIAMETER;
		Ellipse2D.Float clip = new Ellipse2D.Float(0, height - diameter, diameter, diameter);
		g.setPaint(new TexturePaint(resizeImage(avatar, AVATAR_DIAMETER, AVATAR_DIAMETER), new Rectangle(0, height - AVATAR_DIAMETER, AVATAR_DIAMETER, AVATAR_DIAMETER)));
		g.fill(clip);
	}
	
	private void drawMessageBlock(Graphics2D g, int width, int height) {
		g.setColor(new Color(BACKGROUND_COLOR));
		g.fillRoundRect((int) MESSAGE_BLOCK_X, 0, width - (int) MESSAGE_BLOCK_X, height, (int) MESSAGE_BLOCK_ROUND_RADIUS, (int) MESSAGE_BLOCK_ROUND_RADIUS);
		float diameter = MESSAGE_BLOCK_BEGIN_RADIUS * 2;
		Area area = new Area(new Rectangle2D.Float(MESSAGE_BLOCK_X - MESSAGE_BLOCK_BEGIN_RADIUS, height - MESSAGE_BLOCK_BEGIN_RADIUS, diameter, MESSAGE_BLOCK_BEGIN_RADIUS));
		Area clip = new Area(new Ellipse2D.Float(MESSAGE_BLOCK_X - diameter, height - diameter, diameter, diameter));
		area.subtract(clip);
		g.fill(area);
	}
	
	private void renderName(Graphics2D g, String name) {
		renderText(g, name, MESSAGE_TEXT_X, MESSAGE_NAME_BASELINE, data.color, NAME_FONT);
	}
	
	private void renderTime(Graphics2D g, long timestamp, int width, int height) {
		String time = new SimpleDateFormat("H:mm").format(new Date(timestamp));
		int w = width - TIME_X;
		if(timeMore10) w -= TIME_10_OFFSET;
		int h = height - TIME_BASELINE;
		renderText(g, time, w, h, new Color(TIME_COLOR));
	}
	
	public static int getXHeight(Graphics2D g) {
		FontRenderContext frc = g.getFontRenderContext();
		GlyphVector gv = g.getFont().createGlyphVector(frc, "x");
		return gv.getPixelBounds(null, 0, 0).height;
	}
	
	private void renderText(Graphics2D g, String text, float x, float y, Color color) {
		renderText(g, text, x, y, color, TEXT_FONT);
	}
	
	private void renderText(Graphics2D g, String text, float x, float y, Color color, Font font) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(text, x, y);
	}
	
	public static BufferedImage resizeImage(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	public static int getScreenResolution() {
		return GraphicsEnvironment.isHeadless() ? DEFAULT_SCREEN_RESOLUTION : Toolkit.getDefaultToolkit().getScreenResolution();
	}
	
//	TODO WTH is 72pt ?
	private static float fitSize(float pt) {
		return pt / (72.0F / SCREEN_RESOLUTION);
	}
	
	private BufferedImage renderDefaultProfilePicture(String r) {
		BufferedImage img = new BufferedImage(AVATAR_DIAMETER, AVATAR_DIAMETER, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(data.color);
		g.fillRect(0, 0, AVATAR_DIAMETER, AVATAR_DIAMETER);
		g.setColor(Color.WHITE);
		g.setFont(TEXT_FONT.deriveFont(fitSize(FONT_PROFILE_SIZE)));
		int x = (int) g.getFontMetrics().getStringBounds(r, g).getWidth();
		g.drawString(r, (AVATAR_DIAMETER - x) / 2, img.getHeight() - PROFILE_LETTERS_BASELINE);
		return img;
	}
}