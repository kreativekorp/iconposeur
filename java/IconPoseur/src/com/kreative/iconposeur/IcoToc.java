package com.kreative.iconposeur;

import java.io.File;
import java.io.FileInputStream;

public class IcoToc {
	public static void main(String[] args) {
		boolean colors = false;
		boolean parsingArgs = true;
		for (String arg : args) {
			if (parsingArgs && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parsingArgs = false;
				} else if (arg.equals("-c")) {
					colors = true;
				} else if (arg.equals("-C")) {
					colors = false;
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				System.out.println(arg);
				File file = new File(arg);
				process(file, colors);
			}
		}
	}
	
	private static void process(File src, boolean colors) {
		try {
			WinIconDir ico = new WinIconDir();
			FileInputStream in = new FileInputStream(src);
			ico.read(in);
			in.close();
			System.out.println("\tW\tH\tP\tBPP\tCC\tLEN\tPNG");
			for (WinIconDirEntry e : ico) {
				System.out.print('\t');
				System.out.print(e.getWidth());
				System.out.print('\t');
				System.out.print(e.getHeight());
				System.out.print('\t');
				System.out.print(e.getPlanes());
				System.out.print('\t');
				System.out.print(e.getBitsPerPixel());
				System.out.print('\t');
				System.out.print(e.getColorCount());
				System.out.print('\t');
				System.out.print(e.getDataLength());
				System.out.print('\t');
				System.out.print(e.isPNG());
				System.out.println();
				if (colors) {
					for (int color : e.getColorTable()) {
						int r = ((color >> 16) & 0xFF);
						int g = ((color >>  8) & 0xFF);
						int b = ((color >>  0) & 0xFF);
						int k = (r*30 + g*59 + b*11);
						System.out.print("\u001B[48;2;" + r + ";" + g + ";" + b + "m");
						System.out.print((k < 12750) ? "\u001B[97m" : "\u001B[30m");
						System.out.print(" " + Integer.toHexString(color).toUpperCase() + " ");
						System.out.print("\u001B[0m");
					}
					System.out.println();
				}
			}
		} catch (Exception e) {
			System.out.println("\tERROR: " + e);
		}
	}
}
