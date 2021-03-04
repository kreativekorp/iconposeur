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

public class IcoFrame extends JFrame implements SaveInterface {
	private static final long serialVersionUID = 1L;
	
	private final WinIconDir ico;
	private final IconWellGroup wells;
	private final JPanel standardPanel;
	private final JPanel advancedPanel;
	private final JMenu viewMenu;
	private File file;
	private boolean changed;
	
	public IcoFrame() {
		this.ico = new WinIconDir();
		this.wells = new IconWellGroup();
		this.standardPanel = createStandardPanel();
		this.advancedPanel = createAdvancedPanel();
		this.viewMenu = createViewMenu();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(boolean cursor) {
		this.ico = new WinIconDir(cursor);
		this.wells = new IconWellGroup();
		this.standardPanel = createStandardPanel();
		this.advancedPanel = createAdvancedPanel();
		this.viewMenu = createViewMenu();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(byte[] data) throws IOException {
		this.ico = new WinIconDir();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ico.read(in);
		in.close();
		this.wells = new IconWellGroup();
		this.standardPanel = createStandardPanel();
		this.advancedPanel = createAdvancedPanel();
		this.viewMenu = createViewMenu();
		this.file = null;
		this.changed = false;
		build();
	}
	
	public IcoFrame(File file) throws IOException {
		this.ico = new WinIconDir();
		FileInputStream in = new FileInputStream(file);
		ico.read(in);
		in.close();
		this.wells = new IconWellGroup();
		this.standardPanel = createStandardPanel();
		this.advancedPanel = createAdvancedPanel();
		this.viewMenu = createViewMenu();
		this.file = file;
		this.changed = false;
		build();
	}
	
	private void build() {
		setContentPane(standardPanel);
		setJMenuBar(new MainMenuBar(this, this, viewMenu));
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
				setContentPane(standardPanel);
				pack();
			}
		});
		viewMenu.add(standard);
		
		JMenuItem advanced = new JMenuItem("Advanced");
		advanced.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, SwingUtils.SHORTCUT_KEY));
		advanced.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentPane(advancedPanel);
				pack();
			}
		});
		viewMenu.add(advanced);
		
		return viewMenu;
	}
	
	private JPanel createStandardPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(createIconWell(
			"256",
			new WinIconWellModel.Size(256, 256, null, null, new int[0], true, false, true, true),
			new WinIconWellModel.Size(256, 256, 32, true, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"48",
			new WinIconWellModel.Size(48, 48, null, null, new int[0], true, false, true, true),
			new WinIconWellModel.Size(48, 48, 32, false, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"32",
			new WinIconWellModel.Size(32, 32, null, null, new int[0], true, false, true, true),
			new WinIconWellModel.Size(32, 32, 32, false, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"24",
			new WinIconWellModel.Size(24, 24, null, null, new int[0], true, false, true, true),
			new WinIconWellModel.Size(24, 24, 32, false, new int[0], false, true, false, false)
		));
		panel.add(Box.createHorizontalStrut(16));
		panel.add(createIconWell(
			"16",
			new WinIconWellModel.Size(16, 16, null, null, new int[0], true, false, true, true),
			new WinIconWellModel.Size(16, 16, 32, false, new int[0], false, true, false, false)
		));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return panel;
	}
	
	private JPanel createAdvancedPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2,16,16));
		{
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			panel1.add(createIconWell(
				"128 (Black & White)",
				new WinIconWellModel.Size(128, 128, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel1.add(Box.createHorizontalStrut(16));
			panel1.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 1, false, ColorPalettes.createBlackToWhite(1), true, true, true, true)
			));
			panel.add(panel1);
		}
		{
			JPanel panel4 = new JPanel();
			panel4.setLayout(new BoxLayout(panel4, BoxLayout.LINE_AXIS));
			panel4.add(createIconWell(
				"128 (16 Colors)",
				new WinIconWellModel.Size(128, 128, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel4.add(Box.createHorizontalStrut(16));
			panel4.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 4, false, ColorPalettes.createWindows4(), true, true, true, true)
			));
			panel.add(panel4);
		}
		{
			JPanel panel8 = new JPanel();
			panel8.setLayout(new BoxLayout(panel8, BoxLayout.LINE_AXIS));
			panel8.add(createIconWell(
				"128 (256 Colors)",
				new WinIconWellModel.Size(128, 128, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel8.add(Box.createHorizontalStrut(16));
			panel8.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 8, false, ColorPalettes.createWindows8(), true, true, true, true)
			));
			panel.add(panel8);
		}
		{
			JPanel panel16 = new JPanel();
			panel16.setLayout(new BoxLayout(panel16, BoxLayout.LINE_AXIS));
			panel16.add(createIconWell(
				"128 (High Color)",
				new WinIconWellModel.Size(128, 128, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 16, false, new int[0], true, true, true, true)
			));
			panel16.add(Box.createHorizontalStrut(16));
			panel16.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 16, false, new int[0], true, true, true, true)
			));
			panel.add(panel16);
		}
		{
			JPanel panel32 = new JPanel();
			panel32.setLayout(new BoxLayout(panel32, BoxLayout.LINE_AXIS));
			panel32.add(createIconWell(
				"128 (True Color)",
				new WinIconWellModel.Size(128, 128, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 32, false, new int[0], true, true, true, true)
			));
			panel32.add(Box.createHorizontalStrut(16));
			panel32.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 32, false, new int[0], true, true, true, true)
			));
			panel.add(panel32);
		}
		{
			JPanel panelp = new JPanel();
			panelp.setLayout(new BoxLayout(panelp, BoxLayout.LINE_AXIS));
			panelp.add(createIconWell(
				"128 (PNG)",
				new WinIconWellModel.Size(128, 128, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"96",
				new WinIconWellModel.Size(96, 96, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"64",
				new WinIconWellModel.Size(64, 64, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"48",
				new WinIconWellModel.Size(48, 48, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"32",
				new WinIconWellModel.Size(32, 32, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"24",
				new WinIconWellModel.Size(24, 24, 32, true, new int[0], true, true, true, true)
			));
			panelp.add(Box.createHorizontalStrut(16));
			panelp.add(createIconWell(
				"16",
				new WinIconWellModel.Size(16, 16, 32, true, new int[0], true, true, true, true)
			));
			panel.add(panelp);
		}
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		return panel;
	}
	
	private JPanel createIconWell(String title, WinIconWellModel.Size... sizes) {
		IconWellModel model = new WinIconWellModel(ico, sizes);
		IconWell well = new IconWell(model);
		well.addIconWellListener(iconWellListener);
		wells.add(well);
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
