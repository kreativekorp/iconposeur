package com.kreative.applefile;

import java.io.File;

public class FinderDotWat {
	public static void main(String[] args) {
		boolean parseOpts = true;
		for (String arg : args) {
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				System.out.println(arg);
				File file = new File(arg);
				process(file);
			}
		}
	}
	
	private static void process(File src) {
		try {
			int i = 0;
			FinderDotDat dat = new FinderDotDat(src);
			for (FinderDotDat.Entry e : dat.entryList()) {
				System.out.println("\t" + (i++));
				System.out.println("\tLFN\t" + e.getLongName());
				byte finfData[] = new byte[32];
				e.getData(finfData, 0, 32, 32);
				System.out.println("\tType\t" + new String(finfData, 0, 4, "MacRoman"));
				System.out.println("\tCrea\t" + new String(finfData, 4, 4, "MacRoman"));
				int attr = ((finfData[8] & 0xFF) << 8) | (finfData[9] & 0xFF);
				System.out.println("\tAttr\t" + Integer.toHexString(attr));
				System.out.println("\tSer#\t" + Integer.toHexString(e.getSerialNumber()));
				System.out.println("\tSFN\t" + e.getShortName());
				byte wtfData[] = new byte[1];
				e.getData(wtfData, 0, 0x5B, 1);
				System.out.println("\tWTF\t" + Integer.toHexString(wtfData[0] & 0xFF));
			}
		} catch (Exception e1) {
			System.out.println("\tERROR: " + e1);
		}
	}
}
