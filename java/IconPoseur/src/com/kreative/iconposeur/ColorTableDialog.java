package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class ColorTableDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private ColorTablePanel panel;
	private JButton okButton;
	private JButton cancelButton;
	private JButton eisButton;
	private JButton paintButton;
	private JButton websafeButton;
	private JButton macButton;
	private boolean confirmed;
	
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
		eisButton = new JButton("Eis");
		paintButton = new JButton("Paint");
		websafeButton = new JButton("Web-Safe");
		macButton = new JButton("Mac OS");
		
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(eisButton);
		buttonPanel.add(paintButton);
		buttonPanel.add(websafeButton);
		buttonPanel.add(macButton);
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(buttonPanel, BorderLayout.PAGE_START);
		JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
		mainPanel.add(panel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.LINE_END);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		setContentPane(mainPanel);
		SwingUtils.setDefaultButton(getRootPane(), okButton);
		SwingUtils.setCancelButton(getRootPane(), cancelButton);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		okButton.requestFocusInWindow();
		
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
		eisButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setColorTable(ColorPalettes.createWindowsEis());
			}
		});
		paintButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setColorTable(ColorPalettes.createWindowsPaint());
			}
		});
		websafeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setColorTable(ColorPalettes.createWindowsWebSafe());
			}
		});
		macButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.setColorTable(ColorPalettes.createMacintosh8());
			}
		});
	}
	
	public int[] showDialog() {
		confirmed = false;
		setVisible(true);
		return confirmed ? panel.getColorTable() : null;
	}
}
