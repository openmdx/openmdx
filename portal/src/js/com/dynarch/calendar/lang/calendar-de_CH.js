// ** I18N

// Calendar DE language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Sonntag",
 "Montag",
 "Dienstag",
 "Mittwoch",
 "Donnerstag",
 "Freitag",
 "Samstag",
 "Sonntag");

// short day names only use 2 letters instead of 3
Calendar._SDN_len = 2;

// short day names
Calendar._SDN = new Array
("So",
 "Mo",
 "Di",
 "Mi",
 "Do",
 "Fr",
 "Sa",
 "So");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 0;


// full month names
Calendar._MN = new Array
("Januar",
 "Februar",
 "März",
 "April",
 "Mai",
 "Juni",
 "Juli",
 "August",
 "September",
 "Oktober",
 "November",
 "Dezember");

// short month names
Calendar._SMN = new Array
("Jan",
 "Feb",
 "Mär",
 "Apr",
 "Mai",
 "Jun",
 "Jul",
 "Aug",
 "Sep",
 "Okt",
 "Nov",
 "Dez");

// tooltips
Calendar._TT = {};

Calendar._TT["ABOUT"] =
"DHTML Datum/Zeit Selector\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Donwload neueste Version: http://dynarch.com/mishoo/calendar.epl\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"Datumsauswahl:\n" +
"- Jahr ausw\u00e4hlen mit \u00ab und \u00bb\n" +
"- Monat ausw\u00e4hlen mit \u2039 und \u203a\n" +
"- Fr Auswahl aus Liste Maustaste gedr\u00fcckt halten.";

Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Zeit w\u00e4hlen:\n" +
"- Stunde/Minute weiter mit Mausklick\n" +
"- Stunde/Minute zurck mit Shift-Mausklick\n" +
"- oder f\u00fcr schnellere Auswahl nach links oder rechts ziehen.";


Calendar._TT["TOGGLE"] = "Ersten Tag der Woche auswählen";
Calendar._TT["PREV_YEAR"] = "Jahr zurück (halten für Menü)";
Calendar._TT["PREV_MONTH"] = "Monat zurück (halten für Menü)";
Calendar._TT["GO_TODAY"] = "Zum heutigen Datum";
Calendar._TT["NEXT_MONTH"] = "Monat vorwärts (halten für Menü";
Calendar._TT["NEXT_YEAR"] = "Jahr vorwärts (halten für Menü";
Calendar._TT["SEL_DATE"] = "Datum auswählen";
Calendar._TT["DRAG_TO_MOVE"] = "Klicken und halten zum Verschieben";
Calendar._TT["PART_TODAY"] = " (heute)";
Calendar._TT["MON_FIRST"] = "Woche beginnt mit Montag";
Calendar._TT["SUN_FIRST"] = "Woche beginnt mit Sonntag";
Calendar._TT["CLOSE"] = "Schliessen";
Calendar._TT["TODAY"] = "Heute";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d-%m-%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "KW";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Zeige %s zuerst";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";
