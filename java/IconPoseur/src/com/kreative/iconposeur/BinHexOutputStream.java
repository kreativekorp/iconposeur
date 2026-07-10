package com.kreative.iconposeur;

import java.io.IOException;
import java.io.OutputStream;

public class BinHexOutputStream extends OutputStream {
	private final StringBuffer sb;
	private final OutputStream out;
	private final String lineEnding;
	
	public BinHexOutputStream(StringBuffer sb) {
		this(sb, "\r\n");
	}
	
	public BinHexOutputStream(OutputStream out) throws IOException {
		this(out, "\r\n");
	}
	
	public BinHexOutputStream(StringBuffer sb, String lineEnding) {
		this.sb = sb;
		this.out = null;
		this.lineEnding = lineEnding;
		sb.append(hqxh);
		sb.append(lineEnding);
		sb.append(':');
	}
	
	public BinHexOutputStream(OutputStream out, String lineEnding) throws IOException {
		this.sb = null;
		this.out = out;
		this.lineEnding = lineEnding;
		out.write(hqxh.getBytes("US-ASCII"));
		out.write(lineEnding.getBytes("US-ASCII"));
		out.write(':');
	}
	
	private int crc = 0;
	private int rleByte = 0;
	private int rleCount = 0;
	private int b64Word = 0;
	private int b64Count = 0;
	private int lineLen = 1;
	
	@Override
	public void write(int b) throws IOException {
		writeRLEByte(b);
		for (int m = 0x80; m != 0; m >>= 1) {
			crc <<= 1;
			if ((b & m) != 0) crc++;
			if (crc > 0xFFFF) crc ^= 0x11021;
		}
	}
	
	public void writeCRC() throws IOException {
		for (int i = 0; i < 16; i++) {
			crc <<= 1;
			if (crc > 0xFFFF) crc ^= 0x11021;
		}
		writeRLEByte(crc >> 8);
		writeRLEByte(crc);
		crc = 0;
	}
	
	private void writeRLEByte(int b) throws IOException {
		b &= 0xFF;
		if (rleByte == b && rleCount < 0xFF) {
			rleCount++;
		} else {
			writeRLERun();
			rleByte = b;
			rleCount = 1;
		}
	}
	
	private void writeRLERun() throws IOException {
		if (rleCount > 2) {
			writeB64Byte(rleByte);
			if (rleByte == 0x90) writeB64Byte(0);
			writeB64Byte(0x90);
			writeB64Byte(rleCount);
		} else {
			while (rleCount > 0) {
				writeB64Byte(rleByte);
				if (rleByte == 0x90) writeB64Byte(0);
				rleCount--;
			}
		}
	}
	
	private void writeB64Byte(int b) throws IOException {
		b64Word <<= 8;
		b64Word |= b;
		b64Count++;
		if (b64Count >= 3) {
			writeB64Word();
			b64Word = 0;
			b64Count = 0;
		}
	}
	
	private void writeB64Word() throws IOException {
		for (int m = 18, i = 0; i <= b64Count; i++, m -= 6) {
			if (lineLen >= 64) {
				if (sb != null) sb.append(lineEnding);
				if (out != null) out.write(lineEnding.getBytes("US-ASCII"));
				lineLen = 0;
			}
			char c = hqxe[(b64Word >> m) & 0x3F];
			if (sb != null) sb.append(c);
			if (out != null) out.write(c);
			lineLen++;
		}
	}
	
	public void writeAppleFile(String filename, AppleFile file) throws IOException {
		// Write file name.
		byte[] nameData = file.getPartData(AppleFilePart.TYPE_FILE_NAME);
		if (nameData == null) nameData = filename.getBytes("MacRoman");
		int nameLen = (nameData.length < 255) ? nameData.length : 255;
		write(nameLen);
		write(nameData, 0, nameLen);
		write(0);
		// Write Finder info.
		byte[] finfData = file.getPartData(AppleFilePart.TYPE_FINDER_INFO);
		if (finfData == null) write(new byte[10]);
		else write(finfData, 0, 10);
		// Write data fork length.
		byte[] dataData = file.getPartData(AppleFilePart.TYPE_DATA_FORK);
		int dataLen = (dataData != null) ? dataData.length : 0;
		write(dataLen >> 24);
		write(dataLen >> 16);
		write(dataLen >> 8);
		write(dataLen);
		// Write resource fork length.
		byte[] rsrcData = file.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
		int rsrcLen = (rsrcData != null) ? rsrcData.length : 0;
		write(rsrcLen >> 24);
		write(rsrcLen >> 16);
		write(rsrcLen >> 8);
		write(rsrcLen);
		writeCRC();
		// Write data fork.
		if (dataLen > 0) write(dataData);
		writeCRC();
		// Write resource fork.
		if (rsrcLen > 0) write(rsrcData);
		writeCRC();
	}
	
	@Override
	public void flush() throws IOException {
		if (out != null) out.flush();
	}
	
	@Override
	public void close() throws IOException {
		writeRLERun();
		rleByte = 0;
		rleCount = 0;
		if (b64Count > 0) {
			for (int i = b64Count; i < 3; i++) b64Word <<= 8;
			writeB64Word();
		}
		b64Word = 0;
		b64Count = 0;
		if (sb != null) {
			sb.append(':');
			sb.append(lineEnding);
		}
		if (out != null) {
			out.write(':');
			out.write(lineEnding.getBytes("US-ASCII"));
			out.close();
		}
		lineLen = 0;
	}
	
	private static final String hqxh = "(This file must be converted with BinHex 4.0)";
	
	private static final char[] hqxe = {
		'!','"','#','$','%','&','\'','(',')','*','+',',','-','0','1','2',
		'3','4','5','6','8','9','@','A','B','C','D','E','F','G','H','I',
		'J','K','L','M','N','P','Q','R','S','T','U','V','X','Y','Z','[',
		'`','a','b','c','d','e','f','h','i','j','k','l','m','p','q','r',
	};
}
