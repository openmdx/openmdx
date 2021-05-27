/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LDAP Connection 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2019, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.spi;

import java.io.IOException;
import java.util.List;

import org.apache.directory.api.asn1.util.Oid;
import org.apache.directory.api.ldap.codec.api.BinaryAttributeDetector;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationNotSupportedException;
import org.apache.directory.api.ldap.model.exception.LdapException;
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
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.SaslRequest;
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException;
import org.apache.directory.ldap.client.template.exception.LdapRuntimeException;
import org.openmdx.resource.spi.AbstractConnection;

/**
 * LDAP Connection
 */
class Connection extends AbstractConnection implements LdapConnection {
    
    /**
     * Constructor 
     * 
     * @param ldapConnection the delegate
     */
    Connection(LdapConnection ldapConnection) {
        this.ldapConnection = ldapConnection;
    }

    private LdapConnection ldapConnection;
    
    /**
     * Use the JCA contract for authentication
     */
    private static final String AUTHENTICATION_NOT_SUPPORTED = "Authentication not supported in a managed environment";
    
    //------------------------------------------------------------------------
    // Implements AutoCloseable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws IOException{
        if(isOpen()) {
          try {
            this.ldapConnection.unBind();
            } catch (LdapException e) {
                throw new IOException("Unable to unbind", e);
            } finally {
              this.ldapConnection = null;  
              dissociateManagedConnection();
            }
        }
    }
    
    protected boolean isOpen() {
        return this.ldapConnection != null;
    }
    
    private LdapConnection getDelegate() throws LdapException {
        if(!isOpen()) throw new InvalidConnectionException("The connection is alreday closed");
        return this.ldapConnection;
    }

    
    //------------------------------------------------------------------------
    // Extends LdapConnectionWrapper
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind()
     */
    @Override
    public void bind(
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#anonymousBind()
     */
    @Override
    public void anonymousBind(
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(java.lang.String)
     */
    @Override
    public void bind(
        String name
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(java.lang.String, java.lang.String)
     */
    @Override
    public void bind(
        String name,
        String credentials
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(org.apache.directory.api.ldap.model.name.Dn)
     */
    @Override
    public void bind(
        Dn name
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(org.apache.directory.api.ldap.model.name.Dn, java.lang.String)
     */
    @Override
    public void bind(
        Dn name,
        String credentials
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(org.apache.directory.api.ldap.model.message.BindRequest)
     */
    @Override
    public BindResponse bind(
        BindRequest bindRequest
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#bind(org.apache.directory.ldap.client.api.SaslRequest)
     */
    @Override
    public BindResponse bind(
        SaslRequest saslRequest
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }


    /* (non-Javadoc)
     * @see org.apache.directory.ldap.client.api.LdapConnectionWrapper#unBind()
     */
    @Override
    public void unBind(
    ) throws LdapException {
        throw new LdapAuthenticationNotSupportedException(ResultCodeEnum.AUTH_METHOD_NOT_SUPPORTED, AUTHENTICATION_NOT_SUPPORTED);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#isConnected()
     */
    public boolean isConnected(
    ) {
        return isOpen() && ldapConnection.isConnected();
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#isAuthenticated()
     */
    public boolean isAuthenticated(
    ) {
        return isOpen() && ldapConnection.isAuthenticated();
    }

    /**
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#connect()
     */
    public boolean connect(
    ) throws LdapException {
        return getDelegate().connect();
    }

    /**
     * @param entry
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#add(org.apache.directory.api.ldap.model.entry.Entry)
     */
    public void add(
        Entry entry
    ) throws LdapException {
        getDelegate().add(entry);
    }

    /**
     * @param addRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#add(org.apache.directory.api.ldap.model.message.AddRequest)
     */
    public AddResponse add(
        AddRequest addRequest
    ) throws LdapException {
        return getDelegate().add(addRequest);
    }

    /**
     * @param messageId
     * @see org.apache.directory.ldap.client.api.LdapConnection#abandon(int)
     */
    public void abandon(
        int messageId
    ) {
        try {
            getDelegate().abandon(messageId);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param abandonRequest
     * @see org.apache.directory.ldap.client.api.LdapConnection#abandon(org.apache.directory.api.ldap.model.message.AbandonRequest)
     */
    public void abandon(
        AbandonRequest abandonRequest
    ) {
        try {
            getDelegate().abandon(abandonRequest);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param baseDn
     * @param filter
     * @param scope
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#search(org.apache.directory.api.ldap.model.name.Dn, java.lang.String, org.apache.directory.api.ldap.model.message.SearchScope, java.lang.String[])
     */
    public EntryCursor search(
        Dn baseDn,
        String filter,
        SearchScope scope,
        String... attributes
    ) throws LdapException {
        return getDelegate().search(baseDn, filter, scope, attributes);
    }

    /**
     * @param baseDn
     * @param filter
     * @param scope
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#search(java.lang.String, java.lang.String, org.apache.directory.api.ldap.model.message.SearchScope, java.lang.String[])
     */
    public EntryCursor search(
        String baseDn,
        String filter,
        SearchScope scope,
        String... attributes
    ) throws LdapException {
        return getDelegate().search(baseDn, filter, scope, attributes);
    }

    /**
     * @param searchRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#search(org.apache.directory.api.ldap.model.message.SearchRequest)
     */
    public SearchCursor search(
        SearchRequest searchRequest
    ) throws LdapException {
        return getDelegate().search(searchRequest);
    }

    /**
     * @param timeOut
     * @see org.apache.directory.ldap.client.api.LdapConnection#setTimeOut(long)
     */
    public void setTimeOut(
        long timeOut
    ) {
        try {
            getDelegate().setTimeOut(timeOut);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param dn
     * @param modifications
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#modify(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.entry.Modification[])
     */
    public void modify(
        Dn dn,
        Modification... modifications
    ) throws LdapException {
        getDelegate().modify(dn, modifications);
    }

    /**
     * @param dn
     * @param modifications
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#modify(java.lang.String, org.apache.directory.api.ldap.model.entry.Modification[])
     */
    public void modify(
        String dn,
        Modification... modifications
    ) throws LdapException {
        getDelegate().modify(dn, modifications);
    }

    /**
     * @param entry
     * @param modOp
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#modify(org.apache.directory.api.ldap.model.entry.Entry, org.apache.directory.api.ldap.model.entry.ModificationOperation)
     */
    public void modify(
        Entry entry,
        ModificationOperation modOp
    ) throws LdapException {
        getDelegate().modify(entry, modOp);
    }

    /**
     * @param modRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#modify(org.apache.directory.api.ldap.model.message.ModifyRequest)
     */
    public ModifyResponse modify(
        ModifyRequest modRequest
    ) throws LdapException {
        return getDelegate().modify(modRequest);
    }

    /**
     * @param entryDn
     * @param newRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#rename(java.lang.String, java.lang.String)
     */
    public void rename(
        String entryDn,
        String newRdn
    ) throws LdapException {
        getDelegate().rename(entryDn, newRdn);
    }

    /**
     * @param entryDn
     * @param newRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#rename(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.name.Rdn)
     */
    public void rename(
        Dn entryDn,
        Rdn newRdn
    ) throws LdapException {
        getDelegate().rename(entryDn, newRdn);
    }

    /**
     * @param entryDn
     * @param newRdn
     * @param deleteOldRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#rename(java.lang.String, java.lang.String, boolean)
     */
    public void rename(
        String entryDn,
        String newRdn,
        boolean deleteOldRdn
    ) throws LdapException {
        getDelegate().rename(entryDn, newRdn, deleteOldRdn);
    }

    /**
     * @param entryDn
     * @param newRdn
     * @param deleteOldRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#rename(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.name.Rdn, boolean)
     */
    public void rename(
        Dn entryDn,
        Rdn newRdn,
        boolean deleteOldRdn
    ) throws LdapException {
        getDelegate().rename(entryDn, newRdn, deleteOldRdn);
    }

    /**
     * @param entryDn
     * @param newSuperiorDn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#move(java.lang.String, java.lang.String)
     */
    public void move(
        String entryDn,
        String newSuperiorDn
    ) throws LdapException {
        getDelegate().move(entryDn, newSuperiorDn);
    }

    /**
     * @param entryDn
     * @param newSuperiorDn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#move(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.name.Dn)
     */
    public void move(
        Dn entryDn,
        Dn newSuperiorDn
    ) throws LdapException {
        getDelegate().move(entryDn, newSuperiorDn);
    }

    /**
     * @param entryDn
     * @param newDn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#moveAndRename(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.name.Dn)
     */
    public void moveAndRename(
        Dn entryDn,
        Dn newDn
    ) throws LdapException {
        getDelegate().moveAndRename(entryDn, newDn);
    }

    /**
     * @param entryDn
     * @param newDn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#moveAndRename(java.lang.String, java.lang.String)
     */
    public void moveAndRename(
        String entryDn,
        String newDn
    ) throws LdapException {
        getDelegate().moveAndRename(entryDn, newDn);
    }

    /**
     * @param entryDn
     * @param newDn
     * @param deleteOldRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#moveAndRename(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.name.Dn, boolean)
     */
    public void moveAndRename(
        Dn entryDn,
        Dn newDn,
        boolean deleteOldRdn
    ) throws LdapException {
        getDelegate().moveAndRename(entryDn, newDn, deleteOldRdn);
    }

    /**
     * @param entryDn
     * @param newDn
     * @param deleteOldRdn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#moveAndRename(java.lang.String, java.lang.String, boolean)
     */
    public void moveAndRename(
        String entryDn,
        String newDn,
        boolean deleteOldRdn
    ) throws LdapException {
        getDelegate().moveAndRename(entryDn, newDn, deleteOldRdn);
    }

    /**
     * @param modDnRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#modifyDn(org.apache.directory.api.ldap.model.message.ModifyDnRequest)
     */
    public ModifyDnResponse modifyDn(
        ModifyDnRequest modDnRequest
    ) throws LdapException {
        return getDelegate().modifyDn(modDnRequest);
    }

    /**
     * @param dn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#delete(java.lang.String)
     */
    public void delete(
        String dn
    ) throws LdapException {
        getDelegate().delete(dn);
    }

    /**
     * @param dn
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#delete(org.apache.directory.api.ldap.model.name.Dn)
     */
    public void delete(
        Dn dn
    ) throws LdapException {
        getDelegate().delete(dn);
    }

    /**
     * @param deleteRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#delete(org.apache.directory.api.ldap.model.message.DeleteRequest)
     */
    public DeleteResponse delete(
        DeleteRequest deleteRequest
    ) throws LdapException {
        return getDelegate().delete(deleteRequest);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean compare(
        String dn,
        String attributeName,
        String value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(java.lang.String, java.lang.String, byte[])
     */
    public boolean compare(
        String dn,
        String attributeName,
        byte[] value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(java.lang.String, java.lang.String, org.apache.directory.api.ldap.model.entry.Value)
     */
    public boolean compare(
        String dn,
        String attributeName,
        Value value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(org.apache.directory.api.ldap.model.name.Dn, java.lang.String, java.lang.String)
     */
    public boolean compare(
        Dn dn,
        String attributeName,
        String value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(org.apache.directory.api.ldap.model.name.Dn, java.lang.String, byte[])
     */
    public boolean compare(
        Dn dn,
        String attributeName,
        byte[] value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param dn
     * @param attributeName
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(org.apache.directory.api.ldap.model.name.Dn, java.lang.String, org.apache.directory.api.ldap.model.entry.Value)
     */
    public boolean compare(
        Dn dn,
        String attributeName,
        Value value
    ) throws LdapException {
        return getDelegate().compare(dn, attributeName, value);
    }

    /**
     * @param compareRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#compare(org.apache.directory.api.ldap.model.message.CompareRequest)
     */
    public CompareResponse compare(
        CompareRequest compareRequest
    ) throws LdapException {
        return getDelegate().compare(compareRequest);
    }

    /**
     * @param oid
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#extended(java.lang.String)
     */
    public ExtendedResponse extended(
        String oid
    ) throws LdapException {
        return getDelegate().extended(oid);
    }

    /**
     * @param oid
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#extended(java.lang.String, byte[])
     */
    public ExtendedResponse extended(
        String oid,
        byte[] value
    ) throws LdapException {
        return getDelegate().extended(oid, value);
    }

    /**
     * @param oid
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#extended(org.apache.directory.api.asn1.util.Oid)
     */
    public ExtendedResponse extended(
        Oid oid
    ) throws LdapException {
        return getDelegate().extended(oid);
    }

    /**
     * @param oid
     * @param value
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#extended(org.apache.directory.api.asn1.util.Oid, byte[])
     */
    public ExtendedResponse extended(
        Oid oid,
        byte[] value
    ) throws LdapException {
        return getDelegate().extended(oid, value);
    }

    /**
     * @param extendedRequest
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#extended(org.apache.directory.api.ldap.model.message.ExtendedRequest)
     */
    public ExtendedResponse extended(
        ExtendedRequest extendedRequest
    ) throws LdapException {
        return getDelegate().extended(extendedRequest);
    }

    /**
     * @param dn
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#exists(java.lang.String)
     */
    public boolean exists(
        String dn
    ) throws LdapException {
        return getDelegate().exists(dn);
    }

    /**
     * @param dn
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#exists(org.apache.directory.api.ldap.model.name.Dn)
     */
    public boolean exists(
        Dn dn
    ) throws LdapException {
        return getDelegate().exists(dn);
    }

    /**
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#getRootDse()
     */
    public Entry getRootDse(
    ) throws LdapException {
        return getDelegate().getRootDse();
    }

    /**
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#getRootDse(java.lang.String[])
     */
    public Entry getRootDse(
        String... attributes
    ) throws LdapException {
        return getDelegate().getRootDse(attributes);
    }

    /**
     * @param dn
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(org.apache.directory.api.ldap.model.name.Dn)
     */
    public Entry lookup(
        Dn dn
    ) throws LdapException {
        return getDelegate().lookup(dn);
    }

    /**
     * @param dn
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(java.lang.String)
     */
    public Entry lookup(
        String dn
    ) throws LdapException {
        return getDelegate().lookup(dn);
    }

    /**
     * @param dn
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(org.apache.directory.api.ldap.model.name.Dn, java.lang.String[])
     */
    public Entry lookup(
        Dn dn,
        String... attributes
    ) throws LdapException {
        return getDelegate().lookup(dn, attributes);
    }

    /**
     * @param dn
     * @param controls
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(org.apache.directory.api.ldap.model.name.Dn, org.apache.directory.api.ldap.model.message.Control[], java.lang.String[])
     */
    public Entry lookup(
        Dn dn,
        Control[] controls,
        String... attributes
    ) throws LdapException {
        return getDelegate().lookup(dn, controls, attributes);
    }

    /**
     * @param dn
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(java.lang.String, java.lang.String[])
     */
    public Entry lookup(
        String dn,
        String... attributes
    ) throws LdapException {
        return getDelegate().lookup(dn, attributes);
    }

    /**
     * @param dn
     * @param controls
     * @param attributes
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#lookup(java.lang.String, org.apache.directory.api.ldap.model.message.Control[], java.lang.String[])
     */
    public Entry lookup(
        String dn,
        Control[] controls,
        String... attributes
    ) throws LdapException {
        return getDelegate().lookup(dn, controls, attributes);
    }

    /**
     * @param controlOID
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#isControlSupported(java.lang.String)
     */
    public boolean isControlSupported(
        String controlOID
    ) throws LdapException {
        return getDelegate().isControlSupported(controlOID);
    }

    /**
     * @return
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#getSupportedControls()
     */
    public List<String> getSupportedControls(
    ) throws LdapException {
        return getDelegate().getSupportedControls();
    }

    /**
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#loadSchema()
     */
    public void loadSchema(
    ) throws LdapException {
        getDelegate().loadSchema();
    }

    /**
     * @throws LdapException
     * @see org.apache.directory.ldap.client.api.LdapConnection#loadSchemaRelaxed()
     */
    public void loadSchemaRelaxed(
    ) throws LdapException {
        getDelegate().loadSchemaRelaxed();
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#getSchemaManager()
     */
    public SchemaManager getSchemaManager(
    ) {
        try {
            return getDelegate().getSchemaManager();
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#getCodecService()
     */
    public LdapApiService getCodecService(
    ) {
        try {
            return getDelegate().getCodecService();
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param messageId
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#isRequestCompleted(int)
     */
    public boolean isRequestCompleted(
        int messageId
    ) {
        try {
            return getDelegate().isRequestCompleted(messageId);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param messageId
     * @return
     * @deprecated
     * @see org.apache.directory.ldap.client.api.LdapConnection#doesFutureExistFor(int)
     */
    public boolean doesFutureExistFor(
        int messageId
    ) {
        try {
            return getDelegate().doesFutureExistFor(messageId);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnection#getBinaryAttributeDetector()
     */
    public BinaryAttributeDetector getBinaryAttributeDetector(
    ) {
        try {
            return getDelegate().getBinaryAttributeDetector();
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param binaryAttributeDetecter
     * @see org.apache.directory.ldap.client.api.LdapConnection#setBinaryAttributeDetector(org.apache.directory.api.ldap.codec.api.BinaryAttributeDetector)
     */
    public void setBinaryAttributeDetector(
        BinaryAttributeDetector binaryAttributeDetecter
    ) {
        try {
            getDelegate().setBinaryAttributeDetector(binaryAttributeDetecter);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * @param schemaManager
     * @see org.apache.directory.ldap.client.api.LdapConnection#setSchemaManager(org.apache.directory.api.ldap.model.schema.SchemaManager)
     */
    public void setSchemaManager(
        SchemaManager schemaManager
    ) {
        try {
            getDelegate().setSchemaManager(schemaManager);
        } catch (LdapException e) {
            throw new LdapRuntimeException(e);
        }
    }

	public Throwable exceptionCaught() {
		return null;
	}
    
}
