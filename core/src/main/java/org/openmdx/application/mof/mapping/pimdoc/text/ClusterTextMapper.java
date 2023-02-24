/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Group Mapper 
 * Owner: the original authors. 
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Package Group Mapper 
 */
public class ClusterTextMapper extends ElementMapper {

    /**
     * Constructor 
     */
    public ClusterTextMapper(
    	Sink sink, 
        ModelElement_1_0 ancestor,
        boolean markdown, 
        PIMDocConfiguration configuration
    ){
		super("Package Cluster", sink, ancestor, markdown, configuration);
    }    

	@Override
	protected String getDisplayName() {
		return super.getDisplayName() + "::**";
	}

	@Override
	protected  void columnBody() {
		printLine("\t<div class=\"column-body\">");
		printLine("\t\t<img src=\"", MagicFile.PACKAGE_CLUSTER.getFileName(MagicFile.Type.IMAGE), "\" alt=\"", getTitle(), "\">");
		printLine("\t</div>");
	}

	@Override
	protected String getEntryName() {
		return ClusterTextMapper.getEntryName(element, MagicFile.Type.TEXT);
	}
	
	public static String getEntryName(ModelElement_1_0 ancestor, MagicFile.Type fileType) {
    	final StringBuilder entryName = new StringBuilder();
    	if(ancestor != null) try {
    		entryName.append(ancestor.getModel().toJavaPackageName(ancestor, null).replace('.', '/')).append('/');
		} catch (ServiceException exception) {
			throw new RuntimeServiceException(exception);
    	}
    	entryName.append(MagicFile.PACKAGE_CLUSTER.getFileName(fileType));
    	return entryName.toString();
	}
	
}
