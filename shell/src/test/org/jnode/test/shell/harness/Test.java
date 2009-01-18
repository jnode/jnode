package org.jnode.test.shell.harness;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Hi mum");
        } else if (args[0].equals("System.exit")) {
            System.exit(1);
        }
    }

}
