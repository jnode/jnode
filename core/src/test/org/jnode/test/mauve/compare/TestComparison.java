package org.jnode.test.mauve.compare;

import org.jnode.test.mauve.CheckResult;
import org.jnode.test.mauve.TestResult;

/**
 * 
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

    @Override
    public int compareTo(Comparison<TestResult> o) {
        // regressions have negative progression
        // we sort from bigger regression to bigger progression
        int result = progression - ((TestComparison) o).progression;
        
        if (result == 0) {
            result = getName().compareTo(o.getName());
        }
        
        return result;
    }
}
