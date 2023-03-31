/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Diagram Drawer 
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
package org.openmdx.base.mof.spi;

import java.io.File;
import java.net.MalformedURLException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.FileSink;
import org.openmdx.base.mof.image.GraphvizTemplates;
import org.openmdx.base.mof.image.GraphvizStyle;
import org.openmdx.kernel.loading.Resources;

/**
 * Diagram Drawer
 */
public class Model_1DiagramDrawer2 {
	
	/**
     * Completes diagram templates with model information.
     * 
     * @param arguments ‹source-directory› ‹destination-directory› [‹style-sheet-file›]
     */
    public static void main(
        String... arguments
    ) {
        if (arguments == null || arguments.length < 2 || arguments.length > 3) {
            System.err.println("Usage: java " + Model_1DiagramDrawer2.class.getName() + " ‹source-directory› ‹destination-directory› [‹style-sheet-file›]");
        } else {
            try {
                final File sourceDir = new File(arguments[0]);
                final File destDir = new File(arguments[1]);
                final File styleFile = arguments.length == 3 ? new File(arguments[2]) : null;
                System.out.println("INFO: Mapping model diagram templates from " + sourceDir + " to " + destDir);
                System.out.flush();
                final GraphvizTemplates drawer = new GraphvizTemplates(
                	Model_1Factory.getModel(), 
                	getStyleSheet(styleFile), 
                	new FileSink(destDir)
                );
                drawer.drawDiagrams(sourceDir);
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /**
     * Provides the URL if a style file shall be used
     * 
     * @param styleFile the (optional) style file
     * 
     * @return the style sheet URL or {@code null}
     */
    private static GraphvizStyle getStyleSheet(File styleFile) {
    	if(styleFile == null) {
    		System.out.println("INFO: Do not use a Graphviz style file");
    		return new GraphvizStyle();
    	}
    	if(styleFile.exists() && styleFile.isFile()) {
    		try {
				return new GraphvizStyle(styleFile.toURI().toURL());
			} catch (MalformedURLException|RuntimeServiceException e) {
				System.err.println("WARNING: Unable to read " + styleFile);
			}
    	}
		System.out.println("INFO: falling back to default-style-sheet.gvs");
    	return new GraphvizStyle(Resources.getResource("org/openmdx/application/mof/mapping/pimdoc/default-style-sheet.gvs"));
    }
        
}
