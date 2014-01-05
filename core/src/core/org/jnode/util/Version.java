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

	public Version(int major, int minor, int build, int revision) {

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
	}

	public Version(int major, int minor, int build) {

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
	}

	public Version(int major, int minor) {

		if (major < 0)
			throw new IllegalArgumentException("major: " + major);
		if (minor < 0)
			throw new IllegalArgumentException("minor: " + minor);

		this.major = major;
		this.minor = minor;
		this.build = Undefined;
		this.revision = Undefined;
	}

	public Version(String version) {
		String[] parts = version.split("\\.");
		if (parts.length > 4)
			throw new IllegalArgumentException("Too many parts: " + version);
		major = Integer.parseInt(parts[0]);
		minor = (parts.length > 1) ? Integer.parseInt(parts[1]) : Undefined;
		build = (parts.length > 2) ? Integer.parseInt(parts[2]) : Undefined;
		revision = (parts.length > 3) ? Integer.parseInt(parts[3]) : Undefined;

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

	@Override
	public int compareTo(Version o) {
		if (major < o.major)
			return -1;
		if (major > o.major)
			return 1;

		if ((minor == Undefined) && (o.minor == Undefined))
			return 0;
		if (minor == Undefined) 
			return -1;
		if (o.minor == Undefined) 
			return 1;
		
		if (minor < o.minor)
			return -1;
		if (minor > o.minor)
			return 1;

		if ((build == Undefined) && (o.build == Undefined))
			return 0;
		if (build == Undefined) 
			return -1;
		if (o.build == Undefined) 
			return 1;
		
		if (build < o.build)
			return -1;
		if (build > o.build)
			return 1;

		if ((revision == Undefined) && (o.revision == Undefined))
			return 0;
		if (revision == Undefined) 
			return -1;
		if (o.revision == Undefined) 
			return 1;		

		if (revision < o.revision)
			return -1;
		if (revision > o.revision)
			return 1;

		return 0;
	}

	@Override
	public String toString() {
		if (revision != Undefined)
			return major + "." + minor + "." + build + "." + revision;
		if (build != Undefined)
			return major + "." + minor + "." + build;
		if (minor != Undefined)
			return major + "." + minor;
		return Integer.toString(major);
	}
}
