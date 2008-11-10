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
