package com.kreative.applefile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppleFile {
	public static final int MAGIC_APPLESINGLE = 0x00051600;
	public static final int MAGIC_APPLEDOUBLE = 0x00051607;
	
	public static final int VERSION_1 = 0x00010000;
	public static final int VERSION_2 = 0x00020000;
	
	private boolean isAppleDouble;
	private boolean isVersion2;
	private String fileSystem;
	private final List<AppleFilePart> rwParts = new ArrayList<AppleFilePart>();
	private final List<AppleFilePart> roParts = Collections.unmodifiableList(rwParts);
	
	public AppleFile(boolean isAppleDouble, boolean isVersion2, String fileSystem) {
		this.isAppleDouble = isAppleDouble;
		this.isVersion2 = isVersion2;
		this.setFileSystem(fileSystem);
	}
	
	public AppleFile(File file) throws IOException { read(readFile(file)); }
	public AppleFile(InputStream in) throws IOException { read(readInputStream(in)); }
	public AppleFile(byte[] data) throws IOException { read(data); }
	
	public AppleFile(File header, File data) throws IOException {
		read(readFile(header));
		setPartData(AppleFilePart.TYPE_DATA_FORK, readFile(data));
	}
	
	public AppleFile(InputStream header, InputStream data) throws IOException {
		read(readInputStream(header));
		setPartData(AppleFilePart.TYPE_DATA_FORK, readInputStream(data));
	}
	
	public AppleFile(byte[] header, byte[] data) throws IOException {
		read(header);
		setPartData(AppleFilePart.TYPE_DATA_FORK, data);
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
			case MAGIC_APPLESINGLE: this.isAppleDouble = false; break;
			case MAGIC_APPLEDOUBLE: this.isAppleDouble = true; break;
			default: throw new IOException("unknown magic number: 0x" + Integer.toHexString(magic));
		}
		// Read version number.
		int version = in.readInt();
		switch (version) {
			case VERSION_1: this.isVersion2 = false; break;
			case VERSION_2: this.isVersion2 = true; break;
			default: throw new IOException("unknown version number: 0x" + Integer.toHexString(version));
		}
		// Read file system identifier.
		byte[] fileSystem = new byte[16];
		in.readFully(fileSystem);
		if (isZeroes(fileSystem)) this.fileSystem = null;
		else if (isASCII(fileSystem)) this.fileSystem = new String(fileSystem, "US-ASCII").trim();
		else throw new IOException("unknown file system");
		// Read part headers.
		int count = in.readUnsignedShort();
		for (int i = 0; i < count; i++) {
			AppleFilePart part = new AppleFilePart();
			part.readHead(in);
			rwParts.add(part);
		}
		// Read part data.
		for (AppleFilePart part : rwParts) {
			part.readBody(in);
		}
	}
	
	public boolean isAppleSingle() { return !isAppleDouble; }
	public boolean isAppleDouble() { return isAppleDouble; }
	public void setIsAppleDouble(boolean isAppleDouble) { this.isAppleDouble = isAppleDouble; }
	
	public boolean isVersion1() { return !isVersion2; }
	public boolean isVersion2() { return isVersion2; }
	public void setIsVersion2(boolean isVersion2) { this.isVersion2 = isVersion2; }
	
	public String getFileSystem() { return fileSystem; }
	
	public void setFileSystem(String fileSystem) {
		if (fileSystem == null) {
			this.fileSystem = null;
		} else {
			fileSystem = fileSystem.trim();
			try {
				byte[] d = fileSystem.getBytes("US-ASCII");
				if (isZeroes(d)) this.fileSystem = null;
				else if (isASCII(d)) this.fileSystem = fileSystem;
				else throw new IllegalArgumentException("unknown file system");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	public List<AppleFilePart> getParts() {
		return roParts;
	}
	
	public AppleFilePart getPart(int type) {
		for (AppleFilePart part : roParts) {
			if (part.getType() == type) {
				return part;
			}
		}
		return null;
	}
	
	public byte[] getPartData(int type) {
		for (AppleFilePart part : roParts) {
			if (part.getType() == type) {
				return part.getData();
			}
		}
		return null;
	}
	
	public boolean addPart(AppleFilePart newPart) {
		if (newPart == null) return false;
		for (AppleFilePart oldPart : rwParts) {
			if (oldPart.getType() == newPart.getType()) {
				return false;
			}
		}
		return rwParts.add(newPart);
	}
	
	public AppleFilePart removePart(int type) {
		for (AppleFilePart part : rwParts) {
			if (part.getType() == type) {
				if (rwParts.remove(part)) {
					return part;
				}
			}
		}
		return null;
	}
	
	public boolean removePart(AppleFilePart part) {
		return rwParts.remove(part);
	}
	
	public AppleFilePart setPartData(int type, byte[] data, int off, int len) {
		for (AppleFilePart part : rwParts) {
			if (part.getType() == type) {
				part.setData(data, off, len);
				return part;
			}
		}
		AppleFilePart part = new AppleFilePart(type, data, off, len);
		rwParts.add(part);
		return part;
	}
	
	public AppleFilePart setPartData(int type, byte[] data) {
		return setPartData(type, data, 0, data.length);
	}
	
	public AppleFilePart setPartData(int type, File file) throws IOException {
		return setPartData(type, readFile(file));
	}
	
	public byte[] write(boolean isAppleDouble) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out, isAppleDouble);
		return out.toByteArray();
	}
	
	public byte[][] write(byte[][] headerAndData) throws IOException {
		if (headerAndData == null) headerAndData = new byte[2][];
		ByteArrayOutputStream out0 = new ByteArrayOutputStream();
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		write(out0, out1);
		headerAndData[0] = out0.toByteArray();
		headerAndData[1] = out1.toByteArray();
		return headerAndData;
	}
	
	public void write(File file, boolean isAppleDouble) throws IOException {
		OutputStream out = new FileOutputStream(file);
		write(out, isAppleDouble);
		out.close();
	}
	
	public void write(File header, File data) throws IOException {
		OutputStream out0 = new FileOutputStream(header);
		OutputStream out1 = new FileOutputStream(data);
		write(out0, out1);
		out0.close();
		out1.close();
	}
	
	public void write(OutputStream out, boolean isAppleDouble) throws IOException {
		writeImpl(new DataOutputStream(out), isAppleDouble);
	}
	
	public void write(OutputStream header, OutputStream data) throws IOException {
		writeImpl(new DataOutputStream(header), true);
		for (AppleFilePart part : roParts) {
			if (part.getType() == AppleFilePart.TYPE_DATA_FORK) {
				data.write(part.getData());
			}
		}
	}
	
	private void writeImpl(DataOutputStream out, boolean isAppleDouble) throws IOException {
		out.writeInt(isAppleDouble ? MAGIC_APPLEDOUBLE : MAGIC_APPLESINGLE);
		out.writeInt(isVersion2 ? VERSION_2 : VERSION_1);
		// Write file system identifier.
		if (fileSystem == null) {
			out.write(new byte[16]);
		} else {
			byte[] d = fileSystem.getBytes("US-ASCII");
			int len = (d.length < 16) ? d.length : 16;
			out.write(d, 0, len);
			while (len < 16) {
				out.write(32);
				len++;
			}
		}
		// Write part count.
		int count = 0;
		for (AppleFilePart part : roParts) {
			if (isAppleDouble && (part.getType() == AppleFilePart.TYPE_DATA_FORK)) continue;
			count++;
		}
		out.writeShort(count);
		// Write part headers.
		int ptr = count * 12 + 26;
		for (AppleFilePart part : roParts) {
			if (isAppleDouble && (part.getType() == AppleFilePart.TYPE_DATA_FORK)) continue;
			ptr = part.writeHead(out, ptr);
		}
		// Write part data.
		for (AppleFilePart part : roParts) {
			if (isAppleDouble && (part.getType() == AppleFilePart.TYPE_DATA_FORK)) continue;
			out.write(part.getData());
		}
	}
	
	private static boolean isASCII(byte[] bytes) {
		for (byte b : bytes) if (b < 0x20 || b >= 0x7F) return false;
		return true;
	}
	
	private static boolean isZeroes(byte[] bytes) {
		for (byte b : bytes) if (b != 0) return false;
		return true;
	}
	
	private static byte[] readFile(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		byte[] data = readInputStream(in);
		in.close();
		return data;
	}
	
	private static byte[] readInputStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[65536]; int read;
		while ((read = in.read(buf)) >= 0) out.write(buf, 0, read);
		out.close();
		return out.toByteArray();
	}
}
