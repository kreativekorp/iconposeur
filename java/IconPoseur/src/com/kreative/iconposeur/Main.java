package com.kreative.iconposeur;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
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
			String lcname = file.getName().toLowerCase();
			if (lcname.endsWith(".ico")) {
				IcoFrame f = new IcoFrame(file);
				f.setVisible(true);
				return f;
			} else if (lcname.endsWith(".icns")) {
				IcnsFrame f = new IcnsFrame(file);
				f.setVisible(true);
				return f;
			} else {
				Map<?,?> icons = IcnsExtract.getIconSuiteData(file);
				if (icons != null) return openIcons(file.getName(), icons);
				IcnsFrame f = new IcnsFrame(file);
				f.setVisible(true);
				return f;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				null, "This file could not be recognized as a valid icon file.",
				"Open", JOptionPane.ERROR_MESSAGE
			);
			return null;
		}
	}
	
	public static JFrame openIcons(String name, Map<?,?> icons) {
		if (icons == null || icons.isEmpty()) {
			JOptionPane.showMessageDialog(
				null, "Could not open this file because it contains no icons.",
				"Open", JOptionPane.ERROR_MESSAGE
			);
			return null;
		}
		if (icons.size() > 1) {
			IcnsListFrame f = new IcnsListFrame(name, icons);
			f.setVisible(true);
			return f;
		}
		for (Object o : icons.values()) {
			if (o instanceof MacIconSuite) {
				IcnsFrame f = new IcnsFrame((MacIconSuite)o);
				f.setVisible(true);
				return f;
			}
			if (o instanceof byte[]) try {
				IcnsFrame f = new IcnsFrame((byte[])o);
				f.setVisible(true);
				return f;
			} catch (IOException e) {}
		}
		JOptionPane.showMessageDialog(
			null, "Could not open this file because it contains no icons.",
			"Open", JOptionPane.ERROR_MESSAGE
		);
		return null;
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
