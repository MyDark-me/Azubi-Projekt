package org.devcloud.ap.database.enumeration.pattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EPattern {
    /*
     * Name:
     * mindestens 3 zeichen
     * erlaubt sind:
     * groß und klein buchstaben
     * 0-9, _ und -
     */
    NAME("^[a-zA-Z0-9-_]{3,}$"),
    /*
     * Password:
     * mindestens 8 zeichen
     * es muss mindestens:
     * ein Großbuchstabe, Kleinbuchstabe, spezial character
     */
    PASSWORD("^(?=.*?[A-Z])(?=(.*[a-z]){1,})(?=(.*[\\W])*)(?!.*\\s).{8,}$"),
    EMAIL("^[a-zA-Z0-9.!#$%&'*+=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"),
    TOKEN("^[a-zA-Z0-9-_]{6,}$"),
    // Source: https://ihateregex.io/
    PHONE_NUMBER("^[0-9]{3}-[0-9]{3}-[0-9]{4}$"),
    // Source: https://ihateregex.io/
    DATE("(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})"),
    // Source: https://ihateregex.io/
    IP_ADDRESS("(\\b25[0-5]|\\b2[0-4][0-9]|\\b[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}"),
    // Source: https://ihateregex.io/
    IP_ADDRESS6("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");

    final String aPattern;
    EPattern(String pattern) { this.aPattern = pattern; }
    @Override
    public String toString() {return aPattern; }

    public boolean isMatch(CharSequence input) {
        Pattern pattern = Pattern.compile(aPattern);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
