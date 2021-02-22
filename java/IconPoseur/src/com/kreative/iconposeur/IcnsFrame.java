package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class IcnsFrame extends JFrame implements SaveInterface {
	private static final long serialVersionUID = 1L;
	
	private final MacIconSuite icns;
	private File file;
	private boolean changed;
	
	public IcnsFrame() {
		this.icns = new MacIconSuite();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcnsFrame(byte[] data) throws IOException {
		this.icns = new MacIconSuite();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		icns.read(new DataInputStream(in));
		in.close();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcnsFrame(File file) throws IOException {
		this.icns = new MacIconSuite();
		FileInputStream in = new FileInputStream(file);
		icns.read(new DataInputStream(in));
		in.close();
		this.file = file;
		this.changed = false;
		build();
	}
	
	private void build() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createIconWell(1, MacIconSuite.ic09, 512, 512));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(1, MacIconSuite.ic08, 256, 256));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(2, MacIconSuite.it32, 128, 128, MacIconSuite.t8mk, 128, 128));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(2, MacIconSuite.il32, 32, 32, MacIconSuite.l8mk, 32, 32));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(2, MacIconSuite.is32, 16, 16, MacIconSuite.s8mk, 16, 16));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(panel);
		setJMenuBar(new MainMenuBar(this, this));
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		updateWindow();
		addWindowListener(windowListener);
	}
	
	private JPanel createIconWell(int numTypes, int... typesWidthsHeights) {
		IconWell well = new IconWell(icns, numTypes, typesWidthsHeights);
		well.addIconWellListener(iconWellListener);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(well, BorderLayout.PAGE_START);
		return panel;
	}
	
	@Override
	public boolean save() {
		if (file == null) {
			return saveAs();
		} else try {
			FileOutputStream out = new FileOutputStream(file);
			icns.write(new DataOutputStream(out));
			out.flush();
			out.close();
			changed = false;
			updateWindow();
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
				this, "An error occurred while saving this file.",
				"Save", JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
	}
	
	@Override
	public boolean saveAs() {
		FileDialog fd = new FileDialog(this, "Save", FileDialog.SAVE);
		fd.setVisible(true);
		String parent = fd.getDirectory();
		String name = fd.getFile();
		if (parent == null || name == null) return false;
		file = new File(parent, name);
		return save();
	}
	
	private void updateWindow() {
		String title = (file == null) ? "Untitled" : file.getName();
		if (SwingUtils.IS_MAC_OS) {
			getRootPane().putClientProperty("Window.documentFile", file);
			getRootPane().putClientProperty("Window.documentModified", changed);
			setTitle(title);
		} else {
			setTitle(changed ? (title + " \u2022") : title);
		}
	}
	
	private final IconWellListener iconWellListener = new IconWellListener() {
		@Override
		public void iconChanged(IconWell well) {
			if (changed) return;
			changed = true;
			updateWindow();
		}
	};
	
	private final WindowAdapter windowListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			if (!changed || (file == null && icns.isEmpty())) {
				dispose();
			} else {
				String title = (file == null) ? "Untitled" : file.getName();
				switch (new SaveChangesDialog(IcnsFrame.this, title).showDialog()) {
					case SAVE: if (save()) dispose(); break;
					case DONT_SAVE: dispose(); break;
					case CANCEL: break;
				}
			}
		}
	};
}
