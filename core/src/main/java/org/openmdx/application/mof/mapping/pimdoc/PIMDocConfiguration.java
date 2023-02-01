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

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;


/**
 * PIMDoc configuration
 */
class PIMDocConfiguration {

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

	private final Properties properties = new Properties();
	
    private static final String RESOURCE_URL_PREFIX = "xri://+resource/";
    
	boolean enumeratePackages() {
		return Optional.ofNullable(this.properties.getProperty("package-groups")) != null;
	}
	
	Collection<String> getPackageGroups(){
		return Optional.ofNullable(this.properties.getProperty("package-groups"))
				.map(PIMDocConfiguration::toPackageGroups)
				.orElseGet(() -> Collections.singletonList(PackagePatternComparator.getCatchAllPattern()));
	}
	
	String getLinkTarget() {
		return getProperty("link-target").orElse("_top");
	}
	
	String getTitle(){
		return getProperty("title").orElse("openMDX PIM Documentation");
	}

	URL getActualFile(MagicFile magicFile) {
		return getProperty(magicFile.getPropertyName()).map(PIMDocConfiguration::toURL).orElseGet(() -> magicFile.getDefault());
	}
	
	private Optional<String> getProperty(String propertyName){
		return Optional.ofNullable(this.properties.getProperty(propertyName));
	}
	
	private static URL toURL(String url) {
		try {
			return url.startsWith(RESOURCE_URL_PREFIX) ? 
				Resources.getResource(url.substring(RESOURCE_URL_PREFIX.length())) : 
				new URL(url);
		} catch (MalformedURLException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.BAD_PARAMETER,
				"Unable to acquire the configured URL",
				new BasicException.Parameter("url", url)
			);
		}
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
