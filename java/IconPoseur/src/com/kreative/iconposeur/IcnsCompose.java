package com.kreative.iconposeur;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class IcnsCompose {
	private static final Pattern tagPattern = Pattern.compile(
		"[.](ICON|ICN#|icl[48]|ic[msht][#48]|i[mslht]32|[mslht]8mk|icp[4-6]|ic0[4-9]|ic1[0-4])[.]"
	);
	
	public static void main(String[] args) {
		MacIconSuite icns = new MacIconSuite();
		boolean raw = false;
		int tag = 0;
		File dst = null;
		boolean parseOpts = true;
		int argi = 0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("-t") && argi < args.length) {
					raw = false;
					tag = parseTag(args[argi++]);
				} else if (arg.equals("-r") && argi < args.length) {
					raw = true;
					tag = parseTag(args[argi++]);
				} else if (arg.equals("-o") && argi < args.length) {
					dst = new File(args[argi++]);
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				if (raw) putRaw(icns, tag, file);
				else putImage(icns, tag, file);
				raw = false;
				tag = 0;
			}
		}
		if (dst == null) dst = new File("out.icns");
		write(icns, dst);
	}
	
	private static int parseTag(String s) {
		int tag = 0;
		char[] chars = s.trim().toCharArray();
		tag |= (((chars.length > 0) ? (chars[0] & 0xFF) : 0x20) << 24);
		tag |= (((chars.length > 1) ? (chars[1] & 0xFF) : 0x20) << 16);
		tag |= (((chars.length > 2) ? (chars[2] & 0xFF) : 0x20) <<  8);
		tag |= (((chars.length > 3) ? (chars[3] & 0xFF) : 0x20) <<  0);
		return tag;
	}
	
	private static void putImage(MacIconSuite icns, int tag, File file) {
		System.out.print(file.getName() + "... ");
		try {
			BufferedImage image = ImageIO.read(file);
			if (tag == 0) {
				Matcher m = tagPattern.matcher(file.getName());
				if (m.find()) {
					icns.putImage(parseTag(m.group(1)), image);
				} else {
					icns.putImage(image);
				}
			} else {
				icns.putImage(tag, image);
			}
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
	
	private static void putRaw(MacIconSuite icns, int tag, File file) {
		System.out.print(file.getName() + "... ");
		try {
			FileInputStream in = new FileInputStream(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1 << 20]; int read;
			while ((read = in.read(buf)) >= 0) out.write(buf, 0, read);
			out.flush();
			out.close();
			in.close();
			if (tag == 0) {
				Matcher m = tagPattern.matcher(file.getName());
				if (m.find()) {
					icns.put(parseTag(m.group(1)), out.toByteArray());
				} else {
					System.out.println("ERROR: no tag specified");
					return;
				}
			} else {
				icns.put(tag, out.toByteArray());
			}
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
	
	private static void write(MacIconSuite icns, File file) {
		System.out.print(file.getName() + "... ");
		try {
			FileOutputStream out = new FileOutputStream(file);
			icns.write(new DataOutputStream(out));
			out.flush();
			out.close();
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("ERROR: " + e);
		}
	}
}
