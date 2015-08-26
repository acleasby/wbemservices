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
 *are Copyright © 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package org.wbemservices.wbem.cimom;



import javax.wbem.cim.CIMException;

/*
 * This class implements multiple readers and writers algorithm, with some
 * modifications required for the CIMOM. It takes care of nested reads
 * and writes.
 */
class ReadersWriters {

    private String name;
    private int turn=0;
    private int currentTurn=0;
    private int numReaders=0;
    private int numWriters=0;
    private ThreadLocal tl = new ThreadLocal();
    private int count=0;

    private class RWEntry {
	int count=0;
	int    numLocks=1;
	boolean writer=false;
    }

    private boolean checkThreadStart(boolean writer) throws CIMException {
	RWEntry rwe = (RWEntry)tl.get();
	if(rwe != null) {
	    if(writer && !rwe.writer) {
		throw new CIMException(CIMException.CIM_ERR_FAILED,"Reader cant become writer");
	    }
	    rwe.numLocks++;
	    return true;

	} else {
	    rwe = new RWEntry();
	    rwe.writer=writer;
	    rwe.count=count;
	    count++;
	    tl.set(rwe);
	    return false;
	}

    }

    private boolean checkThreadStop() throws CIMException {
	RWEntry rwe = (RWEntry)tl.get();
	if(rwe != null) {
	    rwe.numLocks--;
	    if(rwe.numLocks == 0) {
		tl.set(null);
		return false;
	    } else {
		return true;
	    }
	} else {
	    throw new CIMException(CIMException.CIM_ERR_FAILED,"Thread not active!");
	}

    }

    public ReadersWriters(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public synchronized void startRead() throws CIMException {
	if(checkThreadStart(false)) { return;
        }
	synchronized(this) {
	    int myturn=turn;
	    turn++;
	    while(myturn != currentTurn) {
		try {
		    wait();
		} catch (InterruptedException e) {
		}
	    }
	    currentTurn++;
	    // wake up any readers if present
	    notifyAll();
	    numReaders++;
	}
    }

    public void startWrite() throws CIMException {
	if(checkThreadStart(true)) { return;
        }
	synchronized(this) {
	    int myturn=turn;
	    turn++;
	    while((myturn != currentTurn) || (numReaders != 0)) {
		try {
		    wait();
		} catch (InterruptedException e) {
		}
	    }
	    numWriters++;
	    // We will increment the current turn only when we finish writing.
	}
    }

    public void endRead() throws CIMException {
	if(checkThreadStop()) { return;
        }
	synchronized(this) {
	    numReaders--;
	    if(numReaders == 0) { notifyAll();
            }
	}
    }

    public void endWrite() throws CIMException {
	if(checkThreadStop()) { return;
        }
	synchronized(this) {
	    numWriters--;
	    currentTurn++;
	    notifyAll();
	}
    }
}
