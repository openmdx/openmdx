/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Standard File Handler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package test.openmdx.kernel.log;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.logging.BasicFormatter;
import org.openmdx.kernel.logging.ClassicFormatter;

/**
 * TestStandardFileHandler
 */
public abstract class AbstractFileHandlerTest {

    protected abstract Handler newFileHandler(
        Logger logger
    ) throws SecurityException, IOException;
    
    public AbstractFileHandlerTest(
    ) throws SecurityException, IOException{
        this.classic = Logger.getLogger("Classic");
        {
            Handler handler = newFileHandler(this.classic);
            handler.setLevel(Level.FINER);
            handler.setFormatter(new ClassicFormatter());
            classic.setLevel(Level.FINER);
            classic.addHandler(handler);
        }
        this.basic = Logger.getLogger("Basic");
        {
            Handler handler = newFileHandler(this.basic);
            handler.setLevel(Level.FINER);
            handler.setFormatter(new BasicFormatter());
            basic.addHandler(handler);
            basic.setLevel(Level.FINER);
        }
        this.thrown = new ServiceException(
            new RuntimeException(
                "Throwable[1]",
                new Exception(
                    "Throwable[2]",
                    new IllegalArgumentException(
                        "Throwable[3]",
                        new Throwable(
                            "Throable[4]",
                            new AssertionError(
                                "Throwable[5]"
                            )
                        )
                   )
                )
            ),
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.GENERIC,
            "Logging Test",
            new BasicException.Parameter("parameter",0,1,2)
        );
    }
    
    protected final Logger classic;
    protected final Logger basic;
    protected final Throwable thrown;
    protected static final int COUNT = 100;
    
    @Test
    public void testInit() throws SecurityException, IOException{
        use(classic,1);
        use(basic,1);
    }

    @Before
    public void collectGarbage(){
        Runtime.getRuntime().gc();
    }
    
    @Test
    public void testClassic() throws SecurityException, IOException{
        use(classic,COUNT);
    }

    @Test
    public void testBasic() throws SecurityException, IOException{
        use(basic,COUNT);
    }
        
    /**
     * Use the prepared logger
     * 
     * @param logger
     */
    protected void use(
        Logger logger,
        int count
    ){
        logger.info("Hello " + logger.getName() + " " + count);
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.freeMemory();
        for(int i = 0; i < count; i++) {
            logger.fine("Loop " + i);
            logger.log(
                Level.FINE,
                "An exception occured", 
                this.thrown
            );
        }
        memory -= runtime.freeMemory();
        logger.info(logger.getName() + " used " + memory + " bytes");
    }

}
