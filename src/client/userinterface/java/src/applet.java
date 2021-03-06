/*
   File: applet.java ; This file is part of Twister.

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
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class applet extends Applet {

	private static final long serialVersionUID = 1L;

	public void init() {

		try {
			System.out.println("Current version: ");
			System.out.println("OS current temporary directory is : "
					+ System.getProperty("java.io.tmpdir"));
			Repository.tcicon = loadIcon("tc.png");
			System.out.println("Repository.tcicon "
					+ Repository.tcicon.getHeight(this));
			Repository.background = loadIcon("background.png");
			Repository.pendingicon = loadIcon("pending.png");
			Repository.deviceicon = loadIcon("device.png");
			Repository.moduleicon = loadIcon("module.png");
			Repository.notexecicon = loadIcon("notexec.png");
			Repository.skipicon = loadIcon("skip.png");
			Repository.stoppedicon = loadIcon("stopped.png");
			Repository.timeouticon = loadIcon("timeout.png");
			Repository.waiticon = loadIcon("waiting.png");
			Repository.workingicon = loadIcon("working.png");
			Repository.suitaicon = loadIcon("suita.png");
			Repository.propicon = loadIcon("prop.png");
			Repository.vlcclient = loadIcon("vlcclient.png");
			Repository.failicon = loadIcon("fail.png");
			Repository.passicon = loadIcon("pass.png");
			Repository.stopicon = loadIcon("stop.png");
			Repository.switche = loadIcon("switch.png");
			Repository.switche2 = loadIcon("switch.jpg");
			Repository.flootw = loadIcon("twisterfloodlight.png");
			Repository.rack150 = loadIcon("150.png");
			Repository.rack151 = loadIcon("151.png");
			Repository.rack152 = loadIcon("152.png");
			Repository.vlcserver = loadIcon("vlcserver.png");
			Repository.playicon = loadIcon("play.png");
			Repository.addsuitaicon = loadIcon("addsuita.png");
			Repository.removeicon = loadIcon("deleteicon.png");
			Repository.pauseicon = loadIcon("pause.png");
			Repository.porticon = loadIcon("port.png");
			Repository.testbedicon = loadIcon("testbed.png");
			Repository.inicon = loadIcon("in.png");
			Repository.outicon = loadIcon("out.png");
			Repository.baricon = loadIcon("bar.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		setLayout(null);
		System.out.println("Size is: " + getSize().getWidth() + " - "
				+ getSize().getHeight());
		Repository.initialize(true, getCodeBase().getHost(), this);
	}

	public Image loadIcon(String icon) {
		Image image = null;
		try {
			System.out.println("Getting " + icon + " from applet jar...");
			InputStream in = getClass().getResourceAsStream(
					"Icons" + "/" + icon);
			System.out.println("Saving " + icon + " in memory.....");
			image = new ImageIcon(ImageIO.read(in)).getImage();
			if (image != null) {
				System.out.println(icon + " succsesfully loaded.");
			} else {
				System.out.println(icon + " not loaded.");
			}
		} catch (Exception e) {
			System.out.println("There was a problem in loading " + icon
					+ " on " + image.toString());
			e.printStackTrace();
		}
		return image;
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		Repository.frame.mainpanel.setSize(width - 20, height - 20);
		Repository.frame.mainpanel.p2.splitPane.setSize(width - 52,
				height - 120);
		Repository.frame.mainpanel.p1.splitPane.setSize(width - 52,
				height - 120);
		Repository.frame.mainpanel.setSize(width - 28, height - 40);
		Repository.frame.mainpanel.p4.scroll.setSize(width - 310, height - 150);
		Repository.frame.mainpanel.p4.main.setSize(width - 300, height - 130);
		Repository.frame.mainpanel.p4.dut.setPreferredSize(new Dimension(
				width - 300, height - 150));

		System.out.println("Resizing to: " + width + " - " + height);
		validate();
	}

	public void stop() {
		System.out.println("applet stopping");
	}

	public void destroy() {
		System.out.println("applet destroying");
		File file = new File(Repository.temp);
		if (file.exists()) {
			if (Fereastra.deleteTemp(file)) {
				System.out.println(Repository.temp + " deleted successfull");
			} else {
				System.out.println("Could not delete: " + Repository.temp);
			}
		}
		System.exit(0);
	}

	public void start() {
		System.out.println("applet starting");
	}
}
