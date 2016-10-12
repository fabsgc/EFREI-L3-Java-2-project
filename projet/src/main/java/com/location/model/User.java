package com.location.model;

import com.location.core.dao.Database;
import com.location.core.mvc.Model;
import com.location.util.Password;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User extends Model {
    private int id;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String avatar;

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getUsername() { return username; }
    public String getPassword() {
        return password;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getAvatar() { return avatar; }

    public void setId(int id) { this.id = id;}
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public User(){
        this.avatar = "img/user.png";
    }

    public void insert() {
        String sql = "INSERT INTO user(email, username, password, firstname, lastname, avatar) " +
                "VALUES(:email, :username, :password, :firstname, :lastname, :avatar)";

        try(Connection con = Database.getDao().open()) {
            this.id = con.createQuery(sql, true)
                    .addParameter("email", this.email)
                    .addParameter("username", this.username)
                    .addParameter("password", Password.SHA1(this.password))
                    .addParameter("firstname", this.firstName)
                    .addParameter("lastname", this.lastName)
                    .addParameter("avatar", this.avatar)
                    .executeUpdate().getKey(Integer.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void update(){
        String sql = "UPDATE public.user SET email = :email, username = :username, password = :password, " +
                "firstname = :firstname, lastname = :lastname, avatar = :avatar WHERE id = :id)";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("email", this.email)
                    .addParameter("username", this.username)
                    .addParameter("password", this.password)
                    .addParameter("firstname", this.firstName)
                    .addParameter("lastname", this.lastName)
                    .addParameter("avatar", this.avatar)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public void delete(){
        String sql = "DELETE FROM public.user WHERE id = :id)";

        try(Connection con = Database.getDao().open()) {
            con.createQuery(sql)
                    .addParameter("id", this.id)
                    .executeUpdate();
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }
    }

    public static User hydrate(int id){
        Map<String,Object> data;
        User user = new User();

        String sql = "SELECT id, email, username, password, firstname, lastname, avatar " +
                "FROM public.user WHERE id = :id";

        try(Connection con = Database.getDao().open()) {
            List<Map<String,Object>> dataList = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable().asList();

            if(dataList.size() == 0) return null;
            else data = dataList.get(0);

            try {
                user.setId((int) data.get("id"));
                user.setEmail((String) data.get("email"));
                user.setUsername((String) data.get("username"));
                user.setPassword((String) data.get("password"));
                user.setFirstName((String) data.get("firstname"));
                user.setLastName((String) data.get("lastname"));
                user.setAvatar((String) data.get("avatar"));
            }
            catch ( ClassCastException | NullPointerException e1){
                logger.debug(e1.toString());
            }
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        return user;
    }

    public static User getUserByUserNamePassword(String userName, String password){
        List<User> userList = new ArrayList<>();

        String sql = "SELECT id, email, username, password, firstname, lastname, avatar " +
                "FROM public.user WHERE username = :username AND password = :password";

        try(Connection con = Database.getDao().open()) {
            userList = con.createQuery(sql)
                    .addParameter("username", userName)
                    .addParameter("password", password)
                    .executeAndFetch(User.class);
        }
        catch(Sql2oException e){
            logger.fatal(e.toString());
        }

        if(userList.size() > 0)
            return userList.get(0);

        return null;
    }
}
