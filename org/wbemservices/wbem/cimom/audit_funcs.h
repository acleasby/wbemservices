/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the 
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright © 2003 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

#ifndef __AUDIT_FUNCS_H__
#define __AUDIT_FUNCS_H__

typedef int (*audit_login_save_host_fct) (char*);

typedef int (*audit_login_save_ttyn_fct) (char*);

typedef int (*audit_login_save_port_fct) (void);

typedef int (*audit_login_success_fct) (void);

typedef int (*audit_login_save_pw_fct) (struct passwd *);

typedef int (*audit_login_bad_pw_fct) (void);
typedef int (*audit_admin_auth_success_fct) (char*, char*, long);
typedef int (*audit_admin_auth_fail_fct) (char*, char*, int);


typedef struct audit_int {
	audit_login_save_host_fct audit_login_save_host;
	audit_login_save_ttyn_fct audit_login_save_ttyn;
	audit_login_save_port_fct audit_login_save_port;
	audit_login_success_fct audit_login_success;
	audit_login_save_pw_fct audit_login_save_pw;
	audit_login_bad_pw_fct audit_login_bad_pw;
	audit_admin_auth_fail_fct audit_admin_auth_fail;
	audit_admin_auth_success_fct audit_admin_auth_success;
} audit_int_t;

#endif
