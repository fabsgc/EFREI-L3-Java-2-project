package com.location.util;

import spark.Request;

public class Flash {

    /**
     * @param request Request
     * @param message Response
     */

    public static void message(Request request, String message){
        request.session().attribute("flash", message);
        request.session().attribute("flash-count", "1");
    }

    /**
     * @param request Request
     */

    public static void delete(Request request){
        if(request.session().attribute("flash") != null){
            if(request.session().attribute("flash-count").equals("3")) {
                request.session().removeAttribute("flash");
                request.session().removeAttribute("flash-count");
            }
            else if(request.session().attribute("flash-count").equals("2") && request.session().attribute("flash").equals("Vous êtes bien connecté")) {
                request.session().removeAttribute("flash");
                request.session().removeAttribute("flash-count");
            }
            else if(request.session().attribute("flash-count").equals("2") && !request.session().attribute("flash").equals("Vous êtes bien connecté")) {
                request.session().removeAttribute("flash");
                request.session().removeAttribute("flash-count");
            }
            else {
                request.session().attribute("flash-count", "2");
            }
        }
    }
}
