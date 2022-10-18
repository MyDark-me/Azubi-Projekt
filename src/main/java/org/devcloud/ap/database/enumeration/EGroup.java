package org.devcloud.ap.database.enumeration;

public enum EGroup {
    ID("groupID"),
    NAME("name"),
    COLOR("color");

    final String name;

    EGroup(String name) { this.name = name; }

    @Override
    public String toString() {return name.toLowerCase(); }
}
