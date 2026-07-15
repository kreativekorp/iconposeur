package com.kreative.iconposeur;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import com.kreative.applefile.AppleFile;
import com.kreative.applefile.AppleFilePart;
import com.kreative.applefile.Format;
import com.kreative.applefile.MacResource;
import com.kreative.applefile.MacResourceFile;

public class IcnsToClassic {
	public static void main(String[] args) {
		if (args.length == 0) {
			printHelp();
			return;
		}
		Format format = Format.AUTO;
		File dstDir = null;
		String dstName = null;
		boolean parseOpts = true;
		int argi = 0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("--help")) {
					printHelp();
				} else if (arg.equals("-f") && argi < args.length) {
					String s = args[argi++];
					try { format = Format.forName(s); }
					catch (IllegalArgumentException e) {
						System.err.println("Unknown format: " + s);
					}
				} else if (arg.equals("-a")) {
					format = Format.AUTO;
				} else if (arg.equals("-n")) {
					format = Format.NATIVE;
				} else if (arg.equals("-d")) {
					format = Format.DATA_FORK;
				} else if (arg.equals("-r")) {
					format = Format.RESOURCE_FORK;
				} else if (arg.equals("-b")) {
					format = Format.BASILISK_II;
				} else if (arg.equals("-c")) {
					format = Format.PC_EXCHANGE;
				} else if (arg.equals("-x")) {
					format = Format.APPLEDOUBLE_MACOSX;
				} else if (arg.equals("-u")) {
					format = Format.APPLEDOUBLE_AUX;
				} else if (arg.equals("-p")) {
					format = Format.APPLEDOUBLE_PRODOS;
				} else if (arg.equals("-s")) {
					format = Format.APPLESINGLE;
				} else if (arg.equals("-h")) {
					format = Format.BINHEX;
				} else if (arg.equals("-m")) {
					format = Format.MACBINARY;
				} else if (arg.equals("-o") && argi < args.length) {
					File dst = new File(args[argi++]);
					if (dst.isDirectory()) {
						dstDir = dst;
						dstName = null;
					} else {
						dstDir = dst.getParentFile();
						dstName = dst.getName();
					}
				} else {
					try { format = Format.forName(arg); }
					catch (IllegalArgumentException e) {
						System.err.println("Unknown option: " + arg);
					}
				}
			} else {
				File file = new File(arg);
				File dd = (dstDir != null) ? dstDir : file.getParentFile();
				String dn = (dstName != null) ? dstName : deriveName(file.getName(), format);
				process(file, format, dd, dn);
				dstName = null;
			}
		}
	}
	
	private static void printHelp() {
		System.out.println("IcnsToClassic - Convert icns files to Mac OS Classic custom icon files.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  IcnsToClassic [ -f <format> ] [ -o <dst-path> ] <src-files>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -f <format>   Specify output format. Or use options below.");
		System.out.println("  -a, --auto    Guess output format from parameters.");
		System.out.println("  -n, --native  Write file natively (Mac OS X only).");
		System.out.println("  -d, --data    Data fork in flat file");
		System.out.println("  -r, --rsrc    Resource fork in flat file");
		System.out.println("  -b, --bii     Basilisk II/SheepShaver (resources in .rsrc directory)");
		System.out.println("  -c, --os9     PC/File Exchange (resources in RESOURCE.FRK directory)");
		System.out.println("  -x, --osx     AppleDouble (Mac OS X) (AppleDouble header in ._ file)");
		System.out.println("  -u, --aux     AppleDouble (A/UX) (AppleDouble header in % file)");
		System.out.println("  -p, --pdos    AppleDouble (ProDOS) (AppleDouble header in R. file)");
		System.out.println("  -s, --as      AppleSingle");
		System.out.println("  -h, --hqx     BinHex 4.0");
		System.out.println("  -m, --mbin    MacBinary III");
		System.out.println("  -o <path>     Specify destination file or directory.");
		System.out.println("  --            Treat remaining arguments as source files.");
	}
	
	private static String deriveName(String filename, Format format) {
		if (filename.toLowerCase().endsWith(".icns")) {
			filename = filename.substring(0, filename.length() - 5);
			switch (format) {
				case DATA_FORK: return filename + ".data";
				case RESOURCE_FORK: return filename + ".rsrc";
				case BINHEX: return filename + ".hqx";
				case MACBINARY: return filename + ".bin";
				default: return filename;
			}
		} else {
			switch (format) {
				case DATA_FORK: return filename + ".data";
				case RESOURCE_FORK: return filename + ".rsrc";
				case BINHEX: return filename + ".hqx";
				case MACBINARY: return filename + ".bin";
				default: return filename + ".icon";
			}
		}
	}
	
	private static void process(File src, Format format, File dstDir, String dstName) {
		System.out.print(src.getName() + "...");
		try {
			MacIconSuite icns = new MacIconSuite();
			FileInputStream in = new FileInputStream(src);
			icns.read(new DataInputStream(in));
			in.close();
			// Mac OS Classic requires at least an ICN# resource to recognize an icon.
			// Mac OS X does not, so many .icns files do not contain one.
			if (!icns.containsKey(MacIconSuite.ICN$)) {
				icns.putImage(MacIconSuite.ICN$, icns.getImage());
			}
			File dst = new File(dstDir, dstName);
			format.write(dst, toAppleFile(icns));
			System.out.println(" OK");
		} catch (Exception e) {
			System.out.println(" ERROR: " + e);
		}
	}
	
	public static AppleFile toAppleFile(MacIconSuite icns) {
		MacResourceFile res = toResourceFile(icns);
		AppleFile af = new AppleFile(false, true, "Icon Poseur");
		af.setPartData(AppleFilePart.TYPE_FINDER_INFO, new byte[] {
			0, 0, 0, 0, 'i','c','n','C', 4, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		});
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			res.write(out);
			out.flush(); out.close();
			byte[] rsrc = out.toByteArray();
			af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
		} catch (IOException e) {}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			icns.write(new DataOutputStream(out));
			out.flush(); out.close();
			byte[] data = out.toByteArray();
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, data);
		} catch (IOException e) {}
		return af;
	}
	
	public static MacResourceFile toResourceFile(MacIconSuite icns) {
		MacResourceFile res = new MacResourceFile();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			icns.write(new DataOutputStream(out));
			out.flush(); out.close();
			byte[] data = out.toByteArray();
			res.addResource(new MacResource(MacIconSuite.icns, -16455, 0, null, data, 0, data.length));
		} catch (IOException e) {}
		for (Map.Entry<Integer,byte[]> e : icns.entrySet()) {
			byte[] data = e.getValue();
			res.addResource(new MacResource(e.getKey(), -16455, 0, null, data, 0, data.length));
		}
		return res;
	}
}
