/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.jbi.framework;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Component Name is used internally to identify a Component.
 * 
 * @version $Revision: 1.1 $
 */
public class ComponentNameSpace implements Externalizable {
   
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -9130913368962887486L;
    
    protected String containerName;
    protected String name;

    /**
     * Default Constructor
     */
    public ComponentNameSpace() {
    }

    /**
     * Construct a ComponentName
     * 
     * @param containerName
     * @param componentName
     * @param componentId
     */
    public ComponentNameSpace(String containerName, String componentName) {
        this.containerName = containerName;
        this.name = componentName;
    }
    
    /**
     * @return Returns the componentName.
     */
    public String getName() {
        return name;
    }

    /**
     * @param componentName The componentName to set.
     */
    public void setName(String componentName) {
        this.name = componentName;
    }

    /**
     * @return Returns the containerName.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * @param containerName The containerName to set.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    /**
     * @param obj
     * @return true if obj is equivalent to 'this'
     */
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && obj instanceof ComponentNameSpace) {
            ComponentNameSpace other = (ComponentNameSpace) obj;
            result = other.containerName.equals(this.containerName)
                    && other.name.equals(this.name);
        }
        return result;
    }
    
    /**
     * @return the hashCode
     */
    public int hashCode() {
        return containerName.hashCode() ^ name.hashCode();
    }
    
    /**
     * @return pretty print
     */
    public String toString() {
        return "[container=" + containerName + ",name=" + name + "]";
    }

    /**
     * write out to stream
     * @param out
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(containerName != null ? containerName : "");
        out.writeUTF(name != null ? name : "");
    }

    /**
     * read from Stream
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        containerName = in.readUTF();
        name = in.readUTF();
    }
    
    /**
     * copy this
     * @return
     */
    public ComponentNameSpace copy() {
        ComponentNameSpace result = new ComponentNameSpace(containerName, name);
        return result;
    }
    
}
