#!/bin/sh

qemu-system-x86_64 -machine accel=kvm:tcg -m 768 -sdl -name "JNode x86" -cdrom all/build/cdroms/jnode-x86-lite.iso -usb -vga vmware $JNODE_QEMU_ARGS "$@"

