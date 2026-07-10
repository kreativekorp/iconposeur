package com.kreative.iconposeur;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MacBinaryUtility {
	public static boolean isMacBinary(byte[] header) {
		return (
			// Version number is zero
			header[0] == 0 &&
			// Name length is 1-63
			header[1] > 0 && header[1] < 64 &&
			// Bytes 74 and 82 must be zero
			header[74] == 0 && header[82] == 0 &&
			// Data fork length is non-negative
			header[83] >= 0 &&
			// Resource fork length is non-negative and under 16MB
			header[87] == 0
		);
	}
	
	public static boolean isMacBinaryII(byte[] header) {
		return (
			isMacBinary(header) &&
			// Creator version number is 129 (MBII) or 130 (MBIII)
			(header[122] == (byte)129 || header[122] == (byte)130) &&
			// Minimum version number is 129 (MBII) or 130 (MBIII)
			(header[123] == (byte)129 || header[123] == (byte)130) &&
			// CRC matches
			getInt16(header, 124) == crc(header, 0, 124, 2)
		);
	}
	
	public static boolean isMacBinaryIII(byte[] header) {
		return (
			isMacBinaryII(header) &&
			header[102] == 'm' &&
			header[103] == 'B' &&
			header[104] == 'I' &&
			header[105] == 'N'
		);
	}
	
	public static AppleFile read(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		AppleFile af = read(in);
		in.close();
		return af;
	}
	
	public static AppleFile read(InputStream in) throws IOException {
		byte[] header = new byte[128];
		if (in.read(header) < 128) throw new EOFException();
		if (!isMacBinary(header)) throw new IOException("not a MacBinary file");
		boolean isVersion2 = isMacBinaryII(header);
		boolean isVersion3 = isMacBinaryIII(header);
		AppleFile af = new AppleFile(false, true, (
			isVersion3 ? "MacBinary III" :
			isVersion2 ? "MacBinary II" :
			"MacBinary"
		));
		// File name
		af.setPartData(AppleFilePart.TYPE_FILE_NAME, header, 2, header[1]);
		// Finder info
		byte[] finf = new byte[32];
		for (int di = 0, si = 65; di < 16; si++, di++) finf[di] = header[si];
		if (isVersion2) finf[9] = header[101];
		if (isVersion3) finf[24] = header[106];
		if (isVersion3) finf[25] = header[107];
		af.setPartData(AppleFilePart.TYPE_FINDER_INFO, finf);
		// Creation and modification date
		byte[] date = new byte[16];
		setAppleFileDate(date, 0, getHFSDate(header, 91));
		setAppleFileDate(date, 4, getHFSDate(header, 95));
		date[8] = -128;
		date[12] = -128;
		af.setPartData(AppleFilePart.TYPE_TIMESTAMP, date);
		// Data fork
		int dataLen = getInt32(header, 83);
		if (dataLen > 0) {
			byte[] data = new byte[dataLen];
			if (in.read(data) < dataLen) throw new EOFException();
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, data);
			int extent = dataLen & 0x7F;
			if (extent > 0) in.read(new byte[128 - extent]);
		}
		// Resource fork
		int rsrcLen = getInt32(header, 87);
		if (rsrcLen > 0) {
			byte[] rsrc = new byte[rsrcLen];
			if (in.read(rsrc) < rsrcLen) throw new EOFException();
			af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
			int extent = rsrcLen & 0x7F;
			if (extent > 0) in.read(new byte[128 - extent]);
		}
		// Finder comment
		int commLen = getInt16(header, 99);
		if (commLen > 0) {
			byte[] comm = new byte[commLen];
			if (in.read(comm) < commLen) throw new EOFException();
			af.setPartData(AppleFilePart.TYPE_COMMENT, comm);
			int extent = commLen & 0x7F;
			if (extent > 0) in.read(new byte[128 - extent]);
		}
		return af;
	}
	
	public static void write(File mbf, String filename, AppleFile af) throws IOException {
		FileOutputStream out = new FileOutputStream(mbf);
		write(out, filename, af);
		out.close();
	}
	
	public static void write(OutputStream out, String filename, AppleFile file) throws IOException {
		byte[] header = new byte[128];
		// File name
		byte[] nameData = file.getPartData(AppleFilePart.TYPE_FILE_NAME);
		if (nameData == null) nameData = filename.getBytes("MacRoman");
		int nameLen = (nameData.length < 63) ? nameData.length : 63;
		header[1] = (byte)nameLen;
		for (int di = 2, si = 0; si < nameLen; si++, di++) header[di] = nameData[si];
		// Finder info
		byte[] finfData = file.getPartData(AppleFilePart.TYPE_FINDER_INFO);
		if (finfData != null) {
			for (int di = 65, si = 0; si < 16; si++, di++) header[di] = finfData[si];
			header[74] = 0;
			header[101] = finfData[9];
			header[106] = finfData[24];
			header[107] = finfData[25];
		}
		// Data and resource fork length
		byte[] dataData = file.getPartData(AppleFilePart.TYPE_DATA_FORK);
		if (dataData != null) setInt32(header, 83, dataData.length);
		byte[] rsrcData = file.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
		if (rsrcData != null) setInt32(header, 87, rsrcData.length);
		// Creation and modification date
		byte[] dateData = file.getPartData(AppleFilePart.TYPE_TIMESTAMP);
		if (dateData != null) {
			setHFSDate(header, 91, getAppleFileDate(dateData, 0));
			setHFSDate(header, 95, getAppleFileDate(dateData, 4));
		}
		// Finder comment
		byte[] commData = file.getPartData(AppleFilePart.TYPE_COMMENT);
		if (commData != null) setInt16(header, 99, commData.length);
		// MacBinary III identifier
		header[102] = 'm';
		header[103] = 'B';
		header[104] = 'I';
		header[105] = 'N';
		header[122] = (byte)130;
		header[123] = (byte)129;
		setInt16(header, 124, crc(header, 0, 124, 2));
		// Write
		out.write(header);
		if (dataData != null) {
			out.write(dataData);
			int extent = dataData.length & 0x7F;
			if (extent > 0) out.write(new byte[128 - extent]);
		}
		if (rsrcData != null) {
			out.write(rsrcData);
			int extent = rsrcData.length & 0x7F;
			if (extent > 0) out.write(new byte[128 - extent]);
		}
		if (commData != null) {
			out.write(commData);
			int extent = commData.length & 0x7F;
			if (extent > 0) out.write(new byte[128 - extent]);
		}
	}
	
	public static int crc(byte[] data, int offset, int length, int padding) {
		int crc = 0;
		while (length > 0) {
			int b = data[offset];
			for (int m = 0x80; m != 0; m >>= 1) {
				crc <<= 1;
				if ((b & m) != 0) crc++;
				if (crc > 0xFFFF) crc ^= 0x11021;
			}
			offset++;
			length--;
		}
		while (padding > 0) {
			for (int i = 0; i < 8; i++) {
				crc <<= 1;
				if (crc > 0xFFFF) crc ^= 0x11021;
			}
			padding--;
		}
		return crc;
	}
	
	private static int getInt16(byte[] data, int offset) {
		int v = (data[offset] & 0xFF) << 8;
		v |= (data[++offset] & 0xFF);
		return v;
	}
	
	private static void setInt16(byte[] data, int offset, int v) {
		data[offset] = (byte)(v >> 8);
		data[++offset] = (byte)v;
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
	
	private static int getHFSDate(byte[] data, int offset) {
		return getInt32(data, offset) - 2082844800; // 1904-01-01 00:00:00 UTC
	}
	
	private static void setHFSDate(byte[] data, int offset, int date) {
		setInt32(data, offset, date + 2082844800); // 1904-01-01 00:00:00 UTC
	}
	
	private static int getAppleFileDate(byte[] data, int offset) {
		return getInt32(data, offset) + 946684800; // 2000-01-01 00:00:00 UTC
	}
	
	private static void setAppleFileDate(byte[] data, int offset, int date) {
		setInt32(data, offset, date - 946684800); // 2000-01-01 00:00:00 UTC
	}
}
