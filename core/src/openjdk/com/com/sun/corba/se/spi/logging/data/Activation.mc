;
; Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
; DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
;
; This code is free software; you can redistribute it and/or modify it
; under the terms of the GNU General Public License version 2 only, as
; published by the Free Software Foundation.  Sun designates this
; particular file as subject to the "Classpath" exception as provided
; by Sun in the LICENSE file that accompanied this code.
;
; This code is distributed in the hope that it will be useful, but WITHOUT
; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
; FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
; version 2 for more details (a copy is included in the LICENSE file that
; accompanied this code).
;
; You should have received a copy of the GNU General Public License version
; 2 along with this work; if not, write to the Free Software Foundation,
; Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
;
; Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
; CA 95054 USA or visit www.sun.com if you need additional information or
; have any questions.
;
("com.sun.corba.se.impl.logging" "ActivationSystemException" ACTIVATION
    (
	(INITIALIZE
	    (CANNOT_READ_REPOSITORY_DB 1 WARNING "Cannot read repository datastore")
	    (CANNOT_ADD_INITIAL_NAMING 2 WARNING "Cannot add initial naming"))
	(INTERNAL
	    (CANNOT_WRITE_REPOSITORY_DB 1 WARNING "Cannot write repository datastore")
	    (SERVER_NOT_EXPECTED_TO_REGISTER 3 WARNING "Server not expected to register")
	    (UNABLE_TO_START_PROCESS 4 WARNING "Unable to start server process")
	    (SERVER_NOT_RUNNING 6 WARNING "Server is not running"))
	(OBJECT_NOT_EXIST
	    (ERROR_IN_BAD_SERVER_ID_HANDLER 1 WARNING "Error in BadServerIdHandler"))))
