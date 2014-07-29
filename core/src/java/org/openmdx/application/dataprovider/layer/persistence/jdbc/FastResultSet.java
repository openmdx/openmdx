/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Fast Result Set
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * FastResultSet
 *
 * Optimizes the access to a ResultSet and guarantees that a column is read
 * at most once. This is required because for certain jdbc drivers (e.g. 
 * JdbcOdbc driver in .NET) a) rs.getObject() is very slow and b) may be
 * called at most once.
 */
public class FastResultSet {

    //-----------------------------------------------------------------------
    public FastResultSet(
        AbstractDatabase_1 database,
        ResultSet rs
    ) throws SQLException {
        this.database = database;
        this.rs = rs;
        this.columnNames = FastResultSet.getColumnNames(
            rs.getMetaData()
        );
    }

    //-----------------------------------------------------------------------
    public FastResultSet(
        AbstractDatabase_1 database,
        ResultSet rs,
        List<String> columnNames
    ) throws SQLException {
        this.database = database;
        this.rs = rs;
        this.columnNames = columnNames;
    }

    //-----------------------------------------------------------------------
    static public List<String> getColumnNames(
        ResultSetMetaData rsmd
    ) throws SQLException {
        List<String> columnNames = new ArrayList<String>();
        for(
            int columnIndex = 1, columnCount = rsmd.getColumnCount(); 
            columnIndex <= columnCount; 
            columnIndex++
        ) {
            columnNames.add(
                rsmd.getColumnName(columnIndex).toLowerCase()
            );
        }
        return columnNames;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads specified column from result set. Guarantees that columns 
     * OBJECT_OID, OBJECT_RID and OBJECT_IDX are read at most once otherwise
     * error 'ResultSet can not re-read row data for column' is thrown by
     * certain JDBC drivers.
     */
    public Object getObject(
        String columnName
    ) throws SQLException {
        String columnNameLowerCase = columnName.toLowerCase();
        Object value = this.columnValues.get(columnNameLowerCase);
        if(value == null) {
            int index = this.columnNames.indexOf(columnNameLowerCase);
            if(index < 0) {
                throw new SQLException("AbstractDatabase_1: column " + columnName + " not found");
            }
            // get all column values up to requested index
            while(this.currentColumnIndex < index) {
                this.currentColumnIndex++;
                this.columnValues.put(
                    this.columnNames.get(this.currentColumnIndex),
                    value = toJdbcObject(
                        this.rs.getObject(this.currentColumnIndex+1)
                    )
                );
            }
        }
        if(value instanceof String) {
            return this.database.internalizeStringValue(
                columnName,
                (String)value
            );
        }
        else {
            return value;
        }
    }

    //-----------------------------------------------------------------------
    public void reset(
    ) throws SQLException {
        this.columnValues.clear();
        this.currentColumnIndex = -1;
    }

    //-----------------------------------------------------------------------
    public boolean next(
    ) throws SQLException {
        boolean hasMore = this.rs.next();
        this.reset();
        return hasMore;
    }

    public boolean isAbsolutePositioningEnabled(
    ) throws SQLException{
    	return this.rs.getType() != ResultSet.TYPE_FORWARD_ONLY;
    }
    
    //-----------------------------------------------------------------------
    public boolean absolute(
        int position
    ) throws SQLException {
        boolean hasMore = this.rs.absolute(position);
        this.reset();
        return hasMore;
    }

    //-----------------------------------------------------------------------
    public List<String> getColumnNames(
    ) {
        return this.columnNames;
    }

    //-----------------------------------------------------------------------
    protected Object toJdbcObject(
        Object nativeObject
    ) throws SQLException{
        return Datums.isDatum(nativeObject) ? Datums.toJdbcObject(nativeObject) : nativeObject;
    }

    //-----------------------------------------------------------------------
    // Variables
    //-----------------------------------------------------------------------
    private final ResultSet rs;
    private final AbstractDatabase_1 database;
    private int currentColumnIndex = -1;
    private final Map<String,Object> columnValues = new HashMap<String,Object>();
    private final List<String> columnNames;

}
