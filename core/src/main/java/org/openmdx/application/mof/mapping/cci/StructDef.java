/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: VelocityStructDef class
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.cci;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

@SuppressWarnings({"rawtypes","unchecked"})
public class StructDef
  extends ClassifierDef {

  //-------------------------------------------------------------------------
  public StructDef(
    ModelElement_1_0 structDef,
    Model_1_0 model
  ) throws ServiceException {
    this(
      structDef.getName(),
      structDef.getQualifiedName(),
      (String)structDef.objGetValue("annotation"),
      new HashSet(structDef.objGetList("stereotype")),
      getSupertypes(structDef, model),
      getAllFields(structDef, model)
    );
  }
  
  //-------------------------------------------------------------------------
  private static List getSupertypes(
    ModelElement_1_0 structDef,
    Model_1_0 model  
  ) throws ServiceException {
    List supertypes = new ArrayList();
    for (
      Iterator i = structDef.objGetList("supertype").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 supertype = model.getDereferencedType(i.next());
      supertypes.add(
        new StructDef(
          supertype.getName(),
          supertype.getQualifiedName(),
          (String)supertype.objGetValue("annotation"),
          new HashSet(supertype.objGetList("stereotype")),
          null, // do not determine supertypes for this supertype
          null // do not determine all fields
        )
      );
    }
    return supertypes;
  }

  //-------------------------------------------------------------------------
  private static List getAllFields(
    ModelElement_1_0 structDef,
    Model_1_0 model 
  ) throws ServiceException {  
    List fields = new ArrayList();
    for(
      Iterator it = structDef.objGetList("content").iterator();
      it.hasNext();
    ) {
      fields.add(
        new AttributeDef(
          model.getElement(it.next()),
          model 
        )
      );
    }
    return fields;
  }
  
  //-------------------------------------------------------------------------
    public StructDef(
    String name, 
    String qualifiedName,
    String annotation,
    Set stereotype,
    List supertypes,
    List fields
  ) {
        super(
      name, 
      qualifiedName, 
      annotation,
      stereotype,
      false,
      supertypes
    );
    this.fields = fields;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Retrieves all structure fields.
   */
  public List getFields(
  ) {
    return this.fields;  
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private final List fields;
}
