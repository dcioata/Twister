/*
   File: ScrollGrafic.java ; This file is part of Twister.

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

import javax.swing.JPanel;
import java.awt.ScrollPane;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DefaultKeyboardFocusManager;

public class ScrollGrafic extends JPanel {

	private static final long serialVersionUID = 1L;
	public JScrollPane pane;
	public Grafic g;

	public ScrollGrafic(int x, int y, TreeDropTargetListener tdtl, String user, boolean applet) {
        Repository.intro.text = "Started Users Graphics initialization";
        Repository.intro.percent += 0.035;
        Repository.intro.repaint();
        g = new Grafic(tdtl, user);
        pane = new JScrollPane(g);
        pane.setMinimumSize(new Dimension(100, 350));
        pane.setMaximumSize(new Dimension(1000, 1000));
        pane.setPreferredSize(new Dimension(450, 500));
        pane.getVerticalScrollBar().setUnitIncrement(16);
        add(pane);
        Repository.intro.text = "Finished Users Graphics initialization";
        Repository.intro.percent += 0.035;
        Repository.intro.repaint();
    }
}
