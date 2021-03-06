/*
   File: Repository.java ; This file is part of Twister.

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

import java.applet.Applet;
import java.util.ArrayList;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.awt.Image;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URL;

public class Repository {

	private static ArrayList<Item> suite = new ArrayList<Item>();
	private static ArrayList<Item> suitetest = new ArrayList<Item>();
	private static String bar = System.getProperty("file.separator");
	public static ArrayList<String> logs = new ArrayList<String>();
	public static ArrayList<Integer> editable;
	public static String[] columnNames;
	public static Fereastra frame;
	public static ChannelSftp c;
	public static String temp, USERHOME, REMOTECONFIGDIRECTORY, HTTPSERVERPORT,
			CENTRALENGINEPORT, RESOURCEALLOCATORPORT, REMOTEDATABASECONFIGPATH,
			REMOTEDATABASECONFIGFILE, REMOTEEMAILCONFIGPATH,
			REMOTEEMAILCONFIGFILE, CONFIGDIRECTORY, USERSDIRECTORY,
			XMLDIRECTORY, TCDIRECTORY, TESTSUITEPATH, LOGSPATH, XMLREMOTEDIR,
			REMOTEUSERSDIRECTORY, REMOTEEPIDDIR, REMOTEHARDWARECONFIGDIRECTORY;
	public static Image testbedicon, porticon, suitaicon, tcicon, propicon,
			failicon, passicon, playicon, stopicon, pauseicon, background,
			notexecicon, pendingicon, skipicon, stoppedicon, timeouticon,
			waiticon, workingicon, moduleicon, deviceicon, addsuitaicon,
			removeicon, vlcclient, vlcserver, switche, flootw, rack150,
			rack151, rack152, switche2, inicon, outicon, baricon;
	public static boolean run = true;
	public static boolean applet;
	public static IntroScreen intro;
	public static String user, host, password;
	private static ArrayList<String[]> databaseUserFields = new ArrayList<String[]>();
	public static int LABEL = 0;
	public static int ID = 1;
	public static int SELECTED = 2;
	public static int MANDATORY = 3;
	public static int ELEMENTSNR = 4;

	public static void initialize(final boolean applet, String host,
			Applet container) {
		try {
			File g = File.createTempFile("tmp", "");
			temp = g.getParent();
			g.delete();
			File g1 = new File(temp + bar + host);
			g1.mkdir();
			temp = g1.getCanonicalPath();
		} catch (Exception e) {
			System.out.println("Could not retrieve Temp directory for this OS");
			e.printStackTrace();
		}
		System.out
				.println("Temp directory where Twister Directory is created: "
						+ temp);
		File file = new File(Repository.temp + bar + "Twister");
		if (file.exists()) {
			if (Fereastra.deleteTemp(file)) {
				System.out.println(Repository.temp + bar
						+ "Twister deleted successfull");
			} else {
				System.out.println("Could not delete: " + Repository.temp + bar
						+ "Twister");
			}
		}
		Repository.host = host;
		System.out.println("Setting sftp server to :" + host);
		intro = new IntroScreen();
		intro.setVisible(true);

		intro.text = "Started initialization";
		intro.repaint();
		Repository.applet = applet;
		if (applet) {
			System.out.println("Twister running from applet");
		} else {
			System.out.println("Twister running from Main");
		}
		try {
			if (!applet) {
				InputStream in;
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "background.png");
				background = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "vlcclient.png");
				vlcclient = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "vlcserver.png");
				vlcserver = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "switch.png");
				switche = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "twisterfloodlight.png");
				flootw = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "150.png");
				rack150 = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "151.png");
				rack151 = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "152.png");
				rack152 = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "switch.jpg");
				switche2 = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "in.png");
				inicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "out.png");
				outicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "bar.png");
				baricon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "port.png");
				porticon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "deleteicon.png");
				removeicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "addsuita.png");
				addsuitaicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "device.png");
				deviceicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "module.png");
				moduleicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "tc.png");
				Repository.tcicon = new ImageIcon(ImageIO.read(in)).getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "suita.png");
				Repository.suitaicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "prop.png");
				Repository.propicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "fail.png");
				Repository.failicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "pass.png");
				Repository.passicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "stop.png");
				Repository.stopicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "play.png");
				Repository.playicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "notexec.png");
				Repository.notexecicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "pending.png");
				Repository.pendingicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "skip.png");
				Repository.skipicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "stopped.png");
				Repository.stoppedicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "timeout.png");
				Repository.timeouticon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "waiting.png");
				Repository.waiticon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "working.png");
				Repository.workingicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "pause.png");
				Repository.pauseicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in = Repository.class.getResourceAsStream("Icons" + bar
						+ "testbed.png");
				Repository.testbedicon = new ImageIcon(ImageIO.read(in))
						.getImage();
				in.close();
			}
			if (userpassword()) {
				System.out.println("Authentication succeeded");
				if (new File(temp + bar + "Twister").mkdir()) {
					System.out.println(temp + bar + "Twister"
							+ " folder successfully created");
				} else {
					System.out.println("Could not create " + temp + bar
							+ "Twister" + " folder");
				}
				if (new File(temp + bar + "Twister" + bar + "HardwareConfig")
						.mkdir()) {
					System.out.println(temp + bar + "Twister" + bar
							+ "HardwareConfig folder successfully created");
				} else {
					System.out.println("Could not create " + temp + bar
							+ "Twister" + bar + "HardwareConfig folder");
				}
				if (new File(temp + bar + "Twister" + bar + "XML").mkdir()) {
					System.out.println(temp + bar + "Twister" + bar
							+ "XML folder successfully created");
				} else {
					System.out.println("Could not create " + temp + bar
							+ "Twister" + bar + "XML folder");
				}
				if (new File(temp + bar + "Twister" + bar + "Users").mkdir()) {
					System.out.println(temp + bar + "Twister" + bar
							+ "Users folder successfully created");
				} else {
					System.out.println("Could not create " + temp + bar
							+ "Twister" + bar + "Users folder");
				}
				if (new File(temp + bar + "Twister" + bar + "config").mkdir()) {
					System.out.println(temp + bar + "Twister" + bar
							+ "config folder successfully created");
				} else {
					System.out.println("Could not create " + temp + bar
							+ "Twister" + bar + "config folder");
				}
				USERSDIRECTORY = Repository.temp + bar + "Twister" + bar
						+ "Users";
				TCDIRECTORY = Repository.temp + bar + "Twister" + bar + "TC"
						+ bar;
				CONFIGDIRECTORY = Repository.temp + bar + "Twister" + bar
						+ "config";

				intro.text = "Started to parse the config";
				intro.percent += 0.035;
				intro.repaint();
				parseConfig();

				intro.text = "Finished parsing the config";
				intro.percent += 0.035;
				intro.repaint();
				parseDBConfig(Repository.REMOTEDATABASECONFIGFILE, true);
				frame = new Fereastra(applet, container);
				parseEmailConfig(Repository.REMOTEEMAILCONFIGFILE, true);

			} else {
				if (Fereastra.deleteTemp(file)) {
					System.out.println(Repository.temp + bar
							+ "Twister deleted successfull");
				} else {
					System.out.println("Could not delete: " + Repository.temp
							+ bar + "Twister");
				}
				intro.dispose();
				run = false;
				if (!applet) {
					System.exit(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean userpassword() {
		boolean passed = false;
		while (!passed) {
			try {
				JTextField user1 = new JTextField();
				JPasswordField password1 = new JPasswordField();
				Object[] message = new Object[] { "User: ", user1,
						"Password: ", password1 };
				int r = JOptionPane.showConfirmDialog(intro, message,
						"User&Password", JOptionPane.OK_CANCEL_OPTION);
				if (r == JOptionPane.OK_OPTION) {
					System.out.println("Attempting to connect to: " + host
							+ " with user: " + user1.getText()
							+ " and password: " + password1.getPassword());
					JSch jsch = new JSch();
					user = user1.getText();
					Session session = jsch.getSession(user, host, 22);
					Repository.password = new String(password1.getPassword());
					session.setPassword(new String(password1.getPassword()));
					Properties config = new Properties();
					config.put("StrictHostKeyChecking", "no");
					session.setConfig(config);
					session.connect();
					Channel channel = session.openChannel("sftp");
					channel.connect();
					c = (ChannelSftp) channel;
					try {
						USERHOME = c.pwd();
					} catch (Exception e) {
						System.out
								.println("ERROR: Could not retrieve remote user home directory");
					}
					REMOTECONFIGDIRECTORY = USERHOME + "/twister/config/";
					passed = true;
				} else {
					return false;
				}
			} catch (JSchException ex) {
				if (ex.toString().indexOf("Auth fail") != -1) {
					System.out.println("wrong user and/or password");
				} else {
					ex.printStackTrace();
					System.out.println("Could not connect to server");
				}
			}
		}
		return true;
	}

	public static void resetDBConf(String filename, boolean server) {
		databaseUserFields.clear();
		System.out.println("Reparsing " + filename);
		parseDBConfig(filename, server);
		frame.mainpanel.p1.suitaDetails.restart(databaseUserFields);
	}

	public static void resetEmailConf(String filename, boolean server) {
		System.out.println("Reparsing " + filename);
		parseEmailConfig(filename, server);
	}

	public static File getDBConfFile(String name, boolean fromServer) {
		File file = new File(temp + bar + "Twister" + bar + "config" + bar
				+ name);
		if (fromServer) {
			InputStream in = null;
			try {
				c.cd(Repository.REMOTEDATABASECONFIGPATH);
			} catch (Exception e) {
				System.out.println("Could not get :"
						+ Repository.REMOTEDATABASECONFIGPATH);
			}
			System.out.print("Getting " + name
					+ " as database config file.... ");
			try {
				in = c.get(name);
				InputStreamReader inputStreamReader = new InputStreamReader(in);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				BufferedWriter writer = null;
				String line;
				try {
					writer = new BufferedWriter(new FileWriter(file));
					while ((line = bufferedReader.readLine()) != null) {
						writer.write(line);
						writer.newLine();
					}
					bufferedReader.close();
					writer.close();
					inputStreamReader.close();
					in.close();
					System.out.println("successfull");
				} catch (Exception e) {
					System.out.println("failed");
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println("Could not get :" + name + " from: "
						+ Repository.REMOTEDATABASECONFIGPATH
						+ " as database config file");
			}
		}
		return file;
	}

	public static File getEmailConfFile(String name, boolean fromServer) {
		File file = new File(temp + bar + "Twister" + bar + "config" + bar
				+ name);
		if (fromServer) {
			InputStream in = null;
			try {
				c.cd(Repository.REMOTEEMAILCONFIGPATH);
			} catch (Exception e) {
				System.out.println("Could not get :"
						+ Repository.REMOTEEMAILCONFIGPATH);
			}
			System.out.print("Getting " + name + " as email config file.... ");
			try {
				in = c.get(name);
				InputStreamReader inputStreamReader = new InputStreamReader(in);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				BufferedWriter writer = null;
				String line;
				try {
					writer = new BufferedWriter(new FileWriter(file));
					while ((line = bufferedReader.readLine()) != null) {
						writer.write(line);
						writer.newLine();
					}
					bufferedReader.close();
					writer.close();
					inputStreamReader.close();
					in.close();
					System.out.println("successfull");
				} catch (Exception e) {
					System.out.println("failed");
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println("Could not get :" + name + " from: "
						+ Repository.REMOTEEMAILCONFIGPATH
						+ " as email config file");
			}
		}
		return file;
	}

	public static DefaultMutableTreeNode parseDBConfig(String name,
			boolean fromServer) {
		File dbConf = getDBConfFile(name, fromServer);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(dbConf);
			doc.getDocumentElement().normalize();
			NodeList nodeLst = doc.getElementsByTagName("table_structure");
			for (int i = 0; i < nodeLst.getLength(); i++) {
				Element tablee = (Element) nodeLst.item(i);
				NodeList fields = tablee.getElementsByTagName("field");
				DefaultMutableTreeNode table = new DefaultMutableTreeNode(
						tablee.getAttribute("name"));
				for (int j = 0; j < fields.getLength(); j++) {
					Element fielde = (Element) fields.item(j);
					DefaultMutableTreeNode field = new DefaultMutableTreeNode(
							fielde.getAttribute("Field"));
					table.add(field);
				}
				root.add(table);
			}
			nodeLst = doc.getElementsByTagName("twister_user_defined");
			Element tablee = (Element) nodeLst.item(0);
			NodeList fields = tablee.getElementsByTagName("field_section");
			tablee = (Element) fields.item(0);
			fields = tablee.getElementsByTagName("field");
			for (int i = 0; i < fields.getLength(); i++) {
				tablee = (Element) fields.item(i);
				if (tablee.getAttribute("GUIDefined").equals("true")) {
					String field[] = new String[ELEMENTSNR];
					field[0] = tablee.getAttribute("Label");
					if (field[0] == null) {
						System.out
								.println("Warning, no Label element in field tag in db.xml at filed nr: "
										+ i);
						field[0] = "";
					}
					field[1] = tablee.getAttribute("ID");
					if (field[1] == null) {
						System.out
								.println("Warning, no ID element in field tag in db.xml at filed nr: "
										+ i);
						field[1] = "";
					}
					field[2] = tablee.getAttribute("Type");
					if (field[2] == null) {
						System.out
								.println("Warning, no Type element in field tag in db.xml at filed nr: "
										+ i);
						field[2] = "";
					} else if (field[2].equals("UserSelect")) {
						field[2] = "true";
					} else {
						field[2] = "false";
					}
					field[3] = tablee.getAttribute("Mandatory");
					if (field[3] == null) {
						System.out
								.println("Warning, no Mandatory element in field tag in db.xml at filed nr: "
										+ i);
						field[3] = "";
					}
					databaseUserFields.add(field);
				}
			}
		} catch (Exception e) {
			try {
				System.out.println("Could not parse batabase XML file: "
						+ dbConf.getCanonicalPath());
			} catch (Exception ex) {
				System.out.println("There is a problem with " + name + " file");
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
		return root;
	}

	public static void parseEmailConfig(String name, boolean fromServer) {
		File dbConf = getEmailConfFile(name, fromServer);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(dbConf);
			doc.getDocumentElement().normalize();
			frame.mainpanel.p4.emails.setCheck(Boolean
					.parseBoolean(getTagContent(doc, "Enabled")));
			String smtppath = getTagContent(doc, "SMTPPath");
			frame.mainpanel.p4.emails.setIPName(smtppath.split(":")[0]);
			frame.mainpanel.p4.emails.setPort(smtppath.split(":")[1]);
			frame.mainpanel.p4.emails.setUser(getTagContent(doc, "SMTPUser"));
			frame.mainpanel.p4.emails.setFrom(getTagContent(doc, "From"));
			frame.mainpanel.p4.emails.setEmails(getTagContent(doc, "To"));
			if (!getTagContent(doc, "SMTPPwd").equals("")) {
				frame.mainpanel.p4.emails.setPassword("****");
			}
			frame.mainpanel.p4.emails.setMessage(getTagContent(doc, "Message"));
			frame.mainpanel.p4.emails.setSubject(getTagContent(doc, "Subject"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parseConfig() {
		try {
			InputStream in = null;
			byte[] data = new byte[100];
			int nRead;
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			OutputStream out = null;
			InputStreamReader inputStreamReader = null;
			BufferedReader bufferedReader = null;
			BufferedWriter writer = null;
			File file;
			String line = null;
			String name = null;
			try {
				c.cd(USERHOME + "/twister/config/");
			} catch (Exception e) {
				System.out.println("Could not get :" + USERHOME
						+ "/twister/config/");
				JOptionPane.showMessageDialog(Repository.frame,
						"Could not get :" + USERHOME + "/twister/config/");
				if (Fereastra.deleteTemp(new File(Repository.temp + bar
						+ "Twister"))) {
					System.out.println(Repository.temp + bar
							+ "Twister deleted successfull");
				} else {
					System.out.println("Could not delete: " + Repository.temp
							+ bar + "Twister");
				}
				intro.dispose();
				run = false;
				if (!applet) {
					System.exit(0);
				}
			}
			try {
				System.out.println("fwmconfig.xml size on sftp: "
						+ c.lstat("fwmconfig.xml").getSize() + " bytes");
				in = c.get("fwmconfig.xml");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Repository.frame,
						"Could not get fwmconfig.xml from " + c.pwd()
								+ " creating a blank one.");
				System.out.println("Could not get fwmconfig.xml from "
						+ c.pwd() + " creating a blank one.");
				ConfigFiles.saveXML(true);
				in = c.get("fwmconfig.xml");
			}
			inputStreamReader = new InputStreamReader(in);
			bufferedReader = new BufferedReader(inputStreamReader);
			file = new File(temp + bar + "Twister" + bar + "config" + bar
					+ "fwmconfig.xml");
			writer = new BufferedWriter(new FileWriter(file));
			while ((line = bufferedReader.readLine()) != null) {
				writer.write(line);
				writer.newLine();
			}
			bufferedReader.close();
			writer.close();
			inputStreamReader.close();
			in.close();
			System.out.println("fwmconfig.xml local size: " + file.length()
					+ " bytes");
			String usersdir = "";

			intro.text = "Finished getting fwmconfig";
			intro.percent += 0.035;
			intro.repaint();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(Repository.getFwmConfig());
				doc.getDocumentElement().normalize();
				LOGSPATH = getTagContent(doc, "LogsPath");
				if (doc.getElementsByTagName("LogFiles").getLength() == 0) {
					System.out.println("LogFiles tag not found in fwmconfig");
				} else {
					logs.add(getTagContent(doc, "logRunning"));
					logs.add(getTagContent(doc, "logDebug"));
					logs.add(getTagContent(doc, "logSummary"));
					logs.add(getTagContent(doc, "logTest"));
					logs.add(getTagContent(doc, "logCli"));
				}
				HTTPSERVERPORT = getTagContent(doc, "HttpServerPort");
				CENTRALENGINEPORT = getTagContent(doc, "CentralEnginePort");
				RESOURCEALLOCATORPORT = getTagContent(doc,
						"ResourceAllocatorPort");
				usersdir = getTagContent(doc, "UsersPath");
				REMOTEUSERSDIRECTORY = usersdir;
				XMLREMOTEDIR = getTagContent(doc, "MasterXMLTestSuite");
				XMLDIRECTORY = Repository.temp
						+ bar
						+ "Twister"
						+ bar
						+ "XML"
						+ bar
						+ XMLREMOTEDIR.split("/")[XMLREMOTEDIR.split("/").length - 1];
				REMOTEEPIDDIR = getTagContent(doc, "EPIdsFile");
				REMOTEDATABASECONFIGFILE = getTagContent(doc, "DbConfigFile");
				String[] path = REMOTEDATABASECONFIGFILE.split("/");
				StringBuffer result = new StringBuffer();
				if (path.length > 0) {
					for (int i = 0; i < path.length - 1; i++) {
						result.append(path[i]);
						result.append("/");
					}
				}
				REMOTEDATABASECONFIGPATH = result.toString();
				REMOTEDATABASECONFIGFILE = path[path.length - 1];
				REMOTEEMAILCONFIGFILE = getTagContent(doc, "EmailConfigFile");
				path = REMOTEEMAILCONFIGFILE.split("/");
				result = new StringBuffer();
				if (path.length > 0) {
					for (int i = 0; i < path.length - 1; i++) {
						result.append(path[i]);
						result.append("/");
					}
				}
				REMOTEEMAILCONFIGPATH = result.toString();
				REMOTEEMAILCONFIGFILE = path[path.length - 1];
				TESTSUITEPATH = getTagContent(doc, "TestCaseSourcePath");
				REMOTEHARDWARECONFIGDIRECTORY = getTagContent(doc,
						"HardwareConfig");
			} catch (Exception e) {
				e.printStackTrace();
			}

			intro.text = "Finished initializing variables fwmconfig";
			intro.percent += 0.035;
			intro.repaint();
			intro.text = "Started getting users xml";
			intro.percent += 0.035;
			intro.repaint();
			try {
				c.cd(usersdir);
			} catch (Exception e) {
				System.out.println("Could not get to " + usersdir + "on sftp");
			}
			int subdirnr = usersdir.split("/").length - 1;
			int size;
			try {
				size = c.ls(usersdir).size();
			} catch (Exception e) {
				System.out.println("No suites xml");
				size = 0;
			}
			for (int i = 0; i < size; i++) {
				name = ((LsEntry) c.ls(usersdir).get(i)).getFilename();
				if (name.split("\\.").length == 0) {
					continue;
				}
				if (name.toLowerCase().indexOf(".xml") == -1) {
					continue;
				}
				System.out.print("Getting " + name + " ....");
				in = c.get(name);
				inputStreamReader = new InputStreamReader(in);
				bufferedReader = new BufferedReader(inputStreamReader);
				file = new File(temp + bar + "Twister" + bar + "Users" + bar
						+ name);
				writer = new BufferedWriter(new FileWriter(file));
				while ((line = bufferedReader.readLine()) != null) {
					writer.write(line);
					writer.newLine();
				}
				bufferedReader.close();
				writer.close();
				inputStreamReader.close();
				in.close();
				System.out.println("successfull");
			}

			intro.text = "Finished getting users xml";
			intro.percent += 0.035;
			intro.repaint();
			String dir = Repository.getXMLRemoteDir();
			String[] path = dir.split("/");
			StringBuffer result = new StringBuffer();
			if (path.length > 0) {
				for (int i = 0; i < path.length - 2; i++) {
					result.append(path[i]);
					result.append("/");
				}
			}

			intro.text = "Finished writing xml path";
			intro.percent += 0.035;
			intro.repaint();
			int length = 0;
			try {
				length = c.ls(result.toString() + path[path.length - 2]).size();
			} catch (Exception e) {
				System.out.println("Could not get " + result.toString() + dir);
			}
			if (length > 2) {

				intro.text = "Started looking for xml file";
				intro.percent += 0.035;
				intro.repaint();

				intro.text = "Started getting xml file";
				intro.percent += 0.035;
				intro.repaint();
				System.out.println("XMLREMOTEDIR: " + XMLREMOTEDIR);
				in = c.get(XMLREMOTEDIR);
				data = new byte[900];
				buffer = new ByteArrayOutputStream();
				while ((nRead = in.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}

				intro.text = "Finished reading xml ";
				intro.percent += 0.035;
				intro.repaint();
				buffer.flush();
				out = new FileOutputStream(
						temp
								+ bar
								+ "Twister"
								+ bar
								+ "XML"
								+ bar
								+ XMLREMOTEDIR.split("/")[XMLREMOTEDIR
										.split("/").length - 1]);

				intro.text = "Started writing xml file";
				intro.percent += 0.035;
				intro.repaint();
				buffer.writeTo(out);
				out.close();
				buffer.close();
				in.close();
			}

			intro.text = "Finished writing xml";
			intro.percent += 0.035;
			intro.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getTagContent(Document doc, String tag) {
		NodeList nodeLst = doc.getElementsByTagName(tag);
		if (nodeLst.getLength() == 0) {
			System.out.println("tag " + tag + " not found in "
					+ doc.getDocumentURI());
		}
		Node fstNode = nodeLst.item(0);
		Element fstElmnt = (Element) fstNode;
		NodeList fstNm = fstElmnt.getChildNodes();
		String temp;
		try {
			temp = fstNm.item(0).getNodeValue().toString();
		} catch (Exception e) {
			System.out.println(tag + " empty");
			temp = "";
		}
		return temp;
	}

	public static void addSuita(Item s) {
		suite.add(s);
	}

	public static Item getSuita(int s) {
		return suite.get(s);
	}

	public static int getSuiteNr() {
		return suite.size();
	}

	public static ArrayList<String[]> getDatabaseUserFields() {
		return databaseUserFields;
	}

	public static void emptyTestRepository() {
		suitetest.clear();
	}

	public static void emptyLogs() {
		logs.clear();
	}

	public static File getFwmConfig() {
		return new File(temp + bar + "Twister" + bar + "config" + bar
				+ "fwmconfig.xml");
	}

	public static String getUsersDirectory() {
		return USERSDIRECTORY;
	}

	public static String getRemoteEpIdDir() {
		return REMOTEEPIDDIR;
	}

	public static String getRemoteUsersDirectory() {
		return REMOTEUSERSDIRECTORY;
	}

	public static String getCentralEnginePort() {
		return CENTRALENGINEPORT;
	}

	public static String getResourceAllocatorPort() {
		return RESOURCEALLOCATORPORT;
	}

	public static String getXMLRemoteDir() {
		return XMLREMOTEDIR;
	}

	public static ArrayList<Item> getSuite() {
		return suite;
	}

	public static long getLogSize() {
		try {
			LsEntry entry = (LsEntry) c.ls("Log").get(2);
			return entry.getAttrs().getSize();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getTestSuiteNr() {
		return suitetest.size();
	}

	public static String getConfigDirectory() {
		return CONFIGDIRECTORY;
	}

	public static void addTestSuita(Item suita) {
		suitetest.add(suita);
	}

	public static String getHTTPServerPort() {
		return HTTPSERVERPORT;
	}

	public static Item getTestSuita(int i) {
		return suitetest.get(i);
	}

	public static void setTestSuitePath(String path) {
		TESTSUITEPATH = path;
	}

	public static String getTestSuitePath() {
		return TESTSUITEPATH;
	}

	public static void emptyRepository() {
		suite.clear();
	}

	public static String getTestXMLDirectory() {
		return XMLDIRECTORY;
	}

	public static String getTCDirectory() {
		return TCDIRECTORY;
	}

	public static Image getSuitaIcon() {
		return suitaicon;
	}

	public static Image getFailIcon() {
		return failicon;
	}

	public static Image getPendingIcon() {
		return pendingicon;
	}

	public static Image getWorkingIcon() {
		return workingicon;
	}

	public static Image getNotExecIcon() {
		return notexecicon;
	}

	public static Image getTimeoutIcon() {
		return timeouticon;
	}

	public static Image getSkippedIcon() {
		return skipicon;
	}

	public static Image getWaitingIcon() {
		return waiticon;
	}

	public static Image getStopIcon() {
		return stopicon;
	}

	public static Image getTestBedIcon() {
		return testbedicon;
	}

	public static Image getStoppedIcon() {
		return stoppedicon;
	}

	public static Image getPassIcon() {
		return passicon;
	}

	public static Image getTCIcon() {
		return tcicon;
	}

	public static Image getPlayIcon() {
		return playicon;
	}

	public static String getBar() {
		return bar;
	}

	public static Image getPropertyIcon() {
		return propicon;
	}

	public static String getUser() {
		return user;
	}
}
