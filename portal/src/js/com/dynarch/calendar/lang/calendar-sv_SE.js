// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Söndag",
 "Måndag",
 "Tisdag",
 "Onsdag",
 "Torsdag",
 "Fredag",
 "Lördag",
 "Söndag");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 0;

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("Sön",
 "Mån",
 "Tis",
 "Ons",
 "Tor",
 "Fre",
 "Lör",
 "Sön");

// full month names
Calendar._MN = new Array
("Januari",
 "Februari",
 "Mars",
 "April",
 "Maj",
 "Juni",
 "Juli",
 "Augusti",
 "September",
 "Oktober",
 "November",
 "December");

// short month names
Calendar._SMN = new Array
("Jan",
 "Feb",
 "Mar",
 "Apr",
 "Maj",
 "Jun",
 "Jul",
 "Aug",
 "Sep",
 "Okt",
 "Nov",
 "Dec");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Om kalendern";

Calendar._TT["ABOUT"] =
"DHTML kalender som låter dig välja datum och tid\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Ladda ner senaste versionen på http://dynarch.com/mishoo/calendar.epl\n" +
"Distribueras under GNU LGPL.  För detaljer se http://gnu.org/licenses/lgpl.html." +
"\n\n" +
"För att välja datum:\n" +
"- Använd \u00ab, \u00bb knapparna för att välja år\n" +
"- Använd \u2039, \u203a knapparna för att välja månad\n" +
"- Håll musknappen intryckt på någon av ovanstående knappar för snabbare bläddring.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"För att välja tid:\n" +
"- Klicka på timvärdet respektive minutvärdet för att öka det.\n" +
"- eller Shift-klicka för att minska värdena\n" +
"- eller klicka och dra musen för snabbare bläddring.";

Calendar._TT["PREV_YEAR"] = "Föreg. år (håll intryckt för meny)";
Calendar._TT["PREV_MONTH"] = "Prev. månad (håll intryckt för meny)";
Calendar._TT["GO_TODAY"] = "Gå till Idag";
Calendar._TT["NEXT_MONTH"] = "Nästa månad (håll intryckt för meny)";
Calendar._TT["NEXT_YEAR"] = "Next år (håll intryckt för meny)";
Calendar._TT["SEL_DATE"] = "Välj datum";
Calendar._TT["DRAG_TO_MOVE"] = "Drag för att flytta";
Calendar._TT["PART_TODAY"] = " (idag)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Visa %s först";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Stäng";
Calendar._TT["TODAY"] = "Idag";
Calendar._TT["TIME_PART"] = "(Shift-)Klicka eller drag för att ändra värde";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "v.";
Calendar._TT["TIME"] = "Tid:";
