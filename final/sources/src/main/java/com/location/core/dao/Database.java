package com.location.core.dao;

import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

public class Database {
    private static Database database;
    private Sql2o sql2o;

    private Database(){
        String url = "jdbc:postgresql://localhost:5432/efrei-location";
        String user = "postgres";
        String password = "root";

        this.sql2o = new Sql2o(url, user, password);

        try {
            this.sql2o = new Sql2o(url, user, password);
        }
        catch(Sql2oException e){
            System.out.println(e.toString());
        }
    }

    /**
     * @return Sql2o
     */

    public static Sql2o getDao(){
        if(database == null)
           database = new Database();

        return database.sql2o;
    }
}
