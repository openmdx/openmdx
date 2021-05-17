/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: StandardNamespaceConvention
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.base.json.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public class StandardNamespaceConvention implements Convention, NamespaceContext {
    private Map<?,?> xnsToJns = new HashMap<Object,Object>();
    private Map<String,Object> jnsToXns = new HashMap<>();
    private List<?> attributesAsElements;
    private List<Object> jsonAttributesAsElements;
    private boolean supressAtAttributes;
    private boolean ignoreNamespaces;
    private String attributeKey = "@";
    private TypeConverter typeConverter;
    private Set<?> primitiveArrayKeys;
    
    public StandardNamespaceConvention() {
        super();
        typeConverter = Configuration.newDefaultConverterInstance();
    }
    public StandardNamespaceConvention(Configuration config) {
        super();
        this.xnsToJns = config.getXmlToJsonNamespaces();
        this.attributesAsElements = config.getAttributesAsElements();
        this.supressAtAttributes = config.isSupressAtAttributes();
        this.ignoreNamespaces = config.isIgnoreNamespaces();
        this.attributeKey = config.getAttributeKey();
        this.primitiveArrayKeys = config.getPrimitiveArrayKeys();
        
        for (Iterator<?> itr = xnsToJns.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<?,?> entry = (Map.Entry<?,?>) itr.next();
            jnsToXns.put((String)entry.getValue(), entry.getKey());
        }
        
        jsonAttributesAsElements = new ArrayList<Object>();
        if (attributesAsElements != null) {
            for (Iterator<?> itr = attributesAsElements.iterator(); itr.hasNext();) {
                QName q = (QName) itr.next();
                jsonAttributesAsElements.add(createAttributeKey(q.getPrefix(), 
                                                                q.getNamespaceURI(), 
                                                                q.getLocalPart()));
            }
        }
        typeConverter = config.getTypeConverter();
    }

    /* (non-Javadoc)
     * @see org.codehaus.xson.mapped.Convention#processNamespaces(org.codehaus.xson.Node, org.json.JSONObject)
     */
    public void processAttributesAndNamespaces( Node n, JSONObject object ) throws JSONException {

        // Read in the attributes, and stop when there are no more
        for (Iterator<?> itr = object.keys(); itr.hasNext();) {
            String k = (String) itr.next();

            if ( this.supressAtAttributes ) {
                if ( k.startsWith( attributeKey ) ) {
                    k = k.substring( 1 );
                }
                if ( null == this.jsonAttributesAsElements ) {
                    this.jsonAttributesAsElements = new ArrayList<Object>();
                }
                if ( !this.jsonAttributesAsElements.contains( k ) ) {
                    this.jsonAttributesAsElements.add( k );
                }
            }

            if ( k.startsWith( attributeKey ) ) {
                Object o = object.opt( k );
                //                String value = object.optString( k );
                k = k.substring( 1 );
                if ( k.equals( "xmlns" ) ) {
                    // if its a string its a default namespace
                    if ( o instanceof JSONObject ) {
                        JSONObject jo = (JSONObject) o;
                        for (Iterator<?> pitr = jo.keys(); pitr.hasNext();) {
                            // set namespace if one is specified on this attribute.
                            String prefix = (String) pitr.next();
                            String uri = jo.getString( prefix );

                            //                            if ( prefix.equals( "$" ) ) {
                            //                                prefix = "";
                            //                            }

                            n.setNamespace( prefix, uri );
                        }
                    }
                }
                else {
                    String strValue = o == null ? null : o.toString();
                    QName name = null;
                    // note that a non-prefixed attribute name implies NO namespace,
                    // i.e. as opposed to the in-scope default namespace
                    if ( k.contains( "." ) ) {
                        name = createQName( k, n );
                    }
                    else {
                        name = new QName( XMLConstants.DEFAULT_NS_PREFIX, k );
                    }
                    n.setAttribute( name, strValue );
                }
                itr.remove();
            }
            else {
                // set namespace if one is specified on this attribute.
                int dot = k.lastIndexOf( '.' );
                if ( dot != -1 ) {
                    String jns = k.substring( 0, dot );
                    String xns = getNamespaceURI( jns );
                    n.setNamespace( "", xns );
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI( String prefix ) {

        if ( ignoreNamespaces ) {
            return "";
        }
        else {
            return (String) jnsToXns.get( prefix );
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix( String namespaceURI ) {

        if ( ignoreNamespaces ) {
            return "";
        }
        else {
            return (String) xnsToJns.get( namespaceURI );
        }
    }

    public Iterator<String> getPrefixes( String arg0 ) {

        if ( ignoreNamespaces ) {
            return Collections.<String>emptySet().iterator();
        }
        else {
            return jnsToXns.keySet().iterator();
        }
    }

    public QName createQName( String rootName, Node node ) {

        return createQName( rootName );
    }

//    private void readAttribute( Node n, String k, JSONArray array ) throws JSONException {
//
//        for (int i = 0; i < array.length(); i++) {
//            readAttribute( n, k, array.getString( i ) );
//        }
//    }
//
//    private void readAttribute( Node n, String name, String value ) throws JSONException {
//
//        QName qname = createQName( name );
//        n.getAttributes().put( qname, value );
//    }

    private QName createQName( String name ) {

        int dot = name.lastIndexOf( '.' );
        QName qname = null;
        String local = name;

        if ( dot == -1 ) {
            dot = 0;
        }
        else {
            local = local.substring( dot + 1 );
        }

        String jns = name.substring( 0, dot );
        String xns = getNamespaceURI( jns );

        if ( xns == null ) {
            qname = new QName( name );
        }
        else {
            qname = new QName( xns, local );
        }

        return qname;
    }

    public String createAttributeKey( String p, String ns, String local ) {

        StringBuilder builder = new StringBuilder();
        if ( !this.supressAtAttributes ) {
            builder.append( attributeKey );
        }
        String jns = getJSONNamespace(p, ns);
        //        String jns = getPrefix(ns);
        if ( jns != null && jns.length() != 0 ) {
            builder.append( jns ).append( '.' );
        }
        return builder.append( local ).toString();
    }

    private String getJSONNamespace(String providedPrefix, String ns ) {

        if ( ns == null || ns.length() == 0 || ignoreNamespaces )
            return "";

        String jns = (String) xnsToJns.get( ns );
        if (jns == null && providedPrefix != null && providedPrefix.length() > 0) {
        	jns = providedPrefix;
        }
        if ( jns == null ) {
            throw new IllegalStateException( "Invalid JSON namespace: " + ns );
        }
        return jns;
    }

    public String createKey( String p, String ns, String local ) {

        StringBuilder builder = new StringBuilder();
        String jns = getJSONNamespace(p, ns);
        //        String jns = getPrefix(ns);
        if ( jns != null && jns.length() != 0 ) {
            builder.append( jns ).append( '.' );
        }
        return builder.append( local ).toString();
    }

    public boolean isElement( String p, String ns, String local ) {

        if ( attributesAsElements == null )
            return false;

        for (Iterator<?> itr = attributesAsElements.iterator(); itr.hasNext();) {
            QName q = (QName) itr.next();

            if ( q.getNamespaceURI().equals( ns ) && q.getLocalPart().equals( local ) ) {
                return true;
            }
        }
        return false;
    }

    public Object convertToJSONPrimitive( String text ) {

        return typeConverter.convertToJSONPrimitive( text );
    }

    public Set<?> getPrimitiveArrayKeys() {
		return primitiveArrayKeys;
	}
}
