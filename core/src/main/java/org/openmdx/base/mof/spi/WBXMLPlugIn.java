/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XMI WBXML Plug-In 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.mof.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.wbxml.CodeResolution;
import org.openmdx.base.wbxml.CodeToken;
import org.openmdx.base.wbxml.StandardPlugIn;
import org.openmdx.kernel.exception.BasicException;

/**
 * XMI WBXML Plug-In 
 */
public class WBXMLPlugIn extends StandardPlugIn {

    /**
     * Constructor 
     */
    public WBXMLPlugIn(
    ){
        this("UTF-8");
    }

    /**
     * Constructor 
     *
     * @param stringEncoding
     */
    public WBXMLPlugIn(
        String stringEncoding
    ){
        super(stringEncoding);
        reset();
    }

    private static final String[] INITIAL_STRING_TABLE_ENTRIES ={
        "composite",
        "default",
        "false",
        "id",
        "identity",
        "instance_level",
        "list",
        "map",
        "none",
        "public_vis",
        "query",
        "root",
        "shared",
        "sparsearray",
        "stream",
        "true",
        "0..1",
        "0..n",
        "1..1",
        "200"
    };
    
    /**
     * The AirSync tag tables
     */
    private static final String[][] TAGS = {
        { // 0: technical
            "_object", 
            "_content", 
            "_item"
        }, { // 1: non-derived features
            "aggregation", 
            "container", 
            "direction", 
            "element", 
            "exception",
            "exposedEnd", 
            "isAbstract", 
            "isChangeable",
            "isDerived",
            "isNavigable", 
            "isQuery", 
            "isSingleton", 
            "maxLength",
            "multiplicity",
            "provider",
            "qualifierName", 
            "qualifierType", 
            "referencedEnd",
            "segment",
            "scope", 
            "stereotype", 
            "supertype", 
            "tagValue", 
            "type", 
            "visibility", 
            "semantics" // for upward compatibility
        }, { // 2: derived features
            "allFeature",
            "allFeatureWithSubtype",
            "allSubtype",
            "allSupertype",
            "attribute",
            "compositeReference",
            "content",
            "createdAt",
            "createdBy",
            "dereferencedType",
            "feature",
            "field",
            "identity",
            "modifiedAt",
            "modifiedBy",
            "name",
            "operation",
            "parameter",
            "qualifiedName",
            "reference",
            "referencedEndIsNavigable",
            "subtype"
        }, { // 3: well known classes            
            "org.openmdx.base.Authority",
            "org.openmdx.base.Provider", 
            "org.omg.model1.AliasType", 
            "org.omg.model1.Association", 
            "org.omg.model1.AssociationEnd", 
            "org.omg.model1.Attribute", 
            "org.omg.model1.Class", 
            "org.omg.model1.CollectionType", 
            "org.omg.model1.Constant",
            "org.omg.model1.Constraint", 
            "org.omg.model1.EnumerationType", 
            "org.omg.model1.Exception", 
            "org.omg.model1.Import", 
            "org.omg.model1.Operation", 
            "org.omg.model1.Package", 
            "org.omg.model1.Parameter", 
            "org.omg.model1.PrimitiveType", 
            "org.omg.model1.Reference", 
            "org.omg.model1.Segment", 
            "org.omg.model1.StructureField", 
            "org.omg.model1.StructureType",
            "org.omg.model1.Tag"
        }
    };
    
    private static final String[][] ATTRIBUTE_STARTS = {
        { // xmi
            "qualifiedName=Mof",
            "qualifiedName",
            "name=org:omg:model1",
            "name",
            "href",
            "_key"
        }, { // xml  
            "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance"
        }, { // xsi
            "noNamespaceSchemaLocation=xri://+resource/org/omg/model1/xmi1/model1.xsd"
        }
    };
    
    static final String[] NAMESPACE_URIS = {
        "xri://+resource/org/omg/model1/xmi1/model1.xsd", // 0
        "http://www.w3.org/2000/xmlns/", // 1
        "http://www.w3.org/2001/XMLSchema-instance" // 2
    };
       
    static final String[] NAMESPACE_PREFIXES = {
        "xmi", // 0
        "xml", // 1
        "xsi" // 2
    };

    /**
     * Maps local names to codes
     */
    @SuppressWarnings("unchecked")
    private static Map<String,Integer>[] _TAGS = new Map[TAGS.length];

    /**
     * Maps namespace URIs to code page numbers
     */
    static Map<String,Integer> _NAMESPACE_URIS = new HashMap<String,Integer>();

    /**
     * Maps namespace prefixes to code page numbers
     */
    static Map<String,Integer> _NAMESPACE_PREFIXES = new HashMap<String,Integer>();

    /**
     * 
     */
    private static final NamespaceContext namespaceContext = new NamespaceContext(){

        @Override
        public String getNamespaceURI(String prefix) {
            if(XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                return NAMESPACE_URIS[0];
            } else {
                Integer page = _NAMESPACE_PREFIXES.get(prefix);
                return page == null ? XMLConstants.NULL_NS_URI : NAMESPACE_URIS[page.intValue()];
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            Integer page = _NAMESPACE_URIS.get(namespaceURI);
            return page == null ? null : NAMESPACE_PREFIXES[page.intValue()];
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            return (prefix == null ? Collections.<String>emptySet() : Collections.<String>singleton(prefix)).iterator();
        }
        
    };
    
    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.StandardPlugIn#reset()
     */
    @Override
    public void reset() {
        super.reset();
        for(String stringValue : INITIAL_STRING_TABLE_ENTRIES) {
            super.getStringToken(stringValue);
        }
    }
    
    private static int getCodePage(
        String namespaceURI
    ){
        if(XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
            return 0;
        } else {
            Integer codePage = _NAMESPACE_URIS.get(namespaceURI);
            return codePage == null ?  -1 : codePage.intValue();
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getTagToken(java.lang.String)
     */
    @Override
    public CodeToken getTagToken(
        String namespaceURI, 
        String localName
    ) {
        int page = -1;
        Integer code = null;
        if(XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
            if(localName.startsWith("_")) {
                page = 0;
                code = _TAGS[page].get(localName);
            } else if (localName.indexOf('.') >= 0) {
                page = 3;
                code = _TAGS[page].get(localName);
            } else {
                page = 1;
                code = _TAGS[page].get(localName);
                if(code == null) {
                     page = 2;
                     code = _TAGS[page].get(localName);
                }
            }
        }
        return code == null ? null : new CodeToken(
            page << 8 | code.intValue(),
            localName.length(),
            false
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.TokenHandler#resolveTag(short, short)
     */
    @Override
    public CodeResolution resolveTag(
        int page, 
        int id
    ) throws ServiceException {
        try {
            String localName = TAGS[page][id - 5];
            return new CodeResolution (
                XMLConstants.NULL_NS_URI,
                localName,
                localName
            );
        } catch (IndexOutOfBoundsException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "No such tag entry",
                new BasicException.Parameter("page", page),
                new BasicException.Parameter("id", id)
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#resolveAttributeStart(int, int)
     */
    @Override
    public CodeResolution resolveAttributeStart(
        int page, 
        int id
    ) throws ServiceException {
        String entry = ATTRIBUTE_STARTS[page][id - 5];
        int separator = entry.indexOf('=');
        String localName;
        String valueStart;
        if(separator < 0){
            localName = entry;
            valueStart = "";
        } else {
            localName =  entry.substring(0, separator);
            valueStart = entry.substring(separator + 1);
        }
        String namespaceURI;
        String qName;
        if(page == 0) {
            namespaceURI = XMLConstants.NULL_NS_URI;
            qName = localName;
        } else {
            namespaceURI = NAMESPACE_URIS[page];
            qName = NAMESPACE_PREFIXES[page] + ':' + localName;
        }
        return new CodeResolution(
            namespaceURI,
            localName,
            qName,
            valueStart
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#findAttributeStartToken(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public CodeToken findAttributeStartToken(
        boolean force,
        String namespaceURI,
        String elementName,
        String attributeName,
        String value
    ){
        String nameValuePair = value == null  ? attributeName : (attributeName + '=' + value);
        int page = getCodePage(namespaceURI);
        if(page < 0) throw new RuntimeException("No code page for namespace '" + namespaceURI + "' found");
        String[] candidates = ATTRIBUTE_STARTS[page];
        for(int index = 0; index < candidates.length; index++){
            String candidate = candidates[index];
            if(nameValuePair.startsWith(candidate)){
                return new CodeToken(
                    page << 8 | index + 5 | 0x10000,
                    candidate.length(),
                    false // created
                );
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getDefaultDocumentPublicIdentifier()
     */
    @Override
    public String getDefaultDocumentPublicIdentifier(
        String defautNamespace
    ) {
        return "-//openMDX//XMI 1.0//EN";
    }

    static {
        for(
            int n = 0; 
            n < NAMESPACE_URIS.length; 
            n ++
        ){
            Integer i = Integer.valueOf(n); 
            _NAMESPACE_URIS.put(NAMESPACE_URIS[n], i);
            _NAMESPACE_PREFIXES.put(NAMESPACE_PREFIXES[n], i);
        }
        for(
            int page = 0; 
            page < TAGS.length; 
            page ++
        ){
            String[] tags = TAGS[page];
            Map<String,Integer> _tags = _TAGS[page] = new HashMap<String,Integer>();
            for(
                int code = 0; 
                code < tags.length; 
                code++
            ){
                _tags.put(tags[code], Integer.valueOf(code + 5));
            }
        }
    }

}
