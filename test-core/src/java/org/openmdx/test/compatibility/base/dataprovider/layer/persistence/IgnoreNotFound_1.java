//////////////////////////////////////////////////////////////////////////////
//
// Name:        $Id: IgnoreNotFound_1.java,v 1.2 2008/10/07 08:52:28 hburger Exp $
// Description: Type layer Ignore_1 plugin
// Revision:    $Revision: 1.2 $
// Author:      $Author: hburger $
// Date:        $Date: 2008/10/07 08:52:28 $
// Copyright:   (c) 2000-2003 OMEX AG
//
//////////////////////////////////////////////////////////////////////////////
package org.openmdx.test.compatibility.base.dataprovider.layer.persistence;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * This implementation ignores the metamodel.
 */
public class IgnoreNotFound_1
  extends Layer_1 {

    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        try {
            return super.find(header,request);
        } catch(ServiceException exception) {
            if (exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                final DataproviderReply reply = new DataproviderReply();
                reply.context(DataproviderReplyContexts.TOTAL).set(0, new Integer(0));
                reply.context(DataproviderReplyContexts.HAS_MORE).set(0,Boolean.FALSE);
                  SysLog.trace("Ignoring NOT_FOUND exception");
                return reply;
            }
            throw exception;
        }
    }

}
