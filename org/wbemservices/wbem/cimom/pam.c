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

#include <stdio.h>
#include <string.h>
#include <security/pam_appl.h>
#include <unistd.h>
#include <locale.h>

/*
 * For application data, like password. Passes through the
 * pam framework to the conversation function.
 */
static struct app_pam_data {
	const char *password;
};

/* pam conversation function */
static int login_conv(int num_msg, struct pam_message **msgm,
	struct pam_response **response, void *appdata_ptr);

/*
 * Return 0 if authenticated, 1 if not and sets the auth_error_msg
 * to the pam error string from pam_strerror, could be NULL, even
 * on error.
 */
int
cimomlib_authenticate(const char *user_or_role, const char *password,
	const char *roleuser)
{
	int status;
	pam_handle_t *pamh;		/* pam handle */
	struct app_pam_data app_data;	/* pam application data */
	struct pam_conv pamconv;	/* pam init structure */

	pamh = NULL;

	/*
	 * app_data gets passed through the framework
	 * to "login_conv". Set the password for use in login_conv
	 * and set up the pam_conv data with the conversation
	 * function and the application data pointer.
	 */
	app_data.password = password;
	pamconv.conv = login_conv;
	pamconv.appdata_ptr = &app_data;

	/* pam start session */
	status = pam_start("cimomlib_authenticate",
		user_or_role, &pamconv, &pamh);

	/* set primary user name for role assumption */
	if (status == PAM_SUCCESS && roleuser != NULL) {
		status = pam_set_item(pamh, PAM_RUSER, roleuser);
	}

	/* authenticate user */
	if (status == PAM_SUCCESS) {
		status = pam_authenticate(pamh,
			PAM_SILENT|PAM_DISALLOW_NULL_AUTHTOK);
	}

	/* check if the authenicated user is allowed to use machine */
	if (status == PAM_SUCCESS) {
		status = pam_acct_mgmt(pamh,
			PAM_SILENT|PAM_DISALLOW_NULL_AUTHTOK);
	}

#if 0
	/* get the error message if debugging */
	if (status != PAM_SUCCESS) {
		const char *errstr = pam_strerror(pamh, status);
		printf("cimom_authenticate : (%d) %s\n", status, errstr);
	}
#endif

	/* end pam session */
	if (pamh != NULL) {
	    pam_end(pamh, status == PAM_SUCCESS ? PAM_SUCCESS : PAM_ABORT);
	}

	return (status == PAM_SUCCESS ? 0 : 1);
}

/*
 * login_conv:
 * this is the conversation function called from a PAM authentication module
 * to print erro messagae or get user information
 */
static int
login_conv(int num_msg, struct pam_message **msgm,
	struct pam_response **response, void *appdata_ptr)
{
	int count = 0;
	int reply_used = 0;
	struct pam_response *reply;

	if (num_msg <= 0) {
		return (PAM_CONV_ERR);
	}

	reply = (struct pam_response *)calloc(num_msg,
		sizeof (struct pam_response));
	if (reply == NULL) {
		return (PAM_BUF_ERR);
	}

	for (count = 0; count < num_msg; ++count) {
	    switch (msgm[count]->msg_style) {
	    /* Return the password */
	    case PAM_PROMPT_ECHO_OFF:
		/* Password should never be NULL */
		if (((struct app_pam_data *)appdata_ptr)->password != NULL) {
		    reply[count].resp = strdup(((struct app_pam_data *)
			appdata_ptr)->password);
		    if (reply[count].resp == NULL) {
			/*
			 * It may be the case that some modules won't free
			 * the pam_response memory if the return is not
			 * PAM_SUCCESS. We should not have had
			 * multiple PAM_PROMPT_ECHO_OFF in a single
			 * login_conv call but just in case see if
			 * reply was modified anyway.
			 */
			if (reply_used) {
			    int i;
			    for (i = 0; i < num_msg; ++i) {
				if (reply[i].resp != NULL) {
				    free(reply[i].resp);
				}
			    }
			}
			free(reply);
			*response = NULL;
			return (PAM_BUF_ERR);
		    }
		}
		/*
		 * Always set reply_used even if the password is NULL
		 * A NULL response cannot be returned with PAM_SUCCESS.
		 * Could return PAN_CONV_ERR but let later code determine
		 * the effect of a NULL password.
		 */
		reply_used = 1;
		break;
	    /* For empty user name, shouldn't happen */
	    case PAM_PROMPT_ECHO_ON:
		/*
		 * It may be the case that some modules won't free
		 * the pam_response memory if the return is not
		 * PAM_SUCCESS. We should not have had
		 * multiple PAM_PROMPT_ECHO_OFF in a single
		 * login_conv call but just in case see if
		 * reply was modified anyway.
		 */
		if (reply_used) {
		    int i;
		    for (i = 0; i < num_msg; ++i) {
			if (reply[i].resp != NULL) {
			    free(reply[i].resp);
			}
		    }
		}
		free(reply);
		*response = NULL;
		/*
		 * Have to return error here since PAM can loop
		 * when no username is provided
		 */
		return (PAM_CONV_ERR);
		break;
	    case PAM_ERROR_MSG:
	    case PAM_TEXT_INFO:
		break;
	    default:
		break;
	    }
	}

	/*
	 * The response may not always be freed by
	 * modules for PAM_ERROR_MSG, and PAM_TEXT_INFO
	 * So make sure it is freed if we didn't use it.
	 */
	if (!reply_used) {
	    free(reply);
	    reply = NULL;
	}
	*response = reply;

	return (PAM_SUCCESS);
}
