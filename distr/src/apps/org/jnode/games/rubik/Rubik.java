/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.games.rubik;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Rubik's Cube 3D simulator.
 * Adapted to JNode by Levente S\u00e1ntha.
 *  
 * @author Karl H\u00f6rnell, March 11 1996 (Last modified October 6)
 *
 */
public final class Rubik extends JComponent {
    
    private static final long serialVersionUID = 1L;
    
    int i, j, k, n, o, p, q, lastX, lastY, dx, dy;
    int rectX[], rectY[];
    Color colList[], bgcolor;
    final double sideVec[] = {0, 0, 1, 0, 0, -1, 0, -1, 0, 1, 0, 0, 0, 1, 0, -1, 0, 0}; // Normal vectors
    final double corners[] = {-1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1,
        -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1}; // Vertex co-ordinates
    double topCorners[], botCorners[];
    final int sides[] = {4, 5, 6, 7, 3, 2, 1, 0, 0, 1, 5, 4, 1, 2, 6, 5, 2, 3, 7, 6, 0, 4, 7, 3};
    final int nextSide[] = {2, 3, 4, 5, 4, 3, 2, 5, 1, 3, 0, 5, 1, 4, 0, 2, 1, 5, 0, 3, 2, 0, 4, 1};
    final int mainBlocks[] = {0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3};
    final int twistDir[] = {-1, 1, -1, 1, -1, 1, -1, 1, 1, 1, 1, 1, 1, -1, 1, -1, 1, 1, 1, 1, -1, 1, -1, 1};
    final int colDir[] = {-1, -1, 1, -1, 1, -1};
    final int circleOrder[] = {0, 1, 2, 5, 8, 7, 6, 3};
    int topBlocks[], botBlocks[];
    int sideCols[], sideW, sideH;
    int dragReg, twistSide = -1;
    int nearSide[], buffer[]; // Which side belongs to dragCorn
    double dragCorn[], dragDir[];
    double eye[] = {0.3651, 0.1826, -0.9129}; // Initial observer co-ordinate axes (view)
    double eX[] = {0.9309, -0.0716, 0.3581}; // (sideways)
    double eY[]; // (vertical)
    double Teye[], TeX[], TeY[];
    double light[], temp[] = {0, 0, 0}, temp2[] = {0, 0, 0}, newCoord[];
    double sx, sy, sdxh, sdyh, sdxv, sdyv, d, t1, t2, t3, t4, t5, t6;
    double phi, phibase = 0, Cphi, Sphi, currDragDir[];

    boolean naturalState = true, twisting = false, OKtoDrag = false;
    Math m;
    Graphics offGraphics;
    Image offImage;

    /**
     * Initialization of the component.
     */
    public void init() {
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        setFocusable(true);
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    if (Rubik.this.contains(event.getX(), event.getY())) {
                        if (!Rubik.this.hasFocus() && Rubik.this.isRequestFocusEnabled()) {
                            Rubik.this.requestFocus();
                        }
                    }
                }
                Rubik.this.mousePressed(event.getX(), event.getY());
            }

            public void mouseReleased(MouseEvent event) {
                Rubik.this.mouseReleased(event.getX(), event.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                Rubik.this.mouseDragged(event.getX(), event.getY());
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                Rubik.this.keyPressed(event.getKeyChar());
            }
        });
        rectX = new int[4];
        rectY = new int[4];
        // Projected co-ordinates (on screen)
        newCoord = new double[16];
        dragDir = new double[24];
        dragCorn = new double[96];
        // Vertex co-ordinate storage
        topCorners = new double[24];
        // for sub-cubes during twist
        botCorners = new double[24];
        topBlocks = new int[24];
        botBlocks = new int[24];
        buffer = new int[12];
        nearSide = new int[12];
        light = new double[3];
        Teye = new double[3];
        TeX = new double[3];
        TeY = new double[3];
        currDragDir = new double[2];
        eY = new double[3];
        vecProd(eye, 0, eX, 0, eY, 0); // Fix y axis of observer co-ordinate system
        normalize(eY, 0);
        colList = new Color[120];
        for (i = 0; i < 20; i++) {
            colList[i] = new Color(103 + i * 8, 103 + i * 8, 103 + i * 8);        // White
            colList[i + 20] = new Color(i * 6, i * 6, 84 + i * 9);            // Blue
            colList[i + 40] = new Color(84 + i * 9, i * 5, i * 5);            // Red
            colList[i + 60] = new Color(i * 6, 84 + i * 9, i * 6);            // Green
            colList[i + 80] = new Color(84 + i * 9, 84 + i * 9, i * 6);            // Yellow
            colList[i + 100] = new Color(84 + i * 9, 55 + i * 8, i * 3);        // Orange
        }
        sideCols = new int[54];
        for (i = 0; i < 54; i++)
            sideCols[i] = i / 9;
        bgcolor = findBGColor();
        setSize(125, 125);
        repaint();
    }

    /**
     * Convert hexadecimal RGB parameter to color.
     * @return
     */
    public Color findBGColor() {
        int hex[];
        String s, h = "0123456789abcdef";
        Color c;
        hex = new int[6];
        s = null; //getParameter("bgcolor");
        if ((s != null) && (s.length() == 6)) {
            for (i = 0; i < 6; i++)
                for (j = 0; j < 16; j++)
                    if (Character.toLowerCase(s.charAt(i)) == h.charAt(j))
                        hex[i] = j;
            c = new Color(hex[0] * 16 + hex[1], hex[2] * 16 + hex[3], hex[4] * 16 + hex[5]);
        } else
            c = Color.lightGray; // Default
        return c;
    }

// Various vector manipulation functions

    /**
     * 
     */
    public double scalProd(double v1[], int ix1, double v2[], int ix2) {
        return v1[ix1] * v2[ix2] + v1[ix1 + 1] * v2[ix2 + 1] + v1[ix1 + 2] * v2[ix2 + 2];
    }

    /**
     * 
     * @param v
     * @param ix
     * @return
     */
    public double vNorm(double v[], int ix) {
        return Math.sqrt(v[ix] * v[ix] + v[ix + 1] * v[ix + 1] + v[ix + 2] * v[ix + 2]);
    }

    /**
     * 
     * @param v1
     * @param ix1
     * @param v2
     * @param ix2
     * @return
     */
    public double cosAng(double v1[], int ix1, double v2[], int ix2) {
        return scalProd(v1, ix1, v2, ix2) / (vNorm(v1, ix1) * vNorm(v2, ix2));
    }

    /**
     * 
     * @param v
     * @param ix
     */
    public void normalize(double v[], int ix) {
        double t = vNorm(v, ix);
        v[ix] = v[ix] / t;
        v[ix + 1] = v[ix + 1] / t;
        v[ix + 2] = v[ix + 2] / t;
    }

    /**
     * 
     * @param v
     * @param ix
     * @param a
     */
    public void scalMult(double v[], int ix, double a) {
        v[ix] = v[ix] * a;
        v[ix + 1] = v[ix + 1] * a;
        v[ix + 2] = v[ix + 2] * a;
    }

    /**
     * 
     * @param v1
     * @param ix1
     * @param v2
     * @param ix2
     */
    public void addVec(double v1[], int ix1, double v2[], int ix2) {
        v2[ix2] += v1[ix1];
        v2[ix2 + 1] += v1[ix1 + 1];
        v2[ix2 + 2] += v1[ix1 + 2];
    }

    /**
     * 
     * @param v1
     * @param ix1
     * @param v2
     * @param ix2
     */
    public void subVec(double v1[], int ix1, double v2[], int ix2) {
        v2[ix2] -= v1[ix1];
        v2[ix2 + 1] -= v1[ix1 + 1];
        v2[ix2 + 2] -= v1[ix1 + 2];
    }

    /**
     * 
     * @param v1
     * @param ix1
     * @param v2
     * @param ix2
     */
    public void copyVec(double v1[], int ix1, double v2[], int ix2) {
        v2[ix2] = v1[ix1];
        v2[ix2 + 1] = v1[ix1 + 1];
        v2[ix2 + 2] = v1[ix1 + 2];
    }

    /**
     * 
     * @param v1
     * @param ix1
     * @param v2
     * @param ix2
     * @param v3
     * @param ix3
     */
    public void vecProd(double v1[], int ix1, double v2[], int ix2,
                        double v3[], int ix3) {
        v3[ix3] = v1[ix1 + 1] * v2[ix2 + 2] - v1[ix1 + 2] * v2[ix2 + 1];
        v3[ix3 + 1] = v1[ix1 + 2] * v2[ix2] - v1[ix1] * v2[ix2 + 2];
        v3[ix3 + 2] = v1[ix1] * v2[ix2 + 1] - v1[ix1 + 1] * v2[ix2];
    }

    /**
     * Produce large and small sub-cube for twisting.
     */
    public void cutUpCube() {
        boolean check;
        // Copy main coordinate data
        for (i = 0; i < 24; i++) {
            topCorners[i] = corners[i];
            botCorners[i] = corners[i];
        }
        copyVec(sideVec, 3 * twistSide, temp, 0); // Start manipulating and build new parts
        copyVec(temp, 0, temp2, 0); // Fix new co-ordinates. Some need to be altered.
        scalMult(temp, 0, 1.3333);
        scalMult(temp2, 0, 0.6667);
        for (i = 0; i < 8; i++) {
            check = false;
            for (j = 0; j < 4; j++)
                if (i == sides[twistSide * 4 + j])
                    check = true;
            if (check)
                subVec(temp2, 0, botCorners, i * 3);
            else
                addVec(temp, 0, topCorners, i * 3);
        }

// The sub-cubes need information about which colored fields belong to them.
        // Fix the sub-cube blockings. First copy data from main
        for (i = 0; i < 24; i++) {
            topBlocks[i] = mainBlocks[i]; // Large sub-cube data
            botBlocks[i] = mainBlocks[i]; // Small sub-cube data
        }
        for (i = 0; i < 6; i++) {
            if (i == twistSide) {
                botBlocks[i * 4 + 1] = 0; // Large sub-cube is blank on top
                botBlocks[i * 4 + 3] = 0;
            } else {
                k = -1;
                for (j = 0; j < 4; j++)
                    if (nextSide[i * 4 + j] == twistSide)
                        k = j;
                // Twisted side adjacent to...
                switch (k) {
                    // Up side?
                    case 0: {
                        topBlocks[i * 4 + 3] = 1;
                        botBlocks[i * 4 + 2] = 1;
                        break;
                    }
                    // Right side?
                    case 1: {
                        topBlocks[i * 4] = 2;
                        botBlocks[i * 4 + 1] = 2;
                        break;
                    }
                    // Down side?
                    case 2: {
                        topBlocks[i * 4 + 2] = 2;
                        botBlocks[i * 4 + 3] = 2;
                        break;
                    }
                    // Left side?
                    case 3: {
                        topBlocks[i * 4 + 1] = 1;
                        botBlocks[i * 4] = 1;
                        break;
                    }
                    // None
                    case -1: {
                        topBlocks[i * 4 + 1] = 0; // Small sub-cube is blank on bottom
                        topBlocks[i * 4 + 3] = 0;
                        break;
                    }
                }
            }
        }
    }

    /**
     * 
     * @param key
     * @return
     */
    public boolean keyPressed(int key) {
        // Restore
        if (key == 114) {
            twisting = false;
            naturalState = true;
            for (i = 0; i < 54; i++)
                sideCols[i] = i / 9;
            repaint();
        } else if (key == 115) {
            // Scramble
            twisting = false;
            naturalState = true;
            for (i = 0; i < 20; i++)
                colorTwist((int) (Math.random() * 6), (int) (Math.random() * 3 + 1));
            repaint();
        }
        return false;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean mouseDragged(int x, int y) {
        boolean check;
        double x1, x2, y1, y2, alpha, beta;

        if ((!twisting) && (OKtoDrag)) {
            OKtoDrag = false;
            check = false;
            // Check if inside a drag region
            for (i = 0; i < dragReg; i++) {
                x1 = dragCorn[i * 8 + 1] - dragCorn[i * 8];
                x2 = dragCorn[i * 8 + 5] - dragCorn[i * 8 + 4];
                y1 = dragCorn[i * 8 + 3] - dragCorn[i * 8];
                y2 = dragCorn[i * 8 + 7] - dragCorn[i * 8 + 4];
                alpha = (y2 * (lastX - dragCorn[i * 8]) - y1 * (lastY - dragCorn[i * 8 + 4])) / (x1 * y2 - y1 * x2);
                beta = (-x2 * (lastX - dragCorn[i * 8]) + x1 * (lastY - dragCorn[i * 8 + 4])) / (x1 * y2 - y1 * x2);
                if ((alpha > 0) && (alpha < 1) && (beta > 0) && (beta < 1)) // We're in
                {
                    currDragDir[0] = dragDir[i * 2];
                    currDragDir[1] = dragDir[i * 2 + 1];
                    d = currDragDir[0] * (x - lastX) + currDragDir[1] * (y - lastY);
                    d = d * d / ((currDragDir[0] * currDragDir[0] + currDragDir[1] * currDragDir[1]) *
                        ((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY)));
                    if (d > 0.6) {
                        check = true;
                        twistSide = nearSide[i];
                        i = 100;
                    }
                }
            }
            // We're twisting
            if (check) {
                // The cube still hasn't been split up
                if (naturalState) {
                    cutUpCube();
                    naturalState = false;
                }
                twisting = true;
                phi = 0.02 * (currDragDir[0] * (x - lastX) + currDragDir[1] * (y - lastY)) /
                    Math.sqrt(currDragDir[0] * currDragDir[0] + currDragDir[1] * currDragDir[1]);
                repaint();
                return false;
            }
        }

        OKtoDrag = false;
        // Normal rotation
        if (!twisting) {
            dx = lastX - x; // Vertical shift
            copyVec(eX, 0, temp, 0);
            scalMult(temp, 0, ((double) dx) * 0.016);
            addVec(temp, 0, eye, 0);
            vecProd(eY, 0, eye, 0, eX, 0);
            normalize(eX, 0);
            normalize(eye, 0);
            dy = y - lastY; // Horizontal shift
            copyVec(eY, 0, temp, 0);
            scalMult(temp, 0, ((double) dy) * 0.016);
            addVec(temp, 0, eye, 0);
            vecProd(eye, 0, eX, 0, eY, 0);
            normalize(eY, 0);
            normalize(eye, 0);
            lastX = x;
            lastY = y;
            repaint();
        } else {
            // Twist, compute twisting angle phi
            phi = 0.02 * (currDragDir[0] * (x - lastX) + currDragDir[1] * (y - lastY)) /
                Math.sqrt(currDragDir[0] * currDragDir[0] + currDragDir[1] * currDragDir[1]);
            repaint();
        }
        return false;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean mousePressed(int x, int y) {
        lastX = x;
        lastY = y;
        OKtoDrag = true;
        return false;
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean mouseReleased(int x, int y) {
        int quads;
        double qu;
        // We have let go of the mouse when twisting
        if (twisting) {
            twisting = false;
            phibase += phi; // Save twist angle
            phi = 0;
            qu = phibase;
            while (qu < 0)
                qu += 125.662;
            quads = ((int) (qu * 3.183));
            // Close enough to a corner?
            if (((quads % 5) == 0) || ((quads % 5) == 4)) {
                quads = ((quads + 1) / 5) % 4;
                if (colDir[twistSide] < 0)
                    quads = (4 - quads) % 4;
                phibase = 0;
                naturalState = true; // Return the cube to its natural state
                colorTwist(twistSide, quads); // and shift the colored fields
            }
            repaint();
        }
        return false;
    }

    /**
     * Shift colored fields.
     * @param sideNum
     * @param quads
     */
    public void colorTwist(int sideNum, int quads) {
        int i, j, k, l = 0;
        k = quads * 2; // quads = number of 90-degree multiples
        for (i = 0; i < 8; i++) {
            buffer[k] = sideCols[sideNum * 9 + circleOrder[i]];
            k = (k + 1) % 8;
        }
        for (i = 0; i < 8; i++)
            sideCols[sideNum * 9 + circleOrder[i]] = buffer[i];
        k = quads * 3;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++)
                if (nextSide[nextSide[sideNum * 4 + i] * 4 + j] == sideNum)
                    l = j;
            for (j = 0; j < 3; j++) {
                switch (l) {
                    case 0:
                        buffer[k] = sideCols[nextSide[sideNum * 4 + i] * 9 + j];
                        break;
                    case 1:
                        buffer[k] = sideCols[nextSide[sideNum * 4 + i] * 9 + 2 + 3 * j];
                        break;
                    case 2:
                        buffer[k] = sideCols[nextSide[sideNum * 4 + i] * 9 + 8 - j];
                        break;
                    case 3:
                        buffer[k] = sideCols[nextSide[sideNum * 4 + i] * 9 + 6 - 3 * j];
                        break;
                    default:
                        break;
                }
                k = (k + 1) % 12;
            }
        }
        k = 0;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++)
                if (nextSide[nextSide[sideNum * 4 + i] * 4 + j] == sideNum)
                    l = j;
            for (j = 0; j < 3; j++) {
                switch (l) {
                    case 0:
                        sideCols[nextSide[sideNum * 4 + i] * 9 + j] = buffer[k];
                        break;
                    case 1:
                        sideCols[nextSide[sideNum * 4 + i] * 9 + 2 + 3 * j] = buffer[k];
                        break;
                    case 2:
                        sideCols[nextSide[sideNum * 4 + i] * 9 + 8 - j] = buffer[k];
                        break;
                    case 3:
                        sideCols[nextSide[sideNum * 4 + i] * 9 + 6 - 3 * j] = buffer[k];
                        break;
                    default:
                        break;
                }
                k++;
            }
        }
    }

    /**
     * 
     */
    public void paintComponent(Graphics g) {
        dragReg = 0;
        if (offGraphics == null) {
            // Double buffer
            offImage = createImage(120, 120);
            offGraphics = offImage.getGraphics();
        }
        offGraphics.setColor(bgcolor); // Clear drawing buffer
        offGraphics.fillRect(0, 0, 120, 120);
        if (naturalState)
            fixBlock(eye, eX, eY, corners, mainBlocks, 0); // Draw cube
        else {
            copyVec(eye, 0, Teye, 0); // In twisted state? Compute top observer
            copyVec(eX, 0, TeX, 0);
            Cphi = Math.cos(phi + phibase);
            Sphi = -Math.sin(phi + phibase);
            // Twist around which axis?
            switch (twistSide) {
                case 0: // z
                    Teye[0] = Cphi * eye[0] + Sphi * eye[1];
                    TeX[0] = Cphi * eX[0] + Sphi * eX[1];
                    Teye[1] = -Sphi * eye[0] + Cphi * eye[1];
                    TeX[1] = -Sphi * eX[0] + Cphi * eX[1];
                    break;
                case 1: // -z
                    Teye[0] = Cphi * eye[0] - Sphi * eye[1];
                    TeX[0] = Cphi * eX[0] - Sphi * eX[1];
                    Teye[1] = Sphi * eye[0] + Cphi * eye[1];
                    TeX[1] = Sphi * eX[0] + Cphi * eX[1];
                    break;
                case 2: // -y
                    Teye[0] = Cphi * eye[0] - Sphi * eye[2];
                    TeX[0] = Cphi * eX[0] - Sphi * eX[2];
                    Teye[2] = Sphi * eye[0] + Cphi * eye[2];
                    TeX[2] = Sphi * eX[0] + Cphi * eX[2];
                    break;
                case 3: // x
                    Teye[1] = Cphi * eye[1] + Sphi * eye[2];
                    TeX[1] = Cphi * eX[1] + Sphi * eX[2];
                    Teye[2] = -Sphi * eye[1] + Cphi * eye[2];
                    TeX[2] = -Sphi * eX[1] + Cphi * eX[2];
                    break;
                case 4: // y
                    Teye[0] = Cphi * eye[0] + Sphi * eye[2];
                    TeX[0] = Cphi * eX[0] + Sphi * eX[2];
                    Teye[2] = -Sphi * eye[0] + Cphi * eye[2];
                    TeX[2] = -Sphi * eX[0] + Cphi * eX[2];
                    break;
                case 5: // -x
                    Teye[1] = Cphi * eye[1] - Sphi * eye[2];
                    TeX[1] = Cphi * eX[1] - Sphi * eX[2];
                    Teye[2] = Sphi * eye[1] + Cphi * eye[2];
                    TeX[2] = Sphi * eX[1] + Cphi * eX[2];
                    break;
                default:
                    break;
            }
            vecProd(Teye, 0, TeX, 0, TeY, 0);
            if (scalProd(eye, 0, sideVec, twistSide * 3) < 0) // Top facing away? Draw it first
            {
                fixBlock(Teye, TeX, TeY, topCorners, topBlocks, 2);
                fixBlock(eye, eX, eY, botCorners, botBlocks, 1);
            } else {
                fixBlock(eye, eX, eY, botCorners, botBlocks, 1);
                fixBlock(Teye, TeX, TeY, topCorners, topBlocks, 2);
            }
        }
        g.drawImage(offImage, 0, 0, this);
    }

    /**
     * 
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Draw cube or sub-cube.
     * @param beye
     * @param beX
     * @param beY
     * @param bcorners
     * @param bblocks
     * @param mode
     */
    public void fixBlock(double beye[], double beX[], double beY[],
                         double bcorners[], int bblocks[], int mode) {
        copyVec(beye, 0, light, 0);
        scalMult(light, 0, -3);
        addVec(beX, 0, light, 0);
        subVec(beY, 0, light, 0);

        // Project 3D co-ordinates into 2D screen ones
        for (i = 0; i < 8; i++) {
            newCoord[i * 2] = (60 + 35.1 * scalProd(bcorners, i * 3, beX, 0));
            newCoord[i * 2 + 1] = (60 - 35.1 * scalProd(bcorners, i * 3, beY, 0));
        }

        for (i = 0; i < 6; i++) {
            // Face towards us? Draw it.
            if (scalProd(beye, 0, sideVec, 3 * i) > 0.001) {
                k = (int) (9.6 * (1 - cosAng(light, 0, sideVec, 3 * i)));
                offGraphics.setColor(Color.black);
                // Find corner co-ordinates
                for (j = 0; j < 4; j++) {
                    rectX[j] = (int) newCoord[2 * sides[i * 4 + j]];
                    rectY[j] = (int) newCoord[2 * sides[i * 4 + j] + 1];
                }
                offGraphics.fillPolygon(rectX, rectY, 4); // First draw black
                sideW = bblocks[i * 4 + 1] - bblocks[i * 4];
                sideH = bblocks[i * 4 + 3] - bblocks[i * 4 + 2];
                if (sideW > 0) {
                    sx = newCoord[2 * sides[i * 4]];
                    sy = newCoord[2 * sides[i * 4] + 1];
                    sdxh = (newCoord[2 * sides[i * 4 + 1]] - sx) / sideW;
                    sdxv = (newCoord[2 * sides[i * 4 + 3]] - sx) / sideH;
                    sdyh = (newCoord[2 * sides[i * 4 + 1] + 1] - sy) / sideW;
                    sdyv = (newCoord[2 * sides[i * 4 + 3] + 1] - sy) / sideH;
                    p = bblocks[i * 4 + 2];
                    // Then draw colored fields
                    for (n = 0; n < sideH; n++) {
                        q = bblocks[i * 4];
                        for (o = 0; o < sideW; o++) {
                            rectX[0] = (int) (sx + (o + 0.1) * sdxh + (n + 0.1) * sdxv);
                            rectX[1] = (int) (sx + (o + 0.9) * sdxh + (n + 0.1) * sdxv);
                            rectX[2] = (int) (sx + (o + 0.9) * sdxh + (n + 0.9) * sdxv);
                            rectX[3] = (int) (sx + (o + 0.1) * sdxh + (n + 0.9) * sdxv);
                            rectY[0] = (int) (sy + (o + 0.1) * sdyh + (n + 0.1) * sdyv);
                            rectY[1] = (int) (sy + (o + 0.9) * sdyh + (n + 0.1) * sdyv);
                            rectY[2] = (int) (sy + (o + 0.9) * sdyh + (n + 0.9) * sdyv);
                            rectY[3] = (int) (sy + (o + 0.1) * sdyh + (n + 0.9) * sdyv);
                            offGraphics.setColor(colList[20 * sideCols[i * 9 + p * 3 + q] + k]);
                            offGraphics.fillPolygon(rectX, rectY, 4);
                            q++;
                        }
                        p++;
                    }
                }
                // Determine allowed drag regions and directions
                switch (mode) {
                    case 0: // Just the normal cube
                        t1 = sx;
                        t2 = sy;
                        t3 = sdxh;
                        t4 = sdyh;
                        t5 = sdxv;
                        t6 = sdyv;
                        for (j = 0; j < 4; j++) {
                            dragCorn[8 * dragReg] = t1;
                            dragCorn[8 * dragReg + 4] = t2;
                            dragCorn[8 * dragReg + 3] = t1 + t5;
                            dragCorn[8 * dragReg + 7] = t2 + t6;
                            t1 = t1 + t3 * 3;
                            t2 = t2 + t4 * 3;
                            dragCorn[8 * dragReg + 1] = t1;
                            dragCorn[8 * dragReg + 5] = t2;
                            dragCorn[8 * dragReg + 2] = t1 + t5;
                            dragCorn[8 * dragReg + 6] = t2 + t6;
                            dragDir[dragReg * 2] = t3 * twistDir[i * 4 + j];
                            dragDir[dragReg * 2 + 1] = t4 * twistDir[i * 4 + j];
                            d = t3;
                            t3 = t5;
                            t5 = -d;
                            d = t4;
                            t4 = t6;
                            t6 = -d;
                            nearSide[dragReg] = nextSide[i * 4 + j];
                            dragReg++;
                        }
                        break;
                    case 1: // The large sub-cube
                        break;
                    case 2: // The small sub-cube (twistable part)
                        if ((i != twistSide) && (sideW > 0)) {
                            if (sideW == 3) // Determine positive drag direction
                                if (bblocks[i * 4 + 2] == 0) {
                                    dragDir[dragReg * 2] = sdxh * twistDir[i * 4];
                                    dragDir[dragReg * 2 + 1] = sdyh * twistDir[i * 4];
                                } else {
                                    dragDir[dragReg * 2] = -sdxh * twistDir[i * 4 + 2];
                                    dragDir[dragReg * 2 + 1] = -sdyh * twistDir[i * 4 + 2];
                                }
                            else if (bblocks[i * 4] == 0) {
                                dragDir[dragReg * 2] = -sdxv * twistDir[i * 4 + 3];
                                dragDir[dragReg * 2 + 1] = -sdyv * twistDir[i * 4 + 3];
                            } else {
                                dragDir[dragReg * 2] = sdxv * twistDir[i * 4 + 1];
                                dragDir[dragReg * 2 + 1] = sdyv * twistDir[i * 4 + 1];
                            }
                            for (j = 0; j < 4; j++) {
                                dragCorn[dragReg * 8 + j] = newCoord[2 * sides[i * 4 + j]];
                                dragCorn[dragReg * 8 + 4 + j] = newCoord[2 * sides[i * 4 + j] + 1];
                            }
                            nearSide[dragReg] = twistSide;
                            dragReg++;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 
     * @param argv
     */
    public static void main(String[] argv) {
        JFrame f = new JFrame("Rubik's Cube");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rubik rubik = new Rubik();
        rubik.init();
        rubik.setPreferredSize(new Dimension(120, 120));
        f.add(rubik, BorderLayout.CENTER);
        rubik.requestFocus();
        f.pack();
        f.setVisible(true);
    }
}
