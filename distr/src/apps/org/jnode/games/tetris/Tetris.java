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

package org.jnode.games.tetris;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Simple Tetris game.
 * @author Levente S\u00e1ntha
 */
public class Tetris extends JComponent implements KeyListener, MouseListener {
    private static final int[][][][] BLOCKS = {
        // * * *
        //   *
        {{{0, 0}, {1, 0}, {2, 0}, {1, 1}},
            {{1, 0}, {0, 1}, {1, 1}, {1, 2}},
            {{1, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{0, 0}, {0, 1}, {1, 1}, {0, 2}}},
        // * * * *
        {{{0, 0}, {1, 0}, {2, 0}, {3, 0}},
            {{0, 0}, {0, 1}, {0, 2}, {0, 3}},
            {{0, 0}, {1, 0}, {2, 0}, {3, 0}},
            {{0, 0}, {0, 1}, {0, 2}, {0, 3}}},
        // * * *
        // *
        {{{0, 0}, {1, 0}, {2, 0}, {0, 1}},
            {{0, 0}, {1, 0}, {1, 1}, {1, 2}},
            {{2, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{0, 0}, {0, 1}, {0, 2}, {1, 2}}},
        // * * *
        //     *
        {{{0, 0}, {1, 0}, {2, 0}, {2, 1}},
            {{1, 0}, {1, 1}, {0, 2}, {1, 2}},
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}},
            {{0, 0}, {1, 0}, {0, 1}, {0, 2}}},
        // * *
        //   * *
        {{{1, 0}, {2, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {0, 1}, {1, 1}, {1, 2}},
            {{1, 0}, {2, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {0, 1}, {1, 1}, {1, 2}}},
        //   * *
        // * *
        {{{0, 0}, {1, 0}, {1, 1}, {2, 1}},
            {{1, 0}, {0, 1}, {1, 1}, {0, 2}},
            {{0, 0}, {1, 0}, {1, 1}, {2, 1}},
            {{1, 0}, {0, 1}, {1, 1}, {0, 2}}},
        // * *
        // * *
        {{{0, 0}, {1, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}}}};

    private static final int[][] DIMS = {
        {3, 2}, {2, 3}, {3, 2},
        {2, 3}, {4, 1}, {1, 4},
        {4, 1}, {1, 4}, {3, 2},
        {2, 3}, {3, 2}, {2, 3},
        {3, 2}, {2, 3}, {3, 2},
        {2, 3}, {3, 2}, {2, 3},
        {3, 2}, {2, 3}, {3, 2},
        {2, 3}, {3, 2}, {2, 3},
        {2, 2}, {2, 2}, {2, 2},
        {2, 2}};

    private static final int CELL = 20;

    private static final int WIDTH_C = 10;

    private static final int HEIGHT_C = 20;

    private static final Color[] COLORS = {Color.BLACK, Color.YELLOW,
        Color.RED, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.ORANGE,
        Color.LIGHT_GRAY, Color.DARK_GRAY};

    private int[][] WORLD = new int[WIDTH_C + 2][HEIGHT_C + 2];

    private int next_si = 0;

    private int si = 0;

    private int next_bi = 0;

    private int bi = 0;

    private int x = 1;

    private int y = 1;

    private boolean pause = false;

    private boolean up = true;

    private Thread thread;

    private int score;

    private boolean end = false;

    private Image img;

    private static final Dimension DIM = new Dimension((WIDTH_C + 2) * CELL,
        (HEIGHT_C + 5 + 2) * CELL);

    private Random si_rnd = new Random();

    private Random bi_rnd = new Random();

    private long delay = 500;

    private final Runnable runRepaint = new Runnable() {
        public void run() {
            repaint();
        }
    };

    Tetris() {
        setOpaque(false);
        for (int i = 0; i < WIDTH_C + 2; i++) {
            for (int j = 0; j < HEIGHT_C + 2; j++) {
                if (i == 0 || j == 0 || i == WIDTH_C + 1 || j == HEIGHT_C + 1)
                    WORLD[i][j] = COLORS.length - 1;
                else
                    WORLD[i][j] = 0;
            }
        }
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        setRequestFocusEnabled(true);
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    private int darken(int i) {
        int r = i - 64;
        return r < 0 ? 0 : r;
    }

    private int lighten(int i) {
        int r = i + 64;
        return r > 255 ? 255 : r;
    }

    private void paintBox(Graphics g, int i, int j, Color c) {
        Color dc = new Color(darken(c.getRed()), darken(c.getGreen()), darken(c
            .getBlue()));
        Color lc = new Color(lighten(c.getRed()), lighten(c.getGreen()),
            lighten(c.getBlue()));
        g.setColor(c);
        g.fillRect(i * CELL, j * CELL, CELL - 1, CELL - 1);
        g.setColor(dc);
        g.drawLine(i * CELL, (j + 1) * CELL - 1, (i + 1) * CELL - 1, (j + 1) * CELL - 1);
        g.drawLine((i + 1) * CELL - 1, (j + 1) * CELL - 1, (i + 1) * CELL - 1, j * CELL);
        g.setColor(lc);
        g.drawLine(i * CELL, (j + 1) * CELL - 1, i * CELL, j * CELL);
        g.drawLine(i * CELL, j * CELL, (i + 1) * CELL - 1, j * CELL);
    }

    /**
     * Update the screen.
     *
     * @param g the graphics context
     * @see javax.swing.JComponent#update(java.awt.Graphics)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Paint the game graphics.
     * @param g the graphics context
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (img == null) {
            img = createImage(DIM.width, DIM.height);
        }
        Graphics g2 = img.getGraphics();
        g2.setColor(COLORS[0]);
        g2.fillRect(0, 0, DIM.width, DIM.height);
        for (int i = 0; i < WIDTH_C + 2; i++) {
            for (int j = 0; j < HEIGHT_C + 2; j++) {
                int ci = WORLD[i][j];
                if (ci > 0)
                    paintBox(g2, i, j, COLORS[ci]);
            }
        }

        Color c = COLORS[COLORS.length - 1];
        for (int i = 0; i < WIDTH_C + 2; i++) {
            paintBox(g2, i, HEIGHT_C + 6, c);
        }
        for (int j = 0; j < 4; j++) {
            paintBox(g2, 0, HEIGHT_C + 2 + j, c);
            paintBox(g2, 5, HEIGHT_C + 2 + j, c);
            // paintBox(g2, 6, HEIGHT_C + 2 +j, c );
            paintBox(g2, WIDTH_C + 1, HEIGHT_C + 2 + j, c);
        }

        if (isUp()) {
            int[][] b = BLOCKS[si][bi];
            for (int i = 0; i < b.length; i++) {
                paintBox(g2, x + b[i][0], y + b[i][1], COLORS[si + 1]);
            }

            g2.setColor(Color.WHITE);
            g2.drawString("SCORE:", CELL + 2, (HEIGHT_C + 4) * CELL - 4);
            g2.drawString(String.valueOf(score), 2 * CELL, (HEIGHT_C + 5) * CELL - 4);
            b = BLOCKS[next_si][next_bi];
            for (int i = 0; i < b.length; i++) {
                paintBox(g2, 7 + b[i][0], HEIGHT_C + 2 + b[i][1],
                    COLORS[next_si + 1]);
            }
        } else if (end) {
            g2.setColor(Color.BLACK);
            g2.fillRect(2 * CELL, 9 * CELL, 8 * CELL, 4 * CELL);
            g2.setColor(Color.WHITE);
            g2.drawRect(2 * CELL, 9 * CELL, 8 * CELL, 4 * CELL);
            g2.drawString("GAME OVER! SCORE: " + score, (WIDTH_C - 6) * CELL / 2 + 2,
                (HEIGHT_C + 2) * CELL / 2);
        }
        g2.dispose();
        g.drawImage(img, 0, 0, this);
    }

    private void rot(int i) {
        int t = (bi + i) % 4;
        if (hasRoom(t, x, y)) {
            bi = t;
        }
    }

    private void trans(int i) {
        int t = x + i;
        if (hasRoom(bi, t, y)) {
            x = t;
        }
    }

    private void fall() {
        while (hasRoom(bi, x, y + 1))
            y++;
        thread.interrupt();
    }

    /**
     * Handle keys.
     * @param e the key event
     */
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        if (kc == KeyEvent.VK_N) {
            newGame();
            return;
        }
        if (kc == KeyEvent.VK_P) {
            flipPause();
            return;
        }
        if (!isUp() || pause)
            return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                rot(1);
                break;
            case KeyEvent.VK_LEFT:
                trans(-1);
                break;
            case KeyEvent.VK_DOWN:
                rot(3);
                break;
            case KeyEvent.VK_RIGHT:
                trans(1);
                break;
            case KeyEvent.VK_SPACE:
                fall();
                break;
            case KeyEvent.VK_N:
                newGame();
                break;
            case KeyEvent.VK_P:
                flipPause();
                break;
            default:
                return;
        }
        SwingUtilities.invokeLater(runRepaint);
    }

    private void newGame() {
        setUp(false);
        if (thread != null) {
            if (pause) {
                flipPause();
            }
            try {
                thread.join();
            } catch (InterruptedException ignore) {
                //ignore
            }
        }
        for (int i = 0; i < WIDTH_C + 2; i++) {
            for (int j = 0; j < HEIGHT_C + 2; j++) {
                if (i == 0 || j == 0 || i == WIDTH_C + 1 || j == HEIGHT_C + 1)
                    WORLD[i][j] = COLORS.length - 1;
                else
                    WORLD[i][j] = 0;
            }
        }
        requestFocus();
        end = false;
        score = 0;
        si = si_rnd.nextInt(7);
        next_si = si_rnd.nextInt(7);
        bi = bi_rnd.nextInt(4);
        next_bi = bi_rnd.nextInt(4);
        x = 1 + bi_rnd.nextInt((WIDTH_C - DIMS[si * 4 + bi][0]));
        y = 0;
        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    long before, after, sleep;
                stop:
                    while (isUp()) {
                        before = System.currentTimeMillis();
                        synchronized (Tetris.class) {
                            while (pause) {
                                try {
                                    System.out.println("waiting");
                                    Tetris.class.wait();
                                    System.out.println("back from waiting");
                                } catch (InterruptedException ignore) {
                                    //ignore
                                }
                                if (!isUp())
                                    break stop;
                            }
                        }
                        if (hasRoom(bi, x, y + 1)) {
                            y++;
                            SwingUtilities.invokeLater(runRepaint);
                        } else {
                            newBlock();
                            if (!hasRoom(bi, x, y)) {
                                setUp(false);
                                end = true;
                                SwingUtilities.invokeLater(runRepaint);
                            }
                        }
                        after = System.currentTimeMillis();
                        sleep = delay - (after - before);
                        sleep = sleep < 0 ? delay : sleep;
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException ignore) {
                            //ignore
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }, "Tetris");
        setUp(true);
        thread.start();
    }

    private void flipPause() {
        synchronized (Tetris.class) {
            pause = !pause;
            if (!pause)
                Tetris.class.notifyAll();
        }
    }

    private void newBlock() {
        int[][] b = BLOCKS[si][bi];
        for (int i = 0; i < b.length; i++) {
            WORLD[x + b[i][0]][y + b[i][1]] = si + 1;
        }
        for (int i = 1; i < HEIGHT_C + 1; i++) {
            boolean full = true;
            for (int j = 1; j < WIDTH_C + 1; j++) {
                if (WORLD[j][i] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                int s = WIDTH_C;
                for (int j = 2; j < WIDTH_C + 1; j++) {
                    if (WORLD[j - 1][i] != WORLD[j][i]) {
                        --s;
                    }
                }
                score += s;
            }
            if (full && i > 1) {
                for (int k = 0; k < i - 1; k++) {
                    for (int j = 1; j < WIDTH_C + 1; j++) {
                        WORLD[j][i - k] = WORLD[j][i - k - 1];
                    }
                }
            }
        }
        si = next_si;
        next_si = si_rnd.nextInt(7);
        bi = next_bi;
        next_bi = bi_rnd.nextInt(4);
        x = 1 + bi_rnd.nextInt((WIDTH_C - DIMS[si * 4 + bi][0]));
        y = 1;
    }

    private boolean hasRoom(int bi, int x, int y) {
        boolean hasRoom = true;
        int[][] b = BLOCKS[si][bi];
        for (int i = 0; i < b.length; i++) {
            if (WORLD[x + b[i][0]][y + b[i][1]] != 0) {
                hasRoom = false;
                break;
            }
        }
        return hasRoom;
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return DIM;
    }

    /**
     * @see javax.swing.JComponent#getMinimumSize()
     * @return
     */
    public Dimension getMinimumSize() {
        return DIM;
    }

    /**
     * @see javax.swing.JComponent#getMaximumSize()
     * @return
     */
    public Dimension getMaximumSize() {
        return DIM;
    }

    /**
     * Handle mouse input.
     */
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (this.contains(e.getX(), e.getY())) {
                if (!this.hasFocus() && this.isRequestFocusEnabled()) {
                    this.requestFocus();
                }
            }
        }
    }

    /**
     * Unused.
     */
    public void keyReleased(KeyEvent e) {

    }

    /**
     * Unused.
     */
    public void keyTyped(KeyEvent e) {

    }

    /**
     * Unused.
     */
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Unused.
     */
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Unused.
     */
    public void mouseExited(MouseEvent e) {

    }

    /**
     * Unused.
     */
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Start Tetris.
     */
    public static void main(final String[] argv) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int delay = 500;

                try {
                    if (argv.length > 0) delay = Integer.parseInt(argv[0]);
                } catch (Exception e) {
                    // ignore
                }
                JFrame frame = new JFrame("Tetris");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Tetris tetris = new Tetris();
                tetris.delay = delay;
                frame.add(tetris, BorderLayout.CENTER);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(DIM.width + 7, DIM.height + CELL + CELL / 2);
                frame.setVisible(true);
                tetris.requestFocus();
                tetris.newGame();
            }
        });
    }

    private synchronized boolean isUp() {
        return up;
    }

    private synchronized void setUp(boolean up) {
        this.up = up;
    }
}
