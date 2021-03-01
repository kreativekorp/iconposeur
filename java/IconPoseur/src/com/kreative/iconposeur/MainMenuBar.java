package com.kreative.iconposeur;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import com.kreative.iconposeur.datatransfer.ClearMenuItem;
import com.kreative.iconposeur.datatransfer.CopyMenuItem;
import com.kreative.iconposeur.datatransfer.CutMenuItem;
import com.kreative.iconposeur.datatransfer.PasteMenuItem;

public class MainMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
	public MainMenuBar(Window w, SaveInterface si, JMenu... menus) {
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new NewIcnsMenuItem());
		fileMenu.add(new NewIcoMenuItem());
		fileMenu.add(new OpenMenuItem());
		fileMenu.add(new CloseMenuItem(w));
		fileMenu.addSeparator();
		fileMenu.add(new SaveMenuItem(si));
		fileMenu.add(new SaveAsMenuItem(si));
		if (!SwingUtils.IS_MAC_OS) {
			fileMenu.addSeparator();
			fileMenu.add(new ExitMenuItem());
		}
		add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.add(new CutMenuItem());
		editMenu.add(new CopyMenuItem());
		editMenu.add(new PasteMenuItem());
		editMenu.add(new ClearMenuItem());
		add(editMenu);
		
		for (JMenu menu : menus) add(menu);
	}
	
	public static class NewIcnsMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public NewIcnsMenuItem() {
			super("New .icns File");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SwingUtils.SHORTCUT_KEY));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Main.newIcns();
				}
			});
		}
	}
	
	public static class NewIcoMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public NewIcoMenuItem() {
			super("New .ico File");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, SwingUtils.SHORTCUT_KEY | KeyEvent.SHIFT_MASK));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Main.newIco();
				}
			});
		}
	}
	
	public static class OpenMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public OpenMenuItem() {
			super("Open...");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SwingUtils.SHORTCUT_KEY));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Main.openIcon();
				}
			});
		}
	}
	
	public static class CloseMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public CloseMenuItem(final Window window) {
			super("Close Window");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, SwingUtils.SHORTCUT_KEY));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
				}
			});
		}
	}
	
	public static class SaveMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public SaveMenuItem(final SaveInterface si) {
			super("Save");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SwingUtils.SHORTCUT_KEY));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					si.save();
				}
			});
		}
	}
	
	public static class SaveAsMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public SaveAsMenuItem(final SaveInterface si) {
			super("Save As...");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, SwingUtils.SHORTCUT_KEY | KeyEvent.SHIFT_MASK));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					si.saveAs();
				}
			});
		}
	}
	
	public static class ExitMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		public ExitMenuItem() {
			super("Exit");
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, SwingUtils.SHORTCUT_KEY));
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.gc();
					for (Window window : Window.getWindows()) {
						if (window.isVisible()) {
							window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
							if (window.isVisible()) return;
						}
					}
					System.exit(0);
				}
			});
		}
	}
}
