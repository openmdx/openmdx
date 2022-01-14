/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Meta Data Provider
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

package org.openmdx.application.mof.mapping.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.ClassPersistenceModifier;
import org.openmdx.application.mof.mapping.java.metadata.ColumnMetaData;
import org.openmdx.application.mof.mapping.java.metadata.ExtendableMetaData;
import org.openmdx.application.mof.mapping.java.metadata.ExtensionTarget;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldPersistenceModifier;
import org.openmdx.application.mof.mapping.java.metadata.InheritanceMetaData;
import org.openmdx.application.mof.mapping.java.metadata.InheritanceStrategy;
import org.openmdx.application.mof.mapping.java.metadata.JoinMetaData;
import org.openmdx.application.mof.mapping.java.metadata.MetaData_2_0;
import org.openmdx.application.mof.mapping.java.metadata.PackageMetaData;
import org.openmdx.application.mof.mapping.java.metadata.Visibility;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.xml.EntityMapper;
import org.openmdx.kernel.xri.XRI_2Protocols;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Meta Data Provider
 */
public class MetaData_2 implements MetaData_1_0, MetaData_2_0 {

    /**
     * Constructor 
     *
     * @param base the openmdx files' base directory
     */
    public MetaData_2(
        String base
    ){
        File baseDirectory = base == null ? null : new File(base);
        if(base == null) {
            this.baseDirectory = null;
            SysLog.log(Level.FINE, "No base directory specified for .openmdxjdo files");
        } else if (baseDirectory.exists()) {
            this.baseDirectory = baseDirectory;
            SysLog.log(Level.FINE, "The base directory specified for .openmdxjdo files is {0}", this.baseDirectory);
        } else {
            this.baseDirectory = null;
            SysLog.log(Level.WARNING, "The base directory {0} for .openmdxjdo files does not exist", base);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements MetaData_2_0
    //------------------------------------------------------------------------

    /**
     * Retrieve package meta data
     * 
     * @param qualifiedPackageName
     * 
     * @return the package's meta data
     */
    public PackageMetaData getPackage(
        String qualifiedPackageName
    ){
        PackageMetaData packageMetaData = this.packages.get(qualifiedPackageName);
        if(packageMetaData == null) {
            this.packages.put(
                qualifiedPackageName, 
                packageMetaData = new Package(qualifiedPackageName)
            );
        }
        return packageMetaData;
    }
    
    //------------------------------------------------------------------------
    // Implements MetaData_1_0
    //------------------------------------------------------------------------

    /**
     * Retrieve class meta data
     * 
     * @param qualifiedClassName
     * 
     * @return the class' meta data
     */
    public ClassMetaData getClassMetaData(
        String qualifiedClassName
    ){
        String[] words = qualifiedClassName.split(":");
        String className = Identifier.CLASS_PROXY_NAME.toIdentifier(words[words.length - 1]);
        words[words.length - 1] = "jpa3";
        StringBuilder packageName = new StringBuilder(words[0]);
        for(
            int i = 1;
            i < words.length;
            i++
        ) packageName.append(
            '.'
        ).append(
            Identifier.PACKAGE_NAME.toIdentifier(words[i])
        );
        return getPackage(packageName.toString()).getClassMetaData(className);
    }
    
    //------------------------------------------------------------------------
    // Class MetaData_2
    //------------------------------------------------------------------------

    /**
     * Retieve an element's child
     * 
     * @param parent
     * @param tagName
     * @param expectedName
     * 
     * @return the requested child, or <code>null</code>
     */
    Element getChild(
        Element parent,
        String tagName, 
        String expectedName
    ){
        Element child = (Element) parent.getElementsByTagName(tagName).item(0);
        if(child == null) {
            SysLog.log(Level.FINER,"{0} contains no {1} element", parent.getTagName(), tagName);
            return null;
        } else if (expectedName == null){ 
            SysLog.log(Level.FINER,"Processing {0}", tagName);
            return child;
        } else {
            String elementName = child.getAttribute("name");
            if(expectedName.equals(elementName)) {
                SysLog.log(Level.FINER,"Processing {0}", tagName, elementName);
                return child;
            } else {
                SysLog.log(
                    Level.WARNING,
                    "Could not process {0} {1}, expected {2} {3}", 
                    tagName, elementName, tagName, expectedName
                );
                return null;
            }
        }
    }
    
    /**
     * Retrieve an element's attribute
     * 
     * @param element
     * @param attributeName
     * 
     * @return the requested attribute, or <code>null</code>
     */
    String getAttribute(
        Element element,
        String attributeName
    ){
        String value = element.getAttribute(attributeName);
        if("".equals(value)) {
            value = null;
        }
        if(value == null) {
            SysLog.log(Level.FINER, "{0}'s {1} attribute is not set", element.getTagName(), attributeName);
        } else {
            SysLog.log(
                Level.FINER, 
                "{0}'s {1} attribute is set to {2}", 
                element.getTagName(), attributeName, value
            );
        }
        return value;
    }
    
    /**
     * Extendable
     */
    class Extendable implements ExtendableMetaData {

        /**
         * Constructor 
         */
        protected Extendable(
        ) {
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.mof.mapping.java.metadata.Extendable#getExtension(java.lang.String)
         */
        public Map<String,Map<String, String>> getExtension(
            ExtensionTarget target
        ) {
            return extensions.get(target);
        }

        /**
         * Read the element's extensions
         * 
         * @param element
         */
        protected void getExtensions(
            Element element
        ) {
            processExtensions(
                element,
                false // ignore
            );
        } 
        
        /**
         * Ignore the element's extensions
         * 
         * @param element
         */
        protected void ignoreExtensions(
            Element element
        ) {
            processExtensions(
                element,
                true // ignore
            );
        } 
        
        /**
         * 
         * @param element
         * @param ignore
         */
        private void processExtensions(
            Element element,
            boolean ignore
        ) {
            NodeList children = element.getElementsByTagName("extension");
            for(
                 int i = 0, iLimit = children.getLength();
                 i < iLimit;
                 i++
            ){
                Element child = (Element) children.item(i);
                ExtensionTarget target = ExtensionTarget.fromXMLFormat(
                    child.getAttribute("target")
                );
                String vendor = child.getAttribute("vendor-name");
                String key = child.getAttribute("key");
                String value = child.getAttribute("key");
                if(ignore) {
                    SysLog.log(
                        Level.FINER, 
                        "{0} accepts extension only in package.openmdxjdo, " +
                        "that's why the {1} extension {2} for target {3} is not set to \"{4}\" but ignored",
                        element.getTagName(), vendor, key, target.toXMLFormat(), value
                    );
                } else {
                    Map<String,Map<String,String>> targetSpecificExtensions = this.extensions.get(target);
                    if(targetSpecificExtensions == null) {
                        this.extensions.put(
                            target, 
                            targetSpecificExtensions = new HashMap<String,Map<String,String>>()
                        );
                    }
                    Map<String, String> vendorSpecificExtensions = targetSpecificExtensions.get(vendor);
                    if(vendorSpecificExtensions == null) {
                        targetSpecificExtensions.put(
                            vendor,
                            vendorSpecificExtensions = new HashMap<String,String>()
                        );
                    }
                    vendorSpecificExtensions.put(key, value);
                    SysLog.log(
                        Level.FINER, 
                        "{0} has its {1} extension {2} set to \"{4}\" for target {4}",
                        element.getTagName(), vendor, key, value, target.toXMLFormat()
                    );
                }
            }
        }

        /**
         * 
         */
        private final Map<ExtensionTarget,Map<String,Map<String,String>>> extensions = new HashMap<ExtensionTarget,Map<String,Map<String,String>>>();  
        
    }
    
    /**
     * Package 
     */
    class Package extends Extendable implements PackageMetaData {

        /**
         * Constructor 
         *
         * @param name
         */
        Package(
            String name
        ){
            this.name = name;
            InputStream documentSource = null;
            String documentLocation = null;
            Document document = null;
            if(baseDirectory == null) {
                this.directory = null;
            } else {
                File directory = new File(baseDirectory, name.replace('.', File.separatorChar));
                this.directory = directory.exists() ? directory : null;
                try {
                    if(this.directory == null) {
                        SysLog.log(Level.FINE,"There is no .openmdxjdo directory {0} for package {1}", directory, name);
                    } else {
                        SysLog.log(Level.FINE,"The .openmdxjdo directory for package {0} is {1}", name, directory);
                        File file = new File(directory, "package.openmdxjdo");
                        if(file.exists()) {
                            SysLog.log(
                                Level.FINE,
                                "The .openmdxjdo file for package {0} is {1}", 
                                name, file
                            );
                            documentSource = new FileInputStream(file);
                            documentLocation = file.getAbsolutePath();
                        }
                    }
                    if(documentSource == null) {
                        String documentPath = name.replace('.', '/') + "/package.openmdxjdo"; 
                        URL documentURL = Resources.getResource(documentPath);
                        if(documentURL != null) try {
                            documentSource = documentURL.openStream();
                        } catch (IOException ignrore) {
                            // leave documentSource null
                        }
                        if(documentSource != null) {
                            documentLocation = XRI_2Protocols.RESOURCE_PREFIX + documentPath;
                            SysLog.log(
                                Level.FINE,
                                "Found .openmdxjdo resource for package {0}", 
                                name
                            );
                        }
                    }
                    if(documentSource != null) {
                        System.out.println("INFO:    Loading meta data for " + name);
                        document = newDocumentBuilder().parse(documentSource);
                        documentSource.close();
                    }
                } catch (IOException exception) {
                    SysLog.log(Level.WARNING,"Could not parse " + documentLocation, exception);
                } catch (SAXException exception) {
                    SysLog.log(Level.WARNING,"Could not parse " + documentLocation, exception);
                }
            }
            if(document == null) {
                this.tablePrefix = null;
            } else {
                //
                // openmdxjdo
                //
                Element openmdxjdoElement = document.getDocumentElement();
                if("openmdxjdo".equals(openmdxjdoElement.getTagName())) {
                    SysLog.log(
                        Level.FINER,
                        "Processing 'openmdxjdo' document from {}", 
                        documentSource
                    );
                } else {
                    SysLog.log(
                        Level.WARNING,
                        ".openmdxjdo document {0} has type \"{1}\" instead of \"openmdxjdo\"", 
                        documentLocation, openmdxjdoElement.getTagName()
                    );
                    this.tablePrefix = null;
                    return;
                }
                //
                // package
                //
                Element packageElement = getChild(openmdxjdoElement, "package", Package.this.name);
                super.getExtensions(packageElement);
                this.tablePrefix = getAttribute(packageElement, "table-prefix");
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.application.mof.mapping.java.metadata.PackageMetaData#getClassMetaData(java.lang.String)
         */
        public ClassMetaData getClassMetaData(String name) {
            ClassMetaData classMetaData = this.classes.get(name);
            if(classMetaData == null) {
                this.classes.put(
                    name, 
                    classMetaData = new Class(name)
                );
            }
            return classMetaData;
        }

        /* (non-Javadoc)
         * @see org.openmdx.model1.importer.metadata.PackageMetaData#getName()
         */
        public String getName() {
            return this.name;
        }

        /* (non-Javadoc)
         * @see org.openmdx.model1.importer.metadata.PackageMetaData#getTablePrefix()
         */
        public String getTablePrefix(
        ) {
            return this.tablePrefix;
        }

        /**
         * 
         */
        private final Map<String, ClassMetaData> classes = new HashMap<String, ClassMetaData>();

        /**
         * The package directory
         */
        final File directory;

        /**
         * The package name
         */
        final String name;
        
        /**
         * The package's table prefix
         */
        private final String tablePrefix;
        
        /**
         * Class Modifier
         */
        class Class extends Extendable implements ClassMetaData {
    
            /**
             * Constructor 
             * 
             * @param name
             */
            Class(
                String name
            ){
                this.name = name;
                InputStream documentSource = null;
                String documentLocation = null;
                try {
                    if(directory != null) {
                        String documentName = name + ".openmdxjdo";
                        File file = new File(directory, documentName);
                        if(file.exists()) {
                            SysLog.log(
                                Level.FINE,
                                "The .openmdxjdo file for class {0}.{1} is {2}", 
                                Package.this.name, name, file
                            );
                            System.out.println("INFO:    Loading meta data for " + Package.this.name + "." + name);
                            documentSource = new FileInputStream(file);
                            documentLocation = file.getAbsolutePath();
                        }
                    }
                    if(documentSource == null) {
                        String documentPath = Package.this.name.replace('.', '/') + '/' + name + ".openmdxjdo"; 
                        URL documentURL = Resources.getResource(documentPath);
                        if(documentURL != null) try {
                            documentSource = documentURL.openStream();
                        } catch (IOException ignrore) {
                            // leave documentSource null
                        }
                        if(documentSource != null) {
                            System.out.println("INFO:    Loading meta data for " + Package.this.name + "." + name);
                            documentLocation = XRI_2Protocols.RESOURCE_PREFIX + documentPath;
                            SysLog.log(
                                Level.FINE,
                                "Found .openmdxjdo resource for class {0}.{1}", 
                                Package.this.name, name
                            );
                        }
                    }
                    if(documentSource != null) {    
                        Document document = newDocumentBuilder().parse(documentSource);
                        Element openmdxjdoElement = document.getDocumentElement();
                        if("openmdxjdo".equals(openmdxjdoElement.getTagName())) {
                            SysLog.log(
                                Level.FINER,
                                "Processing 'openmdxjdo' document from {0}", 
                                documentLocation
                            );
                        } else {
                            SysLog.log(
                                Level.WARNING,
                                ".openmdxjdo document {0} has type \"{1}\" instead of \"openmdxjdo\"", 
                                documentLocation, openmdxjdoElement.getTagName()
                            );
                            return;
                        }
                        //
                        // package
                        //
                        Element packageElement = getChild(openmdxjdoElement, "package", Package.this.name);
                        super.ignoreExtensions(packageElement);
                        //
                        // class
                        //
                        Element classElement = getChild(packageElement, "class", Class.this.name);
                        this.persistenceModifier = ClassPersistenceModifier.fromXMLFormat(
                            getAttribute(classElement, "persistence-modifier")
                        );
                        this.requiresExtent = Boolean.parseBoolean(
                            getAttribute(classElement, "requires-extent")
                        );
                        this.requiresSlices = Boolean.parseBoolean(
                            getAttribute(classElement, "requires-slices")
                        );
                        this.table = getAttribute(classElement, "table");                        
                        this.baseClass = getAttribute(classElement, "base-class");
                        super.getExtensions(classElement);
                        Element inheritanceElement = getChild(classElement, "inheritance", null);
                        if(inheritanceElement != null) {
                            this.inheritance = new Inheritance(inheritanceElement);
                        }
                        NodeList fields = classElement.getElementsByTagName("field");
                        for(
                            int i = 0, iLimit = fields.getLength();
                            i < iLimit;
                            i++
                        ){
                            Field field = new Field((Element) fields.item(i));
                            this.fields.put(field.getName(), field);
                        }
                    } 
                    else {
                        SysLog.log(
                            Level.FINE,
                            "An openmdxjdo document for class {0}.{1} exists neither as file nor as resource", 
                            Package.this.name, name
                        );
                    }
                } 
                catch (SAXException exception) {
                    SysLog.log(Level.WARNING,"Could not parse " + documentLocation, exception);
                } 
                catch (IOException exception) {
                    SysLog.log(Level.WARNING,"Could not parse " + documentLocation, exception);
                }
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.application.mof.mapping.java.metadata.ClassMetaData#getFieldMetaData(java.lang.String)
             */
            public FieldMetaData getFieldMetaData(String name) {
                return this.fields.get(name);
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.model1.importer.metadata.ClassMetaData#getFieldMetaData(org.openmdx.model1.importer.metadata.Visibility)
             */
            public Collection<FieldMetaData> getFieldMetaData(Visibility visibility) {
                Collection<FieldMetaData> fields = new ArrayList<FieldMetaData>();
                for(FieldMetaData field : this.fields.values()) {
                    if(visibility == field.getVisibility()) {
                        fields.add(field);
                    }
                }
                return fields.isEmpty() ? null : fields;
            }

            /* (non-Javadoc)
             * @see org.openmdx.application.mof.mapping.java.metadata.ClassMetaData#isRequiresExtent()
             */
            public boolean isRequiresExtent() {
                return this.requiresExtent;
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.application.mof.mapping.java.metadata.ClassMetaData#isRequiresSlices()
             */
            public boolean isRequiresSlices() {
                return this.requiresSlices;
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.application.mof.mapping.java.metadata.ClassMetaData#getInheritance()
             */
            public InheritanceMetaData getInheritance() {
                return this.inheritance;
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.application.mof.mapping.java.metadata.ClassMetaData#getPersistenceModifier()
             */
            public ClassPersistenceModifier getPersistenceModifier() {
                return this.persistenceModifier;
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.model1.importer.metadata.ClassMetaData#getTable()
             */
            public String getTable() {
                return this.table;
            }
    
            /* (non-Javadoc)
             * @see org.openmdx.model1.importer.metadata.ClassMetaData#getName()
             */
            public String getName() {
                return this.name;
            }
    
            public String getBaseClass() {
                return this.baseClass;
            }
            
            //---------------------------------------------------------------
            // Members
            //---------------------------------------------------------------
            private boolean requiresExtent = true;            
            private boolean requiresSlices = true;            
            private InheritanceMetaData inheritance;    
            private ClassPersistenceModifier persistenceModifier = ClassPersistenceModifier.PERSISTENCE_CAPABLE;            
            private final Map<String,FieldMetaData> fields = new HashMap<String,FieldMetaData>();            
            private String table;
            private String baseClass;
            final String name;
                
            /**
             * Join
             */
            class Join extends Extendable implements JoinMetaData {

                /**
                 * Constructor 
                 *
                 * @param joinElement
                 */
                Join(
                    Element joinElement
                ) {
                    this.table = joinElement.getAttribute("table");
                    super.getExtensions(joinElement);
                }
                
                /* (non-Javadoc)
                 * @see org.openmdx.model1.importer.metadata.JoinMetaData#getTable()
                 */
                public String getTable() {
                    return this.table;
                }
                
                /**
                 * The table name
                 */
                private final String table;
              
            }

            /**
             * Field 
             */
            class Field extends Extendable implements FieldMetaData {
                
                /**
                 * Constructor 
                 *
                 * @param fieldElement
                 */
                Field(
                    Element fieldElement
                ) {
                    this.name = fieldElement.getAttribute("name");
                    SysLog.log(Level.FINER,"Processing field {0}", this.name);
                    this.persistenceModifier = FieldPersistenceModifier.fromXMLFormat(
                        getAttribute(fieldElement, "persistence-modifier")
                    );
                    this.visibility = Visibility.fromXMLFormat(
                        getAttribute(fieldElement, "visibility")
                    );
                    this.fieldType = getAttribute(fieldElement, "field-type");
                    String embedded = getAttribute(fieldElement, "embedded");
                    this.embedded = embedded == null ? null : Integer.valueOf(embedded);
                    super.getExtensions(fieldElement);
                    Element columnElement = getChild(fieldElement, "column", null);
                    this.column = columnElement == null ? null : new Column(columnElement);
                    if(this.column != null) {
                        System.out.println("INFO:    Column meta data found for field " + Class.this.name + "." + this.name);                        
                    }
                    Element joinElement = getChild(fieldElement, "join", null);
                    this.join = joinElement == null ? null : new Join(joinElement);
                    if(this.join != null) {
                        System.out.println("INFO:    Join meta data found for field " + Class.this.name + "." + this.name);                        
                    }
                }
        
                /* (non-Javadoc)
                 * @see org.openmdx.application.mof.mapping.java.metadata.FieldMetaData#getPersistenceModifier()
                 */
                public FieldPersistenceModifier getPersistenceModifier() {
                    return this.persistenceModifier;
                }
                
                /* (non-Javadoc)
                 * @see org.openmdx.application.mof.mapping.java.metadata.FieldMetaData#getVisibility()
                 */
                public Visibility getVisibility() {
                    return this.visibility;
                }
        
                /* (non-Javadoc)
                 * @see org.openmdx.application.mof.mapping.java.metadata.FieldMetaData#getFieldType()
                 */
                public String getFieldType() {
                    return this.fieldType;
                }
                
                /* (non-Javadoc)
                 * @see org.openmdx.model1.importer.metadata.FieldMetaData#getColumn()
                 */
                public ColumnMetaData getColumn() {
                    return this.column;
                }
        
                /* (non-Javadoc)
                 * @see org.openmdx.model1.importer.metadata.FieldMetaData#getName()
                 */
                public String getName() {
                    return this.name;
                }
        
                /* (non-Javadoc)
                 * @see org.openmdx.model1.importer.metadata.FieldMetaData#getEmbedded()
                 */
                public Integer getEmbedded() {
                    return this.embedded;
                }
                
                /* (non-Javadoc)
                 * @see org.openmdx.model1.importer.metadata.FieldMetaData#getJoin()
                 */
                public JoinMetaData getJoin() {
                    return this.join;
                }



                private final FieldPersistenceModifier persistenceModifier;
                
                private final Visibility visibility;
                
                private final String fieldType;        
                
                private final ColumnMetaData column;
                
                final String name;
        
                private final Integer embedded;

                private final JoinMetaData join;                
                
                class Column extends Extendable implements ColumnMetaData {
                    
                    Column(
                        Element columnElement
                    ) {
                        this.name = columnElement.getAttribute("name");
                        if(this.name == null) {
                            SysLog.log(Level.FINER,"Processing 1st column for field {0}", Field.this.name);
                        } else {
                            SysLog.log(Level.FINER,"Processing column {0} for field {1}", this.name, Field.this.name);
                        }
                        String length = getAttribute(columnElement, "length");
                        this.length = length == null ? null : Integer.valueOf(length);
                        String scale = getAttribute(columnElement, "scale");
                        this.scale = scale == null ? null : Integer.valueOf(scale);
                        this.jdbcType = getAttribute(columnElement, "jdbc-type");
                        super.getExtensions(columnElement);
                    }

                    /* (non-Javadoc)
                     * @see org.openmdx.model1.importer.metadata.ColumnMetaData#getName()
                     */
                    public String getName() {
                        return this.name;
                    }
            
                    /* (non-Javadoc)
                     * @see org.openmdx.model1.importer.metadata.ColumnMetaData#getLength()
                     */
                    public Integer getLength() {
                        return this.length;
                    }
            
                    /* (non-Javadoc)
                     * @see org.openmdx.model1.importer.metadata.ColumnMetaData#getScale()
                     */
                    public Integer getScale() {
                        return this.scale;
                    }
            
                    /* (non-Javadoc)
                     * @see org.openmdx.model1.importer.metadata.ColumnMetaData#getJdbcType()
                     */
                    public String getJdbcType() {
                        return this.jdbcType;
                    }
            
                    private final String name;
                    
                    private final Integer length;
                    
                    private final Integer scale;
                    
                    private final String jdbcType;
                    
                }

            }

            /**
             * Inheritance 
             */
            class Inheritance extends Extendable implements InheritanceMetaData {
                
                /**
                 * Constructor 
                 *
                 * @param inheritanceElement
                 */
                Inheritance(Element inheritanceElement){
                    SysLog.log(Level.FINER,"Processing inheritance");
                    this.strategy = InheritanceStrategy.fromXMLFormat(
                        getAttribute(inheritanceElement, "strategy")
                    );
                    super.getExtensions(inheritanceElement);
                }
        
                /* (non-Javadoc)
                 * @see org.openmdx.application.mof.mapping.java.metadata.Inheritance#getStrategy()
                 */
                public InheritanceStrategy getStrategy() {
                    return this.strategy;
                }
        
                /**
                 * 
                 */
                private final InheritanceStrategy strategy;
                
            }
        

        }

    }

    class ExceptionLogger implements ErrorHandler {

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(
            SAXParseException exception
        ) throws SAXException {
            SysLog.log(Level.SEVERE,"Could not read .openmdxjdo file", exception);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(
            SAXParseException exception
        ) throws SAXException {
            error(exception);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(
            SAXParseException exception
        ) throws SAXException {
            SysLog.log(Level.WARNING,"Could not read .openmdxjdo file", exception);
        }
        
    }

    /**
     * Create a new Document builder
     * 
     * @return
     */
    DocumentBuilder newDocumentBuilder(
    ){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(VALIDATE);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(EntityMapper.getInstance());
            builder.setErrorHandler(this.errorHandler);
            return builder;
        } catch (ParserConfigurationException exception) {
            SysLog.log(Level.SEVERE,"Document builder acquisition failed", exception);
            return null;
        }
    }
    
    /**
     * The base file
     */
    final File baseDirectory;

    /**
     * The package cache
     */
    private final Map<String, PackageMetaData> packages = new HashMap<String, PackageMetaData>();
    
    /**
     * The SAX error handler
     */
    private final ErrorHandler errorHandler = new ExceptionLogger();

    static {
        EntityMapper.registerPublicId(
          "-//openMDX//DTD Java Data Objects Metadata Extension 2.0//EN",
          XRI_2Protocols.RESOURCE_PREFIX + "org/openmdx/model1/importer/jdo2/openmdx-jdo_2_0.dtd"
        );
    }

    /**
     * Defines whether DTD validation is active
     */
    private final boolean VALIDATE = true;
        
}
