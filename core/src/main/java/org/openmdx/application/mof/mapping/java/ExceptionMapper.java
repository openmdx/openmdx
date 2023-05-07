/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Exception Template 
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
     * @param markdown TODO
     */
    public ExceptionMapper(
        ModelElement_1_0 exceptionDef,
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix, 
        MetaData_1_0 metaData, 
        boolean markdown, PrimitiveTypeMapper primitiveTypeMapper
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix,
            metaData, 
            markdown,
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
        List<String> namespacePrefix = MapperUtils.getNameComponents(MapperUtils.getPackageName(this.exceptionDef.getQualifiedName(), 2));
        List<AttributeDef> attributes = this.exceptionDef.getParameters();
        printLine("package ", this.getNamespace(namespacePrefix), ";");
        newLine();
        this.mapGeneratedAnnotation();
        printLine("@SuppressWarnings(\"serial\")");
        printLine("public class ", this.exceptionDef.getName() );
        print("  extends ");
        if(this.getFormat() == Format.JMI1) {
            print(AbstractMapper.getNamespace(namespacePrefix,Names.CCI2_PACKAGE_SUFFIX) + '.' + this.exceptionDef.getName());
        } else {
            printLine("javax.jmi.reflect.RefException");
        }
        printLine("{");
        //
        // constructor
        //
        boolean hasMessage = false;
        newLine();
        printLine("  public ", this.exceptionDef.getName(), "(");
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
        printLine("  ) {");
        switch(this.getFormat()) {
            case CCI2:
                if(hasMessage) {
                    printLine("    super(message);");
                } else {
                    printLine("    super();");
                }
                for (AttributeDef attributeDef : attributes){
                    if(!"message".equals(attributeDef.getName())){
                        this.mapInitializeMember(attributeDef);
                    }
                }
                printLine("  }");
                //
                // for each parameter
                //
                newLine();
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
                printLine("    this(");
                printLine("      org.openmdx.kernel.exception.BasicException.Code.DEFAULT_DOMAIN");
                printLine("    , org.openmdx.kernel.exception.BasicException.Code.PROCESSING_FAILURE");
                printLine("    , null");
                for (AttributeDef attributeDef : attributes){
                    printLine("    , ", this.getFeatureName(attributeDef));    
                }
                printLine("    );");
                printLine("  }");
                newLine();
                printLine("  public ", this.exceptionDef.getName(), "(");
                printLine("    java.lang.String domain");
                printLine("  , int errorCode");
                printLine("  , java.lang.String description");
                for (AttributeDef attributeDef : attributes) {
                    this.mapParameter(
                        "    , ",
                        attributeDef, 
                        ""
                    );
                }
                printLine("  ) {");
                printLine("    this(");
                printLine("      null");
                printLine("    , domain");
                printLine("    , errorCode");
                printLine("    , description");
                for (AttributeDef attributeDef : attributes) {
                    printLine("    , ", this.getFeatureName(attributeDef));    
                }
                printLine("    );");
                printLine("  }");
                newLine();
                printLine("  public ", this.exceptionDef.getName(), "(");
                printLine("    java.lang.Exception e");
                printLine("  , java.lang.String domain");
                printLine("  , int errorCode");
                printLine("  , java.lang.String description");
                for (AttributeDef attributeDef : attributes) {
                    this.mapParameter(
                        "  , ",
                        attributeDef, 
                        ""
                    );
                }
                printLine("  ) {");
                printLine("    super(");
                separator = "      ";
                for (AttributeDef attributeDef : attributes) {
                    printLine(separator, this.getFeatureName(attributeDef));    
                    separator = "    , ";
                }
                printLine("    );");
                printLine("    initCause(");
                printLine("      new org.openmdx.kernel.exception.BasicException(");
                printLine("        e");
                printLine("      , domain");
                printLine("      , errorCode");
                printLine("      , new org.openmdx.kernel.exception.BasicException.Parameter[]{");
                printLine("          new org.openmdx.kernel.exception.BasicException.Parameter(\"class\",\"", exceptionDef.getQualifiedName(), "\")");
                for (AttributeDef attributeDef : attributes) {
                    printLine(
                    	"        , new org.openmdx.kernel.exception.BasicException.Parameter(\"",
                    	attributeDef.getName(),
                    	"\",",
                    	getFeatureName(attributeDef),
                    	")"
                    );
                }
                printLine("        }");
                printLine("      , description == null ? \"",  exceptionDef.getQualifiedName(), "\" : description");
                printLine("      , this");
                printLine("      )");
                printLine("    );");
                printLine("  }");
                newLine();
                printLine("  /**");
                printLine("   * Note:<br>");
                printLine("   * <em>This method must be called with contract compliant arguments only!</em>");
                printLine("   *");
                printLine("   * @param contractCompliantServiceException with a cause providing at least<ul>");
                printLine("   *   <li>a <em>non-null</em> description");
                printLine("   *   <li>a \"class\" parameter with the qualified exception name");
                printLine("   *   <li>one parameter for each modelled exception feature");
                printLine("   * </ul>");
                printLine("   */");
                printLine("  public ", this.exceptionDef.getName(), "(");
                printLine("    org.openmdx.base.exception.ServiceException contractCompliantServiceException");
                printLine("  ) {");
                printLine("    this(");
                printLine("      (org.openmdx.kernel.exception.BasicException)contractCompliantServiceException.getCause()");
                printLine("    );");
                printLine("  }");
                newLine();
                printLine("  private ", this.exceptionDef.getName(), "(");
                printLine("    org.openmdx.kernel.exception.BasicException e");
                printLine("  ) {");
                printLine("    this(");
                printLine("      e");
                if(attributes.isEmpty()) {
                    printLine("    , (java.lang.String[])null");    
                } else {
                    for (AttributeDef attributeDef : attributes) {
                        printLine("    , e.getParameter(\"", attributeDef.getName(), "\")");    
                    }
                }
                printLine("    );");
                printLine("  }");
                newLine();
                printLine("  private ", this.exceptionDef.getName(), "(");
                printLine("      org.openmdx.kernel.exception.BasicException e");
                printLine("    , java.lang.String... arguments");
                printLine("  ) {");
                printLine("    super(");
                separator = "      ";
                int i = 0;
                for (AttributeDef attributeDef : attributes) {
                    printLine(
                        separator,
                        this.getParseExpression(
                            attributeDef.getQualifiedTypeName(),
                            ModelHelper.toMultiplicity(attributeDef.getMultiplicity()),
                            "arguments[" + i++ + "]"
                        )
                    );
                    separator = "    , ";
                }
                printLine("    );");
                printLine("    initCause(e);");
                printLine("  }");
                newLine();
                printLine("  @Override");
                printLine("  public java.lang.String getMessage(){");
                printLine("    org.openmdx.kernel.exception.BasicException cause = (org.openmdx.kernel.exception.BasicException) getCause();");
                printLine("    return cause == null ? null : (cause.getMessage() + \": \" + cause.getDescription());");
                printLine("  }");
                newLine();
                printLine("  @Override");
                printLine("  public java.lang.String toString(){");
                printLine("    java.lang.Throwable cause = getCause();");
                printLine("    return cause == null ?  super.toString() : cause.toString();");
                printLine("  }");
                newLine();
                printLine("  @Override");
                printLine("  public void printStackTrace(java.io.PrintStream s) {");
                printLine("    java.lang.Throwable cause = getCause();");
                printLine("    if(cause == null){");
                printLine("      super.printStackTrace(s);");
                printLine("    } else {");
                printLine("      cause.printStackTrace(s);");
                printLine("    }");
                printLine("  }");
                newLine();
                printLine("  @Override");
                printLine("  public void printStackTrace(java.io.PrintWriter s) {");
                printLine("    java.lang.Throwable cause = getCause();");
                printLine("    if(cause == null){");
                printLine("      super.printStackTrace(s);");
                printLine("    } else {");
                printLine("      cause.printStackTrace(s);");
                printLine("    }");
                printLine("  }");
                break;
            default:
                break;
        }
        newLine();
        //
        // Epilog
        //
        newLine();
        printLine("}");        
    }
    
    //-----------------------------------------------------------------------
    private final ExceptionDef exceptionDef;
    
}
