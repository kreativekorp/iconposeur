package com.kreative.applefile;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class AppleTurnover {
	public static void main(String[] args) {
		if (args.length == 0) {
			printHelp();
			return;
		}
		Format inputFormat = Format.AUTO;
		Format outputFormat = Format.AUTO;
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
				} else if (arg.equals("-F") && argi < args.length) {
					String s = args[argi++];
					try { inputFormat = Format.forName(s); }
					catch (IllegalArgumentException e) {
						System.err.println("Unknown format: " + s);
					}
				} else if (arg.equals("-f") && argi < args.length) {
					String s = args[argi++];
					try { outputFormat = Format.forName(s); }
					catch (IllegalArgumentException e) {
						System.err.println("Unknown format: " + s);
					}
				} else if (arg.equals("-A")) {
					inputFormat = Format.AUTO;
				} else if (arg.equals("-a")) {
					outputFormat = Format.AUTO;
				} else if (arg.equals("-N")) {
					inputFormat = Format.NATIVE;
				} else if (arg.equals("-n")) {
					outputFormat = Format.NATIVE;
				} else if (arg.equals("-D")) {
					inputFormat = Format.DATA_FORK;
				} else if (arg.equals("-d")) {
					outputFormat = Format.DATA_FORK;
				} else if (arg.equals("-R")) {
					inputFormat = Format.RESOURCE_FORK;
				} else if (arg.equals("-r")) {
					outputFormat = Format.RESOURCE_FORK;
				} else if (arg.equals("-B")) {
					inputFormat = Format.BASILISK_II;
				} else if (arg.equals("-b")) {
					outputFormat = Format.BASILISK_II;
				} else if (arg.equals("-C")) {
					inputFormat = Format.PC_EXCHANGE;
				} else if (arg.equals("-c")) {
					outputFormat = Format.PC_EXCHANGE;
				} else if (arg.equals("-X")) {
					inputFormat = Format.APPLEDOUBLE_MACOSX;
				} else if (arg.equals("-x")) {
					outputFormat = Format.APPLEDOUBLE_MACOSX;
				} else if (arg.equals("-U")) {
					inputFormat = Format.APPLEDOUBLE_AUX;
				} else if (arg.equals("-u")) {
					outputFormat = Format.APPLEDOUBLE_AUX;
				} else if (arg.equals("-P")) {
					inputFormat = Format.APPLEDOUBLE_PRODOS;
				} else if (arg.equals("-p")) {
					outputFormat = Format.APPLEDOUBLE_PRODOS;
				} else if (arg.equals("-S")) {
					inputFormat = Format.APPLESINGLE;
				} else if (arg.equals("-s")) {
					outputFormat = Format.APPLESINGLE;
				} else if (arg.equals("-H")) {
					inputFormat = Format.BINHEX;
				} else if (arg.equals("-h")) {
					outputFormat = Format.BINHEX;
				} else if (arg.equals("-M")) {
					inputFormat = Format.MACBINARY;
				} else if (arg.equals("-m")) {
					outputFormat = Format.MACBINARY;
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
					if (arg.equals(arg.toUpperCase())) {
						try { inputFormat = Format.forName(arg); continue; }
						catch (IllegalArgumentException e) {}
					}
					if (arg.equals(arg.toLowerCase())) {
						try { outputFormat = Format.forName(arg); continue; }
						catch (IllegalArgumentException e) {}
					}
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				process(file, inputFormat, outputFormat, dstDir, dstName);
				dstName = null;
			}
		}
	}
	
	private static void printHelp() {
		System.out.println("AppleTurnover - Convert Mac OS Classic files between transfer formats.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  AppleTurnover [ -F <src-fmt> ] [ -f <dst-fmt> ] [ -o <dst-path> ] <src-files>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -F <format>   Specify input format. Or use options below.");
		System.out.println("  -A, --AUTO    Guess input format from parameters.");
		System.out.println("  -N, --NATIVE  Read file natively (Mac OS X only).");
		System.out.println("  -D, --DATA    Data fork in flat file");
		System.out.println("  -R, --RSRC    Resource fork in flat file");
		System.out.println("  -B, --BII     Basilisk II/SheepShaver (resources in .rsrc directory)");
		System.out.println("  -C, --OS9     PC/File Exchange (resources in RESOURCE.FRK directory)");
		System.out.println("  -X, --OSX     AppleDouble (Mac OS X) (AppleDouble header in ._ file)");
		System.out.println("  -U, --AUX     AppleDouble (A/UX) (AppleDouble header in % file)");
		System.out.println("  -P, --PDOS    AppleDouble (ProDOS) (AppleDouble header in R. file)");
		System.out.println("  -S, --AS      AppleSingle");
		System.out.println("  -H, --HQX     BinHex 4.0");
		System.out.println("  -M, --MBIN    MacBinary, MacBinary II, MacBinary III");
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
	
	private static boolean match(Format inputFormat, Format format, String filename, String ext) {
		return (
			(inputFormat == Format.AUTO || inputFormat == format) &&
			filename.toLowerCase().endsWith(ext)
		);
	}
	
	private static String getInputName(AppleFile af, File src, Format inputFormat) {
		byte[] name = af.getPartData(AppleFilePart.TYPE_FILE_NAME);
		if (name != null && name.length > 0) {
			try { return new String(name, "MacRoman"); }
			catch (UnsupportedEncodingException e) {}
		}
		String fn = src.getName();
		if (match(inputFormat, Format.DATA_FORK, fn, ".data")) return fn.substring(0, fn.length() - 5);
		if (match(inputFormat, Format.RESOURCE_FORK, fn, ".rsrc")) return fn.substring(0, fn.length() - 5);
		if (match(inputFormat, Format.BINHEX, fn, ".hqx")) return fn.substring(0, fn.length() - 4);
		if (match(inputFormat, Format.MACBINARY, fn, ".bin")) return fn.substring(0, fn.length() - 4);
		return fn;
	}
	
	private static String getOutputName(String inputName, File src, Format outputFormat) {
		switch (outputFormat) {
			case DATA_FORK: return inputName + ".data";
			case RESOURCE_FORK: return inputName + ".rsrc";
			case BINHEX: return inputName + ".hqx";
			case MACBINARY: return inputName + ".bin";
			default:
				if (inputName.equals(src.getName())) return inputName + ".out";
				return inputName;
		}
	}
	
	private static void process(File src, Format inputFormat, Format outputFormat, File dstDir, String dstName) {
		System.out.print(src.getName() + "...");
		try {
			AppleFile af = inputFormat.read(src);
			if (dstDir == null) dstDir = src.getParentFile();
			if (dstName == null) {
				String srcName = getInputName(af, src, inputFormat);
				dstName = getOutputName(srcName, src, outputFormat);
			}
			File dst = new File(dstDir, dstName);
			outputFormat.write(dst, af);
			System.out.println(" OK");
		} catch (Exception e) {
			System.out.println(" ERROR: " + e);
		}
	}
}
