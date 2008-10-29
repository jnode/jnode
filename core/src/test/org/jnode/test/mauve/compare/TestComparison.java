package org.jnode.test.mauve.compare;

import org.jnode.test.mauve.CheckResult;
import org.jnode.test.mauve.TestResult;

/**
 * Result of the comparison of 2 {@link TestResult}s
 * @author fabien
 *
 */
public class TestComparison extends Comparison<TestResult> {
    private final int progression;
    private final CheckResult check;
    /**
     * 
     * @param test
     * @param check, might be null
     * @param nbProgressedChecks
     */
    public TestComparison(TestResult test, CheckResult check, int nbProgressedChecks) {
        super(test);
        this.progression = nbProgressedChecks;
        this.check = check;
    }
    
    public CheckResult getCheckResult() {
        return check;
    }

    @Override
    public int getProgression() {
        return progression;
    }
    
    public EvolutionType getEvolutionType() {
        EvolutionType type = EvolutionType.STAGNATION;
        if (getProgression() > 0) {
            type = EvolutionType.PROGRESSION;
        } else if (getProgression() < 0) {
            type = EvolutionType.REGRESSION;
        }
        return type;
    }

    @Override
    public void accept(ComparisonVisitor visitor) {
        visitor.visit(this);
    }    
}
