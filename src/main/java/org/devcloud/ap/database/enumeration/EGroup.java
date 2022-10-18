package org.devcloud.ap.database.enumeration;

public enum EGroup {
    ID("id"),
    NAME("name"),
    COLOR("color");

    final String name;

    EGroup(String name) { this.name = name; }

    @Override
    public String toString() {return name.toLowerCase(); }
}
