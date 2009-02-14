package org.jnode.pluginlist;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
class Plugin implements Comparable {
    private String id;
    private boolean system;
    private Set<Plugin> required = new HashSet<Plugin>();
    private Set<Plugin> used = new HashSet<Plugin>();
    private Set<String> requiredId = new HashSet<String>();


    public Plugin(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setRequired(Set<Plugin> required) {
        this.required = required;
    }

    public Set<Plugin> getRequired() {
        return required;
    }

    void addRecuiredId(String pid) {
        requiredId.add(pid);
    }

    void resolve(PluginRepository m) {
        List<String> sys = new ArrayList<String>();
        for (String r : requiredId) {
            Plugin plugin = m.getPlugin(r);
            if (plugin == null)
                throw new RuntimeException("Unknown plugin: " + r);

            if (plugin.isSystem()) {
                sys.add(r);
                continue;
            }

            required.add(plugin);
            plugin.used.add(this);
        }

        requiredId.removeAll(sys);
    }

    @Override
    public int compareTo(Object o) {
        return this.id.compareTo(((Plugin) o).id);
    }

    @Override
    public String toString() {
        return id;
    }

    Set<Plugin> allRequired() {
        Set<Plugin> req = new HashSet<Plugin>();
        allRequired0(req);
        return req;
    }

    private void allRequired0(Set<Plugin> req) {
        for (Plugin p : required) {
            req.add(p);
            p.allRequired0(req);
        }
    }

    Set<Plugin> allUsed() {
        Set<Plugin> req = new HashSet<Plugin>();
        allUsed0(req);
        return req;
    }

    private void allUsed0(Set<Plugin> req) {
        for (Plugin p : used) {
            req.add(p);
            p.allUsed0(req);
        }
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem() {
        this.system = true;
    }
}
