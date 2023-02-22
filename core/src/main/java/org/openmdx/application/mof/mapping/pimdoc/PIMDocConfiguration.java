/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: PIMDoc configuration 
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.openmdx.application.mof.mapping.pimdoc.spi.PackagePatternComparator;
import org.openmdx.application.mof.mapping.spi.MarkdownRendererFactory;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.loading.Resources;


/**
 * PIMDoc configuration
 */
public class PIMDocConfiguration {

	/**
	 * Constructor
	 * 
	 * @param url the (maybe {@code null}) PIMDoc configuration property file URL
	 * 
	 * @throws ServiceException if the configuration can't be loaded from the specified property file
	 */
	PIMDocConfiguration(
		String url
	) throws ServiceException {
		if(url != null) try {
			properties.load(new URL(url).openStream());
		} catch (IOException exception) {
			throw new ServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.BAD_PARAMETER,
				"Unable to load UML documentation configuration",
				new BasicException.Parameter("url", url)
			);
		}
	}

	static final String TABLE_OF_CONTENT_ENTRIES = "table-of-content-entries";
	
	private final Properties properties = new Properties();
	
	public Collection<String> getTableOfContentEntries(){
		return getProperty(TABLE_OF_CONTENT_ENTRIES)
			.map(PIMDocConfiguration::toPackageGroups)
			.orElse(Collections.emptyList());
	}
	
	public Factory<Function<String, String>> getMarkdownRendererFactory() {
		return new MarkdownRendererFactory(getLinkTarget());
	}
	
	private String getLinkTarget() {
		return getProperty("link-target").orElse("_top");
	}
	
	public String getTitle(){
		return getProperty("custom-title").orElse("openMDX PIM Documentation");
	}

	URL getActualFile(MagicFile magicFile, MagicFile.Type type) {
		return getProperty(magicFile.getPropertyName(type))
			.map(Resources::fromURI)
			.orElseGet(() -> magicFile.getDefault(type));
	}
	
	private Optional<String> getProperty(String propertyName){
		return Optional.ofNullable(this.properties.getProperty(propertyName));
	}
	
	private static Collection<String> toPackageGroups(String groups){
		final Set<String> packageGroups = new HashSet<>();
		for(String entry : groups.split("::")) {
			if(!PackagePatternComparator.isWildcardPattern(entry)) {
				entry = entry + "::" + entry.substring(entry.lastIndexOf(':') + 1);
			}
			packageGroups.add(entry.replaceAll("::", ":"));
		}
		return packageGroups;
	}

}
