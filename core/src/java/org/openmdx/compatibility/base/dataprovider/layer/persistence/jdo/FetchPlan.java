/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;

import java.io.Serializable;

/**
 * FetchPlan for fields for use within JPOX Core.
 * 
 * @version $Revision: 1.1 $
 */
public class FetchPlan implements Serializable
{
//    /** Localisation utility for output messages */
//    protected static final Localiser LOCALISER = Localiser.getInstance("org.jpox.Localisation");
//
//    /** Constant defining the fields in the default fetch group. */
//    public static final String DEFAULT = "default";
//
//    /** Constant defining all fields */
//    public static final String ALL = "all";
//
//    /** Constant defing no fields. */
//    public static final String NONE = "none";
//
//    /** Specify that fields that are loaded but not in the current fetch plan should be unloaded prior to detachment. */
//    public static final int DETACH_UNLOAD_FIELDS = 2;
//
//    /** Specify that fields that are not loaded but are in the current fetch plan should be loaded prior to detachment. */
//    public static final int DETACH_LOAD_FIELDS = 1;
//
//    /** Fetch size to load all possible. */
//    public static final int FETCH_SIZE_GREEDY = -1;
//
//    /** Fetch size for the implementation to decide how many to load. */
//    public static final int FETCH_SIZE_OPTIMAL = 0;
//
//    /** The fetch groups in the current FetchPlan. */
//    final Set groups = new HashSet();
//
//    /** The Fetch size. For use when using large result sets. */
//    int fetchSize = FETCH_SIZE_OPTIMAL;
//
//    /** Options to be used during detachment. Spec 12.7 says that the default is DETACH_LOAD_FIELDS. */
//    int detachmentOptions = FetchPlan.DETACH_LOAD_FIELDS;
//
//    /** Managed class keyed by ClassMetaData **/
//    final transient Map managedClass = new HashMap();
//
//    /** Maximum depth to fetch from the root object. */
//    int maxFetchDepth = 1;
//
//    /** The classes used as the roots for detachment (DetachAllOnCommit). */
//    Class[] detachmentRootClasses = null;
//
//    /** The instances used as the roots for detachment (DetachAllOnCommit). */
//    Collection detachmentRoots = null;
//
//    /**
//     * Constructor.
//     * Initially has the default fetch group.
//     */
//    public FetchPlan()
//    {
//        groups.add(FetchPlan.DEFAULT);
//    }
//
//    /**
//     * Manage the fetch plan for the class
//     * @param cmd AbstractClassMetaData for the class to manage
//     * @return the FetchPlanForClass
//     */
//    public FetchPlanForClass manageFetchPlanForClass(AbstractClassMetaData cmd)
//    {
//        FetchPlanForClass fetchPlanForClass = (FetchPlanForClass) managedClass.get(cmd);
//        if (fetchPlanForClass == null)
//        {
//            fetchPlanForClass = new FetchPlanForClass(cmd, this);
//            managedClass.put(cmd, fetchPlanForClass);
//        }
//        return fetchPlanForClass;
//    }
//
//    /**
//     * Mark all managed fetch plans to be dirty, so the active fields need to
//     * be recomputed.
//     */
//    private void markDirty()
//    {
//        Iterator it = managedClass.values().iterator();
//        while (it.hasNext())
//        {
//            ((FetchPlanForClass) it.next()).markDirty();
//        }
//    }
//
//    /**
//     * Access the fetch plan for the class
//     * @param cmd the AbstractClassMetaData
//     * @return the FetchPlanForClass
//     */
//    public synchronized FetchPlanForClass getFetchPlanForClass(AbstractClassMetaData cmd)
//    {
//        return (FetchPlanForClass)managedClass.get(cmd);
//    }
//
//    /**
//     * Method to add a group to the fetch plan.
//     * @param fetchGroupName The fetch group to add
//     * @return Updated Fetch Plan
//     */
//    public synchronized FetchPlan addGroup(String fetchGroupName)
//    {
//        if (fetchGroupName != null)
//        {
//            if (groups.add(fetchGroupName))
//            {
//                markDirty();
//            }
//        }
//        return this;
//    }
//
//    /**
//     * Method to remove a group from the fetch plan.
//     * @param fetchGroupName The fetch group to remove
//     * @return Updated Fetch Plan
//     */
//    public synchronized FetchPlan removeGroup(String fetchGroupName)
//    {
//        if (groups.remove(fetchGroupName))
//        {
//            markDirty();
//        }
//        return this;
//    }
//
//    /**
//     * Method to clear the current groups and activate the DFG.
//     * @return The FetchPlan
//     */
//    public synchronized FetchPlan clearGroups()
//    {
//        groups.clear();
//        markDirty();
//        return this;
//    }
//
//    /**
//     * Accessor for the groups for this FetchPlan.
//     * @return The fetch plan groups (unmodifiable)
//     */
//    public synchronized Set getGroups()
//    {
//        return Collections.unmodifiableSet(new HashSet(groups));
//    }
//
//    /**
//     * Method to set the groups of the fetch plan.
//     * @param fetchGroupNames The fetch groups
//     * @return Updated Fetch Plan
//     */
//    public synchronized FetchPlan setGroups(Collection fetchGroupNames)
//    {
//        if (fetchGroupNames == null)
//        {
//            return this;
//        }
//
//        Set g = new HashSet(fetchGroupNames);
//        groups.clear();
//        groups.addAll(g);
//
//        markDirty();
//        return this;
//    }
//
//    /**
//     * Method to set the groups using an array.
//     * @param fetchGroupNames Names of the fetch groups
//     * @return The Fetch Plan
//     */
//    public synchronized FetchPlan setGroups(String[] fetchGroupNames)
//    {
//        groups.clear();
//        if (fetchGroupNames != null && fetchGroupNames.length > 0)
//        {
//            for (int i=0;i<fetchGroupNames.length;i++)
//            {
//                groups.add(fetchGroupNames[i]);
//            }
//        }
//        markDirty();
//        return this;
//    }
//
//    /**
//     * Method to set the fetch group.
//     * @param fetchGroupName Name of the fetch group
//     * @return The Fetch Plan
//     */
//    public synchronized FetchPlan setGroup(String fetchGroupName)
//    {
//        groups.clear();
//        if (fetchGroupName != null)
//        {
//            groups.add(fetchGroupName);
//        }
//        markDirty();
//        return this;
//    }
//
//    /**
//     * Set the roots for DetachAllOnCommit
//     * @param roots The roots of the detachment graph.
//     * @return The fetch plan with these roots
//     */
//    public FetchPlan setDetachmentRoots(Collection roots)
//    {
//        if (detachmentRootClasses != null || detachmentRoots != null)
//        {
//            throw new JPOXUserException(LOCALISER.msg("FetchPlan.DetachmentRootsNotChangeableBeforeCommit"));
//        }
//
//        if (roots == null)
//        {
//            detachmentRoots = null;
//        }
//
//        detachmentRoots = new ArrayList();
//        detachmentRoots.addAll(roots);
//        return this;
//    }
//
//    /**
//     * Accessor for the roots of the detachment graph for DetachAllOnCommit.
//     * @return The roots of the detachment graph.
//     */
//    public Collection getDetachmentRoots()
//    {
//        if (detachmentRoots == null)
//        {
//            return Collections.EMPTY_LIST;
//        }
//        return Collections.unmodifiableCollection(detachmentRoots);
//    }
//
//    /**
//     * Set the classes used for roots of the detachment graph for DetachAllOnCommit.
//     * @param rootClasses Classes to be used as roots of the detachment graph
//     * @return The fetch plan with these roots
//     */
//    public FetchPlan setDetachmentRootClasses(Class[] rootClasses)
//    {
//        if (detachmentRootClasses != null || detachmentRoots != null)
//        {
//            throw new JPOXUserException(LOCALISER.msg("FetchPlan.DetachmentRootsNotChangeableBeforeCommit"));
//        }
//
//        if (rootClasses == null)
//        {
//            detachmentRootClasses = null;
//            return this;
//        }
//
//        detachmentRootClasses = new Class[rootClasses.length];
//        for (int i=0;i<rootClasses.length;i++)
//        {
//            detachmentRootClasses[i] = rootClasses[i];
//        }
//
//        return this;
//    }
//
//    /**
//     * Accessor for the root classes of the detachment graph for DetachAllOnCommit.
//     * @return The classes to be used as the root of the detachment graph.
//     */
//    public Class[] getDetachmentRootClasses()
//    {
//        if (detachmentRootClasses == null)
//        {
//            return new Class[0];
//        }
//
//        return detachmentRootClasses;
//    }
//
//    /**
//     * Method called at commit() to clear out the detachment roots.
//     */
//    void resetDetachmentRoots()
//    {
//        detachmentRootClasses = null;
//        detachmentRoots = null;
//    }
//
//    /**
//     * Mutator for the maximum fetch depth where
//     * -1 implies no restriction on the fetch depth and
//     * 0 is invalid and throws a JDOUserException.
//     * @param max The maximum fetch depth to fetch to
//     */
//    public synchronized FetchPlan setMaxFetchDepth(int max)
//    {
//        if (max == 0)
//        {
//            throw new JPOXUserException(LOCALISER.msg("FetchPlan.MaxFetchDepthInvalid", max));
//        }
//        this.maxFetchDepth = max;
//        return this;
//    }
//
//    /**
//     * Accessor for the maximum fetch depth.
//     * @return The maximum fetch depth
//     */
//    public synchronized int getMaxFetchDepth()
//    {
//        return maxFetchDepth;
//    }
//
//    /**
//     * Method to set the fetch size when using large result sets.
//     * @param fetchSize the size
//     * @return Updated Fetch Plan
//     */
//    public synchronized FetchPlan setFetchSize(int fetchSize)
//    {
//        if (fetchSize != FETCH_SIZE_GREEDY && fetchSize != FETCH_SIZE_OPTIMAL && fetchSize < 0)
//        {
//            // Invalid fetch size so just return
//            return this;
//        }
//        this.fetchSize = fetchSize;
//        return this;
//    }
//
//    /**
//     * Accessor for the fetch size when using large result sets.
//     * @return The size
//     */
//    public synchronized int getFetchSize()
//    {
//        return fetchSize;
//    }
//
//    /**
//     * Return the options to be used at detachment.
//     * @return Detachment options
//     */
//    public int getDetachmentOptions()
//    {
//        return detachmentOptions;
//    }
//
//    /**
//     * Set the options to be used at detachment.
//     * @param options The options
//     * @return The updated fetch plan.
//     */
//    public FetchPlan setDetachmentOptions(int options)
//    {
//        detachmentOptions = options;
//        return this;
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see java.lang.Object#toString()
//     */
//    public String toString()
//    {
//        return groups.toString();
//    }
//
//    /**
//     * Returns a copy of this FetchPlan with all settings initialized
//     * @return the FetchPlan
//     */
//    public synchronized FetchPlan getCopy()
//    {
//        FetchPlan fp = new FetchPlan(); // Includes DEFAULT
//        fp.maxFetchDepth = maxFetchDepth;
//        fp.groups.remove(FetchPlan.DEFAULT);
//        fp.groups.addAll(this.groups);
//        for (Iterator it = this.managedClass.entrySet().iterator(); it.hasNext();)
//        {
//            Map.Entry entry = (Map.Entry) it.next();
//            AbstractClassMetaData cmd = (AbstractClassMetaData)entry.getKey();
//            FetchPlanForClass fpcls = (FetchPlanForClass)entry.getValue();
//            fp.managedClass.put(cmd, fpcls.getCopy(fp));
//        }
//        fp.fetchSize = this.fetchSize;
//        return fp;
//    }
//
//    /**
//     * Class managing the fetch plan for a particular class.
//     * This should not use the fields of the enclosing FetchPlan directly, always referring
//     * to them using the "plan" field.
//     */
//    public class FetchPlanForClass
//    {
//        /** Parent FetchPlan. */
//        final FetchPlan plan;
//
//        /** MetaData for the class that this represents. */
//        final AbstractClassMetaData cmd;
//
//        /** Fields in the fetch plan for this class. */
//        int[] fieldsInActualFetchPlan;
//
//        /** Whether the record is dirty and needs the fields recalculating. */
//        boolean dirty = true;
//
//        /**
//         * Constructor
//         * @param cmd the ClassMetaData
//         * @param fetchPlan the FetchPlan
//         */
//        public FetchPlanForClass(final AbstractClassMetaData cmd, FetchPlan fetchPlan)
//        {
//            super();
//            this.cmd = cmd;
//            this.plan = fetchPlan;
//        }
//
//        /**
//         * Accessor for the FetchPlan that this classes plan relates to.
//         * @return The FetchPlan
//         */
//        public final FetchPlan getFetchPlan()
//        {
//            return plan;
//        }
//
//        /**
//         * Accessor for the ClassMetaData for this classes plan.
//         * @return ClassMetaData for the class represented here
//         */
//        public final AbstractClassMetaData getAbstractClassMetaData()
//        {
//            return cmd;
//        }
//
//        void markDirty()
//        {
//            dirty = true;
//        }
//
//        FetchPlanForClass getCopy(FetchPlan impl)
//        {
//            FetchPlanForClass fp = new FetchPlanForClass(cmd, impl);
//            if (this.fieldsInActualFetchPlan != null)
//            {
//                fp.fieldsInActualFetchPlan = new int[this.fieldsInActualFetchPlan.length];
//                for (int i = 0; i < fp.fieldsInActualFetchPlan.length; i++)
//                {
//                    fp.fieldsInActualFetchPlan[i] = this.fieldsInActualFetchPlan[i];
//                }
//            }
//            fp.dirty = this.dirty;
//            return fp;
//        }
//
//        /**
//         * Return whether the specified field is in the fetch plan
//         * @param fieldNumber The field number
//         * @return Whether it is in the FetchPlan
//         */
//        public boolean isFieldInActualFetchPlan(int fieldNumber)
//        {
//            if (dirty)
//            {
//                BitSet fieldsNumber = getFieldsInActualFetchPlanByBitSet();
//                return fieldsNumber.get(fieldNumber);
//            }
//            if (fieldsInActualFetchPlan != null)
//            {
//                for (int i=0;i<fieldsInActualFetchPlan.length;i++)
//                {
//                    if (fieldsInActualFetchPlan[i] == fieldNumber)
//                    {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//
//        /**
//         * Get all fields in the actual fetch plan
//         * @return an array with the absolute position of the fields
//         */
//        public int[] getFieldsInActualFetchPlan()
//        {
//            if (dirty)
//            {
//                dirty = false;
//                BitSet fieldsNumber = getFieldsInActualFetchPlanByBitSet();
//                int countFieldsInFP = 0;
//                for (int i = 0; i < fieldsNumber.length(); i++)
//                {
//                    if (fieldsNumber.get(i))
//                    {
//                        countFieldsInFP++;
//                    }
//                }
//
//                fieldsInActualFetchPlan = new int[countFieldsInFP];
//                int nextField = 0;
//                for (int i = 0; i < fieldsNumber.length(); i++)
//                {
//                    if (fieldsNumber.get(i))
//                    {
//                        fieldsInActualFetchPlan[nextField++] = i;
//                    }
//                }
//            }
//            return fieldsInActualFetchPlan;
//        }
//
//        /**
//         * Method to return the effective depth of this field number in the
//         * overall fetch plan.
//         * @param fieldNumber Number of field in this class
//         * @return The (max) recursion depth
//         */
//        public int getMaxRecursionDepthForFieldInCurrentFetchPlan(int fieldNumber)
//        {
//            // prepare array of FetchGroupMetaData from current fetch plan
//            Set currentGroupNames = new HashSet(plan.getGroups());
//            FetchGroupMetaData[] fgmds = cmd.getFetchGroupMetaData(currentGroupNames);
//
//            // find FetchGroupMetaDatas that contain the field in question
//            Set fetchGroupsContainingField = getFetchGroupsForFieldAbsoluteNumber(fgmds, fieldNumber);
//
//            // find recursion depth for field in its class <field> definition
//            int recursionDepth = cmd.getManagedFieldAbsolute(fieldNumber).getRecursionDepth();
//            if (recursionDepth == AbstractPropertyMetaData.UNDEFINED_RECURSION_DEPTH)
//            {
//                recursionDepth = AbstractPropertyMetaData.DEFAULT_RECURSION_DEPTH;
//            }
//
//            // find if it has been overridden in a <fetch-group> definition
//            String fieldName = cmd.getManagedFieldAbsolute(fieldNumber).getName();
//            for (Iterator iter = fetchGroupsContainingField.iterator(); iter.hasNext();)
//            {
//                FetchGroupMetaData fgmd = (FetchGroupMetaData) iter.next();
//                AbstractPropertyMetaData[] fmds = fgmd.getFieldMetaData();
//                for (int i = 0; i < fmds.length; i++)
//                {
//                    AbstractPropertyMetaData fmd = fmds[i];
//                    if (fmd.getName().equals(fieldName))
//                    {
//                        if (fmd.getRecursionDepth() != AbstractPropertyMetaData.UNDEFINED_RECURSION_DEPTH)
//                        {
//                            recursionDepth = fmd.getRecursionDepth();
//                        }
//                    }
//                }
//            }
//            return recursionDepth;
//        }
//
//        /**
//         * Get all fields in the actual fetch plan.
//         * @return an BitSet with the bits set in the absolute position of the
//         * fields
//         */
//        public BitSet getFieldsInActualFetchPlanByBitSet()
//        {
//            return getFieldsInActualFetchPlanByBitSet(cmd);
//        }
//
//        /**
//         * Get all fields in the actual fetch plan for this class and superclasses
//         * @param cmd this AbstractClassMetaData
//         * @return an BitSet with the bits set in the absolute position of the
//         * fields
//         */
//        private BitSet getFieldsInActualFetchPlanByBitSet(AbstractClassMetaData cmd)
//        {
//            BitSet bitSet = plan.getFetchPlanForClass(cmd).getFieldsAbsoluteNumber(cmd.getFetchGroupMetaData());
//            if (cmd.getPersistenceCapableSuperclass() != null)
//            {
//                plan.manageFetchPlanForClass(cmd.getSuperAbstractClassMetaData());
//                bitSet.or(plan.getFetchPlanForClass(cmd.getSuperAbstractClassMetaData()).getFieldsInActualFetchPlanByBitSet(cmd.getSuperAbstractClassMetaData()));
//            }
//            else
//            {
//                // Make sure that we always have the PK fields in the fetch plan = FetchPlanImpl.NONE
//                setNoneFieldNumbers(bitSet);
//            }
//            return bitSet;
//        }
//
//        /**
//         * Get the absolute number of the fields for an array of Fetch Group
//         * @param fgmds The Fetch Groups
//         * @return a BitSet with flags set to true in the field number positions
//         */
//        private BitSet getFieldsAbsoluteNumber(FetchGroupMetaData[] fgmds)
//        {
//            BitSet fieldsNumber = new BitSet(0);
//            if (fgmds != null)
//            {
//                for (int i = 0; i < fgmds.length; i++)
//                {
//                    if (plan.groups.contains(fgmds[i].getName()))
//                    {
//                        fieldsNumber.or(getFieldsAbsoluteNumberInFetchGroup(fgmds[i]));
//                    }
//                }
//            }
//
//            if (plan.groups.contains(FetchPlan.DEFAULT))
//            {
//                setDefaultFieldNumbers(fieldsNumber);
//            }
//            if (plan.groups.contains(FetchPlan.ALL))
//            {
//                setAllFieldNumbers(fieldsNumber);
//            }
//            if (plan.groups.contains(FetchPlan.NONE))
//            {
//                setNoneFieldNumbers(fieldsNumber);
//            }
//            return fieldsNumber;
//        }
//
//        /**
//         * Sets the given list of field numbers to include all the fields
//         * defined in the DEFAULT FetchPlan.
//         * @param fieldsNumber list of field numbers
//         */
//        private void setDefaultFieldNumbers(BitSet fieldsNumber)
//        {
//            for (int i = 0; i < cmd.getDefaultFetchGroupFieldNumbers().length; i++)
//            {
//                fieldsNumber.set(cmd.getDefaultFetchGroupFieldNumbers()[i]);
//            }
//        }
//
//        /**
//         * Sets the given list of field numbers to include all the fields
//         * defined in the ALL FetchPlan.
//         * @param fieldsNumber list of field numbers
//         */
//        private void setAllFieldNumbers(BitSet fieldsNumber)
//        {
//            for (int i = 0; i < cmd.getNoOfManagedFields(); i++)
//            {
//                if (cmd.getManagedField(i).getPersistenceModifier() != FieldPersistenceModifier.NONE)
//                {
//                    fieldsNumber.set(cmd.getAbsoluteFieldNumberForRelativeFieldNumber(i));
//                }
//            }
//        }
//
//        /**
//         * Sets the given list of field numbers to include all the fields
//         * defined in the NONE FetchPlan.
//         * @param fieldsNumber list of field numbers
//         */
//        private void setNoneFieldNumbers(BitSet fieldsNumber)
//        {
//            for (int i = 0; i < cmd.getNoOfManagedFields(); i++)
//            {
//                AbstractPropertyMetaData fmd = cmd.getField(i);
//                if (fmd.isPrimaryKey())
//                {
//                    fieldsNumber.set(fmd.getAbsoluteFieldNumber());
//                }
//            }
//        }
//
//        /**
//         * Get the absolute field number for a particular Fetch Group
//         * @param fgmd The Fetch Group
//         * @return a list of field numbers
//         */
//        private BitSet getFieldsAbsoluteNumberInFetchGroup(FetchGroupMetaData fgmd)
//        {
//            BitSet fieldsNumber = new BitSet(0);
//            for (int i = 0; i < fgmd.getFieldMetaData().length; i++)
//            {
//                int fieldNumber = getFieldNumber(cmd, fgmd.getFieldMetaData()[i].getName());
//                if (fieldNumber == -1)
//                {
//                    throw new JPOXUserException(LOCALISER.msg("FetchPlan.FetchGroupFieldNotFound", 
//                        fgmd.getFieldMetaData()[i].getName(), fgmd.getName(), cmd.getFullClassName())).setFatal();
//                }
//                fieldsNumber.set(fieldNumber);
//            }
//            // fields in nested fetch-groups
//            for (int i = 0; i < fgmd.getFetchGroupMetaData().length; i++)
//            {
//                String nestedGroupName = fgmd.getFetchGroupMetaData()[i].getName();
//                if (nestedGroupName.equals(FetchPlan.DEFAULT)) 
//                {
//                    setDefaultFieldNumbers(fieldsNumber);
//                }
//                else if (nestedGroupName.equals(FetchPlan.ALL)) 
//                {
//                    setAllFieldNumbers(fieldsNumber);
//                }
//                else if (nestedGroupName.equals(FetchPlan.NONE)) 
//                {
//                    setNoneFieldNumbers(fieldsNumber);
//                }
//                else
//                {
//                    FetchGroupMetaData nestedFGMD = getFetchGroupMetaData(cmd,nestedGroupName);
//                    if (nestedFGMD == null)
//                    {
//                        throw new JPOXUserException(LOCALISER.msg("FetchPlan.NestedFetchGroupNotFound", 
//                            fgmd.getFetchGroupMetaData()[i].getName(), fgmd.getName(), cmd.getFullClassName())).setFatal();
//                    }
//                    fieldsNumber.or(getFieldsAbsoluteNumberInFetchGroup(nestedFGMD));
//                }
//            }
//            return fieldsNumber;
//        }
//
//        /**
//         * Find the FetchGroupMetaData with the specified name for the class.
//         * @param cmd the AbstractClassMetaData
//         * @param fetchGroupName the fetch group name
//         * @return the FetchGroup MetaData declaration or null if not found
//         */
//        private FetchGroupMetaData getFetchGroupMetaData(AbstractClassMetaData cmd, String fetchGroupName)
//        {
//            FetchGroupMetaData nestedFGMD = null;
//            nestedFGMD = cmd.getFetchGroupMetaData(fetchGroupName);
//            return nestedFGMD;
//        }
//        
//        /**
//         * gets the field number for a fieldname in this or superclasses
//         * @param cmd this AbstractClassMetaData
//         * @param fieldName the field Name
//         * @return the field number. -1 if not found
//         */
//        private int getFieldNumber(AbstractClassMetaData cmd, String fieldName)
//        {
//            int fieldNumber = cmd.getFieldNumberAbsolute(fieldName);
//            if (fieldNumber == -1 && cmd.getPersistenceCapableSuperclass() != null)
//            {
//                fieldNumber = getFieldNumber(cmd.getSuperAbstractClassMetaData(), fieldName);
//            }
//            return fieldNumber;
//        }
//
//        /**
//         * Get all the fetch groups this field is included
//         * @param fgmds The Fetch Groups
//         * @param absoluteFieldNumber the field absolute number
//         * @return The Fetch Groups
//         */
//        private Set getFetchGroupsForFieldAbsoluteNumber(FetchGroupMetaData[] fgmds, int absoluteFieldNumber)
//        {
//            Set fetchGroups = new HashSet();
//            if (fgmds != null)
//            {
//                for (int i = 0; i < fgmds.length; i++)
//                {
//                    for (int j = 0; j < fgmds[i].getFieldMetaData().length; j++)
//                    {
//                        if (fgmds[i].getFieldMetaData()[j].getName().equals(cmd.getManagedFieldAbsolute(absoluteFieldNumber).getName()))
//                        {
//                            fetchGroups.add(fgmds[i]);
//                        }
//                    }
//                    for (int j = 0; j < fgmds[i].getFetchGroupMetaData().length; j++)
//                    {
//                        fetchGroups.addAll(getFetchGroupsForFieldAbsoluteNumber(fgmds[i].getFetchGroupMetaData(), absoluteFieldNumber));
//                    }
//                }
//            }
//            return fetchGroups;
//        }
//
//        /** Cache fetch groups by field number, as calculating them 
//         *  in getFetchGroupsForFieldAbsoluteNumber() is O(n^2) 
//         *  Map<Integer, Set<FetchGroupMetaData>>
//         */
//        Map fetchGroupsByFieldNumber = new HashMap();
//        
//        /**
//         * Whether to call the post load or not. Checks if fields in actual
//         * FetchPlan where not previouly loaded and the post-load is enabled in
//         * the metadata
//         * @param loadedFields alredy loaded fields
//         * @return if is to call the postLoad
//         */
//        public boolean isToCallPostLoadFetchPlan(boolean[] loadedFields)
//        {
//            int[] fieldsInActualFetchPlan = getFieldsInActualFetchPlan();
//            for (int i = 0; i < fieldsInActualFetchPlan.length; i++)
//            {
//                final int fieldNumber = fieldsInActualFetchPlan[i];
//                // if field in actual fetch plan was not previously loaded
//                if (!loadedFields[fieldNumber])
//                {
//                    // to call jdoPostLoad, field must be in default-fetch-group when DFG is active
//                    if (cmd.getManagedFieldAbsolute(fieldNumber).isDefaultFetchGroup() && groups.contains(FetchPlan.DEFAULT))
//                    {
//                        return true;
//                    }
//                    //or, field must be in a fetch-group which has post-load set to true
//                    else
//                    {
//                        Integer fieldNumberInteger = new Integer(fieldNumber);
//                        Set fetchGroups = (Set) fetchGroupsByFieldNumber.get(fieldNumberInteger);
//                        if (fetchGroups==null) 
//                        {
//                            fetchGroups = getFetchGroupsForFieldAbsoluteNumber(cmd.getFetchGroupMetaData(), fieldNumber);
//                            // cache those precious results from expensive invocation
//                            fetchGroupsByFieldNumber.put(fieldNumberInteger, fetchGroups);
//                        }
//                        for (Iterator it = fetchGroups.iterator(); it.hasNext();)
//                        {
//                            FetchGroupMetaData fgmd = (FetchGroupMetaData) it.next();
//                            if (fgmd.getPostLoad().booleanValue())
//                            {
//                                return true;
//                            }
//                        }
//                    }
//
//                }
//            }
//            return false;
//        }
//    }
}