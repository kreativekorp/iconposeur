package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridLayout;
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
		
		JMenuItem classic = new JMenuItem("Classic");
		classic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, SwingUtils.SHORTCUT_KEY));
		classic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentPane(createClassicPanel());
				pack();
			}
		});
		viewMenu.add(classic);
		
		JMenuItem compressed = new JMenuItem("Compressed");
		compressed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, SwingUtils.SHORTCUT_KEY));
		compressed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentPane(createCompressedPanel());
				pack();
			}
		});
		viewMenu.add(compressed);
		
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
	
	private JPanel createClassicPanel() {
		JPanel panel = new JPanel(new GridLayout(2,2,16,16));
		{
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			panel1.add(createIconWell(
				"ict#", false,
				new MacIconWellModel.Type(MacIconSuite.ict$, 128, 128, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"ich#", false,
				new MacIconWellModel.Type(MacIconSuite.ich$, 48, 48, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"ICN#", false,
				new MacIconWellModel.Type(MacIconSuite.ICN$, 32, 32, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"ics#", false,
				new MacIconWellModel.Type(MacIconSuite.ics$, 16, 16, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"icm#", false,
				new MacIconWellModel.Type(MacIconSuite.icm$, 16, 12, true, true)
			));
			panel.add(panel1);
		}
		{
			JPanel panel4 = new JPanel();
			panel4.setLayout(new BoxLayout(panel4, BoxLayout.LINE_AXIS));
			panel4.add(createIconWell(
				"ict4", false,
				new MacIconWellModel.Type(MacIconSuite.ict4, 128, 128, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"ich4", false,
				new MacIconWellModel.Type(MacIconSuite.ich4, 48, 48, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"icl4", false,
				new MacIconWellModel.Type(MacIconSuite.icl4, 32, 32, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"ics4", false,
				new MacIconWellModel.Type(MacIconSuite.ics4, 16, 16, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"icm4", false,
				new MacIconWellModel.Type(MacIconSuite.icm4, 16, 12, true, true)
			));
			panel.add(panel4);
		}
		{
			JPanel panel8 = new JPanel();
			panel8.setLayout(new BoxLayout(panel8, BoxLayout.LINE_AXIS));
			panel8.add(createIconWell(
				"ict8", false,
				new MacIconWellModel.Type(MacIconSuite.ict8, 128, 128, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"ich8", false,
				new MacIconWellModel.Type(MacIconSuite.ich8, 48, 48, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"icl8", false,
				new MacIconWellModel.Type(MacIconSuite.icl8, 32, 32, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"ics8", false,
				new MacIconWellModel.Type(MacIconSuite.ics8, 16, 16, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"icm8", false,
				new MacIconWellModel.Type(MacIconSuite.icm8, 16, 12, true, true)
			));
			panel.add(panel8);
		}
		{
			JPanel panel32 = new JPanel();
			panel32.setLayout(new BoxLayout(panel32, BoxLayout.LINE_AXIS));
			panel32.add(createIconWell(
				"it32", false,
				new MacIconWellModel.Type(MacIconSuite.it32, 128, 128, true, true),
				new MacIconWellModel.Type(MacIconSuite.t8mk, 128, 128, false, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"ih32", false,
				new MacIconWellModel.Type(MacIconSuite.ih32, 48, 48, true, true),
				new MacIconWellModel.Type(MacIconSuite.h8mk, 16, 16, false, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"il32", false,
				new MacIconWellModel.Type(MacIconSuite.il32, 32, 32, true, true),
				new MacIconWellModel.Type(MacIconSuite.l8mk, 32, 32, false, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"is32", false,
				new MacIconWellModel.Type(MacIconSuite.is32, 16, 16, true, true),
				new MacIconWellModel.Type(MacIconSuite.s8mk, 16, 16, false, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"im32", false,
				new MacIconWellModel.Type(MacIconSuite.im32, 16, 12, true, true),
				new MacIconWellModel.Type(MacIconSuite.m8mk, 16, 16, false, true)
			));
			panel.add(panel32);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return panel;
	}
	
	private JPanel createCompressedPanel() {
		JPanel panel = new JPanel(new BorderLayout(16, 16));
		JPanel rightPanel = new JPanel(new GridLayout(0, 1, 16, 16));
		{
			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.LINE_AXIS));
			leftPanel.add(createIconWell(
				"ic09", false,
				new MacIconWellModel.Type(MacIconSuite.ic09, 512, 512, true, true)
			));
			leftPanel.add(Box.createHorizontalStrut(16));
			leftPanel.add(createIconWell(
				"ic08", false,
				new MacIconWellModel.Type(MacIconSuite.ic08, 256, 256, true, true)
			));
			leftPanel.add(Box.createHorizontalStrut(16));
			leftPanel.add(createIconWell(
				"ic07", false,
				new MacIconWellModel.Type(MacIconSuite.ic07, 128, 128, true, true)
			));
			panel.add(leftPanel, BorderLayout.LINE_START);
		}
		{
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
			topPanel.add(createIconWell(
				"ic06", false,
				new MacIconWellModel.Type(MacIconSuite.ic06, 64, 64, true, true)
			));
			topPanel.add(Box.createHorizontalStrut(16));
			topPanel.add(createIconWell(
				"ic05", false,
				new MacIconWellModel.Type(MacIconSuite.ic05, 32, 32, true, true)
			));
			topPanel.add(Box.createHorizontalStrut(16));
			topPanel.add(createIconWell(
				"ic04", false,
				new MacIconWellModel.Type(MacIconSuite.ic04, 16, 16, true, true)
			));
			rightPanel.add(topPanel);
		}
		{
			JPanel botPanel = new JPanel();
			botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.LINE_AXIS));
			botPanel.add(createIconWell(
				"icp6", false,
				new MacIconWellModel.Type(MacIconSuite.icp6, 64, 64, true, true)
			));
			botPanel.add(Box.createHorizontalStrut(16));
			botPanel.add(createIconWell(
				"icp5", false,
				new MacIconWellModel.Type(MacIconSuite.icp5, 32, 32, true, true)
			));
			botPanel.add(Box.createHorizontalStrut(16));
			botPanel.add(createIconWell(
				"icp4", false,
				new MacIconWellModel.Type(MacIconSuite.icp4, 16, 16, true, true)
			));
			rightPanel.add(botPanel);
		}
		panel.add(rightPanel, BorderLayout.CENTER);
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
			tl.setFont(tl.getFont().deriveFont((types[0].height < 16) ? 9f : 12f));
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
