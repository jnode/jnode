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
import org.jnode.test.mauve.ClassResult;
import org.jnode.test.mauve.PackageResult;
import org.jnode.test.mauve.RunResult;
import org.jnode.test.mauve.TestResult;

/**
 * Result of the comparison of 2 {@link RunResult}s
 * @author fabien
 *
 */
public class RunComparison extends Comparison<RunResult> {
    
    RunComparison(RunResult result1, RunResult result2) {
        super(new RunResult("Comparison of '" + result1.getName() + "' and '" + result2.getName() + "'"));
    }

    /**
     * 
     * @param pkg
     * @param cls
     * @param test
     * @param check, might be null
     * @param nbProgressedCheck, might be < 0 for regressions in the test
     */
    public void setProgression(PackageResult pkg, ClassResult cls, TestResult test,
            CheckResult check, int nbProgressedChecks) {
        // package
        Comparison<?> pc = get(pkg.getName());
        if (pc == null) {
            pc = new PackageComparison(pkg);
            add(pc);
        }

        // class
        Comparison<?> classComp = pc.get(cls.getName());
        if (classComp == null) {
            classComp = new ClassComparison(cls);
            pc.add(classComp);
        }

        // test
        classComp.add(new TestComparison(test, check, nbProgressedChecks));
    }
    

    @Override
    public void accept(ComparisonVisitor visitor) {
        visitor.visit(this);
        acceptChildren(visitor);
    }
}
