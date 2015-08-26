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


#include <string.h>
#include <jni.h>
#include "CIMOMLibrary.h"
#include <shadow.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <syslog.h>
#include <user_attr.h>
#include <secdb.h>
#include <dlfcn.h>
#include <bsm/libbsm.h>
#include <bsm/audit.h>
#include <unistd.h>
#include <pwd.h>
#include "audit_funcs.h"

#define	USERATTR_USERTYPE_KEY "type"
#define	USERATTR_ROLELIST_KEY "roles"
#define	USERATTR_ROLE "role"
#define	USERATTR_USER "normal"
#define	LOCAL_AUTH_FILE_PREFIX ""
#define	PASSWORD_BUFFER_SIZE 2048
/*
 * User type codes.  Must align with UserSecurityProvider constants!
 */
#define	ANY_USER_TYPE 0
#define	NORMAL_USER 1
#define	ROLE_USER 2

static audit_int_t g_audit_interface;
static void *g_dlp;
static audit_int_t *g_audit_int = NULL;
static int audit_init_int(void);

static void set_authentication_error(JNIEnv *env, jobject jerrmsgs,
	const char *errmsg);

extern int cimomlib_authenticate(const char *user_or_role,
    const char *password, const char *roleuser);

JNIEXPORT    jstring    JNICALL
    Java_com_sun_wbem_cimom_CIMOMLibrary_getPassword(
		    JNIEnv*    env,
		    jclass    class_object,
		    jstring    username_string,
		    jint	usertype)
{

	jstring encrypted_password;
	struct spwd *shadow_password;
	struct spwd shadowStruct_r;
	char buff[PASSWORD_BUFFER_SIZE];
	const char *username;
	char *password;
	int utype;

	utype = (int)usertype;
	username = (*env)->GetStringUTFChars(env, username_string, NULL);

	/*
	 * Look up the user in the passwd table and get the password hash.
	 * Set password hash to NULL if user does not exist.
	 */

	shadow_password = getspnam_r(username, &shadowStruct_r,
		buff, PASSWORD_BUFFER_SIZE);
	if (shadow_password != NULL) {
	    if (shadow_password->sp_pwdp == NULL) {
		password = "";
	    } else {
		password = shadow_password->sp_pwdp;
	    }
	} else {
		password = NULL;
	}

	/*
	 * Return the password hash, an empty password, or a NULL password.
	 */
	if (password != NULL && !check_usertype(username, usertype)) {
	    encrypted_password = (*env)->NewStringUTF(env, password);
	} else {
	    encrypted_password = NULL;
	}

	(*env)->ReleaseStringUTFChars(env, username_string, username);

	return (encrypted_password);

}

JNIEXPORT    jstring    JNICALL
    Java_com_sun_wbem_cimom_CIMOMLibrary_writeLocalFile(
		JNIEnv*    env,
		jclass	   class_object,
		jstring    username_string,
		jstring    dirname_string,
		jstring    value_string)
{

	jstring filename_string;
	struct passwd pswd;
	struct passwd *pswd_p;
	char buff[PASSWORD_BUFFER_SIZE];
	const char *username;
	const char *dirname;
	const char *value;
	char *tempname;
	char *filename;
	uid_t uid;
	uid_t gid;
	int fd, len, oflag;
	mode_t mode;

	/* Get the user identity for changing file ownership later */
	username = (*env)->GetStringUTFChars(env, username_string, NULL);
	pswd_p = getpwnam_r(username, &pswd, buff, PASSWORD_BUFFER_SIZE);
	(*env)->ReleaseStringUTFChars(env, username_string, username);
	if (pswd_p == NULL) {
	    return (NULL);
	}
	uid = pswd_p->pw_uid;
	gid = pswd_p->pw_gid;

	/* Create a new temporary file name to contain the value */
	dirname = (*env)->GetStringUTFChars(env, dirname_string, NULL);
	tempname = tempnam(dirname, LOCAL_AUTH_FILE_PREFIX);
	(*env)->ReleaseStringUTFChars(env, dirname_string, dirname);
	if (tempname == NULL) {
	    return (NULL);
	}
	len = strlen(dirname) + strlen(tempname) + 10;
	filename = malloc((size_t)len);
	if (filename == NULL) {
	    free(tempname);
	    return (NULL);
	}
	strcpy(filename, dirname);
	strcat(filename, "/");
	strcat(filename, tempname);

	/* Create the new file such that only root can write to it */
	oflag = O_WRONLY | O_CREAT | O_TRUNC;
	mode = S_IFREG | S_IRUSR;
	fd = open(filename, oflag, mode);
	if (fd < 0) {
	    free(tempname);
	    free(filename);
	    return (NULL);
	}

	/* Get the value into a character array */
	value = (*env)->GetStringUTFChars(env, value_string, NULL);
	if (value == NULL) {
	    /* We use a dummy hex value! */
	    value = "0bad";
	}
	len = strlen(value);

	/* Write the value into the file */
	write(fd, value, len);
	close(fd);
	(*env)->ReleaseStringUTFChars(env, value_string, value);

	/* Change the ownership of the file */
	chown(filename, uid, (gid_t)0);
	free(filename);

	/* Return the file name */
	filename_string = (*env)->NewStringUTF(env, tempname);
	free(tempname);
	return (filename_string);
}

JNIEXPORT    void    JNICALL
    Java_com_sun_wbem_cimom_CIMOMLibrary_doSyslog(
		    JNIEnv*    env,
		    jclass    class_object,
		    jstring    mesg,
		    jstring identity,
		    jint severity)
{

	const	char *message_string;
	const	char *identity_string;
	const	int	log_option;

	message_string = (*env)->GetStringUTFChars(env, mesg, NULL);
	identity_string = (*env)->GetStringUTFChars(env, identity, NULL);

	if (severity == 0) {
	    openlog(identity_string, LOG_PID, LOG_DAEMON);
	    setlogmask(LOG_UPTO(LOG_INFO));

		/*
		* INFO messages from daemon processes may be ignored
		* by some installations - look at /etc/syslog.conf
		*/

	    syslog(LOG_INFO, message_string);
	    closelog();
	} else if (severity == 1) {
	    openlog(identity_string, LOG_PID, LOG_DAEMON);
	    setlogmask(LOG_UPTO(LOG_INFO));
	    syslog(LOG_NOTICE, message_string);
	    closelog();
	} else if (severity == 2) {
	    openlog(identity_string, LOG_PID|LOG_CONS, LOG_DAEMON);
	    setlogmask(LOG_UPTO(LOG_INFO));
	    syslog(LOG_ERR, message_string);
	    closelog();
	}

	(*env)->ReleaseStringUTFChars(env, mesg, message_string);
	(*env)->ReleaseStringUTFChars(env, identity, identity_string);
}

static int
audit_init_int(void)
{
	char *lib = "/usr/lib/libbsm.so.1";

	if (access(lib, F_OK) != 0)
		return (1);
	if ((g_dlp = dlopen(lib, RTLD_NOW)) == NULL)
		return (1);

	g_audit_int = &g_audit_interface;

	return (0);
}

JNIEXPORT	void	JNICALL
	Java_com_sun_wbem_cimom_CIMOMLibrary_setupAuditLogin(
					JNIEnv*	env,
					jclass	class_object,
					jstring host_name,
					jstring user_name)
{
	char *hostName;
	char *userName;
	char *ttyn;

	if ((g_audit_int == NULL) && (audit_init_int() != 0)) {
		return;
	}

	hostName = (char *)(*env)->GetStringUTFChars(env, host_name, NULL);
	userName = (char *)(*env)->GetStringUTFChars(env, user_name, NULL);

	g_audit_int->audit_login_save_host = (audit_login_save_host_fct)
				dlsym(g_dlp, "audit_login_save_host");

	if (g_audit_int->audit_login_save_host != NULL) {
		g_audit_int->audit_login_save_host(hostName);
	}

	ttyn = ttyname(0);
	if (ttyn == NULL)
	    ttyn = "/dev/???";

	g_audit_int->audit_login_save_ttyn = (audit_login_save_ttyn_fct)
				dlsym(g_dlp, "audit_login_save_ttyn");
	if (g_audit_int->audit_login_save_ttyn != NULL) {
		g_audit_int->audit_login_save_ttyn(ttyn);
	}

	g_audit_int->audit_login_save_port = (audit_login_save_port_fct)
			dlsym(g_dlp, "audit_login_save_port");
	if (g_audit_int->audit_login_save_port != NULL) {
		g_audit_int->audit_login_save_port();
	}

	g_audit_int->audit_login_save_pw = (audit_login_save_pw_fct)
			dlsym(g_dlp, "audit_login_save_pw");
	if (g_audit_int->audit_login_save_pw != NULL) {
	    struct  passwd *pwd;

	    pwd = getpwnam((const char *)userName);
	    g_audit_int->audit_login_save_pw(pwd);
	}

	(*env)->ReleaseStringUTFChars(env, host_name, hostName);
	(*env)->ReleaseStringUTFChars(env, user_name, userName);
}

JNIEXPORT	void	JNICALL
	Java_com_sun_wbem_cimom_CIMOMLibrary_auditLoginFailure(
					JNIEnv*	env,
					jclass	class_object,
					jstring client_host,
					jstring client_user,
					jint failure_code)
{

	char *clientUserName;
	char *clientHostName;

	if ((g_audit_int == NULL) && (audit_init_int() != 0)) {
		return;
	}

	clientUserName =
		(char *)(*env)->GetStringUTFChars(env, client_user, NULL);
	clientHostName =
		(char *)(*env)->GetStringUTFChars(env, client_host, NULL);


	g_audit_int->audit_admin_auth_fail =
		(audit_admin_auth_fail_fct)
				dlsym(g_dlp, "audit_admin_auth_fail");
	if (g_audit_int->audit_admin_auth_fail != NULL) {
		g_audit_int->audit_admin_auth_fail(clientUserName,
			clientHostName, failure_code);
	}

	(*env)->ReleaseStringUTFChars(env, client_user, clientUserName);
	(*env)->ReleaseStringUTFChars(env, client_host, clientHostName);
}

JNIEXPORT	void	JNICALL
	Java_com_sun_wbem_cimom_CIMOMLibrary_auditLoginSuccess(
					JNIEnv*	env,
					jclass	class_object,
					jstring client_host,
					jstring client_user,
					jlong session_id)
{
	char *clientUserName;
	char *clientHostName;

	if ((g_audit_int == NULL) && (audit_init_int() != 0)) {
		return;
	}

	clientUserName =
		(char *)(*env)->GetStringUTFChars(env, client_user, NULL);
	clientHostName =
		(char *)(*env)->GetStringUTFChars(env, client_host, NULL);


	g_audit_int->audit_admin_auth_success = (audit_admin_auth_success_fct)
				dlsym(g_dlp, "audit_admin_auth_success");
	if (g_audit_int->audit_admin_auth_success != NULL) {
		g_audit_int->audit_admin_auth_success(clientUserName,
			clientHostName, session_id);
	}

	(*env)->ReleaseStringUTFChars(env, client_user, clientUserName);
	(*env)->ReleaseStringUTFChars(env, client_host, clientHostName);
}

/*
 * Return JNI_TRUE if authenticated and JNI_FALSE if not.
 */
JNIEXPORT jboolean JNICALL
	Java_com_sun_wbem_cimom_CIMOMLibrary_doAuthenticate(
					JNIEnv *env,
					jclass class_object,
					jstring juser_or_role,
					jstring jpassword,
					jstring jroleuser)
{
	const char *user_or_role;
	const char *password;
	const char *roleuser;
	int result;

	user_or_role = (*env)->GetStringUTFChars(env, juser_or_role, NULL);
	password = (*env)->GetStringUTFChars(env, jpassword, NULL);
	if (jroleuser != NULL) {
	    roleuser = (*env)->GetStringUTFChars(env, jroleuser, NULL);
	} else {
	    roleuser = NULL;
	}

	result = cimomlib_authenticate(user_or_role, password, roleuser);

	(*env)->ReleaseStringUTFChars(env, juser_or_role, user_or_role);
	(*env)->ReleaseStringUTFChars(env, jpassword, password);
	if (roleuser != NULL) {
		(*env)->ReleaseStringUTFChars(env, jroleuser, roleuser);
	}

	return (result == 0 ? JNI_TRUE : JNI_FALSE);
}

/*
 * returns 0 if usertype is ok and 1 if not
 */
static int
check_usertype(const char *username, int utype)
{
	int ttype;
	char *tmp;
	userattr_t *attr_p;

	/*
	 * If we are looking for a specific user type, check the
	 * user_attr table for the user name.  If it exists, check
	 * its type.  If it does not exist or does not have an explicit
	 * type, it is a normal user.  If it exists and has a type,
	 * check that the type matches what was specified in the call.
	 * Always check for the normal user type last (its a negative
	 * check!).  Then check if the user is the specified type.
	 * If not, set the password hash to NULL.
	 */
	if (utype != ANY_USER_TYPE) {
	    ttype = NORMAL_USER;
	    attr_p = getusernam(username);
	    if (attr_p != NULL) {
		tmp = kva_match(attr_p->attr, USERATTR_USERTYPE_KEY);
		if (tmp != NULL) {
		    if (strncmp(tmp, USERATTR_ROLE, strlen(USERATTR_ROLE))
			    == 0) {
			ttype = ROLE_USER;
		    /* Check this last! */
		    } else {
			if (strncmp(tmp, USERATTR_USER,
				strlen(USERATTR_USER)) != 0) {
				ttype = -1;
			}
		    }
		}
		free_userattr(attr_p);
	    }
	    if (utype != ttype) {
		return (1);
	    }
	}
	return (0);
}
