/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: EntityMapper
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
package org.openmdx.kernel.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.openmdx.kernel.loading.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Entity Mapper
 */
public class EntityMapper implements EntityResolver {

    /**
     * Constructor
     */
    private EntityMapper() {
        // Avoid instantiation
    }

    /**
     * The singleton
     */
    private static final EntityMapper instance = new EntityMapper();

    /**
     * Public id mappings
     */
    private final Map<String, String> publicIdMappings =
        new Hashtable<String, String>();

    /**
     * System id mappings
     */
    private final Map<String, String> systemIdMappings =
        new Hashtable<String, String>();

    /**
     * Get an entity mapper instance
     *
     * @return an entity mapper instance
     */
    public static EntityResolver getInstance() {
        return EntityMapper.instance;
    }

    /**
     * Register a public id mapping
     *
     * @param publicId
     * @param uri
     */
    public static void registerPublicId(String publicId, String url) {
        EntityMapper.instance.publicIdMappings.put(publicId, url);
    }

    /**
     * Register a system id mapping.
     * <p>
     * The preferred way is to use registerPublicId instead of the method.
     *
     * @param systemId
     * @param url
     */
    public static void registerSystemId(String systemId, String url) {
        EntityMapper.instance.systemIdMappings.put(systemId, url);
    }
    

    // ------------------------------------------------------------------------
    // Implements EntityResolver
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     * java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId){
    	return resolvePublicId(publicId).orElseGet(
    		() -> resolveSystemId(systemId)
    	);
    }

    private Optional<InputSource> resolvePublicId(String systemId){
    	return Optional.ofNullable(systemId)
    		.map(systemIdMappings::get)
    		.map(EntityMapper::openStreamFromURI)
    		.map(InputSource::new);
    }

    private InputSource resolveSystemId(String publicId){
    	return Optional.ofNullable(publicId)
    		.map(publicIdMappings::get)
    		.map(EntityMapper::openStreamFromURI)
    		.map(InputSource::new)
    		.orElse(null);
    }
    
	/**
     * Handle {@code xri://+resource}-URLs internally
     * 
     * @param uri
     *            the resource identifier
     * @return a stream providing the resource; or {@code null} if no such stream can be acquired
     */
    private static InputStream openStreamFromURI(
        String uri
    ){
    	final URL url = Resources.fromURI(uri);
    	try {
			return url == null ? null : url.openStream();
		} catch (IOException e) {
			return null;
		}
    }

}
