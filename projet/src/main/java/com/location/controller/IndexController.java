package com.location.controller;

import com.location.core.mvc.Controller;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndexController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView home(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Accueil");
        vars.put("filAriane", new ArrayList<String>() {{ add("Accueil"); }});
        vars.put("request", request);
        vars.put("response", response);

        return new ModelAndView(vars, "templates/Index/home.vm");
    }
}