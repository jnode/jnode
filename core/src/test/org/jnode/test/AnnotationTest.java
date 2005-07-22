/*
 * $Id$
 */
package org.jnode.test;

import java.lang.annotation.Annotation;
import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

public class AnnotationTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        show("Annotations for class A", A.class.getAnnotations());
        //show("Annotations for class B", B.class.getAnnotations());
        show("Annotations for class Test", Test.class.getAnnotations());
        show("Annotations for class Test2", Test2.class.getAnnotations());
        show("Annotations for class Test3", Test3.class.getAnnotations());
        show("Annotations for class Test4", Test4.class.getAnnotations());
        show("Declared annotations for class A", A.class.getDeclaredAnnotations());
//        show("Declared annotations for class B", B.class.getDeclaredAnnotations());
    }
    
    private static void show(String msg, Annotation[] ann) {
        System.out.println(msg);
        for (Annotation a : ann) {
            System.out.println(a);
        }        
        System.out.println();
    }
    
    @Test
    @Test4
    public static class A {
        
    }

    @Test2(name="ewout", descr="programmer")
    @Test3(name=5, descr={1,2,3,4})
    public static class B extends A {
        
        @Test
        public void foo() {
            
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({TYPE, METHOD})
    public @interface Test { }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    public @interface Test2 {
        String name();
        String descr();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    public @interface Test3 {
        int name();
        int[] descr();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({TYPE, METHOD})
    @Inherited
    public @interface Test4 { }

    
}
