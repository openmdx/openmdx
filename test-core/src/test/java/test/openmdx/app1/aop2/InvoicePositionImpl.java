/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ProductImpl 
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
package test.openmdx.app1.aop2;

import javax.jdo.listener.StoreCallback;

import org.openmdx.base.aop2.AbstractObject;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import test.openmdx.app1.jmi1.Invoice;
import test.openmdx.app1.jmi1.InvoicePosition;
import test.openmdx.app1.jmi1.Product;
import test.openmdx.app1.jmi1.ProductGroup;
import test.openmdx.app1.jmi1.Segment;

/**
 * ProductImpl
 */
public class InvoicePositionImpl 
    extends AbstractObject<test.openmdx.app1.jmi1.InvoicePosition,test.openmdx.app1.cci2.InvoicePosition,Void> 
    implements StoreCallback
{

    /**
     * Constructor 
     *
     * @param same the same layer JMI API
     * @param next the next layer CCI API
     */
    public InvoicePositionImpl(
        test.openmdx.app1.jmi1.InvoicePosition same,
        test.openmdx.app1.cci2.InvoicePosition next
    ) {
        super(same, next);
    }

    /**
     * @return
     * @see test.openmdx.app1.jmi1.InvoicePosition#getProduct()
     */
    public Product getProduct() {
        InvoicePosition same = sameObject();
        Invoice invoice = same.getInvoice();
        InvoicePosition sibling = sameManager().newInstance(InvoicePosition.class);
        if(!(sibling instanceof StoreCallback)){
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Missing interface: " + StoreCallback.class.getName()
            );
        }
        Segment segment = (Segment) invoice.refImmediateComposite();
        ProductGroup productGroup = segment.getProductGroup(
            invoice.getProductGroupId()
        ); 
        return productGroup.getProduct(
            same.getProductId()
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.AbstractObject#jdoPreStore()
     */
    @Override
    public void jdoPreStore() {
        super.jdoPreStore();
    }
        
}
