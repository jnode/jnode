/*
 * $Id$
 *
 * Copyright (C) 2003-2016 JNode.org
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

package org.jnode.jnasm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.tools.ant.Project;
import org.jnode.ant.taskdefs.Asm;
import org.jnode.jnasm.assembler.Assembler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Simple test for JNasm. Requires the availability of nasm.
 */
public class JNAsmTest {
    static final String TEST_ASM_FILE_NAME = "jnode32.asm";
    private static final String WORKING_DIRECTORY_NAME = "jnasmtest";
    private static final String JNASM_OUTPUT_FILE_NAME = "jnode32.out";
    private static final String NASM_OUTPUT_FILE_NAME = "jnode32.o";

    @Test
    public void jnasm32Test() throws Exception {
        File workingDirectory = createWorkingDirectory();

        //execute jnasm
        File jnasmOutputFile = new File(workingDirectory, JNASM_OUTPUT_FILE_NAME);
        FileOutputStream jnasmOutputStream = new FileOutputStream(jnasmOutputFile);
        InputStreamReader jnasmInputStream = new InputStreamReader(getClass().getResourceAsStream(TEST_ASM_FILE_NAME));
        Assembler assembler = Assembler.newInstance(jnasmInputStream);
        assembler.performTwoPasses(jnasmInputStream, jnasmOutputStream);
        jnasmOutputStream.flush();
        jnasmOutputStream.close();
        jnasmInputStream.close();
        //System.out.println(jnasmOutputFile.getAbsolutePath());

        //prepare nasm input file: copy resource to target file
        File testAsmFile = new File(workingDirectory, TEST_ASM_FILE_NAME);
        InputStream sourceInputStream = getClass().getResourceAsStream(TEST_ASM_FILE_NAME);
        FileOutputStream targetOutputStream = new FileOutputStream(testAsmFile);
        byte[] buff = new byte[1024];
        int c;
        while ((c = sourceInputStream.read(buff)) > -1) {
            targetOutputStream.write(buff, 0, c);
        }
        targetOutputStream.flush();
        targetOutputStream.close();
        sourceInputStream.close();

        //execute nasm
        Asm asm = new Asm();
        asm.setProject(new Project());
        asm.setEnableJNasm(false);
        asm.setJnasmCompatibilityEnabled(true);
        asm.setSrcdir(workingDirectory);
        asm.setDestdir(workingDirectory);
        asm.setOutputFormat("bin");
        asm.execute();

        //compare JNAsm binary with NASM binary
        File nasmOutputFile = new File(workingDirectory, NASM_OUTPUT_FILE_NAME);

        Assert.assertEquals(nasmOutputFile.length(), jnasmOutputFile.length());
        InputStream jnasmBinaryInput = new FileInputStream(jnasmOutputFile);
        InputStream nasmBinaryInput = new FileInputStream(nasmOutputFile);
        int b1, b2;
        while (((b1 = jnasmBinaryInput.read()) > -1) | ((b2 = nasmBinaryInput.read()) > -1)) {
            Assert.assertEquals(b2, b1);
        }
        jnasmBinaryInput.close();
        nasmBinaryInput.close();
        Assert.assertEquals(-1, b1);
        Assert.assertEquals(-1, b2);

        cleanupWorkingDirectory(workingDirectory);
    }

    static File createWorkingDirectory() throws IOException {
        //create folder for temporary files
        File dummy = File.createTempFile("jnasmdummy", ".txt");
        dummy.deleteOnExit();
        File workingDirectory = new File(dummy.getParentFile(), WORKING_DIRECTORY_NAME);
        if (!workingDirectory.exists()) {
            workingDirectory.mkdir();
        }
        dummy.delete();
        return workingDirectory;
    }

    static void copareFiles(File file1, File file2) throws IOException {
        InputStream in1 = new FileInputStream(file1);
        InputStream in2 = new FileInputStream(file2);
        int b1, b2;
        while (((b1 = in1.read()) > -1) | ((b2 = in2.read()) > -1)) {
            Assert.assertEquals(b2, b1);
        }
        in1.close();
        in2.close();
    }

    static void cleanupWorkingDirectory(File workingDirectory) {
        //clean up
        File[] files = workingDirectory.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                f.delete();
            }
        }
        workingDirectory.delete();
    }
}
