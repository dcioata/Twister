/*
   File: Log.java ; This file is part of Twister.

   Copyright (C) 2012 , Luxoft

   Authors: Andrei Costachi <acostachi@luxoft.com>
                
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.io.FileInputStream;
import java.io.FileWriter;
import javax.swing.JFileChooser;
import sun.misc.BASE64Decoder;
import javax.swing.text.DefaultCaret;

public class Log extends JPanel {

	private static final long serialVersionUID = 1L;
	private long size;
	private int line;
	public String log;
	public JTextArea textarea;
	public JScrollPane scroll;
	private long length = 0;
	private long response = 0;
	public JPanel container;
	private int lastIndexFound = -1;

	public Log(int x, int y, final String log) {
		this.log = log;
		size = 0;
		line = 0;
		textarea = new JTextArea();
		((DefaultCaret) textarea.getCaret())
				.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll = new JScrollPane(textarea);
		scroll.setSize(450, 600);
		scroll.setVerticalScrollBarPolicy(22);
		container = new JPanel();
		container.setLayout(new BorderLayout());
		JPanel findpanel = new JPanel();
		JLabel findlabel = new JLabel("Search: ");
		findpanel.add(findlabel);
		JButton next = new JButton("Next");
		next.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		final JTextField find = new JTextField();
		next.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				String toFind = find.getText();
				Element paragraph = textarea.getDocument()
						.getDefaultRootElement();
				int contentCount = paragraph.getElementCount();
				HighlightPainter myHighlighter = new MyHighlightPainter(
						Color.RED);
				for (int i = lastIndexFound + 1; i < contentCount; i++) {
					Element e = paragraph.getElement(i);
					int rangeStart = e.getStartOffset();
					int rangeEnd = e.getEndOffset();
					try {
						if (textarea.getText(rangeStart, rangeEnd - rangeStart)
								.indexOf(toFind) != -1) {
							lastIndexFound = i;
							int index = textarea.getText(rangeStart,
									rangeEnd - rangeStart).indexOf(toFind);
							textarea.setCaretPosition(0);
							textarea.setCaretPosition(rangeStart);
							Highlighter hilite = textarea.getHighlighter();
							hilite.removeAllHighlights();
							hilite.addHighlight(rangeStart + index, rangeStart
									+ index + toFind.length(), myHighlighter);
							hilite.paint(textarea.getGraphics());
							break;
						}
					} catch (BadLocationException ex) {
					}
					if (i == (contentCount - 1)) {
						i = -1;
					}
					if (i == (lastIndexFound - 1)) {
						break;
					}
				}
			}
		});
		find.setPreferredSize(new Dimension(150, 25));
		findpanel.add(find);
		JButton prev = new JButton("Prev");
		prev.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		prev.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				String toFind = find.getText();
				Element paragraph = textarea.getDocument()
						.getDefaultRootElement();
				int contentCount = paragraph.getElementCount();
				HighlightPainter myHighlighter = new MyHighlightPainter(
						Color.RED);
				if (lastIndexFound == -1) {
					lastIndexFound = 0;
				}
				for (int i = lastIndexFound - 1; i >= -1; i--) {
					if (i == -1) {
						i = contentCount - 1;
					}
					Element e = paragraph.getElement(i);
					int rangeStart = e.getStartOffset();
					int rangeEnd = e.getEndOffset();
					try {
						if (textarea.getText(rangeStart, rangeEnd - rangeStart)
								.indexOf(toFind) != -1) {
							lastIndexFound = i;
							int index = textarea.getText(rangeStart,
									rangeEnd - rangeStart).indexOf(toFind);
							textarea.setCaretPosition(0);
							textarea.setCaretPosition(rangeStart);
							Highlighter hilite = textarea.getHighlighter();
							hilite.removeAllHighlights();
							hilite.addHighlight(rangeStart + index, rangeStart
									+ index + toFind.length(), myHighlighter);
							hilite.paint(textarea.getGraphics());
							break;
						}
					} catch (BadLocationException ex) {
						i = contentCount - 1;
					}
					if (i == (lastIndexFound + 1)) {
						break;
					}
				}
			}
		});
		JButton savelog = new JButton("Save log");
		savelog.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		savelog.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				saveLog();
			}
		});
		JButton clearlog = new JButton("Clear log");
		clearlog.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		clearlog.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				clearLog();
			}
		});
		findpanel.add(next);
		findpanel.add(prev);
		findpanel.add(savelog);
		findpanel.add(clearlog);
		container.add(scroll, BorderLayout.CENTER);
		container.add(findpanel, BorderLayout.PAGE_END);
		textarea.setEditable(false);
		textarea.setBackground(Color.BLACK);
		textarea.setForeground(Color.WHITE);
		textarea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		new Thread() {

			public void run() {
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				try {
					config.setServerURL(new URL("http://" + Repository.host
							+ ":" + Repository.getCentralEnginePort()));
				} catch (Exception e) {
					System.out.println("Could not connect to "
							+ Repository.host + " on port "
							+ Repository.getCentralEnginePort());
				}
				XmlRpcClient client = new XmlRpcClient();
				client.setConfig(config);
				String result;
				while (Repository.run) {
					try {
						Thread.sleep(1500);
						if (response == length) {
							result = client.execute("getLogFile", new Object[] {
									"0", "0", log })
									+ "";
							response = Long.parseLong(result);
						}
						if (response > length) {
							result = client.execute("getLogFile", new Object[] {
									"1", length + "", log })
									+ "";
							readText(result);
							length = response;
						} else if (response < length) {
							response = 0;
							length = 0;
							clearScreen();
							for (int i = 0; i < Repository.getSuiteNr(); i++) {
								for (int j = 0; j < Repository.getSuita(i)
										.getSubItemsNr(); j++) {
									Repository.getTestSuita(i).getSubItem(j)
											.getSubItem(0).setValue("");
									Repository.frame.mainpanel.p2.sc.g
											.repaint();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						clearScreen();
						textarea.append("This log has the folowing error: "
								+ e.toString());
					}
				}
			}
		}.start();
	}

	public void clearScreen() {
		textarea.setText("");
		length = 0;
		response = 0;
	}

	public void saveLog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose Location");
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(Repository.frame) == JFileChooser.APPROVE_OPTION) {
			File theone = new File(chooser.getSelectedFile() + "");
			try {
				theone.createNewFile();
				FileWriter writer = new FileWriter(theone);
				writer.write(textarea.getText());
				writer.flush();
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public void clearLog() {
		clearScreen();
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL("http://" + Repository.host + ":"
					+ Repository.getCentralEnginePort()));
		} catch (Exception e) {
			System.out.println("Could not connect to " + Repository.host
					+ " on port " + Repository.getCentralEnginePort());
		}
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		try {
			String result = client.execute("resetLog", new Object[] { log })
					+ "";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Item getSubTC(Item item, String name) {
		String name1 = item.getName().split("/")[item.getName().split("/").length - 1]
				.toLowerCase();
		if (item.getType() == 1 && name1.equals(name)) {
			return item;
		}
		if (item.getType() == 2) {
			for (int i = 0; i < item.getSubItemsNr(); i++) {
				Item temp = getSubTC(item.getSubItem(i), name);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;
	}

	public void readText(String content) {
		BASE64Decoder base64 = new BASE64Decoder();
		byte mydata[] = null;
		try {
			mydata = base64.decodeBuffer(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		textarea.append(new String(mydata));
		scroll.getHorizontalScrollBar().setValue(0);
	}
}

class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

	public MyHighlightPainter(Color color) {
		super(color);
	}
}
