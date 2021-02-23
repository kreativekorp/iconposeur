package com.kreative.iconposeur.mac;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PrintFilesEvent;
import java.awt.desktop.PrintFilesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.WindowEvent;
import java.io.File;
import com.kreative.iconposeur.Main;

public class JavaApplicationListener {
	public JavaApplicationListener() {
		Desktop d = Desktop.getDesktop();
		d.setOpenFileHandler(new OpenFilesHandler() {
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
		d.setPrintFileHandler(new PrintFilesHandler() {
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
		d.setQuitHandler(new QuitHandler() {
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
