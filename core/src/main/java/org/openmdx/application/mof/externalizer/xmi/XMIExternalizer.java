/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XMI externalizer command-line tool
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.mof.externalizer.xmi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.openmdx.application.mof.externalizer.cci.ModelExternalizer_1_0;
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.JMIFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.externalizer.spi.ModelExternalizer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.uses.gnu.getopt.Getopt;
import org.openmdx.uses.gnu.getopt.LongOpt;

/**
 * XMI Externalizer
 */
public class XMIExternalizer {

	/**
	 * Constructor
	 */
	private XMIExternalizer(
		String[] arguments	
	){
        String openmdxjdo = null;
        String xmiDialect = "emf";
        AnnotationFlavour annotationFlavour = AnnotationFlavour.STANDARD;
        JakartaFlavour jakartaFlavour = JakartaFlavour.VERSION_8;
        JMIFlavour jmiFlavour = JMIFlavour.CLASSIC;
        final List<String> pathMapSymbols = new ArrayList<String>();
        final List<String> pathMapPaths = new ArrayList<String>();
        final List<String> formats = new ArrayList<String>();

        final Getopt g = getOptions(arguments); 
        int c;
        while ((c = g.getopt()) != -1) {
            switch(c) {
	            case 'f':
					final String flavour = g.getOptarg();
					SysLog.info("Externalizing model for openMDX " + flavour);
					jakartaFlavour = JakartaFlavour.fromFlavourVersion(flavour);
					jmiFlavour = JMIFlavour.fromFlavourVersion(flavour);
	                break;
	            case 'j':
	                openmdxjdo = g.getOptarg();
	                break;
                case 'm':
                	annotationFlavour = AnnotationFlavour.MARKDOWN;
                    break;
                case 'o':
                    outFileName = g.getOptarg();
                    break;
	            case 'p':
	                pathMapPaths.add(
	                    g.getOptarg()
	                );
	                break;
                case 's':
                    pathMapSymbols.add(
                        g.getOptarg()
                    );
                    break;
                case 't':
                    formats.add(
                        g.getOptarg()
                    );
                    break;
                case 'u':
                    url = g.getOptarg();
                    break;
                case 'x':
                    xmiDialect = g.getOptarg();
                    break;
            }
        }
        
        this.modelNames = getModelNames(arguments, g);
        this.formats = formats;
        this.modelExternalizer = new ModelExternalizer_1(openmdxjdo, annotationFlavour, jakartaFlavour, jmiFlavour);
    	this.xmiFormat = toFormat(xmiDialect);
    	this.pathMap = toPathMap(pathMapSymbols, pathMapPaths);
	}

	private final short xmiFormat;
    private String url = null;
    private String outFileName = "./out.jar";
    
    private final List<String> modelNames;
    private final ModelExternalizer_1_0 modelExternalizer;
    private final List<String> formats;
    private final Map<String, String> pathMap;
	
	private List<String> getModelNames(
		String[] arguments, 
		final Getopt g
	){
		List<String> modelNames = getParameters(g, arguments);
        if(modelNames.size() != 1) {
        	throw BasicException.initHolder(
        		new IllegalArgumentException(
    				"number of model names must be 1",
    				BasicException.newEmbeddedExceptionStack(
		                BasicException.Code.DEFAULT_DOMAIN,
		                BasicException.Code.INVALID_CONFIGURATION,
		                new BasicException.Parameter("model names", modelNames)
		            )
		        )
            );
        }
		return modelNames;
	}

	private Map<String, String> toPathMap(
		List<String> pathMapSymbols,
		List<String> pathMapPaths
	) {
		final Map<String,String> pathMap = new HashMap<String,String>();
        for(int i = 0; i < pathMapSymbols.size(); i++) {
            pathMap.put(
                pathMapSymbols.get(i),
                pathMapPaths.get(i)
            );
        }
        System.out.println("INFO:    pathMap=" + pathMap);
		return pathMap;
	}

    /**
     * Main
     * 
     * @param arguments
     */
    public static void main(
        String... arguments
    ) {
        try {
        	final XMIExternalizer xmiExternalizer = new XMIExternalizer(arguments);
        	xmiExternalizer.importModel();
        	xmiExternalizer.exportModel();
        } catch(Exception e) {
        	new ServiceException(e).log().printStackTrace();
            System.exit(-1);
        }
    }
    
    private void importModel(
	) throws IOException, ResourceException{
    	modelExternalizer.importModel(
			new XMIImporter_1(
				new URL(url),
				xmiFormat,
				pathMap,
				System.out,
				System.err,
				System.err
			)
		);
    }
    
    private void exportModel(
	) throws IOException, ResourceException {
        try (FileOutputStream fos = new FileOutputStream(new File(outFileName))){
        	fos.write(
    			modelExternalizer.externalizePackageAsJar(
    				modelNames.get(0),
    				formats
    			)
    		);  
        }
    }
    
	private static short toFormat(
		String xmiDialect
	) {
		if("poseidon".equals(xmiDialect)) {
            System.out.println("INFO:    Gentleware Poseidon XMI 1");
            return XMIImporter_1.XMI_FORMAT_POSEIDON;
        } else if("magicdraw".equals(xmiDialect)) {
            System.out.println("INFO:    MagicDraw XMI 1");
            return XMIImporter_1.XMI_FORMAT_MAGICDRAW;
        } else if("rsm".equals(xmiDialect)) {
            System.out.println("INFO:    IBM Rational Software Modeler XMI 2");          
            return XMIImporter_1.XMI_FORMAT_RSM;
        } else if("emf".equals(xmiDialect)) {
            System.out.println("INFO:    Eclipse UML2 Tools");          
            return XMIImporter_1.XMI_FORMAT_EMF;
        } else {
            System.out.println("INFO:    Unknown XMI format");          
            return 0;
        }
	}
	
    /**
     * Extract the options from the arguments
     */
	private static Getopt getOptions(
		String[] args
	) {
		Getopt options = new Getopt(
		    XMIExternalizer.class.getName(),
		    args,
		    "",
		    new LongOpt[]{
		        new LongOpt("flavour", LongOpt.REQUIRED_ARGUMENT, null, 'f'), 
		        new LongOpt("openmdxjdo", LongOpt.REQUIRED_ARGUMENT, null, 'j'),
		        new LongOpt("markdown-annotations", LongOpt.NO_ARGUMENT, null, 'm'),
		        new LongOpt("out", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
		        new LongOpt("pathMapPath", LongOpt.REQUIRED_ARGUMENT, null, 'p'),
		        new LongOpt("pathMapSymbol", LongOpt.REQUIRED_ARGUMENT, null, 's'),
		        new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null, 't'),
		        new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
		        new LongOpt("xmi", LongOpt.REQUIRED_ARGUMENT, null, 'x'),
		    }
		);
		return options;
	}

	/**
     * Extract the parameters from the arguments
	 */
	private static List<String> getParameters(Getopt g, String[] args) {
		return Arrays.asList(args).subList(g.getOptind(), args.length);
	}

}
