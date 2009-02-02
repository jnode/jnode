/*
 * $Id$
 *
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
 
package org.jnode.test;

public class TestFPStackException {
    public static void main(String[] args) {
        System.out.println("creating instances");
        Matrix m = new Matrix(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        Matrix m2 = new Matrix(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);

        System.out.println("calling times(...)");
        
        // without bug fix, here the jnode compiler will fail with a org.jnode.vm.bytecode.StackException
        Matrix m3 = m.times(m2);
        
        System.out.println("end");
    }
    
    private static class Matrix {
        private double m11, m12, m13, m14;
        private double m21, m22, m23, m24;
        private double m31, m32, m33, m34;
        private double m41, m42, m43, m44;
        
        public Matrix(double e11, double e12, double e13, double e14, double e21, double e22, double e23,
                double e24, double e31, double e32, double e33, double e34, double e41, double e42,
                double e43, double e44) {
            m11 = e11;
            m12 = e12;
            m13 = e13;
            m14 = e14;
            m21 = e21;
            m22 = e22;
            m23 = e23;
            m24 = e24;
            m31 = e31;
            m32 = e32;
            m33 = e33;
            m34 = e34;
            m41 = e41;
            m42 = e42;
            m43 = e43;
            m44 = e44;
        }
    
        public final Matrix times(Matrix a) {
            return new Matrix(m11 * a.m11 + m12 * a.m21 + m13 * a.m31 + m14 * a.m41, m11 * a.m12 + m12 *
                    a.m22 + m13 * a.m32 + m14 * a.m42, m11 * a.m13 + m12 * a.m23 + m13 * a.m33 + m14 *
                    a.m43, m11 * a.m14 + m12 * a.m24 + m13 * a.m34 + m14 * a.m44, m21 * a.m11 + m22 *
                    a.m21 + m23 * a.m31 + m24 * a.m41, m21 * a.m12 + m22 * a.m22 + m23 * a.m32 + m24 *
                    a.m42, m21 * a.m13 + m22 * a.m23 + m23 * a.m33 + m24 * a.m43, m21 * a.m14 + m22 *
                    a.m24 + m23 * a.m34 + m24 * a.m44, m31 * a.m11 + m32 * a.m21 + m33 * a.m31 + m34 *
                    a.m41, m31 * a.m12 + m32 * a.m22 + m33 * a.m32 + m34 * a.m42, m31 * a.m13 + m32 *
                    a.m23 + m33 * a.m33 + m34 * a.m43, m31 * a.m14 + m32 * a.m24 + m33 * a.m34 + m34 *
                    a.m44, m41 * a.m11 + m42 * a.m21 + m43 * a.m31 + m44 * a.m41, m41 * a.m12 + m42 *
                    a.m22 + m43 * a.m32 + m44 * a.m42, m41 * a.m13 + m42 * a.m23 + m43 * a.m33 + m44 *
                    a.m43, m41 * a.m14 + m42 * a.m24 + m43 * a.m34 + m44 * a.m44);
        }
    }
}
