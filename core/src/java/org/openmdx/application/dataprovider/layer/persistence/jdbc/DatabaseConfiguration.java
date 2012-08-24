/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DatabaseConfiguration 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2011, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */

package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.security.MessageDigest;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.sql.DataSource;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/**
 * The Database Configuration
 */
public class DatabaseConfiguration {

	/**
	 * Constructor
	 * 
	 * @param namespaceId
	 * @param referenceIdFormat
	 * @param useNormalizedReferences
	 * @param useNormalizedObjectIds
	 * @param useViewsForRedundantColumns
	 * @param usePreferences
	 * @param cascadeDeletes
	 * @param connectionManager
	 * @param configuration
	 * 
	 * @throws ServiceException
	 */
    public DatabaseConfiguration(
        String namespaceId,
        String referenceIdFormat,
        boolean useNormalizedReferences,
        boolean useNormalizedObjectIds,
        boolean useViewsForRedundantColumns, 
        boolean usePreferences, 
        boolean cascadeDeletes, 
        DataSource connectionManager, 
        Configuration configuration
    ) throws ServiceException {
        this.namespaceId = namespaceId;
        this.referenceIdFormat = referenceIdFormat;
        this.useNormalizedReferences = useNormalizedReferences;
        this.useNormalizedObjectIds = useNormalizedObjectIds;
        this.useViewsForRedundantColumns = useViewsForRedundantColumns;
        this.cascadeDeletes = cascadeDeletes;
        this.connectionManager = connectionManager;
        this.configuration = configuration;
        this.usePreferences = usePreferences;
        if(!usePreferences) {
        	initialize();
        }
    }

    /**
     * Load lazily if preferences are used
     * 
     * @throws ServiceException
     */
    public void load(
    ) throws ServiceException {
    	if(this.usePreferences) {
    		loadLazily();
    	}
    }

    /**
     * Load lazily
     * 
     * @throws ServiceException 
     */
    private synchronized void loadLazily(
    ) throws ServiceException{
		if(!this.initialized) {
    		loadPreferences();
    		initialize();
		}
    }
    
    /**
     * Read configuration entries from table 'Preferences' if available
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadPreferences(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String currentStatement = 
			"SELECT * FROM prefs_Preference" + 
			" WHERE (object_rid = ?) AND (object_oid IN" +
			" (SELECT object_oid FROM prefs_Preference" + 
			" WHERE (parent = ?) AND (object_rid IN (?)) AND (object_idx = 0)))" + 
			" ORDER BY object_rid, object_oid, object_idx";
		try {
			conn = this.connectionManager.getConnection();
			ps = conn.prepareStatement(currentStatement);
			ps.setString(1, "preference/" + this.namespaceId);
			ps.setString(2, DataproviderLayers.toString(DataproviderLayers.PERSISTENCE));
			ps.setString(3, "preference/" + this.namespaceId);
			rs = ps.executeQuery();
			String objectClass = null;
			String name = null;
			while(rs.next()) {
				int index = rs.getInt("object_idx");
				if(index == 0) {
					objectClass = rs.getString("object__class");                        
					name = rs.getString("name");
				}
				SparseArray values = null;
				if(this.configuration.containsEntry(name)) {
					values = this.configuration.entries().get(name);
				} else {
					this.configuration.entries().put(
						name,
						values = new TreeSparseArray()
					);
				}
				Object newValue;
				if("org:openmdx:preferences1:StringPreference".equals(objectClass)) {
					Object s = rs.getObject("string_value");                        
					newValue = 
						s == null ? null : 
						s instanceof Clob ? ((Clob)s).getSubString(1L, (int)((Clob)s).length()) : 
						(String)s;
				} else if("org:openmdx:preferences1:IntegerPreference".equals(objectClass)) {
					newValue = new Integer(rs.getInt("integer_value"));
				} else if("org:openmdx:preferences1:DecimalPreference".equals(objectClass)) {
					newValue = rs.getBigDecimal("decimal_value");
				} else if("org:openmdx:preferences1:BooleanPreference".equals(objectClass)) {
					newValue = Boolean.valueOf("##true##".equals(rs.getString("boolean_value")));
				} else if("org:openmdx:preferences1:UriPreference".equals(objectClass)) {
					newValue = new Path(rs.getString("uri_value"));
				} else {
					newValue = null;
				}
				values.put(
					index,
					newValue
				);
			}
		} catch(Exception e) {
			SysLog.warning(
				"Did not retrieve preferences from database. " +
				"Execution of statement failed. " +
				"Maybe you should set the configuration property 'usePreferencesTable' to 'false'", 
				currentStatement
			);
		} finally {
			if(rs != null) try {
				rs.close();
			} catch(Throwable ex) {
				// ignore
			}
			if(ps != null) try {  
				ps.close();
			} catch(Throwable ex) {
				// ignore
			}      
			if(conn != null) try {
				conn.close();
			} catch(Throwable ex) {
				// ignore
			}
		}
    }
    
    private void initialize(
	) throws ServiceException {
		// COLUMN_NAME_FROM, COLUMN_NAME_TO mapping
		for(
			ListIterator<?> i = this.configuration.values(LayerConfigurationEntries.COLUMN_NAME_FROM).populationIterator();
			i.hasNext();
		) {
			int index = i.nextIndex();
			this.addColumnNameMapping(
				(String)i.next(),
				(String)this.configuration.values(LayerConfigurationEntries.COLUMN_NAME_TO).get(index)
			);
		}
		SysLog.detail("fromToColumnNameMapping", this.fromToColumnNameMapping);
		SysLog.detail("toFromColumnNameMapping", this.toFromColumnNameMapping);

		// Read configuration entries and create DbObjectConfigurations
		for(
			ListIterator<?> i = this.configuration.values(LayerConfigurationEntries.TYPE).populationIterator();
			i.hasNext();
		) {
			int index = i.nextIndex();

			Object type = i.next();
			SysLog.detail("Reading configuration for type", type);

			// typeName
			Object typeName = configuration.values(LayerConfigurationEntries.TYPE_NAME).get(index);
			if(typeName == null) {
				//
				// if typeName is not configured calculate as MD5 hash or derive it from the index as last resort
				//
				typeName = newTypeName(type, index);
			}
			// dbObject
			Object dbObject = this.configuration.values(LayerConfigurationEntries.DB_OBJECT).get(index);
			// dbObject2
			Object dbObject2 = normalize(this.configuration.values(LayerConfigurationEntries.DB_OBJECT_2).get(index));
			// dbObjectFormat
			Object dbObjectFormat = this.configuration.values(LayerConfigurationEntries.DB_OBJECT_FORMAT).get(index);
			// pathNormalizeLevel
			Object pathNormalizeLevel = this.configuration.values(LayerConfigurationEntries.PATH_NORMALIZE_LEVEL).get(index);
			// dbObjectForQuery
			Object dbObjectForQuery = normalize(this.configuration.values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY).get(index));
			// dbObjectForQuery2
			Object dbObjectForQuery2 = normalize(this.configuration.values(LayerConfigurationEntries.DB_OBJECT_FOR_QUERY_2).get(index));
			// dbObjectsForQueryJoinColumn
			Object dbObjectsForQueryJoinColumn = normalize(this.configuration.values(LayerConfigurationEntries.DB_OBJECTS_FOR_QUERY_JOIN_COLUMN).get(index));
			// dbObjectHint
			Object dbObjectHint = normalize(this.configuration.values(LayerConfigurationEntries.DB_OBJECT_HINT).get(index));
			// objectIdPattern
			Object objectIdPattern = normalize(this.configuration.values(LayerConfigurationEntries.OBJECT_ID_PATTERN).get(index));
			// joinTable
			Object joinTable = normalize(this.configuration.values(LayerConfigurationEntries.JOIN_TABLE).get(index));
			// joinColumnEnd1
			Object joinColumnEnd1 = normalize(this.configuration.values(LayerConfigurationEntries.JOIN_COLUMN_END1).get(index));
			// joinColumnEnd2
			Object joinColumnEnd2 = normalize(this.configuration.values(LayerConfigurationEntries.JOIN_COLUMN_END2).get(index));
			// unitOfWorkProvider
			Object unitOfWorkProvider = normalize(this.configuration.values(LayerConfigurationEntries.UNIT_OF_WORK_PROVIDER).get(index));
			// removableRidPrefix
			Object removableReferenceIdPrefix = normalize(this.configuration.values(LayerConfigurationEntries.REMOVABLE_REFERENCE_ID_PREFIX).get(index));
			// Tells whether one shouldn't try absolute positioning
			boolean disableAboslutePositioning = Boolean.TRUE.equals(this.configuration.values(LayerConfigurationEntries.DISABLE_ABSOLUTE_POSITIONING).get(index));
			Object referenceIdPattern = normalize(this.configuration.values(LayerConfigurationEntries.REFERENCE_ID_PATTERN).get(index));
			try {
				SysLog.detail(
					"Retrieved configuration", 
					Records.getRecordFactory().asMappedRecord(
						type.toString(), // recordName, 
						null, // recordShortDescription
						new String[]{"typeName", "dbObject", "dbObject2", "pathNormalizeLevel", "dbObjectForQuery", "dbObjectForQuery2", "dbObjectHint", "objectIdPattern", "unitOfWorkProvider", "referenceIdPattern"},
						new Object[]{typeName, dbObject, dbObject2, pathNormalizeLevel, dbObjectForQuery, dbObjectForQuery2, dbObjectHint, objectIdPattern, unitOfWorkProvider, referenceIdPattern}
					)                    
				);
			} catch(Exception e) {
				// Ignore
			}
			if(
				(type instanceof Path || type instanceof String) &&
				(typeName instanceof String) &&
				(dbObject == null || dbObject instanceof String) &&                
				(dbObject2 == null || dbObject2 instanceof String) &&                
				(pathNormalizeLevel == null || pathNormalizeLevel instanceof Number) &&
				(dbObjectForQuery == null || dbObjectForQuery instanceof String) &&
				(dbObjectForQuery2 == null || dbObjectForQuery2 instanceof String) &&
				(dbObjectHint == null || dbObjectHint instanceof String) &&
				(objectIdPattern == null || objectIdPattern instanceof String) &&
				(unitOfWorkProvider == null || unitOfWorkProvider instanceof String) &&
				(removableReferenceIdPrefix == null || removableReferenceIdPrefix instanceof String) ||
				(joinTable == null || joinTable instanceof String) &&                
				(joinColumnEnd1 == null || joinColumnEnd1 instanceof String) &&                
				(joinColumnEnd2 == null || joinColumnEnd2 instanceof String) &&
				(referenceIdPattern == null || referenceIdPattern instanceof String)
			) {
				if(
					LayerConfigurationEntries.REFERENCE_ID_FORMAT_TYPE_WITH_PATH_COMPONENTS.equals(this.referenceIdFormat) &&
					(pathNormalizeLevel != null) &&
					((Number)pathNormalizeLevel).intValue() < 2
				) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION, 
						"The database plugin option " + LayerConfigurationEntries.REFERENCE_ID_FORMAT + "=" + LayerConfigurationEntries.REFERENCE_ID_FORMAT_TYPE_WITH_PATH_COMPONENTS + " requires path normalize levels >= 2",
						new BasicException.Parameter("index", index),
						new BasicException.Parameter("path normalize level", pathNormalizeLevel)
					);                                                  
				}
				if(
					this.useNormalizedReferences &&
					(pathNormalizeLevel != null) &&
					((Number)pathNormalizeLevel).intValue() < 2
				) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION, 
						"The database plugin option " + LayerConfigurationEntries.USE_NORMALIZED_REFERENCES + "=" + Boolean.TRUE + " requires path normalize levels >= 2",
						new BasicException.Parameter("index", index),
						new BasicException.Parameter("path normalize level", pathNormalizeLevel)
					);                                                  
				}
				// AUTONUM_COLUMN
				List<String> autonumColumns = new ArrayList<String>();
				String autonumColumnPrefix = (String)dbObject + ".";
				for(
					ListIterator<?> j = this.configuration.values(LayerConfigurationEntries.AUTONUM_COLUMN).populationIterator();
					j.hasNext();
				) {
					String autonumColumn = (String)j.next();
					if((dbObject != null) && autonumColumn.startsWith(autonumColumnPrefix)) {
						autonumColumns.add(
							autonumColumn.substring(autonumColumn.indexOf(".") + 1)
						);
					}
				}
				// Create db object configuration entry
				DbObjectConfiguration entry = new DbObjectConfiguration(
					type instanceof Path ? (Path)type : new Path((String)type),
					(String)typeName,
					(String)dbObject,
					(String)dbObject2,
					(String)dbObjectFormat,
					(String)dbObjectForQuery,
					(String)dbObjectForQuery2,
					(String)dbObjectsForQueryJoinColumn,
					pathNormalizeLevel == null ? -1 : ((Number)pathNormalizeLevel).intValue(),
					(String)dbObjectHint,
					(String)objectIdPattern,
					autonumColumns,
					(String)joinTable,
					(String)joinColumnEnd1,
					(String)joinColumnEnd2, 
					(String)unitOfWorkProvider, 
					(String)removableReferenceIdPrefix, 
					disableAboslutePositioning, 
					(String)referenceIdPattern
				);
				SysLog.detail("Configuration added", entry);
				// Check that no two entries have the same type name
				DbObjectConfiguration entry2 = null;
				if((entry2 = dbObjectConfigurations.get(entry.getTypeName())) != null) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION, 
						"Duplicate type name",
						new BasicException.Parameter("entry.1", entry.getType()),
						new BasicException.Parameter("entry.2", entry2.getType())
					);                            
				}
				dbObjectConfigurations.put(
					entry.getTypeName(),
					entry
				);
			} else {
				new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INVALID_CONFIGURATION, 
					"[type,typeName,dbObject,dbObjectSecondary,dbObjectFormat,dbObjectForQuerySecondary,pathNormalizeLevel,dbObjectHint,objectIdPattern,unitOfWorkProvider,removableReferenceIdPrefix] must be of type [(String|Path),String,String,String,String,String,String,Number,String,String,String,String,String]",
					new BasicException.Parameter("type configuration", type, dbObject, dbObject2, dbObjectFormat, dbObjectForQuery, dbObjectForQuery2, pathNormalizeLevel, dbObjectHint, objectIdPattern, unitOfWorkProvider, removableReferenceIdPrefix, referenceIdPattern)
				).log();
			}
		}                
		SysLog.detail("Configuration", this.dbObjectConfigurations.values());          
    	this.initialized = true;
    }

    /**
     * Normalize empty <code>String</code>s to <code>null</code>
     * 
     * @param value the value to be normalized

     * @return the value unles sthe value is empty; <code>null</code> in case of an empty value
     */
    private Object normalize(
    	Object value
    ){
    	return "".equals(value) ? null : value;
    }
    
    //---------------------------------------------------------------------------
    private void addColumnNameMapping(
        String columnNameFrom,
        String columnNameTo
    ) throws ServiceException {
        if(columnNameTo == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "no columnNameTo defined for columnNameFrom",
                new BasicException.Parameter("columnNameFrom", columnNameFrom)
            );
        }
        // fromToColumnNameMapping
        if(this.fromToColumnNameMapping.containsKey(columnNameFrom)) {
            SysLog.info("duplicate columnNameFrom defined", columnNameFrom);
        }
        else {
            this.fromToColumnNameMapping.put(
                columnNameFrom,
                columnNameTo
            );
        }
        // toFromColumnNameMapping
        if(
            this.toFromColumnNameMapping.containsKey(columnNameTo) ||
            this.toFromColumnNameMapping.containsKey(columnNameTo.toUpperCase())
        ) {
            SysLog.info("duplicate columnNameTo defined", columnNameFrom);
        }
        else {
            this.toFromColumnNameMapping.put(
                columnNameTo,
                columnNameFrom
            );
            this.toFromColumnNameMapping.put(
                columnNameTo.toUpperCase(),
                columnNameFrom
            );
        }
    }

    /**
     * Fallback type names
     * 
     * @param type
     * @param index
     * 
     * @return the fallback type name
     */
    @SuppressWarnings("deprecation")
    protected String newTypeName(
        Object type,
        int index
    ){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(new Path(type.toString()).toXri().getBytes("UTF-8"));
            return Base64.encode(md.digest());
        }  catch(Exception e) {
            //
            // Set to index if hash can not be calculated for some reason
            //
            return Integer.toString(index);
        }
    }
    
    //---------------------------------------------------------------------------
    public String buildReferenceId(
        Path resourceIdentifier
    ) throws ServiceException{
        DbObjectConfiguration dbObjectConfiguration = getDbObjectConfiguration(resourceIdentifier);
        Path type = dbObjectConfiguration.getType();
        String typeName = dbObjectConfiguration.getTypeName();
        if(type.size() >= 2 && resourceIdentifier.isLike(type.getParent())) {
        	List<String> equalRid = new ArrayList<String>();
        	List<String> likeRid = new ArrayList<String>();
            equalRid.add(typeName);
            likeRid.add(AbstractDatabase_1.escape(typeName));
            boolean subtree = false;
            for(
                int l = 0;
                l < resourceIdentifier.size();
                l++
            ) {
                String component = resourceIdentifier.get(l);
                if(":*".equals(component)) {                  
                    if(!subtree){
                        likeRid.add("%");
                        subtree = true;
                        equalRid = null;
                    } 
                } else if(!component.equals(type.get(l))) {
                    if(equalRid != null) {
                        equalRid.add(component);
                    }
                    likeRid.add(AbstractDatabase_1.escape(component));
                    subtree = false;
                }
            }
            List<String> rid = equalRid == null ? likeRid : equalRid; 
            return new Path(
            	rid.toArray(new String[rid.size()])
            ).toString();
        } else {
            return null;
        }
    }
    
    //---------------------------------------------------------------------------
    public String buildObjectId(
        Path resourceIdentifier
    ) throws ServiceException{
        DbObjectConfiguration dbObjectConfiguration = getDbObjectConfiguration(resourceIdentifier);
        Path type = dbObjectConfiguration.getType();
        List<String> target = new ArrayList<String>();
        target.add(dbObjectConfiguration.getTypeName());
        for(
            int l = 0, lLimit = resourceIdentifier.size();
            l < lLimit;
            l++
        ) {
            String component = resourceIdentifier.get(l);
            if(!component.equals(type.get(l))) {
                target.add(buildObjectId(component));
            } else if (":*".equals(component)) {
                target.add("%");
            }
        }
        return new Path(target.toArray(new String[target.size()])).toString();
    }     

    //---------------------------------------------------------------------------
    public String buildObjectId(
        String component
    ) throws ServiceException{
        return isConvertible(component) ? buildObjectId(new Path(component)) : component;
    }
    
    //---------------------------------------------------------------------------
    public Path buildResourceIdentifier(
        String id, 
        boolean reference
    ) throws ServiceException {
        Path source = new Path(id);
        List<String> target = new ArrayList<String>();
        if(source.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "No components found for reference id",
                new BasicException.Parameter("objectId",id)
            );          
        }
        DbObjectConfiguration dbObjectConfiguration = getDbObjectConfiguration(
            source.get(0)
        );
        Path type = dbObjectConfiguration.getType();
        for(
            int i = 0, pos = 1, iLimit = type.size() - (reference ? 1 : 0); 
            i < iLimit; 
            i++
        ) {
            if(":*".equals(type.get(i))) {
                if(pos >= source.size()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "Reference not valid for type",
                        new BasicException.Parameter("objectId",id),
                        new BasicException.Parameter("type", type)
                    );                            
                }
                String component = source.get(pos++);
                target.add(
                    isConvertible(component) ? buildResourceIdentifier(component, reference).toString() : component
                );
            } else {
                target.add(type.get(i));
            }
        }
        return new Path(target.toArray(new String[target.size()]));
    }
    
    //---------------------------------------------------------------------------
    public boolean normalizeObjectIds(
    ) {
        return this.useNormalizedObjectIds;
    }
    
    //---------------------------------------------------------------------------
    private boolean isConvertible(
        String segment
    ){
        return 
            segment != null &&
            segment.indexOf('/') > 0 &&
            !segment.startsWith("("); 
    }
    
    //---------------------------------------------------------------------------
    public DbObjectConfiguration getDbObjectConfiguration(
        Path path
    ) throws ServiceException {
        this.load();        
        Path objectPath = path.size() % 2 == 0 ? path.getChild(":*") : path;
        for(DbObjectConfiguration dbObjectConfiguration: this.dbObjectConfigurations.values()) {
            Path type = dbObjectConfiguration.getType();
            boolean matches = 
                ((objectPath.size() == 1) && (type.size() == 1)) ||
                objectPath.isLike(type);
            if(matches) {
                return dbObjectConfiguration;
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE, 
            "No type configuration found for path",
            new BasicException.Parameter("path", objectPath),            
            new BasicException.Parameter("configuration", this.dbObjectConfigurations)
        );
    }

    //---------------------------------------------------------------------------
    public DbObjectConfiguration getDbObjectConfiguration(
        String typeName
    ) throws ServiceException {
        this.load();
        DbObjectConfiguration dbObjectConfiguration = this.dbObjectConfigurations.get(typeName);
        if(dbObjectConfiguration == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "no type configuration found for type name",
                new BasicException.Parameter("type name", typeName)
            );
        }
        else {
            return dbObjectConfiguration;
        }
    }

    //---------------------------------------------------------------------------
    public Collection<DbObjectConfiguration> getDbObjectConfigurations(
    ) {
        return this.dbObjectConfigurations.values();
    }

    //---------------------------------------------------------------------------
    public Map<String,String> getFromToColumnNameMapping(
    ) {
        return this.fromToColumnNameMapping;
    }

    //---------------------------------------------------------------------------
    public Map<String,String> getToFromColumnNameMapping(
    ) {
        return this.toFromColumnNameMapping;
    }
    
    //---------------------------------------------------------------------------
    public boolean useViewsForRedundantColumns(){
    	return this.useViewsForRedundantColumns;
    }
    
    //---------------------------------------------------------------------------
    public boolean cascadeDeletes(){
        return this.cascadeDeletes;
    }
    
    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private boolean initialized = false;
    private final boolean usePreferences;
    private final String namespaceId;    
    private final String referenceIdFormat;   
    private final boolean cascadeDeletes;
    private final boolean useNormalizedReferences;
    private final boolean useNormalizedObjectIds;
    private final boolean useViewsForRedundantColumns;
    private final Configuration configuration;
    private final DataSource connectionManager;
    // Maps short column names to long column names.
    private final Map<String,String> fromToColumnNameMapping = new HashMap<String, String>();
    private final Map<String,String> toFromColumnNameMapping = new HashMap<String, String>();

    // Map containing entries instanceof TypeConfigurationEntry.
    // Key is the type name
    private final Map<String,DbObjectConfiguration> dbObjectConfigurations = new HashMap<String,DbObjectConfiguration>();

}