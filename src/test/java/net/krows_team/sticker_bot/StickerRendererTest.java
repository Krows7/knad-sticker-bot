package net.krows_team.sticker_bot;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import net.krows_team.sticker_bot.util.OptionalPair;
import net.krows_team.sticker_bot.util.TransferableImage;
import net.krows_team.sticker_bot.util.Utils;

public class StickerRendererTest {
	
	private static BufferedImage makeSample(String name, String text) {
		long timestamp = new Date().getTime();
		Color color = Colors.random().createColor();
		StickerRenderer r = new StickerRenderer(new StickerData(timestamp, name, text, color, OptionalPair.of(name.toUpperCase().charAt(0) + "")));
		r.renderMessage();
		return r.fitImage();
	}
	
	public static BufferedImage helloWorldSample() {
		String msgName = "Ekaterina";
		String msgText = "Если бы у меня были права, я бы банила, но я женщина, у женщин нет прав даже в чате кнада";
		return makeSample(msgName, msgText);
	}
	
	public static BufferedImage firstEmojiRowSmaple() {
		String msgName = "Ekaterina";
		String msgText = "😀😃😄😁😆😅😂🤣🥲☺️😊😇🙂🙃😉😌😍🥰😘😗😙😚😋😛😝😜🤪🤨🧐🤓😎🥸";
		return makeSample(msgName, msgText);
	}
	
	public static BufferedImage multilineEmojiSample() {
		String msgName = "Ekaterina";
		String msgText = "😀😃😄😁😆😅😂🤣🥲☺️😊😇🙂🙃😉😌😍🥰😘😗😙😚😋😛😝😜🤪🤨🧐🤓😎🥸";
		msgText += msgText;
		return makeSample(msgName, msgText);
	}
	
	public static BufferedImage wordWrapSample() {
		String msgName = "Ekaterina";
		String msgText = "Что-то в началеСуперМегаДуперУльтраДлинноеСловоИОноСлитное";
		return makeSample(msgName, msgText);
	}
	
	public static BufferedImage textWithEmojiSample() {
		String msgName = "Ekaterina";
		String msgText = "😀😃😄😁😆sddcedc😅😂🤣🥲☺️😊😇kjdcs🙂🙃😉😌😍vdsfjk🥰😘😗😙😚😋😛😝😜wejfewio🤪🤨icuwehfwecweuic🧐🤓😎🥸vdsfjk🥰😘😗😙😚😋😛😝😜wejfewio🤪🤨icuwehfwecweuic🧐🤓😎🥸vdsfjk🥰😘😗😙😚😋😛😝😜wejfewio🤪🤨icuwehfwecweuic🧐🤓😎🥸vdsfjk🥰😘😗😙😚😋😛😝😜wejfewio🤪🤨icuwehfwecweuic🧐🤓😎🥸vdsfjk🥰😘😗😙😚😋😛😝😜wejfewio🤪🤨icuwehfwecweuic🧐🤓😎🥸";
		return makeSample(msgName, msgText);
	}
	
	public static BufferedImage createSample(Supplier<BufferedImage> s) {
		return s.get();
	}
	
	public static void main(String[] args) throws IOException {
		Supplier<BufferedImage> sample = StickerRendererTest::helloWorldSample;
		BufferedImage sampleImage = createSample(sample);
		
		String samplePath = Utils.getResourcePath("tmp.png");
		ImageIO.write(sampleImage, "png", new File(samplePath));
		
		if(Utils.contains(args, "show")) Desktop.getDesktop().open(new File(samplePath));
		if(Utils.contains(args, "copy")) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(sampleImage), null);
	}
}
