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
 
package org.jnode.test.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

/**
 * @author Levente S\u00e1ntha
 */
public class BoxWorld extends JPanel implements WindowListener, KeyListener,
        MouseListener, MouseMotionListener, ActionListener, ItemListener {

    // ***************** WORLD DATA ****************************
    private int[][] vecs = {
            { 81, -766241563, -831219482, -696967897, -863854201, -896130840,
                    -661281463, -828168022, -793433846, -758894261, 1073742156,
                    10, -829120249, 20, -795533048, 1073742122, 19, -862609146,
                    1073742123, 2382 },
            // 1
            { 105, -834465628, -900427516, -765290203, -630843034, -697000793,
                    -863854169, -729505560, -862543512, -861494039, -860444342,
                    -659182261, -826068565, -725307124, 50, -865856283,
                    -796648249, -728455927, -794580631, -660231894, -759943957,
                    12, -831252250, 11, -661249657, 1582 },
            // 2
            { 97, -867003259, -766241563, -731768507, -965338778, -831316857,
                    -964289273, -829315704, -962189975, -693917558, -860705653,
                    -759943957, -2147111605, 58, -832366426, -764110586,
                    -930767545, -763092792, -795728536, -760993463, 1073742154,
                    20, -729571161, 1073742121, 19, -895277911, 1073742026,
                    1350 },
            // 3
            { 73, -732654331, -798779035, -697066138, -695983896, -795630200,
                    -660362871, -825873173, -725307124, -2147077780, 26,
                    -730523354, -694901432, -2147112630, 20, -728456921,
                    -2147144406, 19, -759943926, -2147111605, 2110 },
            // 4
            { 89, -767291164, -732719804, -832170651, -831316602, -663413497,
                    -796712537, -862543544, -861493911, -827118166, -726356725,
                    -625595029, 50, -731637467, -697000602, -662331096,
                    -661314263, -727406326, 1073742218, 12, -693885658, 11,
                    -828136216, 2094 },
            // 5
            { 169, -801927997, -701166301, -935227005, -666693468, -766241691,
                    -966388411, -664561498, -898523738, -964256441, -628776792,
                    -694999959, -928929367, -626677462, -927487542, -793531189,
                    -725470741, -925388436, -825019219, -724257523, -623495827,
                    -2146978355, 130, -767291164, -933916348, -699198299,
                    -798812026, -698017498, -797762425, -863886969, -729505560,
                    -795728504, -895081143, -794580790, -894031542, -659182261,
                    -859655765, -758894356, -590958196, 20, -796615449,
                    -2147179287, 19, -796549915, -2147177303, 3678 },
            // 6
            { 105, -766241563, -665479867, -832366171, -864642746, -863593145,
                    -762108504, -928602807, -727504726, -927553142, -893242997,
                    -792481588, -691719892, 1073742220, 58, -698050298,
                    -764142361, -796517017, -829021880, -827972247, -726292182,
                    1073742187, 20, -695951128, -2147210999, 19, -827118421,
                    -2147177205, 3126 },
            // 7
            { 89, -732654331, -631892635, -798779194, -864903770, -629826233,
                    -595418681, -594107223, -828168022, -727406326, -692769366,
                    -2147046005, 42, -698017498, -663478906, -662331096,
                    -661314295, -2147079830, 20, -729538265, -2147113687, 19,
                    -830267161, -2147244855, 3398 },
            // 8
            { 129, -799828795, -699067099, -598305403, -731604794, -596435514,
                    -729505592, -862510744, -594139895, -794613590, -559470198,
                    -892883797, -891834004, -791431987, -690670291, -589908595,
                    1073742317, 82, -698083098, -831088250, -697000697,
                    -796484217, -627858040, -860444438, -692802293, -859361909,
                    -758894356, -590991028, 20, -628907705, -625660693, 19,
                    -694868695, -693819094, 3430 },
            // 9
            { 73, -699067099, -598305403, -831055578, -596337401, -828956440,
                    -827906647, -727406326, -692769366, -2147046005, 34,
                    -664430266, -762929754, -693885528, 1073742218, 20,
                    -663380665, -2147179192, 19, -728358552, -2147080855, 2118 },
            // 10
            { 105, -865953658, -698017562, -597255802, -831382425, -964223673,
                    -763092760, -963174072, -928929335, -593057494, -860705653,
                    -759943957, -659182261, 1073742251, 58, -697099097,
                    -930701945, -628743992, -695032695, -861754967, -727439126,
                    1073742218, 20, -862609240, 1073742217, 19, -762043159,
                    1073742153, 2902 },
            // 11
            { 73, -732654331, -865725083, -664528666, -629793593, -862543672,
                    -627825431, -626644726, -726356725, -2147078805, 42,
                    -831186650, -830104313, -761913080, -727406199, 1073742186,
                    20, -728424153, 1073742185, 19, -695951033, 1073742153,
                    2870 },
            // 12
            { 81, -800878396, -700116700, -665643644, -664495930, -863626041,
                    -661445240, -660297526, -793531189, -692769493, 1073742219,
                    34, -830104345, -695918328, -828005143, -2147112694, 28,
                    -764142299, -795565753, -2147146455, 27, -732687131,
                    -798779035, -2147116730, 2646 },
            // 20
            { 113, -868052860, -767291164, -666529468, -967764572, -999910267,
                    -765224762, -998860474, -997810809, -830267288, -662363864,
                    -962190231, -861755254, -760993558, -660231862, 66,
                    -833416027, -699067099, -933127803, -664430426, -764175257,
                    -929718136, -829217623, -694868695, 20, -697131770,
                    -2147213144, 19, -797762299, -2147211961, 1086 },
            // 30
            { 225, -937326526, -635238238, -534279742, -835515325, -734753533,
                    -633991837, -968814077, -900622844, -699067163, -531196539,
                    -698017690, -563701370, -864969210, -562783001, -763256760,
                    -862902808, -995646039, -794580790, -660264662, -559470166,
                    -927880117, -726455125, -591187509, -589974323, -656033586,
                    -856213074, -755745553, -2147107505, 170, -600667005,
                    -868085277, -767291164, -666529468, -933785148, -766241595,
                    -932735547, -798779226, -797795034, -596239033, -830267256,
                    -662331064, -963173976, -829250423, -728455927, -594139799,
                    -895342486, -759943957, -625595029, -624578228,
                    -2147043955, 36, -865693532, -697164346, -796745305,
                    -2147077782, 35, -758894356, -757844755, -790382259,
                    -2147141330, 798 }
             };

    // ************** DIMENSIONS ********************
    private static final int CELL_SIZE = 20;

    private static final int X_SIZE = 20;

    private static final int Y_SIZE = 20;

    private static final int BW_WIDTH = X_SIZE * CELL_SIZE;

    private static final int BW_HEIGHT = Y_SIZE * CELL_SIZE;

    // *************** MODE CODES *******************
    private static final int START_MODE = 0;

    private static final int PLAY_MODE = 1;

    private static final int END_MODE = 2;

    private static final int HELP_MODE = 3;

    // *********** MOVE CODES ****************
    private static final int LEFT_MOVE = 1;

    private static final int RIGHT_MOVE = LEFT_MOVE + 1;

    private static final int UP_MOVE = RIGHT_MOVE + 1;

    private static final int DOWN_MOVE = UP_MOVE + 1;

    private static final int PUSH_BASE = DOWN_MOVE;

    private static final int LEFT_PUSH = PUSH_BASE + 1;

    private static final int RIGHT_PUSH = LEFT_PUSH + 1;

    private static final int UP_PUSH = RIGHT_PUSH + 1;

    private static final int DOWN_PUSH = UP_PUSH + 1;

    private static final int DRAW_BASE = DOWN_PUSH;

    private static final int LEFT_DRAW = DRAW_BASE + 1;

    private static final int RIGHT_DRAW = LEFT_DRAW + 1;

    private static final int UP_DRAW = RIGHT_DRAW + 1;

    private static final int DOWN_DRAW = UP_DRAW + 1;

    // **************** STATE CODE *****************
    private static final int NULL_ST = 0;

    private static final int WALL_ST = 1;

    private static final int BACK_ST = 2;

    private static final int GOAL_ST = 3;

    private static final int BOX_ST = 4;

    private static final int FIT_ST = 5;

    private static final int MAN_ST = 6;

    private static final int MAN_GOAL_ST = 7;

    private int gameMode = PLAY_MODE;

    // current world ID
    private int worldId = 0;

    // current world data
    private int[] vec = vecs[worldId];

    // current world
    private int[][] state = new int[X_SIZE][Y_SIZE];

    // goals list
    private int[] goals;

    // suspends the current movement at an obstacle
    private boolean repeat = true;

    // end state
    // private boolean endState = false;

    // controlls the speed of animation
    private int SLEEP = 5;

    // current and previous position of the man
    private int x, y, xo, yo;

    // popup menu
    private PopupMenu menu;

    private CheckboxMenuItem beep;

    // **************** APPLET METHODS *************************
    // init applet
    public void init() {

        setBackground(Color.lightGray);

        x = xo = 10;
        y = yo = 10;
        // loading the first wold in the world table
        loadWorld();

        // registering listeners
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        requestFocus();
        /*
        // building the popup menu
        menu = new PopupMenu();
        MenuItem start = new MenuItem("Start");
        start.addActionListener(this);
        start.setActionCommand("start");
        menu.add(start);
        MenuItem next = new MenuItem("Next");
        next.addActionListener(this);
        next.setActionCommand("next");
        menu.add(next);
        MenuItem previous = new MenuItem("Previous");
        previous.addActionListener(this);
        previous.setActionCommand("previous");
        menu.add(previous);

        menu.addSeparator();

        MenuItem undo = new MenuItem("Undo");
        undo.addActionListener(this);
        undo.setActionCommand("undo");
        menu.add(undo);
        MenuItem redo = new MenuItem("Redo");
        redo.addActionListener(this);
        redo.setActionCommand("redo");
        menu.add(redo);

        menu.addSeparator();

        beep = new CheckboxMenuItem("Beep", beepOn);
        beep.addItemListener(this);
        beep.setActionCommand("beep");
        menu.add(beep);

        add(menu);
        */
    }

    // start applet
    public void start() {
        requestFocus();
    }

    private void loadWorld() {
        int sh, s, n, xx, yy, ind = 0;
        // int len = vec.length;
        List<Integer> gv = new ArrayList<Integer>();
        for (int i = 0; i < 4; i++) {
            s = 0x00000007 & vec[ind];
            n = vec[ind] >> 3;
            ind++;
            for (int j = 0; j < n; j++, ind++) {
                sh = 0x00000003 & (vec[ind] >> 30);
                // int m = 0xFFFFFFFF;
                // int ssh = 0;
                int v = vec[ind];
                for (int k = 0; k < sh; k++) {
                    xx = 0x0000001F & v;
                    v = v >> 5;
                    yy = 0x0000001F & v;
                    v = v >> 5;
                    if (xx < X_SIZE && yy < Y_SIZE) {
                        if (s == GOAL_ST) {
                            gv.add(xx | (yy << 16));
                        }
                        if (s == BOX_ST && state[xx][yy] == GOAL_ST
                                || s == GOAL_ST && state[xx][yy] == BOX_ST) {
                            state[xx][yy] = FIT_ST;
                        } else if (s == MAN_ST && state[xx][yy] == GOAL_ST
                                || s == GOAL_ST && state[xx][yy] == MAN_ST) {
                            state[xx][yy] = MAN_GOAL_ST;
                        } else {
                            state[xx][yy] = s;
                        }
                    }
                }
            }
        }
        final int goal_cnt = gv.size();
        goals = new int[goal_cnt];
        int i = 0;
        for (int v : gv) {
            goals[i++] = v;
        }
        int v = vec[ind] >> 3;
        xx = 0x0000001F & v;
        v = v >> 5;
        yy = 0x0000001F & v;
        state[xx][yy] = MAN_ST;
        x = xx;
        y = yy;
        // endState = false;
    }

    // ************************************************************************************
    // ******************************* EVENT HANDLING
    // *************************************
    // ************************************************************************************

    // ----------------------------------KEY
    // EVENTS----------------------------------------
    public void keyPressed(KeyEvent e) {
        if (gameMode == START_MODE) {
            controlWorld(KeyEvent.VK_S);
        } else if (gameMode == END_MODE) {
            controlWorld(KeyEvent.VK_N);
        } else {
            int key = e.getKeyCode();
            int code = key == KeyEvent.VK_LEFT ? LEFT_MOVE
                    : key == KeyEvent.VK_RIGHT ? RIGHT_MOVE
                            : key == KeyEvent.VK_UP ? UP_MOVE
                                    : key == KeyEvent.VK_DOWN ? DOWN_MOVE : -1;
            if (code > 0) {
                if (gameMode == PLAY_MODE) {
                    changeState(code);
                }
            } else {
                controlWorld(key);
            }
        }
    }

    // ----------------------------------MOUSE
    // EVENTS---------------------------------------
    public void mousePressed(MouseEvent e) {
        if (gameMode != PLAY_MODE) {
            changeMode();
        } else {
            if (e.isPopupTrigger()) {
//                menu.show(this, e.getX(), e.getY());
                return;
            }

            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
                return;
            }

            moveTo(e.getX(), e.getY());
        }
    }

    public void mouseClicked(MouseEvent e) { // changeMode() ;
    }

    public void mouseDragged(MouseEvent e) {
        /*
         * if(e.isPopupTrigger()){ menu.show(this,e.getX(),e.getY()); return; }
         * if((e.getModifiers()& e.BUTTON1_MASK) == 0)
         */
        moveTo2(e.getX(), e.getY());
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
//            menu.show(this, e.getX(), e.getY());
        }
    }

    // ----------------------------------ACTION
    // EVENTS-------------------------------------
    // FROM MENU
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("start".equals(command)) {
            startWorld();
        } else if ("next".equals(command)) {
            nextWorld();
        } else if ("previous".equals(command)) {
            previousWorld();
        } else if ("undo".equals(command)) {
            undo();
        } else if ("redo".equals(command)) {
            redo();
        }
    }

    // ----------------------------------ITEM
    // EVENTS----------------------------------------
    // CHECKBOX MENUITEM
    public void itemStateChanged(ItemEvent e) {
        toggleBeep();
    }

    // *************************************************************************************
    // ****************GAME CONTROL WITH
    // MOUSE**********************************************
    // *************************************************************************************
    private void moveTo(int xpos, int ypos) {
        repeat = true;
        int x1 = ypos / CELL_SIZE;
        int y1 = xpos / CELL_SIZE;
        int d;
        boolean valid = true;
        int st, bc = 0;
        if (x1 == x && y1 != y) {
            if (y1 > y) {
                for (int i = y; i <= y1; i++) {
                    st = state[x][i];
                    if (st == WALL_ST || st == NULL_ST) {
                        valid = false;
                        break;
                    } else if (st == BOX_ST || st == FIT_ST) {
                        bc++;
                        if (bc > 1) {
                            valid = false;
                            break;
                        }
                    }
                }
            } else {
                for (int i = y1; i <= y; i++) {
                    st = state[x][i];
                    if (st == WALL_ST || st == NULL_ST) {
                        valid = false;
                        break;
                    } else if (st == BOX_ST || st == FIT_ST) {
                        bc++;
                        if (bc > 1) {
                            valid = false;
                            break;
                        }
                    }
                }
            }
        } else if (x1 != x && y1 == y) {
            if (x1 > x) {
                for (int i = x; i <= x1; i++) {
                    st = state[i][y];
                    if (st == WALL_ST || st == NULL_ST) {
                        valid = false;
                        break;
                    } else if (st == BOX_ST || st == FIT_ST) {
                        bc++;
                        if (bc > 1) {
                            valid = false;
                            break;
                        }
                    }
                }
            } else {
                for (int i = x1; i <= x; i++) {
                    st = state[i][y];
                    if (st == WALL_ST || st == NULL_ST) {
                        valid = false;
                        break;
                    } else if (st == BOX_ST || st == FIT_ST) {
                        bc++;
                        if (bc > 1) {
                            valid = false;
                            break;
                        }
                    }
                }
            }
        } else {
            valid = false;
        }

        if (valid) {
            if (x1 == x && y1 != y) {
                if (y1 > y) {
                    d = y1 - y;
                    for (int i = 0; i < d && repeat; i++) {
                        changeState(RIGHT_MOVE);
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e1) {
                        }
                    }
                } else {
                    d = y - y1;
                    for (int i = 0; i < d && repeat; i++) {
                        changeState(LEFT_MOVE);
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            } else if (x1 != x && y1 == y) {
                if (x1 > x) {
                    d = x1 - x;
                    for (int i = 0; i < d && repeat; i++) {
                        changeState(DOWN_MOVE);
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e1) {
                        }
                    }
                } else {
                    d = x - x1;
                    for (int i = 0; i < d && repeat; i++) {
                        changeState(UP_MOVE);
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
            }
        } else {
            List<Point> path = findPath(new Point(y, x), new Point(y1, x1));

            if (path.size() > 1) {
                Point cpnt, opnt;
                opnt = (Point) path.get(0);
                int psize = path.size();
                for (int i = 1; i < psize && repeat; i++) {
                    cpnt = (Point) path.get(i);
                    moveOne(cpnt.y, cpnt.x, opnt.y, opnt.x);
                    opnt = cpnt;
                    try {
                        Thread.sleep(SLEEP);
                    } catch (InterruptedException e1) {
                    }
                }
            }
        }
    }

    private void moveOne(int x1, int y1, int x, int y) {

        int dx = x - x1, dy = y - y1;

        if (Math.abs(dx) + Math.abs(dy) != 1) {
            return;
        }

        if (dx == -1) {
            changeState(DOWN_MOVE);
        } else if (dx == 1) {
            changeState(UP_MOVE);
        } else if (dy == -1) {
            changeState(RIGHT_MOVE);
        } else if (dy == 1) {
            changeState(LEFT_MOVE);
        }
    }

    private List<Point> findPath(Point p1, Point p2) {
        List<Point> nlist = new ArrayList<Point>();
        List<Point> path = new ArrayList<Point>();
        Hashtable<Point, Point> vmap = new Hashtable<Point, Point>();
        nlist.add(p1);

        Point pp = p1, pp2 = null;
        int xx, yy, vi = 0;
        while (!(pp.equals(p2) || nlist.size() == vi)) {

            pp = (Point) nlist.get(vi++);

            xx = pp.x - 1;
            yy = pp.y;
            if (xx >= 0
                    && (state[yy][xx] == BACK_ST || state[yy][xx] == GOAL_ST || state[yy][xx] == GOAL_ST)
                    && !vmap.containsKey(pp2 = new Point(xx, yy))
                    && !nlist.contains(pp2)) {
                nlist.add(pp2);
                vmap.put(pp2, pp);
            }

            xx = pp.x + 1;
            if (xx < X_SIZE
                    && (state[yy][xx] == BACK_ST || state[yy][xx] == GOAL_ST || state[yy][xx] == GOAL_ST)
                    && !vmap.containsKey(pp2 = new Point(xx, yy))
                    && !nlist.contains(pp2)) {
                nlist.add(pp2);
                vmap.put(pp2, pp);
            }

            xx = pp.x;
            yy = pp.y - 1;
            if (yy >= 0
                    && (state[yy][xx] == BACK_ST || state[yy][xx] == GOAL_ST || state[yy][xx] == GOAL_ST)
                    && !vmap.containsKey(pp2 = new Point(xx, yy))
                    && !nlist.contains(pp2)) {
                nlist.add(pp2);
                vmap.put(pp2, pp);
            }

            yy = pp.y + 1;
            if (yy < Y_SIZE
                    && (state[yy][xx] == BACK_ST || state[yy][xx] == GOAL_ST || state[yy][xx] == GOAL_ST)
                    && !vmap.containsKey(pp2 = new Point(xx, yy))
                    && !nlist.contains(pp2)) {
                nlist.add(pp2);
                vmap.put(pp2, pp);
            }
        }
        if (pp.equals(p2)) {
            do {
                path.add(0, pp);
                pp2 = pp;
                pp = (Point) vmap.get(pp);
            } while (pp2 != pp && pp != null);
        }
        return path;
    }

    public void moveTo2(int xpos, int ypos) {
        repeat = true;
        int x1 = ypos / CELL_SIZE;
        int y1 = xpos / CELL_SIZE;
        int d;

        if (state[x1][y1] == NULL_ST || state[x1][y1] == WALL_ST)
            return;
        if (x1 == x && y1 != y) {
            if (y1 > y) {
                d = y1 - y;
                for (int i = 0; i < d && repeat; i++) {
                    changeState(RIGHT_MOVE);
                }
            } else {
                d = y - y1;
                for (int i = 0; i < d && repeat; i++) {
                    changeState(LEFT_MOVE);
                }
            }
        } else if (x1 != x && y1 == y) {
            if (x1 > x) {
                d = x1 - x;
                for (int i = 0; i < d && repeat; i++) {
                    changeState(DOWN_MOVE);
                }
            } else {
                d = x - x1;
                for (int i = 0; i < d && repeat; i++) {
                    changeState(UP_MOVE);
                }
            }
        }
    }

    private void changeMode() {
        switch (gameMode) {
        case START_MODE: {
            gameMode = PLAY_MODE;
            break;
        }
        case PLAY_MODE: {
            break;
        }
        case HELP_MODE: {
            break;
        }
        case END_MODE: {
            nextWorld();
            gameMode = PLAY_MODE;
            break;
        }
        }
        update(getGraphics());

    }

    private void controlWorld(int key) {
        switch (key) {
        case KeyEvent.VK_N:
            // next world
            nextWorld();
            break;
        case KeyEvent.VK_P:
            // previous world
            previousWorld();
            break;
        case KeyEvent.VK_S:
            // restart
            startWorld();
            break;
        case KeyEvent.VK_U:
            // undo
            if (gameMode == PLAY_MODE)
                undo();
            break;
        case KeyEvent.VK_R:
            // redo
            if (gameMode == PLAY_MODE)
                redo();
            break;
        case KeyEvent.VK_B:
            // toggle beep
            toggleBeep();
            break;
        case KeyEvent.VK_H:
            // show help
            help();
            break;
        case KeyEvent.VK_C:
            // continue the game
            continueGame();
            break;
        }
    }

    private void nextWorld() {
        worldId++;
        if (worldId == 5) {
            // int x = worldId * 100;
        }
        if (worldId == vecs.length)
            worldId = 0;
        vec = vecs[worldId];
        controlWorld(KeyEvent.VK_S);
    }

    private void previousWorld() {
        worldId--;
        if (worldId < 0)
            worldId = vecs.length - 1;
        vec = vecs[worldId];
        controlWorld(KeyEvent.VK_S);
    }

    private void startWorld() {
        ereaseWorld();
        loadWorld();
        repaint();
        undoStack.removeAllElements();
        redoStack.removeAllElements();
        gameMode = PLAY_MODE;
    }

    private void toggleBeep() {
        beepOn = !beepOn;
//        beep.setState(beepOn);
    }

    private void help() {
        gameMode = HELP_MODE;
        changeMode();
    }

    private void continueGame() {
        gameMode = PLAY_MODE;
        update(getGraphics());
    }

    private void ereaseWorld() {
        for (int i = 0; i < X_SIZE; i++) {
            for (int j = 0; j < Y_SIZE; j++) {
                state[i][j] = 0;
            }
        }
    }

    // ******************* UNDO ********************

    private Stack<Integer> undoStack = new Stack<Integer>();

    private Stack<Integer> redoStack = new Stack<Integer>();

    private boolean isUndo = false;

    private boolean isRedo = false;

    private void undo() {
        isUndo = true;
        if (undoStack.empty()) {
            beep();
        } else {
            changeState(((Integer) undoStack.pop()).intValue());
        }
        isUndo = false;
    }

    private void redo() {
        isRedo = true;
        if (redoStack.empty()) {
            beep();
        } else {
            changeState(((Integer) redoStack.pop()).intValue());
        }
        isRedo = false;
    }

    private boolean checkEnd() {
        int v, x, y;
        boolean ret = true;
        for (int i = 0; i < goals.length; i++) {
            v = goals[i];
            x = 0xFFFF & v;
            y = 0xFFFF & (v >> 16);
            if (state[x][y] != FIT_ST) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    // ****************** GRAPHICS *********************************

    // private Image[] imgVec = new Image[8];

    // private int ISIZE = CELL_SIZE;
    // STATE PAINTING

    /**
     * Paints the NULL state
     */
    private void paintNull(Graphics g, int j, int i) {
        // Image img=imgVec[NULL_ST];
        // if(img==null){
        // img=createImage(ISIZE,ISIZE);
        // imgVec[NULL_ST]=img;
        // Graphics ig=img.getGraphics();
        g.setColor(Color.black);
        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        // }
        // g.drawImage(img,j * DIM, i * DIM,this);
    }

    private void paintWall(Graphics g, int j, int i) {
        // Image img=imgVec[WALL_ST];
        // if(img==null){
        // img=createImage(ISIZE,ISIZE);
        // imgVec[WALL_ST]=img;
        // Graphics ig=img.getGraphics();
        g.setColor(Color.gray);
        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.lightGray);
        g.drawRect(j * CELL_SIZE + 1, i * CELL_SIZE + 1, CELL_SIZE - 2,
                CELL_SIZE - 2);
        // }
        // g.drawImage(img,j * CELL_SIZE, i * CELL_SIZE,this);
    }

    private void paintBack(Graphics g, int j, int i) {
        // Image img=imgVec[BACK_ST];
        // if(img==null){
        // img=createImage(ISIZE,ISIZE);
        // imgVec[BACK_ST]=img;
        // Graphics ig=img.getGraphics();
        g.setColor(Color.blue);
        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        // }
        // g.drawImage(img,j * CELL_SIZE, i * CELL_SIZE,this);
    }

    private void paintGoal(Graphics g, int j, int i) {
        // Image img=imgVec[GOAL_ST];
        // if(img==null){
        // img=createImage(ISIZE,ISIZE);
        // imgVec[GOAL_ST]=img;
        // Graphics ig=img.getGraphics();
        g.setColor(Color.green);
        g.fillOval(j * CELL_SIZE + CELL_SIZE / 4,
                i * CELL_SIZE + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
        // }
        // g.drawImage(img,j * CELL_SIZE, i * CELL_SIZE,this);
    }

    private void paintBox(Graphics g, int j, int i, Color c) {
        g.setColor(c);
        g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        g.setColor(Color.lightGray);
        g.drawRect(j * CELL_SIZE + 1, i * CELL_SIZE + 1, CELL_SIZE - 2,
                CELL_SIZE - 2);
        g.drawLine(j * CELL_SIZE + 1, i * CELL_SIZE + 1, (j + 1) * CELL_SIZE
                - 1, (i + 1) * CELL_SIZE - 1);
        g.drawLine(j * CELL_SIZE + 1, (i + 1) * CELL_SIZE - 1, (j + 1)
                * CELL_SIZE - 1, i * CELL_SIZE + 1);
    }

    private void paintBox(Graphics g, int j, int i) {
        // Image img=imgVec[BOX_ST];
        // if(img==null){
        // img=createImage(ISIZE,ISIZE);
        // imgVec[BOX_ST]=img;
        // Graphics ig=img.getGraphics();
        paintBox(g, j, i, Color.yellow);
        // }
        // g.drawImage(img,j * CELL_SIZE, i * CELL_SIZE,this);

    }

    private void paintFit(Graphics g, int j, int i) {
        paintBox(g, j, i, Color.red);
    }

    private void paintMan(Graphics g, int j, int i) {
        g.setColor(Color.cyan);

        // head
        g.fillOval(j * CELL_SIZE + CELL_SIZE / 2 - CELL_SIZE / 8, i * CELL_SIZE
                + CELL_SIZE / 16, CELL_SIZE / 4, CELL_SIZE / 4);

        // g.drawLine(j * CELL_SIZE + CELL_SIZE / 2, i * CELL_SIZE + 2, j *
        // CELL_SIZE + CELL_SIZE / 2, i * CELL_SIZE + 13);

        // body
        g.fillRect(j * CELL_SIZE + CELL_SIZE / 2 - CELL_SIZE / 16, i
                * CELL_SIZE + CELL_SIZE / 4, CELL_SIZE / 8, CELL_SIZE / 3
                + CELL_SIZE / 12);

        // g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 + 1, i * CELL_SIZE + 8, j *
        // CELL_SIZE + CELL_SIZE / 2 + 1, i * CELL_SIZE + 13);

        // arms
        g.fillRect(j * CELL_SIZE + CELL_SIZE / 8, i * CELL_SIZE + CELL_SIZE * 5
                / 16, CELL_SIZE * 3 / 4, CELL_SIZE / 16);

        // legs
        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2, i * CELL_SIZE + CELL_SIZE * 7
                / 12, j * CELL_SIZE + 4, (i + 1) * CELL_SIZE - 2);
        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2, i * CELL_SIZE + CELL_SIZE * 7
                / 12, (j + 1) * CELL_SIZE - 4, (i + 1) * CELL_SIZE - 2);

        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 + 1, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, j * CELL_SIZE + 4 + 1, (i + 1) * CELL_SIZE - 2);
        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 - 1, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, (j + 1) * CELL_SIZE - 4 - 1, (i + 1) * CELL_SIZE - 2);

        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 + 2, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, j * CELL_SIZE + 4 + 2, (i + 1) * CELL_SIZE - 2);
        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 - 2, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, (j + 1) * CELL_SIZE - 4 - 2, (i + 1) * CELL_SIZE - 2);

        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 + 3, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, j * CELL_SIZE + 4 + 3, (i + 1) * CELL_SIZE - 2);
        g.drawLine(j * CELL_SIZE + CELL_SIZE / 2 - 3, i * CELL_SIZE + CELL_SIZE
                * 7 / 12, (j + 1) * CELL_SIZE - 4 - 3, (i + 1) * CELL_SIZE - 2);
    }

    private void paintManGoal(Graphics g, int j, int i) {
        paintGoal(g, j, i);
        paintMan(g, j, i);
    }

    private void paintStart(Graphics g) {
        g.setColor(Color.black);
        int dx = 200;
        int dy = 100;
        int x = (BW_WIDTH - dx) / 2;
        int y = (BW_HEIGHT - dy) / 2;
        g.fillRect(x, y, dx, dy);
        g.setColor(Color.white);
        g.drawRect(x + 2, y + 2, dx - 4, dy - 4);
        g.drawRect(x + 3, y + 3, dx - 6, dy - 6);
        g.drawRect(x + 4, y + 4, dx - 8, dy - 8);
        g.drawString("CLICK HERE!", x + 70, y + 50);
    }

    private void paintEnd(Graphics g) {
        g.setColor(Color.black);
        int dx = 200;
        int dy = 100;
        int x = (BW_WIDTH - dx) / 2;
        int y = (BW_HEIGHT - dy) / 2;
        g.fillRect(x, y, dx, dy);
        g.setColor(Color.white);
        g.drawRect(x + 2, y + 2, dx - 4, dy - 4);
        g.drawRect(x + 3, y + 3, dx - 6, dy - 6);
        g.drawRect(x + 4, y + 4, dx - 8, dy - 8);
        g.drawString("GOOD JOB!", x + 70, y + 50);
    }

    private static String[] help = { "s - start game", "n - next world",
            "p - previous world", "u - undo", "r - redo", "b - toggle beep",
            "h - help", " ", "Press C to continue", " ", "Create by",
            "Levente S\u00e1ntha, 2002",

    };

    private void paintHelp(Graphics g) {
        g.setColor(Color.black);
        int dx = 200;
        int dy = 320;
        int x = (BW_WIDTH - dx) / 2;
        int y = (BW_HEIGHT - dy) / 2;
        g.fillRect(x, y, dx, dy);
        g.setColor(Color.white);
        g.drawRect(x + 2, y + 2, dx - 4, dy - 4);
        g.drawRect(x + 3, y + 3, dx - 6, dy - 6);
        g.drawRect(x + 4, y + 4, dx - 8, dy - 8);
        g.drawString("H E L P", x + 80, y + 40);
        for (int i = 0; i < help.length; i++) {
            g.drawString(help[i], x + 40, y + 70 + 20 * i);
        }
    }

    private void paintShape(Graphics g, int j, int i) {
        int c = state[i][j];
        if (c > NULL_ST)
            switch (c) {
            case NULL_ST:
                paintNull(g, j, i);
                break;
            case WALL_ST:
                paintWall(g, j, i);
                break;
            case BACK_ST:
                paintBack(g, j, i);
                break;
            case GOAL_ST:
                paintBack(g, j, i);
                paintGoal(g, j, i);
                break;
            case BOX_ST:
                paintBox(g, j, i);
                break;
            case FIT_ST:
                paintFit(g, j, i);
                break;
            case MAN_ST:
                paintBack(g, j, i);
                paintMan(g, j, i);
                break;
            case MAN_GOAL_ST:
                paintBack(g, j, i);
                paintManGoal(g, j, i);
                break;
            }
    }

    private Image iBuff = null;

    public void paint(Graphics gr) {
        if (iBuff == null) {
            iBuff = createImage(BW_WIDTH, BW_HEIGHT);
        }

        Graphics g = iBuff.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, BW_WIDTH, BW_HEIGHT);
        switch (gameMode) {
        case START_MODE: {
            paintStart(g);
            break;
        }
        case PLAY_MODE: {
            for (int j = 0; j < X_SIZE; j++) {
                for (int i = 0; i < Y_SIZE; i++) {
                    paintShape(g, j, i);
                }
            }
            break;
        }
        case HELP_MODE: {
            paintHelp(g);
            break;
        }
        case END_MODE: {
            paintEnd(g);
            break;
        }
        }
        g.dispose();
        gr.drawImage(iBuff, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    private int invert(int key) {
        int inv = -1;
        switch (key) {
        case LEFT_MOVE:
            inv = RIGHT_MOVE;
            break;
        case RIGHT_MOVE:
            inv = LEFT_MOVE;
            break;
        case UP_MOVE:
            inv = DOWN_MOVE;
            break;
        case DOWN_MOVE:
            inv = UP_MOVE;
            break;
        case LEFT_PUSH:
            inv = RIGHT_DRAW;
            break;
        case RIGHT_PUSH:
            inv = LEFT_DRAW;
            break;
        case UP_PUSH:
            inv = DOWN_DRAW;
            break;
        case DOWN_PUSH:
            inv = UP_DRAW;
            break;
        case LEFT_DRAW:
            inv = RIGHT_MOVE;
            break;
        case RIGHT_DRAW:
            inv = LEFT_MOVE;
            break;
        case UP_DRAW:
            inv = DOWN_MOVE;
            break;
        case DOWN_DRAW:
            inv = UP_MOVE;
            break;
        }
        return inv;
    }

    private void undoController(int key) {
        if (isUndo) {
            redoStack.push(new Integer(invert(key)));
        } else {
            if (redoStack.size() > 0 && !isRedo)
                redoStack.removeAllElements();
            undoStack.push(new Integer(invert(key)));
        }
    }

    private void changeState(int key) {
        Graphics g = getGraphics();
        int xt = x, yt = y, ct;
        xo = x;
        yo = y;
        boolean isDraw = false;
        switch (key) {
        case LEFT_MOVE:
            y--;
            yt = y - 1;
            break;
        case RIGHT_MOVE:
            y++;
            yt = y + 1;
            break;
        case UP_MOVE:
            x--;
            xt = x - 1;
            break;
        case DOWN_MOVE:
            x++;
            xt = x + 1;
            break;
        case LEFT_DRAW:
            yt = y + 1;
            y--;
            isDraw = true;
            break;
        case RIGHT_DRAW:
            yt = y - 1;
            y++;
            isDraw = true;
            break;
        case UP_DRAW:
            xt = x + 1;
            x--;
            isDraw = true;
            break;
        case DOWN_DRAW:
            xt = x - 1;
            x++;
            isDraw = true;
            break;
        }
        int c = state[x][y];
        if (isDraw) {
            undoController(key);
            cellExited(xo, yo);
            cellExited(xt, yt);
            cellEntered(x, y, MAN_ST);
            cellEntered(xo, yo, BOX_ST);
            paintShape(g, yt, xt);
            paintShape(g, y, x);
            paintShape(g, yo, xo);
        } else if (c == BACK_ST || c == GOAL_ST) {
            undoController(key);
            cellExited(xo, yo);
            cellEntered(x, y, MAN_ST);
            paintShape(g, y, x);
            paintShape(g, yo, xo);
        } else if (c == BOX_ST || c == FIT_ST) {
            ct = state[xt][yt];
            if (ct == BACK_ST || ct == GOAL_ST) {
                undoController(PUSH_BASE + key);
                cellExited(xo, yo);
                cellExited(x, y);
                cellEntered(x, y, MAN_ST);
                cellEntered(xt, yt, BOX_ST);
                paintShape(g, yt, xt);
                paintShape(g, y, x);
                paintShape(g, yo, xo);
            } else {
                block();
            }
        } else {
            block();
        }
        if (checkEnd()) {
            gameMode = END_MODE;
            paintEnd(g);
        }
    }

    private void cellEntered(int x, int y, int mst) {
        int st = state[x][y];
        if (st == BACK_ST) {
            state[x][y] = mst;
        } else if (st == GOAL_ST) {
            state[x][y] = mst == MAN_ST ? MAN_GOAL_ST : FIT_ST;
        }
    }

    private void cellExited(int x, int y) {
        int st = state[x][y];
        if (st == MAN_ST || st == BOX_ST) {
            state[x][y] = BACK_ST;
        } else if (st == FIT_ST || st == MAN_GOAL_ST) {
            state[x][y] = GOAL_ST;
        }
    }

    private void block() {
        x = xo;
        y = yo;
        repeat = false;
        beep();
    }

    private boolean beepOn = true;

    private void beep() {
        // if (beepOn) getToolkit().beep();
    }

    public Dimension getPreferredSize() {
        return new Dimension(BW_WIDTH, BW_HEIGHT);
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    // *********** MAIN ************************
    public static void main(String[] argv) {
        BoxWorld bw = new BoxWorld();
        bw.init();
        Frame frame = new JFrame("Boxworld");
        frame.setResizable(false);
        frame.add(bw);
        frame.addWindowListener(bw);
        frame.pack();
        frame.setSize(400, 400);
        frame.setLocation(40, 40);
        frame.setVisible(true);
        bw.requestFocus();
        // frame.setVisible(true);
    }

    // ************ EMPTY METHODS *********************
    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}
