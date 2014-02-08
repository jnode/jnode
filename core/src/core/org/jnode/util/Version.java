package org.jnode.util;

/**
 * Version number consisting of up to 4 numbers.
 *
 * @author ewout
 */
public class Version implements Comparable<Version> {

    private static final int Undefined = -1;
    private final int major;
    private final int minor;
    private final int build;
    private final int revision;
    private final String tag;

    public Version(int major, int minor, int build, int revision, String tag) {

        if (major < 0)
            throw new IllegalArgumentException("major: " + major);
        if (minor < 0)
            throw new IllegalArgumentException("minor: " + minor);
        if (build < 0)
            throw new IllegalArgumentException("build: " + build);
        if (revision < 0)
            throw new IllegalArgumentException("revision: " + revision);

        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;
        this.tag = tag;
    }

    public Version(int major, int minor, int build, int revision) {
        this(major, minor, build, revision, null);
    }

    public Version(int major, int minor, int build, String tag) {

        if (major < 0)
            throw new IllegalArgumentException("major: " + major);
        if (minor < 0)
            throw new IllegalArgumentException("minor: " + minor);
        if (build < 0)
            throw new IllegalArgumentException("build: " + build);

        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = Undefined;
        this.tag = tag;
    }

    public Version(int major, int minor, int build) {
        this(major, minor, build, null);
    }

    public Version(int major, int minor, String tag) {

        if (major < 0)
            throw new IllegalArgumentException("major: " + major);
        if (minor < 0)
            throw new IllegalArgumentException("minor: " + minor);

        this.major = major;
        this.minor = minor;
        this.build = Undefined;
        this.revision = Undefined;
        this.tag = tag;
    }

    public Version(int major, int minor) {
        this(major, minor, null);
    }

    public Version(String version) {
        String[] outerParts = version.split("-", 2);
        String[] parts = outerParts[0].split("\\.");
        if (parts.length > 4)
            throw new IllegalArgumentException("Too many parts: " + version);
        major = Integer.parseInt(parts[0]);
        minor = (parts.length > 1) ? Integer.parseInt(parts[1]) : Undefined;
        build = (parts.length > 2) ? Integer.parseInt(parts[2]) : Undefined;
        revision = (parts.length > 3) ? Integer.parseInt(parts[3]) : Undefined;
        tag = (outerParts.length > 1) ? outerParts[1] : null;
        if (tag != null) {
            if (tag.length() == 0)
                throw new IllegalArgumentException("tag empty");
            if (Character.isDigit(tag.charAt(0)))
                throw new IllegalArgumentException("tag starts with digit");
        }

        if (major < 0)
            throw new IllegalArgumentException("major: " + major);
        if ((minor < 0) && (parts.length > 1))
            throw new IllegalArgumentException("minor: " + minor);
        if ((build < 0) && (parts.length > 2))
            throw new IllegalArgumentException("build: " + build);
        if ((revision < 0) && (parts.length > 3))
            throw new IllegalArgumentException("revision: " + revision);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return Math.max(0, minor);
    }

    public int getBuild() {
        return Math.max(0, build);
    }

    public int getRevision() {
        return Math.max(0, revision);
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int compareTo(Version o) {
        if (major < o.major)
            return -1;
        if (major > o.major)
            return 1;

        if ((minor != Undefined) || (o.minor != Undefined)) {
            if (minor == Undefined)
                return -1;
            if (o.minor == Undefined)
                return 1;

            if (minor < o.minor)
                return -1;
            if (minor > o.minor)
                return 1;

            if ((build != Undefined) || (o.build != Undefined)) {
                if (build == Undefined)
                    return -1;
                if (o.build == Undefined)
                    return 1;

                if (build < o.build)
                    return -1;
                if (build > o.build)
                    return 1;

                if ((revision != Undefined) || (o.revision != Undefined)) {
                    if (revision == Undefined)
                        return -1;
                    if (o.revision == Undefined)
                        return 1;

                    if (revision < o.revision)
                        return -1;
                    if (revision > o.revision)
                        return 1;
                }
            }
        }

        if ((tag != null) && (o.tag == null))
            return 1;
        if ((tag == null) && (o.tag != null))
            return -1;
        if ((tag != null) && (o.tag != null))
            return tag.compareTo(o.tag);

        return 0;
    }

    @Override
    public int hashCode() {
        int hash = (major << 24) | (minor << 16) | (build << 8) | revision;
        if (tag != null)
            hash |= tag.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        String s;
        if (revision != Undefined)
            s = major + "." + minor + "." + build + "." + revision;
        else if (build != Undefined)
            s = major + "." + minor + "." + build;
        else if (minor != Undefined)
            s = major + "." + minor;
        else
            s = Integer.toString(major);
        if (tag != null)
            return s + "-" + tag;
        return s;
    }
}
