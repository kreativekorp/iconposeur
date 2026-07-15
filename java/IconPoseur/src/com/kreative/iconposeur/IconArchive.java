package com.kreative.iconposeur;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.InflaterInputStream;

public class IconArchive {
	private boolean isVersion2;
	private boolean isReadOnly;
	private String copyright;
	private String comment;
	private final List<MacIconSuite> icons = new ArrayList<MacIconSuite>();
	private final Map<String,MacIconSuite> byName = new TreeMap<String,MacIconSuite>();
	
	public IconArchive(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		read(new DataInputStream(in));
		in.close();
	}
	
	public IconArchive(InputStream in) throws IOException {
		read(new DataInputStream(in));
	}
	
	public IconArchive(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		read(new DataInputStream(in));
		in.close();
	}
	
	private void read(DataInput in) throws IOException {
		if (in.readLong() != 0x514253455041434BL) {
			throw new IOException("not an Icon Archiver file");
		}
		in.readShort();
		int version = in.readShort();
		switch (version) {
			case 1: isVersion2 = false; break;
			case 2: isVersion2 = true; break;
			default: throw new IOException("not an Icon Archiver file");
		}
		int count = in.readInt();
		byte[] v1headerData = new byte[48];
		in.readFully(v1headerData);
		
		if (isVersion2) {
			if (in.readInt() != 0x49415542) {
				throw new IOException("not an Icon Archiver file");
			}
			byte[] v2headerData1 = new byte[60];
			in.readFully(v2headerData1);
			isReadOnly = (v2headerData1[57] != 0);
			byte[] copyrightData = new byte[64];
			in.readFully(copyrightData);
			copyright = new String(copyrightData, 1, copyrightData[0] & 0xFF, "MacRoman");
			byte[] commentData = new byte[256];
			in.readFully(commentData);
			comment = new String(commentData, 1, commentData[0] & 0xFF, "MacRoman");
			byte[] v2headerData2 = new byte[0x280];
			in.readFully(v2headerData2);
		} else {
			isReadOnly = false;
			copyright = null;
			comment = null;
		}
		
		int[] offsets = new int[count];
		for (int i = 0; i < count; i++) {
			offsets[i] = in.readInt();
		}
		
		for (int i = 0; i < count; i++) {
			int size = in.readInt();
			byte[] data = new byte[size - 4];
			in.readFully(data);
			ByteArrayInputStream iconIn = new ByteArrayInputStream(data);
			MacIconSuite icns = readIcon(new DataInputStream(iconIn));
			iconIn.close();
			icons.add(icns);
			String name = new String(icns.get(MacIconSuite.name), "MacRoman");
			if (name.length() == 0) name = "Untitled";
			if (byName.containsKey(name)) {
				for (int j = 2; j < Integer.MAX_VALUE; j++) {
					String newName = name + " (" + j + ")";
					if (byName.containsKey(newName)) continue;
					name = newName; break;
				}
			}
			byName.put(name, icns);
		}
	}
	
	private MacIconSuite readIcon(DataInputStream in) throws IOException {
		MacIconSuite icns = new MacIconSuite();
		in.readInt();
		in.readInt();
		int length = in.readUnsignedShort();
		if (isVersion2) {
			int iconTypes = in.readUnsignedShort();
			in.readUnsignedShort();
			int nameLen = in.readUnsignedByte();
			byte[] nameData = new byte[nameLen];
			in.readFully(nameData);
			in.readUnsignedByte();
			icns.put(MacIconSuite.name, nameData);
			InflaterInputStream iin = new InflaterInputStream(in);
			for (int[] type : V2_ORDER) {
				if (((iconTypes >> type[0]) & 1) != 0) {
					byte[] data = new byte[type[1] * type[2] * type[2] / 8];
					// InflaterInputStream.read(byte[]) is USELESS.
					for (int i = 0; i < data.length; i++) {
						int b = iin.read();
						if (b < 0) throw new EOFException();
						data[i] = (byte)b;
					}
					if (type[1] == 32) {
						int[] rgb = new int[type[2] * type[2]];
						for (int si = 0, di = 0; di < rgb.length; di++) {
							rgb[di] = ((data[si++] & 0xFF) << 24);
							rgb[di] |= ((data[si++] & 0xFF) << 16);
							rgb[di] |= ((data[si++] & 0xFF) << 8);
							rgb[di] |= (data[si++] & 0xFF);
						}
						BufferedImage img = new BufferedImage(type[2], type[2], BufferedImage.TYPE_INT_ARGB);
						img.setRGB(0, 0, type[2], type[2], rgb, 0, type[2]);
						icns.putImage(type[3], img);
					} else {
						icns.put(type[3], data);
					}
				}
			}
		} else {
			int[] offsets = new int[V1_ORDER.length];
			for (int i = 0; i < V1_ORDER.length; i++) {
				offsets[i] = in.readUnsignedShort();
			}
			int nameLen = in.readUnsignedByte();
			byte[] nameData = new byte[nameLen];
			in.readFully(nameData);
			icns.put(MacIconSuite.name, nameData);
			byte[] iconData = new byte[length + 256];
			int offset = 0;
			while (offset < length) {
				int n = in.readByte();
				if (n >= 0) {
					for (int i = 1 + n; i > 0; i--) {
						iconData[offset++] = in.readByte();
					}
				} else {
					byte b = in.readByte();
					for (int i = 1 - n; i > 0; i--) {
						iconData[offset++] = b;
					}
				}
			}
			int base = nameLen + 17;
			for (int i = 0; i < V1_ORDER.length; i++) {
				if (offsets[i] >= base) {
					byte[] data = new byte[V1_ORDER[i][0] * V1_ORDER[i][1] * V1_ORDER[i][1] / 8];
					for (int si = offsets[i] - base, di = 0; di < data.length; di++, si++) {
						data[di] = iconData[si];
					}
					icns.put(V1_ORDER[i][2], data);
				}
			}
		}
		return icns;
	}
	
	private static final int[][] V1_ORDER = {
		{ 2, 32, MacIconSuite.ICN$ },
		{ 4, 32, MacIconSuite.icl4 },
		{ 8, 32, MacIconSuite.icl8 },
		{ 2, 16, MacIconSuite.ics$ },
		{ 4, 16, MacIconSuite.ics4 },
		{ 8, 16, MacIconSuite.ics8 },
	};
	
	private static final int[][] V2_ORDER = {
		{  5,  2, 32, MacIconSuite.ICN$ },
		{  6,  4, 32, MacIconSuite.icl4 },
		{  7,  8, 32, MacIconSuite.icl8 },
		{  8, 32, 32, MacIconSuite.il32 },
		{  9,  8, 32, MacIconSuite.l8mk },
		{  0,  2, 16, MacIconSuite.ics$ },
		{  1,  4, 16, MacIconSuite.ics4 },
		{  2,  8, 16, MacIconSuite.ics8 },
		{  3, 32, 16, MacIconSuite.is32 },
		{  4,  8, 16, MacIconSuite.s8mk },
		{ 10,  2, 48, MacIconSuite.ich$ },
		{ 11,  4, 48, MacIconSuite.ich4 },
		{ 12,  8, 48, MacIconSuite.ich8 },
		{ 13, 32, 48, MacIconSuite.ih32 },
		{ 14,  8, 48, MacIconSuite.h8mk },
	};
	
	public boolean isVersion1() { return !isVersion2; }
	public boolean isVersion2() { return isVersion2; }
	public boolean isReadOnly() { return isReadOnly; }
	public String getCopyright() { return copyright; }
	public String getComment() { return comment; }
	public List<MacIconSuite> getIconList() { return Collections.unmodifiableList(icons); }
	public Map<String,MacIconSuite> getIconMap() { return Collections.unmodifiableMap(byName); }
}
