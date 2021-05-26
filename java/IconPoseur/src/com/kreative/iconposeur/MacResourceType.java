package com.kreative.iconposeur;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MacResourceType {
	private int type;
	private String typeString;
	private int count;
	private int offset;
	private List<MacResource> resources;
	private Map<Integer,MacResource> byId;
	private Map<String,MacResource> byName;
	
	MacResourceType() {}
	
	void readHead(DataInputStream in) throws IOException {
		this.type = in.readInt();
		byte[] d = {
			(byte)(this.type >> 24),
			(byte)(this.type >> 16),
			(byte)(this.type >>  8),
			(byte)(this.type >>  0),
		};
		this.typeString = new String(d, "MacRoman");
		this.count = (in.readShort() + 1) & 0xFFFF;
		this.offset = in.readShort();
	}
	
	void readBody(DataInputStream in, int dataOffset, int mapOffset, int typesOffset, int namesOffset) throws IOException {
		this.resources = new ArrayList<MacResource>();
		this.byId = new TreeMap<Integer,MacResource>();
		this.byName = new TreeMap<String,MacResource>();
		// Read resource entries
		in.reset();
		in.skipBytes(mapOffset + typesOffset + this.offset);
		for (int i = 0; i < count; i++) {
			MacResource r = new MacResource();
			r.readHead(in);
			this.resources.add(r);
			this.byId.put(r.getId(), r);
		}
		// Read resource names and data
		for (MacResource r : this.resources) {
			r.readBody(in, dataOffset, mapOffset, namesOffset);
			if (r.getName() != null) this.byName.put(r.getName(), r);
		}
		this.resources = Collections.unmodifiableList(this.resources);
		this.byId = Collections.unmodifiableMap(this.byId);
		this.byName = Collections.unmodifiableMap(this.byName);
	}
	
	public int getType() { return type; }
	public String getTypeString() { return typeString; }
	public List<MacResource> getResources() { return resources; }
	public Set<Integer> getResourceIds() { return byId.keySet(); }
	public Set<String> getResourceNames() { return byName.keySet(); }
	public MacResource getResource(int id) { return byId.get(id); }
	public MacResource getResource(String name) { return byName.get(name); }
}
