Welcome to JNode!

Version: $Id$

In this file, you find the instructions needed to setup a JNode development environment.

Sub-Projects
------------

JNode has been divided into several sub-projects in order to keep it "accessible". These sub-projects are:

JNode-All   The root project where everything comes together
JNode-Core  The core java classes, the Virtual Machine, the OS kernel and the Driver framework
JNode-FS    The Filesystems and the various block device drivers
JNode-GUI   The AWT implementation and the various video & input device drivers
JNode-Net   The Network implementation and the various network device drivers
JNode-Shell The Command line shell and several system commands

Each sub-project has the same directory structure.

<subprj>/build       All build results 
<subprj>/descriptors All plugin descriptors
<subprj>/lib         All sub-project specific libraries
<subprj>/src         All sources 
<subprj>/.classpath  The eclipse classpath file
<subprj>/.project    The eclipse project file
<subprj>/build.xml   The Ant buildfile

Eclipse
-------

JNode is usually developed in Eclipse. (It can be done without)
The various sub-projects must be imported into eclipse. Since they reference each other, it is advisably to import them in the following order:

1) JNode-Core
2) JNode-Shell
3) JNode-FS
4) JNode-GUI
5) JNode-Net
6) JNode-Builder
7) JNode-All

Building
--------
Execute build.bat/sh

Or from within Eclipse, execute the "all" target of all/build.xml.

The build will result in the following files:

all/build/jnodedisk.pln           A disk image for use in VMWare 3.0
all/build/x86/netboot/jnodesys.gz A  bootable kernel image for use in Grub.

Questions
---------

If you have any questions, please post them on the jnode-devel@lists.sourceforge.net mailing list of contact the author.

Ewout Prangsma
epr@users.sourceforge.net


