package com.aakib78.hospitonadmin;

import android.media.Rating;

public class Store {

    private String storeName;
    private String address;
    private String totalDiscount;
    private String minPurchase;
    private String maxDiscount;
    private String storeLatitude;
    private String storeLongitude;
    private String category;
    private int lckyUser;
    private String qrKey;
    private String storeImage;
    private Boolean offerAvailable;
    private String totalUserRated;
    private String ratingValue;
    private String storeId;

    public Store() {

    }
    public Store(String storeId,String storeName, String address, String totalDiscount, String minPurchase, String maxDiscount, String storeLatitude, String storeLongitude, String category, int lckyUser, String qrKey, String storeImage, Boolean offerAvailable, String totalUserRated, String ratingValue) {
        this.storeName = storeName;
        this.storeId=storeId;
        this.address = address;
        this.totalDiscount = totalDiscount;
        this.minPurchase = minPurchase;
        this.maxDiscount = maxDiscount;
        this.storeLatitude = storeLatitude;
        this.storeLongitude = storeLongitude;
        this.category = category;
        this.lckyUser = lckyUser;
        this.qrKey = qrKey;
        this.storeImage = storeImage;
        this.offerAvailable = offerAvailable;
        this.totalUserRated = totalUserRated;
        this.ratingValue = ratingValue;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreImage() {
        return storeImage;
    }

    public String getTotalUserRated() {
        return totalUserRated;
    }

    public void setTotalUserRated(String totalUserRated) {
        this.totalUserRated = totalUserRated;
    }

    public String getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(String ratingValue) {
        this.ratingValue = ratingValue;
    }

    public void setStoreImage(String storeImage) {
        this.storeImage = storeImage;
    }
    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(String totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getMinPurchase() {
        return minPurchase;
    }

    public void setMinPurchase(String minPurchase) {
        this.minPurchase = minPurchase;
    }

    public String getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(String maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public String getStoreLatitude() {
        return storeLatitude;
    }

    public void setStoreLatitude(String storeLatitude) {
        this.storeLatitude = storeLatitude;
    }

    public String getStoreLongitude() {
        return storeLongitude;
    }

    public void setStoreLongitude(String storeLongitude) {
        this.storeLongitude = storeLongitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLckyUser() {
        return lckyUser;
    }

    public void setLckyUser(int lckyUser) {
        this.lckyUser = lckyUser;
    }

    public String getQrKey() {
        return qrKey;
    }

    public void setQrKey(String qrKey) {
        this.qrKey = qrKey;
    }

    public Boolean getOfferAvailable() {
        return offerAvailable;
    }

    public void setOfferAvailable(Boolean offerAvailable) {
        this.offerAvailable = offerAvailable;
    }
}
