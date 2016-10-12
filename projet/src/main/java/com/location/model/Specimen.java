package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.*;

public class Specimen extends Model {
    private int id;
    private int fuel;
    private int state;
    private int kilometers;
    private Vehicle vehicle;
    private Rental rental;
    private String registrationPlate;

    public int getId() {
        return id;
    }
    public int getFuel() { return fuel; }
    public int getState() { return state; }
    public int getKilometers() { return kilometers; }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public Rental getRental() { return rental; }
    public String getRegistrationPlate() { return registrationPlate; }

    public void setId(int id) { this.id = id; }
    public void setFuel(int fuel) { this.fuel = fuel; }
    public void setState(int state) { this.state = state; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public void setRental(Rental rental) { this.rental = rental; }
    public void setKilometers(int kilometers) { this.kilometers = kilometers; }
    public void setRegistrationPlate(String registrationPlate) { this.registrationPlate = registrationPlate; }

    public void insert(){
        String sql = "INSERT INTO specimen(vehicle, rental, fuel, state, kilometers, registrationplate) " +
                "VALUES(:vehicle, :rental, :fuel, :state, :kilometers, :registrationplate)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("vehicle", this.vehicle.getId())
                    .addParameter("rental", (this.rental != null) ? this.rental.getId() : null)
                    .addParameter("fuel", this.fuel)
                    .addParameter("state", this.state)
                    .addParameter("kilometers", this.kilometers)
                    .addParameter("registrationplate", this.registrationPlate)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE specimen SET vehicle = :vehicle, rental = :rental, fuel = :fuel, " +
                "state = :state, kilometers = :kilometers, registrationplate = :registrationplate " +
                "WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("id", this.id)
                    .addParameter("vehicle", this.vehicle.getId())
                    .addParameter("rental", (this.rental != null) ? this.rental.getId() : null)
                    .addParameter("fuel", this.fuel)
                    .addParameter("state", this.state)
                    .addParameter("kilometers", this.kilometers)
                    .addParameter("registrationplate", this.registrationPlate)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void delete(){
        String sql = "DELETE FROM specimen WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    /**
     *
     * @param id int
     * @return Specimen
     */

    public static Specimen hydrate(int id){
        return Specimen.hydrate(id, true);
    }

    /**
     *
     * @param data Map
     * @return Specimen
     */

    public static Specimen hydrate(Map<String,Object> data){
        Specimen specimen = new Specimen();

        specimen.setId((int) data.get("id"));
        specimen.setVehicle(Vehicle.hydrate((int)data.get("vehicle")));
        specimen.setFuel((int) data.get("fuel"));
        specimen.setState((int) data.get("state"));
        specimen.setKilometers((int) data.get("kilometers"));
        specimen.setRegistrationPlate((String) data.get("registrationplate"));

        return specimen;
    }

    /**
     *
     * @param id int
     * @param foreign Boolean
     * @return Specimen
     */

    public static Specimen hydrate(int id, Boolean foreign){
        Map<String,Object> data;
        Specimen specimen = new Specimen();

        String sql = "SELECT * FROM specimen WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                specimen.setId((int) data.get("id"));
                if(foreign) specimen.setVehicle(Vehicle.hydrate((int)data.get("vehicle")));
                if(foreign) specimen.setRental(Rental.hydrate((data.get("rental") != null) ? (int)data.get("rental") : 0));
                specimen.setFuel((int) data.get("fuel"));
                specimen.setState((int) data.get("state"));
                specimen.setKilometers((int) data.get("kilometers"));
                specimen.setRegistrationPlate((String) data.get("registrationplate"));
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimen;
    }

    /**
     *
     * @return List
     */

    public static List<Specimen> findAllWithoutForeign(){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int rental = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(rental, false));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     *
     * @return List
     */

    public static List<Specimen> findAll(){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int rental = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(rental));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     *
     * @return List
     */

    public static List<Specimen> findNoRentals(){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen WHERE rental IS NULL";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    specimenList.add(Specimen.hydrate(data));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     *
     * @param id int
     * @return List
     */

    public static List<Specimen> findNoRentals(int id){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT id FROM specimen WHERE rental IS NULL OR rental = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int vehicle = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(vehicle));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @return List
     */

    public static List<Integer> findIdNoRentals(){
        List<Map<String,Object>> dataList;
        List<Integer> specimenList = new ArrayList<>();

        String sql = "SELECT id FROM specimen WHERE rental IS NULL";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int vehicle = (int) data.get("id");
                    specimenList.add(vehicle);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return list
     */

    public static List<Integer> findIdNoRentals(int id){
        List<Map<String,Object>> dataList;
        List<Integer> specimenList = new ArrayList<>();

        String sql = "SELECT id FROM specimen WHERE rental IS NULL OR rental = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int vehicle = (int) data.get("id");
                    specimenList.add(vehicle);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @param kilometer String
     * @return List
     */

    public static List<Specimen> findByVehicleKilometers(int id, String kilometer){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();
        String[] kilometers = kilometer.split("\\.");
        String sql;

        if(!kilometer.equals("")){
            sql = "SELECT * FROM specimen WHERE vehicle = :id AND kilometers >= :kilometer1 AND kilometers <= :kilometer2";
        }
        else{
            sql = "SELECT * FROM specimen WHERE vehicle = :id";
        }

        try(Connection con = Database.getDao().open()) {
            if (!kilometer.equals("")){
                dataList = con.createQuery(sql)
                        .addParameter("id", id)
                        .addParameter("kilometer1", Integer.parseInt(kilometers[0]))
                        .addParameter("kilometer2", Integer.parseInt(kilometers[1]))
                        .executeAndFetchTable().asList();
            }
            else{
                dataList = con.createQuery(sql)
                        .addParameter("id", id)
                        .executeAndFetchTable().asList();
            }

            try {
                for(Map<String,Object> data: dataList){
                    int vehicle = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(vehicle));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return List
     */

    public static List<Integer> findIdByVehicle(int id){
        List<Map<String,Object>> dataList;
        List<Integer> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen WHERE vehicle = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int vehicle = (int) data.get("id");
                    specimenList.add(vehicle);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return List
     */

    public static List<Specimen> findByRental(int id){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen WHERE rental = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int rental = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(rental));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return List
     */

    public static List<Integer> findIdByRental(int id){
        List<Map<String,Object>> dataList;
        List<Integer> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen WHERE rental = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int rental = (int) data.get("id");
                    specimenList.add(rental);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return List
     */

    public static List<Specimen> findByBorrower(int id){
        List<Map<String,Object>> dataList;
        List<Specimen> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen " +
                "INNER JOIN rental ON specimen.rental = rental.id " +
                "INNER JOIN borrower ON rental.borrower = borrower.id " +
                "WHERE borrower.id = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int borrower = (int) data.get("id");
                    specimenList.add(Specimen.hydrate(borrower));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @return List
     */

    public static List<Integer> findIdByBorrower(int id){
        List<Map<String,Object>> dataList;
        List<Integer> specimenList = new ArrayList<>();

        String sql = "SELECT * FROM specimen " +
                "INNER JOIN rental ON specimen.rental = rental.id " +
                "INNER JOIN borrower ON rental.borrower = borrower.id " +
                "WHERE borrower.id = :id";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int borrower = (int) data.get("id");
                    specimenList.add(borrower);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return specimenList;
    }

    /**
     * @param id int
     * @param specimenList List
     */

    public static void setNewRentals(int id, List<String> specimenList){
        String sql = "UPDATE specimen SET rental = :id, fuel = 100, state = 1 WHERE id IN(";
        String sqlOption = "";

        Iterator<String> it;
        for (it = specimenList.iterator(); it.hasNext();) {
            sqlOption += it.next();

            if (it.hasNext()) {
                sqlOption += ", ";
            }
        }

        sql += sqlOption + ")";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("id", id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    /**
     * @param id int
     */

    public static void setOldRentals(int id){
        String sql = "UPDATE specimen SET rental = NULL WHERE rental = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("id", id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }
}