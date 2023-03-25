/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Dumper 
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
package org.openmdx.base.mof.spi;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.openmdx.application.mof.mapping.xmi.XMIMapper_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Model Dumper
 */
public class Model_1Dumper {

    /**
     * Dump the model repository content to an output stream
     * 
     * @param target
     *            the destination
     * @param mimeType
     *            the MIME type defines the output format
     * @param model
     *            the model to be dumped
     * 
     * @throws ServiceException
     */
    public static void save(
        OutputStream target,
        String mimeType,
        Model_1_0 model
    )
        throws ServiceException {
        new XMIMapper_1().externalizeRepository(
            model,
            target,
            mimeType
        );
    }

    /**
     * Dumps the content of the model repository to the file specified by the
     * (single) argument.
     * 
     * @param the
     *            target file name
     * @param mime
     *            type
     */
    public static void main(
        String... arguments
    ) {
        if (arguments == null || arguments.length < 1 || arguments.length > 2) {
            System.err.println("Usage: java " + Model_1Dumper.class.getName() + " <targetFileName> [<mimeType>]");
        } else {
            final String targetFileName = arguments[0];
            final String mimeType = arguments.length > 1 ? arguments[1] : "application/vnd.openmdx-xmi.wbxml";
            try {
                System.out.println("Saving the model repository to " + targetFileName + "…");
                final Model_1_0 model = Model_1Factory.getModel();
                try (final FileOutputStream target = new FileOutputStream(targetFileName)) {
                    save(target, mimeType, model);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
    }

}
