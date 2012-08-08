/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: XMIReferenceResolver.java,v 1.4 2005/06/16 02:01:08 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.4 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2005/06/16 02:01:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, CRIXP Corp., Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.model1.importer.xmi;

import org.openmdx.model1.uml1.UML1Comment;
import org.openmdx.model1.uml1.UML1Generalization;
import org.openmdx.model1.uml1.UML1TagDefinition;

public interface XMIReferenceResolver {

  //---------------------------------------------------------------------------
  public void parse(
      String uri
  ) throws Exception;
    
  /**
   * Retrieves the fully qualified name of the model element identified by a 
   * given xmiId
   * @param xmiId the xmi.id that identifies desired model element
   * @return the fully qualified name of the model element
   */
  public String lookupXMIId(
    String xmiId
  );

  /**
   * Retrieves an UMLGeneralization for a given xmiId
   * @param xmiId the xmi.id that identifies the UMLGeneralization
   * @return the UMLGeneralization for the given xmi.id
   */
  public UML1Generalization lookupGeneralization(
    String xmiId
  );
    
  /**
   * Retrieves an UMLComment for a given xmiId
   * @param xmiId the xmi.id that identifies the UMLComment
   * @return the UMLComment for the given xmi.id
   */
  public UML1Comment lookupComment(
    String xmiId
  );

  /**
   * Retrieves an UMLTagDefinition for a given xmiId
   * @param xmiId the xmi.id that identifies the UMLTagDefinition
   * @return the UMLTagDefinition for the given xmi.id
   */
  public UML1TagDefinition lookupTagDefinition(
    String xmiId
  );

  /**
   * Retrieves the project containing the given model package.
   * @param packageName model package name
   * @result model project name containing the given model package. null in
   *         case the model package is contained in the local model project.
   */
  public String lookupProject(
      String packageName
  );

  /**
   * Returns true if the parsing reported errors.
   */
  public boolean hasErrors();
  
}
