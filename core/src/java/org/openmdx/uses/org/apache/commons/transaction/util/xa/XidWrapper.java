/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/util/xa/XidWrapper.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.util.xa;

import javax.transaction.xa.Xid;
import java.lang.Object;
//import java.util.Arrays;
import java.lang.String;

/**
 * Wraps an <code>Xid</code> to guarantee methods for equality and hashcode are
 * implemented correctly. This is escpecially necessary when the <code>Xid</code> is used as a key in a <code>HashMap</code>.
 *   
 * @version $Revision: 1.1 $
 * 
 */
public class XidWrapper implements Xid {

    public static final Xid wrap(Xid xid) {
        return wrap(xid, false); // for Slide branch qualifier must not be included in onePhase commit
    }

    public static final Xid wrap(Xid xid, boolean includeBranch) {
        return (xid instanceof XidWrapper ? xid : new XidWrapper(xid, includeBranch));
    }

    private final Xid xid;
    private final String asString;
    private final int hashCode;

    private XidWrapper(Xid xid, boolean includeBranch) {
        this.xid = xid;
        // do calculations once for performance
        StringBuffer b = new StringBuffer(64);
        b.append(new String(xid.getGlobalTransactionId()));
        if (includeBranch) {
            b.append("-").append(new String(xid.getBranchQualifier()));
        }

        asString = b.toString();
        hashCode = asString.hashCode();
    }

    public Xid getXid() {
        return xid;
    }

    public int getFormatId() {
        return xid.getFormatId();
    }

    public byte[] getGlobalTransactionId() {
        return xid.getGlobalTransactionId();
    }

    public byte[] getBranchQualifier() {
        return xid.getBranchQualifier();
    }

    public boolean equals(Object o) {
        return (o != null && asString.equals(o.toString()));
        /*
         if (this == o) {
             return true;
         }
        
         if (o != null && o instanceof Xid) {
             Xid xid2 = (Xid) o;
             // we do not need equality of format Id 
             return (
                 Arrays.equals(xid.getGlobalTransactionId(), xid2.getGlobalTransactionId())
                     && Arrays.equals(xid.getBranchQualifier(), xid2.getBranchQualifier()));
         }
        
         return false;
         */
    }

    public String toString() {
        return asString;
    }

    public int hashCode() {
        return hashCode;
    }
}
