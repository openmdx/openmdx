//////////////////////////////////////////////////////////////////////////////
//
// Name:        $Id: IgnoreNotFound_1.java,v 1.3 2009/01/07 02:44:15 hburger Exp $
// Description: Type layer Ignore_1 plugin
// Revision:    $Revision: 1.3 $
// Author:      $Author: hburger $
// Date:        $Date: 2009/01/07 02:44:15 $
// Copyright:   (c) 2000-2003 OMEX AG
//
//////////////////////////////////////////////////////////////////////////////
package org.openmdx.test.compatibility.base.dataprovider.layer.persistence;

import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
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
