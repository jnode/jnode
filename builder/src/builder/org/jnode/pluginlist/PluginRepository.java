package org.jnode.pluginlist;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 */
class PluginRepository {
    private List<Project> available = new ArrayList<Project>();
    private List<Project> selected = new ArrayList<Project>();
    private HashMap<String, PluginInfo> pluginMap = new HashMap<String, PluginInfo>();

    void addSystemPlugin(Plugin plu) {
        if (plu.isSystem()) {
            pluginMap.put(plu.getId(), new PluginInfo(plu, null));
        }
    }

    void reselect(PluginList pluginList) {
        Set<Plugin> ps = new HashSet<Plugin>();
        for (Map.Entry<String, PluginInfo> e : pluginMap.entrySet()) {
            if (e.getValue().selected)
                ps.add(e.getValue().plugin);
        }
        deselect(ps);

        ps.clear();
        for (String p : pluginList.getPlugins()) {
            Plugin plug = getPlugin(p);
            if (plug == null)
                throw new RuntimeException("Invalid plugin in plugin list: " + p);

            ps.add(plug);
        }

        select(ps);
        Main.setSaved();
    }

    Set<Plugin> select(Set<Plugin> plugins) {
        Set<Plugin> ret = new HashSet<Plugin>();
        for (Plugin plugin : plugins)
            select0(plugin, ret);
        sortSeleted();
        if (!ret.isEmpty())
            Main.setUnsaved();
        return ret;
    }

    private void select0(Plugin plugin, Set<Plugin> set) {
        PluginInfo pi = pluginMap.get(plugin.getId());
        if (pi.selected)
            return;

        Plugin plu = pi.plugin;
        if (plu.isSystem())
            return;

        Project a_pro = getProject(pi.project, available);
        Project s_pro = getProject(pi.project, selected);

        a_pro.remove(plu);
        s_pro.addPlugin(plu);
        s_pro.sort();
        pi.selected = true;

        set.add(plu);

        for (Plugin r : plu.allRequired())
            select0(r, set);
    }

    public Set<Plugin> deselect(Set<Plugin> plugins) {
        Set<Plugin> ret = new HashSet<Plugin>();
        for (Plugin plugin : plugins)
            deselect0(plugin, ret);
        sortAvailable();
        if (!ret.isEmpty())
            Main.setUnsaved();
        return ret;
    }

    private void deselect0(Plugin plugin, Set<Plugin> set) {
        PluginInfo pi = pluginMap.get(plugin.getId());
        if (!pi.selected)
            return;

        Plugin plu = pi.plugin;
        if (plu.isSystem())
            return;

        Project a_pro = getProject(pi.project, available);
        Project s_pro = getProject(pi.project, selected);

        s_pro.remove(plu);
        a_pro.addPlugin(plu);
        a_pro.sort();
        pi.selected = false;

        set.add(plu);

        for (Plugin r : plu.allUsed())
            deselect0(r, set);
    }

    Project getProject(String name, List<Project> list) {
        for (Project pro : list)
            if (name.equals(pro.getName()))
                return pro;
        return null;
    }

    void reload(PluginList pluginList) throws Exception {
        available.clear();
        selected.clear();
        pluginMap.clear();

        addProject(Main.readProject("core", this));
        addProject(Main.readProject("gui", this));
        addProject(Main.readProject("fs", this));
        addProject(Main.readProject("net", this));
        addProject(Main.readProject("shell", this));
        addProject(Main.readProject("distr", this));
        addProject(Main.readProject("textui", this));

        validate();
        reselect(pluginList);
        sortAvailable();
        sortSeleted();
    }

    static class PluginInfo {
        Plugin plugin;
        String project;
        boolean selected;

        PluginInfo(Plugin plugin, String project) {
            this.plugin = plugin;
            this.project = project;
        }
    }

    void addProject(Project proj) {
        available.add(proj);
        Project p = new Project(proj.getName());
        selected.add(p);
    }

    public Project getProject(int index) {
        return available.get(index);
    }

    @Override
    public String toString() {
        return "";
    }

    public int size() {
        return available.size();
    }

    Plugin getPlugin(String id) {
        return pluginMap.get(id).plugin;
    }

    String getProject(String id) {
        return pluginMap.get(id).project;
    }

    boolean isSelected(String id) {
        return pluginMap.get(id).selected;
    }

    void validate() {
        for (Project j : available) {
            for (Plugin p : j.plugins()) {
                pluginMap.put(p.getId(), new PluginInfo(p, j.getName()));
            }
        }

        for (Map.Entry<String, PluginInfo> e : pluginMap.entrySet()) {
            e.getValue().plugin.resolve(this);
        }
    }

    void sortAvailable() {
        Collections.sort(available);
        for (Project p : available)
            p.sort();
    }

    void sortSeleted() {
        Collections.sort(selected);
        for (Project p : selected)
            p.sort();
    }

    PluginListModel getAvailableModel() {
        return new PluginListModelImpl(available) {
            public String getTooltipText(Plugin plugin) {
                Set<Plugin> plugins = plugin.allRequired();
                Set<Plugin> plugins2 = new HashSet<Plugin>();
                for (Plugin p : plugins) {
                    if (!getRepository().isSelected(p.getId()))
                        plugins2.add(p);
                }

                if (plugins2.size() > 0) {
                    StringBuilder sb = new StringBuilder("<html> <b>Requires:</b><br/>");
                    for (Plugin p : plugins2) {
                        sb.append(p.getId());
                        sb.append("<br/>");
                    }
                    sb.append("</html>");
                    return sb.length() == 0 ? null : sb.toString();
                }
                return null;
            }
        };
    }

    PluginListModel getSelectedModel() {
        return new PluginListModelImpl(selected) {
            public String getTooltipText(Plugin plugin) {
                Set<Plugin> plugins = plugin.allUsed();
                Set<Plugin> plugins2 = new HashSet<Plugin>();
                for (Plugin p : plugins) {
                    if (getRepository().isSelected(p.getId()))
                        plugins2.add(p);
                }

                if (plugins2.size() > 0) {
                    StringBuilder sb = new StringBuilder("<html> <b>Used by:</b><br/>");
                    for (Plugin p : plugins2) {
                        sb.append(p.getId());
                        sb.append("<br/>");
                    }
                    sb.append("</html>");
                    return sb.length() == 0 ? null : sb.toString();
                }
                return null;
            }
        };
    }

    Set<String> getSelectedPluginIds() {
        Set<String> ret = new TreeSet<String>();
        for (Map.Entry<String, PluginInfo> e : pluginMap.entrySet()) {
            if (e.getValue().selected)
                ret.add(e.getKey());
        }
        return ret;
    }

    private abstract class PluginListModelImpl implements PluginListModel {
        private List<Project> projectList;

        private PluginListModelImpl(List<Project> projectList) {
            this.projectList = projectList;
        }

        @Override
        public PluginRepository getRepository() {
            return PluginRepository.this;
        }

        @Override
        public Project getProject(int index) {
            return projectList.get(index);
        }

        @Override
        public int size() {
            return projectList.size();
        }
    }
}
