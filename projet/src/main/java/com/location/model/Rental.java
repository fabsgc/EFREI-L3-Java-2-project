package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Rental extends Model {
    private int id;
    private Date begin;
    private Date end;
    private Borrower borrower;
    private Boolean insurance;
    private Boolean ended;
    private int bill;
    private List<Integer> specimenList = new ArrayList<>();

    public int getId() {
        return id;
    }
    public Date getBegin() {
        return begin;
    }
    public Date getEnd() { return end; }
    public Borrower getBorrower() {
        return borrower;
    }
    public Boolean getInsurance() { return insurance; }
    public Boolean getEnded() { return ended; }
    public int getBill() { return bill; }
    public List<Integer> getSpecimenList() { return specimenList; }

    public void setId(int id) { this.id = id; }
    public void setBegin(Date begin) { this.begin = begin; }
    public void setEnd(Date end) {
        this.end = end;
    }
    public void setBorrower(Borrower borrower) { this.borrower = borrower; }
    public void setInsurance(Boolean insurance) { this.insurance = insurance; }
    public void setEnded(Boolean ended) { this.ended = ended; }
    public void setBill(int bill) { this.bill = bill; }
    public void setSpecimenList(List<Integer> specimenList) {
        this.specimenList = specimenList;
    }

    public void insert(){
        String sql = "INSERT INTO rental(borrower, begintime, endtime, insurance, ended) " +
                "VALUES(:borrower, :begin, :end, :insurance, :ended)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("borrower", this.borrower.getId())
                    .addParameter("begin", this.begin)
                    .addParameter("end", this.end)
                    .addParameter("insurance", this.insurance)
                    .addParameter("ended", this.ended)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void update(){
        String sql = "UPDATE rental SET borrower = :borrower, " +
                "begintime = :begin, endtime = :end, insurance = :insurance, ended = :ended WHERE id = :id ";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql, true)
                    .addParameter("borrower", this.borrower.getId())
                    .addParameter("begin", this.begin)
                    .addParameter("end", this.end)
                    .addParameter("insurance", this.insurance)
                    .addParameter("ended", this.ended)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void delete(){
        String sql = "UPDATE specimen SET rental = null WHERE rental = :id ";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        sql = "DELETE FROM rental WHERE id = :id";

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
     * @return Rental
     */

    public static Rental hydrate(int id){
        Map<String,Object> data;
        Rental rental = new Rental();
        String sql = "SELECT *, " +
                "(SELECT " +
                "(SELECT SUM((rental.endtime - rental.begintime + 1)*vehicle.priceday)) " +
                "FROM rental " +
                "INNER JOIN specimen on specimen.rental = rental.id " +
                "INNER JOIN vehicle on specimen.vehicle = vehicle.id " +
                "WHERE rental.id = :id GROUP BY rental.id) AS bill " +
                "FROM rental WHERE rental.id = :id ";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                rental.setId((int) data.get("id"));
                rental.setBorrower(Borrower.hydrate((int)data.get("borrower")));
                rental.setBegin((java.sql.Date)data.get("begintime"));
                rental.setEnd((java.sql.Date)data.get("endtime"));
                rental.setInsurance((Boolean)data.get("insurance"));
                rental.setEnded((Boolean)data.get("ended"));
                rental.setSpecimenList(Specimen.findIdByRental(rental.getId()));

                if(data.get("bill") != null) {
                    if (rental.getInsurance())
                        rental.setBill(Math.toIntExact((Long) data.get("bill")) + rental.getSpecimenList().size() * 25);
                    else
                        rental.setBill(Math.toIntExact((Long) data.get("bill")));
                }
                else{
                    rental.setBill(0);
                }
            }
            catch (ClassCastException | NullPointerException e1){
                User.logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return rental;
    }

    /**
     * @return List
     */

    public static List<Rental> findAll(){
        List<Rental> rentalList = new ArrayList<>();

        String sql = "SELECT id FROM rental";

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results = con.createQuery(sql)
                    .executeAndFetchTable().asList();

            for(Map<String, Object> result : results){
                rentalList.add(Rental.hydrate((int)result.get("id")));
            }
        }
        catch (ClassCastException | NullPointerException e1){
            Rental.logger.debug(e1.toString());
        }

        return rentalList;
    }

    /**
     * @param date String
     * @return List
     */

    public static List<Rental> findAllCurrent(String date){
        List<Rental> rentalList = new ArrayList<>();
        String[] dates = new String[0];
        String sql = "SELECT id FROM rental WHERE ended = false ";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        if(!date.equals("")){
            sql += "AND begintime >= :date1 AND begintime <= :date2";
            dates = date.split(" - ");
        }

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results;

            if(!date.equals("")) {
                results = con.createQuery(sql)
                        .addParameter("date1", formatter.parse(dates[0]))
                        .addParameter("date2", formatter.parse(dates[1]))
                        .executeAndFetchTable().asList();
            }
            else{
                results = con.createQuery(sql)
                        .executeAndFetchTable().asList();
            }

            for(Map<String, Object> result : results){
                rentalList.add(Rental.hydrate((int)result.get("id")));
            }
        }
        catch (ClassCastException | NullPointerException | ParseException e1){
            Rental.logger.debug(e1.toString());
        }

        return rentalList;
    }

    /**
     * @param date String
     * @return List
     */

    public static List<Rental> findAllEnded(String date){
        List<Rental> rentalList = new ArrayList<>();
        String[] dates = new String[0];
        String sql = "SELECT id FROM rental WHERE ended = true ";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");

        if(!date.equals("")){
            sql += "AND begintime >= :date1 AND begintime <= :date2";
            dates = date.split(" - ");
        }

        try(Connection con = Database.getDao().open()) {
            List<Map<String, Object>> results;

            if(!date.equals("")) {
                results = con.createQuery(sql)
                        .addParameter("date1", formatter.parse(dates[0]))
                        .addParameter("date2", formatter.parse(dates[1]))
                        .executeAndFetchTable().asList();
            }
            else{
                results = con.createQuery(sql)
                        .executeAndFetchTable().asList();
            }

            for(Map<String, Object> result : results){
                rentalList.add(Rental.hydrate((int)result.get("id")));
            }
        }
        catch (ClassCastException | NullPointerException | ParseException e1){
            Rental.logger.debug(e1.toString());
        }

        return rentalList;
    }
}
