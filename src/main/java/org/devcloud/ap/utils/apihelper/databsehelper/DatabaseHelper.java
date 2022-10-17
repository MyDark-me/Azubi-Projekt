package org.devcloud.ap.utils.apihelper.databsehelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.Response;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class DatabaseHelper {
    @Getter
    private final InputHelper inputHelper;

    public Response getResponse() {
        return inputHelper.getResponse();
    }

    public Logger getLogger() {
        return inputHelper.getResponse().getLogger();
    }



}
