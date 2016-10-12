package com.location.controller;

import com.location.core.Exception.NotFoundException;
import com.location.core.form.Field;
import com.location.core.form.Form;
import com.location.core.mvc.Controller;
import com.location.model.*;
import com.location.util.Flash;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.*;

public class VehicleController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView home(Request request, Response response){
        String showBike = request.queryMap("showBike").value() != null ? request.queryMap("showBike").value() : "1";
        String showCar = request.queryMap("showCar").value() != null ? request.queryMap("showCar").value() : "1";
        String showCarLuxury = request.queryMap("showCarLuxury").value() != null ? request.queryMap("showCarLuxury").value() : "1";

        String order = request.queryMap("order").value() != null ? request.queryMap("order").value() : "";
        String type = request.queryMap("type").value() != null ? request.queryMap("type").value() : "";
        String search = request.queryMap("search").value() != null ? request.queryMap("search").value() : "";

        if(!showBike.equals("1") && !showBike.equals("0"))
            showBike = "1";
        if(!showCar.equals("1") && !showCar.equals("0"))
            showCar = "1";
        if(!showCarLuxury.equals("1") && !showCarLuxury.equals("0"))
            showCarLuxury = "1";
        if(!order.equals("brand") && !order.equals(""))
            order = "";
        if(!type.equals("asc") && !type.equals("desc") && !order.equals(""))
            order = "desc";

        List<Vehicle> vehicleList = Vehicle.findByBrand(search, showBike, showCar, showCarLuxury);

        switch (order){
            case "brand":
                Collections.sort(vehicleList, (vehicle1, vehicle2) -> {
                    if(type.equals("asc"))
                        return  vehicle1.getBrand().compareTo(vehicle2.getBrand());

                    return  vehicle2.getBrand().compareTo(vehicle1.getBrand());
                });
                break;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Véhicules");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); }});
        vars.put("vehicles", vehicleList);
        vars.put("showBike", showBike);
        vars.put("showCar", showCar);
        vars.put("showCarLuxury", showCarLuxury);
        vars.put("order", order);
        vars.put("type", type);
        vars.put("search", search);
        vars.put("brands", Vehicle.findBrands());

        return new ModelAndView(vars, "templates/Vehicle/home.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView vehicle(Request request, Response response){
        String order = request.queryMap("order").value() != null ? request.queryMap("order").value() : "";
        String type = request.queryMap("type").value() != null ? request.queryMap("type").value() : "";
        String search = request.queryMap("search").value() != null ? request.queryMap("search").value() : "";

        if(!order.equals("kilometers") && !order.equals(""))
            order = "";
        if(!type.equals("asc") && !type.equals("desc") && !order.equals(""))
            order = "desc";

        Map<String, Object> vars = new HashMap<>();
        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params(":id")));

        if(vehicle == null) throw new NotFoundException();

        if(vehicle.getType() == 1){
            Car car = Car.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + car.getModel()); }});
            vars.put("vehicle", car);
        }
        else{
            Bike bike = Bike.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add(vehicle.getBrand() + " " + bike.getCylinder()); }});
            vars.put("vehicle", bike);
        }

        List<Specimen> specimenList = Specimen.findByVehicleKilometers(Integer.parseInt(request.params(":id")), search);

        switch (order){
            case "kilometers":
                Collections.sort(specimenList, (specimen1, specimen2) -> {
                    if(type.equals("asc"))
                        return specimen1.getKilometers() - specimen2.getKilometers();

                    return specimen2.getKilometers() - specimen1.getKilometers();
                });
                break;
        }

        vars.put("specimens", specimenList);
        vars.put("title", "Véhicules");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("order", order);
        vars.put("type", type);
        vars.put("search", search);

        if(!search.equals("")){
            vars.put("kilometer1", search.split("\\.")[0]);
            vars.put("kilometer2", search.split("\\.")[1]);
        }
        else{
            vars.put("kilometer1", 0);
            vars.put("kilometer2", 1000);
        }

        return new ModelAndView(vars, "templates/Vehicle/vehicle.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView createForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("type", "1");
        data.put("priceday", "10");
        data.put("brand", "");
        data.put("model", "");
        data.put("cylinder", "");
        data.put("luxury", "");

        vars.put("title", "Nouveau véhicule");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Nouveau véhicule"); }});

        return new ModelAndView(vars, "templates/Vehicle/create.vm");
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

        data.put("type", request.queryMap("type").value());
        data.put("priceday", request.queryMap("priceday").value());
        data.put("brand", request.queryMap("brand").value());
        data.put("model", request.queryMap("model").value());
        data.put("cylinder", request.queryMap("cylinder").value());
        data.put("luxury", request.queryMap("luxury").value());

        vars.put("title", "Nouveau véhicule");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Nouveau véhicule"); }});

        Form form = new Form("vehicles", request);
        form.text("type").add(Field.DIFFERENT, "", "Vous devez préciser un type");
        form.text("priceday").add(Field.DIFFERENT, "", "Vous devez donner le prix journalier");
        form.text("brand").add(Field.DIFFERENT, "", "Vous devez donner la marque");

        if(data.get("type").equals("1")) {
            form.text("model").add(Field.DIFFERENT, "", "Vous devez préciser le modèle du véhicule");
            form.text("luxury").add(Field.DIFFERENT, "", "Vous devez préciser si le véhicule est standard ou non");
        }
        else {
            form.text("cylinder").add(Field.REGEX, "^(\\d+)$", "Vous devez donner la cylindrée");
        }

        if(form.send()) {
            if (form.check()) {
                if(data.get("type").equals("1")){
                    Car car = new Car();
                    car.setType(Integer.parseInt((String)data.get("type")));
                    car.setPriceDay(Integer.parseInt((String)data.get("priceday")));
                    car.setBrand((String)data.get("brand"));
                    car.setModel((String)data.get("model"));
                    car.setLuxury(Integer.parseInt((String)data.get("luxury")));
                    car.insert();
                }
                else{
                    Bike bike = new Bike();
                    bike.setType(Integer.parseInt((String)data.get("type")));
                    bike.setPriceDay(Integer.parseInt((String)data.get("priceday")));
                    bike.setBrand((String)data.get("brand"));
                    bike.setCylinder(Integer.parseInt((String)data.get("cylinder")));
                    bike.insert();
                }

                Flash.message(request, "Le véhicule a bien été créé");
                response.redirect("/vehicles");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Vehicle/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView updateForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params().get(":id")));
        Bike bike;
        Car car;

        if(vehicle == null) throw new NotFoundException();

        if(vehicle.getType() == 1){
            car = Car.hydrate(Integer.parseInt(request.params().get(":id")));
            data.put("type", String.valueOf(car.getType()));
            data.put("priceday", String.valueOf(car.getPriceDay()));
            data.put("brand", car.getBrand());
            data.put("model", car.getModel());
            data.put("cylinder", String.valueOf(0));
            data.put("luxury", String.valueOf(car.getLuxury()));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Editer un véhicule"); add((String)data.get("brand") + " " + data.get("model")); }});
        }
        else{
            bike = Bike.hydrate(Integer.parseInt(request.params().get(":id")));
            data.put("type", String.valueOf(bike.getType()));
            data.put("priceday", String.valueOf(bike.getPriceDay()));
            data.put("brand", bike.getBrand());
            data.put("model", "");
            data.put("cylinder", String.valueOf(bike.getCylinder()));
            data.put("luxury", "");
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Editer un véhicule"); add((String)data.get("brand") + " " + data.get("cylinder")); }});
        }

        vars.put("title", "Editer un véhicule");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);

        return new ModelAndView(vars, "templates/Vehicle/update.vm");
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
        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params().get(":id")));
        Bike bike = new Bike();
        Car car = new Car();

        if(vehicle == null) throw new NotFoundException();

        data.put("type", request.queryMap("type").value());
        data.put("priceday", request.queryMap("priceday").value());
        data.put("brand", request.queryMap("brand").value());
        data.put("model", request.queryMap("model").value());
        data.put("cylinder", request.queryMap("cylinder").value());
        data.put("luxury", request.queryMap("luxury").value());

        if(vehicle.getType() == 1){
            car = Car.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Editer un véhicule"); add((String)data.get("brand") + " " + data.get("model")); }});
        }
        else{
            bike = Bike.hydrate(Integer.parseInt(request.params().get(":id")));
            vars.put("filAriane", new ArrayList<String>(){{ add("Véhicules"); add("Editer un véhicule"); add((String)data.get("brand") + " " + data.get("cylinder")); }});
        }

        vars.put("title", "Editer un véhicule");
        vars.put("request", request);
        vars.put("response", response);

        Form form = new Form("vehicles", request);
        form.text("type").add(Field.DIFFERENT, "", "Vous devez préciser un type");
        form.text("priceday").add(Field.DIFFERENT, "", "Vous devez donner le prix journalier");
        form.text("brand").add(Field.DIFFERENT, "", "Vous devez donner la marque");

        if(data.get("type").equals("1")) {
            form.text("model").add(Field.DIFFERENT, "", "Vous devez préciser le modèle du véhicule");
            form.text("luxury").add(Field.DIFFERENT, "", "Vous devez préciser si le véhicule est standard ou non");
        }
        else {
            form.text("cylinder").add(Field.REGEX, "^(\\d+)$", "Vous devez donner la cylindrée");
        }

        if(form.send()) {
            if (form.check()) {
                if(data.get("type").equals("1")){
                    car.setType(Integer.parseInt((String)data.get("type")));
                    car.setPriceDay(Integer.parseInt((String)data.get("priceday")));
                    car.setBrand((String)data.get("brand"));
                    car.setModel((String)data.get("model"));
                    car.setLuxury(Integer.parseInt((String)data.get("luxury")));
                    car.update();
                }
                else{
                    bike.setType(Integer.parseInt((String)data.get("type")));
                    bike.setPriceDay(Integer.parseInt((String)data.get("priceday")));
                    bike.setBrand((String)data.get("brand"));
                    bike.setCylinder(Integer.parseInt((String)data.get("cylinder")));
                    bike.update();
                }

                Flash.message(request, "Le véhicule a bien été édité");
                response.redirect("/vehicles");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Vehicle/update.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static String delete(Request request, Response response){
        Vehicle vehicle = Vehicle.hydrate(Integer.parseInt(request.params(":id")));
        if(vehicle == null) throw new NotFoundException();
        vehicle.delete();

        Flash.message(request, "Le véhicule a bien été supprimé");
        response.redirect("/vehicles");
        return "";
    }
}
