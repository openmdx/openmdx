/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TextsLoader
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Codes;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.Texts.TextsBundle;

/**
 * TextsLoader
 *
 */
public class TextsLoader extends Loader {

	/**
	 * Constructor.
	 *
	 * @param context
	 * @param portalExtension
	 */
	public TextsLoader(
		Path codeSegmentIdentity,
		ServletContext context,
		PortalExtension_1_0 portalExtension,
		PersistenceManagerFactory pmf
	) {
		super(
			context,
			portalExtension
		);
		this.pmf = pmf;
		this.codeSegmentIdentity = codeSegmentIdentity;
	}

	/**
	 * Get default texts bundles.
	 * 
	 * @param locale
	 * @param context
	 * @return
	 * @throws ServiceException
	 */
	public static List<ResourceBundle> getDefaultTextsBundles(
		String[] locale,
		ServletContext context
	) throws ServiceException {
		List<ResourceBundle> textsBundles = new ArrayList<ResourceBundle>();
		int fallbackLocaleIndex = 0;
		for(int i = 0; i < locale.length; i++) {
			// Overloading requires sorting of text files by name
			Set<String> textsPaths = new TreeSet<String>();
			if(locale[i] != null) {
				fallbackLocaleIndex = 0;
				Set<String> resourcePaths = context.getResourcePaths("/WEB-INF/config/texts/" + locale[i]);
				if(resourcePaths == null) {
					for(int j = i-1; j >= 0; j--) {
						if((locale[j] != null) && locale[i].substring(0,2).equals(locale[j].substring(0,2))) {
							fallbackLocaleIndex = j;
							break;
						}
					}
					SysLog.info(locale[i] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
					resourcePaths = context.getResourcePaths("/WEB-INF/config/texts/" + locale[fallbackLocaleIndex]);
				}
				textsPaths.addAll(resourcePaths);
			}
			List<InputStream> textsStreams = new ArrayList<InputStream>();
			for(String path: textsPaths) {
				if(!path.endsWith("/")) {            
					SysLog.info("Loading " + path);
					textsStreams.add(
						context.getResourceAsStream(path)
					);
				}
			}
			try {
				textsBundles.add(
					new PropertyResourceBundle(
						new InputStreamReader(
							new SequenceInputStream(Collections.<InputStream>enumeration(textsStreams)),
							"UTF-8"
						)
					)
				);
			} catch(Exception e) {
				throw new ServiceException(e);
			}
		}
		return textsBundles;
	}

	/**
	 * Load texts resources for given locales.
	 * 
	 * @param locale
	 * @return
	 * @throws ServiceException
	 */
	synchronized public void loadTexts(
		String[] locale
	) throws ServiceException {
		String messagePrefix = new Date() + "  ";
		System.out.println(messagePrefix + "Loading texts");
		SysLog.info("Loading texts");
		List<ResourceBundle> textBundles = getDefaultTextsBundles(
			locale, 
			this.context
		);
		String segmentName = this.codeSegmentIdentity.getSegment(4).toClassicRepresentation();
		Codes.storeBundles(
			this.codeSegmentIdentity,
			TextsBundle.class.getSimpleName(),
			this.pmf.getPersistenceManager(this.getAdminPrincipal(segmentName), null),
			textBundles
		);
		System.out.println(messagePrefix + "Done");
		SysLog.info("Done");
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private final PersistenceManagerFactory pmf;
    private final Path codeSegmentIdentity;

}
