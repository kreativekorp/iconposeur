package com.kreative.iconposeur;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppleSingleFile {
	private static final int MAGIC_APPLESINGLE = 0x00051600;
	private static final int MAGIC_APPLEDOUBLE = 0x00051607;
	
	private static final int VERSION_1 = 0x00010000;
	private static final int VERSION_2 = 0x00020000;
	
	public static final int PART_DATA_FORK = 1;
	public static final int PART_RESOURCE_FORK = 2;
	public static final int PART_FILE_NAME = 3;
	public static final int PART_COMMENT = 4;
	public static final int PART_ICON = 5;
	public static final int PART_CICN = 6;
	public static final int PART_FILE_INFO = 7;
	public static final int PART_TIMESTAMP = 8;
	public static final int PART_FINDER_INFO = 9;
	public static final int PART_MACINTOSH_INFO = 10;
	public static final int PART_PRODOS_INFO = 11;
	public static final int PART_MSDOS_INFO = 12;
	public static final int PART_AFP_SHORT_NAME = 13;
	public static final int PART_AFP_FILE_INFO = 14;
	public static final int PART_AFP_DIRECTORY_ID = 15;
	
	private boolean isAppleDouble;
	private boolean hasFileSystem;
	private String fileSystem;
	private AppleSinglePart[] parts;
	
	public AppleSingleFile(File file) throws IOException { read(file); }
	public AppleSingleFile(InputStream in) throws IOException { read(in); }
	public AppleSingleFile(byte[] data) throws IOException { read(data); }
	
	private void read(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		read(in);
		in.close();
	}
	
	private void read(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[65536]; int read;
		while ((read = in.read(buf)) >= 0) out.write(buf, 0, read);
		out.close();
		read(out.toByteArray());
	}
	
	private void read(byte[] data) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(bin);
		readImpl(in);
		in.close();
		bin.close();
	}
	
	private void readImpl(DataInputStream in) throws IOException {
		// Read magic number.
		int magic = in.readInt();
		switch (magic) {
			case MAGIC_APPLESINGLE:
				this.isAppleDouble = false;
				break;
			case MAGIC_APPLEDOUBLE:
				this.isAppleDouble = true;
				break;
			default:
				throw new IOException("unknown magic number: 0x" + Integer.toHexString(magic));
		}
		// Read version number.
		int version = in.readInt();
		switch (version) {
			case VERSION_1:
				this.hasFileSystem = true;
				break;
			case VERSION_2:
				this.hasFileSystem = false;
				break;
			default:
				throw new IOException("unknown version number: 0x" + Integer.toHexString(version));
		}
		// Read file system identifier.
		byte[] fileSystem = new byte[16];
		in.readFully(fileSystem);
		if (this.hasFileSystem) {
			if (isASCII(fileSystem)) this.fileSystem = new String(fileSystem, "US-ASCII").trim();
			else throw new IOException("unknown file system");
		} else {
			if (isZeroes(fileSystem)) this.fileSystem = null;
			else throw new IOException("unknown file system");
		}
		// Read part headers.
		int count = in.readUnsignedShort();
		this.parts = new AppleSinglePart[count];
		for (int i = 0; i < count; i++) {
			this.parts[i] = new AppleSinglePart();
			this.parts[i].readHead(in);
		}
		// Read part data.
		for (AppleSinglePart part : this.parts) {
			part.readBody(in);
		}
	}
	
	public boolean isAppleSingle() { return !isAppleDouble; }
	public boolean isAppleDouble() { return isAppleDouble; }
	public String getFileSystem() { return fileSystem; }
	
	public List<AppleSinglePart> getParts() {
		return Collections.unmodifiableList(Arrays.asList(parts));
	}
	
	public AppleSinglePart getPart(int type) {
		for (AppleSinglePart part : parts) {
			if (part.getType() == type) {
				return part;
			}
		}
		return null;
	}
	
	public byte[] getPartData(int type) {
		for (AppleSinglePart part : parts) {
			if (part.getType() == type) {
				return part.getData();
			}
		}
		return null;
	}
	
	private static boolean isASCII(byte[] bytes) {
		for (byte b : bytes) if (b < 0x20 || b >= 0x7F) return false;
		return true;
	}
	
	private static boolean isZeroes(byte[] bytes) {
		for (byte b : bytes) if (b != 0) return false;
		return true;
	}
}
