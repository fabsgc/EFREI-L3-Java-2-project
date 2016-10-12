package com.location.core.form;

import com.location.core.dao.Database;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2oException;
import spark.Request;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FieldText extends Field {

    /**
     * @param name String
     * @param request Request
     */

    FieldText(String name, Request request){
        super(name, request);
    }

    Boolean check(){
        for(Map<String, Object> constraint : this.constraintList) {
            switch ((Integer) constraint.get("type")) {
                case Field.EQUAL:
                    if (this.data != null && !this.data.equals(constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.DIFFERENT:
                    if (this.data != null && this.data.equals(constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.LENGTHMIN:
                    if (this.data != null && this.data.length() < Integer.parseInt((String) constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.LENGTHMAX:
                    if (this.data != null && this.data.length() > Integer.parseInt((String) constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.REGEX:
                    if (this.data != null && !this.data.matches((String) constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.MAIL:
                    Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(this.data);

                    if (!matcher.find())
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.SQL:
                    if (this.data != null) {
                        try (Connection con = Database.getDao().open()) {
                            Query query = con.createQuery((String) constraint.get("query"));

                            Map<String, Object> varsValues = (Map<String, Object>) constraint.get("vars");

                            try{
                                query.addParameter("data", this.data);
                            }
                            catch (NullPointerException e1){
                                //e1.printStackTrace();
                            }

                            for (Map.Entry<String, Object> entry : varsValues.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();

                                if (entry.getValue().getClass().equals(Integer.class)) {
                                    query.addParameter(key, (int) value);
                                } else if (entry.getValue().getClass().equals(String.class)) {
                                    query.addParameter(key, (String) value);
                                } else if (entry.getValue().getClass().equals(Boolean.class)) {
                                    query.addParameter(key, value);
                                }
                            }

                            Long result = (Long) query.executeScalar();

                            switch ((String) constraint.get("constraint")) {
                                case "==":
                                    if (result != (int) constraint.get("value"))
                                        this.errorList.add((String) constraint.get("error"));
                                    break;

                                case "!=":
                                    if (result == (int) constraint.get("value"))
                                        this.errorList.add((String) constraint.get("error"));
                                    break;

                                case ">":
                                    if (result <= (int) constraint.get("value"))
                                        this.errorList.add((String) constraint.get("error"));
                                    break;

                                case "<":
                                    if (result >= (int) constraint.get("value"))
                                        this.errorList.add((String) constraint.get("error"));
                                    break;
                            }
                        } catch (Sql2oException e) {
                            System.out.println(e.toString());
                        }
                    } else {
                        this.errorList.add((String) constraint.get("error"));
                    }
                    break;

                case Field.EXIST:
                    if (this.data == null)
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.EMPTY:
                    if (this.data != null)
                        this.errorList.add((String) constraint.get("error"));
                    break;

                case Field.ASSERT:
                    if (!((Boolean) constraint.get("constraint")))
                        this.errorList.add((String) constraint.get("error"));
                    break;
            }
        }

        return this.errorList.size() <= 0;
    }
}