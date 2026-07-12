package com.kreative.applefile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class BinHexInputStream extends InputStream {
	private final CharacterIterator ci;
	private final InputStream in;
	
	public BinHexInputStream(String s) throws IOException {
		this.ci = new StringCharacterIterator(s);
		this.in = null;
		checkHeader();
	}
	
	public BinHexInputStream(CharacterIterator ci) throws IOException {
		this.ci = ci;
		this.in = null;
		checkHeader();
	}
	
	public BinHexInputStream(InputStream in) throws IOException {
		this.ci = null;
		this.in = in;
		checkHeader();
	}
	
	private static final String hqxh = "(This file must be converted with BinHex 4.0)";
	
	private void checkHeader() throws IOException {
		int c = readChar(); if (c < 0) throw new EOFException();
		while (Character.isWhitespace(c)) {
			c = readChar(); if (c < 0) throw new EOFException();
		}
		for (char hc : hqxh.toCharArray()) {
			if (c != hc) throw new IOException("not a BinHex file");
			c = readChar(); if (c < 0) throw new EOFException();
		}
		while (Character.isWhitespace(c)) {
			c = readChar(); if (c < 0) throw new EOFException();
		}
		if (c != ':') throw new IOException("not a BinHex file");
	}
	
	private int crc = 0;
	private int rleByte = 0;
	private int rleCount = 0;
	private int b64Word = 0;
	private int b64Count = 0;
	private boolean b64eof = false;
	
	@Override
	public int read() throws IOException {
		int b = readRLEByte();
		if (b < 0) return b;
		for (int m = 0x80; m != 0; m >>= 1) {
			crc <<= 1;
			if ((b & m) != 0) crc++;
			if (crc > 0xFFFF) crc ^= 0x11021;
		}
		return b;
	}
	
	public void readCRC() throws IOException {
		for (int i = 0; i < 16; i++) {
			crc <<= 1;
			if (crc > 0xFFFF) crc ^= 0x11021;
		}
		int actual = crc;
		int expected = readRLEByte() << 8;
		expected |= readRLEByte();
		crc = 0;
		if (actual != expected) {
			throw new IOException(
				"CRC 0x" + Integer.toHexString(actual) +
				" != 0x" + Integer.toHexString(expected)
			);
		}
	}
	
	private int readRLEByte() throws IOException {
		if (rleCount > 0) {
			rleCount--;
			return rleByte;
		} else {
			return readRLERun();
		}
	}
	
	private int readRLERun() throws IOException {
		for (;;) {
			int a = readB64Byte();
			if (a == 0x90) {
				int b = readB64Byte();
				if (b == 1) continue;
				if (b >= 2) {
					rleCount = b - 2;
					return rleByte;
				}
			}
			rleCount = 0;
			rleByte = a;
			return a;
		}
	}
	
	private int readB64Byte() throws IOException {
		for (;;) {
			if (b64Count > 0) {
				b64Count--;
				b64Word <<= 8;
				return (b64Word >>> 24);
			}
			if (b64eof) return -1;
			readB64Word();
		}
	}
	
	private void readB64Word() throws IOException {
		for (;;) {
			int c = readChar();
			if (c < 0 || c == ':' || c == CharacterIterator.DONE) {
				if (b64Count > 0) {
					b64Count--;
					for (int i = b64Count; i < 3; i++) b64Word <<= 6;
				}
				b64eof = true;
				return;
			}
			c = hqxd(c);
			if (c >= 0) {
				b64Word <<= 6;
				b64Word |= c;
				b64Count++;
				if (b64Count > 3) {
					b64Count = 3;
					return;
				}
			}
		}
	}
	
	private int readChar() throws IOException {
		int c = -1;
		if (ci != null) { c = ci.current(); ci.next(); }
		if (in != null) { c = in.read(); }
		return c;
	}
	
	private int hqxd(int c) {
		switch (c) {
			case '!': return 0; case '"': return 1; case '#': return 2; case '$': return 3;
			case '%': return 4; case '&': return 5; case '\'': return 6; case '(': return 7;
			case ')': return 8; case '*': return 9; case '+': return 10; case ',': return 11;
			case '-': return 12; case '0': return 13; case '1': return 14; case '2': return 15;
			case '3': return 16; case '4': return 17; case '5': return 18; case '6': return 19;
			case '8': return 20; case '9': return 21; case '@': return 22; case 'A': return 23;
			case 'B': return 24; case 'C': return 25; case 'D': return 26; case 'E': return 27;
			case 'F': return 28; case 'G': return 29; case 'H': return 30; case 'I': return 31;
			case 'J': return 32; case 'K': return 33; case 'L': return 34; case 'M': return 35;
			case 'N': return 36; case 'P': return 37; case 'Q': return 38; case 'R': return 39;
			case 'S': return 40; case 'T': return 41; case 'U': return 42; case 'V': return 43;
			case 'X': return 44; case 'Y': return 45; case 'Z': return 46; case '[': return 47;
			case '`': return 48; case 'a': return 49; case 'b': return 50; case 'c': return 51;
			case 'd': return 52; case 'e': return 53; case 'f': return 54; case 'h': return 55;
			case 'i': return 56; case 'j': return 57; case 'k': return 58; case 'l': return 59;
			case 'm': return 60; case 'p': return 61; case 'q': return 62; case 'r': return 63;
			default: return -1;
		}
	}
	
	public AppleFile readAppleFile() throws IOException {
		// Read file name.
		int nameLen = read();
		if (nameLen < 0) throw new EOFException();
		byte[] nameData = new byte[nameLen];
		if (read(nameData) < nameLen) throw new EOFException();
		int nameTerm = read();
		if (nameTerm < 0) throw new EOFException();
		if (nameTerm > 0) throw new IOException("not a BinHex file");
		// Read Finder info.
		byte[] finfData = new byte[32];
		if (read(finfData, 0, 10) < 10) throw new EOFException();
		// Read data fork length.
		int dataLen = read() << 24;
		dataLen |= read() << 16;
		dataLen |= read() << 8;
		dataLen |= read();
		if (dataLen < 0) throw new IOException("not a BinHex file");
		// Read resource fork length.
		int rsrcLen = read() << 24;
		rsrcLen |= read() << 16;
		rsrcLen |= read() << 8;
		rsrcLen |= read();
		if (rsrcLen < 0) throw new IOException("not a BinHex file");
		readCRC();
		// Read data fork.
		byte[] dataData = new byte[dataLen];
		if (read(dataData) < dataLen) throw new EOFException();
		readCRC();
		// Read resource fork.
		byte[] rsrcData = new byte[rsrcLen];
		if (read(rsrcData) < rsrcLen) throw new EOFException();
		readCRC();
		// Create AppleFile.
		AppleFile file = new AppleFile(false, true, "BinHex 4.0");
		file.setPartData(AppleFilePart.TYPE_FILE_NAME, nameData);
		file.setPartData(AppleFilePart.TYPE_FINDER_INFO, finfData);
		file.setPartData(AppleFilePart.TYPE_DATA_FORK, dataData);
		file.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrcData);
		return file;
	}
	
	@Override
	public void close() throws IOException {
		if (in != null) in.close();
	}
}
