package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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
		setContentPane(createStandardPanel());
		setJMenuBar(new MainMenuBar(this, this, createViewMenu()));
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		updateWindow();
		addWindowListener(windowListener);
	}
	
	private JMenu createViewMenu() {
		JMenu viewMenu = new JMenu("View");
		
		JMenuItem standard = new JMenuItem("Standard");
		standard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, SwingUtils.SHORTCUT_KEY));
		standard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentPane(createStandardPanel());
				pack();
			}
		});
		viewMenu.add(standard);
		
		JMenuItem retina = new JMenuItem("Retina");
		retina.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, SwingUtils.SHORTCUT_KEY));
		retina.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentPane(createRetinaPanel());
				pack();
			}
		});
		viewMenu.add(retina);
		
		return viewMenu;
	}
	
	private JPanel createStandardPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createIconWell(
			"512", false,
			new MacIconWellModel.Type(MacIconSuite.ic09, 512, 512, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"256", false,
			new MacIconWellModel.Type(MacIconSuite.ic08, 256, 256, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"128", false,
			new MacIconWellModel.Type(MacIconSuite.it32, 128, 128, true, true),
			new MacIconWellModel.Type(MacIconSuite.ict8, 128, 128, true, false),
			new MacIconWellModel.Type(MacIconSuite.ict4, 128, 128, true, false),
			new MacIconWellModel.Type(MacIconSuite.ict$, 128, 128, true, false),
			new MacIconWellModel.Type(MacIconSuite.t8mk, 128, 128, false, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"32", false,
			new MacIconWellModel.Type(MacIconSuite.il32, 32, 32, true, true),
			new MacIconWellModel.Type(MacIconSuite.icl8, 32, 32, true, false),
			new MacIconWellModel.Type(MacIconSuite.icl4, 32, 32, true, false),
			new MacIconWellModel.Type(MacIconSuite.ICN$, 32, 32, true, false),
			new MacIconWellModel.Type(MacIconSuite.l8mk, 32, 32, false, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"16", false,
			new MacIconWellModel.Type(MacIconSuite.is32, 16, 16, true, true),
			new MacIconWellModel.Type(MacIconSuite.ics8, 16, 16, true, false),
			new MacIconWellModel.Type(MacIconSuite.ics4, 16, 16, true, false),
			new MacIconWellModel.Type(MacIconSuite.ics$, 16, 16, true, false),
			new MacIconWellModel.Type(MacIconSuite.s8mk, 16, 16, false, true)
		));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return panel;
	}
	
	private JPanel createRetinaPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createIconWell(
			"512 (2x)", true,
			new MacIconWellModel.Type(MacIconSuite.ic10, 1024, 1024, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"256 (2x)", true,
			new MacIconWellModel.Type(MacIconSuite.ic14, 512, 512, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"128 (2x)", true,
			new MacIconWellModel.Type(MacIconSuite.ic13, 256, 256, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"<html>32<br>(2x)</html>", true,
			new MacIconWellModel.Type(MacIconSuite.ic12, 64, 64, true, true)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"<html>16<br>(2x)</html>", true,
			new MacIconWellModel.Type(MacIconSuite.ic11, 32, 32, true, true)
		));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return panel;
	}
	
	private JPanel createIconWell(String title, boolean retina, MacIconWellModel.Type... types) {
		IconWellModel model = new MacIconWellModel(icns, types);
		IconWell well = new IconWell(model, retina);
		well.addIconWellListener(iconWellListener);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(well, BorderLayout.PAGE_START);
		if (title != null) {
			JLabel tl = new JLabel(title);
			tl.setFont(tl.getFont().deriveFont(12f));
			JPanel tp = new JPanel(new BorderLayout());
			tp.add(tl, BorderLayout.PAGE_START);
			panel.add(tp, BorderLayout.CENTER);
		}
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
