/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * @author epr
 */
public class GeneralPathTest {

	public static void main(String[] args) {
		
		System.out.println("Simple");
		GeneralPath p1 = new GeneralPath();
		p1.moveTo(5, 5);
		for (int i = 0; i < 4000; i++) {
			p1.lineTo(i, 10);
		}
		
		System.out.println("Append");
		GeneralPath p2 = new GeneralPath();
		p2.append(p1, false);
		p1.lineTo(10, 10);
		
		System.out.println("Transform");
		p2.transform(AffineTransform.getScaleInstance(5, 5));
	}
}
