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
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package javax.wbem.client;

import javax.wbem.cim.CIMObjectPath;

public class CIMEnumInstancesOp extends CIMEnumElementOp 
	implements EnumerableOp {

    private boolean deep = false;
    private boolean localOnly = true;
    private boolean includeQualifiers = false;
    private boolean includeClassOrigin = false;
    private String[] propertyList = null;

    private final static long serialVersionUID = 3030617713419750858L;

    public CIMEnumInstancesOp(CIMObjectPath name, 
			boolean deep) {

	super(name);
	this.deep = deep;
    }

    public CIMEnumInstancesOp(CIMObjectPath name, 
			boolean deep,
			boolean localOnly) {
	this(name, deep);
	this.localOnly = localOnly;
    }

    public CIMEnumInstancesOp(CIMObjectPath name, 
				boolean deep,
				boolean localOnly,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) {
	this(name, deep);
	this.localOnly = localOnly;
	this.includeQualifiers = includeQualifiers;
	this.includeClassOrigin = includeClassOrigin;
	this.propertyList = propertyList;
    }

    public boolean isDeep() {
	return deep;
    }

    public boolean isLocalOnly() {
	return localOnly;
    }

    public boolean isClassOriginIncluded() {
	return includeClassOrigin;
    }

    public boolean isQualifiersIncluded() {
	return includeQualifiers;
    }

    public String[] getPropertyList() {
	return propertyList;
    }
}
