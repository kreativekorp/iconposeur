package com.kreative.iconposeur;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class IcnsToc {
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
			File file = new File(arg);
			process(file);
		}
	}
	
	private static void process(File src) {
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
				System.out.println(e.getValue().length);
			}
		} catch (Exception e) {
			System.out.println("\tERROR: " + e);
		}
	}
}
