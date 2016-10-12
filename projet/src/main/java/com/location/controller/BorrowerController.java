package com.location.controller;

import com.location.core.Exception.NotFoundException;
import com.location.core.form.Field;
import com.location.core.form.Form;
import com.location.core.mvc.Controller;
import com.location.model.Borrower;
import com.location.model.Vehicle;
import com.location.util.Flash;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.*;

public class BorrowerController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView home(Request request, Response response){
        String showRentals = request.queryMap("showRentals").value() != null ? request.queryMap("showRentals").value() : "1";
        String showNoRentals = request.queryMap("showNoRentals").value() != null ? request.queryMap("showNoRentals").value() : "1";

        String by = request.queryMap("by").value() != null ? request.queryMap("by").value() : "all";
        String order = request.queryMap("order").value() != null ? request.queryMap("order").value() : "";
        String type = request.queryMap("type").value() != null ? request.queryMap("type").value() : "";
        String search = request.queryMap("search").value() != null ? request.queryMap("search").value() : "";
        List<Borrower> borrowerList = new ArrayList<>();
        List<Vehicle> vehicleList = Vehicle.findAll();

        if(!showRentals.equals("1") && !showRentals.equals("0"))
            showRentals = "1";
        if(!showNoRentals.equals("1") && !showNoRentals.equals("0"))
            showNoRentals = "1";
        if(!new ArrayList<String>(){{ add("all"); add("name"); add("vehicle"); }}.contains(by))
            by = "all";
        if(!order.equals("name") && !order.equals("bill") && !order.equals(""))
            order = "";
        if(!type.equals("asc") && !type.equals("desc") && !order.equals(""))
            order = "desc";

        switch (by){
            case "all":
                borrowerList = Borrower.findAll(showRentals, showNoRentals);
                break;

            case "name":
                borrowerList = Borrower.findByName(search, showRentals, showNoRentals);
                break;

            case "vehicle":
                if(search.equals(""))
                    search = "1";

                borrowerList = Borrower.findByVehicle(Integer.parseInt(search), showRentals, showNoRentals);
                break;
        }

        switch (order){
            case "name":
                Collections.sort(borrowerList, (borrower1, borrower2) -> {
                    if(type.equals("asc"))
                        return  borrower1.getLastName().compareTo(borrower2.getLastName());

                    return  borrower2.getLastName().compareTo(borrower1.getLastName());
                });
                break;

            case "bill":
                Collections.sort(borrowerList, (borrower1, borrower2) -> {
                    if(type.equals("asc"))
                        return  borrower1.getBill() - borrower2.getBill();

                    return  borrower2.getBill() - borrower1.getBill();
                });
                break;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Emprunteurs");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunteurs"); }});
        vars.put("borrowers", borrowerList);
        vars.put("vehicles", vehicleList);
        vars.put("by", by);
        vars.put("order", order);
        vars.put("type", type);
        vars.put("search", search);
        vars.put("showRentals", showRentals);
        vars.put("showNoRentals", showNoRentals);

        Vehicle.hydrate(1);

        return new ModelAndView(vars, "templates/Borrower/home.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView createForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("firstname", "");
        data.put("lastname", "");
        data.put("street", "");
        data.put("city", "");
        data.put("pc", "");

        vars.put("title", "Nouvel emprunteur");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunteurs"); add("Nouvel emprunteur"); }});

        return new ModelAndView(vars, "templates/Borrower/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView create(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();

        data.put("firstname", request.queryMap("firstname").value());
        data.put("lastname", request.queryMap("lastname").value());
        data.put("street", request.queryMap("street").value());
        data.put("city", request.queryMap("city").value());
        data.put("pc", request.queryMap("pc").value());

        vars.put("title", "Nouvel emprunteur");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunteurs"); add("Nouvel emprunteur"); }});

        Form form = new Form("borrower", request);
        form.text("firstname").add(Field.DIFFERENT, "", "Vous devez donner un prénom");
        form.text("lastname").add(Field.DIFFERENT, "", "Vous devez donner un nom de famille");
        form.text("street").add(Field.DIFFERENT, "", "Vous devez donner une rue");
        form.text("city").add(Field.DIFFERENT, "", "Vous devez donner une ville");
        form.text("pc").add(Field.DIFFERENT, "", "Vous devez donner un code postal");

        if(form.send()) {
            if (form.check()) {
                Borrower borrower = new Borrower();
                borrower.setFirstName((String)data.get("firstname"));
                borrower.setLastName((String)data.get("lastname"));
                borrower.setStreet((String)data.get("street"));
                borrower.setPc((String)data.get("pc"));
                borrower.setCity((String)data.get("city"));
                borrower.insert();

                Flash.message(request, "L'emprunteur a bien été créé");
                response.redirect("/borrowers");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Borrower/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView updateForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Borrower borrower = Borrower.hydrate(Integer.parseInt(request.params(":id")));

        if(borrower == null) throw new NotFoundException();

        data.put("firstname", borrower.getFirstName());
        data.put("lastname", borrower.getLastName());
        data.put("street", borrower.getStreet());
        data.put("city", borrower.getCity());
        data.put("pc", borrower.getPc());

        vars.put("title", "Editer un emprunteur");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunteurs"); add("Editer un emprunteur"); add(data.get("firstname") + " " + data.get("lastname")); }});

        return new ModelAndView(vars, "templates/Borrower/update.vm");
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
        Borrower borrower = Borrower.hydrate(Integer.parseInt(request.params(":id")));

        if(borrower == null) throw new NotFoundException();

        data.put("firstname", request.queryMap("firstname").value());
        data.put("lastname", request.queryMap("lastname").value());
        data.put("street", request.queryMap("street").value());
        data.put("city", request.queryMap("city").value());
        data.put("pc", request.queryMap("pc").value());

        Form form = new Form("borrower", request);
        form.text("firstname").add(Field.DIFFERENT, "", "Vous devez donner un prénom");
        form.text("lastname").add(Field.DIFFERENT, "", "Vous devez donner un nom de famille");
        form.text("street").add(Field.DIFFERENT, "", "Vous devez donner une rue");
        form.text("city").add(Field.DIFFERENT, "", "Vous devez donner une ville");
        form.text("pc").add(Field.DIFFERENT, "", "Vous devez donner un code postal");

        vars.put("title", "Editer un emprunteur");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunteurs"); add("Editer un emprunteur"); add(borrower.getFirstName() + " " + borrower.getLastName()); }});

        if(form.send()) {
            if (form.check()) {
                borrower.setFirstName((String)data.get("firstname"));
                borrower.setLastName((String)data.get("lastname"));
                borrower.setStreet((String)data.get("street"));
                borrower.setPc((String)data.get("pc"));
                borrower.setCity((String)data.get("city"));
                borrower.update();

                Flash.message(request, "L'emprunteur a bien été édité");
                response.redirect("/borrowers");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Borrower/update.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static String delete(Request request, Response response){
        Borrower borrower = Borrower.hydrate(Integer.parseInt(request.params(":id")));

        if(borrower == null) throw new NotFoundException();
        if(borrower.getSpecimenList().size() > 0) throw new NotFoundException();

        borrower.delete();

        Flash.message(request, "L'emprunteur a bien été supprimé");
        response.redirect("/borrowers");
        return "";
    }
}