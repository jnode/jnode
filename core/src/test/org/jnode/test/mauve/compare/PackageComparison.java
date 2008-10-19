package org.jnode.test.mauve.compare;

import org.jnode.test.mauve.PackageResult;

/**
 * Result of the comparison of 2 {@link PackageResult}s
 * @author fabien
 *
 */
public class PackageComparison extends Comparison<PackageResult> {

    PackageComparison(PackageResult result) {
        super(result);
    }

    @Override
    public void accept(ComparisonVisitor visitor) {
        visitor.visit(this);
        acceptChildren(visitor);
    }
}
