package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Bill extends Model {
    private int id;
    private float price;
    private Rental rental;

    public int getId(){ return id; }
    public float getPrice() { return price; }
    public Rental getRental() { return rental; }

    public void setId(int id) { this.id = id; }
    public void setPrice(float price) { this.price = price; }
    public void setRental(Rental rental) { this.rental = rental; }

    public void insert(){
        String sql = "INSERT INTO bill(rental, price) " +
                "VALUES(:rental, :price)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("rental", this.rental.getId())
                    .addParameter("price", this.price)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE bill SET rental = :rental, price = :price WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("id", this.id)
                    .addParameter("rental", this.rental.getId())
                    .addParameter("price", this.price)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void delete(){
        String sql = "DELETE FROM bill WHERE id = :id";

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
     * @param id int
     * @return Bill
     */

    public static Bill hydrate(int id){
        Map<String,Object> data;
        Bill bill = new Bill();
        String sql = "SELECT * FROM bill WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                bill.setId((int) data.get("id"));
                Double d =  new Double((Double)data.get("price"));
                bill.setPrice(d.floatValue());
                bill.setRental(Rental.hydrate((int)data.get("rental")));
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return bill;
    }

    /**
     * @return List
     */

    public static List<Bill> findAll(){
        List<Map<String,Object>> dataList;
        List<Bill> billList = new ArrayList<>();

        String sql = "SELECT * FROM bill";

        try(Connection con = Database.getDao().open()) {
            dataList = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            try {
                for(Map<String,Object> data: dataList){
                    int id = (int) data.get("id");
                    billList.add(Bill.hydrate(id));
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return billList;
    }
}
