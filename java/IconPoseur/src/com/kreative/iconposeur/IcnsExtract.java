package com.kreative.iconposeur;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class IcnsExtract {
	public static void main(String[] args) {
		String format = "icns";
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
					format = "icns";
				} else if (arg.equals("-o") && argi < args.length) {
					File dst = new File(args[argi++]);
					if (dst.isDirectory()) {
						dstDir = dst;
						dstName = null;
					} else {
						dstDir = dst.getParentFile();
						dstName = dst.getName();
						if (!dstName.contains("$ID$")) {
							dstName += ".$ID$." + format;
						}
					}
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				File dd = (dstDir != null) ? dstDir : file.getParentFile();
				String dn = (dstName != null) ? dstName : (file.getName() + ".$ID$." + format);
				process(file, format, dd, dn);
				dstName = null;
			}
		}
	}
	
	private static void process(File src, String format, File dstDir, String dstName) {
		System.out.println(src.getName());
		MacResourceFile rsrc = getResourceFile(src);
		if (rsrc == null) {
			System.out.println("\tNo resources found.");
			return;
		}
		Map<Integer,Object> icons = getIconSuiteData(rsrc);
		if (icons.isEmpty()) {
			System.out.println("\tNo icons found.");
			return;
		}
		for (Map.Entry<Integer,Object> e : icons.entrySet()) {
			System.out.print('\t');
			System.out.print(e.getKey());
			System.out.print('\t');
			try {
				String filename;
				if (e.getKey().intValue() == -16455) {
					filename = dstName.replaceAll("(.)[$]ID[$]\\1|[$]ID[$]", "$1");
				} else {
					filename = dstName.replace("$ID$", e.getKey().toString());
				}
				File dst = new File(dstDir, filename);
				FileOutputStream out = new FileOutputStream(dst);
				write(e.getValue(), format, out);
				out.flush();
				out.close();
				System.out.println("OK");
			} catch (Exception e2) {
				System.out.println("ERROR: " + e2);
			}
		}
	}
	
	private static void write(Object o, String format, OutputStream out) throws IOException {
		if (format.equalsIgnoreCase("icns")) {
			if (o instanceof byte[]) {
				out.write((byte[])o);
				return;
			}
			if (o instanceof MacIconSuite) {
				((MacIconSuite)o).write(new DataOutputStream(out));
				return;
			}
		} else {
			if (o instanceof MacIconSuite) {
				ImageIO.write(((MacIconSuite)o).getImage(), format, out);
				return;
			}
			if (o instanceof byte[]) {
				MacIconSuite icns = new MacIconSuite();
				icns.read(new DataInputStream(new ByteArrayInputStream((byte[])o)));
				ImageIO.write(icns.getImage(), format, out);
				return;
			}
		}
		throw new IOException("I don't know what this is");
	}
	
	private static final List<Integer> ICON_TYPES = Arrays.asList(
		MacIconSuite.icm$,MacIconSuite.icm4,MacIconSuite.icm8,MacIconSuite.im32,MacIconSuite.m8mk,
		MacIconSuite.ics$,MacIconSuite.ics4,MacIconSuite.ics8,MacIconSuite.is32,MacIconSuite.s8mk,
		MacIconSuite.ICN$,MacIconSuite.icl4,MacIconSuite.icl8,MacIconSuite.il32,MacIconSuite.l8mk,
		MacIconSuite.ich$,MacIconSuite.ich4,MacIconSuite.ich8,MacIconSuite.ih32,MacIconSuite.h8mk,
		MacIconSuite.ict$,MacIconSuite.ict4,MacIconSuite.ict8,MacIconSuite.it32,MacIconSuite.t8mk
	);
	
	private static Map<Integer,Object> getIconSuiteData(MacResourceFile rsrc) {
		Map<Integer,byte[]> completeSuites = new HashMap<Integer,byte[]>();
		Map<Integer,MacIconSuite> partialSuites = new HashMap<Integer,MacIconSuite>();
		for (MacResourceType rt : rsrc.getResourceTypes()) {
			if (MacIconSuite.icns == rt.getType()) {
				for (MacResource r : rt.getResources()) {
					completeSuites.put(r.getId(), r.getData());
				}
			} else if (ICON_TYPES.contains(rt.getType())) {
				for (MacResource r : rt.getResources()) {
					MacIconSuite icon = partialSuites.get(r.getId());
					if (icon == null) {
						icon = new MacIconSuite();
						partialSuites.put(r.getId(), icon);
					}
					icon.put(rt.getType(), r.getData());
				}
			}
		}
		Map<Integer,Object> allSuites = new HashMap<Integer,Object>();
		allSuites.putAll(partialSuites);
		allSuites.putAll(completeSuites);
		return allSuites;
	}
	
	private static MacResourceFile getResourceFile(File file) {
		if (file.isDirectory()) {
			// Custom icons for folders are kept in an Icon\r file.
			File icon = new File(file, "Icon\r");
			if (icon.isFile()) return getResourceFile(icon);
			
			// On Windows, \r is invalid in file names and is substituted with \uF00D.
			icon = new File(file, "Icon\uF00D");
			if (icon.isFile()) return getResourceFile(icon);
			
			// No custom icon found.
			return null;
		} else {
			try {
				// Try reading the file directly as a resource file.
				return new MacResourceFile(file);
			} catch (Exception e) {}
			
			try {
				// On Mac OS X, the resource fork can be accessed through ..namedfork.
				File rsrc = new File(new File(file, "..namedfork"), "rsrc");
				return new MacResourceFile(rsrc);
			} catch (Exception e) {}
			
			try {
				// Older version of the above.
				File rsrc = new File(file, "rsrc");
				return new MacResourceFile(rsrc);
			} catch (Exception e) {}
			
			try {
				// SheepShaver / Basilisk II
				File rsrc = new File(new File(file.getParentFile(), ".rsrc"), file.getName());
				return new MacResourceFile(rsrc);
			} catch (Exception e) {}
			
			try {
				// PC/File Exchange
				File rsrc = new File(new File(file.getParentFile(), "RESOURCE.FRK"), file.getName());
				return new MacResourceFile(rsrc);
			} catch (Exception e) {}
			
			try {
				// AppleDouble (Mac OS X)
				File adf = new File(file.getParentFile(), "._" + file.getName());
				AppleSingleFile adh = new AppleSingleFile(adf);
				return new MacResourceFile(adh.getPartData(AppleSingleFile.PART_RESOURCE_FORK));
			} catch (Exception e) {}
			
			try {
				// AppleDouble (A/UX)
				File adf = new File(file.getParentFile(), "%" + file.getName());
				AppleSingleFile adh = new AppleSingleFile(adf);
				return new MacResourceFile(adh.getPartData(AppleSingleFile.PART_RESOURCE_FORK));
			} catch (Exception e) {}
			
			try {
				// AppleDouble (ProDOS)
				File adf = new File(file.getParentFile(), "R." + file.getName());
				AppleSingleFile adh = new AppleSingleFile(adf);
				return new MacResourceFile(adh.getPartData(AppleSingleFile.PART_RESOURCE_FORK));
			} catch (Exception e) {}
			
			// No resource fork found.
			return null;
		}
	}
}
