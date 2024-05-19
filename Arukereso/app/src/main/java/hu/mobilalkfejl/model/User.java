package hu.mobilalkfejl.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userName;
    private String email;
    private String password;
    private String phone;
    private boolean seller;
    private List<String> ownedStores;

    public User(String userName, String email, String password, String phone, boolean seller) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.seller = seller;
        ownedStores = new ArrayList<>();
    }

    public User() {
    }

    public boolean isSeller() {
        return seller;
    }

    public void setSeller(boolean seller) {
        this.seller = seller;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getOwnedStores() {
        return ownedStores;
    }

    public void setOwnedStores(List<String> ownedStores) {
        this.ownedStores = ownedStores;
    }
}
