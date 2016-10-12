package com.location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.location.controller.*;
import com.location.core.Exception.NotFoundException;
import com.location.util.Flash;
import com.location.core.template.View;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static spark.Spark.*;
import static spark.debug.DebugScreen.enableDebugScreen;

public class Main {
    protected static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        staticFileLocation("/public");

        before((request, response) -> {
            if (request.session().attribute("username") == null)
                UserController.loginProcess(request, response);

            Pattern pattern = Pattern.compile("(/css|/js|/img|/file)");
            Matcher matcher = pattern.matcher(request.uri());

            if(!matcher.find()) {
                if (request.uri().equals("/")) {
                    if (request.session().attribute("username") != null)
                        response.redirect("/home");
                }
                else {
                    Flash.delete(request);

                    if (request.session().attribute("username") == null)
                        response.redirect("/");
                }
            }
        });

        /*exception(RuntimeException.class, (e, request, response) -> {
            response.status(500);
            response.body("Server error");
            logger.fatal(e.toString());
        });

        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404);
            response.body("Page not found");
            logger.fatal(e.toString());
        });*/

        get("/", UserController::login, new View());
        post("/", UserController::loginProcess);

        get("/home", IndexController::home, new View());
        post("/home", IndexController::home, new View());

        post("/logout", UserController::logout);
        get("/user", UserController::updateForm, new View());
        post("/user", UserController::update, new View());

        get("/vehicles", VehicleController::home, new View());
        get("/vehicles/create", VehicleController::createForm, new View());
        post("/vehicles/create", VehicleController::create, new View());
        get("/vehicles/update/:id", VehicleController::updateForm, new View());
        post("/vehicles/update/:id", VehicleController::update, new View());
        get("/vehicles/delete/:id", VehicleController::delete);
        get("/vehicles/:id", VehicleController::vehicle, new View());

        get("/vehicles/specimens/create/:id", SpecimenController::createForm, new View());
        post("/vehicles/specimens/create/:id", SpecimenController::create, new View());
        get("/vehicles/specimens/update/:id", SpecimenController::updateForm, new View());
        post("/vehicles/specimens/update/:id", SpecimenController::update, new View());
        get("/vehicles/specimens/delete/:id", SpecimenController::delete);

        get("/borrowers", BorrowerController::home, new View());
        get("/borrowers/create", BorrowerController::createForm, new View());
        post("/borrowers/create", BorrowerController::create, new View());
        get("/borrowers/update/:id", BorrowerController::updateForm, new View());
        post("/borrowers/update/:id", BorrowerController::update, new View());
        get("/borrowers/delete/:id", BorrowerController::delete);

        get("/rentals", RentalController::home, new View());
        post("/rentals", RentalController::home, new View());
        get("/rentals/create", RentalController::createForm, new View());
        post("/rentals/create", RentalController::create, new View());
        get("/rentals/update/:id", RentalController::updateForm, new View());
        post("/rentals/update/:id", RentalController::update, new View());
        get("/rentals/back/:id", RentalController::backForm, new View());
        post("/rentals/back/:id", RentalController::back, new View());
        get("/rentals/delete/:id", RentalController::delete);
        get("/rentals/histogram/kilometers.png", RentalController::histogramKilometers);
        get("/rentals/histogram/bills.png", RentalController::histogramBill);
        get("/rentals/:id", RentalController::rental, new View());

        enableDebugScreen();
    }
}