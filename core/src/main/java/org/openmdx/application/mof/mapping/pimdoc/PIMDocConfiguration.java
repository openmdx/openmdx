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

import java.io.File;
import java.io.FileInputStream;
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
import org.openmdx.base.mof.image.GraphvizStyle;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Factory;


/**
 * PIMDoc configuration
 */
public class PIMDocConfiguration {

	/**
	 * Constructor
	 * 
	 * @param baseDir the (maybe {@code null}) PIMDoc configuration base directory
	 * 
	 * @throws ServiceException if the configuration can't be loaded from the specified property file
	 */
	PIMDocConfiguration(
		String baseDir
	) throws ServiceException {
		if(baseDir == null)  {
			this.configurationDir = null;
			this.graphvizStyleSheet = new GraphvizStyle();
		} else try {
			this.configurationDir = new File(baseDir);
			final File configurationFile = new File(this.configurationDir, CONFIGURATION_FILE);
			if(configurationFile.exists()) {
				properties.load(new FileInputStream(configurationFile));
			}
			this.graphvizStyleSheet = new GraphvizStyle(getSource(MagicFile.STYLE_SHEET, MagicFile.Type.IMAGE));
		} catch (IOException exception) {
			throw new ServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.BAD_PARAMETER,
				"Unable to load 'pimdoc-configuration.properties'",
				new BasicException.Parameter("pimdoc-configuration-directory", baseDir),
				new BasicException.Parameter("pimdoc-configuration-file", CONFIGURATION_FILE)
			);
		}
	}

	/**
	 * The PIMDoc configuration file has to be located in the (configurable) PIMDoc
	 * configuration directory and must be named {@code pimdoc-configuration.properties}.
	 */
	private final static String CONFIGURATION_FILE = "pimdoc-configuration.properties";
	
	/**
	 * The table of contents defaults to all non-empty packages followed by {@code "**"}.
	 */
	private final static String TABLE_OF_CONTENT = "table-of-content";
	
	/**
	 * The title defaults to {@code "openMDX PIM Documentation"}
	 * 
	 * @see #DEFAULT_TITLE
	 */
	private final static String TITLE = "title";
	
	/**
	 * The default title for the  PIM documentation
	 */
	private static final String DEFAULT_TITLE = "openMDX PIM Documentation";

	/**
	 * Graphviz versions < 8.0.2 have issue 144
	 */
	private static final String GRAPHVIZ_ISSUE_144 = "graphviz-issue-144";
	
	/**
	 * The logo defaults to openMDX logo
	 */
	private final static String LOGO = "logo";
	
	/**
	 * The link target for markdown URLs defaults to {@code "_top"}
	 * 
	 * @see #DEFAULT_LINK_TARGET
	 */
	private final static String LINK_TARGET = "link-target";

	/**
	 * The default link target for markdown URLs
	 */
	private final static String DEFAULT_LINK_TARGET = "_top";
	
	private final File configurationDir;
	private final Properties properties = new Properties();
	private final GraphvizStyle graphvizStyleSheet;
	
	public Collection<String> getTableOfContentEntries(){
		return getProperty(TABLE_OF_CONTENT)
			.map(PIMDocConfiguration::toTableOfContents)
			.orElse(Collections.emptyList());
	}
	
	public Factory<Function<String, String>> getMarkdownRendererFactory() {
		return new MarkdownRendererFactory(getLinkTarget());
	}
	
	private String getLinkTarget() {
		return getProperty(LINK_TARGET).orElse(DEFAULT_LINK_TARGET);
	}
	
	public String getTitle(){
		return getProperty(TITLE).orElse(DEFAULT_TITLE);
	}
	
	public boolean graphvizHasIssue144() {
		return Boolean.parseBoolean(getProperty(GRAPHVIZ_ISSUE_144).orElse(Boolean.FALSE.toString()));
	}

	public GraphvizStyle getGraphvizStyleSheet() {
		return graphvizStyleSheet;
	}
	
	public Optional<File> getGraphvizTemplateDirectory() {
		return Optional.of(this.configurationDir);
	}

	URL getSource(MagicFile magicFile, MagicFile.Type type) {
		final File file = getSourceFile(magicFile, type);
		if(file == null) {
			return magicFile.getDefault(type);
		} else try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private File getSourceFile(MagicFile magicFile, MagicFile.Type type) {
		if(this.configurationDir != null) {
			final File file = new File(this.configurationDir, getSourceName(magicFile, type));
			if(file.exists()) {
				return file;
			}
		}
		return null;
	}

	private String getSourceName(MagicFile magicFile, MagicFile.Type type) {
		if(magicFile == MagicFile.WELCOME_PAGE && type == MagicFile.Type.IMAGE) {
			 final Optional<String> logo = getProperty(LOGO);
			 if(logo.isPresent()) {
				 return logo.get();
			 }
		}
		return magicFile.getFileName(type);
	}

	public String getTargetName(MagicFile magicFile, MagicFile.Type type) {
		final String sourceName = getSourceName(magicFile, type);
		return sourceName.substring(sourceName.lastIndexOf('/') + 1);
	}
	
	private Optional<String> getProperty(String propertyName){
		return Optional.ofNullable(this.properties.getProperty(propertyName));
	}
	
	private static Collection<String> toTableOfContents(String entries){
		final Set<String> tableOfContentEntries = new HashSet<>();
		for(String entry : entries.split(",")) {
			if(!PackagePatternComparator.isWildcardPattern(entry)) {
				entry = entry + "::" + entry.substring(entry.lastIndexOf(':') + 1);
			}
			tableOfContentEntries.add(entry.replaceAll("::", ":"));
		}
		return tableOfContentEntries;
	}

}
