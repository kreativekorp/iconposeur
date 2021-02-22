package com.kreative.iconposeur;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import javax.imageio.ImageIO;

public class IcnsDecompose {
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
						if (!dstName.contains("$TAG$")) {
							dstName += ".$TAG$." + format;
						}
					}
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				File dd = (dstDir != null) ? dstDir : file.getParentFile();
				String dn = (dstName != null) ? dstName : (file.getName() + ".$TAG$." + format);
				process(file, format, dd, dn);
				dstName = null;
			}
		}
	}
	
	private static void process(File src, String format, File dstDir, String dstName) {
		System.out.println(src.getName());
		try {
			MacIconSuite icns = new MacIconSuite();
			FileInputStream in = new FileInputStream(src);
			icns.read(new DataInputStream(in));
			in.close();
			for (Map.Entry<Integer,byte[]> e : icns.entrySet()) {
				StringBuffer sb = new StringBuffer();
				sb.append((char)((e.getKey() >> 24) & 0xFF));
				sb.append((char)((e.getKey() >> 16) & 0xFF));
				sb.append((char)((e.getKey() >>  8) & 0xFF));
				sb.append((char)((e.getKey() >>  0) & 0xFF));
				String tag = sb.toString();
				System.out.print('\t');
				System.out.print(tag);
				System.out.print('\t');
				System.out.print(e.getValue().length);
				System.out.print('\t');
				try {
					File dst = new File(dstDir, dstName.replace("$TAG$", tag));
					if (format.equalsIgnoreCase("bin") || format.equalsIgnoreCase("raw")) {
						FileOutputStream out = new FileOutputStream(dst);
						out.write(e.getValue());
						out.flush();
						out.close();
						System.out.println("OK");
					} else {
						BufferedImage image = icns.getImage(e.getKey());
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
