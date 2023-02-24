/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Style Sheet
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
package org.openmdx.base.mof.spi;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * Graphviz Style Sheet
 */
public class GraphvizStyleSheet {

	/**
	 * Read the Graphvis style sheet information from the given URL
	 * 
	 * @param url the Graphvis stylesheet source URL
	 */
	public GraphvizStyleSheet(URL url) {
		final String content = readContent(url);
		final Matcher sections = STYLES.matcher(content);
		while(sections.find()) {
			final String name = sections.group(1).toLowerCase();
			final Matcher entries = ENTRY.matcher(sections.group(2));
			final Map<String,String> style = new HashMap<>();
			while(entries.find()) {
				final String key = entries.group(1).toLowerCase();
				final String value = entries.group(2).trim();
				style.put(key, value);
			}
			styles.put(name, style);
		}
	}
	
	/**
	 * All information must be provided in the template files themselves
	 */
	public GraphvizStyleSheet() {
		super();
	}

	private final Map<String,Map<String,String>> styles = new HashMap<>();
	private static final Pattern STYLES = Pattern.compile("([-\\w\\.]+)\\s*\\[([^\\]]+)\\]", Pattern.DOTALL);
	private static final Pattern ENTRY = Pattern.compile("([-\\w]+)\\s*=\"([^\"]+)\"", Pattern.DOTALL);
	
	static String readContent(URL url) {
		try (Reader reader = new InputStreamReader(url.openStream()); CharArrayWriter buffer = new CharArrayWriter()) {
			CharacterLargeObjects.streamCopy(reader, 0, buffer);
			return removeComments(buffer.toString());
		} catch (IOException|NullPointerException exception) {
			throw new RuntimeServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.INVALID_CONFIGURATION, "Unable to read the image style sheet",
					new BasicException.Parameter("url", url));
		}
	}

	private static String removeComments(final String text) {
		return text.replaceAll("(?s)//[^\\v]*[\\v]|/\\*.*?\\*/", "\n");
	}

	public Map<String, String> getElementStyle(String name) {
		return new TreeMap<>(styles.getOrDefault(name.toLowerCase(), Collections.emptyMap()));
	}

	public Map<String, String> getClassStyle(String name) {
		return new TreeMap<>(styles.getOrDefault('_' + name.toLowerCase(), Collections.emptyMap()));
	}
	
}
