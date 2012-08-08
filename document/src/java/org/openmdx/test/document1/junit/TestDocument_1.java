/*
 * ====================================================================
 * Name:        $Id: TestDocument_1.java,v 1.13 2005/10/10 15:26:59 hburger Exp $
 * Description: Class Loading Test
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/10 15:26:59 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.test.document1.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jmi.reflect.RefPackage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.cci.Provider;
import org.openmdx.base.cci.basePackage;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.document1.cci.BinaryContent;
import org.openmdx.document1.cci.Cabinet;
import org.openmdx.document1.cci.DocumentObject;
import org.openmdx.document1.cci.Folder;
import org.openmdx.document1.cci.FolderFilter;
import org.openmdx.document1.cci.Node;
import org.openmdx.document1.cci.Resource;
import org.openmdx.document1.cci.Revision;
import org.openmdx.document1.cci.Segment;
import org.openmdx.document1.cci.document1Package;

/**
 * openMDX/Document Test
 */
public class TestDocument_1 extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public TestDocument_1(
		String name
	){
		super(name);
	}  
  
	/**
	 * 
	 * @param args
	 */
	public static void main(
		String[] args
	){
		TestRunner.run(suite());
	}

	/**
	 * 
	 * @return
	 */
	public static Test suite(
	){
	    TestSuite suite = new TestSuite();
	    for(
	    	Iterator i = TestDocument_1.SEGMENT_NAMES.iterator();
	    	i.hasNext();
	    ){
	    	suite.addTest(new TestDocument_1((String)i.next()));
	    }
	    return suite;
	}
  
	/**
	 * 
	 */
	protected synchronized void setUp(
	) throws Exception {  

        System.out.println("Creating connection to " + this.getName() + "...");
	    RefPackage rootPkg = new RefRootPackage_1(
    		new Manager_1(
                new Connection_1(
                    new Provider_1(
                        new RequestCollection(
                            new ServiceHeader(
                                PRINCIPALS[0],
                                this.getName(),
                                false, // traceRequest,
                                new QualityOfService()
                            ),
                            dataproviderConnectionfactory.createConnection()
                        ),
                        "persistent".equals(getName())
                    ),
                    false
                )
    		),
            null, // impls
            null, // context
            "cci",
            false
        );
        this.provider = (
            (basePackage)rootPkg.refPackage(
                basePackage.class.getName() // or "org:openmdx:base"
            )
        ).getProviderClass().getProvider(
            new Path(PROVIDER_PATH)
        );
        this.document1 = (document1Package)rootPkg.refPackage(
            document1Package.class.getName() // or "org:openmdx:document1"
        );
    }

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#runTest()
	 */
	protected void runTest() throws Throwable {
		testDocument();
	}
	
	/**
	 * 
	 */
	protected void tearDown(
	) {
		System.out.println("Tearing down " + this.getName() + "...");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testDocument(
	) throws Exception {
        Segment segment;        
        try {
            segment = (Segment) this.provider.getSegment(this.getName());
        } catch (JmiServiceException exception) {
            exception.printStackTrace();
            segment = null;
        }
        if (segment == null) {
            document1.refBegin();
            segment = document1.getSegmentClass().createSegment();
            grantPermissions(segment, null);
            this.provider.addSegment(this.getName(), segment);
            document1.refCommit();
        }
        document1.refBegin();
        Cabinet cabinet = document1.getCabinetClass().createCabinet();
        grantPermissions(cabinet, null);
        segment.addCabinet(DateFormat.getInstance().format(new Date()), cabinet);
        Node drawer = document1.getFolderClass().createFolder();
        grantPermissions(drawer, null);
        drawer.setUri(new String[]{"drawer"});
        cabinet.addNode(drawer);
        Folder folder = document1.getFolderClass().createFolder();
        grantPermissions(folder, null);
        folder.setUri(new String[]{"drawer/folder"});
        cabinet.addNode(folder);
        Folder aap = document1.getFolderClass().createFolder();
        grantPermissions(aap, null);
        aap.setUri(new String[]{"alternate"});
        cabinet.addNode(aap);
        aap = document1.getFolderClass().createFolder();
        grantPermissions(aap, null);
        aap.setUri(new String[]{"alternate/access"});
        cabinet.addNode(aap);
        aap = document1.getFolderClass().createFolder();
        grantPermissions(aap, Collections.singleton(PRINCIPALS[1]));
        aap.setLinkGrantedTo(Collections.singleton(PRINCIPALS[0]));
        aap.setUri(new String[]{"alternate/access/path"});
        cabinet.addNode(aap);
        document1.refCommit();
        document1.refBegin();
        cabinet.setModifyGrantedTo(Collections.EMPTY_SET);        
        document1.refCommit();
        document1.refBegin();
        BinaryContent[] content = new BinaryContent[PDF_FILES.length];
        for(
            int i = 0;
            i < PDF_FILES.length;
            i++
        ){
            Resource resource = document1.getResourceClass().createResource("application/pdf");
            grantPermissions(resource, null);
            resource.setUri(
                i == 0 ? 
                    new String[]{"drawer/folder/document" + i, "alternate/access/path/linked" + i} :
                    new String[]{"drawer/folder/document" + i}
            );
            cabinet.addNode(resource);
            content[i] = document1.getBinaryContentClass().createBinaryContent();
            content[i].setValue(new FileInputStream(PDF_FILES[i]));
            resource.addRevision(content[i]);
        }
        document1.refCommit();
        FolderFilter rootFolders = document1.createFolderFilter();
        rootFolders.forAllParent(FilterOperators.IS_IN, Collections.EMPTY_LIST);
        showNodes(cabinet.getNode(rootFolders), "");
        for(
            int i = 0;
            i < PDF_FILES.length;
            i++
        ){
            File saveAs = File.createTempFile("junit", ".pdf", new File("c:\\tmp"));
            System.out.println("Saving as " + saveAs);
            OutputStream out = new FileOutputStream(saveAs);
            content[i].getValue(out, 0L);
            out.close();
        }
        document1.refBegin();
        deleteNodes(cabinet.getNode(rootFolders));
        document1.refRollback();
        showNodes(cabinet.getNode(rootFolders), "");
//      document1.refBegin();
//      deleteNodes(cabinet.getNode(rootFolders));
//      document1.refCommit();
//      showNodes(cabinet.getNode(rootFolders), "");
	}
  
    protected void showNodes(        
        Collection nodes,
        String indent
    ){
        System.out.println("{");
        for(
            Iterator i = nodes.iterator();
            i.hasNext();
        ){
            String nextIndent = indent + '\t';
            Node node = (Node) i.next();
            System.out.print(nextIndent + node.refMofId() + " (" + node.getClass().getName() + ") uris=" + node.getUri() + ": ");
            if(node instanceof Folder) {
                Folder folder = (Folder) node;
                showNodes(folder.getChild(folder.getCabinet()), nextIndent);
            } else if (node instanceof Resource) {
                Resource resource = (Resource) node;
                System.out.print("type=" + resource.getType() + ": ");
                showRevisions(resource.getRevision(), nextIndent);
            } else {
                System.out.println(node.toString());
            }
        }
        System.out.println(indent + "}");
    }

    protected void deleteNodes(        
        Collection nodes
    ){
        for(
            Iterator i = nodes.iterator();
            i.hasNext();
        ){
            Node node = (Node) i.next();
            if(node instanceof Folder) {
                Folder folder = (Folder) node;
                deleteNodes(folder.getChild(folder.getCabinet()));
            }
            node.refDelete();
        }
    }

    protected void showRevisions(        
        Collection revisions,
        String indent
    ){
        System.out.println("{");
        for(
            Iterator i = revisions.iterator();
            i.hasNext();
        ){
            String nextIndent = indent + '\t';
            Revision revision = (Revision) i.next();
            System.out.print(nextIndent + revision.getClass().getName() + ", length=" + revision.getLength());
        }
        System.out.println(indent + "}");
    }
    
    /**
     * Grant all permission to a set of principals
     */
    private void  grantPermissions(
        DocumentObject object,
        Set to
    ){
        Set principals = to != null ? to : new HashSet(Arrays.asList(PRINCIPALS));
        object.setDeleteGrantedTo(principals);
        object.setModifyGrantedTo(principals);
        object.setReadGrantedTo(principals);
        if(object instanceof Folder)((Folder)object).setLinkGrantedTo(principals);
        if(object instanceof Cabinet)((Cabinet)object).setCreateGrantedTo(principals);
    }

    /**
     * 
     */
    protected document1Package document1;

    /**
     * 
     */
    protected Provider provider;
    
    /**
     * 
     */
    private static final String PROVIDER_PATH = "xri:@openmdx:org.openmdx.document1/provider/ch:omex:test:junit";
    
	/**
	 * 
	 */
	static private final List SEGMENT_NAMES = Arrays.asList(
		new String[]{
        	"volatile",
            "persistent"
		}
	); 

    /**
     * The JUnit principal
     */
    static protected final String[] PRINCIPALS = {"anonymous", "authenticated"};
    
    /**
     * 
     */
    static private final String[] PDF_FILES = new String[]{
        "C:\\opt\\BEA\\weblogic81\\common\\eval\\pointbase\\docs\\pbconsole.pdf",
        "C:\\opt\\BEA\\weblogic81\\common\\eval\\pointbase\\docs\\pbdeveloper.pdf",
        "C:\\opt\\BEA\\weblogic81\\common\\eval\\pointbase\\docs\\pbsystem.pdf"        
    };

    /**
     * Define whether deployment details should logged to the console
     */
    final private static boolean LOG_DEPLOYMENT_DETAIL = false;
    
    /**
     * 
     */
    protected final static Dataprovider_1ConnectionFactory dataproviderConnectionfactory = new Dataprovider_1Deployment(
        new InProcessDeployment(
            "file:src/connector/openmdx-2/sql-server-2000.rar",
            "file:src/ear/junit-test.ear",
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
            System.err
        ),
        null,
        "org/openmdx/test/gateway1/NoOrNew"  
    );
    
}
