package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.database.SQLPostgres;

public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;

    public static void main(String[] args) {
        sqlPostgres = new SQLPostgres();
    }
}
