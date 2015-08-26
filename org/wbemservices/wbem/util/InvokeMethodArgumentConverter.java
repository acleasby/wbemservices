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

package org.wbemservices.wbem.util;

import java.util.*;

import javax.wbem.cim.*;
import javax.wbem.client.CIMOMHandle;

/** Has helper methods which convert javax.wbem.cim.CIMValue to CIMArgument
 * Used to facilitate the use of providers which take a vector of CIMValues
 * in their invokeMethod call, but must actually return an array of
 * CIMArgument (e.g. CIMMethodProvider -> MethodProvider)
 * @author  jmars@east.sun.com
 * @version 02/01/30
 */
public class InvokeMethodArgumentConverter {
    /** The output parameter qualifier */
    private static final String OUTPARAM = "out";

    /** Converts an array of CIMArguments to a vector of CIMValues
     * @param pArgs the array of CIMArguments to convert
     * @return Vector A vector of CIMValues
     */
    public static final Vector convertArgumentToValue(CIMArgument[] pArgs) {
        Vector ret = new Vector(pArgs.length);
        if (pArgs != null && pArgs.length > 0) {
            for (int x = 0; x < pArgs.length; x++) {
                CIMValue val = pArgs[x].getValue();
                ret.add(val);
            }
        }
        return ret;
    }

    /** Converts a Vector of CIMValues to an array of CIMArgument
     * This function converts a vector of javax.wbem.cim.CIMValue to
     * an array of CIMArgument. <I>It is assumed that the CIMValues are in the
     * Vector in teh appropriate order, meaning that the order matches the
     * argument parameters specified in teh CIMClasses MOF file</I>
     * @param pCimom Handle to the CIMOM
     * @param pOP The objectpath for the CIMClass being used
     * @param pMethodName the name of the method being called
     * @param pValues The vector of CIMValues to be converted
     * @param outArray Contains the converted array
     * @throws CIMException if the returned Vector has less then, or more then,
     * the number of elements the function is suppose to return (defined in
     * the CIMClass). Will also throw CIMException if calls into the CIMOM fail
     */
    public static final void convertValue2Argument(CIMOMHandle pCimom,
    CIMObjectPath pOP, String pMethodName, Vector pValues,
    CIMArgument[] outArray) throws CIMException {
        // get the class
        CIMClass clz = pCimom.getClass(pOP, false, true, true, null);
        // find the method in the class
        CIMMethod mthd = clz.getMethod(pMethodName);
        // get the parameters
        Vector paramsVec = mthd.getParameters();
        // convert the parameter vector to an array
        CIMParameter[] params = getOutputParams(paramsVec.iterator());
        // make sure we have the correct # of params
        if (pValues.size() != params.length) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
                "The method (" + pMethodName + ") returned the wrong number of output parmeters. " +
                "Expected:" + params.length + ", got:" + pValues.size());
        }
        // convert the output vector to an array
        CIMValue[] vals = new CIMValue[pValues.size()];
        vals = (CIMValue[])pValues.toArray(vals);
        convertParameter2Argument(params, vals, outArray);
    }

    /** Takes a List of CIMParameters and returns an array of CIMParmeters
     * which are output parameters, e.g. input only params will be striped
     * @param pList an Iterator representing the CIMParmeters for a method
     * @return CIMParameter[] An array of output parameters
     */
    private static final CIMParameter[] getOutputParams(Iterator pList) {
        CIMParameter[] ret = new CIMParameter[0];
        ArrayList outParams = new ArrayList();
        while (pList.hasNext()) {
            CIMParameter cur = (CIMParameter)pList.next();
            if (cur.getQualifier(OUTPARAM) != null) {
                outParams.add(cur);
            }
        }
        ret = (CIMParameter[])outParams.toArray(ret);
        return ret;
    }

    /** Combines the passed in values and parameters into a CIMArgument
     * This function combines the passed in parameters with the passed in arrays
     * creating a CIMArgument. <I>It is assumed that the parameters and values
     * are in the correct order.</I>
     * @param pParams An array of the output parameters
     * @param pVals An array of the actual values returned from teh invoked method
     * @param outArray The out argument array
     */
    private static final void convertParameter2Argument(CIMParameter[] pParams, CIMValue[] pVals, CIMArgument[] outArray) {
        int num = pVals.length;
        for (int x = 0; x < num; x++) {
            outArray[x] = new CIMArgument(pParams[x].getName(), pVals[x]);
            // set the qualifiers
            outArray[x].setQualifiers(pParams[x].getQualifiers());
        }
    }
}
