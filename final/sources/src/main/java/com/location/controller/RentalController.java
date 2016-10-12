package com.location.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.location.core.Exception.NotFoundException;
import com.location.core.form.Field;
import com.location.core.form.Form;
import com.location.core.mvc.Controller;
import com.location.model.Bill;
import com.location.model.Borrower;
import com.location.model.Rental;
import com.location.model.Specimen;
import com.location.util.Flash;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import javax.servlet.ServletOutputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RentalController extends Controller {

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView home(Request request, Response response){
        String search = request.queryMap("search").value() != null ? request.queryMap("search").value() : "";

        float averageKilometer = 0;
        float averageBill = 0;
        float medianKilometer = 0;
        float medianBill = 0;
        float standardKilometer = 0;
        float standardBill = 0;

        /* ################################################### */

        List<Specimen> specimenList = Specimen.findAllWithoutForeign();

        for(Specimen specimen : specimenList) averageKilometer += (float) specimen.getKilometers();
        averageKilometer /= specimenList.size();

        if(specimenList.size() % 2 == 0) medianKilometer = specimenList.get(specimenList.size()/2).getKilometers();
        else medianKilometer = specimenList.get((specimenList.size()+1)/2 - 1).getKilometers();

        for(Specimen specimen : specimenList) standardKilometer += (specimen.getKilometers() - averageKilometer) * (specimen.getKilometers() - averageKilometer);

        standardKilometer = (float)Math.sqrt((standardKilometer / (float)specimenList.size()));

        /* ################################################### */

        List<Bill> billList = Bill.findAll();

        for(Bill bill : billList) averageBill += bill.getPrice();
        averageBill /= billList.size();

        if(billList.size() % 2 == 0) medianBill = billList.get(billList.size()/2).getPrice();
        else medianBill = billList.get((billList.size()+1)/2 - 1).getPrice();

        for(Bill bill : billList) standardBill += (bill.getPrice() - averageBill) * (bill.getPrice() - averageBill);

        standardBill = (float) Math.sqrt((standardBill / (float)billList.size()));

        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Emprunter");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); }});
        vars.put("search", search);
        vars.put("rentalsCurrent", Rental.findAllCurrent(search));
        vars.put("rentalsEnded", Rental.findAllEnded(search));
        vars.put("dateTool", new DateTool());
        vars.put("averageKilometer", averageKilometer);
        vars.put("averageBill", averageBill);
        vars.put("medianKilometer", medianKilometer);
        vars.put("medianBill", medianBill);
        vars.put("standardKilometer", standardKilometer);
        vars.put("standardBill", standardBill);

        return new ModelAndView(vars, "templates/Rental/home.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView createForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        data.put("borrower", "0");
        data.put("date", "");
        data.put("insurance", "1");
        data.put("specimens", new ArrayList<>());

        vars.put("title", "Nouvelle location");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("specimens", Specimen.findNoRentals());
        vars.put("borrowers", Borrower.findAll());
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Nouvelle location"); }});
        vars.put("dateTool", new DateTool());

        return new ModelAndView(vars, "templates/Rental/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     * @throws ParseException
     */

    public static ModelAndView create(Request request, Response response) throws ParseException {
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();
        List<String> specimenList = new ArrayList<>();
        List<Specimen> specimensNoRentals = Specimen.findNoRentals();

        try{
            specimenList = new ArrayList<>(Arrays.asList(request.queryMap("specimens").values()));
        }
        catch (NullPointerException e1){
            logger.debug(e1 + "liste vide");
        }

        data.put("borrower", request.queryMap("borrower").value());
        data.put("date", request.queryMap("date").value());
        data.put("insurance", request.queryMap("insurance").value());
        data.put("specimens", specimenList);

        vars.put("title", "Nouvelle location");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("specimens", specimensNoRentals);
        vars.put("borrowers", Borrower.findAll());
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Nouvelle location"); }});
        vars.put("dateTool", new DateTool());

        Boolean dateValid = true;
        Boolean specimensValid = true;
        Date date1 = new Date();
        Date date2 = new Date();

        {
            String[] dates;

            if(!request.queryMap("date").value().equals("")){
                dates = request.queryMap("date").value().split(" - ");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                date1 = formatter.parse(dates[0]);
                date2 = formatter.parse(dates[1]);

                if(date2.before(date1)){
                    dateValid = false;
                }
            }
            else{
                dateValid = false;
            }
        }

        {
            if(specimenList.size() > 0) {
                List<Integer> specimenIdList = Specimen.findIdNoRentals();

                for (String specimen : specimenList) {
                    if (!specimenIdList.contains(Integer.parseInt(specimen))) {
                        specimensValid = false;
                        break;
                    }
                }
            }
            else{
                specimensValid = false;
            }
        }

        Form form = new Form("rental", request);
        form.text("borrower").add(Field.SQL, "SELECT COUNT(*) FROM borrower WHERE id = :id", "==", 1, new HashMap<String, Object>(){{ put("id", Integer.parseInt((String)data.get("borrower"))); }}, "L'emprunteur n'existe pas");
        form.text("date").add(Field.ASSERT, dateValid, "Les dates ne sont pas valides");
        form.text("insurance").add(Field.REGEX, "^(1|0)$", "Vous devez préciser si l'assurance est activée ou non");
        form.text("specimens").add(Field.ASSERT, specimensValid, "Certains véhicules sélectionnés ne sont pas disponibles");

        if(form.send()) {
            if (form.check()) {
                Rental rental = new Rental();
                rental.setBorrower(Borrower.hydrate(Integer.parseInt((String)data.get("borrower"))));
                rental.setBegin(date1);
                rental.setEnd(date2);
                rental.setEnded(false);

                if(data.get("insurance").equals("1"))
                    rental.setInsurance(true);
                else
                    rental.setInsurance(false);

                rental.insert();

                Specimen.setNewRentals(rental.getId(), specimenList);

                Flash.message(request, "La location a bien été créée");
                response.redirect("/rentals");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Rental/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView updateForm(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));

        if(rental == null) throw new NotFoundException();

        List<String> newSpecimens = rental.getSpecimenList().stream().map(String::valueOf).collect(Collectors.toList());

        data.put("borrower", String.valueOf(rental.getBorrower().getId()));
        data.put("date", formatter.format(rental.getBegin()) + " - " + formatter.format(rental.getEnd()));
        data.put("insurance", rental.getInsurance() ? "1" : "0");
        data.put("specimens", newSpecimens);

        vars.put("title", "Modifier une location");
        vars.put("data", data);
        vars.put("request", request);
        vars.put("response", response);
        vars.put("specimens", Specimen.findNoRentals(rental.getId()));
        vars.put("borrowers", Borrower.findAll());
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Modifier une location"); add("#" + rental.getId()); }});

        return new ModelAndView(vars, "templates/Rental/update.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     * @throws ParseException
     */

    public static ModelAndView update(Request request, Response response) throws ParseException {
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        List<String> specimenList = new ArrayList<>();
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));

        if(rental == null) throw new NotFoundException();

        List<Specimen> specimensNoRentals = Specimen.findNoRentals(rental.getId());

        try{
            specimenList = new ArrayList<>(Arrays.asList(request.queryMap("specimens").values()));
        }
        catch (NullPointerException e1){
            logger.debug(e1 + "liste vide");
        }

        data.put("borrower", request.queryMap("borrower").value());
        data.put("date", request.queryMap("date").value());
        data.put("insurance", request.queryMap("insurance").value());
        data.put("specimens", specimenList);

        vars.put("title", "Modifier une location");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("specimens", specimensNoRentals);
        vars.put("borrowers", Borrower.findAll());
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Modifier une location"); add("#" + rental.getId()); }});

        Boolean dateValid = true;
        Boolean specimensValid = true;
        Date date1 = new Date();
        Date date2 = new Date();

        {
            String[] dates;

            if(!request.queryMap("date").value().equals("")){
                dates = request.queryMap("date").value().split(" - ");

                date1 = formatter.parse(dates[0]);
                date2 = formatter.parse(dates[1]);

                if(date2.before(date1)){
                    dateValid = false;
                }
            }
            else{
                dateValid = false;
            }
        }

        {
            if(specimenList.size() > 0) {
                List<Integer> specimenIdList = Specimen.findIdNoRentals(rental.getId());

                for (String specimen : specimenList) {
                    if (!specimenIdList.contains(Integer.parseInt(specimen))) {
                        specimensValid = false;
                        break;
                    }
                }
            }
            else{
                specimensValid = false;
            }
        }

        Form form = new Form("rental", request);
        form.text("borrower").add(Field.SQL, "SELECT COUNT(*) FROM borrower WHERE id = :id", "==", 1, new HashMap<String, Object>(){{ put("id", Integer.parseInt((String)data.get("borrower"))); }}, "L'emprunteur n'existe pas");
        form.text("date").add(Field.ASSERT, dateValid, "Les dates ne sont pas valides");
        form.text("insurance").add(Field.REGEX, "^(1|0)$", "Vous devez préciser si l'assurance est activée ou non");
        form.text("specimens").add(Field.ASSERT, specimensValid, "Certains véhicules sélectionnés ne sont pas disponibles");

        if(form.send()) {
            if (form.check()) {
                rental.setBorrower(Borrower.hydrate(Integer.parseInt((String)data.get("borrower"))));
                rental.setBegin(date1);
                rental.setEnd(date2);
                rental.setEnded(false);

                if(data.get("insurance").equals("1"))
                    rental.setInsurance(true);
                else
                    rental.setInsurance(false);

                rental.update();

                Specimen.setOldRentals(rental.getId());
                Specimen.setNewRentals(rental.getId(), specimenList);

                Flash.message(request, "La location a bien été modifiée");
                response.redirect("/rentals");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("data", data);
        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Rental/create.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView backForm(Request request, Response response) {
        Map<String, Object> vars = new HashMap<>();
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));

        if(rental == null) throw new NotFoundException();

        List<Specimen> specimenList = Specimen.findByRental(rental.getId());

        vars.put("title", "Retour");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("rental", rental);
        vars.put("specimens", specimenList);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Retour"); add("#" + rental.getId()); }});

        return new ModelAndView(vars, "templates/Rental/back.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     * @throws IOException
     * @throws DocumentException
     */

    public static ModelAndView back(Request request, Response response) throws IOException, DocumentException {
        Map<String, Object> vars = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));

        if(rental == null) throw new NotFoundException();

        List<Specimen> specimenList = Specimen.findByRental(rental.getId());
        int cpt = 1;

        Form form = new Form("rental", request);

        for(Specimen specimen : specimenList){
            if(request.queryMap("state-" + String.valueOf(specimen.getId())) != null && request.queryMap("fuel-" + String.valueOf(specimen.getId())) != null && request.queryMap("kilometers-" + String.valueOf(specimen.getId())) != null){
                form.text("fuel-" + String.valueOf(specimen.getId())).add(Field.REGEX, "^(\\d+)$", "Vous devez indiquer un niveau de carburant pour le véhicule");
                form.text("state" + String.valueOf(specimen.getId())).add(Field.REGEX, "$(0|1)^", "Vous devez préciser l'état du véhicule");
                form.text("kilometers" + String.valueOf(specimen.getId())).add(Field.REGEX, "$(\\d+)^", "Veuillez indiquer le nombre de kilomètres parcourus par le véhicule");
            }
            else{
                form.text("fuel-" + String.valueOf(specimen.getId())).add(Field.ASSERT, false, "Veuillez donner le niveau de carburant du véhicule " + String.valueOf(cpt));
                form.text("state" + String.valueOf(specimen.getId())).add(Field.ASSERT, false, "Veuillez indiquer l'état du véhicule " + String.valueOf(cpt));
                form.text("kilometers" + String.valueOf(specimen.getId())).add(Field.ASSERT, false, "Veuillez indiquer le nombre de kilomètres parcourus par le véhicule " + String.valueOf(cpt));
            }

            cpt++;
        }

        vars.put("title", "Retour");
        vars.put("request", request);
        vars.put("response", response);
        vars.put("rental", rental);
        vars.put("specimens", specimenList);
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Retour"); add("#" + rental.getId()); }});

        if(form.send()) {
            if (form.check()) {
                Document document = new Document(PageSize.A4, 20, 20, 20, 20);
                PdfWriter.getInstance(document, new FileOutputStream("bill-" + String.valueOf(rental.getId()) + ".pdf"));
                File file = new File("bill-" + String.valueOf(rental.getId()) + ".txt");
                FileWriter fileWriter = new FileWriter(file);
                String separator = System.getProperty("line.separator");

                document.open();

                Font fontTitleH1 = new Font(Font.FontFamily.HELVETICA, 22, Font.NORMAL);
                Font fontTitleH2 = new Font(Font.FontFamily.HELVETICA, 17, Font.NORMAL);
                Font fontContent = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL);

                Paragraph paragraphTitle = new Paragraph("Facture location #" + String.valueOf(rental.getId()), fontTitleH1);
                paragraphTitle.setSpacingAfter(20f);
                document.add(paragraphTitle);
                fileWriter.write("Facture location #" + String.valueOf(rental.getId()) + separator + separator);

                /* ############################################################################# */

                Paragraph paragraphSpecimens = new Paragraph("Liste des véhicules loués", fontTitleH2);
                paragraphSpecimens.setSpacingAfter(15f);
                document.add(paragraphSpecimens);

                fileWriter.write("Liste des véhicules loués" + separator + separator);

                PdfPTable tableSpecimens = new PdfPTable(5);
                tableSpecimens.setWidthPercentage(100);

                PdfPCell tableSpecimensHeaderCells[];
                tableSpecimensHeaderCells = new PdfPCell[5];

                tableSpecimensHeaderCells[0] = new PdfPCell(new Phrase("Marque", fontContent));
                tableSpecimensHeaderCells[0].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableSpecimensHeaderCells[0].setPadding(3.0f);
                tableSpecimensHeaderCells[0].setBackgroundColor(new BaseColor(180,180,180));
                tableSpecimens.addCell(tableSpecimensHeaderCells[0]);

                tableSpecimensHeaderCells[1] = new PdfPCell(new Phrase("Etat", fontContent));
                tableSpecimensHeaderCells[1].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableSpecimensHeaderCells[1].setPadding(3.0f);
                tableSpecimensHeaderCells[1].setBackgroundColor(new BaseColor(180,180,180));
                tableSpecimens.addCell(tableSpecimensHeaderCells[1]);

                tableSpecimensHeaderCells[2] = new PdfPCell(new Phrase("Carburant utilisé", fontContent));
                tableSpecimensHeaderCells[2].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableSpecimensHeaderCells[2].setPadding(3.0f);
                tableSpecimensHeaderCells[2].setBackgroundColor(new BaseColor(180,180,180));
                tableSpecimens.addCell(tableSpecimensHeaderCells[2]);

                tableSpecimensHeaderCells[3] = new PdfPCell(new Phrase("Kilomètres parcourus", fontContent));
                tableSpecimensHeaderCells[3].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableSpecimensHeaderCells[3].setPadding(3.0f);
                tableSpecimensHeaderCells[3].setBackgroundColor(new BaseColor(180,180,180));
                tableSpecimens.addCell(tableSpecimensHeaderCells[3]);

                tableSpecimensHeaderCells[4] = new PdfPCell(new Phrase("Prix/jour", fontContent));
                tableSpecimensHeaderCells[4].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableSpecimensHeaderCells[4].setPadding(3.0f);
                tableSpecimensHeaderCells[4].setBackgroundColor(new BaseColor(180,180,180));
                tableSpecimens.addCell(tableSpecimensHeaderCells[4]);

                for(Specimen specimen : specimenList){
                    PdfPCell tableSpecimensContentCells[];
                    tableSpecimensContentCells = new PdfPCell[5];

                    tableSpecimensContentCells[0] = new PdfPCell(new Phrase(specimen.getVehicle().getBrand(), fontContent));
                    tableSpecimensContentCells[0].setPadding(3.0f);
                    tableSpecimens.addCell(tableSpecimensContentCells[0]);
                    fileWriter.write("- Marque : " + specimen.getVehicle().getBrand() + "\n");

                    tableSpecimensContentCells[1] = new PdfPCell(new Phrase(specimen.getState() == 1 ? "Bon état" : "Mauvais état", fontContent));
                    tableSpecimensContentCells[1].setPadding(3.0f);
                    tableSpecimens.addCell(tableSpecimensContentCells[1]);
                    fileWriter.write("- Etat : " + (request.queryMap().get("fuel-" + String.valueOf(specimen.getId())).value().equals("1s") ? "Bon état" : "Mauvais état") + separator);

                    tableSpecimensContentCells[2] = new PdfPCell(new Phrase(String.valueOf(specimen.getFuel() - Integer.parseInt(request.queryMap().get("fuel-" + String.valueOf(specimen.getId())).value())) + "%", fontContent));
                    tableSpecimensContentCells[2].setPadding(3.0f);
                    tableSpecimens.addCell(tableSpecimensContentCells[2]);
                    fileWriter.write("- Carburant : " + String.valueOf(specimen.getFuel() - Integer.parseInt(request.queryMap().get("fuel-" + String.valueOf(specimen.getId())).value())) + "%" + separator);

                    tableSpecimensContentCells[3] = new PdfPCell(new Phrase(request.queryMap().get("kilometers-" + String.valueOf(specimen.getId())).value() + "km", fontContent));
                    tableSpecimensContentCells[3].setPadding(3.0f);
                    tableSpecimens.addCell(tableSpecimensContentCells[3]);
                    fileWriter.write("- Kilomètres parcourus : " + request.queryMap().get("kilometers-" + String.valueOf(specimen.getId())).value() + "km" + separator);

                    tableSpecimensContentCells[4] = new PdfPCell(new Phrase(String.valueOf(specimen.getVehicle().getPriceDay()) + "€", fontContent));
                    tableSpecimensContentCells[4].setPadding(3.0f);
                    tableSpecimens.addCell(tableSpecimensContentCells[4]);
                    fileWriter.write("- Prix/jour : " + String.valueOf(specimen.getVehicle().getPriceDay()) + "€" + separator + separator);
                }

                document.add(tableSpecimens);

                /* ############################################################################# */

                float price = 0;
                int diff = (int) (new Date().getTime() - rental.getBegin().getTime());
                int days = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;

                for(Specimen specimen : specimenList){
                    int km = Integer.parseInt(request.queryMap().get("kilometers-" + String.valueOf(specimen.getId())).value());

                    if(km < 50000){
                        price += days * specimen.getVehicle().getPriceDay();
                    }
                    else{
                        price += days * specimen.getVehicle().getPriceDay() * (1f - (km / 50000)/10f);
                    }
                }

                if(price < 0){
                    price = 0;
                }

                Paragraph paragraphToPay = new Paragraph("Prix de la location", fontTitleH2);
                paragraphToPay.setSpacingAfter(15f);
                document.add(paragraphToPay);
                fileWriter.write("- Prix de la location : " + String.valueOf(price) + "€" + separator + separator);

                PdfPTable tableToPay = new PdfPTable(1);
                tableToPay.setWidthPercentage(100);

                PdfPCell tableToPayCell = new PdfPCell(new Phrase(String.valueOf(price) + "€", fontContent));
                tableToPayCell.setPadding(3.0f);
                tableToPay.addCell(tableToPayCell);

                document.add(tableToPay);

                /* ############################################################################# */

                float priceInsurance = 0;
                float priceState = 0;
                float priceLate = 0;
                float priceFuel = 0;

                if(rental.getInsurance()){
                    priceInsurance = 25 * specimenList.size();
                }

                if(!DateUtils.isSameDay(rental.getEnd(), new Date()) && rental.getEnd().compareTo(new Date()) < 0){
                    priceLate = 50 * specimenList.size();
                }

                for(Specimen specimen : specimenList){
                    if(request.queryMap().get("state-" + String.valueOf(specimen.getId())).value().equals("0")){
                        if(rental.getInsurance()){
                            priceState += 112;
                        }
                        else{
                            priceState += 223;
                        }
                    }

                    priceFuel += specimen.getFuel() - Integer.parseInt(request.queryMap().get("fuel-" + String.valueOf(specimen.getId())).value());
                }

                Paragraph paragraphToPayMore = new Paragraph("Majorations", fontTitleH2);
                paragraphToPayMore.setSpacingAfter(15f);
                document.add(paragraphToPayMore);
                fileWriter.write("Majorations" + separator + separator);

                PdfPTable tableToPayMore = new PdfPTable(4);
                tableToPayMore.setWidthPercentage(100);

                PdfPCell tableToPayMoreHeaderCells[];
                tableToPayMoreHeaderCells = new PdfPCell[8];

                tableToPayMoreHeaderCells[0] = new PdfPCell(new Phrase("Assurance", fontContent));
                tableToPayMoreHeaderCells[0].setPadding(3.0f);
                tableToPayMoreHeaderCells[0].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableToPayMoreHeaderCells[0].setBackgroundColor(new BaseColor(180,180,180));
                tableToPayMore.addCell(tableToPayMoreHeaderCells[0]);

                tableToPayMoreHeaderCells[1] = new PdfPCell(new Phrase("Etat", fontContent));
                tableToPayMoreHeaderCells[1].setPadding(3.0f);
                tableToPayMoreHeaderCells[1].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableToPayMoreHeaderCells[1].setBackgroundColor(new BaseColor(180,180,180));
                tableToPayMore.addCell(tableToPayMoreHeaderCells[1]);

                tableToPayMoreHeaderCells[2] = new PdfPCell(new Phrase("Retard", fontContent));
                tableToPayMoreHeaderCells[2].setPadding(3.0f);
                tableToPayMoreHeaderCells[2].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableToPayMoreHeaderCells[2].setBackgroundColor(new BaseColor(180,180,180));
                tableToPayMore.addCell(tableToPayMoreHeaderCells[2]);

                tableToPayMoreHeaderCells[3] = new PdfPCell(new Phrase("Carburant", fontContent));
                tableToPayMoreHeaderCells[3].setPadding(3.0f);
                tableToPayMoreHeaderCells[3].setHorizontalAlignment(Element.ALIGN_CENTER);
                tableToPayMoreHeaderCells[3].setBackgroundColor(new BaseColor(180,180,180));
                tableToPayMore.addCell(tableToPayMoreHeaderCells[3]);

                tableToPayMoreHeaderCells[4] = new PdfPCell(new Phrase(String.valueOf(priceInsurance) + "€", fontContent));
                tableToPayMoreHeaderCells[4].setPadding(3.0f);
                tableToPayMore.addCell(tableToPayMoreHeaderCells[4]);
                fileWriter.write("- Assurance : " + String.valueOf(priceInsurance) + "€" + separator);

                tableToPayMoreHeaderCells[5] = new PdfPCell(new Phrase(String.valueOf(priceState) + "€", fontContent));
                tableToPayMoreHeaderCells[5].setPadding(3.0f);
                tableToPayMore.addCell(tableToPayMoreHeaderCells[5]);
                fileWriter.write("- Etat : " + String.valueOf(priceState) + "€" + separator);

                tableToPayMoreHeaderCells[6] = new PdfPCell(new Phrase(String.valueOf(priceLate) + "€", fontContent));
                tableToPayMoreHeaderCells[6].setPadding(3.0f);
                tableToPayMore.addCell(tableToPayMoreHeaderCells[6]);
                fileWriter.write("- Retard : " + String.valueOf(priceLate) + "€" + separator);

                tableToPayMoreHeaderCells[7] = new PdfPCell(new Phrase(String.valueOf(priceFuel) + "€", fontContent));
                tableToPayMoreHeaderCells[7].setPadding(3.0f);
                tableToPayMore.addCell(tableToPayMoreHeaderCells[7]);
                fileWriter.write("- Carburant : " + String.valueOf(priceFuel) + "€" + separator + separator);

                document.add(tableToPayMore);

                /* ############################################################################# */

                float priceTotal = price + priceInsurance + priceLate + priceState + priceFuel;

                Paragraph paragraphTotal = new Paragraph("Total", fontTitleH2);
                paragraphTotal.setSpacingAfter(15f);
                document.add(paragraphTotal);

                PdfPTable tableTotal = new PdfPTable(1);
                tableTotal.setWidthPercentage(100);
                PdfPCell tableTotalCell = new PdfPCell(new Phrase(String.valueOf(priceTotal) + "€", fontContent));
                tableTotal.addCell(tableTotalCell);
                document.add(tableTotal);
                fileWriter.write("- Total : " + String.valueOf(priceTotal) + "€" + separator);

                document.close();

                fileWriter.flush();
                fileWriter.close();

                Bill bill = new Bill();
                bill.setPrice(priceTotal);
                bill.setRental(rental);
                bill.insert();

                for(Specimen specimen : specimenList){
                    int state = Integer.parseInt(request.queryMap("state-" + String.valueOf(specimen.getId())).value());
                    int fuel = Integer.parseInt(request.queryMap("fuel-" + String.valueOf(specimen.getId())).value());
                    int kilometers = Integer.parseInt(request.queryMap("kilometers-" + String.valueOf(specimen.getId())).value());

                    specimen.setState(state);
                    specimen.setFuel(fuel);
                    specimen.setKilometers(specimen.getKilometers() + kilometers);
                    specimen.setRental(null);
                    specimen.update();
                }

                rental.setEnded(true);
                rental.update();

                File source = new File("bill-" + String.valueOf(rental.getId()) + ".pdf");
                File dest = new File("target\\classes\\public\\bill-" + String.valueOf(rental.getId()) + ".pdf");

                try {
                    FileUtils.copyFile(source, dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                source = new File("bill-" + String.valueOf(rental.getId()) + ".txt");
                dest = new File("target\\classes\\public\\bill-" + String.valueOf(rental.getId()) + ".txt");

                try {
                    FileUtils.copyFile(source, dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Flash.message(request, "La location a bien été terminée et la facture générée");
                response.redirect("/rentals");
            }
            else{
                errors = form.errors();
            }
        }

        vars.put("errors", errors);

        return new ModelAndView(vars, "templates/Rental/back.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return ModelAndView
     */

    public static ModelAndView rental(Request request, Response response){
        Map<String, Object> vars = new HashMap<>();
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));

        if(rental == null) throw new NotFoundException();

        vars.put("title", "Location #" + rental.getId());
        vars.put("request", request);
        vars.put("response", response);
        vars.put("rental", rental);
        vars.put("dateTool", new DateTool());
        vars.put("specimens", Specimen.findByRental(Integer.parseInt(request.params(":id"))));
        vars.put("filAriane", new ArrayList<String>(){{ add("Emprunter"); add("Location #" + rental.getId()); }});

        return new ModelAndView(vars, "templates/Rental/rental.vm");
    }

    /**
     * @param request Request
     * @param response Response
     * @return String
     */

    public static String delete(Request request, Response response){
        Rental rental = Rental.hydrate(Integer.parseInt(request.params(":id")));
        if(rental == null) throw new NotFoundException();
        rental.delete();
        Flash.message(request, "La location a bien été supprimée");

        response.redirect("/rentals");
        return "";
    }

    /**
     * @param request Request
     * @param response Response
     * @return String
     * @throws IOException
     */

    public static String histogramKilometers(Request request, Response response) throws IOException {
        final String k0_5000 = "[0, 5 000[";
        final String k5000_20000 = "[5 000, 20 000[";
        final String k20000_40000 = "[20 000, 40 000[";
        final String k40000_plus = "[40 000, et plus[";

        int n0_5000 = 0;
        int n5000_20000 = 0;
        int n20000_40000 = 0;
        int n40000_plus = 0;

        List<Specimen> specimenList = Specimen.findAllWithoutForeign();

        for(Specimen specimen : specimenList){
            int kilometers = specimen.getKilometers();

            if(kilometers < 5000){
                n0_5000++;
            }
            else if(kilometers < 20000){
                n5000_20000++;
            }
            else if(kilometers < 40000){
                n20000_40000++;
            }
            else{
                n40000_plus++;
            }
        }

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

        dataset.addValue( n0_5000 , k0_5000 , "");
        dataset.addValue( n5000_20000 , k5000_20000 , "");
        dataset.addValue( n20000_40000 , k20000_40000 , "");
        dataset.addValue( n40000_plus , k40000_plus , "");

        JFreeChart barChart = ChartFactory.createBarChart(
                "Kilométrages des véhicules",
                "Kilométrage", "Nombre de véhicules",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        int width = 480;
        int height = 300;

        BufferedImage img = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = img.createGraphics();
        barChart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();

        ServletOutputStream output = response.raw().getOutputStream();
        ChartUtilities.writeBufferedImageAsPNG(output, img, true, 9);

        return "";
    }

    /**
     * @param request Request
     * @param response Response
     * @return String
     * @throws IOException
     */

    public static String histogramBill(Request request, Response response) throws IOException {
        final String k0_1000 = "[0, 1 000[";
        final String k1000_5000 = "[1 000, 5 000[";
        final String k5000_50000 = "[5 000, 50 000[";
        final String k50000_plus = "[50 000, et plus[";

        int n0_1000 = 0;
        int n1000_5000 = 0;
        int n5000_50000 = 0;
        int n50000_plus = 0;

        List<Bill> billList = Bill.findAll();

        for(Bill bill : billList){
            float bills = bill.getPrice();

            if(bills < 1000){
                n0_1000++;
            }
            else if(bills < 5000){
                n1000_5000++;
            }
            else if(bills < 50000){
                n5000_50000++;
            }
            else{
                n50000_plus++;
            }
        }

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

        dataset.addValue( n0_1000 , k0_1000 , "");
        dataset.addValue( n1000_5000 , k1000_5000 , "");
        dataset.addValue( n5000_50000 , k5000_50000 , "");
        dataset.addValue( n50000_plus , k50000_plus , "");

        JFreeChart barChart = ChartFactory.createBarChart(
                "Montant des factures",
                "Montant", "Nombre de factures",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        int width = 480;
        int height = 300;

        BufferedImage img = new BufferedImage(width , height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = img.createGraphics();
        barChart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();

        ServletOutputStream output = response.raw().getOutputStream();
        ChartUtilities.writeBufferedImageAsPNG(output, img, true, 9);

        return "";
    }
}