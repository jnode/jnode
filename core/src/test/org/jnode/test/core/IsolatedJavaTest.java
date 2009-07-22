package org.jnode.test.core;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.isolate.Link;
import javax.isolate.LinkMessage;
import javax.isolate.IsolateStatus;
import java.lang.reflect.Field;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import org.jnode.vm.isolate.VmIsolate;

/**
 * Prototype for new isolates based "java" command.
 */
public class IsolatedJavaTest {
    private List<URL> classPath = new ArrayList<URL>();
    private Properties properties;
    private String mainClass;
    private String[] classArgs;

    public void run(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-cp".equals(arg) || "-classpath".equals(arg)) {
                if (args.length > i + 1) {
                    parseClasspath(args[i + 1]);
                    i++;
                } else {
                    //error
                }
            } else if (arg.startsWith("-D")) {
                parseSystemProperty(arg);
            } else if (arg.startsWith("-jar")) {
                if (args.length > i + 1) {
                    parseJarAndArgs(args, i);
                    break;
                } else {
                    //error
                }
            } else if (
                "-d32".equals(arg) ||
                    "-d64".equals(arg) ||
                    "-client".equals(arg) ||
                    "-server".equals(arg) ||
                    "-hotspot".equals(arg) ||
                    arg.startsWith("-verbose") ||
                    arg.startsWith("-version") ||
                    "-showversion".equals(arg) ||
                    "-jre-restrict-search".equals(arg) ||
                    "-jre-no-restrict-search".equals(arg) ||
                    "-help".equals(arg) ||
                    "-?".equals(arg) ||
                    "-X".equals(arg) ||
                    arg.startsWith("-X") ||
                    arg.startsWith("-ea") ||
                    arg.startsWith("-enableassertions") ||
                    arg.startsWith("-da") ||
                    arg.startsWith("-disableassertions") ||
                    "-esa".equals(arg) ||
                    "-enablesystemassertions".equals(arg) ||
                    "-dsa".equals(arg) ||
                    "-disablesystemassertions".equals(arg) ||
                    arg.startsWith("-agentlib") ||
                    arg.startsWith("-agentpath") ||
                    arg.startsWith("-javaagent") ||
                    arg.startsWith("-splash") ||
                    false
                ) {
                //ignore
            } else if (arg.startsWith("-")) {
                //error invalid option
            } else {
                parseClassNameAndArgs(args, i);
                break;
            }
        }

        if (mainClass == null) {
            //print usage
            return;
        }

        if (classArgs == null)
            classArgs = new String[0];
        Isolate newIsolate;
        if (properties != null && properties.size() > 0) {
            newIsolate = new Isolate(properties, mainClass, classArgs);
        } else {
            newIsolate = new Isolate(mainClass, classArgs);
        }

        try {
            classPath.add(0, new File(".").toURI().toURL());
            Field field = newIsolate.getClass().getDeclaredField("impl");
            field.setAccessible(true);
            VmIsolate vmi = (VmIsolate) field.get(newIsolate);
            vmi.setClasspath(classPath.toArray(new URL[classPath.size()]));
        } catch (Exception x) {
            x.printStackTrace();
            return;
        }

        try {
            Link link = newIsolate.newStatusLink();
            newIsolate.start();
            //wait for exit
            for (; ;) {
                LinkMessage msg = link.receive();
                if (msg.containsStatus() && IsolateStatus.State.EXITED.equals(msg.extractStatus().getState()))
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseJarAndArgs(String[] args, int i) {
        try {
            if (args.length > i + 1) {
                File jar = new File(args[i + 1]).getCanonicalFile();
                JarFile jf = new JarFile(jar);
                Manifest mani = jf.getManifest();
                mainClass = mani.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                parseClassArgs(args, i + 1);
                jf.close();
                classPath.add(new URL("jar:" + jar.toURI().toURL() + "!/"));
            } else {
                //error
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void parseClassNameAndArgs(String[] args, int i) {
        mainClass = args[i];
        parseClassArgs(args, i);
    }

    private void parseClassArgs(String[] args, int i) {
        if (args.length > i + 1) {
            classArgs = new String[args.length - i - 1];
            System.arraycopy(args, i + 1, classArgs, 0, classArgs.length);
        }
    }

    private void parseSystemProperty(String arg) {
        String s = arg.substring(2);
        int ep = s.indexOf('=');
        if (ep > -1) {
            String name = s.substring(0, ep).trim();
            if (name.length() > 0) {
                String value = s.substring(ep + 1).trim();
                if (value.length() > 0) {
                    if (properties == null) {
                        properties = new Properties();
                    }
                    properties.setProperty(name, value);
                } else {
                    //error invalid property value
                }
            } else {
                //error invalid property name
            }
        } else {
            //error invalid property setting
        }
    }

    private void parseClasspath(String arg) {
        String[] pes = arg.split("\\:");
        for (int i = 0; i < pes.length; i++) {
            String pe = pes[i];
            if (pe == null) continue;
            pe = pe.trim();
            if (pe.length() == 0) continue;

            //add url support

            File file = new File(pe);
            if (!file.exists()) {
                System.out.println("dropping invalid classpath entry: " + pe);
                //ignore it
                continue;
            }

            if (file.isDirectory()) {
                try {
                    classPath.add(file.getCanonicalFile().toURI().toURL());
                } catch (IOException ioe) {
                    System.out.println("dropping invalid classpath entry: " + pe);
                    ioe.printStackTrace();
                }
            } else if (file.isFile()) {
                if (pe.endsWith(".jar") || pe.endsWith(".zip")) {
                    try {
                        classPath.add(new URL("jar:" + file.toURI().toURL() + "!/"));
                    } catch (MalformedURLException x) {
                        System.out.println("dropping invalid classpath entry: " + pe);
                        x.printStackTrace();
                    }
                } else {
                    System.out.println("dropping invalid classpath entry: " + pe);
                    //ignore it
                }
            } else {
                System.out.println("dropping invalid classpath entry: " + pe);
                //ignore it
            }
        }
    }

    public static void main(String[] args) {
        new IsolatedJavaTest().run(args);
    }
}
