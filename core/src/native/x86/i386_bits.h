; -----------------------------------------------
; $Id$
;
; Intel 386+ constants
;
; Author       : E.Prangsma 
; -----------------------------------------------

; ----------------------
; Define address macro
; ----------------------

%undef DA
%undef SLOT_SIZE
%undef WORD
%undef AAX
%undef ABX
%undef ACX
%undef ADX
%undef ADI
%undef ASI
%undef ABP
%undef ASP

%ifdef BITS32
	%define DA			dd
	%define SLOT_SIZE	4
	%define WORD		dword
	; Word sized registers
	%define AAX			eax
	%define ABX			ebx
	%define ACX			ecx
	%define ADX			edx
	%define ADI			edi
	%define ASI			esi
	%define ABP			ebp
	%define ASP			esp
%endif
%ifdef BITS64
	%define DA			dq
	%define SLOT_SIZE	8
	%define WORD		qword
	%define AAX			rax
	%define ABX			rbx
	%define ACX			rcx
	%define ADX			rdx
	%define ADI			rdi
	%define ASI			rsi
	%define ABP			rbp
	%define ASP			rsp
%endif
	

