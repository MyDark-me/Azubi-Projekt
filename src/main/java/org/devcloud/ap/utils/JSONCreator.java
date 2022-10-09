package org.devcloud.ap.utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class JSONCreator {
    private final ArrayList<String> keyArray;
    private final ArrayList<Object> valueArray;

    public JSONCreator() {
        this.keyArray = new ArrayList<>();
        this.valueArray = new ArrayList<>();
    }

    public JSONCreator addKeys(String ... keys) {
        this.keyArray.addAll(Arrays.asList(keys));
        return this;
    }

    public JSONCreator addValue(Object ... values) {
        this.valueArray.addAll(Arrays.asList(values));
        return this;
    }

    public JSONObject create() {
        if(this.keyArray.isEmpty() || this.valueArray.isEmpty())
            throw new NullPointerException("Einer der Listen sind leer!");
        if(this.keyArray.size() != this.valueArray.size())
            throw new IllegalArgumentException("Die Listen sind nicht gleich groß!");

        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < this.keyArray.size(); i++) {
            Object value = this.valueArray.get(i);
            if(value instanceof String aString)
                jsonObject.put(this.keyArray.get(i), aString);
            else if(value instanceof Integer aInteger)
                jsonObject.put(this.keyArray.get(i), aInteger);
            else if(value instanceof Long aLong)
                jsonObject.put(this.keyArray.get(i), aLong);
            else if(value instanceof Boolean aBool)
                jsonObject.put(this.keyArray.get(i), aBool);
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return create().toString();
    }
}
