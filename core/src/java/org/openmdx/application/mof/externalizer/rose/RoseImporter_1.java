/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: model.importer.RoseImporter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
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

import javax.resource.cci.MappedRecord;

import org.omg.mof.cci.DirectionKind;
import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.externalizer.spi.ModelImporter_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Reads and parses a rose MDL file. Creates the corresponding org::omg::model1
 * objects for objects defined in "Logical View", i.e. classes, attributes
 * and associations.
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class RoseImporter_1 extends ModelImporter_1 {

    //------------------------------------------------------------------------

    /*
     * Constructs a rose importer.
     *
     * @param header dataprovider service header.
     *,
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
    private MappedRecord parseRoseParameter(
        RoseLexer lexer,
        List scope,
        MappedRecord parameterType
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Parameter");

        String parameterName = lexer.getToken();

        // because Rose does not support multiplicity for parameters they are put
        // in front of the parameter name: <<x..>> parameterName
        StringBuffer multiplicity = new StringBuffer();
        parameterName = this.parseMultiplicity(
            parameterName,
            parameterType,
            parameterName,
            multiplicity
        );
        Object_2Facade parameterDefFacade;
        parameterDefFacade = Facades.newObject(
		    newFeaturePath(
		        Object_2Facade.getPath(parameterType),
		        parameterName
		    ),
		    ModelAttributes.STRUCTURE_FIELD
		);
        // container
        parameterDefFacade.addToAttributeValuesAsList("container",
            Object_2Facade.getPath(parameterType)
        );

        parameterDefFacade.addToAttributeValuesAsList("maxLength",Integer.valueOf(1000000));
        parameterDefFacade.addToAttributeValuesAsList("multiplicity",multiplicity.toString());

        // quid
        if("quid".equals(lexer.peekToken())) {
            lexer.assertToken("quid");
            lexer.getToken();
        }

        // documentation
        if("documentation".equals(lexer.peekToken())) {
            lexer.assertToken("documentation");
            parameterDefFacade.addToAttributeValuesAsList("annotation",
                lexer.getToken()
            );
        }

        // type
        if("type".equals(lexer.peekToken())) {
            lexer.assertToken("type");
            parameterDefFacade.addToAttributeValuesAsList("type",
                roseNameToPath(lexer.getToken())
            );
        }

        // quid
        if("quidu".equals(lexer.peekToken())) {
            lexer.assertToken("quidu");
            lexer.getToken();
        }

        lexer.assertToken(")");

        return parameterDefFacade.getDelegate();
    }

    //---------------------------------------------------------------------------
    private void parseRoseOperation(
        RoseLexer lexer,
        List scope,
        MappedRecord classDef
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Operation");

        String operationName = lexer.getToken();

        Object_2Facade operationDefFacade;
        operationDefFacade = Facades.newObject(
		    newFeaturePath(
		        Object_2Facade.getPath(classDef),
		        operationName
		    ),
		    ModelAttributes.OPERATION
		);
        // container
        operationDefFacade.addToAttributeValuesAsList("container",
            Object_2Facade.getPath(classDef)
        );

        // default values
        operationDefFacade.addToAttributeValuesAsList("isQuery",
            Boolean.FALSE
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
                        operationDefFacade.replaceAttributeValuesAsListBySingleton(
                            attrName,
                            Boolean.valueOf((String)attrValue)
                        );
                    }
                    else {
                        operationDefFacade.replaceAttributeValuesAsListBySingleton(
                            attrName,
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
            operationDefFacade.addToAttributeValuesAsList("annotation",
                lexer.getToken()
            );
        }

        // stereotype
        boolean isException = false;
        if("stereotype".equals(lexer.peekToken())) {
            lexer.assertToken("stereotype");
            operationDefFacade.addAllToAttributeValuesAsList("stereotype",
                this.parseStereotype(lexer.getToken())
            );
            // Stereotype <<exception>>
            if(operationDefFacade.attributeValuesAsListContains("stereotype",Stereotypes.EXCEPTION)) {
                operationDefFacade.getValue().setRecordName(
                    ModelAttributes.EXCEPTION
                );
                operationDefFacade.clearAttributeValuesAsList("stereotype");
                isException = true;
            }
        }
        
        // parameters
        if("parameters".equals(lexer.peekToken())) {

            /**
             * In openMDX all operations have exactly one parameter with name 'in'. The importer
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
            Object_2Facade parameterTypeFacade;
            parameterTypeFacade = Facades.newObject(
			    new Path(
			        Object_2Facade.getPath(classDef).toString() + capOperationName + "Params"
			    ),
			    ModelAttributes.STRUCTURE_TYPE
			);
            parameterTypeFacade.addToAttributeValuesAsList("visibility",VisibilityKind.PUBLIC_VIS);
            parameterTypeFacade.addToAttributeValuesAsList("isAbstract",Boolean.FALSE);
            parameterTypeFacade.addAllToAttributeValuesAsList(
                "container",
                Facades.asObject(classDef).getAttributeValuesAsReadOnlyList("container")
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
                MappedRecord parameterDef = this.parseRoseParameter(
                    lexer,
                    scope,
                    parameterTypeFacade.getDelegate()
                );
                /**
                 * Case 2: Parameter with name 'in'. Create object as PARAMETER.
                 */
                String fullQualifiedParameterName = Object_2Facade.getPath(parameterDef).getLastSegment().toClassicRepresentation();
                if("in".equals(fullQualifiedParameterName.substring(fullQualifiedParameterName.lastIndexOf(':') + 1))) {
                    // 'in' is the only allowed parameter
                    if(parametersCreated) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE,
                            new BasicException.Parameter("operation", operationDefFacade.getDelegate())
                        );              
                    }
                    parameterTypeFacade = Facades.newObject(
					    (Path)Facades.asObject(parameterDef).getSingletonFromAttributeValuesAsList("type")
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
                            new BasicException.Parameter("operation", operationDefFacade.getDelegate())
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
                    parameterTypeFacade.getDelegate()
                );
            }
            // in-parameter
            Object_2Facade inParameterDefFacade;
            inParameterDefFacade = Facades.newObject(
			    newFeaturePath(
			        operationDefFacade.getPath(),
			        "in"
			    ),
			    ModelAttributes.PARAMETER
			);
            inParameterDefFacade.addToAttributeValuesAsList("container",
                operationDefFacade.getPath()
            );  
            inParameterDefFacade.addToAttributeValuesAsList("direction",
                DirectionKind.IN_DIR
            );
            inParameterDefFacade.addToAttributeValuesAsList("multiplicity",
                "1..1"
            );
            inParameterDefFacade.addToAttributeValuesAsList("type",
                parameterTypeFacade.getPath()
            );
            this.createModelElement(
                scope,
                inParameterDefFacade.getDelegate()
            );
        }

        // void in-parameter
        else {
            Object_2Facade inParameterDefFacade;
            inParameterDefFacade = Facades.newObject(
			    newFeaturePath(
			        operationDefFacade.getPath(),
			        "in"
			    ),
			    ModelAttributes.PARAMETER
			);
            inParameterDefFacade.addToAttributeValuesAsList("container",
                operationDefFacade.getPath()
            );  
            inParameterDefFacade.addToAttributeValuesAsList("direction",
                DirectionKind.IN_DIR
            );
            inParameterDefFacade.addToAttributeValuesAsList("multiplicity",
                "1..1"
            );
            inParameterDefFacade.addToAttributeValuesAsList("type",
                roseNameToPath("org::openmdx::base::Void")
            );
            this.createModelElement(
                scope,
                inParameterDefFacade.getDelegate()
            );
        }

        // result
        Object_2Facade resultDefFacade;
        resultDefFacade = Facades.newObject(
		    newFeaturePath(
		        operationDefFacade.getPath(),
		        "result"
		    ),
		    ModelAttributes.PARAMETER
		);
        resultDefFacade.addToAttributeValuesAsList("container",
            operationDefFacade.getPath()
        );  
        resultDefFacade.addToAttributeValuesAsList("direction",
            DirectionKind.RETURN_DIR
        );
        resultDefFacade.addToAttributeValuesAsList("multiplicity",
            "1..1"
        );

        // result type
        if("result".equals(lexer.peekToken())) {
            lexer.assertToken("result");
            resultDefFacade.addToAttributeValuesAsList("type",
                this.roseNameToPath(
                    lexer.getToken()
                )
            );
        }
        else {
            resultDefFacade.addToAttributeValuesAsList("type",
                this.roseNameToPath(
                    "org:openmdx:base:Void"
                )
            );
        }

        // only create result for operations
        if(!isException) {      
            this.createModelElement(
                scope,
                resultDefFacade.getDelegate()
            );
        }

        // exceptions
        if("exceptions".equals(lexer.peekToken())) {
            lexer.assertToken("exceptions");
            StringTokenizer exceptions = new StringTokenizer(lexer.getToken(), ", ");
            while(exceptions.hasMoreTokens()) {
                String qualifiedExceptionName = exceptions.nextToken();
                String qualifiedClassName = qualifiedExceptionName.substring(0, qualifiedExceptionName.lastIndexOf(':'));
                operationDefFacade.addToAttributeValuesAsList("exception",
                    newFeaturePath(
                        this.roseNameToPath(
                            qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'))
                        ),
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
            operationDefFacade.addToAttributeValuesAsList("semantics",
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
        operationDefFacade.addToAttributeValuesAsList("visibility",
            VisibilityKind.PUBLIC_VIS
        );

        // scope
        operationDefFacade.addToAttributeValuesAsList("scope",
            ScopeKind.INSTANCE_LEVEL
        );

        this.createModelElement(
            scope,
            operationDefFacade.getDelegate()
        );

        lexer.assertToken(")");

    }

    //---------------------------------------------------------------------------
    private MappedRecord parseRoseAssociationEnd(
        RoseLexer lexer,
        List scope,
        MappedRecord associationDef
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("Role");
        String roleName = lexer.getToken();

        Object_2Facade associationEndDefFacade;
        associationEndDefFacade = Facades.newObject(
		    newFeaturePath(
		        Object_2Facade.getPath(associationDef),
		        roleName
		    ),
		    ModelAttributes.ASSOCIATION_END
		);

        // warning if assocation end is unnamed
        if(roleName.startsWith("$UNNAMED$")) {
            printWarning(
                "association end " + associationEndDefFacade.getPath() + " is not named"
            );
        }

        // container
        associationEndDefFacade.addToAttributeValuesAsList(
            "container",
            Object_2Facade.getPath(associationDef)
        );   

        // name
        associationEndDefFacade.addToAttributeValuesAsList(
            "name",
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
            associationEndDefFacade.addToAttributeValuesAsList(
                "annotation",
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
            associationEndDefFacade.addToAttributeValuesAsList(
                "type",
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
                associationEndDefFacade.addToAttributeValuesAsList(
                    "qualifierName",
                    lexer.getToken()
                );
                lexer.assertToken("quid");
                lexer.getToken();
                lexer.assertToken("type");
                associationEndDefFacade.addToAttributeValuesAsList(
                    "qualifierType",
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
            associationEndDefFacade.addToAttributeValuesAsList(
                "multiplicity",
                multiplicity.toString()
            );
            lexer.assertToken(")");
        }

        // isChangeable
        if("Constraints".equals(lexer.peekToken())) {
            lexer.assertToken("Constraints");
            String constraints = lexer.getToken();
            boolean constraintIsFrozen = "isFrozen".equals(constraints);
            associationEndDefFacade.addToAttributeValuesAsList(
                "isChangeable",
                Boolean.valueOf(!constraintIsFrozen)
            );
            if(!constraintIsFrozen) {
                // add new constraint
                Object_2Facade associationEndConstraintDefFacade;
                associationEndConstraintDefFacade = Facades.newObject(
				    newFeaturePath(
				        associationEndDefFacade.getPath(),
				        constraints
				    ),
				    ModelAttributes.CONSTRAINT
				);
                // container
                associationEndConstraintDefFacade.addToAttributeValuesAsList(
                    "container",
                    associationEndDefFacade.getPath()
                );   
                this.createModelElement(
                    scope,
                    associationEndConstraintDefFacade.getDelegate()
                );
            }
        }
        else {
            associationEndDefFacade.addToAttributeValuesAsList(
                "isChangeable",
                Boolean.TRUE
            );
        }

        // exportControl
        if("exportControl".equals(lexer.peekToken())) {
            lexer.assertToken("exportControl");
            lexer.getToken();
        }

        // aggregation (none = link)    
        associationEndDefFacade.addToAttributeValuesAsList(
            "aggregation",
            AggregationKind.NONE
        );
        if("Containment".equals(lexer.peekToken())) {
            lexer.assertToken("Containment");
            String containment = lexer.getToken();
            if("By Reference".equals(containment)) {
                associationEndDefFacade.replaceAttributeValuesAsListBySingleton(
                    "aggregation",
                    AggregationKind.SHARED
                );
            } else if("By Value".equals(containment)) {
                associationEndDefFacade.replaceAttributeValuesAsListBySingleton(
                    "aggregation",
                    AggregationKind.COMPOSITE
                );
            }
        }

        // isNavigable
        if("is_navigable".equals(lexer.peekToken())) {
            lexer.assertToken("is_navigable");
            associationEndDefFacade.replaceAttributeValuesAsListBySingleton(
                "isNavigable",
                Boolean.valueOf("TRUE".equals(lexer.getToken()))
            );
        }
        else { 
            associationEndDefFacade.replaceAttributeValuesAsListBySingleton(
                "isNavigable",
                Boolean.FALSE
            );
        }

        // is_aggregate
        if("is_aggregate".equals(lexer.peekToken())) {
            lexer.assertToken("is_aggregate");
            lexer.getToken();
        }

        lexer.assertToken(")");

        // Print aggregation!=NONE should be navigable
        if(!AggregationKind.NONE.equals(associationEndDefFacade.attributeValue("aggregation"))) {
            if(!((Boolean)associationEndDefFacade.attributeValue("isNavigable")).booleanValue()) {
                printWarning(
                    "Probably wrong value for isNavigable for " + 
                    associationEndDefFacade.getPath() +
                    "; association ends with aggregation 'composite|shared' should be navigable." 
                );
            }
        }
        return associationEndDefFacade.getDelegate();
    }

    //---------------------------------------------------------------------------
    private void parseRoseAttribute(
        RoseLexer lexer,
        List scope,
        MappedRecord classDef,
        boolean isStructureField
    ) throws ServiceException {

        lexer.assertToken("(");
        lexer.assertToken("object");
        lexer.assertToken("ClassAttribute");
        String attributeName = lexer.getToken();

        Object_2Facade attributeDefFacade;
        attributeDefFacade = Facades.newObject(
		    newFeaturePath(
		        Object_2Facade.getPath(classDef),
		        attributeName
		    ),
		    ModelAttributes.STRUCTURE_FIELD
		);

        if(isStructureField) {
            attributeDefFacade.getValue().setRecordName(
                ModelAttributes.STRUCTURE_FIELD
            );
        }
        else {
            attributeDefFacade.getValue().setRecordName(
                ModelAttributes.ATTRIBUTE
            );
        }

        // container
        attributeDefFacade.addToAttributeValuesAsList(
            "container",
            Object_2Facade.getPath(classDef)
        );

        // user-defined attributes. set default values for
        // maxLength, uniqueValues, isLanguageNeutral, isChangeable
        attributeDefFacade.addToAttributeValuesAsList("maxLength", Integer.valueOf(DEFAULT_ATTRIBUTE_MAX_LENGTH));
        attributeDefFacade.addToAttributeValuesAsList("isChangeable",Boolean.TRUE);

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
                        attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                            attrName,
                            Boolean.valueOf((String)attrValue)
                        );
                    }
                    else if ("maxLength".equals(attrName)) {
                        Integer intVal = null;
                        try {
                            intVal = Integer.valueOf((String)attrValue);
                        } 
                        catch(NumberFormatException ex) {
                            printWarning(
                                "Illegal number format for value for attribute " + 
                                attrName + 
                                " (using default value instead)"
                            );
                            intVal = Integer.valueOf(DEFAULT_ATTRIBUTE_MAX_LENGTH); 
                        }
                        attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                            "maxLength",
                            intVal
                        );
                    }
                    else {
                        attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                            attrName,
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
            attributeDefFacade.addToAttributeValuesAsList(
                "annotation",
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
            attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                "multiplicity",
                multiplicity.toString()
            );
        }
        else {
            attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                "multiplicity",
                "1..1"
            );
        }

        // type
        if("type".equals(lexer.peekToken())) {
            lexer.assertToken("type");
            attributeDefFacade.replaceAttributeValuesAsListBySingleton(
                "type",
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
            attributeDefFacade.addToAttributeValuesAsList(
                "visibility",
                VisibilityKind.PUBLIC_VIS
            );
        }
        else {
            attributeDefFacade.addToAttributeValuesAsList(
                "visibility",
                VisibilityKind.PRIVATE_VIS
            );
        }

        // isDerived
        if("derived".equals(lexer.peekToken())) {
            lexer.assertToken("derived");
            attributeDefFacade.addToAttributeValuesAsList(
                "isDerived",
                Boolean.valueOf("TRUE".equals(lexer.getToken()))
            );
        }
        else {
            attributeDefFacade.addToAttributeValuesAsList(
                "isDerived",
                Boolean.FALSE
            );
        }

        // scope
        attributeDefFacade.addToAttributeValuesAsList(
            "scope",
            ScopeKind.INSTANCE_LEVEL
        );

        lexer.assertToken(")");

        // remove non StructureField attributes
        if(isStructureField) {
            attributeDefFacade.clearAttributeValuesAsList("visibility");
            attributeDefFacade.clearAttributeValuesAsList("isDerived");
            attributeDefFacade.clearAttributeValuesAsList("scope");
            attributeDefFacade.clearAttributeValuesAsList("isChangeable");
        }

        this.createModelElement(
            scope,
            attributeDefFacade.getDelegate()
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
        Object_2Facade modelAssociationFacade;
        modelAssociationFacade = Facades.newObject(
		    toElementPath(
		        scope,
		        associationName
		    ),
		    ModelAttributes.ASSOCIATION
		);

        // container
        modelAssociationFacade.addToAttributeValuesAsList(
            "container",
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
            modelAssociationFacade.addToAttributeValuesAsList(
                "annotation",
                lexer.getToken()
            );
        }

        // association ends
        MappedRecord modelAssociationEnd1 = null;
        MappedRecord modelAssociationEnd2 = null;
        if("roles".equals(lexer.peekToken())) {
            lexer.assertToken("roles");
            lexer.assertToken("(");
            lexer.assertToken("list");
            lexer.assertToken("role_list");
            modelAssociationEnd1 = this.parseRoseAssociationEnd(
                lexer,
                scope,
                modelAssociationFacade.getDelegate()
            );
            modelAssociationEnd2 = this.parseRoseAssociationEnd(
                lexer,
                scope,
                modelAssociationFacade.getDelegate()
            );
            lexer.assertToken(")");
        }
        // isDerived
        if("derived".equals(lexer.peekToken())) {
            lexer.assertToken("derived");
            modelAssociationFacade.addToAttributeValuesAsList(
                "isDerived",
                Boolean.valueOf("TRUE".equals(lexer.getToken()))
            );
        }
        else {
            modelAssociationFacade.addToAttributeValuesAsList(
                "isDerived",
                Boolean.FALSE
            );
        }
        this.verifyAndCompleteAssociationEnds(
            modelAssociationEnd1,
            modelAssociationEnd2
        );
        this.exportAssociationEndAsReference(
            modelAssociationEnd1,
            modelAssociationEnd2,
            modelAssociationFacade.getDelegate(),
            scope
        );
        this.exportAssociationEndAsReference(
            modelAssociationEnd2,
            modelAssociationEnd1,
            modelAssociationFacade.getDelegate(),
            scope
        );
        // remove name attributes before setting
        Facades.asObject(modelAssociationEnd1).getValue().keySet().remove("name");
        this.createModelElement(
            scope,
            modelAssociationEnd1
        );
        Facades.asObject(modelAssociationEnd2).getValue().keySet().remove("name");
        this.createModelElement(
            scope,
            modelAssociationEnd2
        );
        // complete association
        modelAssociationFacade.addToAttributeValuesAsList("isAbstract",Boolean.FALSE);
        modelAssociationFacade.addToAttributeValuesAsList("visibility",VisibilityKind.PUBLIC_VIS);
        this.createModelElement(
            scope,
            modelAssociationFacade.getDelegate()
        );
        lexer.assertToken(")");
    }

    //---------------------------------------------------------------------------
    private void parseRoseClassifier(
        RoseLexer lexer,
        List scope
    ) throws ServiceException {

        String className = lexer.getToken();

        // ModelClass object
        Object_2Facade classifierDefFacade;
        classifierDefFacade = Facades.newObject(
		    toElementPath(
		        scope,
		        className
		    ),
		    ModelAttributes.CLASS
		);

        // container
        classifierDefFacade.addToAttributeValuesAsList(
            "container",
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
            classifierDefFacade.addToAttributeValuesAsList(
                "annotation",
                lexer.getToken()
            );
        }

        // stereotype
        boolean isStructureType = false;
        boolean isAliasType = false;

        if("stereotype".equals(lexer.peekToken())) {
            lexer.assertToken("stereotype");
            classifierDefFacade.addAllToAttributeValuesAsList(
                "stereotype",
                this.parseStereotype(lexer.getToken())
            );

            // handle well-known stereotypes
            if(classifierDefFacade.attributeValuesAsListContains("stereotype",Stereotypes.PRIMITIVE)) {
                SysLog.trace("changing type to " + ModelAttributes.PRIMITIVE_TYPE);
                classifierDefFacade.getValue().setRecordName(
                    ModelAttributes.PRIMITIVE_TYPE
                );
                classifierDefFacade.clearAttributeValuesAsList("stereotype");
            }
            else if(classifierDefFacade.attributeValuesAsListContains("stereotype",Stereotypes.STRUCT)) {
                isStructureType = true;
                SysLog.trace("changing type to " + ModelAttributes.STRUCTURE_TYPE);
                classifierDefFacade.getValue().setRecordName(
                    ModelAttributes.STRUCTURE_TYPE
                );
                classifierDefFacade.clearAttributeValuesAsList("stereotype");
            }
            else if(classifierDefFacade.attributeValuesAsListContains("stereotype",Stereotypes.ALIAS)) {
                isAliasType = true;
                SysLog.trace("changing type to " + ModelAttributes.ALIAS_TYPE);
                classifierDefFacade.getValue().setRecordName(
                    ModelAttributes.ALIAS_TYPE
                );
                classifierDefFacade.clearAttributeValuesAsList("stereotype");
            }
            else if(classifierDefFacade.attributeValuesAsListContains("stereotype","parameter")) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "Stereotype <<parameter>> is not supported anymore. Use stereotype <<" + Stereotypes.STRUCT + ">> instead",
                    new BasicException.Parameter("classifier", classifierDefFacade.getDelegate())
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
                classifierDefFacade.addToAttributeValuesAsList("supertype",
                    it.next()
                );
            }
        }
        // operations
        if("operations".equals(lexer.peekToken())) {
            // operations not allowed on STRUCTURE_TYPE and ALIAS_TYPE
            if(
                ModelAttributes.STRUCTURE_TYPE.equals(classifierDefFacade.getObjectClass()) ||
                ModelAttributes.ALIAS_TYPE.equals(classifierDefFacade.getObjectClass())
            ) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "operations not allowed on structure and alias types",
                    new BasicException.Parameter("classifier", classifierDefFacade.getDelegate())
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
                    classifierDefFacade.getDelegate()
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
                classifierDefFacade.addToAttributeValuesAsList(
                    "type",
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
                        classifierDefFacade.getDelegate(),
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
            classifierDefFacade.addToAttributeValuesAsList(
                "isAbstract",
                Boolean.valueOf(
                    "TRUE".equals(lexer.getToken())
                )
            );
        }
        else {
            classifierDefFacade.addToAttributeValuesAsList(
                "isAbstract",
                Boolean.FALSE
            );      
        }  

        // visibility
        classifierDefFacade.addToAttributeValuesAsList(
            "visibility",
            VisibilityKind.PUBLIC_VIS
        );

        // isSingleton
        classifierDefFacade.addToAttributeValuesAsList(
            "isSingleton",
            Boolean.FALSE
        );

        // usage
        if("used_nodes".equals(lexer.peekToken())) {
            lexer.assertToken("used_nodes");
            parseRoseObject(lexer, scope, false);
        }

        lexer.assertToken(")");

        // write object
        this.createModelElement(
            scope,
            classifierDefFacade.getDelegate()
        );

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
            Object_2Facade modelPackageFacade;
            modelPackageFacade = Facades.newObject(
			    toElementPath(
			        newScope,
			        (String)newScope.get(newScope.size()-1)
			    ),
			    ModelAttributes.PACKAGE
			);
            modelPackageFacade.addToAttributeValuesAsList("isAbstract",Boolean.FALSE);
            modelPackageFacade.addToAttributeValuesAsList("visibility",VisibilityKind.PUBLIC_VIS);
            createModelElement(
                newScope,
                modelPackageFacade.getDelegate()
            );

            // Nested modelPackage
            // modelPackage != null --> new model package
            modelPackageFacade = null;

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
                    modelPackageFacade = Facades.newObject(
					    toElementPath(
					        newScope,
					        (String)newScope.get(newScope.size()-1)
					    ),
					    ModelAttributes.PACKAGE
					);
                    modelPackageFacade.addToAttributeValuesAsList("isAbstract",Boolean.FALSE);
                    modelPackageFacade.addToAttributeValuesAsList("visibility",VisibilityKind.PUBLIC_VIS);
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
                if(modelPackageFacade != null) {
                    modelPackageFacade.addToAttributeValuesAsList("annotation",lexer.getToken());
                }
            }

            // dump modelPackage
            if(modelPackageFacade != null) {
                this.createModelElement(
                    newScope,
                    modelPackageFacade.getDelegate()
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
            this.parseRoseAssociation(
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
