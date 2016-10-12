package com.location.core.form;

import spark.Request;
import java.util.*;

public class Form {
    private String name;
    private List<Field> fieldList;
    private Map<String, Object> errorList;
    private Request request;

    /**
     * @param name String
     * @param request Request
     */

    public Form(String name, Request request){
        this.name = name;
        this.fieldList = new ArrayList<>();
        this.request = request;
        this.errorList = new HashMap<>();
    }

    /**
     * @param name String
     * @return Field
     */

    public Field text(String name){
        Field field = new FieldText(name, this.request);
        this.fieldList.add(field);

        return field;
    }

    /**
     * @return Boolean
     */

    public Boolean send() {
        return this.request.queryMap(this.name) != null;
    }

    /**
     * @return Boolean
     */

    public Boolean check(){
        this.fieldList.stream().filter(field -> !field.check()).forEach(field -> {
            this.errorList.put(field.getName(), field.errors());
        });

        return this.errorList.size() <= 0;
    }

    /**
     * @return Map
     */

    public Map<String, Object> errors(){
        return this.errorList;
    }
}
