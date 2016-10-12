package com.location.model;

import com.location.core.dao.Database;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.List;
import java.util.Map;

public class Car extends Vehicle {
    private String model;
    private int luxury;

    public String getModel() { return model; }
    public int getLuxury() { return luxury; }
    public void setModel(String model) { this.model = model; }
    public void setLuxury(int luxury) { this.luxury = luxury; }

    public void insert(){
        String sql = "INSERT INTO vehicle(type, priceday, brand, model, cylinder, luxury) " +
                "VALUES(:type, :priceday, :brand, :model, 0, :luxury)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("type", this.type)
                    .addParameter("priceday", this.priceDay)
                    .addParameter("brand", this.brand)
                    .addParameter("model", this.model)
                    .addParameter("luxury", this.luxury)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE vehicle SET type = :type, priceday = :priceday, brand = :brand, model = :model, " +
                "cylinder = 0, luxury = :luxury WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("id", this.id)
                    .addParameter("type", this.type)
                    .addParameter("priceday", this.priceDay)
                    .addParameter("brand", this.brand)
                    .addParameter("model", this.model)
                    .addParameter("luxury", this.luxury)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    /**
     * @param id int
     * @return Car
     */

    public static Car hydrate(int id){
        Map<String,Object> data;
        Car car = new Car();
        String sql = "SELECT * FROM vehicle WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String,Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                car.setId((int) data.get("id"));
                car.setType((int) data.get("type"));
                car.setPriceDay((int) data.get("priceday"));
                car.setBrand((String) data.get("brand"));
                car.setModel((String) data.get("model"));
                car.setLuxury((int) data.get("luxury"));
                car.setSpecimenList(Specimen.findIdByVehicle(car.getId()));
            }
            catch (ClassCastException | NullPointerException e1){
                User.logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return car;
    }

    /**
     * @param data Map
     * @return Car
     */

    public static Car hydrate(Map<String,Object> data){
        Car car = new Car();

        try {
            car.setId((int) data.get("id"));
            car.setType((int) data.get("type"));
            car.setPriceDay((int) data.get("priceday"));
            car.setBrand((String) data.get("brand"));
            car.setModel((String) data.get("model"));
            car.setLuxury((int) data.get("luxury"));
            car.setSpecimenList(Specimen.findIdByVehicle(car.getId()));
        }
        catch (ClassCastException | NullPointerException e1){
            User.logger.debug(e1.toString());
        }

        return car;
    }
}
