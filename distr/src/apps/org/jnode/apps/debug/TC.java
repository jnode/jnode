/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.debug;

import charva.awt.BorderLayout;
import charva.awt.Color;
import charva.awt.FlowLayout;
import charva.awt.Toolkit;
import charvax.swing.JFrame;
import charvax.swing.JPanel;
import charvax.swing.border.LineBorder;
import charvax.swing.border.TitledBorder;
import java.util.Vector;

/**
 * @author blind
 *  
 */
public class TC extends JFrame {
	private RootObjectPanel rootObjectPanel;
	private PropertiesPanel propertiesPane;
	private ListPanel resultPane;
	private JPanel contentPane;
	public TC() {
		super("JNode Test Client");
		initialize();
		setLocation(0, 0);
		setSize(80, 24);
		validate();
	}
	private void initialize() {
		contentPane = (JPanel) getContentPane();
		setBackground(Color.black);
		setForeground(Color.cyan);
		contentPane.setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout());
		northPanel.add(getRootObjectPanel());
		northPanel.add(getPropertiesPane());
		contentPane.add(northPanel, BorderLayout.NORTH);
		contentPane.add(getResultPane(), BorderLayout.CENTER);
		getRootObjectPanel().fill();
	}
	
	protected RootObjectPanel getRootObjectPanel() {
		if (rootObjectPanel == null) {
			rootObjectPanel = new RootObjectPanel(38, 10) {
				public void elementSelected(Object o) {
					getPropertiesPane().fillPanel(o);
				}
			};
			rootObjectPanel.setBorder(new LineBorder(Color.green));
			rootObjectPanel.setBorder(new TitledBorder(new LineBorder(
					Color.green), "root objects"));
		}
		return rootObjectPanel;
	}
	
	protected PropertiesPanel getPropertiesPane() {
		if (propertiesPane == null) {
			propertiesPane = new PropertiesPanel(38, 10) {
				public void elementSelected(Object o) {
					Object result = null;
					try {
						if (o instanceof ObjectFieldPair) {
							ObjectFieldPair ofp = (ObjectFieldPair) o;
							result = ofp.getField().get(ofp.getObject());
						}
						if (o instanceof ObjectMethodPair) {
							ObjectMethodPair omp = (ObjectMethodPair) o;
							//TODO: support for methods that take arguments
							result = omp.getMethod().invoke(omp.getObject(),
									null);
						}
					} catch (Exception ex) {
						addResultItem(ex);
					}
					addResultItem(result);
				}
			};
			//propertiesPane.setBorder(new LineBorder(Color.green));
			propertiesPane.setBorder(new TitledBorder(new LineBorder(
					Color.green), "fields & methods"));
		}
		return propertiesPane;
	}
	private ListPanel getResultPane() {
		if (resultPane == null) {
			resultPane = new ListPanel(75, 10) {
				public void elementSelected(Object o) {
					if (o != null) {
						final Vector newList = new Vector();
						newList.addElement(new ListElement(o, getElementLabel(o)));
						getRootObjectPanel().setList(newList);
					}
				}
			};
			resultPane.setBorder(new LineBorder(Color.green));
			resultPane.setForeground(Color.cyan);
			resultPane.setBorder(new TitledBorder(new LineBorder(
					Color.green), "values"));
		}
		return resultPane;
	}
	protected void addResultItem(Object o) {
//if(o!=null)System.out.println(o.toString());else System.out.println("NULL");
		Vector resultList = getResultPane().getList();
		if (resultList == null)
			resultList = new Vector();
		String label = (o == null) ? "null" : o.getClass().getName()+" ["+o.toString()+"]"; 
		resultList.addElement(new ListElement(o, label));
		getResultPane().setList(resultList);
		getResultPane().positionToLastRow();
	}
	public static void main(String[] args) {
		Toolkit.getDefaultToolkit().register(); //JNODE
		TC tc = new TC();
		tc.setVisible(true);
	}
}
