package net.krows_team.sticker_bot;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageIO;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.UserProfilePhotos;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.response.GetFileResponse;

public class StickerRenderer {
	
	private TelegramBot bot;
	
	private static boolean DEBUG = false;
	
	private static final int BACKGROUND_COLOR = 0x1c2028;
	private static final int TIME_COLOR = 0x787D87;
	private static int MIN_HEIGHT = 103;
	private static int MAX_WIDTH = 512;
	private static final int AVATAR_DIAMETER = 53;
	private static final float FONT_SIZE = 12.25F;
	private static final float FONT_NAME_SIZE = 12F;
	private static final float FONT_PROFILE_SIZE = 17F;
	private static final float MESSAGE_BLOCK_X = AVATAR_DIAMETER + 15;
	private static final float MESSAGE_BLOCK_ROUND_RADIUS = 17;
	private static final float MESSAGE_BLOCK_BEGIN_RADIUS = 12;
	private static final float MESSAGE_TEXT_X = MESSAGE_BLOCK_X + 21;
	private static final float MESSAGE_NAME_BASELINE = 35;
	private static final float MESSAGE_TEXT_BASELINE = 63;
	private static final int TIME_HEIGHT = 15;
	private static final int TIME_BASELINE = 12;
	private static final int TIME_X = 59;
	private static final int MAX_TEXT_TIME_GAP = 13;
	private static final int MAX_TEXT_GAP = 20;
	private static final int PROFILE_LETTERS_BASELINE = 16;
	private static final int TIME_10_OFFSET = 14;
	private static final String FONT_NAME = "Open Sans Medium";
	
	private Font NAME_FONT = new Font(FONT_NAME, Font.BOLD, 0).deriveFont(fitSize(FONT_NAME_SIZE));
	private Font TEXT_FONT = new Font(FONT_NAME, Font.PLAIN, 0).deriveFont(fitSize(FONT_SIZE));
	
	private Color color;
	private boolean timeAdjust = true;
	private boolean timeMore10 = false;
	private int lastLineWidth = 0;
	private int lineCount = 0;
	
	public StickerRenderer(TelegramBot bot) {
		this.bot = bot;
	}
	
	public BufferedImage renderMessage(Message msg) throws MalformedURLException, IOException {
		long timestamp = (msg.forwardDate() == null ? msg.date() : msg.forwardDate()) * 1000L;
		var date = new Date(timestamp);
		timeMore10 = date.getHours() > 9;
		var name = msg.forwardFrom() != null ? msg.forwardFrom().username() : msg.forwardSenderName() != null ? msg.forwardSenderName() : msg.from().username();
		color = Colors.getFixed(name).createColor();
		String msgText = msg.text();
		BufferedImage img = new BufferedImage(MAX_WIDTH, MIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		var photo = getProfilePhoto(msg.from().id());
		BufferedImage profilePhoto = null;
		if(msg.forwardSenderName() != null || msg.forwardFrom() != null) {
			if(msg.forwardFrom() == null) profilePhoto = renderDefaultProfilePicture(msg.forwardSenderName(), null);
			else {
				photo = getProfilePhoto(msg.forwardFrom().id());
				if(photo.totalCount() == 0) profilePhoto = renderDefaultProfilePicture(msg.forwardFrom().firstName(), msg.forwardFrom().lastName());
				else profilePhoto = ImageIO.read(new URL(getURLById(photo.photos()[0][0].fileId())));
			}
		} else if(photo.totalCount() == 0) profilePhoto = renderDefaultProfilePicture(msg.from().firstName(), msg.from().lastName());
		else profilePhoto = ImageIO.read(new URL(getURLById(photo.photos()[0][0].fileId())));
		Graphics2D g = img.createGraphics();
		initGraphics(g);
		float trueHeight = getHeight(g, msgText, MAX_WIDTH);
		trueHeight += MESSAGE_NAME_BASELINE + 14;
		if(timeAdjust) trueHeight += TIME_BASELINE / 2;
		else trueHeight += TIME_HEIGHT + TIME_BASELINE;
		MIN_HEIGHT = (int) trueHeight;
		if(timeAdjust && lineCount == 1) MAX_WIDTH = (int) Math.min(MAX_WIDTH, (MESSAGE_TEXT_X + lastLineWidth + MAX_TEXT_TIME_GAP + TIME_X + (timeMore10 ? TIME_10_OFFSET : 0)));
		img = new BufferedImage(MAX_WIDTH, (int) MIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		if(DEBUG) {
			initDebug(img, g);
			return null;
		}
		initGraphics(g);
		fillTransparent(g, MAX_WIDTH, MIN_HEIGHT);
		drawAvatar(profilePhoto, g, MIN_HEIGHT);
		drawMessageBlock(g, MAX_WIDTH, MIN_HEIGHT);
		String from = null;
		if(msg.forwardFrom() != null) {
			from = msg.forwardFrom().firstName() + (msg.forwardFrom().lastName() == null ? "" : " " + msg.forwardFrom().lastName());
		} else if(msg.forwardSenderName() != null) { 
			from = msg.forwardSenderName();
		} else from = msg.from().firstName() + (msg.from().lastName() == null ? "" : " " + msg.from().lastName());
		renderName(g, from);
		renderText(g, msg.text());
		renderTime(g, timestamp, MAX_WIDTH, MIN_HEIGHT);
		if (img.getWidth() != 512) return resize(img, 512, (int) (img.getHeight() * (512.0F / img.getWidth())));
		return img;
	}
	
	private void initDebug(BufferedImage img, Graphics2D g) throws IOException {
		String msgName = "Ekaterina";
//		String msgName = "Danil Fedorovykh";
		String msgText = "Если бы у меня были права, я бы банила, но я женщина, у женщин нет прав даже в чате кнада";
//		String msgText = "Лох";
//		String msgText = "что мне делать с этими результатами?";
		color = Colors.random().createColor();
		BufferedImage profilePhoto = null;
		initGraphics(g);
		fillTransparent(g, MAX_WIDTH, MIN_HEIGHT);
		drawAvatar(profilePhoto, g, MIN_HEIGHT);
		drawMessageBlock(g, MAX_WIDTH, MIN_HEIGHT);
		renderName(g, msgName);
		renderText(g, msgText);
		renderTime(g, new Date().getTime(), MAX_WIDTH, MIN_HEIGHT);
		ImageIO.write(img, "png", new File("C:/Users/Krows/Desktop/tmp.png"));
		Desktop.getDesktop().open(new File("C:/Users/Krows/Desktop/tmp.png"));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(img), null);
	}
	
	public void initGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	public void fillTransparent(Graphics2D g, int width, int height) {
		g.setColor(new Color(0, true));
		g.fillRect(0, 0, width, height);
	}
	
	public void drawAvatar(BufferedImage avatar, Graphics2D g, int height) {
		float diameter = AVATAR_DIAMETER;
		var clip = new Ellipse2D.Float(0, height - diameter, diameter, diameter);
		g.setPaint(new TexturePaint(resize(avatar, AVATAR_DIAMETER, AVATAR_DIAMETER), new Rectangle(0, height - AVATAR_DIAMETER, AVATAR_DIAMETER, AVATAR_DIAMETER)));
		g.fill(clip);
	}
	
	public void drawMessageBlock(Graphics2D g, int width, int height) {
		g.setColor(new Color(BACKGROUND_COLOR));
		g.fillRoundRect((int) MESSAGE_BLOCK_X, 0, width - (int) MESSAGE_BLOCK_X, height, (int) MESSAGE_BLOCK_ROUND_RADIUS, (int) MESSAGE_BLOCK_ROUND_RADIUS);
		float diameter = MESSAGE_BLOCK_BEGIN_RADIUS * 2;
		Area area = new Area(new Rectangle2D.Float(MESSAGE_BLOCK_X - MESSAGE_BLOCK_BEGIN_RADIUS, height - MESSAGE_BLOCK_BEGIN_RADIUS, diameter, MESSAGE_BLOCK_BEGIN_RADIUS));
		Area clip = new Area(new Ellipse2D.Float(MESSAGE_BLOCK_X - diameter, height - diameter, diameter, diameter));
		area.subtract(clip);
		g.fill(area);
	}
	
	public void renderName(Graphics2D g, String name) {
		renderText(g, name, MESSAGE_TEXT_X, MESSAGE_NAME_BASELINE, color, NAME_FONT);
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	public void renderTime(Graphics2D g, long timestamp, int width, int height) {
		String time = new SimpleDateFormat("H:mm").format(new Date(timestamp));
		int w = width - TIME_X;
		if(timeMore10) w -= TIME_10_OFFSET;
		int h = height - TIME_BASELINE;
		renderText(g, time, w, h, new Color(TIME_COLOR));
	}
	
	public void renderText(Graphics2D g, String text, float x, float y, Color color) {
		renderText(g, text, x, y, color, TEXT_FONT);
	}
	
	public void renderText(Graphics2D g, String text, float x, float y, Color color, Font font) {
		g.setColor(color);
		g.setFont(font);
		g.drawString(text, x, y);
	}
	
	public void renderText(Graphics2D g, String text) {
		g.setColor(new Color(0xEBEBEB));
		g.setFont(TEXT_FONT);
		renderText(g, text, MAX_WIDTH, MIN_HEIGHT);
	}
	
	public void renderText(Graphics2D g, String text, int width, int height) {
        AttributedCharacterIterator paragraph = new AttributedString(text, Map.of(TextAttribute.FONT, TEXT_FONT)).getIterator();
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        FontRenderContext frc = g.getFontRenderContext();
        var lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        
        float breakWidth = width - MESSAGE_TEXT_X - MAX_TEXT_GAP;
        float y = MESSAGE_TEXT_BASELINE;
        lineMeasurer.setPosition(paragraphStart);
        
        while (lineMeasurer.getPosition() < paragraphEnd) {
            TextLayout layout = lineMeasurer.nextLayout(breakWidth);
            layout.draw(g, MESSAGE_TEXT_X, y);
            y += layout.getAscent();
            y += layout.getDescent() + layout.getLeading();
        }
	}
	
	public String getURLById(String id) {
		GetFile request = new GetFile(id);
		GetFileResponse response = bot.execute(request);
		return bot.getFullFilePath(response.file());
	}
	
	public UserProfilePhotos getProfilePhoto(long id) {
		return bot.execute(new GetUserProfilePhotos(id)).photos();
	}
	
	public float getHeight(Graphics2D g, String text, int width) {
		AttributedCharacterIterator paragraph = new AttributedString(text, Map.of(TextAttribute.FONT, TEXT_FONT)).getIterator();
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        FontRenderContext frc = g.getFontRenderContext();
        var lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        
        float breakWidth = width - MESSAGE_TEXT_X - MAX_TEXT_GAP;
        lineMeasurer.setPosition(paragraphStart);
		TextLayout layout = null;
		float r = 0;
		while (lineMeasurer.getPosition() < paragraphEnd) {
            layout = lineMeasurer.nextLayout(breakWidth);
            r += layout.getAscent() + layout.getDescent() + layout.getLeading();
            lineCount++;
        }
		lastLineWidth = (int) layout.getBounds().getWidth();
		if (TIME_X + MAX_TEXT_TIME_GAP + lastLineWidth > width - MESSAGE_TEXT_X - (timeMore10 ? TIME_10_OFFSET : 0)) timeAdjust = false;
		return r;
	}
	
	private float fitSize(float pt) {
		return pt / (72.0F / Toolkit.getDefaultToolkit().getScreenResolution());
	}
	
	public BufferedImage renderDefaultProfilePicture(String firstName, String lastName) {
		String r = "" + Character.toUpperCase(firstName.charAt(0)) + (lastName == null ? "" : Character.toUpperCase(lastName.charAt(0)));
		BufferedImage img = new BufferedImage(AVATAR_DIAMETER, AVATAR_DIAMETER, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, AVATAR_DIAMETER, AVATAR_DIAMETER);
		g.setColor(Color.WHITE);
		g.setFont(TEXT_FONT.deriveFont(fitSize(FONT_PROFILE_SIZE)));
		int x = (int) g.getFontMetrics().getStringBounds(r, g).getWidth();
		g.drawString(r, (AVATAR_DIAMETER - x) / 2, img.getHeight() - PROFILE_LETTERS_BASELINE);
		return img;
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		DEBUG = true;
		new StickerRenderer(null).renderMessage(null);
	}
}

// TODO Fix Default Avatar Render
// TODO Render Emojis