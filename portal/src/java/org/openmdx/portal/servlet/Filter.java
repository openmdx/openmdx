/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Filter.java,v 1.10 2008/09/26 12:10:39 hburger Exp $
 * Description: Filter
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/26 12:10:39 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;

public class Filter 
extends org.openmdx.base.query.Filter
implements Serializable {

    //-------------------------------------------------------------------------
    public Filter(
    ) {
    }

    //-------------------------------------------------------------------------
    public Filter(
        String name,
        String[] labels,
        String groupName,
        String iconKey,
        Integer[] order,
        Condition[] condition,
        OrderSpecifier[] orderSpecifier,
        Object[] context
    ) {
        super(
            condition,
            removeDuplicateOrderSpecifiers(name, orderSpecifier, context)
        );
        this.name = name;
        this.labels = labels;
        this.groupName = groupName;
        this.iconKey = iconKey;
        this.order = order;
    }

    //-------------------------------------------------------------------------
    private static OrderSpecifier[] removeDuplicateOrderSpecifiers(
        String name,
        OrderSpecifier[] orderSpecifier,
        Object[] context
    ) {
        if(orderSpecifier == null) return null;
        List<String> features = new ArrayList<String>();
        List<OrderSpecifier> specifiers = new ArrayList<OrderSpecifier>();
        boolean hasDuplicates = false;
        for(int i = 0; i < orderSpecifier.length; i++) {
            if(!features.contains(orderSpecifier[i].getFeature())) {
                specifiers.add(orderSpecifier[i]);
                features.add(orderSpecifier[i].getFeature());            
            }
            else {
                hasDuplicates = true;
            }
        }
        if(hasDuplicates) {
            AppLog.warning("Filter has duplicate order specifiers [name=" + name + "; context=" + Arrays.asList(context) + "]", Arrays.asList(orderSpecifier));
        }
        return (OrderSpecifier[])specifiers.toArray(new OrderSpecifier[specifiers.size()]);
    }

    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.name;
    }

    //-------------------------------------------------------------------------
    public void setName(
        String newValue
    ) {
        this.name = newValue;
    }

    //-------------------------------------------------------------------------
    public String[] getLabel(
    ) {
        return this.labels;
    }

    //-------------------------------------------------------------------------
    public String getLabel(
        short index
    ) {
        return (this.labels != null) && (index < this.labels.length)
            ? this.labels[index]
            : (this.labels != null) && (this.labels.length > 0)
                ? this.labels[0]
                : this.name;
    }

    //-------------------------------------------------------------------------
    public void setLabel(
        String[] newValue
    ) {
        this.labels = newValue;
    }

    //-------------------------------------------------------------------------
    public void setLabel(
        int index,
        String value
    ) {
        if(this.labels == null) {
            this.labels = new String[index+1];
        }
        else if(index > this.labels.length) {
            String[] newValue = new String[index+1];          
            System.arraycopy(this.labels, 0, newValue, 0, this.labels.length);
            this.labels = newValue;
        }
        this.labels[index] = value;
    }

    //-------------------------------------------------------------------------
    public String getIconKey(
    ) {
        return this.iconKey;
    }

    //-------------------------------------------------------------------------
    public void setIconKey(
        String iconKey
    ) {
        this.iconKey = iconKey;
    }

    //-------------------------------------------------------------------------
    public String getGroupName(
    ) {
        return this.groupName;
    }

    //-------------------------------------------------------------------------
    public Integer getOrder(
        int index
    ) {
        return this.order == null
        ? new Integer(0)
        : this.order[index];
    }

    //-------------------------------------------------------------------------
    public Integer[] getOrder(
    ) {
        return this.order;
    }

    //-------------------------------------------------------------------------
    public void setOrder(
        Integer[] order
    ) {
        this.order = order;
    }

    //-------------------------------------------------------------------------
    public void setOrder(
        int index,
        Integer value
    ) {
        if(this.order == null) {
            this.order = new Integer[index+1];
        }
        else if(index > this.order.length) {
            Integer[] newOrder = new Integer[index+1];          
            System.arraycopy(this.order, 0, newOrder, 0, this.order.length);
            this.order = newOrder;
        }
        this.order[index] = value;
    }

    //-------------------------------------------------------------------------
    public void setGroupName(
        String groupName
    ) {
        this.groupName = groupName;
    }

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), 
                this.groupName + '-' + this.labels,
                TO_STRING_FIELDS,
                new Object[]{
                    this.name,
                    this.labels,
                    this.groupName,
                    this.iconKey,
                    this.order,
                    Records.getRecordFactory().asIndexedRecord(
                        Condition.class.getName(),
                        "",
                        this.getCondition()
                    ),
                    Records.getRecordFactory().asIndexedRecord(
                        OrderSpecifier.class.getName(),
                        "",
                        this.getOrderSpecifier()
                    )
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * filter has a parameter (filter value (?))
     */
    public boolean hasParameter(
    ) {
        for(int i = 0; i < this.getCondition().length; i++) {
            Condition condition = this.getCondition()[i];
            for(int j = 0; j < condition.getValue().length; j++) {
                if("?".equals(condition.getValue()[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257572814784770101L;

    private String name = null;
    private String[] labels = null;
    private String groupName = null;
    private String iconKey = null;
    private Integer[] order = null;

    private static final String[] TO_STRING_FIELDS = {
        "name",
        "labels",
        "groupName",
        "iconKey",
        "order",
        "condition",
        "orderSpecifier"
    };
    
}

//--- End of File -----------------------------------------------------------
