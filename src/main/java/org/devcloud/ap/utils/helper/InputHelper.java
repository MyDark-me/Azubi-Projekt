package org.devcloud.ap.utils.helper;

import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.utils.helper.exeption.WrongInputException;
import org.slf4j.Logger;

import java.util.Map;

public class InputHelper {
    @Getter
    private Response response;
    @Getter
    private Map<String, String> queryMap;
    @Getter @Setter
    private boolean called;

    public InputHelper(final Logger logger, final HttpExchange httpExchange) {
        this.response = new Response(logger, httpExchange);
        this.queryMap = this.response.getEntities();
        this.called = false;

        response.addResponseHeaders();
        response.debugRequest();
    }

    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        DATABASE_NOT_AVAILABLE(500, "Datenbank ist nicht Erreichbar!"),
        NO_QUERY(400, "Es wurden keine Informationen mitgegeben."),
        MISSING_KEY(400, "Es wurden nicht die richtigen Informationen mitgegeben."),
        WRONG_KEY_NAME(400, "Name entspricht nicht dem Muster."),
        WRONG_KEY_PASSWORD(400, "Passwort entspricht nicht dem Muster."),
        WRONG_KEY_EMAIL(400, "Email entspricht nicht dem Muster."),
        WRONG_KEY_TOKEN(400, "Token entspricht nicht dem Muster."),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    public void checkConnection() throws WrongInputException {
        if(this.called) return;
        if(!Azubiprojekt.getSqlPostgres().isConnection()) {
            this.response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
            this.called = true;
            throw new WrongInputException(EMessages.DATABASE_NOT_AVAILABLE.getMessage());
        }
    }

    public void checkUserName() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.NAME.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(!EPattern.NAME.isMatch(this.queryMap.get(EUser.NAME.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_NAME);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_NAME.getMessage());
        }
    }

    public String getUserName()  {
        return this.queryMap.get(EUser.NAME.toString());
    }

    public void checkUserPassword() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.PASSWORD.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(!EPattern.PASSWORD.isMatch(this.queryMap.get(EUser.PASSWORD.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_PASSWORD);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_PASSWORD.getMessage());
        }
    }

    public String getUserPassword()  {
        return this.queryMap.get(EUser.PASSWORD.toString());
    }

    public void checkUserEMail() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.EMAIL.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(!EPattern.EMAIL.isMatch(this.queryMap.get(EUser.EMAIL.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_EMAIL);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_EMAIL.getMessage());
        }
    }

    public String getUserEMail()  {
        return this.queryMap.get(EUser.EMAIL.toString());
    }

    public void checkUserToken() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.TOKEN.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(!EPattern.TOKEN.isMatch(this.queryMap.get(EUser.TOKEN.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public String getUserToken()  {
        return this.queryMap.get(EUser.TOKEN.toString());
    }
}
