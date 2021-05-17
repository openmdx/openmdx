/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Encodings
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 20010, OMEX AG, Switzerland
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
package org.openmdx.base.text;

import java.nio.charset.Charset;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Encoding helper
 * <p>
 * See <code>http://www.iana.org/assignments/character-sets</code>
 */
public class Charsets {

	private Charsets(
	){
	}

	/**
	 * Convert IANA's MIBenum values to Java charset names
	 * 
	 * @param mibEnum
	 * 
	 * @return the Java charset name
	 * 
	 * @throws ServiceException if the given value is not supported
	 */
	public static String toCharsetName(
	    int mibEnum
	) throws ServiceException {
	    String[] names;
	    if(mibEnum >= 3 && mibEnum <= 999) {
            int i = mibEnum - 3;
            names = i < STANDARD_CHARACTER_SETS.length ? STANDARD_CHARACTER_SETS[i] : null;
	    } else if (mibEnum >= 1000 && mibEnum <= 1999) {
            int i = mibEnum - 1000;
            names = i < UNICODE_CHARACTER_SETS.length ? UNICODE_CHARACTER_SETS[i] : null;
	    } else if (mibEnum >= 2000 && mibEnum <= 2999) {
            int i = mibEnum - 2000;
            names = i < VENDOR_CHARACTER_SETS.length ? VENDOR_CHARACTER_SETS[i] : null;
	    } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "mibEnum should be in the range [3..2999]",
                new BasicException.Parameter("mibEnum",mibEnum)
            );
	    }
	    if(names == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                "The given mibEnum is not recognized by the Charsets class",
                new BasicException.Parameter("maximum",VENDOR_CHARACTER_SETS.length-1),
                new BasicException.Parameter("mibEnum",mibEnum)
            );
	    } else {
	        for(String name : names) {
	            if(Charset.isSupported(name)) {
	                return names.length == 1 ? name : Charset.forName(name).name();
	            }
	        }
	        throw new ServiceException(
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.BAD_PARAMETER,
	            "The requested charset is not supported by this Java VM",
                new BasicException.Parameter("mibEnum",mibEnum),
                new BasicException.Parameter("name",(Object[])names)
	        );
	    }
	}

	/**
	 * Retrieve a charset's MIBEnum
	 * 
     * @param name the character set name
	 * @param namespace
	 * @param offset
	 * 
	 * @return the MIBenum or -1
	 */
	private static int toEnum(
        String name,
	    String[][] table,
	    int offset
	){
	    for(
	        int i = 0;
	        i < table.length;
	        i++
	    ){
	        String[] entry = table[i];
	        if(entry != null) {
	            for(String alias : entry) {
	                if(name.equals(alias)) {
	                    return i + offset;
	                }
	            }
	        }
	    }
	    return -1;
	}
	  
    /**
     * Retrieve a charset's MIBEnum
     * 
     * @param name the character set name
     * 
     * @return its MIBenum
     * 
     * @throws ServiceException if the given value is not supported
     */
	public static int toEnum(
        String name
	) throws ServiceException {
	    int i = toEnum(name, STANDARD_CHARACTER_SETS, 3);
        if(i < 0) i = toEnum(name, UNICODE_CHARACTER_SETS, 1000);
        if(i < 0) i = toEnum(name, VENDOR_CHARACTER_SETS, 2000);
        if(i < 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No MIBenum found for the given character set",
            new BasicException.Parameter("name", name)
        );
        return i;
	}
	    
	/**
	 * Standard Character Sets Range: [3 .. 999]
	 */
	private static final String[][] STANDARD_CHARACTER_SETS = {
	    {"US-ASCII"}, // MIBenum 3
	    {"ISO-8859-1"}, // MIBenum 4
        {"ISO-8859-2"}, // MIBenum 5
        {"ISO-8859-3"}, // MIBenum 6
        {"ISO-8859-4"}, // MIBenum 7
        {"ISO-8859-5"}, // MIBenum 8
        {"ISO-8859-6"}, // MIBenum 9
        {"ISO-8859-7"}, // MIBenum 10
        {"ISO-8859-8"}, // MIBenum 11
        {"ISO-8859-9"}, // MIBenum 12
        {"ISO-8859-10"}, // MIBenum 13
        {"ISO_6937-2-add","iso-ir-142","csISOTextComm"}, // MIBenum 14
        {"JIS_X0201"}, // MIBenum 15
        {"ISO-2022-JP"}, // MIBenum 16
        {"Shift_JIS"}, // MIBenum 17
        {"EUC-JP"}, // MIBenum 18
        {"EUC-JP"}, // MIBenum 19
        {"BS_4730","iso-ir-4","ISO646-GB","gb","uk","csISO4UnitedKingdom"}, // MIBenum 20
        {"SEN_850200_C", "iso-ir-11", "ISO646-SE2", "se2", "csISO11SwedishForNames"}, // MIBenum 21
        {"IT","iso-ir-15","ISO646-IT","csISO15Italian"}, // MIBenum 22
        {"ES","iso-ir-17","ISO646-ES","csISO17Spanish"}, // MIBenum 23
        {"DIN_66003","iso-ir-21","de","ISO646-DE","csISO21German"}, // MIBenum 24
        {"NS_4551-1","iso-ir-60","ISO646-NO","no","csISO60DanishNorwegian","csISO60DanishNorwegian"}, // MIBenum 25
        {"NF_Z_62-010","iso-ir-69","ISO646-FR","fr","csISO69French"}, // MIBenum 26
        {"ISO-10646-UTF-1","csISO10646UTF1"}, // MIBenum 27
        {"ISO_646.basic:1983","ref","csISO646basic1983"}, // MIBenum 28
        {"INVARIANT","csINVARIANT"}, // MIBenum 29
        {"ISO_646.irv:1983"}, // MIBenum 30
        {"NATS-SEFI","iso-ir-8-1","csNATSSEFI"}, // MIBenum 31
        {"NATS-SEFI-ADD","iso-ir-8-2","csNATSSEFIADD"}, // MIBenum 32
        {"NATS-DANO","iso-ir-9-1","csNATSDANO"}, // MIBenum 33
        {"NATS-DANO-ADD","iso-ir-9-2","csNATSDANOADD"}, // MIBenum 34
        {"SEN_850200_B","iso-ir-10","FI","ISO646-FI","ISO646-SE","se","csISO10Swedish"}, // MIBenum 35
        {"EUC-KR"}, // MIBenum 36
        {"ISO-2022-KR"}, // MIBenum 37
        {"EUC-KR"}, // MIBenum 38
        {"ISO-2022-JP"}, // MIBenum 39
        {"ISO-2022-JP-2"}, // MIBenum 40
        {"JIS_C6220-1969-jp","JIS_C6220-1969","iso-ir-13","katakana","x0201-7","csISO13JISC6220jp"}, // MIBenum 41
        {"JIS_C6220-1969-ro","iso-ir-14","jp","ISO646-JP","csISO14JISC6220ro"}, // MIBenum 42
        {"PT","iso-ir-16","ISO646-PT","csISO16Portuguese"}, // MIBenum 43
        {"greek7-old","iso-ir-18","csISO18Greek7Old"}, // MIBenum 44
        {"latin-greek","iso-ir-19","csISO19LatinGreek"}, // MIBenum 45
        {"iso-ir-25","ISO646-FR1","csISO25French"}, // MIBenum 46
        {"Latin-greek-1","iso-ir-27","csISO27LatinGreek1"}, // MIBenum 47
        {"ISO_5427","iso-ir-37","csISO5427Cyrillic"}, // MIBenum 48
        {"JIS_C6226-1978","iso-ir-42","csISO42JISC62261978"}, // MIBenum 49
        {"BS_viewdata","iso-ir-47","csISO47BSViewdata"}, // MIBenum 50
        {"INIS","iso-ir-49","csISO49INIS"}, // MIBenum 51
        {"INIS-8","iso-ir-50","csISO50INIS8"}, // MIBenum 52
        {"INIS-cyrillic","iso-ir-51","csISO51INISCyrillic"}, // MIBenum 53
        {"ISO_5427:1981","iso-ir-54","ISO5427Cyrillic1981"}, // MIBenum 54
        {"ISO_5428:1980","iso-ir-55","csISO5428Greek"}, // MIBenum 55
        {"GB_1988-80","iso-ir-57","cn","ISO646-CN","csISO57GB1988"}, // MIBenum 56
        {"GB_2312-80"}, // MIBenum 57
        {"NS_4551-2","ISO646-NO2","iso-ir-61","no2","csISO61Norwegian2"}, // MIBenum 58
        {"videotex-suppl","iso-ir-70","csISO70VideotexSupp1"}, // MIBenum 59
        {"PT2","iso-ir-84","ISO646-PT2","csISO84Portuguese2"}, // MIBenum 60
        {"ES2","iso-ir-85","ISO646-ES2","csISO85Spanish2"}, // MIBenum 61
        {"MSZ_7795.3","iso-ir-86","ISO646-HU","hu","csISO86Hungarian"}, // MIBenum 62
        {"x-JIS0208"}, // MIBenum 63
        {"greek7","iso-ir-88","csISO88Greek7"}, // MIBenum 64
        {"ASMO_449","ISO_9036","arabic7","iso-ir-89","csISO89ASMO449"}, // MIBenum 65
        {"iso-ir-90","csISO90"}, // MIBenum 66
        {"JIS_C6229-1984-a","iso-ir-91","jp-ocr-a","csISO91JISC62291984a"}, // MIBenum 67
        {"JIS_C6229-1984-b","iso-ir-92","ISO646-JP-OCR-B","jp-ocr-b","csISO92JISC62991984b"}, // MIBenum 68
        {"JIS_C6229-1984-b-add","iso-ir-93","jp-ocr-b-add","jp-ocr-b-add"}, // MIBenum 69
        {"JIS_C6229-1984-hand","iso-ir-94","iso-ir-94","iso-ir-94"}, // MIBenum 70
        {"JIS_C6229-1984-hand-add","iso-ir-95","iso-ir-95","iso-ir-95"}, // MIBenum 71
        {"JIS_C6229-1984-kana","iso-ir-96","csISO96JISC62291984kana"}, // MIBenum 72
        {"ISO_2033-1983","iso-ir-98","e13be13b","csISO2033"}, // MIBenum 73
        {"ANSI_X3.110-1983","iso-ir-99","CSA_T500-1983","NAPLPS","csISO99NAPLPS"}, // MIBenum 74
        {"T.61-7bit","iso-ir-102","csISO102T617bit"}, // MIBenum 75
        {"T.61-8bit","iso-ir-103","csISO103T618bit"}, // MIBenum 76
        {"ECMA-cyrillic","iso-ir-111","KOI8-E","csISO111ECMACyrillic"}, // MIBenum 77
        {"CSA_Z243.4-1985-1","iso-ir-121","ISO646-CA","csa7-1","ca","csISO121Canadian1"}, // MIBenum 78
        {"CSA_Z243.4-1985-2","iso-ir-122","ISO646-CA2","csa7-2","csISO122Canadian2"}, // MIBenum 79
        {"CSA_Z243.4-1985-gr","iso-ir-123","csISO123CSAZ24341985gr"}, // MIBenum 80
        {"ISO-8859-6-E"}, // MIBenum 81
        {"ISO-8859-6-I"}, // MIBenum 82
        {"T.101-G2","iso-ir-128","csISO128T101G2"}, // MIBenum 83
        {"ISO_8859-8-E"}, // MIBenum 84
        {"ISO_8859-8-I"}, // MIBenum 85
        {"CSN_369103","iso-ir-139","csISO139CSN369103"}, // MIBenum 86
        {"JUS_I.B1.002","iso-ir-141","ISO646-YU","js","yu","csISO141JUSIB1002"}, // MIBenum 87
        {"IEC_P27-1","iso-ir-143","csISO143IECP271"}, // MIBenum 88
        {"JUS_I.B1.003-serb","iso-ir-146","serbian","csISO146Serbian"}, // MIBenum 89
        {"JUS_I.B1.003-mac","macedonian","iso-ir-147","csISO147Macedonian"}, // MIBenum 90
        {"greek-ccitt","iso-ir-150","csISO150","csISO150GreekCCITT"}, // MIBenum 91
        {"NC_NC00-10:81","cuba","iso-ir-151","ISO646-CU","csISO151Cuba"}, // MIBenum 92
        {"ISO_6937-2-25","iso-ir-152","csISO6937Add"}, // MIBenum 93
        {"ST_SEV_358-88"}, // MIBenum 94
        {"ISO_8859-supp","iso-ir-154","latin1-2-5","csISO8859Supp"}, // MIBenum 95
        {"ISO_10367-box","iso-ir-155","csISO10367Box"}, // MIBenum 96
        {"latin-lap","lap","iso-ir-158","csISO158Lap"}, // MIBenum 97
        {"JIS_X0212-1990"}, // MIBenum 98
        {"DS_2089","DS2089","ISO646-DK","dk","csISO646Danish"}, // MIBenum 99
        {"us-dk","csUSDK"}, // MIBenum 100
        {"dk-us","csDKUS"}, // MIBenum 101
        {"KSC5636","ISO646-KR","csKSC5636"}, // MIBenum 102
        {"UNICODE-1-1-UTF-7","csUnicode11UTF7"}, // MIBenum 103
        {"ISO-2022-CN"}, // MIBenum 104
        {"ISO-2022-CN-EXT"}, // MIBenum 105
        {"UTF-8"}, // MIBenum 106
        null, // MIBenum 107
        null, // MIBenum 108
        {"ISO-8859-13"}, // MIBenum 109
        {"ISO-8859-14"}, // MIBenum 110
        {"ISO-8859-15"}, // MIBenum 111
        {"ISO-8859-16","iso-ir-226","ISO_8859-16:2001","ISO_8859-16","ISO_8859-16","l10"}, // MIBenum 112
        {"GBK"}, // MIBenum 113
        {"GB18030"}, // MIBenum 114
        {"OSD_EBCDIC_DF04_15"}, // MIBenum 115
        {"OSD_EBCDIC_DF03_IRV"}, // MIBenum 116
        {"OSD_EBCDIC_DF04_1"}, // MIBenum 117
        {"ISO-11548-1","ISO_11548-1","ISO_TR_11548-1","csISO115481"}, // MIBenum 118
        {"KZ-1048","STRK1048-2002","RK1048","csKZ1048"} // MIBenum 119
	};
	
    /**
     * Standard Character Sets Range: [1000 .. 1999]
     */
    private static final String[][] UNICODE_CHARACTER_SETS = {
        {"ISO-10646-UCS-2"}, // MIBenum 1000 
        {"ISO-10646-UCS-4"}, // MIBenum 1001 
        {"ISO-10646-UCS-Basic","csUnicodeASCII"}, // MIBenum 1002
        {"ISO-10646-Unicode-Latin1","csUnicodeLatin1","ISO-10646"}, // MIBenum 1003
        {"ISO-10646-J-1"}, // MIBenum 1004
        {"ISO-Unicode-IBM-1261","csUnicodeIBM1261"}, // MIBenum 1005 
        {"ISO-Unicode-IBM-1268","csUnicodeIBM1268"}, // MIBenum 1006
        {"ISO-Unicode-IBM-1276","csUnicodeIBM1276"}, // MIBenum 1007 
        {"ISO-Unicode-IBM-1264","csUnicodeIBM1264"}, // MIBenum 1008
        {"ISO-Unicode-IBM-1265","csUnicodeIBM1265"}, // MIBenum 1009 
        {"UNICODE-1-1","csUnicode11"}, // MIBenum 1010
        {"SCSU"}, // MIBenum 1011 
        {"UTF-7"}, // MIBenum 1012
        {"UTF-16BE"}, // MIBenum 1013
        {"UTF-16LE"}, // MIBenum 1014
        {"UTF-16"}, // MIBenum 1015
        {"CESU-8"}, // MIBenum 1016
        {"UTF-32"}, // MIBenum 1017
        {"UTF-32BE"}, // MIBenum 1018
        {"UTF-32LE"}, // MIBenum 1019
        {"BOCU-1"} // MIBenum 1020
    };

    /**
     * Vendor Character Sets Range: [2000 .. 2999]
     */
    private static final String[][] VENDOR_CHARACTER_SETS = {
        {"ISO-8859-1-Windows-3.0-Latin-1","csWindows30Latin1"}, // MIBenum 2000 
        {"ISO-8859-1-Windows-3.1-Latin-1","csWindows31Latin1"}, // MIBenum 2001 
        {"ISO-8859-2-Windows-Latin-2","csWindows31Latin2"}, // MIBenum 2002
        {"ISO-8859-9-Windows-Latin-5","csWindows31Latin5"}, // MIBenum 2003
        {"hp-roman8"}, // MIBenum 2004
        {"Adobe-Standard-Encoding"} // MIBenum 2005
        // TODO add more vendor codes if necessary
    };
    
}
