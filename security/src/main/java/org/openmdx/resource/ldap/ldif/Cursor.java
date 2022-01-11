/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Cursor
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2019, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.ldif;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.cursor.ClosureMonitor;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchResultDone;

class Cursor implements EntryCursor {
    
    Cursor(
        List<Entry> entries
    ){
        this.entries = entries;
        this.current = NOT_SET;
    }

    private List<Entry> entries;
    private int current;
    private static final int NOT_SET = Integer.MIN_VALUE;
    
    @Override
    public boolean available(
    ) {
        return !isClosed() && 0 <= current && current < entries.size();
    }

    private int lookup(Entry element) throws CursorException {
        assertOpen();
        final int index = this.entries.indexOf(element);
        if(index < 0) {
            throw new CursorException("Nu such element");
        }
        return index;
    }
    
    @Override
    public void before(
        Entry element
    ) throws LdapException, CursorException {
        this.current = lookup(element) - 1;
    }

    @Override
    public void after(
        Entry element
    ) throws LdapException, CursorException {
        this.current = lookup(element) + 1;
    }

    @Override
    public void beforeFirst(
    ) throws LdapException, CursorException {
        assertOpen();
        this.current = -1;
    }

    @Override
    public void afterLast(
    ) throws LdapException, CursorException {
        this.current = size();
    }

    @Override
    public boolean first(
    ) throws LdapException, CursorException {
        assertOpen();
        this.current = 0;
        return !isEmpty();
    }

    @Override
    public boolean isFirst(
    ) {
        return !isClosed() && this.current == 0 && !isEmpty();
    }

    @Override
    public boolean isBeforeFirst(
    ) {
        return !isClosed() && this.current < 0;
    }

    @Override
    public boolean last(
    ) throws LdapException, CursorException {
        this.current = size() - 1;
        return !isEmpty();
    }

    @Override
    public boolean isLast(
    ) {
        return !isClosed() && this.current == this.entries.size() - 1 && !isEmpty();
    }

    private boolean isEmpty(
    ) {
        return this.entries.isEmpty();
    }

    @Override
    public boolean isAfterLast(
    ) {
        return !isClosed() && this.current >= this.entries.size();
    }

    private int size(
    ) throws CursorException {
        assertOpen();
        return this.entries.size();
    }

    @Override
    public boolean isClosed(
    ) {
        return this.entries == null;
    }

    @Override
    public boolean previous(
    ) throws LdapException, CursorException {
        final boolean advanced;
        if(this.current == NOT_SET) {
            advanced = last();
        } else if(this.current < 0) {
            advanced = false;
        } else {
            this.current--;
            advanced = true;
        } 
        return advanced;
    }

    @Override
    public boolean next(
    ) throws LdapException, CursorException {
        final boolean advanced;
        if(this.current == NOT_SET) {
            advanced = first();
        } else if(this.current >= size()) {
            advanced = false;
        } else {
            this.current++;
            advanced = true;
        } 
        return advanced;
    }

    @Override
    public Entry get(
    ) throws CursorException {
        assertOpen();
        if(!available()) {
            throw new CursorException("No element available");
        }
        return this.entries.get(this.current);
    }

    @Override
    public void close(
        Exception reason
    ) throws IOException {
        close();
    }

    @Override
    public void setClosureMonitor(
        ClosureMonitor monitor
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(
        String tabs
    ) {
        final StringBuilder text = new StringBuilder("[");
        for(Entry entry : this) {
            text.append('\n').append(tabs).append(entry.toString(tabs));
        }
        return text.append("\n]").toString();
    }

    @Override
    public Iterator<Entry> iterator(
    ) {
        return this.entries.iterator();
    }

    @Override
    public void close(
    ) throws IOException {
        this.entries = null;
        this.current = NOT_SET;
    }

    @Override
    public SearchResultDone getSearchResultDone(
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMessageId(
    ) {
        throw new UnsupportedOperationException();
    }

    private void assertOpen() throws CursorException {
        if(isClosed()) {
            throw new CursorException("Cursor is closed");
        }
    }

}
