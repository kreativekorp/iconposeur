package com.kreative.applefile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public enum Format {
	AUTO("Auto", "auto") {
		public AppleFile read(File file) throws IOException {
			File parent = file.getParentFile();
			String name = file.getName();
			
			// Guess using related files
			File finf = new File(new File(parent, ".finf"), name);
			File rsrc = new File(new File(parent, ".rsrc"), name);
			if (finf.exists() || rsrc.exists()) return BASILISK_II.read(file);
			
			File rf = new File(new File(parent, "RESOURCE.FRK"), name);
			if (rf.exists()) return PC_EXCHANGE.read(file);
			
			File osxadf = new File(parent, "._" + name);
			if (osxadf.exists()) return APPLEDOUBLE_MACOSX.read(file);
			
			File auxadf = new File(parent, "%" + name);
			if (auxadf.exists()) return APPLEDOUBLE_AUX.read(file);
			
			File pdosadf = new File(parent, "R." + name);
			if (pdosadf.exists()) return APPLEDOUBLE_PRODOS.read(file);
			
			// Guess using file extension
			String lcname = name.toLowerCase();
			if (lcname.endsWith(".data")) return DATA_FORK.read(file);
			if (lcname.endsWith(".rsrc")) return RESOURCE_FORK.read(file);
			if (lcname.endsWith(".hqx")) return BINHEX.read(file);
			if (lcname.endsWith(".bin")) return MACBINARY.read(file);
			
			// Guess using file contents
			try { return APPLESINGLE.read(file); }
			catch (IOException e) {}
			
			try { return BINHEX.read(file); }
			catch (IOException e) {}
			
			try { return MACBINARY.read(file); }
			catch (IOException e) {}
			
			return (isMacOS() ? NATIVE : DATA_FORK).read(file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			String lcname = file.getName().toLowerCase();
			if (lcname.endsWith(".data")) return DATA_FORK.write(file, af);
			if (lcname.endsWith(".rsrc")) return RESOURCE_FORK.write(file, af);
			if (lcname.endsWith(".hqx")) return BINHEX.write(file, af);
			if (lcname.endsWith(".bin")) return MACBINARY.write(file, af);
			return (isMacOS() ? NATIVE : APPLEDOUBLE_MACOSX).write(file, af);
		}
	},
	NATIVE("Native", "native") {
		public AppleFile read(File file) throws IOException {
			AppleFile af = new AppleFile(false, true, "Mac OS X");
			try {
				byte[] finf = getXAttr(file, "com.apple.FinderInfo");
				if (finf.length > 0) af.setPartData(AppleFilePart.TYPE_FINDER_INFO, finf);
			} catch (IOException e0) {
				// no finder info or unsupported by host system
			}
			try {
				File rsrc = new File(new File(file, "..namedfork"), "rsrc");
				af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
			} catch (IOException e1) {
				try {
					File rsrc = new File(file, "rsrc");
					af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
				} catch (IOException e2) {
					try {
						byte[] rsrc = getXAttr(file, "com.apple.ResourceFork");
						if (rsrc.length > 0) af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
					} catch (IOException e3) {
						// no resource fork or unsupported by host system
					}
				}
			}
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, file);
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			byte[] data = af.getPartData(AppleFilePart.TYPE_DATA_FORK);
			FileOutputStream out = new FileOutputStream(file);
			if (data != null) out.write(data);
			out.close();
			byte[] rsrc = af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
			if (rsrc != null && rsrc.length > 0) {
				try {
					File f = new File(new File(file, "..namedfork"), "rsrc");
					FileOutputStream o = new FileOutputStream(f);
					o.write(rsrc);
					o.close();
				} catch (IOException e1) {
					try {
						File f = new File(file, "rsrc");
						FileOutputStream o = new FileOutputStream(f);
						o.write(rsrc);
						o.close();
					} catch (IOException e2) {
						// We cannot write the resource fork using setXAttr because
						// it exceeds the maximum length of a command line argument.
						// If only xattr could read the value from standard input...
						throw new IOException("cannot write resource fork", e1);
					}
				}
			}
			byte[] finf = af.getPartData(AppleFilePart.TYPE_FINDER_INFO);
			if (finf != null && finf.length >= 32) {
				try {
					setXAttr(file, "com.apple.FinderInfo", finf, 0, 32);
				} catch (IOException e) {
					throw new IOException("cannot write Finder info", e);
				}
			}
			return true;
		}
	},
	DATA_FORK("Data Fork in Flat File", "data", "dataindata") {
		public AppleFile read(File file) throws IOException {
			AppleFile af = new AppleFile(false, true, "Data Fork");
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, file);
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			byte[] data = af.getPartData(AppleFilePart.TYPE_DATA_FORK);
			FileOutputStream out = new FileOutputStream(file);
			if (data != null) out.write(data);
			out.close();
			return true;
		}
	},
	RESOURCE_FORK("Resource Fork in Flat File", "rsrc", "rsrcindata", "resourceindata") {
		public AppleFile read(File file) throws IOException {
			AppleFile af = new AppleFile(false, true, "Resource Fork");
			af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, file);
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			byte[] rsrc = af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
			FileOutputStream out = new FileOutputStream(file);
			if (rsrc != null) out.write(rsrc);
			out.close();
			return true;
		}
	},
	BASILISK_II("Basilisk II/SheepShaver", "bii", "basiliskii", "ss", "sheepshaver") {
		public AppleFile read(File file) throws IOException {
			AppleFile af = new AppleFile(false, true, "Basilisk II");
			File finf = new File(new File(file.getParentFile(), ".finf"), file.getName());
			if (finf.exists()) af.setPartData(AppleFilePart.TYPE_FINDER_INFO, finf);
			File rsrc = new File(new File(file.getParentFile(), ".rsrc"), file.getName());
			if (rsrc.exists()) af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, file);
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			byte[] data = af.getPartData(AppleFilePart.TYPE_DATA_FORK);
			FileOutputStream out = new FileOutputStream(file);
			if (data != null) out.write(data);
			out.close();
			byte[] rsrc = af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
			if (rsrc != null && rsrc.length > 0) {
				File d = new File(file.getParentFile(), ".rsrc");
				if (!d.isDirectory()) d.mkdir();
				File f = new File(d, file.getName());
				FileOutputStream o = new FileOutputStream(f);
				o.write(rsrc);
				o.close();
			}
			byte[] finf = af.getPartData(AppleFilePart.TYPE_FINDER_INFO);
			if (finf != null && finf.length > 0) {
				File d = new File(file.getParentFile(), ".finf");
				if (!d.isDirectory()) d.mkdir();
				File f = new File(d, file.getName());
				FileOutputStream o = new FileOutputStream(f);
				o.write(finf);
				o.close();
			}
			return true;
		}
	},
	PC_EXCHANGE("PC/File Exchange", "pcexchange", "fileexchange", "macos9", "os9") {
		public AppleFile read(File file) throws IOException {
			AppleFile af = new AppleFile(false, true, "Mac OS 9");
			// How to do Finder info?
			File rsrc = new File(new File(file.getParentFile(), "RESOURCE.FRK"), file.getName());
			if (rsrc.exists()) af.setPartData(AppleFilePart.TYPE_RESOURCE_FORK, rsrc);
			af.setPartData(AppleFilePart.TYPE_DATA_FORK, file);
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			byte[] data = af.getPartData(AppleFilePart.TYPE_DATA_FORK);
			FileOutputStream out = new FileOutputStream(file);
			if (data != null) out.write(data);
			out.close();
			byte[] rsrc = af.getPartData(AppleFilePart.TYPE_RESOURCE_FORK);
			if (rsrc != null && rsrc.length > 0) {
				File d = new File(file.getParentFile(), "RESOURCE.FRK");
				if (!d.isDirectory()) d.mkdir();
				File f = new File(d, file.getName());
				FileOutputStream o = new FileOutputStream(f);
				o.write(rsrc);
				o.close();
			}
			// How to do Finder info?
			return true;
		}
	},
	APPLEDOUBLE_MACOSX("AppleDouble (Mac OS X)", "appledoublemacosx", "macosx", "osx") {
		public AppleFile read(File file) throws IOException {
			File adf = new File(file.getParentFile(), "._" + file.getName());
			return new AppleFile(adf, file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			File adf = new File(file.getParentFile(), "._" + file.getName());
			af.write(adf, file);
			return true;
		}
	},
	APPLEDOUBLE_AUX("AppleDouble (A/UX)", "appledoubleaux", "aux") {
		public AppleFile read(File file) throws IOException {
			File adf = new File(file.getParentFile(), "%" + file.getName());
			return new AppleFile(adf, file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			File adf = new File(file.getParentFile(), "%" + file.getName());
			af.write(adf, file);
			return true;
		}
	},
	APPLEDOUBLE_PRODOS("AppleDouble (ProDOS)", "appledoubleprodos", "prodos", "pdos") {
		public AppleFile read(File file) throws IOException {
			File adf = new File(file.getParentFile(), "R." + file.getName());
			return new AppleFile(adf, file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			File adf = new File(file.getParentFile(), "R." + file.getName());
			af.write(adf, file);
			return true;
		}
	},
	APPLESINGLE("AppleSingle", "as", "applesingle") {
		public AppleFile read(File file) throws IOException {
			return new AppleFile(file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			af.write(file, false);
			return true;
		}
	},
	BINHEX("BinHex 4.0", "hqx", "binhex", "binhex40") {
		public AppleFile read(File file) throws IOException {
			BinHexInputStream hqx = new BinHexInputStream(new FileInputStream(file));
			AppleFile af = hqx.readAppleFile();
			hqx.close();
			return af;
		}
		public boolean write(File file, AppleFile af) throws IOException {
			String filename = file.getName();
			boolean hasext = filename.toLowerCase().endsWith(".hqx");
			String macname = hasext ? filename.substring(0, filename.length() - 4) : filename;
			BinHexOutputStream hqx = new BinHexOutputStream(new FileOutputStream(file));
			hqx.writeAppleFile(macname, af);
			hqx.close();
			return true;
		}
	},
	MACBINARY("MacBinary", "bin", "mbin", "macbin", "macbinary") {
		public AppleFile read(File file) throws IOException {
			return MacBinaryUtility.read(file);
		}
		public boolean write(File file, AppleFile af) throws IOException {
			String filename = file.getName();
			boolean hasext = filename.toLowerCase().endsWith(".bin");
			String macname = hasext ? filename.substring(0, filename.length() - 4) : filename;
			MacBinaryUtility.write(file, macname, af);
			return true;
		}
	};
	
	private final String name;
	private final String[] ids;
	
	private Format(String name, String... ids) {
		this.name = name;
		this.ids = ids;
	}
	
	public abstract AppleFile read(File file) throws IOException;
	public abstract boolean write(File file, AppleFile af) throws IOException;
	
	public String toString() {
		return this.name;
	}
	
	public static Format forName(String name) {
		for (Format format : values()) {
			if (format.name.equalsIgnoreCase(name)) {
				return format;
			}
		}
		String nn = name.replaceAll("[^A-Za-z0-9]","").toLowerCase();
		for (Format format : values()) {
			for (String id : format.ids) {
				if (id.equals(nn)) {
					return format;
				}
			}
		}
		return valueOf(name);
	}
	
	private static boolean isMacOS() throws IOException {
		try { return System.getProperty("os.name").toUpperCase().contains("MAC OS"); }
		catch (Exception e) { return false; }
	}
	
	private static byte[] getXAttr(File file, String key) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		String[] cmd = {"/usr/bin/xattr", "-px", key, file.getAbsolutePath()};
		Process p = Runtime.getRuntime().exec(cmd);
		Scanner scan = new Scanner(p.getInputStream());
		while (scan.hasNextLine()) {
			char[] line = scan.nextLine().toCharArray();
			int i = 0, n = line.length;
			while (i < n) {
				int d1 = Character.digit(line[i++], 16);
				if (d1 >= 0) {
					if (i < n) {
						int d2 = Character.digit(line[i++], 16);
						if (d2 >= 0) {
							bytes.write((d1 << 4) | d2);
							continue;
						}
					}
					bytes.write(d1);
					continue;
				}
			}
		}
		scan.close();
		try { p.waitFor(); }
		catch (InterruptedException e) {}
		return bytes.toByteArray();
	}
	
	private static void setXAttr(File file, String key, byte[] data, int off, int len) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(HEX[data[off++] & 0xFF]);
			if ((i & 15) == 15) sb.append("\n");
			else sb.append(" ");
		}
		String[] cmd = {"/usr/bin/xattr", "-wx", key, sb.toString(), file.getAbsolutePath()};
		Process p = Runtime.getRuntime().exec(cmd);
		try { p.waitFor(); }
		catch (InterruptedException e) {}
	}
	
	private static final String[] HEX = {
		"00","01","02","03","04","05","06","07","08","09","0A","0B","0C","0D","0E","0F",
		"10","11","12","13","14","15","16","17","18","19","1A","1B","1C","1D","1E","1F",
		"20","21","22","23","24","25","26","27","28","29","2A","2B","2C","2D","2E","2F",
		"30","31","32","33","34","35","36","37","38","39","3A","3B","3C","3D","3E","3F",
		"40","41","42","43","44","45","46","47","48","49","4A","4B","4C","4D","4E","4F",
		"50","51","52","53","54","55","56","57","58","59","5A","5B","5C","5D","5E","5F",
		"60","61","62","63","64","65","66","67","68","69","6A","6B","6C","6D","6E","6F",
		"70","71","72","73","74","75","76","77","78","79","7A","7B","7C","7D","7E","7F",
		"80","81","82","83","84","85","86","87","88","89","8A","8B","8C","8D","8E","8F",
		"90","91","92","93","94","95","96","97","98","99","9A","9B","9C","9D","9E","9F",
		"A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","AA","AB","AC","AD","AE","AF",
		"B0","B1","B2","B3","B4","B5","B6","B7","B8","B9","BA","BB","BC","BD","BE","BF",
		"C0","C1","C2","C3","C4","C5","C6","C7","C8","C9","CA","CB","CC","CD","CE","CF",
		"D0","D1","D2","D3","D4","D5","D6","D7","D8","D9","DA","DB","DC","DD","DE","DF",
		"E0","E1","E2","E3","E4","E5","E6","E7","E8","E9","EA","EB","EC","ED","EE","EF",
		"F0","F1","F2","F3","F4","F5","F6","F7","F8","F9","FA","FB","FC","FD","FE","FF",
	};
}
