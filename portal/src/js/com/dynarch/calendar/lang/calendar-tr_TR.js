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
("Pazar",
 "Pazartesi",
 "Salı",
 "Çarşamba",
 "Perşembe",
 "Cuma",
 "Cumartesi",
 "Pazar");

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
("Pzr",
 "Pte",
 "Sal",
 "Çrş",
 "Prş",
 "Cum",
 "Cte",
 "Pzr");

// full month names
Calendar._MN = new Array
("Ocak",
 "Şubat",
 "Mart",
 "Nisan",
 "Mayıs",
 "Haziran",
 "Temmuz",
 "Ağustos",
 "Eylül",
 "Ekim",
 "Kasım",
 "Aralık");

// short month names
Calendar._SMN = new Array
("Ock",
 "Şbt",
 "Mar",
 "Nis",
 "May",
 "Hzn",
 "Tem",
 "Ağu",
 "Eyl",
 "Ekm",
 "Ksm",
 "Ara");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Takvim Hakkında";

Calendar._TT["ABOUT"] =
"DHTML Tarih/Zaman Seçicisi\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Yeni sürümler için : http://dynarch.com/mishoo/calendar.epl\n" +
"GNU LGPL lisansı altında dağıtılmaktadır. Ayrıntılar için http://gnu.org/licenses/lgpl.html sayfasına bakınız." +
"\n\n" +
"Tarih seçimi:\n" +
"- \u00ab, \u00bb düğmelerini yıl seçmek için kullanınız\n" +
"- \u2039, \u203a düğmelerini ay seçmek için kullanınız\n" +
"- Fare tuşunu yukarıdaki düğmeler üzerinde basılı tutarak hızlı seçim yapabilirsiniz";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Zaman Seçicisi:\n" +
"- Zaman parçaları üzerine tıklayarak zamanı arttırabilir\n" +
"- veya  Maj-tıkla ile onları azaltabilir\n" +
"- veya sürükle bırak ile hızlı seçim yapabilirsiniz";

Calendar._TT["PREV_YEAR"] = "Önceki Yıl (Menü için basılı tutunuz)";
Calendar._TT["PREV_MONTH"] = "Önceki Ay (Menü için basılı tutunuz)";
Calendar._TT["GO_TODAY"] = "Bugün";
Calendar._TT["NEXT_MONTH"] = "Gelecek Ay (Menü için basılı tutunuz)";
Calendar._TT["NEXT_YEAR"] = "Gelecek Sene (Menü için basılı tutunuz)";
Calendar._TT["SEL_DATE"] = "Tarih seçiniz";
Calendar._TT["DRAG_TO_MOVE"] = "Sürükle Bırak";
Calendar._TT["PART_TODAY"] = " (bugün)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Önce %s gününü göster";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Kapalı";
Calendar._TT["TODAY"] = "Bugün";
Calendar._TT["TIME_PART"] = "Değeri değiştirmek için (Maj-)Tıkla veya sürükle.";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%a-%g";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "hf";
Calendar._TT["TIME"] = "Saat:";
