/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RoseImporter_1.java,v 1.1 2009/01/13 02:10:45 wfro Exp $
 * Description: model.importer.RoseImporter
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:45 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 */

/**
 * @author wfro
 */
package org.openmdx.application.mof.externalizer.rose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.omg.mof.cci.DirectionKind;
import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.mof.cci.AggregationKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.Stereotypes;
import org.openmdx.application.mof.externalizer.spi.ModelImporter_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Reads and parses a rose MDL file. Creates the corresponding org::omg::model1
 * objects for objects defined in "Logical View", i.e. classes, attributes
 * and associations.
 *
 */
@SuppressWarnings("unchecked")
public class RoseImporter_1
extends ModelImporter_1 {

    //------------------------------------------------------------------------

    /*
     * Constructs a rose importer.
     *
     * @param header dataprovider service header.
     *
     * @param target dataprovider used to store objects. The created model1 objects
     *        are stored using the addSetRequest() operation.
     *
     * @param providerName to root path where the objects are stored is 
     *        'org::omg::model1/provider/<providerName>.
     *
     * @param dirName directory name of the MDL file. Referenced CAT files must
     *        either be prefixed with the macro $curdir or be absolute paths.
     *
     * @param mdlFileName mdl file name.
     *
     * @param pathMap rose path map of the form key=<symbol>, actualPath=<path>. The
     *        path map is required to resolve symbols referenced in the MDL and
     *        CAT files.
     *
     * @param warnings print stream where warning messages or printed to.
     *
     */
    public RoseImporter_1(
        String dirName,
        String mdlFileName,
        Map pathMap,
        PrintStream warnings
    ) throws ServiceException {

        this.dirName = dirName;
        this.mdlFileName = mdlFileName;
        this.pathMap = pathMap;
        this.warnings = warnings;

    }

    //---------------------------------------------------------------------------

    /**
     * Tokenizes rose mdl and cat files. Documentation requires special
     * treatment (line beginning with |). 
     * <p>
     * LIMITATION:
     * <ul>
     *   <li> escapes (\) in string literals are not supported </li>
     * </ul>
     * <p>
     * NOTE:
     * <ul>
     *   <li> closes the reader when eof is reached </li>
     * </ul>
     */
    class RoseLexer {

        //-------------------------------------------------------------------------
        public RoseLexer(
            String fileName
        ) throws ServiceException {

            SysLog.trace("creating RoseLexer for file " + fileName);

            // init reader
            try {
                this.reader = new BufferedReader(
                    new FileReader(
                        this.file = new File(fileName)
                    )
                );
            }
            catch(FileNotFoundException e) {
                throw new ServiceException(e);
            }

            // init tokenizer ...
            this.currentLineNumber = 0;
            this.tokenizer = new StringTokenizer(
                "",
                ROSE_DELIMITERS,
                true
            );

            // ... and get first token
            getToken();

        }

        //-------------------------------------------------------------------------
        public String getToken(
        ) throws ServiceException {

            try {

                // can not get token if no tokenizer
                if(this.tokenizer == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "unexpected end of file"
                    );
                }

                // loop until we have a non-blank token
                for(;;) {

                    // get next line
                    while((this.tokenizer != null) && !this.tokenizer.hasMoreTokens()) {
                        this.currentLine = this.reader.readLine();
                        if(this.currentLine == null) {
                            this.reader.close();
                            this.tokenizer = null;
                        }
                        else {          
                            this.currentLineNumber++;
                            this.tokenizer = new StringTokenizer(
                                this.currentLine,
                                ROSE_DELIMITERS,
                                true
                            );
                        }
                    }

                    // get token
                    if(this.tokenizer != null) {

                        String nextToken = this.tokenizer.nextToken(ROSE_DELIMITERS);
                        boolean nextTokenIsString = false;

                        //SysLog.trace("token=>" + nextToken + "<");

                        // string literal
                        if("\"".equals(nextToken)) {
                            nextToken = this.tokenizer.nextToken("\"");
                            if("\"".equals(nextToken)) {
                                nextToken = "";
                            }
                            else if(!"\"".equals(this.tokenizer.nextToken(ROSE_DELIMITERS))) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "string terminator expected",
                                    new BasicException.Parameter("at", getCurrentLine())
                                );
                            }
                            //SysLog.trace("string found >" + nextToken + "<");              
                            nextTokenIsString = true;
                        }

                        // documentation
                        else if("|".equals(nextToken)) {
                            StringBuffer documentation = new StringBuffer();
                            while(this.currentLine.charAt(0) == '|') {
                                documentation.append(this.currentLine.substring(1));
                                this.currentLine = this.reader.readLine();
                                this.currentLineNumber++;
                            }
                            this.tokenizer = new StringTokenizer(
                                this.currentLine,
                                ROSE_DELIMITERS,
                                true
                            );
                            nextToken = documentation.toString();
                            nextTokenIsString = true;
                        }

                        // standard token
                        else {
                            if(" ".equals(nextToken) || "\t".equals(nextToken)) {
                                continue;
                            }
                        }

                        // only return if non-blank or string literal
                        String tokenToReturn = this.currentToken;
                        this.currentToken = nextToken;
                        this.currentTokenIsString = nextTokenIsString;
                        return tokenToReturn;
                    }

                    // no next token. Return current
                    else {
                        return this.currentToken;
                    }

                }

            }
            catch(IOException e) {
                throw new ServiceException(e);
            }

        }

        //-------------------------------------------------------------------------
        public String peekToken(
        ) throws ServiceException { 
            return this.currentToken;
        }

        //-------------------------------------------------------------------------
        public void assertToken(
            String token
        ) throws ServiceException {

            if(!token.equals(this.currentToken)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "unexpected token",
                    new BasicException.Parameter("current token", currentToken),
                    new BasicException.Parameter("expected token", token),
                    new BasicException.Parameter("at", getCurrentLine())
                );
            }
            getToken();
        }

        //-------------------------------------------------------------------------
        public boolean isLeftParam(
        ) {
            return "(".equals(this.currentToken);
        }

        //-------------------------------------------------------------------------
        public boolean isRightParam(
        ) {
            return ")".equals(this.currentToken);
        }

        //-------------------------------------------------------------------------
        public boolean isString(
        ) {
            return this.currentTokenIsString;
        }

        //-------------------------------------------------------------------------
        public String getCurrentLine(
        ) {
            return 
            "file=" + this.file.getName() + 
            "; line=>" + this.currentLineNumber + 
            "<; token=>" + this.currentToken + 
            "<; line=>" + this.currentLine + "<";
        }

        //-------------------------------------------------------------------------
        public boolean eof(
        ) {
            return (currentLine == null);
        }

        public String currentDir(
        ) {
            return this.file.getParent();
        }

        //---------------------------------------------------------------------------
        // Variables
        //---------------------------------------------------------------------------

        private File file = null;
        private BufferedReader reader = null;
        private String currentLine = null;
        private int currentLineNumber = 0;
        private String currentToken = null;
        private boolean currentTokenIsString = false;
        private StringTokenizer tokenizer = null;

        private static final String ROSE_DELIMITERS = " \t|(),\"";
    }

    //---------------------------------------------------------------------------
    private void printWarning(
        String text
    ) {
        if(this.warnings != null) {
            this.warnings.println(text);
        }
    }

    //---------------------------------------------------------------------------
    private String scopeToPathComponent(
        List scope
    ) throws ServiceException {

        if(scope.size() >= 2) {
            StringBuffer scopeAsString = new StringBuffer(
                (String)scope.get(1)
            );
            for(int i = 2; i < scope.size(); i++) {
                scopeAsString.append(":" + (String)scope.get(i));
            }
            return scopeAsString.toString();
        }
        else {
            return "UNDEF";
        }
    }

    //---------------------------------------------------------------------------
    /**
     * tokenizes name by '::' and returns a list of tokens (scope name)
     */
    private List toScope(
        String name
    ) throws ServiceException {

        // parse name into its name components
        StringTokenizer tokenizer = null;
        if(!name.startsWith("Logical View::")) {
            tokenizer = new StringTokenizer(
                "Logical View::" + name,
                ":"
            );
        }
        else {
            tokenizer = new StringTokenizer(
                name,
                ":"
            );
        }
        ArrayList scope = new ArrayList();
        while(tokenizer.hasMoreTokens()) {
            scope.add(tokenizer.nextToken());
        }
        return scope;
    }

    //---------------------------------------------------------------------------
    /**
     * Convert a string of the form 
     * 'Logical View::org::openmdx::address1::CAddress'
     * to a path of the form 
     * 'org::omg::model1/provider/<providerName>/segment/org::openmdx::address1/element/CAddress'
     *
     */
    private Path roseNameToPath(
        String qualifiedRoseName
    ) throws ServiceException {

        List scope = toScope(qualifiedRoseName);
        String elementName = (String)scope.get(scope.size()-1);
        scope.remove(scope.size()-1);
        return toElementPath(
            scope,
            elementName
        );

    }

    //---------------------------------------------------------------------------
    /**
     * the path of a ModelClass is of the form
     * org::omg/model1/<provider>/segment/<modelName>/element/<modelName>:<elementName>
     */
    private Path toElementPath(
        List scope,
        String elementName
    ) throws ServiceException {
        return toElementPath(
            scopeToPathComponent(scope),
            elementName
        );
    }

    //---------------------------------------------------------------------------

    /**
     * Parse an operation parameter as STRUCTURE_FIELD
     */
    private DataproviderObject parseRoseParameter(
        RoseLexer lexer,
        List scope,
        DataproviderObject parameterType
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Parameter");

        String parameterName = lexer.getToken();
//      int posOfFirstChar = 0;

        // because Rose does not support multiplicity for parameters they are put
        // in front of the parameter name: <<x..>> parameterName
        StringBuffer multiplicity = new StringBuffer();
        parameterName = this.parseMultiplicity(
            parameterName,
            parameterType,
            parameterName,
            multiplicity
        );
        DataproviderObject parameterDef = new DataproviderObject(
            new Path(
                parameterType.path().toString() + "::" + parameterName
            ) 
        );

        // object_class
        parameterDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.STRUCTURE_FIELD
        );

        // container
        parameterDef.values("container").add(
            parameterType.path()
        );

        parameterDef.values("maxLength").add(new Integer(1000000));
        parameterDef.values("multiplicity").add(multiplicity.toString());

        // quid
        if("quid".equals(lexer.peekToken())) {
            lexer.assertToken("quid");
            lexer.getToken();
        }

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            parameterDef.values("annotation").add(
                lexer.getToken()
            );
        }

        // type
        if("type".equals(lexer.peekToken())) {
            lexer.assertToken("type");
            parameterDef.values("type").add(
                roseNameToPath(lexer.getToken())
            );
        }

        // quid
        if("quidu".equals(lexer.peekToken())) {
            lexer.assertToken("quidu");
            lexer.getToken();
        }

        lexer.assertToken(")");

        return parameterDef;
    }

    //---------------------------------------------------------------------------
    private void parseRoseOperation(
        RoseLexer lexer,
        List scope,
        DataproviderObject classDef
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Operation");

        String operationName = lexer.getToken();

        DataproviderObject operationDef = new DataproviderObject(
            new Path(
                classDef.path().toString() + "::" + operationName
            ) 
        );

        // object_class
        operationDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.OPERATION
        );

        // container
        operationDef.values("container").add(
            classDef.path()
        );

        // default values
        operationDef.values("isQuery").add(
            new Boolean(false)
        );

        // attributes
        if("attributes".equals(lexer.peekToken())) {
            lexer.assertToken("attributes");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("Attribute_Set");
            while(!lexer.isRightParam()) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("Attribute");
                lexer.assertToken("tool");
                String toolName = lexer.getToken();
                lexer.assertToken("name");
                String attrName = lexer.getToken();
                lexer.assertToken("value");
                Object attrValue = null;
                if(lexer.isLeftParam()) {
                    lexer.assertToken("(");
                    lexer.assertToken("value");
                    lexer.assertToken("Text");
                    attrValue = Boolean.valueOf(lexer.getToken());
                    lexer.assertToken(")");
                }
                else {
                    attrValue = lexer.getToken();
                }
                //SysLog.trace("tool=" + toolName + "; attribute=" + attrName + "; value=", attrValue);
                if("SPICE".equals(toolName)) {
                    if("true".equals(attrValue) || "false".equals(attrValue)) {
                        operationDef.clearValues(attrName).add(
                            new Boolean((String)attrValue)
                        );
                    }
                    else {
                        operationDef.clearValues(attrName).add(
                            attrValue 
                        );
                    }
                }
                lexer.assertToken(")");
            }
            lexer.assertToken(")");
        }

        // quid
        if("quid".equals(lexer.peekToken())) {
            lexer.assertToken("quid");
            lexer.getToken();
        }

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            operationDef.values("annotation").add(
                lexer.getToken()
            );
        }

        // stereotype
        boolean isException = false;
        if("stereotype".equals(lexer.peekToken())) {
            lexer.assertToken("stereotype");
            operationDef.values("stereotype").addAll(
                this.parseStereotype(lexer.getToken())
            );

            // well-known stereotype <<exception>>
            if(operationDef.values("stereotype").contains(Stereotypes.EXCEPTION)) {
                operationDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.EXCEPTION
                );
                operationDef.clearValues("stereotype");
                isException = true;
            }
        }

        // parameters
        if("parameters".equals(lexer.peekToken())) {

            /**
             * In SPICE all operations have excatly one parameter with name 'in'. The importer
             * supports two forms how parameters may be specified:
             * 1) p0:t0, p1:t1, ..., pn:tn. In this case a class with stereotype <parameter> is created
             *    and p0, ..., pn are added as class attributes. Finally, a parameter with name 'in'
             *    is created with the created parameter type.
             * 2) in:t. In this case the the parameter with name 'in' is created with the specified type.
             */

            /**
             * Create the parameter type class. We need this class only in case 1. Because we only know
             * at the end whether we really need it, create it anyway but do not add it to the repository.
             */
            String capOperationName = 
                operationName.substring(0,1).toUpperCase() +
                operationName.substring(1);

            DataproviderObject parameterType = new DataproviderObject(
                new Path(
                    classDef.path().toString() + capOperationName + "Params"
                )
            );
            parameterType.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.STRUCTURE_TYPE
            );
            parameterType.values("visibility").add(VisibilityKind.PUBLIC_VIS);
            parameterType.values("isAbstract").add(new Boolean(false));
            parameterType.values("container").addAll(
                classDef.values("container")
            );

            // parse parameters (features of modelInParameterType)
            lexer.assertToken("parameters");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("Parameters");

            /**
             * Create parameters either as STRUCTURE_FIELDs of parameterType (case 1) 
             * or as PARAMETER of operationDef (case 2)
             */
            boolean createParameterType = true;
            boolean parametersCreated = false;

            while(!lexer.isRightParam()) {

                DataproviderObject parameterDef = this.parseRoseParameter(
                    lexer,
                    scope,
                    parameterType
                );

                /**
                 * Case 2: Parameter with name 'in'. Create object as PARAMETER.
                 */
                String fullQualifiedParameterName = parameterDef.path().getBase();
                if("in".equals(fullQualifiedParameterName.substring(fullQualifiedParameterName.lastIndexOf(':') + 1))) {

                    // 'in' is the only allowed parameter
                    if(parametersCreated) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE,
                            new BasicException.Parameter("operation", operationDef)
                        );        		
                    }
                    parameterType = new DataproviderObject(
                        (Path)parameterDef.values("type").get(0)
                    );
                    createParameterType = false;
                }

                /**
                 * Case 1: Parameter is attribute of parameter type. Create object as STRUCTURE_FIELD.
                 */
                else {

                    // 'in' is the only allowed parameter
                    if(!createParameterType) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "Parameter format must be [p0:T0, ... ,pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE,
                            new BasicException.Parameter("operation", operationDef)
                        );        		
                    }
                    this.createModelElement(
                        scope,
                        parameterDef
                    );
                    parametersCreated = true;
                }
            }
            lexer.assertToken(")");

            /**
             * Case 1: parameter type must be created
             */
            if(createParameterType) {
                this.createModelElement(
                    scope,
                    parameterType
                );
            }

            // in-parameter
            DataproviderObject inParameterDef = new DataproviderObject(
                new Path(
                    operationDef.path().toString() + "::in"
                ) 
            );
            inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PARAMETER
            );  
            inParameterDef.values("container").add(
                operationDef.path()
            );  
            inParameterDef.values("direction").add(
                DirectionKind.IN_DIR
            );
            inParameterDef.values("multiplicity").add(
                "1..1"
            );
            inParameterDef.values("type").add(
                parameterType.path()
            );
            this.createModelElement(
                scope,
                inParameterDef
            );

        }

        // void in-parameter
        else {
            DataproviderObject inParameterDef = new DataproviderObject(
                new Path(
                    operationDef.path().toString() + "::in"
                ) 
            );
            inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PARAMETER
            );  
            inParameterDef.values("container").add(
                operationDef.path()
            );  
            inParameterDef.values("direction").add(
                DirectionKind.IN_DIR
            );
            inParameterDef.values("multiplicity").add(
                "1..1"
            );
            inParameterDef.values("type").add(
                roseNameToPath("org::openmdx::base::Void")
            );
            this.createModelElement(
                scope,
                inParameterDef
            );
        }

        // result
        DataproviderObject resultDef = new DataproviderObject(
            new Path(
                operationDef.path().toString() + "::result"
            ) 
        );
        resultDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.PARAMETER
        );  
        resultDef.values("container").add(
            operationDef.path()
        );  
        resultDef.values("direction").add(
            DirectionKind.RETURN_DIR
        );
        resultDef.values("multiplicity").add(
            "1..1"
        );

        // result type
        if("result".equals(lexer.peekToken())) {
            lexer.assertToken("result");
            resultDef.values("type").add(
                this.roseNameToPath(
                    lexer.getToken()
                )
            );
        }
        else {
            resultDef.values("type").add(
                this.roseNameToPath(
                    "org:openmdx:base:Void"
                )
            );
        }

        // only create result for operations
        if(!isException) {    	
            this.createModelElement(
                scope,
                resultDef
            );
        }

        // exceptions
        if("exceptions".equals(lexer.peekToken())) {
            lexer.assertToken("exceptions");
            StringTokenizer exceptions = new StringTokenizer(lexer.getToken(), ", ");
            while(exceptions.hasMoreTokens()) {
                String qualifiedExceptionName = exceptions.nextToken();
                String qualifiedClassName = qualifiedExceptionName.substring(0, qualifiedExceptionName.lastIndexOf(':'));
                operationDef.values("exception").add(
                    new Path(
                        this.roseNameToPath(
                            qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'))
                        ).toString() + 
                        "::" + 
                        qualifiedExceptionName.substring(qualifiedExceptionName.lastIndexOf(':') + 1)
                    )
                );      	
            }
        }

        // concurrency
        if("concurrency".equals(lexer.peekToken())) {
            lexer.assertToken("concurrency");
            lexer.getToken();
        }

        // semantics
        if("semantics".equals(lexer.peekToken())) {
            lexer.assertToken("semantics");
            lexer.assertToken("(");
            lexer.assertToken("object");
            lexer.assertToken("Semantic_Info");
            lexer.assertToken("PDL");
            operationDef.values("semantics").add(
                lexer.getToken()
            );
            lexer.assertToken(")");
        }

        // opExportControl
        if("opExportControl".equals(lexer.peekToken())) {
            lexer.assertToken("opExportControl");
            lexer.getToken();
        }

        // uid
        if("uid".equals(lexer.peekToken())) {
            lexer.assertToken("uid");
            lexer.getToken();
        }

        // visibility
        if("exportControl".equals(lexer.peekToken())) {
            lexer.assertToken("exportControl");
            lexer.getToken();
        }
        operationDef.values("visibility").add(
            VisibilityKind.PUBLIC_VIS
        );

        // scope
        operationDef.values("scope").add(
            ScopeKind.INSTANCE_LEVEL
        );

        this.createModelElement(
            scope,
            operationDef
        );

        lexer.assertToken(")");

    }

    //---------------------------------------------------------------------------
    private DataproviderObject parseRoseAssociationEnd(
        RoseLexer lexer,
        List scope,
        DataproviderObject associationDef
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Role");
        String roleName = lexer.getToken();

        DataproviderObject associationEndDef = new DataproviderObject(
            new Path(
                associationDef.path().toString() + "::" + roleName
            ) 
        );

        // warning if assocation end is unnamed
        if(roleName.startsWith("$UNNAMED$")) {
            printWarning(
                "association end " +
                associationEndDef.path() +
                " is not named"
            );
        }

        // object_class
        associationEndDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.ASSOCIATION_END
        );

        // container
        associationEndDef.values("container").add(
            associationDef.path()
        );   

        // name
        associationEndDef.values("name").add(
            roleName
        );

        // ignore optional attributes
        ignoreAttributes(lexer);

        // quid
        if("quid".equals(lexer.peekToken())) {
            lexer.assertToken("quid");
            lexer.getToken();
        }

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            associationEndDef.values("annotation").add(
                lexer.getToken()
            );
        }

        // label
        if("label".equals(lexer.peekToken())) {
            lexer.assertToken("label");
            lexer.getToken();
        }

        // supplier
        if("supplier".equals(lexer.peekToken())) {
            lexer.assertToken("supplier");
            associationEndDef.values("type").add(
                roseNameToPath(lexer.getToken())
            );
        }

        // quidu
        if("quidu".equals(lexer.peekToken())) {
            lexer.assertToken("quidu");
            lexer.getToken();
        }

        // keys
        if("keys".equals(lexer.peekToken())) {
            lexer.assertToken("keys");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("class_attribute_list");
            while(!lexer.isRightParam()) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("ClassAttribute");
                associationEndDef.values("qualifierName").add(
                    lexer.getToken()
                );
                lexer.assertToken("quid");
                lexer.getToken();
                lexer.assertToken("type");
                associationEndDef.values("qualifierType").add(
                    roseNameToPath(lexer.getToken())
                );
                if("quidu".equals(lexer.peekToken())) {
                    lexer.assertToken("quidu");
                    lexer.getToken();
                }
                lexer.assertToken(")");
            }
            lexer.assertToken(")");
        }

        // multiplicity
        if("client_cardinality".equals(lexer.peekToken())) {
            lexer.assertToken("client_cardinality");
            lexer.assertToken("(");
            lexer.assertToken("value");
            lexer.assertToken("cardinality");
            StringBuffer multiplicity = new StringBuffer();
            parseMultiplicity(
                lexer.getToken(),
                associationDef,
                roleName,
                multiplicity
            );
            associationEndDef.values("multiplicity").add(
                multiplicity.toString()
            );
            lexer.assertToken(")");
        }

        // isChangeable
        if("Constraints".equals(lexer.peekToken())) {
            lexer.assertToken("Constraints");
            String constraints = lexer.getToken();
            boolean constraintIsFrozen = "isFrozen".equals(constraints);
            associationEndDef.values("isChangeable").add(
                new Boolean(!constraintIsFrozen)
            );
            if(!constraintIsFrozen) {
                // add new constraint
                DataproviderObject associationEndConstraintDef = new DataproviderObject(
                    new Path(
                        associationEndDef.path().toString() + "::" + constraints
                    ) 
                );
                // object_class
                associationEndConstraintDef.values(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.CONSTRAINT
                );

                // container
                associationEndConstraintDef.values("container").add(
                    associationEndDef.path()
                );   
                this.createModelElement(
                    scope,
                    associationEndConstraintDef
                );
            }
        }
        else {
            associationEndDef.values("isChangeable").add(
                new Boolean(true)
            );
        }

        // exportControl
        if("exportControl".equals(lexer.peekToken())) {
            lexer.assertToken("exportControl");
            lexer.getToken();
        }

        // aggregation (none = link)    
        associationEndDef.values("aggregation").add(
            AggregationKind.NONE
        );
        if("Containment".equals(lexer.peekToken())) {
            lexer.assertToken("Containment");
            String containment = lexer.getToken();
            if("By Reference".equals(containment)) {
                associationEndDef.clearValues("aggregation").add(
                    AggregationKind.SHARED
                );
            }
            else if("By Value".equals(containment)) {
                associationEndDef.clearValues("aggregation").add(
                    AggregationKind.COMPOSITE
                );
            }
        }

        // isNavigable
        if("is_navigable".equals(lexer.peekToken())) {
            lexer.assertToken("is_navigable");
            associationEndDef.values("isNavigable").add(
                new Boolean("TRUE".equals(lexer.getToken()))
            );
        }
        else { 
            associationEndDef.values("isNavigable").add(
                new Boolean(false)
            );
        }

        // is_aggregate
        if("is_aggregate".equals(lexer.peekToken())) {
            lexer.assertToken("is_aggregate");
            lexer.getToken();
        }

        lexer.assertToken(")");

        // Print aggregation!=NONE should be navigable
        if(!AggregationKind.NONE.equals(associationEndDef.values("aggregation").get(0))) {
            if(!((Boolean)associationEndDef.values("isNavigable").get(0)).booleanValue()) {
                printWarning(
                    "Probably wrong value for isNavigable for " + 
                    associationEndDef.path() +
                    "; association ends with aggregation 'composite|shared' should be navigable." 
                );
            }
        }

        return associationEndDef;

    }

    //---------------------------------------------------------------------------
    private void parseRoseAttribute(
        RoseLexer lexer,
        List scope,
        DataproviderObject classDef,
        boolean isStructureField
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("ClassAttribute");
        String attributeName = lexer.getToken();

        DataproviderObject attributeDef = new DataproviderObject(
            new Path(classDef.path().toString() + "::" + attributeName)
        );

        if(isStructureField) {
            attributeDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.STRUCTURE_FIELD
            );
        }
        else {
            attributeDef.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.ATTRIBUTE
            );
        }

        // container
        attributeDef.values("container").add(
            classDef.path()
        );

        // user-defined attributes. set default values for
        // maxLength, uniqueValues, isLanguageNeutral, isChangeable
        attributeDef.values("maxLength").add(new Integer(DEFAULT_ATTRIBUTE_MAX_LENGTH));
        attributeDef.values("isChangeable").add(new Boolean(true));

        // overwrite default values if set
        if("attributes".equals(lexer.peekToken())) {
            lexer.assertToken("attributes");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("Attribute_Set");
            while(!lexer.isRightParam()) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("Attribute");
                lexer.assertToken("tool");
                String toolName = lexer.getToken();
                lexer.assertToken("name");
                String attrName = lexer.getToken();
                lexer.assertToken("value");
                Object attrValue = null;
                if(lexer.isLeftParam()) {
                    lexer.assertToken("(");
                    lexer.assertToken("value");
                    lexer.assertToken("Text");
                    attrValue = Boolean.valueOf(lexer.getToken());
                    lexer.assertToken(")");
                }
                else {
                    attrValue = lexer.getToken();
                }
                //SysLog.trace("tool=" + toolName + "; attribute=" + attrName + "; value=", attrValue);
                if("SPICE".equals(toolName)) {
                    if("true".equals(attrValue) || "false".equals(attrValue)) {
                        attributeDef.clearValues(attrName).add(
                            new Boolean((String)attrValue)
                        );
                    }
                    else if ("maxLength".equals(attrName)) {
                        Integer intVal = null;
                        try {
                            intVal = new Integer((String)attrValue);
                        } catch(NumberFormatException ex) {
                            printWarning(
                                "Illegal number format for value for attribute " + 
                                attrName + 
                                " (using default value instead)"
                            );
                            intVal = new Integer(DEFAULT_ATTRIBUTE_MAX_LENGTH); 
                        }
                        attributeDef.clearValues("maxLength").add(intVal);
                    }
                    else {
                        attributeDef.clearValues(attrName).add(
                            attrValue 
                        );
                    }
                }
                lexer.assertToken(")");
            }
            lexer.assertToken(")");
        }

        // quid
        if("quid".equals(lexer.peekToken())) {
            lexer.assertToken("quid");
            lexer.getToken();
        }

        // annotation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            attributeDef.values("annotation").add(
                lexer.getToken()
            );
        }

        // multiplicity
        if("stereotype".equals(lexer.peekToken())) {
            lexer.assertToken("stereotype");
            StringBuffer multiplicity = new StringBuffer();
            this.parseMultiplicity(
                lexer.getToken(),
                classDef,
                attributeName,
                multiplicity
            );
            attributeDef.values("multiplicity").add(
                multiplicity.toString()
            );
        }
        else {
            attributeDef.values("multiplicity").add(
                "1..1"
            );
        }

        // type
        if("type".equals(lexer.peekToken())) {
            lexer.assertToken("type");
            attributeDef.values("type").add(
                roseNameToPath(lexer.getToken())
            );
        }

        // quidu
        if("quidu".equals(lexer.peekToken())) {
            lexer.assertToken("quidu");
            lexer.getToken();
        }

        // visibility
        if("exportControl".equals(lexer.peekToken())) {
            lexer.assertToken("exportControl");
            lexer.getToken();
            attributeDef.values("visibility").add(
                VisibilityKind.PUBLIC_VIS
            );
        }
        else {
            attributeDef.values("visibility").add(
                VisibilityKind.PRIVATE_VIS
            );
        }

        // isDerived
        if("derived".equals(lexer.peekToken())) {
            lexer.assertToken("derived");
            attributeDef.values("isDerived").add(
                new Boolean("TRUE".equals(lexer.getToken()))
            );
        }
        else {
            attributeDef.values("isDerived").add(
                new Boolean(false)
            );
        }

        // scope
        attributeDef.values("scope").add(
            ScopeKind.INSTANCE_LEVEL
        );

        lexer.assertToken(")");

        // remove non StructureField attributes
        if(isStructureField) {
            attributeDef.clearValues("visibility");
            attributeDef.clearValues("isDerived");
            attributeDef.clearValues("scope");
            attributeDef.clearValues("isChangeable");
        }

        this.createModelElement(
            scope,
            attributeDef
        );
    }

    private void ignoreAttributes(RoseLexer lexer) throws ServiceException
    {
        // overwrite default values if set
        if("attributes".equals(lexer.peekToken())) {
            lexer.assertToken("attributes");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("Attribute_Set");
            while(!lexer.isRightParam()) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("Attribute");
                lexer.assertToken("tool");
                /* String toolName = */ lexer.getToken();
                lexer.assertToken("name");
                /* String attrName = */ lexer.getToken();
                lexer.assertToken("value");
                /* Object attrValue = null; */
                if(lexer.isLeftParam()) {
                    lexer.assertToken("(");
                    lexer.assertToken("value");
                    lexer.assertToken("Text");
                    /* attrValue = Boolean.valueOf( */ lexer.getToken() /*) */;
                    lexer.assertToken(")");
                }
                else {
                    /* attrValue = */ lexer.getToken();
                }
                lexer.assertToken(")");
            }
            lexer.assertToken(")");
        }
    }

    //---------------------------------------------------------------------------
    private void parseRoseAssociation(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        String associationName = lexer.getToken();

        // ModelAssociation object
        DataproviderObject modelAssociation = new DataproviderObject(
            toElementPath(
                scope,
                associationName
            )
        );

        // object_class
        modelAssociation.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.ASSOCIATION
        );

        // container
        modelAssociation.values("container").add(
            toElementPath(
                scope,
                (String)scope.get(scope.size()-1)
            )
        );

        // quid
        lexer.assertToken("quid");
        lexer.getToken();

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            modelAssociation.values("annotation").add(
                lexer.getToken()
            );
        }

        // association ends
        DataproviderObject modelAssociationEnd1 = null;
        DataproviderObject modelAssociationEnd2 = null;

        if("roles".equals(lexer.peekToken())) {
            lexer.assertToken("roles");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("role_list");
            modelAssociationEnd1 = parseRoseAssociationEnd(
                lexer,
                scope,
                modelAssociation
            );
            modelAssociationEnd2 = parseRoseAssociationEnd(
                lexer,
                scope,
                modelAssociation
            );
            lexer.assertToken(")");
        }

        // isDerived
        if("derived".equals(lexer.peekToken())) {
            lexer.assertToken("derived");
            modelAssociation.values("isDerived").add(
                new Boolean("TRUE".equals(lexer.getToken()))
            );
        }
        else {
            modelAssociation.values("isDerived").add(
                new Boolean(false)
            );
        }

        verifyAndCompleteAssociationEnds(
            modelAssociationEnd1,
            modelAssociationEnd2
        );

        exportAssociationEndAsReference(
            modelAssociationEnd1,
            modelAssociationEnd2,
            modelAssociation,
            scope
        );
        exportAssociationEndAsReference(
            modelAssociationEnd2,
            modelAssociationEnd1,
            modelAssociation,
            scope
        );

        // remove name attributes before setting
        modelAssociationEnd1.attributeNames().remove("name");
        createModelElement(
            scope,
            modelAssociationEnd1
        );

        modelAssociationEnd2.attributeNames().remove("name");
        createModelElement(
            scope,
            modelAssociationEnd2
        );

        // complete association
        modelAssociation.values("isAbstract").add(new Boolean(false));
        modelAssociation.values("visibility").add(VisibilityKind.PUBLIC_VIS);
        createModelElement(
            scope,
            modelAssociation
        );

        lexer.assertToken(")");

    }

    //---------------------------------------------------------------------------
    private void parseRoseClassifier(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        String className = lexer.getToken();
        //SysLog.trace("> parseRoseClass=" + className);

        // ModelClass object
        DataproviderObject classifierDef = new DataproviderObject(
            toElementPath(
                scope,
                className
            )
        );

        // object_class
        classifierDef.values(SystemAttributes.OBJECT_CLASS).add(
            ModelAttributes.CLASS
        );

        // container
        classifierDef.values("container").add(
            toElementPath(
                scope,
                (String)scope.get(scope.size()-1)
            )
        );

        // ignore optional attributes
        ignoreAttributes(lexer);

        // quid
        lexer.assertToken("quid");
        lexer.getToken();

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            classifierDef.values("annotation").add(
                lexer.getToken()
            );
        }

        // stereotype
        boolean isStructureType = false;
        boolean isAliasType = false;

        if("stereotype".equals(lexer.peekToken())) {
            lexer.assertToken("stereotype");
            classifierDef.values("stereotype").addAll(
                this.parseStereotype(lexer.getToken())
            );

            // handle well-known stereotypes
            if(classifierDef.values("stereotype").contains(Stereotypes.PRIMITIVE)) {
                SysLog.trace("changing type to " + ModelAttributes.PRIMITIVE_TYPE);
                classifierDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.PRIMITIVE_TYPE
                );
                classifierDef.clearValues("stereotype");
            }
            else if(classifierDef.values("stereotype").contains(Stereotypes.STRUCT)) {
                isStructureType = true;
                SysLog.trace("changing type to " + ModelAttributes.STRUCTURE_TYPE);
                classifierDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.STRUCTURE_TYPE
                );
                classifierDef.clearValues("stereotype");
            }
            else if(classifierDef.values("stereotype").contains(Stereotypes.ALIAS)) {
                isAliasType = true;
                SysLog.trace("changing type to " + ModelAttributes.ALIAS_TYPE);
                classifierDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
                    ModelAttributes.ALIAS_TYPE
                );
                classifierDef.clearValues("stereotype");
            }
            else if(classifierDef.values("stereotype").contains("parameter")) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "Stereotype <<parameter>> is not supported anymore. Use stereotype <<" + Stereotypes.STRUCT + ">> instead",
                    new BasicException.Parameter("classifier", classifierDef)
                );
            }
        }

        // supertype
        if("superclasses".equals(lexer.peekToken())) {
            SortedSet superTypePaths = new TreeSet();
            lexer.assertToken("superclasses");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("inheritance_relationship_list");
            while(!lexer.isRightParam()) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("Inheritance_Relationship");
                lexer.assertToken("quid");
                lexer.getToken();
                if("label".equals(lexer.peekToken())) {
                    lexer.assertToken("label");
                    lexer.getToken();
                }
                lexer.assertToken("supplier");
                superTypePaths.add(
                    roseNameToPath(lexer.getToken())
                );
                lexer.assertToken("quidu");
                lexer.getToken();
                lexer.assertToken(")");
            }
            lexer.assertToken(")");

            // add supertypes in sorted order
            for(
                    Iterator it = superTypePaths.iterator();
                    it.hasNext();
            ) {
                classifierDef.values("supertype").add(
                    it.next()
                );
            }
        }

        // operations
        if("operations".equals(lexer.peekToken())) {

            // operations not allowed on STRUCTURE_TYPE and ALIAS_TYPE
            if(
                    ModelAttributes.STRUCTURE_TYPE.equals(classifierDef.values(SystemAttributes.OBJECT_CLASS).get(0)) ||
                    ModelAttributes.ALIAS_TYPE.equals(classifierDef.values(SystemAttributes.OBJECT_CLASS).get(0))
            ) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "operations not allowed on structure and alias types",
                    new BasicException.Parameter("classifier", classifierDef)
                );
            }

            lexer.assertToken("operations");      
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("Operations");
            while(!lexer.isRightParam()) {
                this.parseRoseOperation(
                    lexer,
                    scope,
                    classifierDef
                );
            }
            lexer.assertToken(")");
        }

        // attributes
        if("class_attributes".equals(lexer.peekToken())) {
            lexer.assertToken("class_attributes");      
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("class_attribute_list");

            // get referenced type in case of AliasType
            if(isAliasType) {
                lexer.assertToken("(");
                lexer.assertToken("object");
                lexer.assertToken("ClassAttribute");
                classifierDef.values("type").add(
                    roseNameToPath(lexer.getToken())
                );
                // Ignore attributes
                ignoreAttributes(lexer);

                while(!lexer.isRightParam()) {
                    lexer.getToken();
                }
                lexer.assertToken(")");
            }

            // ATTRIBUTE | STRUCTURE_FIELD
            else {
                while(!lexer.isRightParam()) {
                    this.parseRoseAttribute(
                        lexer,
                        scope,
                        classifierDef,
                        isStructureType
                    );
                }
            }
            lexer.assertToken(")");
        }

        // {module, quidu}
        while("module".equals(lexer.peekToken()) || "quidu".equals(lexer.peekToken())) {
            lexer.getToken();
            lexer.getToken();
        }

        // isAbstract
        if("abstract".equals(lexer.peekToken())) {
            lexer.assertToken("abstract");      
            classifierDef.values("isAbstract").add(
                new Boolean(
                    "TRUE".equals(lexer.getToken())
                )
            );
        }
        else {
            classifierDef.values("isAbstract").add(
                new Boolean(false)
            );      
        }  

        // visibility
        classifierDef.values("visibility").add(
            VisibilityKind.PUBLIC_VIS
        );

        // isSingleton
        classifierDef.values("isSingleton").add(new Boolean(false));

        // usage
        if("used_nodes".equals(lexer.peekToken())) {
            lexer.assertToken("used_nodes");
            parseRoseObject(lexer, scope, false);
        }

        lexer.assertToken(")");

        // write object
        this.createModelElement(
            scope,
            classifierDef
        );

        //if(isAliasType) {
        //  SysLog.detail("< parseRoseClass", modelClass);
        //}

    }

    //---------------------------------------------------------------------------
    private void parseRoseAttributeValue(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        //SysLog.trace("> parseRoseAttributeValue");

        // complex value
        if(lexer.isLeftParam()) {
            lexer.getToken();

            // object
            if("object".equals(lexer.peekToken())) {
                lexer.assertToken("object");
                parseRoseObject(
                    lexer,
                    scope,
                    false
                );
            }

            // list
            else if("list".equals(lexer.peekToken())) {
                lexer.assertToken("list");
                parseRoseList(
                    lexer,
                    scope
                );
                lexer.assertToken(")");
            }

            // value list
            else {
                while(!lexer.isRightParam()) {
                    lexer.getToken();
                }
                lexer.assertToken(")");
            }
        }

        // simple value
        else {
            /* String simpleValue = */ lexer.getToken();
            //SysLog.trace("simple value=" + simpleValue);
        }

        //SysLog.trace("< parseRoseAttributeValue");
    }

    //---------------------------------------------------------------------------
    private void parseRoseList(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        // Compartment
        if("Compartment".equals(lexer.peekToken())) {
            lexer.assertToken("Compartment");
            while(!lexer.isRightParam()) {
                lexer.getToken();
            }
        }

        // Points 
        else if("Points".equals(lexer.peekToken())) {
            lexer.assertToken("Points");

            // a point is of the form "(x, y)"
            while(!lexer.isRightParam()) {
                lexer.getToken();
                lexer.getToken();
                lexer.getToken();
                lexer.getToken();
                lexer.getToken();
            }
        }

        // standard list
        else {
            if(!lexer.isLeftParam()) {
                lexer.getToken();
            }
            parseRoseObjects(
                lexer,
                scope
            );
        }

    }

    //---------------------------------------------------------------------------
    private void parseRoseObject(
        RoseLexer lexer,
        List scope,
        boolean usePrefix
    ) throws ServiceException {

        //SysLog.trace("> parseRoseObject");

        if(usePrefix) {
            lexer.assertToken("(");
            lexer.assertToken("object");
        }

        // object type, name
        String objectType = lexer.getToken();
        //SysLog.trace("object type=" + objectType);


        // Class
        if("Class".equals(objectType)) {
            this.parseRoseClassifier(
                lexer,
                scope
            );
        }

        // Class_Category
        else if("Class_Category".equals(objectType)) {

            ArrayList newScope = new ArrayList(scope);
            newScope.add(
                lexer.getToken()
            );

            // Container modelPackage. 
            // The container package can be overwritten at a later time depending 
            // on the structure of the mdl/cat files
            DataproviderObject modelPackage = new DataproviderObject(
                toElementPath(
                    newScope,
                    (String)newScope.get(newScope.size()-1)
                )
            );
            modelPackage.values(SystemAttributes.OBJECT_CLASS).add(
                ModelAttributes.PACKAGE
            );
            modelPackage.values("isAbstract").add(new Boolean(false));
            modelPackage.values("visibility").add(VisibilityKind.PUBLIC_VIS);
            createModelElement(
                newScope,
                modelPackage
            );

            // Nested modelPackage
            // modelPackage != null --> new model package
            modelPackage = null;

            // is_unit
            if("is_unit".equals(lexer.peekToken())) {
                lexer.assertToken("is_unit");
                lexer.getToken();
                lexer.assertToken("is_loaded");
                String isLoaded = lexer.getToken();

                // external unit --> load from file
                if("FALSE".equals(isLoaded)) {
                    lexer.assertToken("file_name");
                    String rawFileName = lexer.getToken();
                    StringBuffer tmp = new StringBuffer();
                    int pos = 0;
                    while(pos < rawFileName.length()) {
                        if((pos < rawFileName.length()-1) && (rawFileName.charAt(pos) == '\\') && (rawFileName.charAt(pos+1) == '\\')) {
                            tmp.append('/');
                            pos+=2;
                        }
                        else {
                            tmp.append(rawFileName.charAt(pos));
                            pos++;
                        }
                    }
                    String fileName = tmp.toString().replace('\\', '/');

                    // path starts with a pathMapSymbol
                    if(fileName.startsWith("$")) {

                        boolean found = false;
                        for(
                                Iterator i = this.pathMap.keySet().iterator();
                                i.hasNext();
                        ) {
                            String pathMapSymbol = (String)i.next();
                            String pathMapPath = (String)this.pathMap.get(pathMapSymbol);

                            if(fileName.startsWith(pathMapSymbol)) {
                                found = true;

                                if(!((String)newScope.get(newScope.size()-1)).startsWith("$tmp$")) {

                                    // scope is defined again in fileName --> do not use newScope            
                                    // exclude models with prefix $tmp$

                                    // local dir macro
                                    if("&".equals(pathMapPath)) {
                                        parseRoseObjects(
                                            new RoseLexer(lexer.currentDir() + "/" + fileName.substring(pathMapSymbol.length())),
                                            scope
                                        );
                                    }

                                    // take value from path map
                                    else if(fileName.startsWith(pathMapSymbol)) {
                                        parseRoseObjects(
                                            new RoseLexer(pathMapPath + "/" + fileName.substring(pathMapSymbol.length())),
                                            scope
                                        );
                                    }
                                }
                                break;
                            }
                        }

                        // path map symbol not defined
                        if(!found) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE, 
                                "undefined symbol in file name",
                                new BasicException.Parameter("file name", fileName),
                                new BasicException.Parameter("path map", this.pathMap)
                            );
                        }
                    }
                    else {
                        parseRoseObjects(
                            new RoseLexer(fileName),
                            newScope
                        );
                    }
                }

                // internal unit --> ModelPackage
                else {
                    modelPackage = new DataproviderObject(
                        toElementPath(
                            newScope,
                            (String)newScope.get(newScope.size()-1)
                        )
                    );
                    modelPackage.values(SystemAttributes.OBJECT_CLASS).add(
                        ModelAttributes.PACKAGE
                    );
                    modelPackage.values("isAbstract").add(new Boolean(false));
                    modelPackage.values("visibility").add(VisibilityKind.PUBLIC_VIS);
                }

            }

            // attributes
            if("attributes".equals(lexer.peekToken())) {
                lexer.assertToken("attributes");
                lexer.assertToken("(");
                lexer.assertToken("list");
                lexer.assertToken("Attribute_Set");
                while(!lexer.isRightParam()) {
                    lexer.assertToken("(");
                    lexer.assertToken("object");
                    lexer.assertToken("Attribute");
                    lexer.assertToken("tool");
                    /* String toolName = */ lexer.getToken();
                    lexer.assertToken("name");
                    /* String attrName = */ lexer.getToken();
                    lexer.assertToken("value");
//                  String attrValue = null;
                    if(lexer.isLeftParam()) {
                        lexer.assertToken("(");
                        lexer.assertToken("value");
                        lexer.assertToken("Text");
                        /* attrValue = */ lexer.getToken();
                        lexer.assertToken(")");
                    } 
                    else {
                        /* attrValue = */ lexer.getToken();
                    }
                    lexer.assertToken(")");
                }
                lexer.assertToken(")");
            }

            // documentation
            if("documentation".equals(lexer.peekToken())) {
                lexer.assertToken("documentation");
                if(modelPackage != null) {
                    modelPackage.values("annotation").add(lexer.getToken());
                }
            }

            // dump modelPackage
            if(modelPackage != null) {
                createModelElement(
                    newScope,
                    modelPackage
                );
            }

            // object attributes
            while(!lexer.isRightParam()) {
                /* String attributeName = */ lexer.getToken();
                parseRoseAttributeValue(
                    lexer,
                    newScope
                );        
            }
            lexer.assertToken(")");
        }

        // Association
        else if("Association".equals(objectType)) {
            parseRoseAssociation(
                lexer,
                scope
            );
        }

        // other types
        else {    
            if(lexer.isString()) {
                lexer.getToken();
            }
            if(lexer.peekToken().charAt(0) == '@') {
                lexer.getToken();
            }

            // object attributes
            while(!lexer.isRightParam()) {
                /* String attributeName = */ lexer.getToken();
                //SysLog.trace("parsing attribute " + attributeName);
                parseRoseAttributeValue(
                    lexer,
                    scope
                );        
            }
            lexer.assertToken(")");
        }

        //SysLog.trace("< parseRoseObject");
    }

    //---------------------------------------------------------------------------
    private void parseRoseObjects(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        while(lexer.isLeftParam()) {
            parseRoseObject(
                lexer,
                scope,
                true
            );
        }
    }

    //---------------------------------------------------------------------------
    public void process(
        ServiceHeader header,
        Dataprovider_1_0 target,
        String providerName
    ) throws ServiceException {

        this.header = header;
        this.target = target;
        this.providerName = providerName;
        this.beginImport();
        parseRoseObjects(
            new RoseLexer(this.dirName + "/" + this.mdlFileName),
            new ArrayList()
        );
        this.endImport();
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------

    private String dirName = null;
    private String mdlFileName = null;
    private Map pathMap = null;
    private PrintStream warnings = null;

}

//--- End of File -----------------------------------------------------------
