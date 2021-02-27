package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
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

public class IcoFrame extends JFrame implements SaveInterface {
	private static final long serialVersionUID = 1L;
	
	private final WinIconDir ico;
	private File file;
	private boolean changed;
	
	public IcoFrame() {
		this.ico = new WinIconDir();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(boolean cursor) {
		this.ico = new WinIconDir(cursor);
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(byte[] data) throws IOException {
		this.ico = new WinIconDir();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ico.read(in);
		in.close();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(File file) throws IOException {
		this.ico = new WinIconDir();
		FileInputStream in = new FileInputStream(file);
		ico.read(in);
		in.close();
		this.file = file;
		this.changed = false;
		build();
	}
	
	private void build() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createIconWell(
			new WinIconWellModel.Size(256, 256, 0, new int[0], true, false, true, true),
			new WinIconWellModel.Size(256, 256, 0, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			new WinIconWellModel.Size(48, 48, 0, new int[0], true, false, true, true),
			new WinIconWellModel.Size(48, 48, 32, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			new WinIconWellModel.Size(32, 32, 0, new int[0], true, false, true, true),
			new WinIconWellModel.Size(32, 32, 32, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			new WinIconWellModel.Size(24, 24, 0, new int[0], true, false, true, true),
			new WinIconWellModel.Size(24, 24, 32, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			new WinIconWellModel.Size(16, 16, 0, new int[0], true, false, true, true),
			new WinIconWellModel.Size(16, 16, 32, new int[0], false, true, false, false)
		));
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
	
	private JPanel createIconWell(WinIconWellModel.Size... sizes) {
		IconWellModel model = new WinIconWellModel(ico, sizes);
		IconWell well = new IconWell(model);
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
			ico.write(out);
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
			if (!changed || (file == null && ico.isEmpty())) {
				dispose();
			} else {
				String title = (file == null) ? "Untitled" : file.getName();
				switch (new SaveChangesDialog(IcoFrame.this, title).showDialog()) {
					case SAVE: if (save()) dispose(); break;
					case DONT_SAVE: dispose(); break;
					case CANCEL: break;
				}
			}
		}
	};
}
