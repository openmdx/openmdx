/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CacheTest.scala,v 1.1 2010/11/10 14:37:25 wfro Exp $
 * Description: Cache Test 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/10 14:37:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
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
package test_scala.cache;

import javax.cache._
import java.util.{Date, Collections}
import collection.JavaConversions._

/**
 * Cache Test
 */
object CacheTest {
    
    /**
     * Use the configured cache factory
     * 
     * @param arguments
     */
	def main(args: Array[String]) {
		val manager = CacheManager.getInstance();
        try {
            val factory = manager.getCacheFactory(); 
            var now = new Date();
            val cache1 = factory.createCache(Collections.emptyMap());
            cache1.asInstanceOf[java.util.Map[String,Object]].put("created", now);
            now = new Date(now.getTime() + 1000);
            val cache2 = factory.createCache(Collections.emptyMap());
            cache2.asInstanceOf[java.util.Map[String,Object]].put("created", now);
            println(cache1.getClass().getName() + '@' + System.identityHashCode(cache1) + " created at " + cache1.get("created") + "(" + cache1.getCacheEntry("created") + ")");
            println(cache2.getClass().getName() + '@' + System.identityHashCode(cache2) + " created at " + cache2.get("created") + "(" + cache2.getCacheEntry("created") + ")");
        } catch {
        	case exception: CacheException =>
        		exception.printStackTrace();
        }
        
    }

}
