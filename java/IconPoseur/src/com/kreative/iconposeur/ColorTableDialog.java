package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ColorTableDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private ColorTablePanel panel;
	private JButton okButton;
	private JButton cancelButton;
	private JButton loadButton;
	private JButton saveButton;
	private JComboBox presetMenu;
	private boolean confirmed = false;
	private boolean eventLock = false;
	
	public ColorTableDialog(Dialog parent, int rows, int columns, int[] colorTable) {
		super(parent, "Color Table");
		setModal(true);
		make(rows, columns, colorTable);
	}
	
	public ColorTableDialog(Frame parent, int rows, int columns, int[] colorTable) {
		super(parent, "Color Table");
		setModal(true);
		make(rows, columns, colorTable);
	}
	
	public ColorTableDialog(Window parent, int rows, int columns, int[] colorTable) {
		super(parent, "Color Table");
		setModal(true);
		make(rows, columns, colorTable);
	}
	
	private void make(int rows, int columns, int[] colorTable) {
		panel = new ColorTablePanel(rows, columns, colorTable);
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		loadButton = new JButton("Load...");
		saveButton = new JButton("Save...");
		
		Preset[] presets = createPresets(colorTable.length);
		presetMenu = new JComboBox(presets);
		presetMenu.setEditable(false);
		presetMenu.setMaximumRowCount(presetMenu.getItemCount());
		presetMenu.setSelectedIndex(0);
		for (Preset preset : presets) {
			if (Arrays.equals(preset.createColorTable(), colorTable)) {
				presetMenu.setSelectedItem(preset);
				break;
			}
		}
		
		JPanel presetPanel = new JPanel(new BorderLayout(4, 4));
		presetPanel.add(new JLabel("Table:"), BorderLayout.LINE_START);
		presetPanel.add(presetMenu, BorderLayout.CENTER);
		JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
		leftPanel.add(presetPanel, BorderLayout.PAGE_START);
		leftPanel.add(panel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(loadButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(Box.createHorizontalStrut(80));
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(buttonPanel, BorderLayout.PAGE_START);
		
		JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
		mainPanel.add(leftPanel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.LINE_END);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		setContentPane(mainPanel);
		SwingUtils.setDefaultButton(getRootPane(), okButton);
		SwingUtils.setCancelButton(getRootPane(), cancelButton);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		okButton.requestFocusInWindow();
		
		panel.addColorTableListener(new ColorTableListener() {
			@Override
			public void dimensionsChanged(ColorTablePanel src) {
				// Nothing.
			}
			@Override
			public void colorTableChanged(ColorTablePanel src) {
				if (eventLock) return;
				eventLock = true;
				presetMenu.setSelectedIndex(0);
				eventLock = false;
			}
			@Override
			public void selectionChanged(ColorTablePanel src) {
				// Nothing.
			}
		});
		presetMenu.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (eventLock) return;
				eventLock = true;
				Preset preset = (Preset)presetMenu.getSelectedItem();
				if (preset != null) {
					int[] colorTable = preset.createColorTable();
					if (colorTable != null) {
						panel.setColorTable(colorTable);
					}
				}
				eventLock = false;
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmed = true;
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmed = false;
				dispose();
			}
		});
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				load();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
	}
	
	private void load() {
		FileDialog fd = new FileDialog(this, "Load", FileDialog.LOAD);
		fd.setVisible(true);
		String parent = fd.getDirectory();
		String name = fd.getFile();
		if (parent == null || name == null) return;
		File file = new File(parent, name);
		try {
			if (name.toLowerCase().endsWith(".act")) {
				readACT(file);
				return;
			}
			if (name.toLowerCase().endsWith(".bmp")) {
				readBMP(file);
				return;
			}
			JOptionPane.showMessageDialog(
				this, "The format of the selected file was not recognized.",
				"Load", JOptionPane.ERROR_MESSAGE
			);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(
				this, "An error occurred while loading the selected file.",
				"Load", JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void readACT(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		int[] ct = panel.getColorTable();
		for (int i = 0; i < ct.length; i++) {
			int r = (in.read() << 16);
			int g = (in.read() <<  8);
			int b = (in.read() <<  0);
			ct[i] = (0xFF << 24) | r | g | b;
		}
		panel.setColorTable(ct);
		in.close();
	}
	
	private void readBMP(File file) throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		readBMP(in);
		in.close();
	}
	
	private void readBMP(DataInputStream in) throws IOException {
		int[] ct = panel.getColorTable();
		int magic = in.readShort();
		if (magic != 0x424D) throw new IOException("Invalid value in header");
		int fileLength = Integer.reverseBytes(in.readInt());
		if (fileLength < 54) throw new IOException("Invalid value in header");
		in.readInt(); // reserved
		int dataOffset = Integer.reverseBytes(in.readInt());
		if (dataOffset < 54) throw new IOException("Invalid value in header");
		int headerLength = Integer.reverseBytes(in.readInt());
		if (headerLength < 40) throw new IOException("Invalid value in header");
		int width = Integer.reverseBytes(in.readInt());
		if (width <= 0) throw new IOException("Invalid value in header");
		int height = Integer.reverseBytes(in.readInt());
		if (height <= 0) throw new IOException("Invalid value in header");
		int planes = Short.reverseBytes(in.readShort());
		if (planes < 0 || planes > 1) throw new IOException("Invalid value in header");
		int bpp = Short.reverseBytes(in.readShort());
		if (bpp < 1 || bpp > 32) throw new IOException("Invalid value in header");
		in.readInt(); // compression
		int dataLength = Integer.reverseBytes(in.readInt());
		if (dataLength < 0) throw new IOException("Invalid value in header");
		in.readInt(); // ppm-x
		in.readInt(); // ppm-y
		int colorCount = Integer.reverseBytes(in.readInt());
		if (colorCount < 0) throw new IOException("Invalid value in header");
		if (colorCount == 0 && bpp <= 8) colorCount = (1 << bpp);
		in.readInt(); // important colors
		in.skipBytes(headerLength - 40);
		for (int i = 0; i < colorCount && i < ct.length; i++) {
			ct[i] = Integer.reverseBytes(in.readInt() | 0xFF);
		}
		for (int i = colorCount; i < ct.length; i++) {
			ct[i] = -1;
		}
		panel.setColorTable(ct);
	}
	
	private void save() {
		FileDialog fd = new FileDialog(this, "Save", FileDialog.SAVE);
		fd.setVisible(true);
		String parent = fd.getDirectory();
		String name = fd.getFile();
		if (parent == null || name == null) return;
		File file = new File(parent, name);
		try {
			if (name.toLowerCase().endsWith(".act")) {
				writeACT(file);
				return;
			}
			if (name.toLowerCase().endsWith(".bmp")) {
				writeBMP(file);
				return;
			}
			JOptionPane.showMessageDialog(
				this, "The format of the selected file was not recognized.",
				"Save", JOptionPane.ERROR_MESSAGE
			);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(
				this, "An error occurred while saving the selected file.",
				"Save", JOptionPane.ERROR_MESSAGE
			);
		}
	}
	
	private void writeACT(File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		int[] ct = panel.getColorTable();
		for (int i = 0; i < ct.length; i++) {
			int rgb = ct[i];
			out.write(rgb >> 16);
			out.write(rgb >>  8);
			out.write(rgb >>  0);
		}
		out.flush();
		out.close();
	}
	
	private void writeBMP(File file) throws IOException {
		DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
		int w = panel.getColumnCount();
		int h = panel.getRowCount();
		int[] ct = panel.getColorTable();
		int ctLength = ct.length * 4;
		int rowLength = ((w + 3) / 4) * 4;
		int dataLength = rowLength * h;
		int dataOffset = 54 + ctLength;
		int fileLength = dataOffset + dataLength;
		out.writeShort(0x424D);                          // magic
		out.writeInt(Integer.reverseBytes(fileLength));  // file length
		out.writeInt(0);                                 // reserved
		out.writeInt(Integer.reverseBytes(dataOffset));  // data offset
		out.writeInt(Integer.reverseBytes(40));          // header length
		out.writeInt(Integer.reverseBytes(w));           // width
		out.writeInt(Integer.reverseBytes(h));           // height
		out.writeShort(Short.reverseBytes((short)1));    // planes
		out.writeShort(Short.reverseBytes((short)8));    // bpp
		out.writeInt(0);                                 // compression
		out.writeInt(Integer.reverseBytes(dataLength));  // data length
		out.writeInt(0);                                 // ppm-x
		out.writeInt(0);                                 // ppm-y
		out.writeInt(Integer.reverseBytes(ct.length));   // color count
		out.writeInt(0);                                 // important colors
		for (int c : ct) {
			out.writeInt(Integer.reverseBytes(c & 0xFFFFFF));
		}
		for (int i = w * h, y = 0; y < h; y++) {
			i -= w;
			for (int x = 0; x < w; x++, i++) {
				out.writeByte((i < ct.length) ? i : 0);
			}
			for (int x = w; x < rowLength; x++) {
				out.writeByte(0);
			}
			i -= w;
		}
		out.flush();
		out.close();
	}
	
	public int[] showDialog() {
		confirmed = false;
		setVisible(true);
		return confirmed ? panel.getColorTable() : null;
	}
	
	private static abstract class Preset {
		public abstract String toString();
		public abstract int[] createColorTable();
	}
	
	private static Preset[] createPresets(int colorCount) {
		List<Preset> presets = new ArrayList<Preset>();
		presets.add(new Preset() {
			@Override public String toString() { return "Custom"; }
			@Override public int[] createColorTable() { return null; }
		});
		if (colorCount == 2) {
			presets.add(new Preset() {
				@Override public String toString() { return "Black to White"; }
				@Override public int[] createColorTable() { return ColorTables.createBlackToWhite(1); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "White to Black"; }
				@Override public int[] createColorTable() { return ColorTables.createWhiteToBlack(1); }
			});
		}
		if (colorCount == 4) {
			presets.add(new Preset() {
				@Override public String toString() { return "Black to White"; }
				@Override public int[] createColorTable() { return ColorTables.createBlackToWhite(2); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "White to Black"; }
				@Override public int[] createColorTable() { return ColorTables.createWhiteToBlack(2); }
			});
		}
		if (colorCount == 16) {
			presets.add(new Preset() {
				@Override public String toString() { return "Windows"; }
				@Override public int[] createColorTable() { return ColorTables.createWindows4(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Mac OS"; }
				@Override public int[] createColorTable() { return ColorTables.createMacintosh4(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Black to White"; }
				@Override public int[] createColorTable() { return ColorTables.createBlackToWhite(4); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "White to Black"; }
				@Override public int[] createColorTable() { return ColorTables.createWhiteToBlack(4); }
			});
		}
		if (colorCount == 256) {
			presets.add(new Preset() {
				@Override public String toString() { return "Windows (Eis)"; }
				@Override public int[] createColorTable() { return ColorTables.createWindowsEis(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Windows (Paint)"; }
				@Override public int[] createColorTable() { return ColorTables.createWindowsPaint(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Windows (Web-Safe)"; }
				@Override public int[] createColorTable() { return ColorTables.createWindowsWebSafe(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Mac OS"; }
				@Override public int[] createColorTable() { return ColorTables.createMacintosh8(); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "Black to White"; }
				@Override public int[] createColorTable() { return ColorTables.createBlackToWhite(8); }
			});
			presets.add(new Preset() {
				@Override public String toString() { return "White to Black"; }
				@Override public int[] createColorTable() { return ColorTables.createWhiteToBlack(8); }
			});
		}
		return presets.toArray(new Preset[presets.size()]);
	}
}
