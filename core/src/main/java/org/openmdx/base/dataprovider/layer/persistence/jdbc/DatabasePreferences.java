/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database Preferences 
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MappedRecord;
#endif

import org.openmdx.base.dataprovider.layer.persistence.jdbc.dbobject.DbObjectConfiguration;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.StringMacro;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.spi.Numbers;
import org.openmdx.base.rest.spi.Object_2Facade;

/**
 * Database Preferences
 */
public class DatabasePreferences {

	private static final Path PREFERENCES = new Path(
			"xri://@openmdx*org:openmdx:preferences2/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences");

	private static final Path CONFIGURATION_PATTERN = PREFERENCES.getChild("%");

	private static final Path NODES_PATTERN = PREFERENCES.getDescendant(":*", "node");

	private static final Path ENTRIES_PATTERN = NODES_PATTERN.getDescendant(":*", "entry");

	@SuppressWarnings("unchecked")
	private static ObjectRecord createRootNode(Path nodes) throws ResourceException {
		Object_2Facade node = Object_2Facade.newInstance(nodes.getChild("+"), "org:openmdx:preferences2:Node");
		MappedRecord values = node.getValue();
		values.put("name", "");
		values.put("absolutePath", "/");
		return node.getDelegate();
	}

	@SuppressWarnings("unchecked")
	private static ObjectRecord createTopNode(ObjectRecord parent, String name) throws ResourceException {
		final Object_2Facade node = Object_2Facade.newInstance(
				parent.getResourceIdentifier().getParent().getChild(name), "org:openmdx:preferences2:Node");
		final MappedRecord values = node.getValue();
		values.put("name", name);
		values.put("absolutePath", "/" + name);
		values.put("parent", parent.getResourceIdentifier());
		return node.getDelegate();
	}

	@SuppressWarnings("unchecked")
	private static ObjectRecord createNode(ObjectRecord parent, String type, String name) throws ResourceException {
		final Object_2Facade node = Object_2Facade.newInstance(
				parent.getResourceIdentifier().getParent().getChild(type + "*" + name),
				"org:openmdx:preferences2:Node");
		final MappedRecord values = node.getValue();
		values.put("name", name);
		values.put("absolutePath", "/" + type + "/" + name);
		values.put("parent", parent.getResourceIdentifier());
		return node.getDelegate();
	}

	@SuppressWarnings("unchecked")
	private static ObjectRecord createNode(ObjectRecord parent, String type, String name, String subType) throws ResourceException {
		final Object_2Facade node = Object_2Facade.newInstance(
				parent.getResourceIdentifier().getParent().getChild(type + "*" + name + "*" + subType),
				"org:openmdx:preferences2:Node");
		final MappedRecord values = node.getValue();
		values.put("name", subType);
		values.put("absolutePath", "/" + type + "/" + name + "/" + subType);
		values.put("parent", parent.getResourceIdentifier());
		return node.getDelegate();
	}
	
	@SuppressWarnings("unchecked")
	private static ObjectRecord createNode(ObjectRecord parent, String type, String name, String subType, String index) throws ResourceException {
		final Object_2Facade node = Object_2Facade.newInstance(
				parent.getResourceIdentifier().getParent().getChild(type + "*" + name + "*" + subType + "*" + index),
				"org:openmdx:preferences2:Node");
		final MappedRecord values = node.getValue();
		values.put("name", index);
		values.put("absolutePath", "/" + type + "/" + name + "/" + subType + "/" + index);
		values.put("parent", parent.getResourceIdentifier());
		return node.getDelegate();
	}

	
	@SuppressWarnings("unchecked")
	private static ObjectRecord createEntry(Path entries, String name, String value) throws ResourceException {
		final Object_2Facade entry = Object_2Facade.newInstance(entries.getChild(name),
				"org:openmdx:preferences2:Entry");
		final MappedRecord values = entry.getValue();
		values.put("name", name);
		values.put("value", value);
		return entry.getDelegate();
	}
	
	/**
	 * Tells whether the XRI refers to a configuration request
	 * 
	 * @param xri
	 * 
	 * @return {@code true} if the XRI refers to a configuration request
	 */
	public static boolean isConfigurationRequest(Path xri) {
		return xri.isLike(CONFIGURATION_PATTERN);
	}

	public static void retrieveDatabaseConfiguration(
	    QueryRecord request,
		Target target,
		Collection<DbObjectConfiguration> configurations, 
		Map<String, String> columnMapping,
		Map<String, List<StringMacro>> valueMapping
	) throws ResourceException, ServiceException {
	    final List<ObjectRecord> objects = retrieveDatabaseConfiguration(
		    request.getResourceIdentifier(), 
		    configurations, 
		    columnMapping, 
		    valueMapping
        );
	    Collections.sort(objects, new ObjectRecordComparator());
	    int position = Numbers.getValue(request.getPosition(), 0);
	    for(int i = position; !target.isSaturated() && i < objects.size(); i++) {
	        target.accept(objects.get(i));
	    }
	    target.close(objects.size());
	}

    private static List<ObjectRecord> retrieveDatabaseConfiguration(
        Path xri, 
        Collection<DbObjectConfiguration> configurations, 
        Map<String, String> columnMapping,
        Map<String, List<StringMacro>> valueMapping
    )
        throws ResourceException {
        final List<ObjectRecord> objects = new ArrayList<ObjectRecord>();        
        if (xri.isLike(NODES_PATTERN)) {
        	ObjectRecord rootNode = createRootNode(xri);
        	objects.add(rootNode);
        	{
        		final String type = "dbObject";
        		ObjectRecord topNode = createTopNode(rootNode, type);
        		objects.add(topNode);
        		for (DbObjectConfiguration configuration : configurations) {
        			final ObjectRecord configurationNode = createNode(topNode, type, configuration.getTypeName());
                    objects.add(configurationNode);
        		}
        	}
        	{
        		final String type = "dbColumn";
        		ObjectRecord topNode = createTopNode(rootNode, type);
                objects.add(topNode);
        		final Set<String> columnNames = new HashSet<String>();
        		columnNames.addAll(columnMapping.keySet());
        		columnNames.addAll(valueMapping.keySet());
        		for (String columnName : columnNames) {
        			final ObjectRecord columnNode = createNode(topNode, type, columnName);
                    objects.add(columnNode);
        			final List<StringMacro> macros = valueMapping.get(columnName);
        			if(macros != null) {
        				final ObjectRecord macrosNode = createNode(columnNode, type, columnName, "stringMacro");
        				for(int i = 0; i < macros.size(); i++) {
        					final ObjectRecord macroNode = createNode(macrosNode, type, columnName, "stringMacro", String.valueOf(i));
                            objects.add(macroNode);
        				}
        			}
        		}
        	}
        } else if (xri.isLike(ENTRIES_PATTERN)) {
        	final String nodeName = xri.getSegment(8).toClassicRepresentation();
        	if (nodeName.startsWith("dbObject*")) {
        		final String typeName = nodeName.substring(9);
        		Configurations: for (DbObjectConfiguration configuration : configurations) {
        			if (configuration.getTypeName().equals(typeName)) {
        				objects.add(createEntry(xri, "dbObjectType", configuration.getType().toXRI()));
        				objects.add(createEntry(xri, "dbObjectForUpdate1", configuration.getDbObjectForUpdate1()));
        				objects.add(createEntry(xri, "dbObjectForUpdate2", configuration.getDbObjectForUpdate2()));
        				objects.add(createEntry(xri, "dbObjectForQuery1", configuration.getDbObjectForQuery1()));
        				objects.add(createEntry(xri, "dbObjectForQuery2", configuration.getDbObjectForQuery2()));
        				break Configurations;
        			}
        		}
        	} else if (nodeName.startsWith("dbColumn*")) {
        		final int end = nodeName.indexOf("*stringMacro*", 10);
        		if(end < 0) {
        			final String columnName = nodeName.substring(9);
        			objects.add(createEntry(xri, "canonicalColumnName", columnName));
        			final String mappedColumName = columnMapping.get(columnName);
                    if(mappedColumName != null) {
        				objects.add(createEntry(xri, "mappedColumnName", mappedColumName));
                    }
        		} else {
        			final String columnName = nodeName.substring(9, end);
                    final List<StringMacro> macros = valueMapping.get(columnName);
                    if(macros != null) {
                    	int macroIndex = Integer.parseInt(nodeName.substring(end + 13));
                    	final StringMacro mapping = macros.get(macroIndex);
        				objects.add(createEntry(xri, "macroName", mapping.getName()));
        				objects.add(createEntry(xri, "macroValue", mapping.getValue()));
                    }
        		}
        	}
        }
        return objects;
    }

}
