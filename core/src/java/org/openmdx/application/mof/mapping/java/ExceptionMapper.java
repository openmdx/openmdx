/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Exception Template 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2011, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.java;

import java.io.Writer;
import java.util.List;

import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ExceptionDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;

public class ExceptionMapper extends AbstractMapper {
    
    /**
     * Constructor 
     */
    public ExceptionMapper(
        ModelElement_1_0 exceptionDef,
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix, 
        MetaData_1_0 metaData, 
        PrimitiveTypeMapper primitiveTypeMapper
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix,
            metaData, 
            primitiveTypeMapper
        );
        this.exceptionDef = new ExceptionDef(exceptionDef, model);
    }
    
    //-----------------------------------------------------------------------
    public void mapException(
    ) throws ServiceException {
        //
        // Prolog
        //
        this.trace("Exception/Exception");
        this.fileHeader();
        List<String> namespacePrefix = MapperUtils.getNameComponents(MapperUtils.getPackageName(this.exceptionDef.getQualifiedName(), 2));
        List<AttributeDef> attributes = this.exceptionDef.getParameters();
        this.pw.println("package " + this.getNamespace(namespacePrefix) + ";");
        this.pw.println();
        this.pw.println("public class " + this.exceptionDef.getName() );
        this.pw.print("  extends ");
        if(this.getFormat() == Format.JMI1) {
            this.pw.print(AbstractMapper.getNamespace(namespacePrefix,Names.CCI2_PACKAGE_SUFFIX) + '.' + this.exceptionDef.getName());
        } else {
            this.pw.println("javax.jmi.reflect.RefException");
        }
        this.pw.println('{');
        //
        // constructor
        //
        boolean hasMessage = false;
        this.pw.println();
        this.pw.println("  public " + this.exceptionDef.getName() + "(");
        String separator = "      "; 
        for (AttributeDef attributeDef : attributes){
            this.mapParameter(
                separator,
                attributeDef, 
                ""
            );
            hasMessage |= "message".equals(attributeDef.getName());
            separator = "    , ";
        }
        this.pw.println("  ) {");
        switch(this.getFormat()) {
            case CCI2:
                if(hasMessage) {
                    this.pw.println("    super(message);");
                } else {
                    this.pw.println("    super();");
                }
                for (AttributeDef attributeDef : attributes){
                    if(!"message".equals(attributeDef.getName())){
                        this.mapInitializeMember(attributeDef);
                    }
                }
                this.pw.println("  }");
                //
                // for each parameter
                //
                this.pw.println();
                for (AttributeDef attributeDef : attributes){
                    if(!"message".equals(attributeDef.getName())){
                        this.mapParameter(
                            "  private final ", 
                            attributeDef, 
                            ";"
                        );
                    }
                }
                //
                // for each parameter
                //
                for (AttributeDef attributeDef : attributes) {
                    if(!"message".equals(attributeDef.getName())){
                        this.mapGetMember(
                            "public",
                            attributeDef
                        );
                    }
                }
                break;
            case JMI1:
                this.pw.println("    this(");
                this.pw.println("      org.openmdx.kernel.exception.BasicException.Code.DEFAULT_DOMAIN");
                this.pw.println("    , org.openmdx.kernel.exception.BasicException.Code.PROCESSING_FAILURE");
                this.pw.println("    , null");
                for (AttributeDef attributeDef : attributes){
                    this.pw.println("    , " + this.getFeatureName(attributeDef));    
                }
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public " + this.exceptionDef.getName() + "(");
                this.pw.println("    java.lang.String domain");
                this.pw.println("  , int errorCode");
                this.pw.println("  , java.lang.String description");
                for (AttributeDef attributeDef : attributes) {
                    this.mapParameter(
                        "    , ",
                        attributeDef, 
                        ""
                    );
                }
                this.pw.println("  ) {");
                this.pw.println("    this(");
                this.pw.println("      null");
                this.pw.println("    , domain");
                this.pw.println("    , errorCode");
                this.pw.println("    , description");
                for (AttributeDef attributeDef : attributes) {
                    this.pw.println("    , " + this.getFeatureName(attributeDef));    
                }
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public " + this.exceptionDef.getName() + "(");
                this.pw.println("    java.lang.Exception e");
                this.pw.println("  , java.lang.String domain");
                this.pw.println("  , int errorCode");
                this.pw.println("  , java.lang.String description");
                for (AttributeDef attributeDef : attributes) {
                    this.mapParameter(
                        "  , ",
                        attributeDef, 
                        ""
                    );
                }
                this.pw.println("  ) {");
                this.pw.println("    super(");
                separator = "      ";
                for (AttributeDef attributeDef : attributes) {
                    this.pw.println(separator + this.getFeatureName(attributeDef));    
                    separator = "    , ";
                }
                this.pw.println("    );");
                this.pw.println("    initCause(");
                this.pw.println("      new org.openmdx.kernel.exception.BasicException(");
                this.pw.println("        e");
                this.pw.println("      , domain");
                this.pw.println("      , errorCode");
                this.pw.println("      , new org.openmdx.kernel.exception.BasicException.Parameter[]{");
                this.pw.println("          new org.openmdx.kernel.exception.BasicException.Parameter(\"class\",\"" + exceptionDef.getQualifiedName() + "\")");
                for (AttributeDef attributeDef : attributes) {
                    this.pw.println("        , new org.openmdx.kernel.exception.BasicException.Parameter(\"" + attributeDef.getName() + "\"," + getFeatureName(attributeDef) + ")");
                }
                this.pw.println("        }");
                this.pw.println("      , description == null ? \"" +  exceptionDef.getQualifiedName() + "\" : description");
                this.pw.println("      , this");
                this.pw.println("      )");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Note:<br>");
                this.pw.println("   * <em>This method must be called with contract compliant arguments only!</em>");
                this.pw.println("   *");
                this.pw.println("   * @param contractCompliantServiceException with a cause providing at least<ul>");
                this.pw.println("   *   <li>a <em>non-null</em> description");
                this.pw.println("   *   <li>a \"class\" parameter with the qualified exception name");
                this.pw.println("   *   <li>one parameter for each modelled exception feature");
                this.pw.println("   * </ul>");
                this.pw.println("   */");
                this.pw.println("  public " + this.exceptionDef.getName() + "(");
                this.pw.println("    org.openmdx.base.exception.ServiceException contractCompliantServiceException");
                this.pw.println("  ) {");
                this.pw.println("    this(");
                this.pw.println("      (org.openmdx.kernel.exception.BasicException)contractCompliantServiceException.getCause()");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  private " + this.exceptionDef.getName() + "(");
                this.pw.println("    org.openmdx.kernel.exception.BasicException e");
                this.pw.println("  ) {");
                this.pw.println("    this(");
                this.pw.println("      e");
                if(attributes.isEmpty()) {
                    this.pw.println("    , (java.lang.String[])null");    
                } else {
                    for (AttributeDef attributeDef : attributes) {
                        this.pw.println("    , e.getParameter(\"" + attributeDef.getName() + "\")");    
                    }
                }
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  private " + this.exceptionDef.getName() + "(");
                this.pw.println("      org.openmdx.kernel.exception.BasicException e");
                this.pw.println("    , java.lang.String... arguments");
                this.pw.println("  ) {");
                this.pw.println("    super(");
                separator = "      ";
                int i = 0;
                for (AttributeDef attributeDef : attributes) {
                    this.pw.println(
                        separator + this.getParseExpression(
                            attributeDef.getQualifiedTypeName(),
                            ModelHelper.toMultiplicity(attributeDef.getMultiplicity()),
                            "arguments[" + i++ + "]"
                        )
                    );
                    separator = "    , ";
                }
                this.pw.println("    );");
                this.pw.println("    initCause(e);");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  @Override");
                this.pw.println("  public java.lang.String getMessage(){");
                this.pw.println("    org.openmdx.kernel.exception.BasicException cause = (org.openmdx.kernel.exception.BasicException) getCause();");
                this.pw.println("    return cause == null ? null : (cause.getMessage() + \": \" + cause.getDescription());");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  @Override");
                this.pw.println("  public java.lang.String toString(){");
                this.pw.println("    java.lang.Throwable cause = getCause();");
                this.pw.println("    return cause == null ?  super.toString() : cause.toString();");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  @Override");
                this.pw.println("  public void printStackTrace(java.io.PrintStream s) {");
                this.pw.println("    java.lang.Throwable cause = getCause();");
                this.pw.println("    if(cause == null){");
                this.pw.println("      super.printStackTrace(s);");
                this.pw.println("    } else {");
                this.pw.println("      cause.printStackTrace(s);");
                this.pw.println("    }");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  @Override");
                this.pw.println("  public void printStackTrace(java.io.PrintWriter s) {");
                this.pw.println("    java.lang.Throwable cause = getCause();");
                this.pw.println("    if(cause == null){");
                this.pw.println("      super.printStackTrace(s);");
                this.pw.println("    } else {");
                this.pw.println("      cause.printStackTrace(s);");
                this.pw.println("    }");
                this.pw.println("  }");
                break;
            default:
                break;
        }
        this.pw.println();
        //
        // Epilog
        //
        this.pw.println();
        this.pw.println("}");        
    }
    
    //-----------------------------------------------------------------------
    private final ExceptionDef exceptionDef;
    
}
