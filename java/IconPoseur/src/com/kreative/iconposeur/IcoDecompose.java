package com.kreative.iconposeur;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;

public class IcoDecompose {
	public static void main(String[] args) {
		String format = "png";
		File dstDir = null;
		String dstName = null;
		boolean parseOpts = true;
		int argi = 0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("-f") && argi < args.length) {
					format = args[argi++];
				} else if (arg.equals("-r")) {
					format = "bin";
				} else if (arg.equals("-o") && argi < args.length) {
					File dst = new File(args[argi++]);
					if (dst.isDirectory()) {
						dstDir = dst;
						dstName = null;
					} else {
						dstDir = dst.getParentFile();
						dstName = dst.getName();
						if (!(
							dstName.contains("$WIDTH$") &&
							dstName.contains("$HEIGHT$") &&
							dstName.contains("$DEPTH$")
						)) {
							dstName += ".$WIDTH$x$HEIGHT$x$DEPTH$." + format;
						}
					}
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				File dd = (dstDir != null) ? dstDir : file.getParentFile();
				String dn = (dstName != null) ? dstName : (file.getName() + ".$WIDTH$x$HEIGHT$x$DEPTH$." + format);
				process(file, format, dd, dn);
				dstName = null;
			}
		}
	}
	
	private static void process(File src, String format, File dstDir, String dstName) {
		System.out.println(src.getName());
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
				System.out.print('\t');
				try {
					String name = dstName;
					name = name.replace("$WIDTH$", Integer.toString(e.getWidth()));
					name = name.replace("$HEIGHT$", Integer.toString(e.getHeight()));
					name = name.replace("$DEPTH$", Integer.toString(e.getBitsPerPixel()));
					File dst = new File(dstDir, name);
					if (format.equalsIgnoreCase("bin") || format.equalsIgnoreCase("raw")) {
						FileOutputStream out = new FileOutputStream(dst);
						out.write(e.getData());
						out.flush();
						out.close();
						System.out.println("OK");
					} else {
						BufferedImage image = e.getImage();
						if (image == null) {
							System.out.println("ERROR: null");
						} else {
							ImageIO.write(image, format, dst);
							System.out.println("OK");
						}
					}
				} catch (Exception e2) {
					System.out.println("ERROR: " + e2);
				}
			}
		} catch (Exception e) {
			System.out.println("\tERROR: " + e);
		}
	}
}
