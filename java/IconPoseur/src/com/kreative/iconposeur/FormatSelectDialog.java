package com.kreative.iconposeur;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.kreative.applefile.Format;

public class FormatSelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private Format selectedFormat;
	
	public FormatSelectDialog(Dialog parent, String title, String label, String cancel, String ok) {
		super(parent, title);
		setModal(true);
		build(label, cancel, ok);
	}
	
	public FormatSelectDialog(Frame parent, String title, String label, String cancel, String ok) {
		super(parent, title);
		setModal(true);
		build(label, cancel, ok);
	}
	
	public FormatSelectDialog(Window parent, String title, String label, String cancel, String ok) {
		super(parent, title);
		setModal(true);
		build(label, cancel, ok);
	}
	
	private void build(String label, String cancel, String ok) {
		final Format[] formats = Format.values();
		final JComboBox format = new JComboBox(formats);
		format.setEditable(false);
		format.setMaximumRowCount(formats.length);
		format.setSelectedItem(SwingUtils.IS_MAC_OS ? Format.NATIVE : Format.APPLEDOUBLE_MACOSX);
		
		final JButton cancelButton = new JButton(cancel);
		final JButton okButton = new JButton(ok);
		
		final JPanel buttonPanel1 = new JPanel(new GridLayout(1, 0, 4, 4));
		buttonPanel1.add(cancelButton);
		buttonPanel1.add(okButton);
		
		final JPanel buttonPanel2 = new JPanel(new FlowLayout());
		buttonPanel2.add(buttonPanel1);
		
		final JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
		mainPanel.add(new JLabel(label), BorderLayout.PAGE_START);
		mainPanel.add(format, BorderLayout.CENTER);
		mainPanel.add(buttonPanel2, BorderLayout.PAGE_END);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		setContentPane(mainPanel);
		SwingUtils.setCancelButton(getRootPane(), cancelButton);
		SwingUtils.setDefaultButton(getRootPane(), okButton);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		okButton.requestFocusInWindow();
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedFormat = (Format)format.getSelectedItem();
				dispose();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedFormat = null;
				dispose();
			}
		});
	}
	
	public Format showDialog() {
		selectedFormat = null;
		setVisible(true);
		return selectedFormat;
	}
}
