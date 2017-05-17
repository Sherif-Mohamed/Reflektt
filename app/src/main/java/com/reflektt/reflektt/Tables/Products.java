package com.reflektt.reflektt.Tables;

import com.backendless.BackendlessUser;

import java.util.LinkedList;

/**
 * Created by Shiko on 09/03/2017.
 */

public class Products {
    private BackendlessUser addedBy;
    private String category;
    private LinkedList<Comments> comments;
    private String companyName;
    private BackendlessUser[] favorites;
    private String name;
    private String parent;
    private BackendlessUser[] rates1;
    private BackendlessUser[] rates2;
    private BackendlessUser[] rates3;
    private BackendlessUser[] rates4;
    private BackendlessUser[] rates5;
    private String type;
    private String objectId;

    public Products(){

    }
    public BackendlessUser getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(BackendlessUser addedBy) {
        this.addedBy = addedBy;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LinkedList<Comments> getComments() {
        return comments;
    }

    public void setComments(LinkedList<Comments> comments) {
        this.comments = comments;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BackendlessUser[] getFavorites() {
        return favorites;
    }

    public void setFavorites(BackendlessUser[] favorites) {
        this.favorites = favorites;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public BackendlessUser[] getRates1() {
        return rates1;
    }

    public void setRates1(BackendlessUser[] rates1) {
        this.rates1 = rates1;
    }

    public BackendlessUser[] getRates2() {
        return rates2;
    }

    public void setRates2(BackendlessUser[] rates2) {
        this.rates2 = rates2;
    }

    public BackendlessUser[] getRates3() {
        return rates3;
    }

    public void setRates3(BackendlessUser[] rates3) {
        this.rates3 = rates3;
    }

    public BackendlessUser[] getRates4() {
        return rates4;
    }

    public void setRates4(BackendlessUser[] rates4) {
        this.rates4 = rates4;
    }

    public BackendlessUser[] getRates5() {
        return rates5;
    }

    public void setRates5(BackendlessUser[] rates5) {
        this.rates5 = rates5;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean equals (Products i){
        if (companyName.equals(i.companyName)
                && type.equals(i.type)
                && name.equals(i.name)
                && category.equals(i.category)
                && parent.equals(i.parent))return true;
        return false;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
