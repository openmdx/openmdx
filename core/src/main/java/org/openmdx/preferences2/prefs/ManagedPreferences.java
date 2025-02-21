/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Preferences 
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
package org.openmdx.preferences2.prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import javax.jdo.PersistenceManager;

import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UnitOfWork;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.preferences2.cci2.NodeQuery;
import org.openmdx.preferences2.jmi1.Entry;
import org.openmdx.preferences2.jmi1.Node;

/**
 * Standard Preferences
 */
class ManagedPreferences extends AbstractPreferences implements Retrievable {

    /**
     * Constructor 
     *
     * @param node
     */
    ManagedPreferences(
        Node node
    ) {
        super(null, "");
        this.node = node;
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param node
     */
    protected ManagedPreferences(
        ManagedPreferences parent,
        Node node
    ) {
        super(parent, node.getName());
        this.node = node;
    }
    
    /**
     * The JMI node corresponding to this preferences node
     */
    private final Node node;
    
    /**
     * Retrieve the persistence manager associated with the root node
     * 
     * @return the persistence manager associated with the root node
     */
    protected PersistenceManager jmiEntityManager(){
        return ReducedJDOHelper.getPersistenceManager(this.node);
    }

    /**
     * Retrieve the unit of work associated with the root node
     * 
     * @return the unit of work associated with the root node
     */
    protected UnitOfWork currentUnitOfWork(){
        return PersistenceHelper.currentUnitOfWork(jmiEntityManager());
    }
    
    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#putSpi(java.lang.String, java.lang.String)
     */
    @Override
    protected void putSpi(String key, String value) {
        Entry entry = this.node.getEntry(key);
        if(entry == null) {
            entry = jmiEntityManager().newInstance(Entry.class);
            this.node.addEntry(key, entry);
        }
        entry.setValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#getSpi(java.lang.String)
     */
    @Override
    protected String getSpi(String key) {
        Entry entry = this.node.getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeSpi(java.lang.String)
     */
    @Override
    protected void removeSpi(String key) {
        Entry entry = this.node.getEntry(key);
        if(entry != null) {
            entry.setValue(null);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeNodeSpi()
     */
    @Override
    protected void removeNodeSpi(
    ) throws BackingStoreException {
        try {
            this.node.refDelete();
        } catch (RuntimeException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#keysSpi()
     */
    @Override
    protected String[] keysSpi(
    ) throws BackingStoreException {
        try {
            List<String> keys = new ArrayList<String>();
            for(Entry entry : this.node.<Entry>getEntry()) {
                if(entry.getValue() != null) {
                    keys.add(entry.getName());
                }
            }
            return keys.toArray(new String[keys.size()]);
        } catch (RuntimeException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childrenNamesSpi()
     */
    @Override
    protected String[] childrenNamesSpi(
    ) throws BackingStoreException {
        try {
            List<String> childrenNames = new ArrayList<String>();
            for(Node child : this.node.<Node>getChild(this.node.getRoot())) {
                childrenNames.add(child.getName());
            }
            return childrenNames.toArray(new String[childrenNames.size()]);
        } catch (RuntimeException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childSpi(java.lang.String)
     */
    @Override
    protected AbstractPreferences childSpi(
        String name
    ) {
        return new ManagedPreferences(this, getChildNode(name));
    }

    /**
     * Create a new child node
     * 
     * @param name
     * 
     * @param entityManager
     * 
     * @return a new Node
     */
    protected Node getChildNode(
        String name
    ) {
        PersistenceManager entityManager = jmiEntityManager();
        NodeQuery query = (NodeQuery) entityManager.newQuery(Node.class);
        query.name().equalTo(name);
        query.thereExistsParent().equalTo(this.node);
        List<Node> nodes = this.node.getRoot().getNode(query);
        return nodes.isEmpty() ? newChildNode(name) : nodes.get(0);
    }
    
    /**
     * Create a new child node
     * 
     * @param name
     * 
     * @param entityManager
     * 
     * @return a new Node
     */
    protected Node newChildNode(
        String name
    ) {
        Node child = jmiEntityManager().newInstance(Node.class);
        child.setParent(this.node);
        child.setName(name);
        this.node.getRoot().addNode(child);
        return child;
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#sync()
     */
    @Override
    public void sync(
    ) throws BackingStoreException {
        flush();
        PersistenceHelper.retrieveAllDescendants(this.node);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#syncSpi()
     */
    @Override
    protected void syncSpi(
    ) throws BackingStoreException {
        // We did override sync() instead
    }
    
    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flush()
     */
    @Override
    public void flush(
    ) throws BackingStoreException {
        try {
            jmiEntityManager().flush();
        } catch (RuntimeException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flushSpi()
     */
    @Override
    protected void flushSpi(
    ) throws BackingStoreException {
        // We did override flush() instead
    }

    /* (non-Javadoc)
     * @see org.openmdx.preferences2.prefs.Retrievable#retrieve()
     */
    @Override
    public void retrieveAll() {
        PersistenceHelper.retrieveAllDescendants(
            ReducedJDOHelper.getPersistenceManager(
                this.node
            ).getObjectById(
                this.node.refGetPath().getPrefix(7)
            )
        );
    }

}
