/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DatabaseConfiguration 
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */

package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObjectConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * The Database Configuration
 */
public class DatabaseConfiguration {

	/**
	 * Constructor
	 * 
	 * @param database
	 * 
	 * @throws ServiceException
	 */
    public DatabaseConfiguration(
        Database_1_0 database
    ) throws ServiceException {
        this.database = database;
        this.initialize();
    }
    
    private void initialize(
	) throws ServiceException {
		// COLUMN_NAME_FROM, COLUMN_NAME_TO mapping
		for(
			ListIterator<String> i = this.database.getColumnNameFrom().populationIterator();
			i.hasNext();
		) {
			Integer index = Integer.valueOf(i.nextIndex());
			this.addColumnNameMapping(
				i.next(),
				this.database.getColumnNameTo().get(index)
			);
		}
		SysLog.detail("fromToColumnNameMapping", this.fromToColumnNameMapping);
		SysLog.detail("toFromColumnNameMapping", this.toFromColumnNameMapping);
		// Read configuration entries and create DbObjectConfigurations
		for(
			ListIterator<Path> i = this.database.getType().populationIterator();
			i.hasNext();
		) {
			Integer index = Integer.valueOf(i.nextIndex());
			Path type = i.next();
			SysLog.detail("Reading configuration for type", type);
			// typeName
			Object typeName = this.database.getTypeName().get(index);
			if(typeName == null) {
				//
				// if typeName is not configured calculate as MD5 hash or derive it from the index as last resort
				//
				typeName = newTypeName(type, index.intValue());
			}
			String dbObject = this.database.getDbObject().get(index);
			String dbObject2 = normalize(this.database.getDbObject2().get(index));
			String dbObjectFormat = this.database.getDbObjectFormat().get(index);
			Integer pathNormalizeLevel = this.database.getPathNormalizeLevel().get(index);
			String dbObjectForQuery = normalize(this.database.getDbObjectForQuery().get(index));
			String dbObjectForQuery2 = normalize(this.database.getDbObjectForQuery2().get(index));
			String dbObjectsForQueryJoinColumn = normalize(this.database.getDbObjectsForQueryJoinColumn().get(index));
			String dbObjectHint = normalize(this.database.getDbObjectHint().get(index));
			String objectIdPattern = normalize(this.database.getObjectIdPattern().get(index));
			String joinTable = normalize(this.database.getJoinTable().get(index));
			String joinColumnEnd1 = normalize(this.database.getJoinColumnEnd1().get(index));
			String joinColumnEnd2 = normalize(this.database.getJoinColumnEnd2().get(index));
			String unitOfWorkProvider = normalize(this.database.getUnitOfWorkProvider().get(index));
			String removableReferenceIdPrefix = normalize(this.database.getRemovableReferenceIdPrefix().get(index));
			boolean disableAboslutePositioning = Boolean.TRUE.equals(this.database.getDisableAbsolutePositioning().get(index));
			String referenceIdPattern = normalize(this.database.getReferenceIdPattern().get(index));
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
			if(typeName instanceof String) {
				if(
					LayerConfigurationEntries.REFERENCE_ID_FORMAT_TYPE_WITH_PATH_COMPONENTS.equals(this.database.getReferenceIdFormat()) &&
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
					this.database.isUseNormalizedReferences() &&
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
				String autonumColumnPrefix = dbObject + ".";
				for(
					ListIterator<String> j = this.database.getAutonumColumn().populationIterator();
					j.hasNext();
				) {
					String autonumColumn = j.next();
					if((dbObject != null) && autonumColumn.startsWith(autonumColumnPrefix)) {
						autonumColumns.add(
							autonumColumn.substring(autonumColumn.indexOf(".") + 1)
						);
					}
				}
				// Create db object configuration entry
				DbObjectConfiguration entry = new DbObjectConfiguration(
					type,
					(String)typeName,
					dbObject,
					dbObject2,
					dbObjectFormat,
					dbObjectForQuery,
					dbObjectForQuery2,
					dbObjectsForQueryJoinColumn,
					pathNormalizeLevel == null ? -1 : ((Number)pathNormalizeLevel).intValue(),
					dbObjectHint,
					objectIdPattern,
					autonumColumns,
					joinTable,
					joinColumnEnd1,
					joinColumnEnd2, 
					unitOfWorkProvider, 
					removableReferenceIdPrefix, 
					disableAboslutePositioning, 
					referenceIdPattern
				);
				SysLog.detail("Configuration added", entry);
				// Check that no two entries have the same type name
				DbObjectConfiguration entry2 = null;
				if((entry2 = this.dbObjectConfigurations.get(entry.getTypeName())) != null) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION, 
						"Duplicate type name",
						new BasicException.Parameter("entry.1", entry.getType()),
						new BasicException.Parameter("entry.2", entry2.getType())
					);                            
				}
				this.dbObjectConfigurations.put(
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
    }

    /**
     * Normalize empty <code>String</code>s to <code>null</code>
     * 
     * @param value the value to be normalized

     * @return the value unles sthe value is empty; <code>null</code> in case of an empty value
     */
    private String normalize(
    	String value
    ) {
    	return "".equals(value) ? null : value;
    }
    
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
        } else {
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
        } else {
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
            likeRid.add(Database_2.escape(typeName));
            boolean subtree = false;
            for(
                int l = 0;
                l < resourceIdentifier.size();
                l++
            ) {
                String component = resourceIdentifier.getSegment(l).toClassicRepresentation();
                if(":*".equals(component)) {                  
                    if(!subtree){
                        likeRid.add("%");
                        subtree = true;
                        equalRid = null;
                    } 
                } else if(!component.equals(type.getSegment(l).toClassicRepresentation())) {
                    if(equalRid != null) {
                        equalRid.add(component);
                    }
                    likeRid.add(Database_2.escape(component));
                    subtree = false;
                }
            }
            List<String> rid = equalRid == null ? likeRid : equalRid; 
            return new Path(
            	rid.toArray(new String[rid.size()])
            ).toClassicRepresentation();
        } else {
            return null;
        }
    }
    
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
            String component = resourceIdentifier.getSegment(l).toClassicRepresentation();
            if(!component.equals(type.getSegment(l).toClassicRepresentation())) {
                target.add(buildObjectId(component));
            } else if (":*".equals(component)) {
                target.add("%");
            }
        }
        return new Path(target.toArray(new String[target.size()])).toClassicRepresentation();
    }     

    public String buildObjectId(
        String component
    ) throws ServiceException{
        return isConvertible(component) ? buildObjectId(new Path(component)) : component;
    }
    
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
            source.getSegment(0).toClassicRepresentation()
        );
        Path type = dbObjectConfiguration.getType();
        for(
            int i = 0, pos = 1, iLimit = type.size() - (reference ? 1 : 0); 
            i < iLimit; 
            i++
        ) {
            if(":*".equals(type.getSegment(i).toClassicRepresentation())) {
                if(pos >= source.size()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "Reference not valid for type",
                        new BasicException.Parameter("objectId",id),
                        new BasicException.Parameter("type", type)
                    );                            
                }
                String component = source.getSegment(pos++).toClassicRepresentation();
                target.add(
                    isConvertible(component) ? buildResourceIdentifier(component, reference).toString() : component
                );
            } else {
                target.add(type.getSegment(i).toClassicRepresentation());
            }
        }
        return new Path(target.toArray(new String[target.size()]));
    }
    
    public boolean normalizeObjectIds(
    ) {
        return this.database.isNormalizeObjectIds();
    }

    private boolean isConvertible(
        String segment
    ){
        return 
            segment != null &&
            segment.indexOf('/') > 0 &&
            !segment.startsWith("("); 
    }

    public List<DbObjectConfiguration> getDbObjectConfigurations(
        Path path
    ) throws ServiceException {       
        Path objectPattern = path.size() % 2 == 0 ? path.getChild(":*") : path;
        List<DbObjectConfiguration> replies = new ArrayList<DbObjectConfiguration>();
        for(DbObjectConfiguration dbObjectConfiguration: this.dbObjectConfigurations.values()) {
            Path typePattern = dbObjectConfiguration.getType();
            if(objectPattern.isLike(typePattern)) {
                Boolean exclude = null;
                for(Path excludeType: this.database.getExcludeType()) {
                    if(objectPattern.isLike(excludeType)) {
                        exclude = true;
                    }
                }
                Boolean include = null;
                for(Path includeType: this.database.getIncludeType()) {
                    if(objectPattern.isLike(includeType)) {
                        include = true;
                    }
                }
                if(!Boolean.TRUE.equals(exclude) || Boolean.TRUE.equals(include)) {
                    replies.add(dbObjectConfiguration);
                }
            }
        }
        if(replies.isEmpty()) {
            Path objectSuffix = new Path(objectPattern.getSuffix(1));
            for(DbObjectConfiguration dbObjectConfiguration: this.dbObjectConfigurations.values()) {
                Path typeSuffix = new Path(dbObjectConfiguration.getType().getSuffix(1));
                if(typeSuffix.isLike(objectSuffix)) {
                    replies.add(dbObjectConfiguration);
                }
            }
        }
        return replies;
    }
    
    /**
     * Get a single DB object configuration
     * 
     * @param path
     * 
     * @return the DB object configuration for the given path
     * 
     * @throws ServiceException if the cardinality was not 1
     */
    public DbObjectConfiguration getDbObjectConfiguration(
        Path path
    ) throws ServiceException {
        List<DbObjectConfiguration> replies = getDbObjectConfigurations(path);
        switch(replies.size()) {
            case 0:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CARDINALITY, 
                    "No type configuration found for the given path",
                    new BasicException.Parameter(BasicException.Parameter.XRI, path),            
                    new BasicException.Parameter("mismatching", this.dbObjectConfigurations)
                );
            case 1:
                return replies.get(0);
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CARDINALITY, 
                    "There is more than one type for the given path (maybe by ignoring the authority)",
                    new BasicException.Parameter(BasicException.Parameter.XRI, path),            
                    new BasicException.Parameter("matching", replies)
                ); 
        } 
    }
    
    public DbObjectConfiguration getDbObjectConfiguration(
        String typeName
    ) throws ServiceException {
        DbObjectConfiguration dbObjectConfiguration = this.dbObjectConfigurations.get(typeName);
        if(dbObjectConfiguration == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "no type configuration found for type name",
                new BasicException.Parameter("type name", typeName)
            );
        } else {
            return dbObjectConfiguration;
        }
    }

    public Collection<DbObjectConfiguration> getDbObjectConfigurations(
    ) {
        return this.dbObjectConfigurations.values();
    }

    public Map<String,String> getFromToColumnNameMapping(
    ) {
        return this.fromToColumnNameMapping;
    }

    public Map<String,String> getToFromColumnNameMapping(
    ) {
        return this.toFromColumnNameMapping;
    }
    
    public boolean useViewsForRedundantColumns(
    ) {
    	return this.database.isUseViewsForRedundantColumns();
    }

    public boolean cascadeDeletes(
    ) {
        return this.database.isCascadeDeletes();
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private final Database_1_0 database;
    // Maps short column names to long column names.
    private final Map<String,String> fromToColumnNameMapping = new HashMap<String, String>();
    private final Map<String,String> toFromColumnNameMapping = new HashMap<String, String>();

    // Map containing entries instanceof TypeConfigurationEntry.
    // Key is the type name
    private final Map<String,DbObjectConfiguration> dbObjectConfigurations = new HashMap<String,DbObjectConfiguration>();

}
