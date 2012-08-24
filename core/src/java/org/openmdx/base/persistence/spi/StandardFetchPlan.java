/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard Fetch Plan 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

/**
 * Standard Fetch Plan 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class StandardFetchPlan
    implements FetchPlan
{

    /**
     * Constructor 
     *
     * @param that the fetch plan to be cloned
     */
    private StandardFetchPlan(
        FetchPlan that
    ){
        this.maxFetchDepths = that.getMaxFetchDepth();
        this.fetchSize = that.getFetchSize();
        this.groups = new HashSet<String>(that.getGroups());
        this.detachmentOptions = that.getDetachmentOptions();
        this.detachmentRoots = that.getDetachmentRoots();
        this.detachmentRootClasses = that.getDetachmentRootClasses();
    }

    /**
     * Constructor 
     */
    private StandardFetchPlan(
    ){
        this.maxFetchDepths = 1;
        this.fetchSize = FETCH_SIZE_OPTIMAL;
        this.groups = new HashSet<String>(DEFAULT_GROUPS);
        this.detachmentOptions = DETACH_LOAD_FIELDS;
        this.detachmentRoots = Collections.emptySet();
        this.detachmentRootClasses = NO_ROOT_CLASSES;
    }
    
    /**
     * The default fetch groups
     */
    public static final Set<String> DEFAULT_GROUPS = Collections.singleton(
        FetchPlan.DEFAULT
    );
    
    private static final Class<?>[] NO_ROOT_CLASSES = {};
    
    private int maxFetchDepths;
    
    private int fetchSize;
    
    private int detachmentOptions;
    
    private final Set<String> groups;
    
    private Collection detachmentRoots = null;
    
    private Class[] detachmentRootClasses = null;
    
    /**
     * Fetch plan factory
     * 
     * @param persistenceManager
     * 
     * @return a new fetch plan
     */
    public static FetchPlan newInstance(
        PersistenceManager persistenceManager
    ){
        if(persistenceManager != null) {
            FetchPlan that = persistenceManager.getFetchPlan();
            if(that != null) {
                return new StandardFetchPlan(that);
            }
        }
        return new StandardFetchPlan();
    }
        
    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#addGroup(java.lang.String)
     */
    public FetchPlan addGroup(String fetchGroupName) {
        this.groups.add(fetchGroupName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#clearGroups()
     */
    public FetchPlan clearGroups() {
        this.groups.clear();
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getDetachmentOptions()
     */
    public int getDetachmentOptions() {
        return this.detachmentOptions;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getDetachmentRootClasses()
     */
    public Class[] getDetachmentRootClasses() {
        return this.detachmentRootClasses;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getDetachmentRoots()
     */
    public Collection getDetachmentRoots() {
        return this.detachmentRoots;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getFetchSize()
     */
    public int getFetchSize() {
        return this.fetchSize;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getGroups()
     */
    public Set getGroups() {
        return Collections.unmodifiableSet(new HashSet(this.groups));
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#getMaxFetchDepth()
     */
    public int getMaxFetchDepth() {
        return this.maxFetchDepths;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#removeGroup(java.lang.String)
     */
    public FetchPlan removeGroup(String fetchGroupName) {
        this.groups.remove(fetchGroupName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setDetachmentOptions(int)
     */
    public FetchPlan setDetachmentOptions(int options) {
        this.detachmentOptions = options;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setDetachmentRootClasses(java.lang.Class[])
     */
    public FetchPlan setDetachmentRootClasses(Class... rootClasses) {
        this.detachmentRootClasses = rootClasses;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setDetachmentRoots(java.util.Collection)
     */
    public FetchPlan setDetachmentRoots(Collection roots) {
        this.detachmentRoots = roots;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setFetchSize(int)
     */
    public FetchPlan setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setGroup(java.lang.String)
     */
    public FetchPlan setGroup(String fetchGroupName) {
        this.groups.clear();
        this.groups.add(fetchGroupName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setGroups(java.util.Collection)
     */
    public FetchPlan setGroups(Collection fetchGroupNames) {
        this.groups.clear();
        this.groups.addAll(fetchGroupNames);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setGroups(java.lang.String[])
     */
    public FetchPlan setGroups(String... fetchGroupNames) {
        this.groups.clear();
        this.groups.addAll(Arrays.asList(fetchGroupNames));
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchPlan#setMaxFetchDepth(int)
     */
    public FetchPlan setMaxFetchDepth(int fetchDepth) {
        if(fetchDepth == 0) {
            throw new JDOUserException("Invalid 'fetchDepth' value: " + fetchDepth);
        }
        this.maxFetchDepths = fetchDepth;
        return this;
    }
    
}
