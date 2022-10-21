package org.devcloud.ap.utils.apihelper;

import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.enumeration.EGroup;
import org.devcloud.ap.database.enumeration.ERole;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.slf4j.Logger;

import java.util.Map;

public class InputHelper {
    @Getter
    private final Response response;
    @Getter
    private final Map<String, String> queryMap;
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

    // USER STUFF

    public void checkUserID() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.ID.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.ID.isMatching(this.queryMap.get(EUser.ID.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_PASSWORD);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_PASSWORD.getMessage());
        }
    }

    public Boolean isUserID()  {
        return this.queryMap.containsKey(EUser.ID.toString());
    }
    public String getUserID()  {
        return this.queryMap.get(EUser.ID.toString());
    }

    public void checkUserName() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EUser.NAME.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(EUser.NAME.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_NAME);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_NAME.getMessage());
        }
    }

    public Boolean isUserName()  {
        return this.queryMap.containsKey(EUser.NAME.toString());
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
        if(EPattern.PASSWORD.isMatching(this.queryMap.get(EUser.PASSWORD.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_PASSWORD);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_PASSWORD.getMessage());
        }
    }

    public Boolean isUserPassword()  {
        return this.queryMap.containsKey(EUser.PASSWORD.toString());
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
        if(EPattern.EMAIL.isMatching(this.queryMap.get(EUser.EMAIL.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_EMAIL);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_EMAIL.getMessage());
        }
    }

    public Boolean isUserEMail()  {
        return this.queryMap.containsKey(EUser.EMAIL.toString());
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
        if(EPattern.TOKEN.isMatching(this.queryMap.get(EUser.TOKEN.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isUserToken()  {
        return this.queryMap.containsKey(EUser.TOKEN.toString());
    }
    public String getUserToken()  {
        return this.queryMap.get(EUser.TOKEN.toString());
    }

    // GROUP STUFF

    public void checkGroupID() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EGroup.ID.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(EGroup.ID.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isGroupID()  {
        return this.queryMap.containsKey(EGroup.ID.toString());
    }
    public String getGroupID()  {
        return this.queryMap.get(EGroup.ID.toString());
    }

    public void checkGroupName() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EGroup.NAME.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(EGroup.NAME.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isGroupName()  {
        return this.queryMap.containsKey(EGroup.NAME.toString());
    }
    public String getGroupName()  {
        return this.queryMap.get(EGroup.NAME.toString());
    }

    public void checkGroupColor() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(EGroup.COLOR.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(EGroup.COLOR.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isGroupColor()  {
        return this.queryMap.containsKey(EGroup.COLOR.toString());
    }
    public String getGroupColor()  {
        return this.queryMap.get(EGroup.COLOR.toString());
    }

    // ROLE STUFF

    public void checkRoleID() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(ERole.ID.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(ERole.ID.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isRoleID()  {
        return this.queryMap.containsKey(ERole.ID.toString());
    }
    public String getRoleID()  {
        return this.queryMap.get(ERole.ID.toString());
    }

    public void checkRoleName() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(ERole.NAME.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(ERole.NAME.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isRoleName()  {
        return this.queryMap.containsKey(ERole.NAME.toString());
    }
    public String getRoleName()  {
        return this.queryMap.get(ERole.NAME.toString());
    }

    public void checkRoleColor() throws WrongInputException {
        if(this.called) return;
        if(!this.queryMap.containsKey(ERole.COLOR.toString())) {
            this.response.writeResponse(EMessages.MISSING_KEY);
            this.called = true;
            throw new WrongInputException(EMessages.MISSING_KEY.getMessage());
        }
        if(EPattern.NAME.isMatching(this.queryMap.get(ERole.COLOR.toString()))) {
            this.response.writeResponse(EMessages.WRONG_KEY_TOKEN);
            this.called = true;
            throw new WrongInputException(EMessages.WRONG_KEY_TOKEN.getMessage());
        }
    }

    public Boolean isRoleColor()  {
        return this.queryMap.containsKey(ERole.COLOR.toString());
    }
    public String getRoleColor()  {
        return this.queryMap.get(ERole.COLOR.toString());
    }
}
