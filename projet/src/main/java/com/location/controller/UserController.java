package com.location.controller;

import com.location.core.form.Field;
import com.location.core.form.Form;
import com.location.core.mvc.Controller;
import com.location.model.User;
import com.location.util.Password;
import com.location.util.Flash;
import spark.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import spark.Response;
import spark.Request;

public class UserController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView login(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Connexion");
        return new ModelAndView(vars, "templates/User/login.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return String
     * @throws Exception
     */

    public static String loginProcess(Request request, Response response) throws Exception {
        /*String userName = request.queryMap("username").value();
        String password = request.queryMap("password").value();*/

        String userName = "fabsgc";
        String password = "mdpmdp";

        if(!userName.equals("") && !password.equals("")){
            com.location.model.User user = User.getUserByUserNamePassword(userName, Password.SHA1(password));

            if(user != null){
                request.session().attribute("id", user.getId());
                request.session().attribute("username", user.getUsername());
                request.session().attribute("email", user.getEmail());
                request.session().attribute("firstname", user.getFirstName());
                request.session().attribute("lastname", user.getLastName());
                request.session().attribute("avatar", user.getAvatar());
                Flash.message(request, "Vous êtes bien connecté");

                return "1";
            }
        }

        return "0";
    }

    /**
     * @param request Request
     * @param response Response
     * @return String
     */

    public static String logout(Request request, Response response){
        request.session().removeAttribute("id");
        request.session().removeAttribute("username");
        request.session().removeAttribute("email");
        request.session().removeAttribute("firstname");
        request.session().removeAttribute("lastname");
        request.session().removeAttribute("avatar");
        return "";
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView updateForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        vars.put("title", "Mon compte");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Mon compte"); }});

        data.put("email", request.session().attribute("email"));
        data.put("username", request.session().attribute("username"));
        data.put("firstname", request.session().attribute("firstname"));
        data.put("lastname", request.session().attribute("lastname"));
        data.put("password", "");

        vars.put("data", data);
        vars.put("errors", new ArrayList<>());

        return new ModelAndView(vars, "templates/User/update.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView update(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();

        vars.put("title", "Mon compte");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Mon compte"); }});

        Form form = new Form("user", request);

        form.text("email")
                .add(Field.DIFFERENT, "", "Vous devez donner une adresse email")
                .add(Field.MAIL, "", "Vous devez donner une adresse mail valide");
        form.text("username")
                .add(Field.DIFFERENT, "", "Vous devez indiquer votre nom d'utilisateur")
                .add(Field.SQL, "SELECT COUNT(*) FROM public.user WHERE username = :data AND id != :id", "==", 0,
                        new HashMap<String, Object>() {{ put("id", request.session().attribute("id")); }},
                        "Ce nom d'utilisateur est déjà utilisé");
        form.text("firstname").add(Field.DIFFERENT, "", "Vous devez donner votre prénom");
        form.text("lastname").add(Field.DIFFERENT, "", "Vous devez donner votre nom de famille");
        form.text("password").add(Field.LENGTHMIN, "6", "Votre mot de passe doit faire 6 caractères au minimum");
        form.text("test").add(Field.DIFFERENT, "", "error");

        if(form.send()){
            if(form.check()){
                User user = User.hydrate(request.session().attribute("id"));

                if(user != null){
                    user.setEmail(request.queryParams("email"));
                    user.setUsername(request.queryParams("username"));
                    user.setFirstName(request.queryParams("firstname"));
                    user.setLastName(request.queryParams("lastname"));

                    try {
                        user.setPassword(Password.SHA1(request.queryParams("password")));
                    }
                    catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                        logger.debug(e.toString());
                    }
                    user.update();
                }

                Flash.message(request, "Votre compte a bien été mis à jour");
                response.redirect("/user");
            }
            else{
                errors = form.errors();
            }
        }

        data.put("email", request.queryMap("email").value());
        data.put("username", request.queryMap("username").value());
        data.put("firstname", request.queryMap("firstname").value());
        data.put("lastname", request.queryMap("lastname").value());
        data.put("password", request.queryMap("password").value());

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/User/update.vm");
    }
}