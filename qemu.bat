@echo off
set QEMUDIR=c:\progra~1\qemu
%QEMUDIR%\qemu.exe -monitor vc -m 400 -L %QEMUDIR%\bios -hda all\build\x86\jnodedisk.dat