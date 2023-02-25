package com.kreative.iconposeur;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main {
	public static void main(String[] args) {
		try { System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Icon Poseur"); } catch (Exception e) {}
		try { System.setProperty("apple.laf.useScreenMenuBar", "true"); } catch (Exception e) {}
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
		
		try {
			Method getModule = Class.class.getMethod("getModule");
			Object javaDesktop = getModule.invoke(Toolkit.getDefaultToolkit().getClass());
			Object allUnnamed = getModule.invoke(Main.class);
			Class<?> module = Class.forName("java.lang.Module");
			Method addOpens = module.getMethod("addOpens", String.class, module);
			addOpens.invoke(javaDesktop, "sun.awt.X11", allUnnamed);
		} catch (Exception e) {}
		
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			Field aacn = tk.getClass().getDeclaredField("awtAppClassName");
			aacn.setAccessible(true);
			aacn.set(tk, "IconPoseur");
		} catch (Exception e) {}
		
		if (args.length == 0) {
			newIcns();
		} else {
			for (String arg : args) {
				openIcon(new File(arg));
			}
		}
		
		try { Class.forName("com.kreative.iconposeur.mac.MyApplicationListener").newInstance(); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static IcnsFrame newIcns() {
		IcnsFrame f = new IcnsFrame();
		f.setVisible(true);
		return f;
	}
	
	public static IcoFrame newIco() {
		IcoFrame f = new IcoFrame();
		f.setVisible(true);
		return f;
	}
	
	private static String lastOpenDirectory = null;
	public static JFrame openIcon() {
		Frame frame = new Frame();
		FileDialog fd = new FileDialog(frame, "Open", FileDialog.LOAD);
		if (lastOpenDirectory != null) fd.setDirectory(lastOpenDirectory);
		fd.setVisible(true);
		String parent = fd.getDirectory();
		String name = fd.getFile();
		fd.dispose();
		frame.dispose();
		if (parent == null || name == null) return null;
		File file = new File((lastOpenDirectory = parent), name);
		return openIcon(file);
	}
	
	public static JFrame openIcon(File file) {
		if (file == null) {
			return openIcon();
		} else try {
			if (file.getName().toLowerCase().endsWith(".ico")) {
				IcoFrame f = new IcoFrame(file);
				f.setVisible(true);
				return f;
			} else {
				IcnsFrame f = new IcnsFrame(file);
				f.setVisible(true);
				return f;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null, "An error occurred while opening this file.",
				"Open", JOptionPane.ERROR_MESSAGE
			);
			return null;
		}
	}
	
	private static String lastSaveDirectory = null;
	public static File getSaveFile(Frame frame, String suffix) {
		FileDialog fd = new FileDialog(frame, "Save", FileDialog.SAVE);
		if (lastSaveDirectory != null) fd.setDirectory(lastSaveDirectory);
		fd.setVisible(true);
		String parent = fd.getDirectory();
		String name = fd.getFile();
		fd.dispose();
		if (parent == null || name == null) return null;
		if (!name.toLowerCase().endsWith(suffix.toLowerCase())) name += suffix;
		return new File((lastSaveDirectory = parent), name);
	}
}
