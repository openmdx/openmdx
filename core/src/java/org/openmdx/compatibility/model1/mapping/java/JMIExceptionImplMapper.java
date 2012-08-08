/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIExceptionImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIExceptionTemplate 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:39:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
 

package org.openmdx.compatibility.model1.mapping.java;

import java.io.Writer;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ExceptionDef;
import org.openmdx.model1.mapping.FeatureDef;
import org.openmdx.model1.mapping.MapperUtils;

@SuppressWarnings("unchecked")
public class JMIExceptionImplMapper
    extends JMIAbstractMapper {
    
    //-----------------------------------------------------------------------
    public JMIExceptionImplMapper(
        ModelElement_1_0 exceptionDef,
        Writer writer,
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
        this.exceptionDef = new ExceptionDef(exceptionDef, model, true);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIExceptionImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapImplException(
    ) throws ServiceException {
        this.trace("Exception/ImplException");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.exceptionDef.getQualifiedName(), 2))) + ";");
        this.pw.println();
        this.pw.println("public class " + this.exceptionDef.getName() );
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefException_1 {");
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext();) {
            AttributeDef param = (AttributeDef) i.next();
            this.mapImplGetFeatureUsingObjectType(
                "public",
                param
            );
        }
        this.pw.println();
        this.pw.println("private static org.openmdx.kernel.exception.BasicException.Parameter[] toProperties(");
        int ii = 0;
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext(); ii++) {
            AttributeDef param = (AttributeDef) i.next();
            String separator = ii == 0
                ? "      "
                : "      , ";
            this.mapParameter(
                separator,
                param
            );
        }
        this.pw.println(") {");
        this.pw.println("    return new org.openmdx.kernel.exception.BasicException.Parameter[]{");
        this.pw.println("        refNewProperty(\"typeName\", \"" + this.exceptionDef.getQualifiedName() + "\")");
        for(Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext();) {
            FeatureDef param = (FeatureDef) i.next();
            this.pw.println("      , refNewProperty(\"" + this.getParamName(param.getName()) + "\",");
            this.pw.println( this.getParamName(param.getName()) + ")");
        }
        this.pw.println("    };");
        this.pw.println("  }");
        this.pw.println();
        
        this.pw.println("  public " + this.exceptionDef.getName() + "(");
        this.pw.println("    org.openmdx.base.exception.ServiceException e");
        this.pw.println("  ) {");
        this.pw.println("    super(e);");
        this.pw.println("  }");
        this.pw.println();
        
        this.pw.println("  public " + this.exceptionDef.getName() + "(");
        ii = 0;
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext(); ii++) {
            AttributeDef param = (AttributeDef) i.next();
            String separator = ii == 0
                ? "      "
                : "      , ";
            this.mapParameter(
                separator,
                param
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    super(");
        this.pw.println("      toProperties(");
        ii = 0;
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext(); ii++) {
            AttributeDef param = (AttributeDef) i.next();
            if (ii > 0) {
                this.pw.println(", ");
            }
            this.pw.println( this.getParamName(param.getName()) );
        }
        this.pw.println("    )");
        this.pw.println("  );");
        this.pw.println("}");
        this.pw.println();
        
        this.pw.println("  public " + this.exceptionDef.getName() + "(");
        this.pw.println("      String domain");
        this.pw.println("    , int errorCode");
        this.pw.println("    , String description");
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext();) {
            AttributeDef param = (AttributeDef) i.next();
            this.mapParameter(
                "    , ",
                param
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    super(");
        this.pw.println("      domain,");
        this.pw.println("      errorCode,");
        this.pw.println("      description,");
        this.pw.println("      toProperties(");
        ii = 0;
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext(); ii++) {
            AttributeDef param = (AttributeDef) i.next();
            if (ii > 0) {
                this.pw.print(", ");
            }
            this.pw.println( this.getParamName(param.getName()) );
        }
        this.pw.println(")");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        
        this.pw.println("  public " + this.exceptionDef.getName() + "(");
        this.pw.println("      Exception e");
        this.pw.println("    , String domain");
        this.pw.println("    , int errorCode");
        this.pw.println("    , String description");
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext();) {
            AttributeDef param = (AttributeDef) i.next();
            this.mapParameter(
                "    , ",
                param
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    super(");
        this.pw.println("      e,");
        this.pw.println("      domain,");
        this.pw.println("      errorCode,");
        this.pw.println("      description,");
        this.pw.println("      toProperties(");
        ii = 0;
        for (Iterator i = this.exceptionDef.getParameters().iterator(); i.hasNext(); ii++) {
            AttributeDef param = (AttributeDef) i.next();
            if (ii > 0) {
                this.pw.print(", ");
            }
            this.pw.println( this.getParamName(param.getName()) );
        }
        this.pw.println(")");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("}");        
    }
    
    //-----------------------------------------------------------------------
    private final ExceptionDef exceptionDef;
    
}
