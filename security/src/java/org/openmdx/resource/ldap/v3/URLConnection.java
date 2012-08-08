/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: URLConnection.java,v 1.5 2009/03/08 18:52:20 wfro Exp $
 * Description: URL Connection 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.resource.ResourceException;
import javax.resource.spi.CommException;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPAttributeSet;
import netscape.ldap.LDAPConstraints;
import netscape.ldap.LDAPControl;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPEntryComparator;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPExtendedOperation;
import netscape.ldap.LDAPModification;
import netscape.ldap.LDAPModificationSet;
import netscape.ldap.LDAPSearchConstraints;
import netscape.ldap.LDAPSearchResults;

import org.openmdx.resource.ldap.cci.Connection;

/**
 * URL Connection
 */
class URLConnection
    implements Connection {

	/**
	 * Constructor
	 * 
	 * @param source
	 * @param distinguishedNamePattern
	 * @param attributePattern
	 * @param caseSensitive TODO
	 * @throws LDAPException
	 */
	URLConnection(
		URL source,
		Pattern distinguishedNamePattern,
		Pattern attributePattern,
		Pattern commentPattern, 
		boolean caseSensitive
	) throws ResourceException {
		this.caseSensitive = caseSensitive;
		try {
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					source.openStream()
				)
			);
			LDAPEntry entry = null;
			for(
				String line = reader.readLine();
				line != null;
				line = reader.readLine()
			){
				Matcher matcher = commentPattern.matcher(line); 
				if(!matcher.matches()) {
					matcher = distinguishedNamePattern.matcher(line); 
					if(matcher.matches()) {
						String name = matcher.group(1).trim();
						entry = new LDAPEntry(name);
						this.directory.put(
							this.toKey(name), entry
						);
					} 
					else if (entry != null) {
						matcher = attributePattern.matcher(line);
						if(matcher.matches()) {
							String name = matcher.group(1).trim();
							String value = matcher.group(2).trim();
							LDAPAttribute attribute = entry.getAttribute(name);
							if(attribute == null) {
								attribute = new LDAPAttribute(name, value);
								entry.getAttributeSet().add(attribute);
							} 
							else {
								attribute.addValue(value);
							}
						}
					}
				}
			}
		} 
		catch (IOException exception) {
			throw new CommException(
				"Population failed",
				exception
			);
		}
	}
	
    private boolean endsWith(
    	List<String> left,
    	List<String> right
    ){
    	if(left == null) {
    		return right == null || right.isEmpty();
    	} 
    	else if (right == null) {
    		return left == null || left.isEmpty();
    	} 
    	else {
			if(left.size() < right.size()) {
				return false;
			} 
			else {
				for(
					int i = right.size(), j = left.size();
					i > 0;
				){
					if(!this.equals(left.get(--j), right.get(--i))) {
						return false;
					}
				}
				return true;
			}
    	}
    }
    
    private boolean equals(
    	String left,
    	String right
    ){
    	return 
    		left == null ? right == null :
    		this.caseSensitive ? left.equals(right) :
    		left.equalsIgnoreCase(right);
    }
            
	private Map<String,List<String>> toKey(
		String entryName
	){
		Map<String,List<String>> key = new HashMap<String,List<String>>();
		for(String component : entryName.split(",")){
			String[] nameValue = component.split("=");
			List<String> values = key.get(nameValue[0]);
			if(values == null) {
				values = new ArrayList<String>();
				key.put(nameValue[0].trim(), values);
			}
			values.add(nameValue[1].trim());
		}
		return key;
	}
	
	private final Map<Map<String,List<String>>, LDAPEntry> directory = new HashMap<Map<String,List<String>>, LDAPEntry>();

	private final boolean caseSensitive;
	
	//------------------------------------------------------------------------
	// Imeplements Closeable
	//------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		// Does nothing at the moment
	}
	
	
	//------------------------------------------------------------------------
	// Imeplements LDAPv3
	//------------------------------------------------------------------------

    public void abandon(LDAPSearchResults arg0) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void add(LDAPEntry arg0, LDAPConstraints arg1) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void add(LDAPEntry arg0) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void authenticate(int arg0, String arg1, String arg2)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void authenticate(String arg0, String arg1) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void bind(int arg0, String arg1, String arg2) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void bind(String arg0, String arg1) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public boolean compare(String arg0, LDAPAttribute arg1, LDAPConstraints arg2)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public boolean compare(String arg0, LDAPAttribute arg1)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void connect(int arg0, String arg1, int arg2, String arg3,
			String arg4) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void connect(String arg0, int arg1, String arg2, String arg3)
			throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void connect(String arg0, int arg1) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void delete(String arg0, LDAPConstraints arg1) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void delete(String arg0) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void disconnect() throws LDAPException {
	}

	public LDAPExtendedOperation extendedOperation(LDAPExtendedOperation arg0)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public Object getOption(int arg0) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public LDAPControl[] getResponseControls() {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void modify(String arg0, LDAPModification arg1, LDAPConstraints arg2)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void modify(String arg0, LDAPModification arg1) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void modify(String arg0, LDAPModificationSet arg1,
			LDAPConstraints arg2) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void modify(String arg0, LDAPModificationSet arg1)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public LDAPEntry read(String arg0, String[] arg1, LDAPSearchConstraints arg2)
			throws LDAPException {
		// TODO check whether the constraints can be ignored or not
    	return this.read(arg0, arg1);
	}

	public LDAPEntry read(String arg0, String[] arg1) throws LDAPException {
		// TODO remove attributes in excess
		return this.read(arg0);
	}

	public LDAPEntry read(String arg0) throws LDAPException {
		return this.directory.get(this.toKey(arg0));
	}

	public void rename(String arg0, String arg1, boolean arg2,
			LDAPConstraints arg3) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void rename(String arg0, String arg1, boolean arg2)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void rename(String arg0, String arg1, String arg2, boolean arg3,
			LDAPConstraints arg4) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public void rename(String arg0, String arg1, String arg2, boolean arg3)
			throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

	public LDAPSearchResults search(
		String base, 
		int scope, 
		String filter,
		String[] atrs, 
		boolean atrsOnly, 
		LDAPSearchConstraints constraints
	) throws LDAPException {
		// TODO check whether the constraints can be ignored 
		return this.search(
			base, 
			scope, 
			filter,
			atrs, 
			atrsOnly
		);
	}

	@SuppressWarnings("unchecked")
	public LDAPSearchResults search(
		String base, 
		int scope, 
		String filter,
		String[] atrs, 
		boolean atrsOnly
	) throws LDAPException {
		// TODO remove attributes or attribute values in excess
		List<LDAPEntry> entries = new ArrayList<LDAPEntry>();
		Map<String,List<String>> baseFilter = this.toKey(base);
		String[][] attributeFilter = this.toFilter(filter);
		Entries: for(Map.Entry<Map<String,List<String>>, LDAPEntry> entry : this.directory.entrySet()) {
			for(Map.Entry<String,List<String>> predicate : baseFilter.entrySet()) {
				List<String> values = entry.getKey().get(predicate.getKey());
				if(!this.endsWith(values, predicate.getValue())) continue Entries;
			}
			LDAPAttributeSet attributes = entry.getValue().getAttributeSet();
			AttributeFilter: for(String[] predicate : attributeFilter) {
				LDAPAttribute a = attributes.getAttribute(predicate[0]);
				if(a != null) {
					for(
						Enumeration<String> e = a.getStringValues();
						e.hasMoreElements();
					){
						if(this.equals(e.nextElement(),predicate[1])) continue AttributeFilter;
					}
				}
				continue Entries;
			}
			entries.add(entry.getValue());
		}
		return new SearchResults(entries);
	}

	private String[][] toFilter(
		String filter
	){
		if(filter == null) {
			return new String[][]{};
		} 
		else if(
			filter.startsWith("(&(") &&
			filter.endsWith("))")
		){
			String[] ands = filter.substring(3, filter.length() - 2).split("\\)\\(");
			String[][] reply = new String[ands.length][];
			for(
				int i = 0;
				i < ands.length;
				i++
			){
				String source = ands[i];
				String[] target = reply[i] = new String[2];
				int e = source.indexOf('=');
				target[0] = source.substring(0, e);
				target[1] = source.substring(e + 1);
			}
			return reply;
		} 
		else {
			String[][] reply = new String[1][];
			String source = filter;
			String[] target = reply[0] = new String[2];
			int e = source.indexOf('=');
			target[0] = source.substring(0, e);
			target[1] = source.substring(e + 1);
			return reply;
		}
	}
	
	
	public void setOption(int arg0, Object arg1) throws LDAPException {
    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
    
	final static private String MANAGED_ENVIRONMENT = 
		"This method is not supported in a managed environment";
	
	static class SearchResults extends LDAPSearchResults{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2984609947887815202L;

		/**
		 * 
		 */
		private final List<LDAPEntry> entries;
		
		SearchResults(
			List<LDAPEntry> entries
		){
			this.entries = entries;
		}

		@Override
		public int getCount() {
			return this.entries.size();
		}

		@Override
		public boolean hasMoreElements() {
			return !this.entries.isEmpty();
		}

		@Override
		public LDAPEntry next() throws LDAPException {
			return this.entries.remove(0);
		}

		@Override
		public Object nextElement() {
			try {
				return this.next();
			} 
			catch (LDAPException e) {
				return e;
			}
		}

		@Override
		public synchronized void sort(LDAPEntryComparator arg0) {
	    	throw new UnsupportedOperationException("Not yet implemented"); // TODO
		}
		
		
	}

}
