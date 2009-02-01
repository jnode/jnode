/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.compiler;

import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.memmgr.VmWriteBarrier;

/**
 * @author epr
 */
public class EntryPoints extends VmSystemObject {

    private final VmType vmMethodCodeClass;

    private final VmType vmInstanceMethodClass;

    private final VmType vmInstanceFieldClass;

    private final VmType vmStaticFieldClass;

    private final VmInstanceField vmMemberDeclaringClassField;

    private final VmInstanceField vmFieldOffsetField;

    private final VmInstanceField vmFieldStaticsIndexField;

    private final VmInstanceField vmMethodTibOffsetField;

    private final VmInstanceField vmMethodSelectorField;

    private final VmInstanceField vmMethodNativeCodeField;

    private final VmInstanceField vmConstIMethodRefSelectorField;

    private final VmInstanceField vmProcessorMeField;

    private final int vmThreadSwitchIndicatorOffset;

    private final VmType vmSoftByteCodesClass;

    private final VmType vmMonitorManagerClass;

    private final VmMethod anewarrayMethod;

    private final VmMethod allocArrayMethod;

    private final VmMethod allocPrimitiveArrayMethod;

    private final VmMethod allocMultiArrayMethod;

    private final VmMethod resolveFieldMethod;

    private final VmMethod resolveMethodMethod;

    private final VmMethod resolveClassMethod;

    private final VmMethod allocObjectMethod;

    private final VmMethod monitorEnterMethod;

    private final VmMethod monitorExitMethod;

    private final VmMethod ldivMethod;

    private final VmMethod lremMethod;

    private final VmMethod systemExceptionMethod;

    private final VmMethod classCastFailedMethod;

    private final VmMethod getClassForVmTypeMethod;

    private final VmMethod vmTypeInitialize;

    private final VmMethod throwArrayOutOfBounds;

    private final VmInstanceField vmTypeModifiers;

    private final VmInstanceField vmTypeState;

    private final VmInstanceField vmTypeCp;

    private final VmInstanceField vmCPCp;

    private final VmInstanceField vmProcessorStackEnd;

    private final VmInstanceField vmProcessorSharedStaticsTable;

    private final VmInstanceField vmProcessorIsolatedStaticsTable;

    private final VmMethod arrayStoreWriteBarrier;

    private final VmMethod putfieldWriteBarrier;

    private final VmMethod putstaticWriteBarrier;

    private final VmWriteBarrier writeBarrier;

    private final VmMethod yieldPoint;

    private final VmMethod recompileMethod;

    private final int magic;

    /**
     * Create a new instance
     *
     * @param loader
     */
    public EntryPoints(VmClassLoader loader, VmHeapManager heapManager,
                       int magic) {
        try {
            this.magic = magic;
            // VmMember class
            final VmType vmMemberType = loader.loadClass(
                "org.jnode.vm.classmgr.VmMember", true);
            this.vmMemberDeclaringClassField = (VmInstanceField) testField(vmMemberType
                .getField("declaringClass"));

            // SoftByteCode
            this.vmSoftByteCodesClass = loader.loadClass(
                "org.jnode.vm.SoftByteCodes", true);
            anewarrayMethod = testMethod(vmSoftByteCodesClass.getMethod(
                "anewarray",
                "(Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
            allocArrayMethod = testMethod(vmSoftByteCodesClass.getMethod(
                "allocArray",
                "(Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
            allocMultiArrayMethod = testMethod(vmSoftByteCodesClass.getMethod(
                "allocMultiArray",
                "(Lorg/jnode/vm/classmgr/VmType;[I)Ljava/lang/Object;"));
            allocPrimitiveArrayMethod = testMethod(vmSoftByteCodesClass
                .getMethod("allocPrimitiveArray", "(Lorg/jnode/vm/classmgr/VmType;II)Ljava/lang/Object;"));
            resolveFieldMethod = testMethod(vmSoftByteCodesClass.getMethod("resolveField",
                "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmConstFieldRef;Z)" +
                    "Lorg/jnode/vm/classmgr/VmField;"));
            resolveMethodMethod = testMethod(vmSoftByteCodesClass.getMethod("resolveMethod",
                "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmConstMethodRef;)" +
                    "Lorg/jnode/vm/classmgr/VmMethod;"));
            resolveClassMethod = testMethod(vmSoftByteCodesClass.getMethod("resolveClass",
                "(Lorg/jnode/vm/classmgr/VmConstClass;)Lorg/jnode/vm/classmgr/VmType;"));
            allocObjectMethod = testMethod(vmSoftByteCodesClass.getMethod("allocObject",
                "(Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
            classCastFailedMethod = testMethod(vmSoftByteCodesClass.getMethod(
                "classCastFailed", "(Ljava/lang/Object;Lorg/jnode/vm/classmgr/VmType;)V"));
            throwArrayOutOfBounds = testMethod(vmSoftByteCodesClass.getMethod(
                "throwArrayOutOfBounds", "(Ljava/lang/Object;I)V"));
            getClassForVmTypeMethod = testMethod(vmSoftByteCodesClass.getMethod("getClassForVmType",
                "(Lorg/jnode/vm/classmgr/VmType;)Ljava/lang/Class;"));

            // Write barrier
            writeBarrier = (heapManager != null) ? heapManager
                .getWriteBarrier() : null;
            if (writeBarrier != null) {
                final VmType wbClass = loader.loadClass(writeBarrier.getClass()
                    .getName(), true);
                arrayStoreWriteBarrier = testMethod(wbClass.getMethod(
                    "arrayStoreWriteBarrier",
                    "(Ljava/lang/Object;ILjava/lang/Object;)V"));
                putfieldWriteBarrier = testMethod(wbClass.getMethod(
                    "putfieldWriteBarrier",
                    "(Ljava/lang/Object;ILjava/lang/Object;)V"));
                putstaticWriteBarrier = testMethod(wbClass.getMethod(
                    "putstaticWriteBarrier", "(ILjava/lang/Object;)V"));
            } else {
                arrayStoreWriteBarrier = null;
                putfieldWriteBarrier = null;
                putstaticWriteBarrier = null;
            }

            // MonitorManager
            this.vmMonitorManagerClass = loader.loadClass(
                "org.jnode.vm.scheduler.MonitorManager", true);
            monitorEnterMethod = testMethod(vmMonitorManagerClass.getMethod(
                "monitorEnter", "(Ljava/lang/Object;)V"));
            monitorExitMethod = testMethod(vmMonitorManagerClass.getMethod(
                "monitorExit", "(Ljava/lang/Object;)V"));

            // MathSupport
            final VmType vmClass = loader.loadClass("org.jnode.vm.MathSupport",
                true);
            ldivMethod = testMethod(vmClass.getMethod("ldiv", "(JJ)J"));
            lremMethod = testMethod(vmClass.getMethod("lrem", "(JJ)J"));

            // VmInstanceField
            this.vmInstanceFieldClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmInstanceField", true);
            vmFieldOffsetField = (VmInstanceField) testField(vmInstanceFieldClass
                .getField("offset"));

            // VmStaticField
            this.vmStaticFieldClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmStaticField", true);
            vmFieldStaticsIndexField = (VmInstanceField) testField(vmStaticFieldClass
                .getField("staticsIndex"));

            // VmInstanceMethod
            this.vmInstanceMethodClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmInstanceMethod", true);
            vmMethodTibOffsetField = (VmInstanceField) testField(vmInstanceMethodClass
                .getField("tibOffset"));

            // VmMethodCode
            this.vmMethodCodeClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmMethodCode", true);
            vmMethodSelectorField = (VmInstanceField) testField(vmInstanceMethodClass
                .getField("selector"));
            vmMethodNativeCodeField = (VmInstanceField) testField(vmInstanceMethodClass
                .getField("nativeCode"));

            // VmConstIMethodRef
            final VmType cimrClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmConstIMethodRef", true);
            this.vmConstIMethodRefSelectorField = (VmInstanceField) testField(cimrClass
                .getField("selector"));

            // VmProcessor
            final VmType processorClass = loader.loadClass(
                "org.jnode.vm.scheduler.VmProcessor", true);
            vmThreadSwitchIndicatorOffset =
                ((VmInstanceField) testField(processorClass.getField("threadSwitchIndicator"))).getOffset();
            yieldPoint = testMethod(processorClass.getMethod("yieldPoint", "()V"));
            vmProcessorMeField = (VmInstanceField) testField(processorClass.getField("me"));
            vmProcessorStackEnd = (VmInstanceField) testField(processorClass
                .getField("stackEnd"));
            vmProcessorSharedStaticsTable = (VmInstanceField) testField(processorClass
                .getField("staticsTable"));
            vmProcessorIsolatedStaticsTable = (VmInstanceField) testField(processorClass
                .getField("isolatedStaticsTable"));

            // VmType
            final VmType typeClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmType", true);
            vmTypeInitialize = testMethod(typeClass.getMethod("initialize",
                "()V"));
            vmTypeModifiers = (VmInstanceField) testField(typeClass
                .getField("modifiers"));
            vmTypeState = (VmInstanceField) testField(typeClass
                .getField("state"));
            vmTypeCp = (VmInstanceField) testField(typeClass.getField("cp"));

            // VmCP
            final VmType cpClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmCP", true);
            vmCPCp = (VmInstanceField) testField(cpClass.getField("cp"));

            // VmProcessor
            // VmThread
            final VmType vmThreadClass = loader.loadClass("org.jnode.vm.scheduler.VmThread", true);
            systemExceptionMethod = testMethod(vmThreadClass.getMethod(
                "systemException", "(II)Ljava/lang/Throwable;"));

            // VmMethod
            final VmType vmMethodClass = loader.loadClass(
                "org.jnode.vm.classmgr.VmMethod", true);
            recompileMethod = testMethod(vmMethodClass.getDeclaredMethod(
                "recompileMethod", "(II)V"));

        } catch (ClassNotFoundException ex) {
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }

    private final VmMethod testMethod(VmMethod method) {
        if (method == null) {
            throw new RuntimeException("Cannot find a method");
        }
        return method;
    }

    private final VmField testField(VmField field) {
        if (field == null) {
            throw new RuntimeException("Cannot find a field");
        }
        return field;
    }

    /**
     * Gets the allocArray method
     *
     * @return method
     */
    public final VmMethod getAllocArrayMethod() {
        return allocArrayMethod;
    }

    /**
     * Gets the allocObject method
     *
     * @return method
     */
    public final VmMethod getAllocObjectMethod() {
        return allocObjectMethod;
    }

    /**
     * Gets the anewArray
     *
     * @return method
     */
    public final VmMethod getAnewarrayMethod() {
        return anewarrayMethod;
    }

    /**
     * Gets the ldiv method
     *
     * @return method
     */
    public final VmMethod getLdivMethod() {
        return ldivMethod;
    }

    /**
     * Gets the lrem method
     *
     * @return method
     */
    public final VmMethod getLremMethod() {
        return lremMethod;
    }

    /**
     * Gets the monitorEnter method
     *
     * @return method
     */
    public final VmMethod getMonitorEnterMethod() {
        return monitorEnterMethod;
    }

    /**
     * Gets the monitorExit method
     *
     * @return method
     */
    public final VmMethod getMonitorExitMethod() {
        return monitorExitMethod;
    }

    /**
     * Gets the resolveClass method
     *
     * @return method
     */
    public final VmMethod getResolveClassMethod() {
        return resolveClassMethod;
    }

    /**
     * Gets the resolveField method
     *
     * @return method
     */
    public final VmMethod getResolveFieldMethod() {
        return resolveFieldMethod;
    }

    /**
     * Gets the resolveMethod method
     *
     * @return method
     */
    public final VmMethod getResolveMethodMethod() {
        return resolveMethodMethod;
    }

    /**
     * Gets the offset field of VmInstanceField
     *
     * @return field
     */
    public final VmInstanceField getVmFieldOffsetField() {
        return vmFieldOffsetField;
    }

    /**
     * Gets the VmInstanceField class
     *
     * @return type
     */
    public final VmType getVmInstanceFieldClass() {
        return vmInstanceFieldClass;
    }

    /**
     * Gets the VmInstanceMethod class
     *
     * @return type
     */
    public final VmType getVmInstanceMethodClass() {
        return vmInstanceMethodClass;
    }

    /**
     * Gets the VmMethodCode class
     *
     * @return type
     */
    public final VmType getVmMethodCodeClass() {
        return vmMethodCodeClass;
    }

    /**
     * Gets the vmtOffset field of VmMethod
     *
     * @return field
     */
    public final VmInstanceField getVmMethodVmtOffsetField() {
        return vmMethodTibOffsetField;
    }

    /**
     * Gets the MonitorManager class
     *
     * @return type
     */
    public final VmType getVmMonitorManagerClass() {
        return vmMonitorManagerClass;
    }

    /**
     * Gets the SoftByteCodes class
     *
     * @return type
     */
    public final VmType getVmSoftByteCodesClass() {
        return vmSoftByteCodesClass;
    }

    /**
     * Gets the VmStaticField class
     *
     * @return type
     */
    public final VmType getVmStaticFieldClass() {
        return vmStaticFieldClass;
    }

    /**
     * Gets the selector field of VmConstIMethodRef
     *
     * @return type
     */
    public final VmInstanceField getVmConstIMethodRefSelectorField() {
        return vmConstIMethodRefSelectorField;
    }

    /**
     * Gets the selector field of VmMethod
     *
     * @return type
     */
    public final VmInstanceField getVmMethodSelectorField() {
        return vmMethodSelectorField;
    }

    /**
     * Gets the systemException method of SoftByteCodes
     *
     * @return type
     */
    public final VmMethod getSystemExceptionMethod() {
        return systemExceptionMethod;
    }

    /**
     * @return Returns the offset of the vmThreadSwitchIndicator field.
     */
    public final int getVmThreadSwitchIndicatorOffset() {
        return this.vmThreadSwitchIndicatorOffset;
    }

    /**
     * @return Returns the vmTypeInitialize.
     */
    public final VmMethod getVmTypeInitialize() {
        return this.vmTypeInitialize;
    }

    /**
     * @return Returns the vmTypeModifiers.
     */
    public final VmInstanceField getVmTypeModifiers() {
        return this.vmTypeModifiers;
    }

    /**
     * @return Returns the vmTypeState.
     */
    public final VmInstanceField getVmTypeState() {
        return this.vmTypeState;
    }

    /**
     * @return Returns the vmMemberDeclaringClassField.
     */
    public final VmInstanceField getVmMemberDeclaringClassField() {
        return this.vmMemberDeclaringClassField;
    }

    /**
     * @return Returns the vmProcessorStackEnd.
     */
    public final VmInstanceField getVmProcessorStackEnd() {
        return this.vmProcessorStackEnd;
    }

    /**
     * @return Returns the vmMethodNativeCodeField.
     */
    public final VmInstanceField getVmMethodNativeCodeField() {
        return this.vmMethodNativeCodeField;
    }

    /**
     * @return Returns the vmTypeCp.
     */
    public final VmInstanceField getVmTypeCp() {
        return this.vmTypeCp;
    }

    /**
     * @return Returns the vmCPCp.
     */
    public final VmInstanceField getVmCPCp() {
        return this.vmCPCp;
    }

    /**
     * @return Returns the allocPrimitiveArrayMethod.
     */
    public final VmMethod getAllocPrimitiveArrayMethod() {
        return this.allocPrimitiveArrayMethod;
    }

    /**
     * @return Returns the vmFieldStaticsIndexField.
     */
    public final VmInstanceField getVmFieldStaticsIndexField() {
        return this.vmFieldStaticsIndexField;
    }

    /**
     * @return Returns the vmProcessorStaticsTable.
     */
    public final VmInstanceField getVmProcessorSharedStaticsTable() {
        return this.vmProcessorSharedStaticsTable;
    }

    /**
     * @return Returns the arrayStoreWriteBarrier.
     */
    public final VmMethod getArrayStoreWriteBarrier() {
        return this.arrayStoreWriteBarrier;
    }

    /**
     * @return Returns the putfieldWriteBarrier.
     */
    public final VmMethod getPutfieldWriteBarrier() {
        return this.putfieldWriteBarrier;
    }

    /**
     * @return Returns the putstaticWriteBarrier.
     */
    public final VmMethod getPutstaticWriteBarrier() {
        return this.putstaticWriteBarrier;
    }

    /**
     * @return Returns the writeBarrier.
     */
    public final VmWriteBarrier getWriteBarrier() {
        return this.writeBarrier;
    }

    /**
     * @return Returns the magic.
     */
    public final int getMagic() {
        return this.magic;
    }

    /**
     * @return Returns the allocMultiArrayMethod.
     */
    public final VmMethod getAllocMultiArrayMethod() {
        return this.allocMultiArrayMethod;
    }

    /**
     * @return Returns the throwArrayOutOfBounds.
     */
    public final VmMethod getThrowArrayOutOfBounds() {
        return throwArrayOutOfBounds;
    }

    /**
     * @return Returns the classCastFailedMethod.
     */
    public final VmMethod getClassCastFailedMethod() {
        return classCastFailedMethod;
    }

    /**
     * @return Returns the yieldPoint.
     * @see org.jnode.vm.scheduler.VmProcessor#yieldPoint()
     */
    public final VmMethod getYieldPoint() {
        return yieldPoint;
    }

    /**
     * @return Returns the recompileMethod.
     * @see VmMethod#recompileMethod(int, int)
     */
    public final VmMethod getRecompileMethod() {
        return recompileMethod;
    }

    /**
     * @return Returns the getClassForVmTypeMethod.
     * @see org.jnode.vm.SoftByteCodes#getClassForVmType(VmType)
     */
    public final VmMethod getGetClassForVmTypeMethod() {
        return getClassForVmTypeMethod;
    }

    /**
     * @return Returns the vmProcessorIsolatedStaticsTable.
     */
    public final VmInstanceField getVmProcessorIsolatedStaticsTable() {
        return vmProcessorIsolatedStaticsTable;
    }

    /**
     * @return Returns the vmProcessorMeField.
     */
    public final VmInstanceField getVmProcessorMeField() {
        return vmProcessorMeField;
    }
}
