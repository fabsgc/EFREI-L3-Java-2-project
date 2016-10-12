package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vehicle extends Model {
    protected int id;
    protected int type;
    protected int priceDay;
    protected String brand;
    private List<Integer> specimenList = new ArrayList<>();

    public int getId() { return id; }
    public int getType() { return type; }
    public String getBrand() {
        return brand;
    }
    public int getPriceDay() { return priceDay; }
    public List<Integer> getSpecimenList() { return specimenList; }

    public void setId(int id) { this.id = id;}
    public void setType(int type) { this.type = type; }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setPriceDay(int priceDay) { this.priceDay = priceDay; }
    public void setSpecimenList(List<Integer> specimenList) {
        this.specimenList = specimenList;
    }

    /**
     * @param id int
     * @return Vehicle
     */

    public static Vehicle hydrate(int id){
        Vehicle vehicle = new Vehicle();
        Map<String,Object> data;
        String sql = "SELECT * FROM vehicle WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                if((int)data.get("type") == 1){
                    return Car.hydrate(data);
                }
                else{
                    return Bike.hydrate(data);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                User.logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return vehicle;
    }

    /**
     * @return List
     */

    public static List<Vehicle> findAll(){
        List<Vehicle> data = new ArrayList<>();

        String sql = "SELECT id FROM vehicle";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results)
                data.add(Vehicle.hydrate((int)result.get("id")));
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return data;
    }

    /**
     * @return List
     */

    public static List<Vehicle> findSpecimensAvailable(){
        List<Vehicle> data = new ArrayList<>();

        String sql = "SELECT id, (SELECT COUNT(*) FROM specimen WHERE specimen.vehicle = vehicle.id AND rental = null) AS specimens FROM vehicle WHERE specimens = 0";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results)
                data.add(Vehicle.hydrate((int)result.get("id")));
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return data;
    }

    /**
     * @param brand String
     * @param showBike String
     * @param showCar String
     * @param showCarLuxury String
     * @return List
     */

    public static List<Vehicle> findByBrand(String brand , String showBike, String showCar, String showCarLuxury){
        List<Vehicle> data = new ArrayList<>();

        String sql = "SELECT id FROM vehicle WHERE ";
        String sqlOption = "";

        if(!brand.equals(""))
            sqlOption += "brand = '" + brand + "' ";

        if(showBike.equals("0")){
            if(sqlOption.equals(""))
                sqlOption += "type != 2 ";
            else
                sqlOption += "AND type != 2 ";
        }

        if(showCar.equals("0")){
            if(sqlOption.equals(""))
                sqlOption += "((type != 1 AND luxury != 0) OR (type = 2 OR (type = 1 AND luxury = 1))) ";
            else
                sqlOption += "AND ((type != 1 AND luxury != 0) (type = 2 OR (type = 1 AND luxury = 1))) ";
        }

        if(showCarLuxury.equals("0")){
            if(sqlOption.equals(""))
                sqlOption += "(luxury != 1) ";
            else
                sqlOption += "AND (luxury != 1) ";
        }

        if(sqlOption.equals(""))
            sql += "1=1";
        else
            sql += sqlOption;

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results)
                data.add(Vehicle.hydrate((int)result.get("id")));
        }

        return data;
    }

    /**
     * @return List
     */

    public static List<Map<String,Object>> findBrands(){
        List<Map<String,Object>> brandList = new ArrayList<>();

        String sql = "SELECT DISTINCT(brand) FROM vehicle";

        try(Connection con = Database.getDao().open()) {
            brandList = con.createQuery(sql)
                    .executeAndFetchTable().asList();
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return brandList;
    }

    public void delete(){
        String sql = "DELETE FROM specimen WHERE vehicle = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        sql = "DELETE FROM vehicle WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }
}