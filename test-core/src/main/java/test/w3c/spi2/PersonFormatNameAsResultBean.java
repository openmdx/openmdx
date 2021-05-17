/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: PersonFormatNameAsResultBean 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package test.w3c.spi2;


/**
 * PersonFormatNameAsResultBean
 *
 */
public class PersonFormatNameAsResultBean {

    private String formattedName = null;
    private String[] formattedNameAsList = new String[]{};
    private String[] formattedNameAsSet = new String[]{};
    private String[] formattedNameAsSparseArray = new String[]{};
    
    public String getFormattedName() {
        return this.formattedName;
    }
    public void setFormattedName(String formattedName) {
        this.formattedName = formattedName;
    }
    public String[] getFormattedNameAsList() {
        return this.formattedNameAsList;
    }
    public String getFormattedNameAsList(int index) {
        return this.formattedNameAsList[index];
    }
    public void setFormattedNameAsList(String... formattedNameAsList) {
        this.formattedNameAsList = formattedNameAsList;
    }
    public void setFormattedNameAsList(int index, String formattedNameAsList) {
        this.formattedNameAsList[index] = formattedNameAsList;
    }
    public String[] getFormattedNameAsSet() {
        return this.formattedNameAsSet;
    }
    public String getFormattedNameAsSet(int index) {
        return this.formattedNameAsSet[index];
    }
    public void setFormattedNameAsSet(String... formattedNameAsSet) {
        this.formattedNameAsSet = formattedNameAsSet;
    }
    public String[] getFormattedNameAsSparseArray() {
        return this.formattedNameAsSparseArray;
    }
    public String getFormattedNameAsSparseArray(int index) {
        return this.formattedNameAsSparseArray[index];
    }
    public void setFormattedNameAsSparseArray(String... formattedNameAsSparseArray) {
        this.formattedNameAsSparseArray = formattedNameAsSparseArray;
    }
    public void setFormattedNameAsSparseArray(int index, String formattedNameAsSparseArray) {
        this.formattedNameAsSparseArray[index] = formattedNameAsSparseArray;
    }

}
