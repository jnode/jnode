package org.jnode.test.mauve.compare;

import org.jnode.test.mauve.ClassResult;

/**
 * Result of the comparison of 2 {@link ClassResult}s
 * @author fabien
 *
 */
public class ClassComparison extends Comparison<ClassResult> {

    ClassComparison(ClassResult result) {
        super(result);
    }

    @Override
    public void accept(ComparisonVisitor visitor) {
        visitor.visit(this);
        acceptChildren(visitor);
    }
}
