package org.devcloud.ap.database.enumeration;

public enum EUser {
    ID("id"),
    NAME("username"),
    PASSWORD("password"),
    EMAIL("email"),
    TOKEN("token");

    final String name;

    EUser(String name) { this.name = name; }

    @Override
    public String toString() {return name.toLowerCase(); }
}
