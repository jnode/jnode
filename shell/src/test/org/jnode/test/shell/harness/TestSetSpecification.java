package org.jnode.test.shell.harness;

import java.util.ArrayList;
import java.util.List;

public class TestSetSpecification {

    private final List<TestSpecification> specs = 
        new ArrayList<TestSpecification>();
    
    private final List<PluginSpecification> plugins = 
        new ArrayList<PluginSpecification>();
    
    private final String title;

    public TestSetSpecification(String title) {
        super();
        this.title = title;
    }

    public List<TestSpecification> getSpecs() {
        return specs;
    }

    public List<PluginSpecification> getPlugins() {
        return plugins;
    }

    public String getTitle() {
        return title;
    }
    
    public void addPluginSpec(PluginSpecification plugin) {
        plugins.add(plugin);
    }
    
    public void addTestSpec(TestSpecification spec) {
        specs.add(spec);
        spec.setTestSet(this);
    }
}
