package com.kreative.applefile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AppleFilePart {
	public static final int TYPE_DATA_FORK = 1;
	public static final int TYPE_RESOURCE_FORK = 2;
	public static final int TYPE_FILE_NAME = 3;
	public static final int TYPE_COMMENT = 4;
	public static final int TYPE_ICON = 5;
	public static final int TYPE_CICN = 6;
	public static final int TYPE_FILE_INFO = 7;
	public static final int TYPE_TIMESTAMP = 8;
	public static final int TYPE_FINDER_INFO = 9;
	public static final int TYPE_MACINTOSH_INFO = 10;
	public static final int TYPE_PRODOS_INFO = 11;
	public static final int TYPE_MSDOS_INFO = 12;
	public static final int TYPE_AFP_SHORT_NAME = 13;
	public static final int TYPE_AFP_FILE_INFO = 14;
	public static final int TYPE_AFP_DIRECTORY_ID = 15;
	
	private int type;
	private int offset;
	private int length;
	private byte[] data;
	
	public AppleFilePart(int type, byte[] data, int off, int len) {
		this.type = type;
		this.setData(data, off, len);
	}
	
	AppleFilePart() {}
	
	void readHead(DataInputStream in) throws IOException {
		this.type = in.readInt();
		this.offset = in.readInt();
		this.length = in.readInt();
	}
	
	void readBody(DataInputStream in) throws IOException {
		in.reset();
		in.skipBytes(this.offset);
		byte[] d = new byte[this.length];
		in.readFully(d);
		this.data = d;
	}
	
	public int getType() { return type; }
	public byte[] getData() { return data; }
	
	public void setData(byte[] data, int offset, int length) {
		this.data = new byte[length];
		for (int i = 0; i < length; i++) {
			this.data[i] = data[offset];
			offset++;
		}
	}
	
	int writeHead(DataOutputStream out, int ptr) throws IOException {
		this.offset = ptr;
		this.length = data.length;
		out.writeInt(this.type);
		out.writeInt(this.offset);
		out.writeInt(this.length);
		return ptr + data.length;
	}
}
