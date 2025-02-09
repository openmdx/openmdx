/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Flexmark Extensions
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
package org.openmdx.application.mof.mapping.spi;

import java.util.Collections;

import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.html.MutableAttributes;

/**
 * Flexmark Extensions
 */
class FlexmarkExtensions {
	
	static MutableDataSet getOptions(final String linkTarget) {
		final MutableDataSet options = new MutableDataSet();
		options.set(Parser.EXTENSIONS, Collections.singleton(new LinkTargetExtension(linkTarget)));
		return options;
	}
	
	/**
	 * Link Target Extension
	 */
	private static class LinkTargetExtension implements HtmlRenderer.HtmlRendererExtension {

		LinkTargetExtension(String linkTarget) {
			this.linkTarget = linkTarget;
		}

		private final String linkTarget;
		
	    @Override
	    public void rendererOptions(final MutableDataHolder options) {
	        // add any configuration settings to options you want to apply to everything, here
	    }

	    @Override
	    public void extend(final HtmlRenderer.Builder rendererBuilder, final String rendererType) {
	        rendererBuilder.attributeProviderFactory(createAttributeProviderFactory());
	    }
		
		private AttributeProviderFactory createAttributeProviderFactory() {
	        return new IndependentAttributeProviderFactory() {

				@Override
				public  AttributeProvider apply( LinkResolverContext context) {
	                return new LinkTargetAttributeProvider();
				}
	        };
	    }
		
		/**
		 * Link Target Attribute Provider
		 */
		class LinkTargetAttributeProvider implements AttributeProvider {
			
			@Override
			public void setAttributes( Node node,  AttributablePart part,
					 MutableAttributes attributes) {
		        if (part == AttributablePart.LINK) {
		            attributes.replaceValue(Attribute.TARGET_ATTR, linkTarget);
		        }
			}
		}
		
	}
	
}