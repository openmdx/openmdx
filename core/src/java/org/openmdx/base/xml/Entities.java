/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Entities 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.xml;

import java.util.Arrays;

/**
 * Entities
 */
public class Entities {
    
    /**
     * Constructor 
     */
    private Entities() {
        // Avoid instantiation
    }

    /**
     * XML Entities
     */
    private static final String[] NAMES = {
        "Aacute", //   Latin capital letter A with acute
        "aacute", //   Latin small letter a with acute
        "Acirc", //   Latin capital letter A with circumflex
        "acirc", //   Latin small letter a with circumflex
        "acute", //   acute accent (= spacing acute)
        "AElig", //   Latin capital letter AE (= Latin capital ligature AE)
        "aelig", //   Latin small letter ae (= Latin small ligature ae)
        "Agrave", //   Latin capital letter A with grave (= Latin capital letter A grave)
        "agrave", //   Latin small letter a with grave
        "alefsym", //   alef symbol (= first transfinite cardinal)[13]
        "Alpha", //   Greek capital letter Alpha
        "alpha", //   Greek small letter alpha
        "amp", //   ampersand
        "and", //   logical and (= wedge)
        "ang", //   angle
        "apos", //   apostrophe (= apostrophe-quote); see below
        "Aring", //   Latin capital letter A with ring above (= Latin capital letter A ring)
        "aring", //   Latin small letter a with ring above
        "asymp", //   almost equal to (= asymptotic to)
        "Atilde", //   Latin capital letter A with tilde
        "atilde", //   Latin small letter a with tilde
        "Auml", //   Latin capital letter A with diaeresis
        "auml", //   Latin small letter a with diaeresis
        "bdquo", //   double low-9 quotation mark
        "Beta", //   Greek capital letter Beta
        "beta", //   Greek small letter beta
        "brvbar", //   broken bar (= broken vertical bar)
        "bull", //   bullet (= black small circle)[10]
        "cap", //   intersection (= cap)
        "Ccedil", //   Latin capital letter C with cedilla
        "ccedil", //   Latin small letter c with cedilla
        "cedil", //   cedilla (= spacing cedilla)
        "cent", //   cent sign
        "Chi", //   Greek capital letter Chi
        "chi", //   Greek small letter chi
        "circ", //   modifier letter circumflex accent
        "clubs", //   black club suit (= shamrock)[25]
        "cong", //   congruent to
        "copy", //   copyright sign
        "crarr", //   downwards arrow with corner leftwards (= carriage return)
        "cup", //   union (= cup)
        "curren", //   currency sign
        "dagger", //   dagger
        "Dagger", //   double dagger
        "darr", //   downwards arrow
        "dArr", //   downwards double arrow
        "deg", //   degree sign
        "Delta", //   Greek capital letter Delta
        "delta", //   Greek small letter delta
        "diams", //   black diamond suit[27]
        "divide", //   division sign
        "Eacute", //   Latin capital letter E with acute
        "eacute", //   Latin small letter e with acute
        "Ecirc", //   Latin capital letter E with circumflex
        "ecirc", //   Latin small letter e with circumflex
        "Egrave", //   Latin capital letter E with grave
        "egrave", //   Latin small letter e with grave
        "empty", //   empty set (= null set = diameter)
        "emsp", //   em space[8]
        "ensp", //   en space[7]
        "Epsilon", //   Greek capital letter Epsilon
        "epsilon", //   Greek small letter epsilon
        "equiv", //   identical to; sometimes used for 'equivalent to'
        "Eta", //   Greek capital letter Eta
        "eta", //   Greek small letter eta
        "ETH", //   Latin capital letter ETH
        "eth", //   Latin small letter eth
        "Euml", //   Latin capital letter E with diaeresis
        "euml", //   Latin small letter e with diaeresis
        "euro", //   euro sign
        "exist", //   there exists
        "fnof", //   Latin small letter f with hook (= function = florin)
        "forall", //   for all
        "frac12", //   vulgar fraction one half (= fraction one half)
        "frac14", //   vulgar fraction one quarter (= fraction one quarter)
        "frac34", //   vulgar fraction three quarters (= fraction three quarters)
        "frasl", //   fraction slash (= solidus)
        "Gamma", //   Greek capital letter Gamma
        "gamma", //   Greek small letter gamma
        "ge", //   greater-than or equal to
        "gt", //   greater-than sign
        "harr", //   left right arrow
        "hArr", //   left right double arrow
        "hearts", //   black heart suit (= valentine)[26]
        "hellip", //   horizontal ellipsis (= three dot leader)
        "Iacute", //   Latin capital letter I with acute
        "iacute", //   Latin small letter i with acute
        "Icirc", //   Latin capital letter I with circumflex
        "icirc", //   Latin small letter i with circumflex
        "iexcl", //   inverted exclamation mark
        "Igrave", //   Latin capital letter I with grave
        "igrave", //   Latin small letter i with grave
        "image", //   black-letter capital I (= imaginary part)
        "infin", //   infinity
        "int", //   integral
        "Iota", //   Greek capital letter Iota
        "iota", //   Greek small letter iota
        "iquest", //   inverted question mark (= turned question mark)
        "isin", //   element of
        "Iuml", //   Latin capital letter I with diaeresis
        "iuml", //   Latin small letter i with diaeresis
        "Kappa", //   Greek capital letter Kappa
        "kappa", //   Greek small letter kappa
        "Lambda", //   Greek capital letter Lambda
        "lambda", //   Greek small letter lambda
        "lang", //   left-pointing angle bracket (= bra)[22]
        "laquo", //   left-pointing double angle quotation mark (= left pointing guillemet)
        "larr", //   leftwards arrow
        "lArr", //   leftwards double arrow[14]
        "lceil", //   left ceiling (= APL upstile)
        "ldquo", //   left double quotation mark
        "le", //   less-than or equal to
        "lfloor", //   left floor (= APL downstile)
        "lowast", //   asterisk operator
        "loz", //   lozenge
        "lrm", //   left-to-right mark
        "lsaquo", //   single left-pointing angle quotation mark[11]
        "lsquo", //   left single quotation mark
        "lt", //   less-than sign
        "macr", //   macron (= spacing macron = overline = APL overbar)
        "mdash", //   em dash
        "micro", //   micro sign
        "middot", //   middle dot (= Georgian comma = Greek middle dot)
        "minus", //   minus sign
        "Mu", //   Greek capital letter Mu
        "mu", //   Greek small letter mu
        "nabla", //   nabla (= backward difference)
        "nbsp", //   no-break space (= non-breaking space)[4]
        "ndash", //   en dash
        "ne", //   not equal to
        "ni", //   contains as member
        "not", //   not sign
        "notin", //   not an element of
        "nsub", //   not a subset of
        "Ntilde", //   Latin capital letter N with tilde
        "ntilde", //   Latin small letter n with tilde
        "Nu", //   Greek capital letter Nu
        "nu", //   Greek small letter nu
        "Oacute", //   Latin capital letter O with acute
        "oacute", //   Latin small letter o with acute
        "Ocirc", //   Latin capital letter O with circumflex
        "ocirc", //   Latin small letter o with circumflex
        "OElig", //   Latin capital ligature oe[5]
        "oelig", //   Latin small ligature oe[6]
        "Ograve", //   Latin capital letter O with grave
        "ograve", //   Latin small letter o with grave
        "oline", //   overline (= spacing overscore)
        "Omega", //   Greek capital letter Omega
        "omega", //   Greek small letter omega
        "Omicron", //   Greek capital letter Omicron
        "omicron", //   Greek small letter omicron
        "oplus", //   circled plus (= direct sum)
        "or", //   logical or (= vee)
        "ordf", //   feminine ordinal indicator
        "ordm", //   masculine ordinal indicator
        "Oslash", //   Latin capital letter O with stroke (= Latin capital letter O slash)
        "oslash", //   Latin small letter o with stroke (= Latin small letter o slash)
        "Otilde", //   Latin capital letter O with tilde
        "otilde", //   Latin small letter o with tilde
        "otimes", //   circled times (= vector product)
        "Ouml", //   Latin capital letter O with diaeresis
        "ouml", //   Latin small letter o with diaeresis
        "para", //   pilcrow sign ( = paragraph sign)
        "part", //   partial differential
        "permil", //   per mille sign
        "perp", //   up tack (= orthogonal to = perpendicular)[20]
        "Phi", //   Greek capital letter Phi
        "phi", //   Greek small letter phi
        "Pi", //   Greek capital letter Pi
        "pi", //   Greek small letter pi
        "piv", //   Greek pi symbol
        "plusmn", //   plus-minus sign (= plus-or-minus sign)
        "pound", //   pound sign
        "prime", //   prime (= minutes = feet)
        "Prime", //   double prime (= seconds = inches)
        "prod", //   n-ary product (= product sign)[16]
        "prop", //   proportional to
        "Psi", //   Greek capital letter Psi
        "psi", //   Greek small letter psi
        "quot", //   quotation mark (= APL quote)
        "radic", //   square root (= radical sign)
        "rang", //   right-pointing angle bracket (= ket)[23]
        "raquo", //   right-pointing double angle quotation mark (= right pointing guillemet)
        "rarr", //   rightwards arrow
        "rArr", //   rightwards double arrow[15]
        "rceil", //   right ceiling
        "rdquo", //   right double quotation mark
        "real", //   black-letter capital R (= real part symbol)
        "reg", //   registered sign ( = registered trade mark sign)
        "rfloor", //   right floor
        "Rho", //   Greek capital letter Rho
        "rho", //   Greek small letter rho
        "rlm", //   right-to-left mark
        "rsaquo", //   single right-pointing angle quotation mark[12]
        "rsquo", //   right single quotation mark
        "sbquo", //   single low-9 quotation mark
        "Scaron", //   Latin capital letter s with caron
        "scaron", //   Latin small letter s with caron
        "sdot", //   dot operator[21]
        "sect", //   section sign
        "shy", //   soft hyphen (= discretionary hyphen)
        "Sigma", //   Greek capital letter Sigma
        "sigma", //   Greek small letter sigma
        "sigmaf", //   Greek small letter final sigma
        "sim", //   tilde operator (= varies with = similar to)[18]
        "spades", //   black spade suit[24]
        "sub", //   subset of
        "sube", //   subset of or equal to
        "sum", //   n-ary summation[17]
        "sup", //   superset of[19]
        "sup1", //   superscript one (= superscript digit one)
        "sup2", //   superscript two (= superscript digit two = squared)
        "sup3", //   superscript three (= superscript digit three = cubed)
        "supe", //   superset of or equal to
        "szlig", //   Latin small letter sharp s (= ess-zed); see German Eszett
        "Tau", //   Greek capital letter Tau
        "tau", //   Greek small letter tau
        "there4", //   therefore
        "Theta", //   Greek capital letter Theta
        "theta", //   Greek small letter theta
        "thetasym", //   Greek theta symbol
        "thinsp", //   thin space[9]
        "THORN", //   Latin capital letter THORN
        "thorn", //   Latin small letter thorn
        "tilde", //   small tilde
        "times", //   multiplication sign
        "trade", //   trademark sign
        "Uacute", //   Latin capital letter U with acute
        "uacute", //   Latin small letter u with acute
        "uarr", //   upwards arrow
        "uArr", //   upwards double arrow
        "Ucirc", //   Latin capital letter U with circumflex
        "ucirc", //   Latin small letter u with circumflex
        "Ugrave", //   Latin capital letter U with grave
        "ugrave", //   Latin small letter u with grave
        "uml", //   diaeresis (= spacing diaeresis); see German umlaut
        "upsih", //   Greek Upsilon with hook symbol
        "Upsilon", //   Greek capital letter Upsilon
        "upsilon", //   Greek small letter upsilon
        "Uuml", //   Latin capital letter U with diaeresis
        "uuml", //   Latin small letter u with diaeresis
        "weierp", //   script capital P (= power set = Weierstrass p)
        "Xi", //   Greek capital letter Xi
        "xi", //   Greek small letter xi
        "Yacute", //   Latin capital letter Y with acute
        "yacute", //   Latin small letter y with acute
        "yen", //   yen sign (= yuan sign)
        "yuml", //   Latin small letter y with diaeresis
        "Yuml", //   Latin capital letter y with diaeresis
        "Zeta", //   Greek capital letter Zeta
        "zeta", //   Greek small letter zeta
        "zwj", //   zero-width joiner
        "zwnj" //   zero-width non-joiner
    };
    
    /**
     * The XML Entities character representation
     */
    private static char[] VALUES = {
        '\u00C1', //    Latin capital letter A with acute
        '\u00E1', //    Latin small letter a with acute
        '\u00C2', //    Latin capital letter A with circumflex
        '\u00E2', //    Latin small letter a with circumflex
        '\u00B4', //    acute accent (= spacing acute)
        '\u00C6', //    Latin capital letter AE (= Latin capital ligature AE)
        '\u00E6', //    Latin small letter ae (= Latin small ligature ae)
        '\u00C0', //    Latin capital letter A with grave (= Latin capital letter A grave)
        '\u00E0', //    Latin small letter a with grave
        '\u2135', //   alef symbol (= first transfinite cardinal)[13]
        '\u0391', //    Greek capital letter Alpha
        '\u03B1', //    Greek small letter alpha
        '\u0026', // ampersand
        '\u2227', //   logical and (= wedge)
        '\u2220', //   angle
        '\'', // apostrophe (= apostrophe-quote); see below
        '\u00C5', //    Latin capital letter A with ring above (= Latin capital letter A ring)
        '\u00E5', //    Latin small letter a with ring above
        '\u2248', //   almost equal to (= asymptotic to)
        '\u00C3', //    Latin capital letter A with tilde
        '\u00E3', //    Latin small letter a with tilde
        '\u00C4', //    Latin capital letter A with diaeresis
        '\u00E4', //    Latin small letter a with diaeresis
        '\u201E', //   double low-9 quotation mark
        '\u0392', //    Greek capital letter Beta
        '\u03B2', //    Greek small letter beta
        '\u00A6', //    broken bar (= broken vertical bar)
        '\u2022', //   bullet (= black small circle)[10]
        '\u2229', //   intersection (= cap)
        '\u00C7', //    Latin capital letter C with cedilla
        '\u00E7', //    Latin small letter c with cedilla
        '\u00B8', //    cedilla (= spacing cedilla)
        '\u00A2', //    cent sign
        '\u03A7', //    Greek capital letter Chi
        '\u03C7', //    Greek small letter chi
        '\u02C6', //    modifier letter circumflex accent
        '\u2663', //   black club suit (= shamrock)[25]
        '\u2245', //   congruent to
        '\u00A9', //    copyright sign
        '\u21B5', //   downwards arrow with corner leftwards (= carriage return)
        '\u222A', //   union (= cup)
        '\u00A4', //    currency sign
        '\u2020', //   dagger
        '\u2021', //   double dagger
        '\u2193', //   downwards arrow
        '\u21D3', //   downwards double arrow
        '\u00B0', //    degree sign
        '\u0394', //    Greek capital letter Delta
        '\u03B4', //    Greek small letter delta
        '\u2666', //   black diamond suit[27]
        '\u00F7', //    division sign
        '\u00C9', //    Latin capital letter E with acute
        '\u00E9', //    Latin small letter e with acute
        '\u00CA', //    Latin capital letter E with circumflex
        '\u00EA', //    Latin small letter e with circumflex
        '\u00C8', //    Latin capital letter E with grave
        '\u00E8', //    Latin small letter e with grave
        '\u2205', //   empty set (= null set = diameter)
        '\u2003', //   em space[8]
        '\u2002', //   en space[7]
        '\u0395', //    Greek capital letter Epsilon
        '\u03B5', //    Greek small letter epsilon
        '\u2261', //   identical to; sometimes used for 'equivalent to'
        '\u0397', //    Greek capital letter Eta
        '\u03B7', //    Greek small letter eta
        '\u00D0', //    Latin capital letter ETH
        '\u00F0', //    Latin small letter eth
        '\u00CB', //    Latin capital letter E with diaeresis
        '\u00EB', //    Latin small letter e with diaeresis
        '\u20AC', //   euro sign
        '\u2203', //   there exists
        '\u0192', //    Latin small letter f with hook (= function = florin)
        '\u2200', //   for all
        '\u00BD', //    vulgar fraction one half (= fraction one half)
        '\u00BC', //    vulgar fraction one quarter (= fraction one quarter)
        '\u00BE', //    vulgar fraction three quarters (= fraction three quarters)
        '\u2044', //   fraction slash (= solidus)
        '\u0393', //    Greek capital letter Gamma
        '\u03B3', //    Greek small letter gamma
        '\u2265', //   greater-than or equal to
        '\u003E', // greater-than sign
        '\u2194', //   left right arrow
        '\u21D4', //   left right double arrow
        '\u2665', //   black heart suit (= valentine)[26]
        '\u2026', //   horizontal ellipsis (= three dot leader)
        '\u00CD', //    Latin capital letter I with acute
        '\u00ED', //    Latin small letter i with acute
        '\u00CE', //    Latin capital letter I with circumflex
        '\u00EE', //    Latin small letter i with circumflex
        '\u00A1', //    inverted exclamation mark
        '\u00CC', //    Latin capital letter I with grave
        '\u00EC', //    Latin small letter i with grave
        '\u2111', //   black-letter capital I (= imaginary part)
        '\u221E', //   infinity
        '\u222B', //   integral
        '\u0399', //    Greek capital letter Iota
        '\u03B9', //    Greek small letter iota
        '\u00BF', //    inverted question mark (= turned question mark)
        '\u2208', //   element of
        '\u00CF', //    Latin capital letter I with diaeresis
        '\u00EF', //    Latin small letter i with diaeresis
        '\u039A', //    Greek capital letter Kappa
        '\u03BA', //    Greek small letter kappa
        '\u039B', //    Greek capital letter Lambda
        '\u03BB', //    Greek small letter lambda
        '\u2329', //   left-pointing angle bracket (= bra)[22]
        '\u00AB', //    left-pointing double angle quotation mark (= left pointing guillemet)
        '\u2190', //   leftwards arrow
        '\u21D0', //   leftwards double arrow[14]
        '\u2308', //   left ceiling (= APL upstile)
        '\u201C', //   left double quotation mark
        '\u2264', //   less-than or equal to
        '\u230A', //   left floor (= APL downstile)
        '\u2217', //   asterisk operator
        '\u25CA', //   lozenge
        '\u200E', //   left-to-right mark
        '\u2039', //   single left-pointing angle quotation mark[11]
        '\u2018', //   left single quotation mark
        '\u003C', // less-than sign
        '\u00AF', //    macron (= spacing macron = overline = APL overbar)
        '\u2014', //   em dash
        '\u00B5', //    micro sign
        '\u00B7', //    middle dot (= Georgian comma = Greek middle dot)
        '\u2212', //   minus sign
        '\u039C', //    Greek capital letter Mu
        '\u03BC', //    Greek small letter mu
        '\u2207', //   nabla (= backward difference)
        '\u00A0', //    no-break space (= non-breaking space)[4]
        '\u2013', //   en dash
        '\u2260', //   not equal to
        '\u220B', //   contains as member
        '\u00AC', //    not sign
        '\u2209', //   not an element of
        '\u2284', //   not a subset of
        '\u00D1', //    Latin capital letter N with tilde
        '\u00F1', //    Latin small letter n with tilde
        '\u039D', //    Greek capital letter Nu
        '\u03BD', //    Greek small letter nu
        '\u00D3', //    Latin capital letter O with acute
        '\u00F3', //    Latin small letter o with acute
        '\u00D4', //    Latin capital letter O with circumflex
        '\u00F4', //    Latin small letter o with circumflex
        '\u0152', //    Latin capital ligature oe[5]
        '\u0153', //    Latin small ligature oe[6]
        '\u00D2', //    Latin capital letter O with grave
        '\u00F2', //    Latin small letter o with grave
        '\u203E', //   overline (= spacing overscore)
        '\u03A9', //    Greek capital letter Omega
        '\u03C9', //    Greek small letter omega
        '\u039F', //    Greek capital letter Omicron
        '\u03BF', //    Greek small letter omicron
        '\u2295', //   circled plus (= direct sum)
        '\u2228', //   logical or (= vee)
        '\u00AA', //    feminine ordinal indicator
        '\u00BA', //    masculine ordinal indicator
        '\u00D8', //    Latin capital letter O with stroke (= Latin capital letter O slash)
        '\u00F8', //    Latin small letter o with stroke (= Latin small letter o slash)
        '\u00D5', //    Latin capital letter O with tilde
        '\u00F5', //    Latin small letter o with tilde
        '\u2297', //   circled times (= vector product)
        '\u00D6', //    Latin capital letter O with diaeresis
        '\u00F6', //    Latin small letter o with diaeresis
        '\u00B6', //    pilcrow sign ( = paragraph sign)
        '\u2202', //   partial differential
        '\u2030', //   per mille sign
        '\u22A5', //   up tack (= orthogonal to = perpendicular)[20]
        '\u03A6', //    Greek capital letter Phi
        '\u03C6', //    Greek small letter phi
        '\u03A0', //    Greek capital letter Pi
        '\u03C0', //    Greek small letter pi
        '\u03D6', //    Greek pi symbol
        '\u00B1', //    plus-minus sign (= plus-or-minus sign)
        '\u00A3', //    pound sign
        '\u2032', //   prime (= minutes = feet)
        '\u2033', //   double prime (= seconds = inches)
        '\u220F', //   n-ary product (= product sign)[16]
        '\u221D', //   proportional to
        '\u03A8', //    Greek capital letter Psi
        '\u03C8', //    Greek small letter psi
        '\u0022', // quotation mark (= APL quote)
        '\u221A', //   square root (= radical sign)
        '\u232A', //   right-pointing angle bracket (= ket)[23]
        '\u00BB', //    right-pointing double angle quotation mark (= right pointing guillemet)
        '\u2192', //   rightwards arrow
        '\u21D2', //   rightwards double arrow[15]
        '\u2309', //   right ceiling
        '\u201D', //   right double quotation mark
        '\u211C', //   black-letter capital R (= real part symbol)
        '\u00AE', //    registered sign ( = registered trade mark sign)
        '\u230B', //   right floor
        '\u03A1', //    Greek capital letter Rho
        '\u03C1', //    Greek small letter rho
        '\u200F', //   right-to-left mark
        '\u203A', //   single right-pointing angle quotation mark[12]
        '\u2019', //   right single quotation mark
        '\u201A', //   single low-9 quotation mark
        '\u0160', //    Latin capital letter s with caron
        '\u0161', //    Latin small letter s with caron
        '\u22C5', //   dot operator[21]
        '\u00A7', //    section sign
        '\u00AD', //    soft hyphen (= discretionary hyphen)
        '\u03A3', //    Greek capital letter Sigma
        '\u03C3', //    Greek small letter sigma
        '\u03C2', //    Greek small letter final sigma
        '\u223C', //   tilde operator (= varies with = similar to)[18]
        '\u2660', //   black spade suit[24]
        '\u2282', //   subset of
        '\u2286', //   subset of or equal to
        '\u2211', //   n-ary summation[17]
        '\u2283', //   superset of[19]
        '\u00B9', //    superscript one (= superscript digit one)
        '\u00B2', //    superscript two (= superscript digit two = squared)
        '\u00B3', //    superscript three (= superscript digit three = cubed)
        '\u2287', //   superset of or equal to
        '\u00DF', //    Latin small letter sharp s (= ess-zed); see German Eszett
        '\u03A4', //    Greek capital letter Tau
        '\u03C4', //    Greek small letter tau
        '\u2234', //   therefore
        '\u0398', //    Greek capital letter Theta
        '\u03B8', //    Greek small letter theta
        '\u03D1', //    Greek theta symbol
        '\u2009', //   thin space[9]
        '\u00DE', //    Latin capital letter THORN
        '\u00FE', //    Latin small letter thorn
        '\u02DC', //    small tilde
        '\u00D7', //    multiplication sign
        '\u2122', //   trademark sign
        '\u00DA', //    Latin capital letter U with acute
        '\u00FA', //    Latin small letter u with acute
        '\u2191', //   upwards arrow
        '\u21D1', //   upwards double arrow
        '\u00DB', //    Latin capital letter U with circumflex
        '\u00FB', //    Latin small letter u with circumflex
        '\u00D9', //    Latin capital letter U with grave
        '\u00F9', //    Latin small letter u with grave
        '\u00A8', //    diaeresis (= spacing diaeresis); see German umlaut
        '\u03D2', //    Greek Upsilon with hook symbol
        '\u03A5', //    Greek capital letter Upsilon
        '\u03C5', //    Greek small letter upsilon
        '\u00DC', //    Latin capital letter U with diaeresis
        '\u00FC', //    Latin small letter u with diaeresis
        '\u2118', //   script capital P (= power set = Weierstrass p)
        '\u039E', //    Greek capital letter Xi
        '\u03BE', //    Greek small letter xi
        '\u00DD', //    Latin capital letter Y with acute
        '\u00FD', //    Latin small letter y with acute
        '\u00A5', //    yen sign (= yuan sign)
        '\u00FF', //    Latin small letter y with diaeresis
        '\u0178', //    Latin capital letter y with diaeresis
        '\u0396', //    Greek capital letter Zeta
        '\u03B6', //    Greek small letter zeta
        '\u200D', //   zero-width joiner
        '\u200C', //   zero-width non-joiner
    };

    /**
     * Retrieve the named character
     * 
     * @param name the entity name
     * 
     * @return the entity value
     */
    public static char valueOf(
        String name
    ){
        //
        // Null values
        //
        if(name == null) return '\u0000';
        //
        // Numeric entity references
        //
        if(name.startsWith("#")) try {
            return (char) (name.startsWith("#x") ?
                Integer.parseInt(name.substring(2), 16) :
                Integer.parseInt(name.substring(1))
            );
        } catch (NumberFormatException exception) {
            return '\u0000';
        }
        //
        // Named entity references
        //
        int index = Arrays.binarySearch(NAMES, name);
        return index < 0 ? '\u0000' : VALUES[index];
    }
    
}
