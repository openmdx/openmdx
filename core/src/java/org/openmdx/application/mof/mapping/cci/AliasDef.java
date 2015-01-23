//////////////////////////////////////////////////////////////////////////////
//
//Name: $Id: AliasDef.java,v 1.2 2009/01/13 17:34:04 wfro Exp $
//Description: VelocityAliasDef.java
//Revision: $Revision: 1.2 $
//Author: $Author: wfro $ 
//Date: $Date: 2009/01/13 17:34:04 $
//Copyright: (c) 2003-2006 OMEX AG
//
//////////////////////////////////////////////////////////////////////////////
package org.openmdx.application.mof.mapping.cci;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

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
    this.qualifiedTypeName = model.getElement(
        aliasDef.getType()
    ).getQualifiedName();
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
