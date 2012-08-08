/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ElementFilter.java,v 1.6 2005/11/20 16:41:14 hburger Exp $
 * Description: element Filter
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/11/20 16:41:14 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.tools.ant.filters;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.util.regexp.RegexpFactory;
import org.apache.tools.ant.util.regexp.RegexpMatcher;

/**
 * Element Filter
 */
public class ElementFilter 
	extends BaseParamFilterReader 
	implements ChainableReader 
{

	/**
	 * Constructor
	 */
	public ElementFilter() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param in
	 */
	public ElementFilter(Reader in) {
		super(in);
	}

	/**
	 * 
	 */
    private ElementFilterSet filterSet = new ElementFilterSet();
    
	/**
	 * 
	 */
    private Vector elements = new Vector();

    /**
     * 
     */
    private Map mapping = null;
    	
	protected static final byte CONTENT = 0;
	protected static final byte ELEMENT = 1;
	protected static final byte COMMENT = 2;	
	protected static final byte CDATA   = 3;
	private int state = CONTENT;
	private StringBuffer element = new StringBuffer();
	private String content = null;
	private int contentPos;
	private char c1 = '\0';
	private char c2 = '\0';
	
	private static final String CDATA_START = "![CDATA[";
	private static final String COMMENT_START = "!--";
	private static final String ID_PATTERN;
	
	private static final RegexpFactory regexpFactory = new RegexpFactory();
	RegexpMatcher regexpMatcher = null;
	
	/**
     * Return the entries added explicitely to thos <code>IdFilter</code>.
     * 
     * @return a <code>Vector</code> fo
     */
    protected Vector getElements(){
    	return this.elements;
    }
    
    /**
	 * Element filter ignores begin end end tokens
	 * 
	 * @return the mapping
	 */
	protected synchronized Map getMapping(
	){
		if(!getInitialized()){
			this.mapping = this.filterSet.getMapping(
				getParameters(),
				getElements()
			);
			setInitialized(true);
		}
		return this.mapping;		
	}
	
	/**
	 * Set the mapping
	 * 
	 * @param mapping
	 */
	protected void setMapping(
		Map mapping 
	){
		this.mapping = mapping;
	}

	/**
     * Adds a filterset.
     * @return a filter set object
     */
    public FilterSet createFilterSet() {
        FilterSet filterSet = new FilterSet();
        this.filterSet.addFilterSet(filterSet);
        return filterSet;
    }

    /**
     * Adds an element entry.
     * @return an <code>Element</code> object
     */
    public Element createElement() {
        Element element = new Element();
        this.elements.add(element);
        return element;
    }
    
    
    //------------------------------------------------------------------------
    // Implements ChainableReader
    //------------------------------------------------------------------------

    /**
     * Creates a new HeadFilter using the passed in
     * Reader for instantiation.
     *
     * @param rdr A Reader object providing the underlying stream.
     *            Must not be <code>null</code>.
     *
     * @return a new filter based on this configuration, but filtering
     *         the specified reader
     */
    public final Reader chain(final Reader rdr) {
        ElementFilter newFilter = new ElementFilter(rdr);
        newFilter.setMapping(getMapping());
        newFilter.setInitialized(true);
        return newFilter;
    }

    
    //------------------------------------------------------------------------
    // Extends BaseFilterReader
    //------------------------------------------------------------------------
    
    /**
     * Read a single character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read() throws IOException {    	
    	int c0;
    	if(this.content == null) {
    		c0 = super.read();
    	} else if (this.contentPos < this.content.length()) {
    		c0 = this.content.charAt(this.contentPos++);
    	} else {
    		c0 = '<';
    		this.content = null;
    	}
		switch (this.state) {
			case CONTENT:
				if(c0 == '<') {
					this.element.setLength(0);
					this.state = ELEMENT;
				}
				break;
			case ELEMENT:
				if(c0 == '>') {
					if(this.regexpMatcher == null) {
						this.regexpMatcher = regexpFactory.newRegexpMatcher(getProject());
						this.regexpMatcher.setPattern(ID_PATTERN);
					}
					Vector v = this.regexpMatcher.getGroups(this.element.toString());
					if( v != null) {
						String key = (String) v.get(1);
						if(key != null && key.length() > 2) {
							key = key.substring(1, key.length() - 1);
							String content = (String) getMapping().get(key);
							if(content != null) {
								for(
									int i = super.read();
									i != '<';
									i = super.read()
								) if (i < 0) return -1;
								this.content = content;
								this.contentPos = 0;
							}
						}
					}
					this.state = CONTENT;
				} else {
	    			this.element.append((char)c0);
	    			if(elementStartsWith(COMMENT_START)) {
	    				this.state = COMMENT;
	    			} else if (elementStartsWith(CDATA_START)) {
	    				this.state = CDATA;
	    			}
				}
				break;
			case COMMENT:
				if(
					c0 == '>' &&
					this.c1 == '-' && 
					this.c2 == '-'
				) {
					this.state = CONTENT;
				} else {
		    		this.c2 = this.c1;
		    		this.c1 = (char)c0;
				}
	    		break;
			case CDATA:
				if(
					c0 == '>' &&
					this.c1 == ']' && 
					this.c2 == ']'
				) {
					this.state = CONTENT;
				} else {
		    		this.c2 = this.c1;
		    		this.c1 = (char)c0;
				}
	    		break;
		}
    	return c0;
    }
    
    private final boolean elementStartsWith(
    	String tag
    ){
    	return tag.length() == this.element.length() &&
    		tag.equals(this.element.toString());    	
    }
    
    
    //------------------------------------------------------------------------
    // Class ElementFilterSet
    //------------------------------------------------------------------------
    
    static final class ElementFilterSet extends FilterSet {
    	
        private Vector filterSets = new Vector();

        private Map mapping;

        synchronized Map getMapping(
        	Parameter[] parameters,
        	Vector entries
        ){
        	this.mapping = new HashMap();
        	for(
        		Enumeration e = filterSets.elements();
        		e.hasMoreElements();
            ) this.addConfiguredFilterSet((FilterSet)e.nextElement());
        	if(parameters != null) for(
        		int i = 0; 
        		i < parameters.length;
        		i++
            ) addFilter(
            	parameters[i].getName(),
            	parameters[i].getValue()            	
            );
        	if(entries != null) for(
        		Enumeration e = entries.elements();
        		e.hasMoreElements();
        	){
        		Element f = (Element) e.nextElement();
        		addFilter(
        			f.getId(),
        			f.getContent()
        	    );
        	}
        	return Collections.unmodifiableMap(this.mapping);
    	}
    	
        /**
         * Add a Filterset to this filter set.
         *
         * @param filterSet the filterset to be added to this filterset
         */
        void addFilterSet(FilterSet filterSet) {
        	this.filterSets.add(filterSet);
        }

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.types.FilterSet#addFilter(org.apache.tools.ant.types.FilterSet.Filter)
		 */
		public synchronized void addFilter(Filter filter) {
			addFilter(filter.getToken(), filter.getValue());
		}

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.types.FilterSet#addFilter(java.lang.String, java.lang.String)
		 */
		public synchronized void addFilter(String token, String value) {
			this.mapping.put(token, value);
		}
        
    }

    
    //------------------------------------------------------------------------
    // Class Element
    //------------------------------------------------------------------------

    /**
     * An element object is composed of id and content
     */
    public static final class Element {

	    private String id = null;
	    private String content = null;

	    public final void setId(final String id) {
	        this.id = id;
	    }

	    public final void setContent(final String content) {
	        this.content = content;
	    }

	    public final String getId() {
	        return id;
	    }

	    public final String getContent() {
	        return content;
	    }
	    
    }

    
	//------------------------------------------------------------------------
	// Provide Patterns
	//------------------------------------------------------------------------

	static {
		String whitespace = "[ \n\r\t]";
		String optionalWhitespace = whitespace + '*';
		String mandatoryWhitespace = whitespace + '+';
		String value = optionalWhitespace + "=" + optionalWhitespace + 
			"('[^']*'|\"[^\"]*\")";
		ID_PATTERN =  mandatoryWhitespace + "id" + value;
	}

}
