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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MacResourceFile {
	private int dataOffset;
	private int mapOffset;
	private int attributes;
	private int typesOffset;
	private int namesOffset;
	private final List<MacResourceType> rwTypes = new ArrayList<MacResourceType>();
	private final Map<Integer,MacResourceType> rwByType = new TreeMap<Integer,MacResourceType>();
	private final Map<String,MacResourceType> rwByTypeString = new TreeMap<String,MacResourceType>();
	private final List<MacResourceType> roTypes = Collections.unmodifiableList(rwTypes);
	private final Map<Integer,MacResourceType> roByType = Collections.unmodifiableMap(rwByType);
	private final Map<String,MacResourceType> roByTypeString = Collections.unmodifiableMap(rwByTypeString);
	
	public MacResourceFile() {}
	public MacResourceFile(File file) throws IOException { read(file); }
	public MacResourceFile(InputStream in) throws IOException { read(in); }
	public MacResourceFile(byte[] data) throws IOException { read(data); }
	
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
		readHead(in);
		readBody(in);
		in.close();
		bin.close();
	}
	
	private void readHead(DataInputStream in) throws IOException {
		this.dataOffset = in.readInt();
		this.mapOffset = in.readInt();
		in.reset();
		in.skipBytes(this.mapOffset + 22);
		this.attributes = in.readShort();
		this.typesOffset = in.readShort();
		this.namesOffset = in.readShort();
	}
	
	private void readBody(DataInputStream in) throws IOException {
		// Read type entries
		in.reset();
		in.skipBytes(mapOffset + typesOffset);
		int count = (in.readShort() + 1) & 0xFFFF;
		for (int i = 0; i < count; i++) {
			int type = in.readInt();
			String typeString = MacResourceType.toString(type);
			MacResourceType t = new MacResourceType(type, typeString);
			t.readHead(in);
			rwTypes.add(t);
			rwByType.put(type, t);
			rwByTypeString.put(typeString, t);
		}
		// Read resources
		for (MacResourceType t : rwTypes) {
			t.readBody(in, dataOffset, mapOffset, typesOffset, namesOffset);
		}
	}
	
	public int getAttributes() { return attributes; }
	public List<MacResourceType> getResourceTypes() { return roTypes; }
	public Set<Integer> getResourceTypeIds() { return roByType.keySet(); }
	public Set<String> getResourceTypeStrings() { return roByTypeString.keySet(); }
	public MacResourceType getResourceType(int type) { return roByType.get(type); }
	public MacResourceType getResourceType(String type) { return roByTypeString.get(type); }
	
	public MacResource getResource(int type, int id) {
		MacResourceType t = getResourceType(type);
		return (t != null) ? t.getResource(id) : null;
	}
	public MacResource getResource(int type, String name) {
		MacResourceType t = getResourceType(type);
		return (t != null) ? t.getResource(name) : null;
	}
	public MacResource getResource(String type, int id) {
		MacResourceType t = getResourceType(type);
		return (t != null) ? t.getResource(id) : null;
	}
	public MacResource getResource(String type, String name) {
		MacResourceType t = getResourceType(type);
		return (t != null) ? t.getResource(name) : null;
	}
	
	public boolean addResource(MacResource r) {
		if (r == null) return false;
		MacResourceType t = getResourceType(r.getType());
		if (t == null) {
			t = new MacResourceType(r.getType(), r.getTypeString());
			rwTypes.add(t);
			rwByType.put(r.getType(), t);
			rwByTypeString.put(r.getTypeString(), t);
		}
		return t.addResource(r);
	}
	
	public boolean removeResource(MacResource r) {
		MacResourceType t = getResourceType(r.getType());
		boolean ret = (t != null) ? t.removeResource(r) : false;
		checkType(t); return ret;
	}
	public MacResource removeResource(int type, int id) {
		MacResourceType t = getResourceType(type);
		MacResource ret = (t != null) ? t.removeResource(id) : null;
		checkType(t); return ret;
	}
	public MacResource removeResource(int type, String name) {
		MacResourceType t = getResourceType(type);
		MacResource ret = (t != null) ? t.removeResource(name) : null;
		checkType(t); return ret;
	}
	public MacResource removeResource(String type, int id) {
		MacResourceType t = getResourceType(type);
		MacResource ret = (t != null) ? t.removeResource(id) : null;
		checkType(t); return ret;
	}
	public MacResource removeResource(String type, String name) {
		MacResourceType t = getResourceType(type);
		MacResource ret = (t != null) ? t.removeResource(name) : null;
		checkType(t); return ret;
	}
	private void checkType(MacResourceType t) {
		if (t != null && t.getResources().isEmpty()) {
			rwTypes.remove(t);
			rwByType.values().remove(t);
			rwByTypeString.values().remove(t);
		}
	}
	
	public byte[] write() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		write(out);
		return out.toByteArray();
	}
	
	public void write(File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		write(out);
		out.close();
	}
	
	public void write(OutputStream out) throws IOException {
		writeImpl(new DataOutputStream(out));
	}
	
	private void writeImpl(DataOutputStream out) throws IOException {
		ByteArrayOutputStream dataArr = new ByteArrayOutputStream();
		ByteArrayOutputStream nameArr = new ByteArrayOutputStream();
		ByteArrayOutputStream listArr = new ByteArrayOutputStream();
		ByteArrayOutputStream typeArr = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(dataArr);
		DataOutputStream nameOut = new DataOutputStream(nameArr);
		DataOutputStream listOut = new DataOutputStream(listArr);
		DataOutputStream typeOut = new DataOutputStream(typeArr);
		int dataPtr = 0;
		int namePtr = 0;
		int listPtr = roTypes.size() * 8 + 2;
		typeOut.writeShort(roTypes.size() - 1);
		for (MacResourceType t : roTypes) {
			dataPtr = t.writeData(dataOut, dataPtr);
			namePtr = t.writeName(nameOut, namePtr);
			listPtr = t.writeList(listOut, listPtr);
			t.writeHead(typeOut);
		}
		out.writeInt(256);                    // resource data offset
		out.writeInt(dataPtr + 256);          // resource map offset
		out.writeInt(dataPtr);                // resource data size
		out.writeInt(namePtr + listPtr + 28); // resource map size
		out.write(new byte[240]);             // empty space
		out.write(dataArr.toByteArray());     // resource data
		out.writeInt(256);                    // resource data offset
		out.writeInt(dataPtr + 256);          // resource map offset
		out.writeInt(dataPtr);                // resource data size
		out.writeInt(namePtr + listPtr + 28); // resource map size
		out.writeInt(0);                      // next resource map
		out.writeShort(0);                    // file ref
		out.writeShort(attributes);           // attributes
		out.writeShort(28);                   // offset from map to type list
		out.writeShort(listPtr + 28);         // offset from map to name list
		out.write(typeArr.toByteArray());     // type list
		out.write(listArr.toByteArray());     // resource list
		out.write(nameArr.toByteArray());     // name list
	}
}
