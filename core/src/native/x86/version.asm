; -----------------------------------------------
; $Id$
;
; JNode Version
;
; Author       : E.Prangsma 
; -----------------------------------------------

global sys_version
; sys_version: db '$Id$',0xd,0xa,0

sys_version: db 'Version ',JNODE_VERSION,0xd,0xa,0
