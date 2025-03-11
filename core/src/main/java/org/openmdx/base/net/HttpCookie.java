/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Basic Cookie
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * * Redistribution and use in source and binary forms, with or
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
package org.openmdx.base.net;

import java.net.URI;
import java.text.ParseException;

#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif

/**
 * Basic Cookie
 */
public class HttpCookie implements Comparable<HttpCookie> {

   /**
    * Constructor
    *
    * @param uri the URI provides the target default values
    * @param responseCookie
    */
   public HttpCookie(URI uri, String responseCookie) {
      String name = null;
      String value = null;
      String version = null;
      String domain = null;
      String port = null;
      String path = null;
      this.secure = false;
      this.discard = false;
      this.expiresAt = Long.MAX_VALUE;
      for (String nameValuePair : responseCookie.split(";")) {
         int e = nameValuePair.indexOf('=');
         String key;
         String argument;
         if (e < 0) {
            key = trim(nameValuePair);
            argument = null;
         } else {
            key = trim(nameValuePair.substring(0, e));
            argument = trim(nameValuePair.substring(e + 1));
         }
         if (name == null) {
            name = key;
            value = argument;
         } else {
            key = key.toLowerCase();
            if ("domain".equals(key)) {
               domain = argument;
            } else if ("path".equals(key)) {
               path = argument;
            } else if ("port".equals(key)) {
               port = argument;
            } else if ("secure".equals(key)) {
               this.secure = true;
            } else if ("version".equals(key)) {
               version = argument;
            } else if ("discard".equals(key)) {
               this.discard = true;
            } else if ("expires".equals(key)) {
               try {
                  this.expiresAt = netscapeDateFormat.parse(argument)#if CLASSIC_CHRONO_TYPES .getTime() #else .toEpochMilli()#endif;
               } catch (ParseException exception) {
                  throw new IllegalArgumentException("Invalid 'expires' value: " + argument, exception);
               }
            } else if ("max-age".equals(key)) {
               try {
                  this.expiresAt = System.currentTimeMillis() + 1000 * Long.parseLong(argument);
               } catch (NumberFormatException exception) {
                  throw new IllegalArgumentException("Invalid 'max-age' value: " + port, exception);
               }
            } else if (!"httponly".equals(key) && !"comment".equals(key) && !"commenturl".equals(key)) {
               throw new IllegalArgumentException("Unsupported cookie parameter: " + key);
            }
         }
      }
      if (version == null) {
         this.requestCookie = responseCookie;
      } else {
         StringBuilder requestCookie = new StringBuilder(name).append('=');
         if ("0".equals(version)) {
            requestCookie.append(value);
         } else {
            requestCookie.append('"').append(value).append('"');
            if (path != null) {
               requestCookie.append(";$Path=\"").append(path).append('"');
            }
            if (domain != null) {
               requestCookie.append(";$Domain=\"").append(domain).append('"');
            }
            if (port != null) {
               requestCookie.append(";$Port=\"").append(port).append('"');
            }
         }
         this.requestCookie = requestCookie.toString();
      }
      this.name = name.toLowerCase();
      this.domain = (domain == null ? uri.getHost() : domain).toLowerCase();
      if (port == null) {
         this.ports = new int[] { getPort(uri) };
      } else {
         String[] ports = port.split(",");
         this.ports = new int[ports.length];
         for (int i = 0; i < ports.length; i++) {
            try {
               this.ports[i] = Integer.parseInt(ports[i].trim());
            } catch (NumberFormatException exception) {
               throw new IllegalArgumentException("Invalid 'port' value: " + port, exception);
            }
         }
      }
      this.path = path == null ? uri.getPath() : path;
   }

   /**
    * Otherwise even the same Java object does not match itself unless it contains a dot
    */
   private static final Boolean EMBEDDED_DOT_DOMAIN_HANDLING = Boolean.FALSE;

   /**
    * The netscape date format
    */
   private static final DateTimeFormat netscapeDateFormat = DateTimeFormat.getInstance("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", "GMT", true // lenient
         );

   /**
    * The RFC 2965 compliant form
    */
   private final String requestCookie;

   /**
    *
    */
   private final String name;

   /**
    * The target domain
    */
   private final String domain;

   /**
    * The target ports
    */
   private int[] ports;

   /**
    * The target domain
    */
   private final String path;

   /**
    * The target transport guarantee
    */
   private boolean secure;

   /**
    * The cookie has to be discarded
    */
   private boolean discard;

   /**
    *
    */
   private long expiresAt;

   /**
    * Trim the string and remove quotes
    *
    * @param text
    *
    * @return
    */
   private static String trim(String text) {
      String value = text.trim();
      return value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value;
   }

   /**
    * Retrieve the port from the URL.
    *
    * @param uri
    *
    * @return the port specified by the URL port or scheme part
    */
   private static int getPort(URI uri) {
      int port = uri.getPort();
      if (port >= 0)
         return port;
      String protocol = uri.getScheme();
      if ("http".equalsIgnoreCase(protocol))
         return 80; // according to RFC 2965
      if ("https".equalsIgnoreCase(protocol))
         return 443;
      return -1;
   }

   /**
    * Tells whether the cookie should be sent to the given URI
    *
    * @param uri
    *
    * @return {@code true} if the cookie should be sent to the given URI
    */
   public boolean matches(URI uri) {
      if ((!this.secure || "https".equalsIgnoreCase(uri.getScheme())) &&
      /*java.net.*/HttpCookie.domainMatches(this.domain, uri.getHost())) {
         int uriPort = getPort(uri);
         for (int cookiePort : this.ports) {
            if (uriPort == cookiePort) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Tells whether the cookie has to be discarded
    *
    * @return {@code true} if the cookie has to be discarded
    */
   public boolean isDiscard() {
      return this.discard;
   }

   /**
    * Tells whether the cookie has expired
    *
    * @return {@code true} if the cookie has expired
    */
   public boolean isExpired() {
      return this.expiresAt < System.currentTimeMillis();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return this.requestCookie;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object object) {
      if (object instanceof HttpCookie) {
         HttpCookie that = (HttpCookie) object;
         return this.name.equalsIgnoreCase(that.name) && this.domain.equalsIgnoreCase(that.domain) && this.path.equals(that.path);
      } else {
         return false;
      }
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return this.name.hashCode() + this.domain.hashCode() + this.path.hashCode();
   }

   // @Override
   public int compareTo(HttpCookie that) {
      if (equals(that))
         return 0;
      int result = this.name.compareTo(that.name);
      if (result != 0)
         return result;
      result = this.domain.compareTo(that.domain);
      if (result != 0)
         return result;
      if (this.path.startsWith(that.path))
         return -1;
      if (that.path.startsWith(this.path))
         return +1;
      return this.path.compareTo(that.path);
   }

   // ------------------------------------------------------------------------
   // Supports JRE 5.0
   // ------------------------------------------------------------------------

   /**
    * The utility method to check whether a host name is in a domain
    * or not.
    *
    * <p>This concept is described in the cookie specification.
    * To understand the concept, some terminologies need to be defined first:
    * <blockquote>
    * effective host name = hostname if host name contains dot<br>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;or = hostname.local if not
    * </blockquote>
    * <p>Host A's name domain-matches host B's if:
    * <blockquote><ul>
    *   <li>their host name strings string-compare equal; or</li>
    *   <li>A is a HDN string and has the form NB, where N is a non-empty
    *   name string, B has the form .B', and B' is a HDN string.  (So,
    *   x.y.com domain-matches .Y.com but not Y.com.)</li>
    * </ul></blockquote>
    *
    * <p>A host isn't in a domain (RFC 2965 sec. 3.3.2) if:
    * <blockquote><ul>
    *   <li>The value for the Domain attribute contains no embedded dots,
    *   and the value is not .local.</li>
    *   <li>The effective host name that derives from the request-host does
    *   not domain-match the Domain attribute.</li>
    *   <li>The request-host is a HDN (not IP address) and has the form HD,
    *   where D is the value of the Domain attribute, and H is a string
    *   that contains one or more dots.</li>
    * </ul></blockquote>
    *
    * <p>Examples:
    * <blockquote><ul>
    *   <li>A Set-Cookie2 from request-host y.x.foo.com for Domain=.foo.com
    *   would be rejected, because H is y.x and contains a dot.</li>
    *   <li>A Set-Cookie2 from request-host x.foo.com for Domain=.foo.com
    *   would be accepted.</li>
    *   <li>A Set-Cookie2 with Domain=.com or Domain=.com., will always be
    *   rejected, because there is no embedded dot.</li>
    *   <li>A Set-Cookie2 with Domain=ajax.com will be accepted, and the
    *   value for Domain will be taken to be .ajax.com, because a dot
    *   gets prepended to the value.</li>
    *   <li>A Set-Cookie2 from request-host example for Domain=.local will
    *   be accepted, because the effective host name for the request-
    *   host is example.local, and example.local domain-matches .local.</li>
    * </ul></blockquote>
    *
    * @param domain    the domain name to check host name with
    * @param host      the host name in question
    * @return          <tt>true</tt> if they domain-matches; <tt>false</tt> if not
    */
   private static boolean domainMatches(
      String domain, 
      String host
   ) {
      if (domain == null || host == null)
         return false;

      // if there's no embedded dot in domain and domain is not .local
      boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
      if (EMBEDDED_DOT_DOMAIN_HANDLING.booleanValue()) {
         int embeddedDotInDomain = domain.indexOf('.');
         if (embeddedDotInDomain == 0)
            embeddedDotInDomain = domain.indexOf('.', 1);
         if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1))
            return false;
      }

      // if the host name contains no dot and the domain name is .local
      int firstDotInHost = host.indexOf('.');
      if (firstDotInHost == -1 && isLocalDomain)
         return true;

      int domainLength = domain.length();
      int lengthDiff = host.length() - domainLength;
      if (lengthDiff == 0) {
         // if the host name and the domain name are just string-compare equal
         return host.equalsIgnoreCase(domain);
      } else if (lengthDiff > 0) {
         // need to check H & D component
         String H = host.substring(0, lengthDiff);
         String D = host.substring(lengthDiff);

         return (H.indexOf('.') == -1 && D.equalsIgnoreCase(domain));
      } else if (lengthDiff == -1) {
         // if domain is actually .host
         return (domain.charAt(0) == '.' && host.equalsIgnoreCase(domain.substring(1)));
      }

      return false;
   }

}
