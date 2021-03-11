package com.kreative.iconposeur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

public class ColorTablePanel extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private int rows;
	private int columns;
	private int[] colorTable;
	private int selectedIndex;
	private final List<ColorTableListener> listeners;
	
	public ColorTablePanel(int rows, int columns, int[] colorTable) {
		this.rows = rows;
		this.columns = columns;
		this.colorTable = colorTable;
		this.selectedIndex = -1;
		this.listeners = new ArrayList<ColorTableListener>();
		setFocusable(true);
		setRequestFocusEnabled(true);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addKeyListener(keyListener);
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public void setRowCount(int rows) {
		this.rows = rows;
		this.repaint();
		for (ColorTableListener l : listeners) l.dimensionsChanged(this);
	}
	
	public int getColumnCount() {
		return columns;
	}
	
	public void setColumnCount(int columns) {
		this.columns = columns;
		this.repaint();
		for (ColorTableListener l : listeners) l.dimensionsChanged(this);
	}
	
	public int[] getColorTable() {
		return colorTable;
	}
	
	public void setColorTable(int[] colorTable) {
		this.colorTable = colorTable;
		this.repaint();
		for (ColorTableListener l : listeners) l.colorTableChanged(this);
	}
	
	public int getColor(int index) {
		return colorTable[index];
	}
	
	public void setColor(int index, int color) {
		this.colorTable[index] = color;
		this.repaint();
		for (ColorTableListener l : listeners) l.colorTableChanged(this);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
		this.repaint();
		for (ColorTableListener l : listeners) l.selectionChanged(this);
	}
	
	public void addColorTableListener(ColorTableListener listener) {
		listeners.add(listener);
	}
	
	public void removeColorTableListener(ColorTableListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public Dimension getMinimumSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getMaximumSize() {
		Insets i = getInsets();
		int w = columns * 16 + i.left + i.right;
		int h = rows * 16 + i.top + i.bottom;
		return new Dimension(w, h);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (colorTable == null) return;
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		for (int idx = 0, y = 0; y < rows; y++) {
			int gy = (h * y / rows);
			int gh = (h * (y + 1) / rows) - gy;
			gy += i.top;
			for (int x = 0; x < columns; x++, idx++) {
				int gx = (w * x / columns);
				int gw = (w * (x + 1) / columns) - gx;
				gx += i.left;
				if (idx < colorTable.length) {
					if (idx == selectedIndex) {
						g.setColor(Color.black);
						g.fillRect(gx, gy, gw, gh);
					}
					g.setColor(new Color(colorTable[idx], true));
					g.fillRect(gx + 1, gy + 1, gw - 2, gh - 2);
				}
			}
		}
	}
	
	public int getIndexAt(int x, int y) {
		Insets i = getInsets();
		int w = getWidth() - i.left - i.right;
		int h = getHeight() - i.top - i.bottom;
		x -= i.left; if (x < 0 || x >= w) return -1;
		y -= i.top; if (y < 0 || y >= h) return -1;
		return (rows * y / h) * columns + (columns * x / w);
	}
	
	public boolean editColor(int index) {
		if (index >= 0 && index < colorTable.length) {
			Color c = JColorChooser.showDialog(
				ColorTablePanel.this,
				("Color #" + index),
				new Color(colorTable[index], true)
			);
			if (c != null) {
				setColor(index, c.getRGB());
				return true;
			}
		}
		return false;
	}
	
	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			requestFocusInWindow();
			setSelectedIndex(getIndexAt(e.getX(), e.getY()));
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			setSelectedIndex(getIndexAt(e.getX(), e.getY()));
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				editColor(getIndexAt(e.getX(), e.getY()));
			}
		}
	};
	
	private final KeyAdapter keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					if (selectedIndex < 0) {
						setSelectedIndex(colorTable.length - 1);
					} else if (selectedIndex > 0) {
						setSelectedIndex(selectedIndex - 1);
					}
					e.consume();
					break;
				case KeyEvent.VK_RIGHT:
					if (selectedIndex < 0) {
						setSelectedIndex(0);
					} else if (selectedIndex < (colorTable.length - 1)) {
						setSelectedIndex(selectedIndex + 1);
					}
					e.consume();
					break;
				case KeyEvent.VK_UP:
					if (selectedIndex < 0) {
						setSelectedIndex(colorTable.length - 1);
					} else if (selectedIndex > 0) {
						setSelectedIndex(Math.max(0, selectedIndex - columns));
					}
					e.consume();
					break;
				case KeyEvent.VK_DOWN:
					if (selectedIndex < 0) {
						setSelectedIndex(0);
					} else if (selectedIndex < (colorTable.length - 1)) {
						setSelectedIndex(Math.min(colorTable.length - 1, selectedIndex + columns));
					}
					e.consume();
					break;
				case KeyEvent.VK_HOME:
					setSelectedIndex(0);
					e.consume();
					break;
				case KeyEvent.VK_END:
					setSelectedIndex(colorTable.length - 1);
					e.consume();
					break;
				case KeyEvent.VK_BACK_QUOTE:
				case KeyEvent.VK_CLEAR:
					setSelectedIndex(-1);
					e.consume();
					break;
				case KeyEvent.VK_BACK_SPACE:
				case KeyEvent.VK_DELETE:
					if (selectedIndex >= 0) setColor(selectedIndex, 0);
					e.consume();
					break;
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_INSERT:
					if (selectedIndex >= 0) editColor(selectedIndex);
					e.consume();
					break;
			}
		}
	};
}
