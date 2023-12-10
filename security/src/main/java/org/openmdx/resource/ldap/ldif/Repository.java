/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LDIF Connection 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD licenseas listed below.
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
package org.openmdx.resource.ldap.ldif;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.resource.ResourceException;
import javax.resource.spi.CommException;

import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.ldap.codec.api.BinaryAttributeDetector;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.message.AbandonRequest;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.CompareRequest;
import org.apache.directory.api.ldap.model.message.CompareResponse;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.DeleteRequest;
import org.apache.directory.api.ldap.model.message.DeleteResponse;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyDnResponse;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.SaslRequest;

/**
 * LDAP repository
 */
class Repository implements LdapConnection {

	/**
	 * Constructor
	 * 
	 * @throws ResourceException
	 */
	Repository(
		URL source
	) throws ResourceException {
	    entries = new ArrayList<>();
	    try (LdifReader reader = new LdifReader(source.openStream())){
	        rootDse = createRootDse();
	        for(LdifEntry entry : reader) {
	            entries.add(entry);
	        }
        } catch (LdapException | IOException e) {
            throw new CommException("Unable to populate the LDAP repository from " + source, e);
        }
	}

	private final List<LdifEntry> entries;
	private final Entry rootDse;
	
	private static Entry createRootDse(
	) throws LdapException {
	    return new DefaultEntry("");
	}


	//------------------------------------------------------------------------
	// Implements LDAPConnection
	//------------------------------------------------------------------------

    @Override
    public void close(
    ){
        // Nothing to do
    }

    
    //------------------------------------------------------------------------
    // Implements LdapConnection
    //------------------------------------------------------------------------

//    @Override
//	@SuppressWarnings("unchecked")
//	public LDAPSearchResults search(
//		String base, 
//		int scope, 
//		String filter,
//		String[] atrs, 
//		boolean atrsOnly
//	) throws LdapException {
//		List<LDAPEntry> entries = new ArrayList<LDAPEntry>();
//		Map<String,List<String>> baseFilter = this.toKey(base);
//		String[][] attributeFilter = this.toFilter(filter);
//		Entries: for(Map.Entry<Map<String,List<String>>, LDAPEntry> entry : this.directory.entrySet()) {
//			for(Map.Entry<String,List<String>> predicate : baseFilter.entrySet()) {
//				List<String> values = entry.getKey().get(predicate.getKey());
//				if(!this.endsWith(values, predicate.getValue())) continue Entries;
//			}
//			LDAPAttributeSet attributes = entry.getValue().getAttributeSet();
//			AttributeFilter: for(String[] predicate : attributeFilter) {
//				LDAPAttribute a = attributes.getAttribute(predicate[0]);
//				if(a != null) {
//					for(
//						Enumeration<String> e = a.getStringValues();
//						e.hasMoreElements();
//					){
//						if(this.equals(e.nextElement(),predicate[1])) continue AttributeFilter;
//					}
//				}
//				continue Entries;
//			}
//			entries.add(entry.getValue());
//		}
//		return new SearchResults(entries);
//	}
//
//	private String[][] toFilter(
//		String filter
//	){
//		if(filter == null) {
//			return new String[][]{};
//		} else if(
//			filter.startsWith("(&(") &&
//			filter.endsWith("))")
//		){
//			String[] ands = filter.substring(3, filter.length() - 2).split("\\)\\(");
//			String[][] reply = new String[ands.length][];
//			for(
//				int i = 0;
//				i < ands.length;
//				i++
//			){
//				String source = ands[i];
//				String[] target = reply[i] = new String[2];
//				int e = source.indexOf('=');
//				target[0] = source.substring(0, e);
//				target[1] = source.substring(e + 1);
//			}
//			return reply;
//		} else if(
//			filter.startsWith("(") &&
//			filter.endsWith(")")
//		){
//			String[][] reply = new String[1][];
//			String source = filter;
//			String[] target = reply[0] = new String[2];
//			int e = source.indexOf('=');
//			target[0] = source.substring(1, e);
//			target[1] = source.substring(e + 1, source.length()-1);
//			return reply;
//		} else {
//			String[][] reply = new String[1][];
//			String source = filter;
//			String[] target = reply[0] = new String[2];
//			int e = source.indexOf('=');
//			target[0] = source.substring(0, e);
//			target[1] = source.substring(e + 1);
//			return reply;
//		}
//	}
	
    @Override
    public boolean isConnected(
    ) {
        return true;
    }

    @Override
    public boolean isAuthenticated(
    ) {
        return false;
    }

    @Override
    public boolean connect(
    ) throws LdapException {
        return true;
    }

    @Override
    public void add(
        Entry entry
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddResponse add(
        AddRequest addRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandon(
        int messageId
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandon(
        AbandonRequest abandonRequest
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(
    ) throws LdapException {
        // Nothing to do
    }

    @Override
    public void anonymousBind(
    ) throws LdapException {
        // Nothing to do
    }

    @Override
    public void bind(
        String name
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(
        String name,
        String credentials
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(
        Dn name
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(
        Dn name,
        String credentials
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BindResponse bind(
        BindRequest bindRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BindResponse bind(
        SaslRequest saslRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntryCursor search(
        Dn baseDn,
        String filter,
        SearchScope scope,
        String... attributes
    ) throws LdapException {
        return new Cursor(
            this.entries.stream(
            ).map(
                LdifEntry::getEntry
            ).filter(
                Predicates.fromName(baseDn, scope).and(Predicates.fromFilter(filter))
            ).collect(
                Collectors.toList()
            )
        );
    }

    @Override
    public EntryCursor search(
        String baseDn,
        String filter,
        SearchScope scope,
        String... attributes
    ) throws LdapException {
        return search(new Dn(baseDn), filter, scope, attributes);
    }

    @Override
    public SearchCursor search(
        SearchRequest searchRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unBind(
    ) throws LdapException {
        // Nothing to do
    }

    @Override
    public void setTimeOut(
        long timeOut
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void modify(
        Dn dn,
        Modification... modifications
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void modify(
        String dn,
        Modification... modifications
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void modify(
        Entry entry,
        ModificationOperation modOp
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModifyResponse modify(
        ModifyRequest modRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(
        String entryDn,
        String newRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(
        Dn entryDn,
        Rdn newRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(
        String entryDn,
        String newRdn,
        boolean deleteOldRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rename(
        Dn entryDn,
        Rdn newRdn,
        boolean deleteOldRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(
        String entryDn,
        String newSuperiorDn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(
        Dn entryDn,
        Dn newSuperiorDn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveAndRename(
        Dn entryDn,
        Dn newDn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveAndRename(
        String entryDn,
        String newDn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveAndRename(
        Dn entryDn,
        Dn newDn,
        boolean deleteOldRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveAndRename(
        String entryDn,
        String newDn,
        boolean deleteOldRdn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModifyDnResponse modifyDn(
        ModifyDnRequest modDnRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(
        String dn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(
        Dn dn
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeleteResponse delete(
        DeleteRequest deleteRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        String dn,
        String attributeName,
        String value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        String dn,
        String attributeName,
        byte[] value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        String dn,
        String attributeName,
        Value value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        Dn dn,
        String attributeName,
        String value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        Dn dn,
        String attributeName,
        byte[] value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compare(
        Dn dn,
        String attributeName,
        Value value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompareResponse compare(
        CompareRequest compareRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedResponse extended(
        String oid
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedResponse extended(
        String oid,
        byte[] value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedResponse extended(
        Oid oid
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedResponse extended(
        Oid oid,
        byte[] value
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedResponse extended(
        ExtendedRequest extendedRequest
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(
        String dn
    ) throws LdapException {
        return exists(new Dn(dn));
    }

    @Override
    public boolean exists(
        Dn dn
    ) throws LdapException {
        for(LdifEntry entry : entries) {
            if(dn.equals(entry.getDn())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Entry getRootDse(
    ) throws LdapException {
        return rootDse;
    }

    @Override
    public Entry getRootDse(
        String... attributes
    ) throws LdapException {
        return restrictAttributes(getRootDse());
    }

    @Override
    public Entry lookup(
        Dn dn
    ) throws LdapException {
        for(LdifEntry entry : entries) {
            if(dn.equals(entry.getDn())) {
                return entry.getEntry();
            }
        }
        throw new LdapNoSuchObjectException(
            "There is no obejct named " + dn
        );
    }

    @Override
    public Entry lookup(
        String dn
    ) throws LdapException {
        return lookup(new Dn(dn));
    }

    @Override
    public Entry lookup(
        Dn dn,
        String... attributes
    ) throws LdapException {
        return restrictAttributes(lookup(dn), attributes);
    }

    /**
     * Restriction is not really necessary, is it?
     */
    private static Entry restrictAttributes(
        Entry entry,
        String... attributes
    ){
        return entry;
    }

    @Override
    public Entry lookup(
        Dn dn,
        Control[] controls,
        String... attributes
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry lookup(
        String dn,
        String... attributes
    ) throws LdapException {
        return lookup(new Dn(dn), attributes);
    }

    @Override
    public Entry lookup(
        String dn,
        Control[] controls,
        String... attributes
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isControlSupported(
        String controlOID
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getSupportedControls(
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadSchema(
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadSchemaRelaxed(
    ) throws LdapException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaManager getSchemaManager(
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LdapApiService getCodecService(
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestCompleted(
        int messageId
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesFutureExistFor(
        int messageId
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BinaryAttributeDetector getBinaryAttributeDetector(
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinaryAttributeDetector(
        BinaryAttributeDetector binaryAttributeDetecter
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchemaManager(
        SchemaManager schemaManager
    ) {
        throw new UnsupportedOperationException();
    }

	public Throwable exceptionCaught() {
		return null;
	}

}
