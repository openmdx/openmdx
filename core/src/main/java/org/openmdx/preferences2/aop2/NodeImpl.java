/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Preferences Node plug-in
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.preferences2.aop2;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.StoreCallback;

import org.openmdx.base.aop2.AbstractObject;
import org.openmdx.preferences2.jmi1.Node;

/**
 * Node plug-in
 */
public class NodeImpl<
    S extends org.openmdx.preferences2.jmi1.Node,
    N extends org.openmdx.preferences2.cci2.Node
> extends AbstractObject<S, N, Void> implements StoreCallback {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public NodeImpl(S same, N next) {
        super(same, next);
    }

    /**
     * Tests whether name and parent are consistent:
     * <em>The root node and only the root node has an empty name</em>
     * 
     * @return <code>true</code> if name and parent are consistent
     */
    protected boolean isConsistent(){
        S same = sameObject();
        Node parent = same.getParent();
        String name = same.getName();
        return (parent == null) == "".equals(name);
    }
    
    /**name
     * Sets a new value for the attribute <code>name</code>.
     * <p>
     * This attribute is not changeable, i.e. its value can be set as long as the object is <em>TRANSIENT</em> or <em>NEW</em>
     * <p>
     * The root node has a node name of the empty string ("").
     * Every other node has an arbitrary node name, specified at the time it is created.
     * The only restrictions on this name are that it cannot be the empty string, and it cannot contain the slash character ('/').
     * @param name The non-null new value for attribute <code>name</code>.
     * 
     * @exception NullPointerException if name is <code>null</code>
     * @exception IllegalArgumentException if name contains a '/' character
     * 
     */
    public void setName(
      java.lang.String name
    ){
        if(name.indexOf('/') < 0) {
            nextObject().setName(name);
        } else {
            throw new IllegalArgumentException(
                "The name must not contain a '/'"
            );
        }
    }

    /**
     * Retrieves the value for the attribute <code>name</code>.
     * <p>
     * The root node has a node name of the empty string ("").
     * Every other node has an arbitrary node name, specified at the time it is created.
     * The only restrictions on this name are that it cannot be the empty string, and it cannot contain the slash character ('/').
     * @return The non-null value for attribute <code>name</code>.
     */
    public java.lang.String getName(
    ){
        String name = nextObject().getName();
        return name == null ? "" : name;
    }
    
    /**
     * Retrieves the value for the attribute <code>absolutePath</code>.
     * <p>
     * The root node has an absolute path name of "/".
     * Children of the root node have absolute path names of "/" + <node name>.
     * All other nodes have absolute path names of <parent's absolute path name> + "/" + <node name>.
     * Note that all absolute path names begin with the slash character.
     * @return The non-null value for attribute <code>absolutePath</code>.
     */
    public java.lang.String getAbsolutePath(
    ){
        if(isConsistent()) {
            S same = sameObject();
            Node parent = same.getParent();
            if(parent == null) {
                return "/";
            } else {
                String parentPath = parent.getAbsolutePath();
                String delimiter = "/".equals(parentPath) ? "" : "/";
                return parentPath + delimiter + same.getName();
            }
        } else {
            throw new JDOUserCallbackException(
               "The absolute path can be determined only of parent and name are set consistently"
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.AbstractObject#jdoPreStore()
     */
    @Override
    public void jdoPreStore(
    ){
        if(isConsistent()) {
            super.jdoPreStore();
        } else {
            throw new JDOUserCallbackException(
                "The root node and only the root node has an empty name"
            );
        }
    }

}
