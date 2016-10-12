package com.location.model;

import com.location.core.dao.Database;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.List;
import java.util.Map;

public class Bike extends Vehicle {
    protected int cylinder;

    public int getCylinder() { return cylinder; }
    public void setCylinder(int cylinder) {
        this.cylinder = cylinder;
    }

    public void insert(){
        String sql = "INSERT INTO vehicle(type, priceday, brand, model, cylinder, luxury) " +
                "VALUES(:type, :priceday, :brand, '', :cylinder, 0)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("type", this.type)
                    .addParameter("priceday", this.priceDay)
                    .addParameter("brand", this.brand)
                    .addParameter("cylinder", this.cylinder)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE vehicle SET type = :type, priceday = :priceday, brand = :brand, model = '', " +
                "cylinder = :cylinder, luxury = 0 WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("id", this.id)
                    .addParameter("type", this.type)
                    .addParameter("priceday", this.priceDay)
                    .addParameter("brand", this.brand)
                    .addParameter("cylinder", this.cylinder)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    /**
     * @param id int
     * @return Bike
     */

    public static Bike hydrate(int id){
        Map<String,Object> data;
        Bike bike = new Bike();
        String sql = "SELECT * FROM vehicle WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            data = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList().get(0);

            try {
                bike.setId((int) data.get("id"));
                bike.setType((int) data.get("type"));
                bike.setPriceDay((int) data.get("priceday"));
                bike.setBrand((String) data.get("brand"));
                bike.setCylinder((int) data.get("cylinder"));
                bike.setSpecimenList(Specimen.findIdByVehicle(bike.getId()));
            }
            catch (ClassCastException | NullPointerException e1){
                User.logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return bike;
    }

    /**
     * @param data Map
     * @return Bike
     */

    public static Bike hydrate(Map<String,Object> data){
        Bike bike = new Bike();

        try {
            bike.setId((int) data.get("id"));
            bike.setType((int) data.get("type"));
            bike.setPriceDay((int) data.get("priceday"));
            bike.setBrand((String) data.get("brand"));
            bike.setCylinder((int) data.get("cylinder"));
            bike.setSpecimenList(Specimen.findIdByVehicle(bike.getId()));
        }
        catch (ClassCastException | NullPointerException e1){
            User.logger.debug(e1.toString());
        }

        return bike;
    }
}
