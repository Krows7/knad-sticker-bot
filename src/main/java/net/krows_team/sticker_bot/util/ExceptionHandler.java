package net.krows_team.sticker_bot.util;

import java.util.function.Consumer;

public class ExceptionHandler<T> implements Runnable {

	private Executable script;

	private Consumer<Exception> handler;

	public static <T> ExceptionHandler<T> create(Executable script, Consumer<Exception> handler) {
		return new ExceptionHandler<>(script, handler);
	}

	private ExceptionHandler(Executable script, Consumer<Exception> handler) {
		this.script = script;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			script.execute();
		} catch (Exception e) {
			handler.accept(e);
		}
	}
}
