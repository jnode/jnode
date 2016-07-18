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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86TextAssembler;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.jnasm.util.X86DualAssemblerFactory;
import org.jnode.vm.x86.X86CpuID;
import org.junit.Test;

/**
 * User: lsantha
 * Date: 6/14/15 4:33 PM
 */
public class DualAssemblerTest {

    private static final String GENERATED_ASM_FILE_NAME = "jnode.asm";
    private static final String OUTPUT_FILE1_NAME = "jnode1.out";
    private static final String OUTPUT_FILE2_NAME = "jnode2.out";

    @Test
    public void dualAssemblerTest() throws Exception {
        File workingDirectory = JNAsmTest.createWorkingDirectory();

        //execute jnasm
        InputStreamReader jnasmInputStream = new InputStreamReader(getClass().getResourceAsStream(JNAsmTest.TEST_ASM_FILE_NAME));
        Assembler assembler = Assembler.newInstance(jnasmInputStream);
        X86CpuID cpuId = X86CpuID.createID("pentium");
        X86Constants.Mode mode = X86Constants.Mode.CODE32;
        NativeStream nativeStream = new X86BinaryAssembler(cpuId, mode, 0);
        ((X86BinaryAssembler) nativeStream).setByteValueEnabled(true);
        ((X86BinaryAssembler) nativeStream).setRelJumpEnabled(false);
        File textFile = new File(workingDirectory, GENERATED_ASM_FILE_NAME);
        BufferedWriter textWriter = new BufferedWriter(new FileWriter(textFile));
        X86TextAssembler x86TextAssembler = new X86TextAssembler(textWriter, cpuId, mode);
        nativeStream = X86DualAssemblerFactory.create(x86TextAssembler, (X86BinaryAssembler) nativeStream);

        assembler.performTwoPasses(jnasmInputStream, nativeStream);

        File jnasmOutputFile1 = new File(workingDirectory, OUTPUT_FILE1_NAME);
        FileOutputStream jnasmOutputStream = new FileOutputStream(jnasmOutputFile1);
        nativeStream.writeTo(jnasmOutputStream);
        jnasmOutputStream.flush();
        jnasmOutputStream.close();
        jnasmInputStream.close();
        x86TextAssembler.flush();
        textWriter.flush();
        textWriter.close();


        //System.out.println(jnasmOutputFile1.getAbsolutePath());

        jnasmInputStream = new InputStreamReader(new FileInputStream(textFile));
        assembler = Assembler.newInstance(jnasmInputStream);
        nativeStream = new X86BinaryAssembler(cpuId, mode, 0);
        ((X86BinaryAssembler) nativeStream).setByteValueEnabled(true);
        ((X86BinaryAssembler) nativeStream).setRelJumpEnabled(false);

        assembler.performTwoPasses(jnasmInputStream, nativeStream);

        File jnasmOutputFile2 = new File(workingDirectory, OUTPUT_FILE2_NAME);
        jnasmOutputStream = new FileOutputStream(jnasmOutputFile2);
        nativeStream.writeTo(jnasmOutputStream);
        jnasmOutputStream.flush();
        jnasmOutputStream.close();
        jnasmInputStream.close();

        JNAsmTest.copareFiles(jnasmOutputFile1, jnasmOutputFile2);
        JNAsmTest.cleanupWorkingDirectory(workingDirectory);
    }

}
