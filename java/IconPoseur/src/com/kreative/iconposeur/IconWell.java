package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import com.kreative.iconposeur.datatransfer.ImageSelection;

public class IconWell extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private final List<IconWellListener> listeners;
	private final IconWellModel model;
	private final boolean retina;
	private Image image;
	
	public IconWell(IconWellModel model) {
		this.listeners = new ArrayList<IconWellListener>();
		this.model = model;
		this.retina = false;
		this.image = model.getImage();
		createListeners();
	}
	
	public IconWell(IconWellModel model, boolean retina) {
		this.listeners = new ArrayList<IconWellListener>();
		this.model = model;
		this.retina = retina;
		this.image = model.getImage();
		createListeners();
	}
	
	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		if (image == null) {
			this.model.removeImage();
			this.image = null;
		} else {
			this.model.setImage(this, image);
			this.image = model.getImage();
		}
		for (IconWellListener l : listeners) l.iconChanged(this);
		repaint();
	}
	
	public boolean cut() {
		if (image == null) return false;
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		ImageSelection sel = new ImageSelection(image);
		cb.setContents(sel, sel);
		setImage(null);
		return true;
	}
	
	public boolean copy() {
		if (image == null) return false;
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		ImageSelection sel = new ImageSelection(image);
		cb.setContents(sel, sel);
		return true;
	}
	
	public boolean paste() {
		try {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			Image image = (Image)cb.getData(DataFlavor.imageFlavor);
			setImage(image);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void addIconWellListener(IconWellListener listener) {
		listeners.add(listener);
	}
	
	public void removeIconWellListener(IconWellListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public Dimension getMinimumSize() {
		Dimension d = model.getImageSize();
		Insets i = getInsets();
		int w = (retina ? (d.width / 2) : d.width) + 8 + i.left + i.right;
		int h = (retina ? (d.height / 2) : d.height) + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = model.getImageSize();
		Insets i = getInsets();
		int w = (retina ? (d.width / 2) : d.width) + 8 + i.left + i.right;
		int h = (retina ? (d.height / 2) : d.height) + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getMaximumSize() {
		Dimension d = model.getImageSize();
		Insets i = getInsets();
		int w = (retina ? (d.width / 2) : d.width) + 8 + i.left + i.right;
		int h = (retina ? (d.height / 2) : d.height) + 8 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		int t = isFocusOwner() ? 3 : 1;
		g.setColor(Color.darkGray);
		g.fillRect(i.left, i.top, w, t);
		g.fillRect(i.left, i.top, t, h);
		g.fillRect(i.left, i.top + h - t, w, t);
		g.fillRect(i.left + w - t, i.top, t, h);
		if (image != null) {
			Dimension d = model.getImageSize();
			int iw = (retina ? (d.width / 2) : d.width);
			int ih = (retina ? (d.height / 2) : d.height);
			int ix = i.left + (w - iw) / 2;
			int iy = i.top + (h - ih) / 2;
			g.drawImage(image, ix, iy, iw, ih, null);
		}
	}
	
	private void createListeners() {
		setFocusable(true);
		setRequestFocusEnabled(true);
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				repaint();
			}
			@Override
			public void focusLost(FocusEvent e) {
				repaint();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow();
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE) {
					setImage(null);
				}
			}
		});
		
		ActionMap actions = getActionMap();
		actions.put("Cut", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) { cut(); }
		});
		actions.put("Copy", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) { copy(); }
		});
		actions.put("Paste", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) { paste(); }
		});
		actions.put("Clear", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override public void actionPerformed(ActionEvent e) { setImage(null); }
		});
		
		new DropTarget(this, new DropTargetListener() {
			public void dragEnter(DropTargetDragEvent e) {}
			public void dragExit(DropTargetEvent e) {}
			public void dragOver(DropTargetDragEvent e) {}
			public void dropActionChanged(DropTargetDragEvent e) {}
			public void drop(DropTargetDropEvent e) {
				e.acceptDrop(e.getDropAction());
				Transferable t = e.getTransferable();
				try {
					if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
						Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
						if (image != null) {
							setImage(image);
							e.dropComplete(true);
							return;
						}
					}
					if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						List<?> list = (List<?>)t.getTransferData(DataFlavor.javaFileListFlavor);
						if (list != null && list.size() == 1) {
							BufferedImage image = ImageIO.read((File)list.get(0));
							if (image != null) {
								setImage(image);
								e.dropComplete(true);
								return;
							}
						}
					}
				} catch (Exception e2) {}
				e.dropComplete(false);
			}
		});
	}
}
