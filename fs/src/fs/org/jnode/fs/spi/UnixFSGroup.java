package org.jnode.fs.spi;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

public class UnixFSGroup implements Group {

    private final String name;

    private final Set<Principal> members = new LinkedHashSet<Principal>();

    public UnixFSGroup(String name) {
        this.name = name;
    }

    @Override
    public boolean addMember(Principal user) {
        return members.add(user);
    }

    @Override
    public boolean removeMember(Principal user) {
        return members.remove(user);
    }

    @Override
    public boolean isMember(Principal member) {
        return members.contains(member);
    }

    @Override
    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(members);
    }

    @Override
    public String getName() {
        return name;
    }
}
