package com.kreative.iconposeur;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IcoCompose {
	public static void main(String[] args) {
		WinIconDir ico = new WinIconDir();
		int bpp = 0;
		int[] colorTable = new int[0];
		File dst = null;
		boolean parseOpts = true;
		int argi = 0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("-c") || arg.equals("--cursor")) {
					ico.clear(true);
				} else if (arg.equals("-i") || arg.equals("--icon")) {
					ico.clear(false);
				} else if (arg.equals("-p") || arg.equals("--png")) {
					bpp = 0;
					colorTable = new int[0];
				} else if ((arg.equals("-q") || arg.equals("--pngthreshold")) && argi < args.length) {
					String qs = args[argi++];
					try { bpp = -Integer.parseInt(qs); }
					catch (Exception e) { bpp = 0; }
					colorTable = new int[0];
				} else if (arg.equals("-bw") || arg.equals("--blackwhite")) {
					bpp = 1;
					colorTable = ColorTables.createBlackToWhite(1);
				} else if (arg.equals("-wb") || arg.equals("--whiteblack")) {
					bpp = 1;
					colorTable = ColorTables.createWhiteToBlack(1);
				} else if (arg.equals("-bw2") || arg.equals("--blackwhite2bit")) {
					bpp = 2;
					colorTable = ColorTables.createBlackToWhite(2);
				} else if (arg.equals("-wb2") || arg.equals("--whiteblack2bit")) {
					bpp = 2;
					colorTable = ColorTables.createWhiteToBlack(2);
				} else if (arg.equals("-bw4") || arg.equals("--blackwhite4bit")) {
					bpp = 4;
					colorTable = ColorTables.createBlackToWhite(4);
				} else if (arg.equals("-wb4") || arg.equals("--whiteblack4bit")) {
					bpp = 4;
					colorTable = ColorTables.createWhiteToBlack(4);
				} else if (arg.equals("-bw8") || arg.equals("--blackwhite8bit")) {
					bpp = 8;
					colorTable = ColorTables.createBlackToWhite(8);
				} else if (arg.equals("-wb8") || arg.equals("--whiteblack8bit")) {
					bpp = 8;
					colorTable = ColorTables.createWhiteToBlack(8);
				} else if (arg.equals("-a1") || arg.equals("--adaptive1bit")) {
					bpp = 1;
					colorTable = new int[2];
				} else if (arg.equals("-a2") || arg.equals("--adaptive2bit")) {
					bpp = 2;
					colorTable = new int[4];
				} else if (arg.equals("-a4") || arg.equals("--adaptive4bit")) {
					bpp = 4;
					colorTable = new int[16];
				} else if (arg.equals("-a8") || arg.equals("--adaptive8bit")) {
					bpp = 8;
					colorTable = new int[256];
				} else if (arg.equals("-w4") || arg.equals("--windows4bit")) {
					bpp = 4;
					colorTable = ColorTables.createWindows4();
				} else if (arg.equals("-m4") || arg.equals("--macintosh4bit")) {
					bpp = 4;
					colorTable = ColorTables.createMacintosh4();
				} else if (arg.equals("-w8") || arg.equals("--windows8bit")) {
					bpp = 8;
					colorTable = ColorTables.createWindowsBase();
				} else if (arg.equals("-we") || arg.equals("--windowseis")) {
					bpp = 8;
					colorTable = ColorTables.createWindowsEis();
				} else if (arg.equals("-wp") || arg.equals("--windowspaint")) {
					bpp = 8;
					colorTable = ColorTables.createWindowsPaint();
				} else if (arg.equals("-ww") || arg.equals("--windowswebsafe")) {
					bpp = 8;
					colorTable = ColorTables.createWindowsWebSafe();
				} else if (arg.equals("-m8") || arg.equals("--macintosh8bit")) {
					bpp = 8;
					colorTable = ColorTables.createMacintosh8();
				} else if (arg.equals("-h") || arg.equals("--highcolor")) {
					bpp = 16;
					colorTable = new int[0];
				} else if (arg.equals("-t") || arg.equals("--truecolor")) {
					bpp = 24;
					colorTable = new int[0];
				} else if (arg.equals("-a") || arg.equals("--truealpha")) {
					bpp = 32;
					colorTable = new int[0];
				} else if (arg.equals("-o") && argi < args.length) {
					dst = new File(args[argi++]);
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				putImage(ico, bpp, colorTable, file);
			}
		}
		if (dst == null) dst = new File("out.ico");
		write(ico, dst);
	}
	
	private static void putImage(WinIconDir ico, int bpp, int[] colorTable, File file) {
		System.out.print(file.getName() + "... ");
		try {
			WinIconDirEntry e = new WinIconDirEntry(ico.isCursor());
			BufferedImage image = ImageIO.read(file);
			if (bpp > 0) {
				e.setBMPImage(image, bpp, colorTable);
			} else if (image.getWidth() < (-bpp) && image.getHeight() < (-bpp)) {
				e.setBMPImage(image, 32, colorTable);
			} else {
				e.setPNGImage(image);
			}
			ico.add(e);
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
	
	private static void write(WinIconDir ico, File file) {
		System.out.print(file.getName() + "... ");
		try {
			FileOutputStream out = new FileOutputStream(file);
			ico.write(out);
			out.flush();
			out.close();
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
}
