// Copyright (c) 2008 Fabien DUMINY (fduminy@jnode.org)

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.  */

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
