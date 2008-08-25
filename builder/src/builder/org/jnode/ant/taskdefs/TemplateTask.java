/*
 * $
 */
package org.jnode.ant.taskdefs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Levente S\u00e1ntha
 */
public class TemplateTask extends Task {
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private String file;
    private String toFile;

    @Override
    public void execute() throws BuildException {
        try {
            //check file
            if (file == null || file.trim().length() == 0)
                return;

            File ff = new File(file);
            if (!ff.exists())
                return;

            //check toFile
            if (toFile == null || toFile.trim().length() == 0)
                return;

            //check tokens
            boolean found = true;
            for (Token t : tokens) {
                if (t.name == null || t.name.trim().length() == 0) {
                    t.name = null;
                    found = false;
                } else if (t.value == null ||
                    t.value.trim().length() == 0 ||
                    t.value.startsWith("${") && t.value.endsWith("}")) {
                    t.value = null;
                    found = false;
                }
            }

            //read input file
            FileReader fr = new FileReader(ff);
            StringWriter sw = new StringWriter();
            char[] buff = new char[512];
            int cc;
            while ((cc = fr.read(buff)) > 0) {
                sw.write(buff, 0, cc);
            }
            fr.close();
            sw.close();

            if (found) {
                //default token
                Token enabled = new Token();
                enabled.setName("ENABLED = false");
                enabled.setValue("ENABLED = true");
                tokens.add(enabled);
            }

            //replace tokens
            String s1 = sw.toString();
            if (found) {
                for (Token t : tokens) {
                    s1 = s1.replace(t.name, t.value);
                }
            }

            String s2 = "";

            //read output file
            ff = new File(toFile);
            if (ff.exists()) {
                fr = new FileReader(ff);
                sw = new StringWriter();
                buff = new char[512];
                while ((cc = fr.read(buff)) > 0) {
                    sw.write(buff, 0, cc);
                }
                fr.close();
                sw.close();
                s2 = sw.toString();
            }

            //if changed replace old file contents with new file contents
            if (!s1.equals(s2)) {
                ff.getAbsoluteFile().getParentFile().mkdirs();
                FileWriter fw = new FileWriter(ff);
                fw.write(s1);
                fw.close();
            }
        } catch (Exception x) {
            throw new BuildException(x);
        }
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getToFile() {
        return toFile;
    }

    public void setToFile(String toFile) {
        this.toFile = toFile;
    }

    public Token createToken() {
        Token token = new Token();
        tokens.add(token);
        return token;
    }

    public static class Token {
        String name;
        String value;
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Token(" + name + "," + value + ")";
        }
    }
}
