package com.kreative.iconposeur;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import com.kreative.iconposeur.MainMenuBar.CloseMenuItem;
import com.kreative.iconposeur.MainMenuBar.ExitMenuItem;
import com.kreative.iconposeur.MainMenuBar.NewIcnsMenuItem;
import com.kreative.iconposeur.MainMenuBar.NewIcoMenuItem;
import com.kreative.iconposeur.MainMenuBar.OpenMenuItem;

public class IcnsListFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final List<MacIconSuite> icons = new ArrayList<MacIconSuite>();
	private final List<BufferedImage> images = new ArrayList<BufferedImage>();
	private final List<String> labels = new ArrayList<String>();
	
	public IcnsListFrame(String title, Map<?,?> icons) {
		super(title);
		for (Map.Entry<?,?> e : icons.entrySet()) {
			if (e.getValue() instanceof MacIconSuite) {
				MacIconSuite icns = (MacIconSuite)e.getValue();
				this.icons.add(icns);
				this.images.add(resizeImage(icns.getImage(), 64, 64));
				this.labels.add(e.getKey().toString());
			} else if (e.getValue() instanceof byte[]) {
				try {
					ByteArrayInputStream in = new ByteArrayInputStream((byte[])e.getValue());
					MacIconSuite icns = new MacIconSuite();
					icns.read(new DataInputStream(in));
					in.close();
					this.icons.add(icns);
					this.images.add(resizeImage(icns.getImage(), 64, 64));
					this.labels.add(e.getKey().toString());
				} catch (IOException ex) {}
			}
		}
		build();
	}
	
	private void build() {
		final JList list = new JList(new MyListModel());
		list.setCellRenderer(new MyListCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(0);
		list.addMouseListener(new MyListMouseListener(list));
		list.addKeyListener(new MyListKeyListener(list));
		final JScrollPane pane = new JScrollPane(
			list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		setJMenuBar(new MyMenuBar());
		setContentPane(pane);
		setSize(560, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private class MyMenuBar extends JMenuBar {
		private static final long serialVersionUID = 1L;
		public MyMenuBar() {
			JMenu fileMenu = new JMenu("File");
			fileMenu.add(new NewIcnsMenuItem());
			fileMenu.add(new NewIcoMenuItem());
			fileMenu.add(new OpenMenuItem());
			fileMenu.add(new CloseMenuItem(IcnsListFrame.this));
			if (!SwingUtils.IS_MAC_OS) {
				fileMenu.addSeparator();
				fileMenu.add(new ExitMenuItem());
			}
			add(fileMenu);
		}
	}
	
	private class MyListModel extends DefaultListModel {
		private static final long serialVersionUID = 1L;
		public int getSize() {
			return icons.size();
		}
		public Object getElementAt(int index) {
			return icons.get(index);
		}
	}
	
	private class MyListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		public Component getListCellRendererComponent(JList l, Object v, int i, boolean s, boolean f) {
			Component c = super.getListCellRendererComponent(l, v, i, s, f);
			if (c instanceof JLabel) {
				JLabel label = (JLabel)c;
				label.setIcon(new ImageIcon(images.get(i)));
				label.setText(labels.get(i));
				label.setHorizontalTextPosition(JLabel.CENTER);
				label.setVerticalTextPosition(JLabel.BOTTOM);
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setVerticalAlignment(JLabel.BOTTOM);
				label.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			}
			return c;
		}
	}
	
	private class MyListMouseListener extends MouseAdapter {
		private final JList list;
		public MyListMouseListener(JList list) { this.list = list; }
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				int i = list.getSelectedIndex();
				if (i >= 0) new IcnsFrame(icons.get(i)).setVisible(true);
			}
		}
	}
	
	private class MyListKeyListener extends KeyAdapter {
		private final JList list;
		public MyListKeyListener(JList list) { this.list = list; }
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				int i = list.getSelectedIndex();
				if (i >= 0) new IcnsFrame(icons.get(i)).setVisible(true);
			}
		}
	}
	
	private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
		if (image.getWidth() == width && image.getHeight() == height) return image;
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resized;
	}
}
