package com.kreative.applefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class FinderDotDat {
	private final File parent;
	private final File finderDotDat;
	private final List<Entry> entries = new ArrayList<Entry>();
	private final Map<String,Entry> byLongName = new HashMap<String,Entry>();
	private final Map<Integer,Entry> bySerialNumber = new HashMap<Integer,Entry>();
	private final Map<String,Entry> byShortName = new HashMap<String,Entry>();
	
	public FinderDotDat(File finderDotDat) throws IOException {
		this.parent = finderDotDat.getParentFile();
		this.finderDotDat = finderDotDat;
		if (finderDotDat.exists()) {
			FileInputStream in = new FileInputStream(finderDotDat);
			for (;;) {
				byte[] data = new byte[0x5C];
				if (in.read(data) < 0x5C) {
					in.close();
					return;
				}
				Entry entry = new Entry(parent, data);
				entries.add(entry);
				byLongName.put(entry.longName, entry);
				bySerialNumber.put(entry.serialNumber, entry);
				byShortName.put(entry.shortName, entry);
			}
		}
	}
	
	public void clear() {
		entries.clear();
	}
	
	public List<Entry> entryList() {
		return Collections.unmodifiableList(entries);
	}
	
	public Entry get(int index) {
		return entries.get(index);
	}
	
	public Entry get(String name, boolean create) throws IOException {
		Entry entry = byLongName.get(name);
		if (entry == null) {
			entry = byShortName.get(name.toUpperCase());
			if (entry == null && create) {
				entry = new Entry(parent, name, bySerialNumber.keySet());
				entries.add(entry);
				byLongName.put(entry.longName, entry);
				bySerialNumber.put(entry.serialNumber, entry);
				byShortName.put(entry.shortName, entry);
			}
		}
		return entry;
	}
	
	public boolean isEmpty() {
		return entries.isEmpty();
	}
	
	public Entry remove(int index) {
		Entry entry = entries.remove(index);
		byLongName.values().remove(entry);
		bySerialNumber.values().remove(entry);
		byShortName.values().remove(entry);
		return entry;
	}
	
	public Entry remove(String name) throws IOException {
		Entry entry = get(name, false);
		entries.remove(entry);
		byLongName.values().remove(entry);
		bySerialNumber.values().remove(entry);
		byShortName.values().remove(entry);
		return entry;
	}
	
	public int size() {
		return entries.size();
	}
	
	public void write(File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		for (Entry entry : entries) out.write(entry.data);
		out.flush(); out.close();
	}
	
	public void write() throws IOException {
		write(finderDotDat);
	}
	
	public static final class Entry {
		private final File parent;
		private final byte[] data;
		private final String longName;
		private final int serialNumber;
		private final String shortName;
		
		private Entry(File parent, byte[] data) throws IOException {
			this.parent = parent;
			this.data = data;
			this.longName = new String(data, 1, data[0], "MacRoman");
			this.serialNumber = getInt32(data, 0x4C);
			String base = getSpacePaddedString(data, 0x50, 8);
			String ext = getSpacePaddedString(data, 0x58, 3);
			this.shortName = (ext.length() > 0) ? (base + "." + ext) : base;
		}
		
		private Entry(File parent, String name, Set<Integer> serials) throws IOException {
			this.parent = parent;
			this.data = new byte[0x5C];
			this.longName = (name.length() > 31) ? name.substring(0, 31) : name;
			this.serialNumber = createSerialNumber(serials);
			this.shortName = createShortName(parent, name);
			// Set long name
			byte[] nameData = longName.getBytes("MacRoman");
			data[0] = (byte)nameData.length;
			for (int di = 1, si = 0; si < nameData.length; si++, di++) {
				data[di] = nameData[si];
			}
			// Set serial number
			setInt32(data, 0x4C, serialNumber);
			// Set short name
			int o = shortName.indexOf('.');
			String base = ((o > 0) ? shortName.substring(0, o) : shortName);
			String ext = ((o > 0) ? shortName.substring(o + 1) : "");
			setSpacePaddedString(data, 0x50, 8, base);
			setSpacePaddedString(data, 0x58, 3, ext);
			// What is this?
			data[0x5B] = 0x01;
		}
		
		public byte[] getData(byte[] dst, int dstOff, int srcOff, int len) {
			if (dst == null) dst = new byte[dstOff + len];
			while (len > 0) { dst[dstOff++] = data[srcOff++]; len--; }
			return dst;
		}
		
		public String getLongName() { return longName; }
		public int getSerialNumber() { return serialNumber; }
		public String getShortName() { return shortName; }
		
		public AppleFile read() throws IOException {
			AppleFile af = new AppleFile(false, true, "Mac OS 9");
			af.setPartData(AppleFilePart.TYPE_FILE_NAME, data, 1, data[0]);
			af.setPartData(AppleFilePart.TYPE_FINDER_INFO, data, 0x20, 0x20);
			
			byte[] date = new byte[16];
			setAppleFileDate(date, 0, getHFSDate(data, 0x40));
			setAppleFileDate(date, 4, getHFSDate(data, 0x44));
			setAppleFileDate(date, 8, getHFSDate(data, 0x48));
			setAppleFileDate(date, 12, null);
			af.setPartData(AppleFilePart.TYPE_TIMESTAMP, date);
			
			File rsrcDir = new File(parent, "RESOURCE.FRK");
			File rsrcFile = new File(rsrcDir, longName);
			if (rsrcFile.exists()) af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrcFile);
			else {
				rsrcFile = new File(rsrcDir, shortName);
				if (rsrcFile.exists()) af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrcFile);
			}
			
			File dataFile = new File(parent, longName);
			if (dataFile.exists()) af.setPartData(AppleFilePart.TYPE_DATA_FORK, dataFile);
			else {
				dataFile = new File(parent, shortName);
				if (dataFile.exists()) af.setPartData(AppleFilePart.TYPE_DATA_FORK, dataFile);
			}
			return af;
		}
		
		public void write(AppleFile af) throws IOException {
			byte[] finf = af.getPartData(AppleFilePart.TYPE_FINDER_INFO);
			if (finf != null) {
				for (int di = 0x20, si = 0; si < finf.length && si < 0x20; si++, di++) {
					data[di] = finf[si];
				}
			}
			byte[] date = af.getPartData(AppleFilePart.TYPE_TIMESTAMP);
			if (date != null) {
				setHFSDate(data, 0x40, getAppleFileDate(date, 0));
				setHFSDate(data, 0x44, getAppleFileDate(date, 4));
				setHFSDate(data, 0x48, getAppleFileDate(date, 8));
			}
			byte[] rsrcData = af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
			if (rsrcData != null) {
				File rsrcDir = new File(parent, "RESOURCE.FRK");
				if (!rsrcDir.exists()) rsrcDir.mkdir();
				File rsrcFile = new File(rsrcDir, longName);
				if (!rsrcFile.exists()) rsrcFile = new File(rsrcDir, shortName);
				FileOutputStream out = new FileOutputStream(rsrcFile);
				out.write(rsrcData);
				out.close();
			}
			byte[] dataData = af.getPartData(AppleFilePart.TYPE_DATA_FORK);
			if (dataData != null) {
				File dataFile = new File(parent, shortName);
				if (!dataFile.exists()) dataFile = new File(parent, longName);
				FileOutputStream out = new FileOutputStream(dataFile);
				out.write(dataData);
				out.close();
			}
		}
	}
	
	private static final Random random = new Random();
	private static int createSerialNumber(Set<Integer> serials) {
		// I don't know how these are generated so
		// I'll just make up a random serial number.
		int serial;
		do { serial = random.nextInt(0x10000000) | 0x70000000; }
		while (serials != null && serials.contains(serial));
		return serial;
	}
	
	private static int getInt32(byte[] data, int offset) {
		int v = (data[offset] & 0xFF) << 24;
		v |= (data[++offset] & 0xFF) << 16;
		v |= (data[++offset] & 0xFF) << 8;
		v |= (data[++offset] & 0xFF);
		return v;
	}
	
	private static void setInt32(byte[] data, int offset, int v) {
		data[offset] = (byte)(v >> 24);
		data[++offset] = (byte)(v >> 16);
		data[++offset] = (byte)(v >> 8);
		data[++offset] = (byte)v;
	}
	
	private static Integer getHFSDate(byte[] data, int offset) {
		int i = getInt32(data, offset);
		if (i == 0) return null;
		return i - 2082844800; // 1904-01-01 00:00:00 UTC
	}
	
	private static void setHFSDate(byte[] data, int offset, Integer date) {
		if (date == null) setInt32(data, offset, 0);
		else setInt32(data, offset, date + 2082844800); // 1904-01-01 00:00:00 UTC
	}
	
	private static Integer getAppleFileDate(byte[] data, int offset) {
		int i = getInt32(data, offset);
		if (i == Integer.MIN_VALUE) return null;
		return i + 946684800; // 2000-01-01 00:00:00 UTC
	}
	
	private static void setAppleFileDate(byte[] data, int offset, Integer date) {
		if (date == null) setInt32(data, offset, Integer.MIN_VALUE);
		else setInt32(data, offset, date - 946684800); // 2000-01-01 00:00:00 UTC
	}
	
	private static String getSpacePaddedString(byte[] data, int off, int len) {
		while (len > 0 && data[off + len - 1] == ' ') len--;
		try { return new String(data, off, len, "IBM437"); }
		catch (UnsupportedEncodingException e1) {
			try { return new String(data, off, len, "US-ASCII"); }
			catch (UnsupportedEncodingException e2) {
				throw new IllegalStateException(e2);
			}
		}
	}
	
	private static void setSpacePaddedString(byte[] data, int off, int len, String s) {
		byte[] d;
		try { d = s.getBytes("IBM437"); }
		catch (UnsupportedEncodingException e1) {
			try { d = s.getBytes("US-ASCII"); }
			catch (UnsupportedEncodingException e2) {
				throw new IllegalStateException(e2);
			}
		}
		for (int i = 0; i < d.length && i < len; i++) data[off++] = d[i];
		for (int i = d.length; i < len; i++) data[off++] = ' ';
	}
	
	private static String createShortName(File parent, String lfn) throws IOException {
		int o = lfn.lastIndexOf(".");
		String base = stripIllegalChars((o > 0) ? lfn.substring(0, o) : lfn);
		String ext = stripIllegalChars((o > 0) ? lfn.substring(o + 1) : "");
		if (ext.length() > 3) ext = ext.substring(0, 3);
		if (base.length() == 0) base = "_";
		if (base.length() <= 8) {
			String name = (ext.length() > 0) ? (base + "." + ext) : base;
			if (name.equalsIgnoreCase(lfn)) {
				if (parent == null || !new File(parent, name).exists()) return name;
			}
		}
		for (int len = 6, start = 1, end = 10; len > 0; start *= 10, end *= 10, len--) {
			if (base.length() > len) base = base.substring(0, len);
			for (int i = start; i < end; i++) {
				String name = (ext.length() > 0) ? (base+"~"+i+"."+ext) : (base+"~"+i);
				if (parent == null || !new File(parent, name).exists()) return name;
			}
		}
		throw new IOException("cannot find an available short file name");
	}
	
	private static String stripIllegalChars(String s) {
		s = s.replaceAll("\\s", "");
		s = s.replaceAll("[^!#$%&'()0-9@A-Z^_`a-z{}~-]", "_");
		return s.toUpperCase();
	}
}
