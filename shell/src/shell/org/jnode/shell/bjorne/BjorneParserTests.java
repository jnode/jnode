package org.jnode.shell.bjorne;

import junit.framework.TestCase;

import org.jnode.shell.ShellException;

public class BjorneParserTests extends TestCase {

    private static final boolean DEBUG = true;

    public void testParser() {
        new BjorneParser(new BjorneTokenizer(""));
    }

    public void test1() throws ShellException {
        assertEquals(
                "SimpleCommand{nodeType=1,command=WORD{foo},arguments=[]}",
                doTest("foo"));
    }

    public void test2() throws ShellException {
        assertEquals(
                "SimpleCommand{nodeType=1,command=WORD{ls},arguments=[WORD{-l}]}",
                doTest("ls -l"));
    }

    public void test3() throws ShellException {
        assertEquals(
                "SimpleCommand{nodeType=1,"
                        + "redirects=[Redirect{redirectionType=60,io=IO_NUMBER{1},arg=WORD{/tmp/foo}},"
                        + "Redirect{redirectionType=62,arg=WORD{/tmp/bar}}],"
                        + "command=WORD{ls},arguments=[WORD{-l}]}",
                doTest("ls -l 1< /tmp/foo > /tmp/bar"));
    }

    public void test4() throws ShellException {
        assertEquals(
                "ListCommand{nodeType=2,flags=0x10,"
                        + "commands=[SimpleCommand{nodeType=1,assignments=[ASSIGNMENT{FOO=BAR}],"
                        + "command=WORD{ls},arguments=[WORD{-l}]},"
                        + "SimpleCommand{nodeType=1,command=WORD{less},arguments=[]}]}",
                doTest("FOO=BAR ls -l | less"));
    }

    public void test5() throws ShellException {
        assertEquals(
                "ListCommand{nodeType=2,flags=0x10,commands=["
                        + "SimpleCommand{nodeType=1,command=WORD{cat},arguments=[WORD{foo}]},"
                        + "ListCommand{nodeType=10,commands=["
                        + "ListCommand{nodeType=2,commands=["
                        + "SimpleCommand{nodeType=1,command=WORD{wc},arguments=[WORD{1}]},"
                        + "SimpleCommand{nodeType=1,flags=0x2,command=WORD{wc},arguments=[WORD{2}]}]},"
                        + "ListCommand{nodeType=2,commands=["
                        + "SimpleCommand{nodeType=1,command=WORD{wc},arguments=[WORD{3}]},"
                        + "SimpleCommand{nodeType=1,flags=0x4,command=WORD{wc},arguments=[WORD{4}]}]}]}]}",
                doTest("cat foo | ( wc 1 && wc 2 ; wc 3 || wc 4 )"));
    }

    public void test6() throws ShellException {
        assertEquals(
                "ListCommand{nodeType=2,commands=["
                        + "SimpleCommand{nodeType=1,flags=0x1,command=WORD{cat},arguments=[WORD{foo}]},"
                        + "SimpleCommand{nodeType=1,command=WORD{cat},arguments=[WORD{bar}]},"
                        + "SimpleCommand{nodeType=1,command=WORD{cat},arguments=[WORD{baz}]}]}",
                doTest("cat foo & cat bar ; cat baz ;"));
    }

    public void test7() throws ShellException {
        assertEquals(
                "LoopCommand{nodeType=3,var=NAME{i},"
                        + "words=[WORD{1},WORD{2},WORD{3},WORD{4},WORD{5}],"
                        + "body=SimpleCommand{nodeType=1,command=WORD{echo},arguments=[WORD{$i}]}}",
                doTest("for i in 1 2 3 4 5 ; do echo $i ; done"));
    }

    public void test7a() throws ShellException {
        assertEquals(
                "LoopCommand{nodeType=3,var=NAME{i},"
                        + "words=[WORD{1},WORD{2},WORD{3},WORD{4},WORD{5}],"
                        + "body=SimpleCommand{nodeType=1,command=WORD{echo},arguments=[WORD{$i}]}}",
                doTest("for i in 1 2 3 4 5 ; do \n echo $i ; done"));
    }

    public void test8() throws ShellException {
        assertEquals(
                "LoopCommand{nodeType=4,"
                        + "cond=SimpleCommand{nodeType=1,command=WORD{true},arguments=[]},"
                        + "body=SimpleCommand{nodeType=1,command=WORD{echo},arguments=[WORD{$i}]}}",
                doTest("while true ; do echo $i ; done"));
    }

    public void test9() throws ShellException {
        assertEquals(
                "LoopCommand{nodeType=5,"
                        + "cond=SimpleCommand{nodeType=1,command=WORD{true},arguments=[]},"
                        + "body=SimpleCommand{nodeType=1,command=WORD{echo},arguments=[WORD{$i}]}}",
                doTest("until true ; do echo $i ; done"));
    }

    public void test10() throws ShellException {
        assertEquals(
                "CaseCommand{nodeType=9,word=WORD{$1},caseItems=["
                        + "CaseItem{,pattern=[],body="
                        + "SimpleCommand{nodeType=1,command=WORD{ls},arguments=[WORD{-l}]}},"
                        + "CaseItem{,pattern=[],body="
                        + "SimpleCommand{nodeType=1,command=WORD{ls},arguments=[WORD{-a}]}}]}",
                doTest("case $1 in ( a ) ls -l ;; b ) ls -a ; esac"));
    }

    private String doTest(String input) throws ShellException {
        BjorneParser p = new BjorneParser(new BjorneTokenizer(input, DEBUG));
        String res = p.parse().toString();
        if (DEBUG) {
            System.err.println(res);
        }
        return res;
    }
}
