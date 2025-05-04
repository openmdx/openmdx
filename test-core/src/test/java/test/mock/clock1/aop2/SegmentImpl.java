/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SegmentImpl 
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
package test.mock.clock1.aop2;

import java.lang.reflect.UndeclaredThrowableException;
import java.text.ParseException;

import org.openmdx.base.aop2.AbstractObject;

import org.w3c.format.DateTimeFormat;
import org.w3c.time.ChronoUtils;
import test.openmdx.clock1.jmi1.Clock1Package;

/**
 * AOP 2 Segment Plugin-In
 */
public class SegmentImpl extends AbstractObject<test.openmdx.clock1.jmi1.Segment,test.openmdx.clock1.cci2.Segment, SegmentImpl.Context> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public SegmentImpl(
        test.openmdx.clock1.jmi1.Segment same,
        test.openmdx.clock1.cci2.Segment next
    ) {
        super(same, next);
        
    }

    static class Context {

        private long epochMillis = java.time.Instant.parse("2000-04-01T12:00:00Z").toEpochMilli();

        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getDateTime() {
            return ChronoUtils.ofEpochMilliseconds(this.epochMillis);
        }

        void setDateTime(#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif dateTime) {
            this.epochMillis = ChronoUtils.getEpochMilliseconds(dateTime);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.AbstractObject#newContext()
     */
    @Override
    protected Context newContext(
    ) {
        return new Context();
    }

    public test.openmdx.clock1.jmi1.Time currentDateAndTime(
    ){
        Clock1Package clock1Package = (Clock1Package) this.sameObject().refImmediatePackage();
        return clock1Package.createTime(thisContext().getDateTime());
    }
    
    public org.openmdx.base.jmi1.Void setDateAndTime(
        test.openmdx.clock1.jmi1.Time in
    ){
        final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif utc = in.getUtc();
        thisContext().setDateTime(utc);
        AsAdmin.notifyDateAndTimeChange(sameObject(), utc);
        return super.newVoid();
    }
    
    public java.lang.String getDescription(
    ){
        throw new ArrayIndexOutOfBoundsException("Mocked behaviour");
    }
    
    /**
     * Sets a new value for the attribute {@code description}.
     * @param description The possibly null new value for attribute {@code description}.
     */
    public void setDescription(
      java.lang.String description
    ) {
        System.out.println(sameObject().refMofId() + ": " + description);
    }
    
    
    public org.openmdx.base.cci2.Provider getProvider(
    ){
        return this.nextManager().getObjectById(
            org.openmdx.base.cci2.Provider.class,
            "xri://@openmdx*test.openmdx.clock1/provider/Mocked"
        );
    }
    
}
