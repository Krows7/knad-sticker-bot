package net.krows_team.sticker_bot.text;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.krows_team.sticker_bot.StickerRenderer;

public class TextRenderer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TextRenderer.class);
	
	public static void renderText(Graphics2D g, String text, int x, int y, int width) {
		LOGGER.debug(String.format("getBounds(%s)", width));
		LOGGER.debug("Font Height: " + g.getFontMetrics().getHeight());
		List<Node> nodes = CustomEmojiParser.splitIntoNodes(text);
		int xx = x;
		for(Node node : nodes) {
			int nodeWidth = node.getWidth(g);
			if(node instanceof EmojiNode) {
				if(nodeWidth > width - xx + x) {
					y += g.getFontMetrics().getHeight();
					xx = x;
				}
				node.render(g, xx, y);
				xx += nodeWidth + 1;
			} else {
				TextNode t = (TextNode) node;
				AttributedString a = new AttributedString(t.getText());
				a.addAttribute(TextAttribute.FONT, StickerRenderer.TEXT_FONT);
				AttributedCharacterIterator paragraph = a.getIterator();
		        int paragraphStart = paragraph.getBeginIndex();
		        int paragraphEnd = paragraph.getEndIndex();
		        FontRenderContext frc = g.getFontRenderContext();
		        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);
		        lineMeasurer.setPosition(paragraphStart);
		        boolean previousNull = false;
		        
		        while (lineMeasurer.getPosition() < paragraphEnd) {
		        	TextLayout layout = lineMeasurer.nextLayout(width - xx + x, paragraphEnd, !previousNull);
		        	if(layout == null || (nodeWidth = layout.getBounds().getBounds().width) > width - xx + x) {
						y += g.getFontMetrics().getHeight();
						xx = x;
					}
		            if(layout != null) {
		            	layout.draw(g, xx, y);
		            	xx += nodeWidth + 1;
		            }
		            previousNull = layout == null;
		        }
			}
		}
	}
	
	public static float[] getBounds(Graphics2D g, String text, int width) {
		LOGGER.debug("getBounds(" + width +")");
		LOGGER.debug("Font Height: " + g.getFontMetrics().getHeight());
        TextLayout layout = null;
		List<Node> nodes = CustomEmojiParser.splitIntoNodes(text);
		float y = g.getFontMetrics().getHeight();
		float xx = 0;
		int lineCount = 1;
		for(Node node : nodes) {
			LOGGER.debug(node.toString());
			int nodeWidth = node.getWidth(g);
			if(node instanceof EmojiNode) {
				if(nodeWidth > width - xx) {
					LOGGER.debug(String.format("EmojiNode width: %s; Left space: %s", nodeWidth, width - xx));
					y += g.getFontMetrics().getHeight();
					lineCount++;
					xx = 0;
				}
				xx += nodeWidth + 1;
			} else {
				TextNode t = (TextNode) node;
				AttributedString a = new AttributedString(t.getText());
				a.addAttribute(TextAttribute.FONT, StickerRenderer.TEXT_FONT);
				AttributedCharacterIterator paragraph = a.getIterator();
		        int paragraphStart = paragraph.getBeginIndex();
		        int paragraphEnd = paragraph.getEndIndex();
		        FontRenderContext frc = g.getFontRenderContext();
		        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);
		        lineMeasurer.setPosition(paragraphStart);
		        int prev = lineMeasurer.getPosition();
		        boolean previousNull = false;
		        
		        while (lineMeasurer.getPosition() < paragraphEnd) {
		        	layout = lineMeasurer.nextLayout(width - xx, paragraphEnd, !previousNull);
		        	if(layout != null) LOGGER.debug(String.format("Next Node line: [%s]", text.substring(prev, lineMeasurer.getPosition())));
		        	if(layout == null || (nodeWidth = layout.getBounds().getBounds().width) > width - xx) {
		        		lineCount++;
		        		//y += layout.getAscent() + layout.getDescent() + layout.getLeading();
		        		y += g.getFontMetrics().getHeight();
						xx = 0;
					}
		        	prev = lineMeasurer.getPosition();
		            if(layout != null) {
		            	LOGGER.debug(String.format("Layout Height: %s", layout.getAscent() + layout.getDescent() + layout.getLeading()));
		            	xx += nodeWidth + 1;
		            }
		            previousNull = layout == null;
		        }
			}
		}
		float lastLineWidth = xx;
		LOGGER.debug(String.format("Total Nodes: %s", nodes.size()));
		return new float[] {lastLineWidth, y, lineCount};
	}
}
