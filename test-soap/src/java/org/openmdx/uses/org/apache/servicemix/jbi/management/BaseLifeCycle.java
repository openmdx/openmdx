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
package org.openmdx.uses.org.apache.servicemix.jbi.management;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

/**
 * A BasicLifeCycle implementation
 * 
 * @version $Revision: 1.1 $
 */
public abstract class BaseLifeCycle implements LifeCycleMBean /*, MBeanInfoProvider */ {
    
    public static final String INITIALIZED = "Initialized";
    
    protected String currentState = LifeCycleMBean.UNKNOWN;
    
    protected PropertyChangeListener listener;
    
    
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName() {
        String name = getClass().getName();
        int index = name.lastIndexOf(".");
        if (index >= 0 && (index+1) < name.length()) {
            name = name.substring(index+1);
        }
        return name;
    }
    
    /**
     * Get the type of the item
     * @return the type
     */
    public String getType() {
        String name = getClass().getName();
        int index = name.lastIndexOf(".");
        if (index >= 0 && (index+1) < name.length()) {
            name = name.substring(index+1);
        }
        return name;
    }
    
    public String getSubType() {
        return null;
    }
    
    /**
     * set state to initialized
     * @throws JBIException 
     *
     */
    protected void init() throws JBIException{
        setCurrentState(INITIALIZED);
    }

    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        setCurrentState(LifeCycleMBean.STARTED);
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException {
        setCurrentState(LifeCycleMBean.STOPPED);
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException {
        setCurrentState(LifeCycleMBean.SHUTDOWN);
    }

    /**
     * Get the current state of this managed compononent.
     * 
     * @return the current state of this managed component (must be one of the string constants defined by this
     * interface)
     * @org.apache.xbean.Property hidden="true"
     */
    public String getCurrentState() {
        return currentState;
    }
    
    /**
     * Set the current state
     * @param newValue
     */
    protected void setCurrentState(String newValue){
        String oldValue = currentState;
        this.currentState = newValue;
        firePropertyChanged("currentState",oldValue,newValue);
    }
    
    /**
     * @return true if the object is in the started state
     */
    public boolean isStarted(){
        return currentState != null && currentState.equals(LifeCycleMBean.STARTED);
    }
    
    /**
    * @return true if the object is stopped
    */
   public boolean isStopped(){
       return currentState != null && currentState.equals(LifeCycleMBean.STOPPED);
   }
   
   /**
    * @return true if the object is shutDown
    */
   public boolean isShutDown(){
       return currentState != null && currentState.equals(LifeCycleMBean.SHUTDOWN);
   }
   
   /**
    * @return true if the object is shutDown
    */
   public boolean isInitialized(){
       return currentState != null && currentState.equals(INITIALIZED);
   }
   
   /**
    * @return true if the object is shutDown
    */
   public boolean isUnknown(){
       return currentState == null || currentState.equals(LifeCycleMBean.UNKNOWN);
   }
    
    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        throw new UnsupportedOperationException();
//        AttributeInfoHelper helper = new AttributeInfoHelper();
//        helper.addAttribute(getObjectToManage(), "currentState", "Current State of Managed Item");
//        helper.addAttribute(getObjectToManage(), "name", "name of the Item");
//        helper.addAttribute(getObjectToManage(), "description", "description of the Item");
//        return helper.getAttributeInfos();
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        throw new UnsupportedOperationException();
//        OperationInfoHelper helper = new OperationInfoHelper();
//        helper.addOperation(getObjectToManage(), "start", "start the item");
//        helper.addOperation(getObjectToManage(), "stop", "stop the item");
//        helper.addOperation(getObjectToManage(), "shutDown", "shutdown the item");
//        return helper.getOperationInfos();
    }

    /**
     * Get the Object to Manage
     * 
     * @return the Object to Manage
     */
    public Object getObjectToManage() {
        return this;
    }
    
    /**
     * Register for propertyChange events
     * @param l
     * @org.apache.xbean.Property hidden="true"
     */
    public void setPropertyChangeListener(PropertyChangeListener l){
        this.listener = l;
    }
    
    protected void firePropertyChanged(String name,Object oldValue, Object newValue){
        PropertyChangeListener l = listener;
        if (l != null){
            PropertyChangeEvent event = new PropertyChangeEvent(this,name,oldValue,newValue);
            l.propertyChange(event);
        }
    }
}