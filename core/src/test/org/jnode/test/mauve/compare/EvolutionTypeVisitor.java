package org.jnode.test.mauve.compare;


public class EvolutionTypeVisitor implements ComparisonVisitor {
    private final int[] counters = new int[EvolutionType.values().length];

    public int getCounter(EvolutionType type) {
        return counters[type.ordinal()];
    }
    
    @Override
    public void visit(RunComparison run) {
        // nothing
    }

    @Override
    public void visit(PackageComparison pkg) {
        // nothing
    }

    @Override
    public void visit(ClassComparison cls) {
        // nothing
    }

    @Override
    public void visit(TestComparison test) {
        counters[test.getEvolutionType().ordinal()]++;
    }

}
