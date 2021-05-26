package com.kreative.iconposeur;

import java.io.DataInputStream;
import java.io.IOException;

public class AppleSinglePart {
	private int type;
	private int offset;
	private int length;
	private byte[] data;
	
	AppleSinglePart() {}
	
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
}
