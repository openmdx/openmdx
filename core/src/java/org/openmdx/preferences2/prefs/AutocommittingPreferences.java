/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AutocommittingPreferences 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.preferences2.prefs;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import javax.jdo.Transaction;

import org.openmdx.preferences2.jmi1.Node;


/**
 * AutocommittingPreferences
 *
 */
public class AutocommittingPreferences extends ManagedPreferences {

    /**
     * Constructor 
     *
     * @param node
     */
    AutocommittingPreferences(
        Node node
    ) {
        super(node);
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param node
     */
    protected AutocommittingPreferences(
        AutocommittingPreferences parent, 
        Node node
    ) {
        super(parent, node);
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#putSpi(java.lang.String, java.lang.String)
     */
    @Override
    protected void putSpi(String key, String value) {
        Transaction currentTransaction = jmiEntityManager().currentTransaction();
        currentTransaction.begin();
        super.putSpi(key, value);
        currentTransaction.commit();
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#removeSpi(java.lang.String)
     */
    @Override
    protected void removeSpi(String key) {
        Transaction currentTransaction = jmiEntityManager().currentTransaction();
        currentTransaction.begin();
        super.removeSpi(key);
        currentTransaction.commit();
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#removeNodeSpi()
     */
    @Override
    protected void removeNodeSpi(
    ) throws BackingStoreException {
        try {
            Transaction currentTransaction = jmiEntityManager().currentTransaction();
            currentTransaction.begin();
            super.removeNodeSpi();
            currentTransaction.commit();
        } catch (RuntimeException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#childSpi(java.lang.String)
     */
    @Override
    protected AbstractPreferences childSpi(
        String name
    ) {
        return new AutocommittingPreferences(this, getChildNode(name));
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#newNode(java.lang.String)
     */
    @Override
    protected Node newChildNode(
        String name
     ) {
        Transaction currentTransaction = jmiEntityManager().currentTransaction();
        currentTransaction.begin();
        Node child = super.newChildNode(name);
        currentTransaction.commit();
        return child;
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.StandardPreferences#flush()
     */
    @Override
    public void flush(
    ) throws BackingStoreException {
        // Nothing to do
    }

}
