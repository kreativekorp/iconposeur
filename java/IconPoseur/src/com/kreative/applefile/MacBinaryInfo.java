package com.kreative.applefile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

public class MacBinaryInfo {
	public static void main(String[] args) {
		boolean parseOpts = true;
		boolean roundTripTest = false;
		for (String arg : args) {
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("--roundtrip")) {
					roundTripTest = true;
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				System.out.println(arg);
				File file = new File(arg);
				process(file, roundTripTest);
			}
		}
	}
	
	private static void process(File src, boolean roundTripTest) {
		try {
			AppleFile af = MacBinaryUtility.read(src);
			System.out.println("\t" + af.getFileSystem());
			byte[] nameData = af.getPartData(AppleFilePart.TYPE_FILE_NAME);
			System.out.println("\tName\t" + new String(nameData, "MacRoman"));
			byte[] finfData = af.getPartData(AppleFilePart.TYPE_FINDER_INFO);
			System.out.println("\tType\t" + new String(finfData, 0, 4, "MacRoman"));
			System.out.println("\tCrea\t" + new String(finfData, 4, 4, "MacRoman"));
			int attr = ((finfData[8] & 0xFF) << 8) | (finfData[9] & 0xFF);
			System.out.println("\tAttr\t" + Integer.toHexString(attr));
			int dataLen = len(af.getPartData(AppleFilePart.TYPE_DATA_FORK));
			System.out.println("\tData\t" + dataLen);
			int rsrcLen = len(af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK));
			System.out.println("\tRsrc\t" + rsrcLen);
			
			if (roundTripTest) try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				MacBinaryUtility.write(out, null, af);
				out.close();
				ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
				AppleFile af2 = MacBinaryUtility.read(in2);
				in2.close();
				System.out.println("\t" + af2.getFileSystem());
				for (int i : PARTS) {
					System.out.print("\t" + i + "\t");
					byte[] b1 = af.getPartData(i);
					byte[] b2 = af2.getPartData(i);
					System.out.println(arraysEqual(b1, b2) ? "OK" : "NG");
				}
			} catch (Exception e) {
				System.out.println("\tRT ERROR: " + e);
			}
		} catch (Exception e) {
			System.out.println("\tERROR: " + e);
		}
	}
	
	private static int len(byte[] data) {
		return (data != null) ? data.length : 0;
	}
	
	private static boolean arraysEqual(byte[] a, byte[] b) {
		if (a == null || a.length == 0) return (b == null || b.length == 0);
		if (b == null || b.length == 0) return (a == null || a.length == 0);
		return Arrays.equals(a, b);
	}
	
	private static final int[] PARTS = {
		AppleFilePart.TYPE_FILE_NAME, AppleFilePart.TYPE_FINDER_INFO,
		AppleFilePart.TYPE_TIMESTAMP, AppleFilePart.TYPE_DATA_FORK,
		AppleFilePart.TYPE_RESOURCE_FORK, AppleFilePart.TYPE_COMMENT
	};
}
