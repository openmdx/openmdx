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
