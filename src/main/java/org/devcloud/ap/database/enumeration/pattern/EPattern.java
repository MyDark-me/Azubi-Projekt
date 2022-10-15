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
    NROMAL("^[a-zA-Z0-9-_]{3,}$"),
    /*
     * Password:
     * mindestens 8 zeichen
     * es muss mindestens:
     * ein Großbuchstabe, Kleinbuchstabe, spezial character
     */
    PASSWORD("^(?=.*?[A-Z])(?=(.*[a-z]){1,})(?=(.*[\\W])*)(?!.*\\s).{8,}$"),
    EMAIL("^[a-zA-Z0-9.!#$%&'*+=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

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
