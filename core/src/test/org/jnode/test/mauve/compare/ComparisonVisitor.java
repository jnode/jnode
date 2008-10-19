package org.jnode.test.mauve.compare;

/**
 * Interface for a {@link Comparison} visitor
 * 
 * @author fabien
 *
 */
public interface ComparisonVisitor {

    void visit(RunComparison run);

    void visit(PackageComparison pkg);

    void visit(ClassComparison cls);

    void visit(TestComparison test);
}
