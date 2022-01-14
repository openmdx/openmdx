/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: State plug-in
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.state2.aopm;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;

import org.openmdx.base.dataprovider.layer.spi.AbstractLayer;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.state2.spi.TechnicalAttributes;

/**
 * State plug-in
 */
public class State_2 extends AbstractLayer {

    /**
     * Constructor
     */
    public State_2(
    ) {
        super(
            false, // incomingInterceptionEnabled
            true // outgoingInterceptionEnabled
        );
    }

    /**
     * Tells whether the transaction time is unique for object hosted by this
     * provider.
     */
    private boolean transactionTimeUnique;

    /**
     * Tells whether the transaction time is unique for object hosted by this
     * provider.
     *
     * @return <code>true</code> if the transaction time is unique for object
     *         hosted by this provider.
     */
    public boolean isTransactionTimeUnique() {
        return this.transactionTimeUnique;
    }

    /**
     * Defines whether the transaction time is unique for objects hosted by this
     * provider.
     * 
     * @param transactionTimeUnique
     *            <code>true</code> if the transaction time
     *            shall be unique for for objects hosted by this provider
     */
    public void setTransactionTimeUnique(
        boolean transactionTimeUnique
    ) {
        this.transactionTimeUnique = transactionTimeUnique;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.dataprovider.layer.spi.AbstractLayer#
     * interceptOutgoingObject(org.openmdx.base.resource.cci.RestFunction,
     * org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    protected void interceptOutgoingObject(
        RestFunction method,
        ObjectRecord object
    )
        throws ResourceException {
        if (isInstanceOf(object, "org:openmdx:state2:StateCapable")) {
            amendStateCapable(object);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.dataprovider.layer.spi.AbstractLayer#
     * interceptIncomingObject(org.openmdx.base.resource.cci.RestFunction,
     * org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    protected final void interceptIncomingObject(
        RestFunction method,
        ObjectRecord object
    )
        throws ResourceException {
        throw new NotSupportedException("interception of incoming objects is disabled");
    }

    @SuppressWarnings("unchecked")
    protected void amendStateCapable(
        ObjectRecord stateCapable
    )
        throws ResourceException {
        stateCapable.getValue().put(TechnicalAttributes.TRANSACTION_TIME_UNIQUE, Boolean.valueOf(transactionTimeUnique));
    }

}
