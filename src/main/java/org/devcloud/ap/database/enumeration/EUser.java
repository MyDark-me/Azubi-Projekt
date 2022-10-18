package org.devcloud.ap.database.enumeration;

public enum EUser {
    ID("userID"),
    NAME("userName"),
    PASSWORD("userPassword"),
    EMAIL("userEmail"),
    TOKEN("userToken");

    final String name;

    EUser(String name) { this.name = name; }

    @Override
    public String toString() {return name.toLowerCase(); }
}
