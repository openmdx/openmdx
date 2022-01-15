/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ReplyHeaderFilter
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 */
package org.openmdx.kernel.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/** A servlet filter that simply adds all header specified in its config
to replies the filter is mapped to. An example would be to set the cache
 control max age:

   <filter>
      <filter-name>CacheControlFilter</filter-name>
      <filter-class>filter.ReplyHeaderFilter</filter-class>
      <init-param>
         <param-name>Cache-Control</param-name>
         <param-value>max-age=3600</param-value>
      </init-param>
   </filter>
   
 <filter-mapping>
    <filter-name>CacheControlFilter</filter-name>
    <url-pattern>/images/*</url-pattern>
 </filter-mapping>
 <filter-mapping>
    <filter-name>CacheControlFilter</filter-name>
    <url-pattern>*.js</url-pattern>
 </filter-mapping>
 */
public class ReplyHeaderFilter implements Filter
{
   private String[][] replyHeaders = {{}};

   //------------------------------------------------------------------------
   public void init(
       FilterConfig config
   ) {
      Enumeration<?> names = config.getInitParameterNames();
      List<String[]> headers = new ArrayList<String[]>();
      while( names.hasMoreElements()) {
         String name = (String) names.nextElement();
         String value = config.getInitParameter(name);
         String[] pair = {name, value};
         headers.add(pair);
      }
      this.replyHeaders = new String[headers.size()][2];
      headers.toArray(this.replyHeaders);
   }

   //------------------------------------------------------------------------
   public void doFilter(
       ServletRequest request, 
       ServletResponse response,
      FilterChain chain
   ) throws IOException, ServletException {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      for(int n = 0; n < replyHeaders.length; n ++)
      {
         String name = replyHeaders[n][0];
         String value = replyHeaders[n][1];
         httpResponse.addHeader(name, value);
      }
      chain.doFilter(request, response);
   }

   //------------------------------------------------------------------------
   public void destroy(
   ){
       // Nothing to do
   }
   
}
