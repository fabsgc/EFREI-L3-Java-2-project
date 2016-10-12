package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Borrower extends Model {
    private int id;
    private String lastName;
    private String firstName;
    private String pc;
    private String city;
    private String street;
    private int bill;
    private List<Integer> specimenList = new ArrayList<>();

    public int getId() {
        return id;
    }
    public String getLastName() { return lastName; }
    public String getFirstName() {
        return firstName;
    }
    public String getPc() {
        return pc;
    }
    public String getCity() {
        return city;
    }
    public String getStreet() { return street; }
    public int getBill() { return bill; }
    public List<Integer> getSpecimenList() { return specimenList; }

    public void setId(int id) { this.id = id; }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setPc(String pc) { this.pc = pc; }
    public void setStreet(String street) {
        this.street = street;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setBill(int bill) { this.bill = bill; }
    public void setSpecimenList(List<Integer> specimenList) {
        this.specimenList = specimenList;
    }

    public void insert(){
        String sql = "INSERT INTO borrower(firstname, lastname, street, pc, city) " +
                "VALUES(:firstname, :lastname, :street, :pc, :city)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("firstname", this.firstName)
                    .addParameter("lastname", this.lastName)
                    .addParameter("street", this.street)
                    .addParameter("pc", this.pc)
                    .addParameter("city", this.city)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE borrower SET firstname = :firstname, lastname = :lastname, " +
                "street = :street, pc = :pc, city = :city WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("firstname", this.firstName)
                    .addParameter("lastname", this.lastName)
                    .addParameter("street", this.street)
                    .addParameter("pc", this.pc)
                    .addParameter("city", this.city)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void delete(){
        String sql = "DELETE FROM borrower WHERE id = :id";

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
     * @return Borrower
     */

    public static Borrower hydrate(int id){
        Map<String,Object> data;
        Borrower borrower = new Borrower();

        String sql = "SELECT *, " +
                "(SELECT SUM(bill.price) " +
                "FROM bill " +
                "INNER JOIN rental ON bill.rental = rental.id " +
                "WHERE rental.borrower = :id GROUP BY rental.borrower) AS bill " +
                "FROM borrower WHERE borrower.id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                borrower.setId((int) data.get("id"));
                borrower.setFirstName((String) data.get("firstname"));
                borrower.setLastName((String) data.get("lastname"));
                borrower.setStreet((String)data.get("street"));
                borrower.setPc((String) data.get("pc"));
                borrower.setCity((String) data.get("city"));
                borrower.setSpecimenList(Specimen.findIdByBorrower(borrower.getId()));

                if(data.get("bill") != null) {
                    Double d = new Double((Double) data.get("bill"));
                    borrower.setBill(d.intValue());
                }
                else {
                    borrower.setBill(0);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
                e1.printStackTrace();
                System.out.println(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
            e.printStackTrace();
        }

        return borrower;
    }

    /**
     * @return List
     */

    public static List<Borrower> findAll(){
        return findAll("1", "1");
    }

    public static List<Borrower> findAll(String showRentals, String showNoRentals){
        List<Borrower> data = new ArrayList<>();

        String sql = "SELECT id FROM borrower";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results){
                Borrower borrower = Borrower.hydrate((int)result.get("id"));

                if(borrower.getSpecimenList().size() > 0 && (showRentals.equals("1"))){
                    data.add(borrower);
                }
                else if(borrower.getSpecimenList().size() == 0 && (showNoRentals.equals("1"))){
                    data.add(borrower);
                }
            }
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return data;
    }

    /**
     * @param name String
     * @param showRentals String
     * @param showNoRentals String
     * @return List
     */

    public static List<Borrower> findByName(String name, String showRentals, String showNoRentals){
        List<Borrower> data = new ArrayList<>();
        String[] parts = name.split(" ");
        String in = "";

        for (String part: parts){
            in += "'" + part.toLowerCase() + "', ";
        }

        in = in.substring(0, in.length()-2);

        String sql = "SELECT id FROM borrower WHERE lower(lastname) IN (" + in + ") OR lower(firstname) IN ("+ in + ")";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql).executeAndFetchTable().asList();

            for(Map<String, Object> result : results){
                Borrower borrower = Borrower.hydrate((int)result.get("id"));

                if(borrower.getSpecimenList().size() > 0 && (showRentals.equals("1"))){
                    data.add(borrower);
                }
                else if(borrower.getSpecimenList().size() == 0 && (showNoRentals.equals("1"))){
                    data.add(borrower);
                }
            }
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return data;
    }

    /**
     * @param id int
     * @param showRentals String
     * @param showNoRentals String
     * @return List
     */

    public static List<Borrower> findByVehicle(int id, String showRentals, String showNoRentals){
        List<Borrower> data = new ArrayList<>();

        String sql = "SELECT borrower.id " +
                "FROM borrower " +
                "INNER JOIN rental ON borrower.id = rental.borrower " +
                "INNER JOIN specimen ON rental.id = specimen.rental " +
                "INNER JOIN vehicle ON specimen.vehicle = vehicle.id " +
                "WHERE vehicle.id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results){
                Borrower borrower = Borrower.hydrate((int)result.get("id"));

                if(borrower.getSpecimenList().size() > 0 && (showRentals.equals("1"))){
                    data.add(borrower);
                }
                else if(borrower.getSpecimenList().size() == 0 && (showNoRentals.equals("1"))){
                    data.add(borrower);
                }
            }
        }
        catch (ClassCastException | NullPointerException e1){
            Borrower.logger.debug(e1.toString());
        }

        return data;
    }
}
