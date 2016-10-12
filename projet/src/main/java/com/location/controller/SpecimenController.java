package com.location.controller;

import com.location.core.Exception.NotFoundException;
import com.location.core.form.Field;
import com.location.core.form.Form;
import com.location.core.mvc.Controller;
import com.location.model.Bike;
import com.location.model.Car;
import com.location.model.Specimen;
import com.location.model.Vehicle;
import com.location.util.Flash;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpecimenController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView createForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params(":id")));
        if(vehicle == null) throw new NotFoundException();

        if(vehicle.getType() == 1){
            Car car = Car.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + car.getModel()); add("Nouvel exemplaire"); }});
        }
        else{
            Bike bike = Bike.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + bike.getCylinder()); add("Nouvel exemplaire"); }});
            vars.put("vehicle", bike);
        }

        data.put("fuel", "0");
        data.put("state", "1");
        data.put("kilometers", "0");
        data.put("registrationplate", "");

        vars.put("title", "Nouvel exemplaire");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);

        return new ModelAndView(vars, "templates/Specimen/create.vm");
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

        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params(":id")));
        if(vehicle == null) throw new NotFoundException();

        if(vehicle.getType() == 1){
            Car car = Car.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + car.getModel()); add("Nouvel exemplaire"); }});
        }
        else{
            Bike bike = Bike.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + bike.getCylinder()); add("Nouvel exemplaire"); }});
            vars.put("vehicle", bike);
        }

        data.put("fuel", request.queryMap("fuel").value());
        data.put("state", request.queryMap("state").value());
        data.put("kilometers", request.queryMap("kilometers").value());
        data.put("registrationplate", request.queryMap("registrationplate").value());

        vars.put("title", "Nouvel exemplaire");
        vars.put("request", request);
        vars.put("response", response);

        Form form = new Form("vehicles", request);
        form.text("fuel").add(Field.REGEX, "^(\\d+)$", "Vous devez indiquer un niveau de carburant");
        form.text("state").add(Field.DIFFERENT, "", "Vous devez préciser l'état du véhicule");
        form.text("kilometers").add(Field.DIFFERENT, "^(\\d+)$", "Vous devez indiquer la valeur du compteur");
        form.text("registrationplate")
                .add(Field.DIFFERENT, "", "Vous devez indiquer la plaque d'immatriculation")
                .add(Field.SQL, "SELECT COUNT(*) FROM specimen WHERE registrationplate = :data", "==", 0, new HashMap<String, Object>(), "Cette plaque d'immatriculation existe déjà");

        if(form.send()) {
            if (form.check()) {
                Specimen specimen = new Specimen();
                specimen.setVehicle(vehicle);
                specimen.setRental(null);
                specimen.setFuel(Integer.parseInt((String)data.get("fuel")));
                specimen.setState(Integer.parseInt((String)data.get("state")));
                specimen.setKilometers(Integer.parseInt((String)data.get("kilometers")));
                specimen.setRegistrationPlate((String)data.get("registrationplate"));
                specimen.insert();

                Flash.message(request, "L'exemplaire a bien été créé");
                response.redirect("/vehicles/" + request.params(":id"));
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Specimen/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView updateForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        Specimen specimen = Specimen.hydrate(Integer.parseInt(request.params(":id")));
        if(specimen == null) throw new NotFoundException();

        if(specimen.getVehicle().getType() == 1){
            Car car = Car.hydrate(specimen.getVehicle().getId());
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(specimen.getVehicle().getBrand() + " " + car.getModel()); add(specimen.getRegistrationPlate()); add("Modifier un exemplaire"); }});
        }
        else{
            Bike bike = Bike.hydrate(specimen.getVehicle().getId());
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(specimen.getVehicle().getBrand() + " " + bike.getCylinder()); add(specimen.getRegistrationPlate()); add("Modifier un exemplaire"); }});
            vars.put("vehicle", bike);
        }

        data.put("fuel", String.valueOf(specimen.getFuel()));
        data.put("state", String.valueOf(specimen.getState()));
        data.put("kilometers", String.valueOf(specimen.getKilometers()));
        data.put("registrationplate", specimen.getRegistrationPlate());

        vars.put("title", "Editer un véhicule");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Exemplaires"); add("Editer un exemplaire"); }});

        return new ModelAndView(vars, "templates/Specimen/update.vm");
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

        Specimen specimen = Specimen.hydrate(Integer.parseInt(request.params(":id")));
        if(specimen == null) throw new NotFoundException();

        if(specimen.getVehicle().getType() == 1){
            Car car = Car.hydrate(specimen.getVehicle().getId());
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(specimen.getVehicle().getBrand() + " " + car.getModel()); add(specimen.getRegistrationPlate()); add("Modifier un exemplaire"); }});
        }
        else{
            Bike bike = Bike.hydrate(specimen.getVehicle().getId());
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(specimen.getVehicle().getBrand() + " " + bike.getCylinder()); add(specimen.getRegistrationPlate()); add("Modifier un exemplaire"); }});
            vars.put("vehicle", bike);
        }

        data.put("fuel", request.queryMap("fuel").value());
        data.put("state", request.queryMap("state").value());
        data.put("kilometers", request.queryMap("kilometers").value());
        data.put("registrationplate", request.queryMap("registrationplate").value());

        vars.put("title", "Editer un exemplaire");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);

        Form form = new Form("vehicles", request);
        form.text("fuel").add(Field.REGEX, "^(\\d+)$", "Vous devez indiquer un niveau de carburant");
        form.text("state").add(Field.DIFFERENT, "", "Vous devez préciser l'état du véhicule");
        form.text("kilometers").add(Field.DIFFERENT, "^(\\d+)$", "Vous devez indiquer la valeur du compteur");
        form.text("registrationplate")
                .add(Field.DIFFERENT, "", "Vous devez indiquer la plaque d'immatriculation")
                .add(Field.SQL, "SELECT COUNT(*) FROM specimen WHERE registrationplate = :data AND id != :id", "==", 0, new HashMap<String, Object>() {{ put("id", specimen.getId()); }}, "Cette plaque d'immatriculation existe déjà");

        if(form.send()) {
            if (form.check()) {
                specimen.setFuel(Integer.parseInt((String)data.get("fuel")));
                specimen.setState(Integer.parseInt((String)data.get("state")));
                specimen.setKilometers(Integer.parseInt((String)data.get("kilometers")));
                specimen.setRegistrationPlate((String)data.get("registrationplate"));
                specimen.update();

                Flash.message(request, "L'exemplaire a bien été édité");
                response.redirect("/vehicles/" + specimen.getVehicle().getId());
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Specimen/update.vm");
    }

    public static String delete(Request request, Response response){
        Specimen specimen = Specimen.hydrate(Integer.parseInt(request.params(":id")));
        if(specimen == null) throw new NotFoundException();

        if(specimen.getRental() == null){
            specimen.delete();
            Flash.message(request, "L'exemplaire a bien été supprimé");
        }
        else{
            Flash.message(request, "L'exemplaire ne peut pas être supprimé");
        }

        response.redirect("/vehicles/" + specimen.getVehicle().getId());
        return "";
    }
}
