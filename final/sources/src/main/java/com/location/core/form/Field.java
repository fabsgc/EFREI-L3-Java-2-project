package com.location.core.form;

import spark.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Field {
    protected String name;
    protected String data;
    protected Request request;
    protected List<Map<String, Object>> constraintList;
    protected List<String> errorList;

    public static final int EQUAL     = 0;
    public static final int DIFFERENT = 1;

    public static final int LENGTHMIN = 2;
    public static final int LENGTHMAX = 3;

    public static final int REGEX     = 4;
    public static final int MAIL      = 5;
    public static final int SQL       = 6;

    public static final int EXIST     = 7;
    public static final int EMPTY     = 8;

    public static final int ALPHADASH = 8;

    public static final int ASSERT    = 9;

    /**
     * @param name String
     * @param request Request
     */

    Field(String name, Request request){
        this.name = name;
        this.request = request;
        this.constraintList = new ArrayList<>();
        this.errorList = new ArrayList<String>();
        this.data = request.queryMap(this.name).value();
    }

    /**
     * @param type int
     * @param constraint String
     * @param error error
     * @return Field
     */

    public Field add(int type, String constraint, String error){
        Map<String, Object> data = new HashMap<>();

        data.put("constraint", constraint);
        data.put("type", type);
        data.put("error", error);

        this.constraintList.add(data);

        return this;
    }

    /**
     * @param type int
     * @param constraint Boolean
     * @param error error
     * @return Field
     */

    public Field add(int type, Boolean constraint, String error){
        Map<String, Object> data = new HashMap<>();

        data.put("constraint", constraint);
        data.put("type", type);
        data.put("error", error);

        this.constraintList.add(data);

        return this;
    }

    /**
     * @param type int
     * @param query String
     * @param constraint String
     * @param value int
     * @param vars Map
     * @param error error
     * @return Field
     */

    public Field add(int type, String query, String constraint, int value, Map<String, Object> vars, String error){
        Map<String, Object> data = new HashMap<>();

        data.put("query", query);
        data.put("constraint", constraint);
        data.put("value", value);
        data.put("vars", vars);
        data.put("type", type);
        data.put("error", error);

        this.constraintList.add(data);

        return this;
    }

    /**
     * @return Boolean
     */

    Boolean check(){
        return true;
    }

    /**
     * @return List
     */

    List errors(){
        return this.errorList;
    }

    /**
     * @return String
     */

    String getName() {
        return name;
    }
}
