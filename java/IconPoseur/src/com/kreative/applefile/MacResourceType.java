package com.kreative.applefile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MacResourceType {
	public static int toInteger(String type) {
		try {
			byte[] d = ((type == null) ? "    " : (type + "    ")).getBytes("MacRoman");
			return ((d[0]&0xFF) << 24) | ((d[1]&0xFF) << 16) | ((d[2]&0xFF) << 8) | (d[3]&0xFF);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static String toString(int type) {
		try {
			byte[] d = { (byte)(type >> 24), (byte)(type >> 16), (byte)(type >> 8), (byte)type };
			return new String(d, "MacRoman");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private final int type;
	private final String typeString;
	private int count;
	private int offset;
	private final List<MacResource> rwResources = new ArrayList<MacResource>();
	private final Map<Integer,MacResource> rwById = new TreeMap<Integer,MacResource>();
	private final Map<String,MacResource> rwByName = new TreeMap<String,MacResource>();
	private final List<MacResource> roResources = Collections.unmodifiableList(rwResources);
	private final Map<Integer,MacResource> roById = Collections.unmodifiableMap(rwById);
	private final Map<String,MacResource> roByName = Collections.unmodifiableMap(rwByName);
	
	MacResourceType(int type, String typeString) {
		this.type = type;
		this.typeString = typeString;
	}
	
	void readHead(DataInputStream in) throws IOException {
		this.count = (in.readShort() + 1) & 0xFFFF;
		this.offset = in.readShort();
	}
	
	void readBody(DataInputStream in, int dataOffset, int mapOffset, int typesOffset, int namesOffset) throws IOException {
		// Read resource entries
		in.reset();
		in.skipBytes(mapOffset + typesOffset + offset);
		for (int i = 0; i < count; i++) {
			MacResource r = new MacResource(type, typeString);
			r.readHead(in);
			rwResources.add(r);
			rwById.put(r.getId(), r);
		}
		// Read resource names and data
		for (MacResource r : rwResources) {
			r.readBody(in, dataOffset, mapOffset, namesOffset);
			if (r.getName() != null) rwByName.put(r.getName(), r);
		}
	}
	
	public int getType() { return type; }
	public String getTypeString() { return typeString; }
	public List<MacResource> getResources() { return roResources; }
	public Set<Integer> getResourceIds() { return roById.keySet(); }
	public Set<String> getResourceNames() { return roByName.keySet(); }
	public MacResource getResource(int id) { return roById.get(id); }
	public MacResource getResource(String name) { return roByName.get(name); }
	
	boolean addResource(MacResource r) {
		if (r == null) return false;
		if (r.getType() != this.type) return false;
		if (getResource(r.getId()) != null) return false;
		rwResources.add(r);
		rwById.put(r.getId(), r);
		if (r.getName() != null) rwByName.put(r.getName(), r);
		return true;
	}
	
	boolean removeResource(MacResource r) {
		boolean a = rwResources.remove(r);
		boolean b = rwById.values().remove(r);
		boolean c = rwByName.values().remove(r);
		return a || b || c;
	}
	
	MacResource removeResource(int id) {
		MacResource r = getResource(id);
		return removeResource(r) ? r : null;
	}
	
	MacResource removeResource(String name) {
		MacResource r = getResource(name);
		return removeResource(r) ? r : null;
	}
	
	int writeData(DataOutputStream out, int ptr) throws IOException {
		for (MacResource r : roResources) {
			ptr = r.writeData(out, ptr);
		}
		return ptr;
	}
	
	int writeName(DataOutputStream out, int ptr) throws IOException {
		for (MacResource r : roResources) {
			ptr = r.writeName(out, ptr);
		}
		return ptr;
	}
	
	int writeList(DataOutputStream out, int ptr) throws IOException {
		for (MacResource r : roResources) {
			r.writeHead(out);
		}
		this.offset = ptr;
		return roResources.size() * 12 + ptr;
	}
	
	void writeHead(DataOutputStream out) throws IOException {
		out.writeInt(this.type);
		out.writeShort(this.roResources.size() - 1);
		out.writeShort(this.offset);
	}
}
