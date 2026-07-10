package com.kreative.iconposeur;

import java.io.File;

public class AppleFileToc {
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
			File file = new File(arg);
			process(file);
		}
	}
	
	private static void process(File src) {
		try {
			AppleFile af = new AppleFile(src);
			System.out.println(af.isAppleDouble() ? "\tAppleDouble" : "\tAppleSingle");
			System.out.println(af.isVersion2() ? "\tVersion 2" : "\tVersion 1");
			if (af.getFileSystem() != null) System.out.println("\t" + af.getFileSystem());
			for (AppleFilePart part : af.getParts()) {
				System.out.println("\t" + part.getType() + "\t" + part.getData().length);
			}
		} catch (Exception e) {
			System.out.println("\tERROR: " + e);
		}
	}
}
