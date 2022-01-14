/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard Fetch Group 
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
package org.openmdx.base.persistence.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchGroup;
import javax.jdo.JDOUserException;

/**
 * Standard Fetch Group 
 */
@SuppressWarnings({"rawtypes"})
public class StandardFetchGroup
    implements FetchGroup
{

    /**
     * Constructor 
     *
     * @param that
     */
    public StandardFetchGroup(
        FetchGroup that
    ){
        this.name = that.getName();
        this.type = that.getType();
        this.postLoad = that.getPostLoad();
        this.members = new HashMap<String,Integer>();
        for(Object member : that.getMembers()) {
            String memberName = (String) member;
            this.members.put(
                memberName,
                Integer.valueOf(that.getRecursionDepth(memberName))
            );
        }
    }
    
    /**
     * Constructor 
     * 
     * @param type
     * @param name
     */
    public StandardFetchGroup(
        Class<?> type,
        String name
    ){
        this.type = type;
        this.name = name;
        this.postLoad = !DEFAULT.equals(name);
        this.members = new HashMap<String,Integer>();
    }

    private final String name;
    
    private final Class<?> type;
    
    private final Map<String,Integer> members;
    
    private boolean postLoad;
    
    private static final Integer DEFAULT_RECURSION_DEPTH = Integer.valueOf(1);
    
    private boolean unmodifiable = false;
    
    private void assertModifiability(
    ){
        if(this.unmodifiable) {
            throw new JDOUserException("This fetch group is unmodifiable");
        }
    }
    
    private void assertMembership(
        String memberName
    ){
        if(!this.members.containsKey(memberName)) {
            throw new JDOUserException("Not member of this fetch group: " + memberName);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#addCategory(java.lang.String)
     */
    public FetchGroup addCategory(String categoryName) {
        assertModifiability();
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#addMember(java.lang.String)
     */
    public FetchGroup addMember(String memberName) {
        assertModifiability();
        if(!this.members.containsKey(memberName)) {
            this.members.put(memberName,DEFAULT_RECURSION_DEPTH);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#addMembers(java.lang.String[])
     */
    public FetchGroup addMembers(String... memberNames) {
        assertModifiability();
        for(String memberName : memberNames) {
            if(!this.members.containsKey(memberName)) {
                this.members.put(memberName,DEFAULT_RECURSION_DEPTH);
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#getMembers()
     */
    public Set getMembers() {
        return Collections.unmodifiableSet(new HashSet<String>(this.members.keySet()));
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#getPostLoad()
     */
    public boolean getPostLoad() {
        return this.postLoad;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#getRecursionDepth(java.lang.String)
     */
    public int getRecursionDepth(String memberName) {
        assertModifiability();
        assertMembership(memberName);
        return this.members.get(memberName).intValue();
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#getType()
     */
    public Class getType() {
        return this.type;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#isUnmodifiable()
     */
    public boolean isUnmodifiable() {
        return this.unmodifiable;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#removeCategory(java.lang.String)
     */
    public FetchGroup removeCategory(String categoryName) {
        assertModifiability();
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#removeMember(java.lang.String)
     */
    public FetchGroup removeMember(String memberName) {
        assertModifiability();
        this.members.remove(memberName);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#removeMembers(java.lang.String[])
     */
    public FetchGroup removeMembers(String... memberNames) {
        assertModifiability();
        for(String memberName : memberNames) {
            this.members.remove(memberName);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#setPostLoad(boolean)
     */
    public FetchGroup setPostLoad(boolean postLoad) {
        assertModifiability();
        this.postLoad = true;
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#setRecursionDepth(java.lang.String, int)
     */
    public FetchGroup setRecursionDepth(String memberName, int recursionDepth) {
        assertModifiability();
        assertMembership(memberName);
        this.members.put(memberName, Integer.valueOf(recursionDepth));
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.FetchGroup#setUnmodifiable()
     */
    public FetchGroup setUnmodifiable() {
        this.unmodifiable = true;
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FetchGroup) {
            FetchGroup that = (FetchGroup) obj;
            return
                this.type == that.getType() &&
                this.name.equals(that.getName());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.name.hashCode() + 31 * this.type.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "<" + this.name + "," + this.type.getName() + ">";
    }

}
