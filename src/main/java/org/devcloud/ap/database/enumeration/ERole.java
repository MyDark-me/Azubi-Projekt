package org.devcloud.ap.database.enumeration;

public enum ERole {
    ID("roleID"),
    NAME("name"),
    COLOR("color");

    final String name;

    ERole(String name) { this.name = name; }

    @Override
    public String toString() {return name.toLowerCase(); }
}
