/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Classic Bean
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
package org.openmdx.base.beans;

import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
import org.openmdx.kernel.log.SysLog;

public class ClassicBean {
    private Date mutableDateTime;
    private Date immutableDateTime;
    private XMLGregorianCalendar mutableDate;
    private XMLGregorianCalendar immutableDate;
    private Duration yearMonthDuration;
    private Duration yearMonthDayDuration;
    private Duration dayTimeDuration;

    public Date getMutableDateTime() {
        return this.mutableDateTime;
    }

    public void setMutableDateTime(Date mutableDateTime) {
        SysLog.detail("setMutableDateTime", mutableDateTime);
        this.mutableDateTime = mutableDateTime;
    }

    public Date getImmutableDateTime() {
        return this.immutableDateTime;
    }

    public void setImmutableDateTime(Date immutableDateTime) {
        SysLog.detail("setImmutableDateTime", immutableDateTime);
        this.immutableDateTime = immutableDateTime;
    }

    public XMLGregorianCalendar getMutableDate() {
        return this.mutableDate;
    }

    public void setMutableDate(XMLGregorianCalendar mutableDate) {
        SysLog.detail("setMutableDate", mutableDate);
        this.mutableDate = mutableDate;
    }

    public XMLGregorianCalendar getImmutableDate() {
        return this.immutableDate;
    }

    public void setImmutableDate(XMLGregorianCalendar immutableDate) {
        SysLog.detail("setImmutableDate", immutableDate);
        this.immutableDate = immutableDate;
    }

    public Duration getYearMonthDuration() {
        return yearMonthDuration;
    }

    public void setYearMonthDuration(Duration yearMonthDuration) {
        SysLog.detail("setYearMonthDuration", yearMonthDuration);
        this.yearMonthDuration = yearMonthDuration;
    }

    public Duration getDayTimeDuration() {
        return dayTimeDuration;
    }

    public void setDayTimeDuration(Duration dayTimeDuration) {
        SysLog.detail("setDayTimeDuration", dayTimeDuration);
        this.dayTimeDuration = dayTimeDuration;
    }

    public Duration getYearMonthDayDuration() {
        return yearMonthDayDuration;
    }

    public void setYearMonthDayDuration(Duration yearMonthDayDuration) {
        SysLog.detail("setYearMonthDayDuration", dayTimeDuration);
        this.yearMonthDayDuration = yearMonthDayDuration;
    }
}
