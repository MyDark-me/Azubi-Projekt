package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.database.SQLPostgres;
import org.devcloud.ap.utils.HTTPServer;

import java.io.IOException;

public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;

    public static void main(String[] args) throws IOException {
        HTTPServer.startServer();
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");
    }
}
