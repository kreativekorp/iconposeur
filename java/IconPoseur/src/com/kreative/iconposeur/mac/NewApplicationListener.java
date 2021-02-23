package com.kreative.iconposeur.mac;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.File;
import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.AppEvent.PrintFilesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PrintFilesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.kreative.iconposeur.Main;

public class NewApplicationListener {
	public NewApplicationListener() {
		Application a = Application.getApplication();
		a.setOpenFileHandler(new OpenFilesHandler() {
			@Override
			public void openFiles(final OpenFilesEvent e) {
				new Thread() {
					public void run() {
						for (Object o : e.getFiles()) {
							Main.openIcon((File)o);
						}
					}
				}.start();
			}
		});
		a.setPrintFileHandler(new PrintFilesHandler() {
			@Override
			public void printFiles(final PrintFilesEvent e) {
				new Thread() {
					public void run() {
						for (Object o : e.getFiles()) {
							Main.openIcon((File)o);
						}
					}
				}.start();
			}
		});
		a.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(final QuitEvent e, final QuitResponse r) {
				new Thread() {
					public void run() {
						System.gc();
						for (Window window : Window.getWindows()) {
							if (window.isVisible()) {
								window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
								if (window.isVisible()) {
									r.cancelQuit();
									return;
								}
							}
						}
						r.performQuit();
						System.exit(0);
					}
				}.start();
			}
		});
	}
}
