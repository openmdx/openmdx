//////////////////////////////////////////////////////////////////////////////
//
//Name: $Id: AliasDef.java,v 1.2 2006/02/19 21:51:06 wfro Exp $
//Description: VelocityAliasDef.java
//Revision: $Revision: 1.2 $
//Author: $Author: wfro $ 
//Date: $Date: 2006/02/19 21:51:06 $
//Copyright: © 2003-2006 OMEX AG
//
//////////////////////////////////////////////////////////////////////////////
package org.openmdx.model1.mapping;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

public class AliasDef extends ClassDef {

  //-------------------------------------------------------------------------
  public AliasDef(
    ModelElement_1_0 aliasDef,
    Model_1_0 model
  ) throws ServiceException {
    super(
      aliasDef,
      model
    );
    this.qualifiedTypeName = (String)model.getElement(
      aliasDef.values("type").get(0)
    ).values("qualifiedName").get(0);
  }
  
  public String getQualifiedTypeName(
  ) {
    return this.qualifiedTypeName;
  }
  
  //-------------------------------------------------------------------------
  // Members
  //-------------------------------------------------------------------------
  private final String qualifiedTypeName;
  
}
