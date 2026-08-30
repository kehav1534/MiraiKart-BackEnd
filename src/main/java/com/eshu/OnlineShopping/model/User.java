package com.eshu.OnlineShopping.model;

import com.eshu.OnlineShopping.enums.Gender;
import com.eshu.OnlineShopping.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(name = "contact_no", unique = true)
    private String contactNo;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    @JsonIgnore
    private UserStatus status;

    @Column(name = "gender")
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @Column(name = "dob")
    private String dob;

    /**
     * Set only on the auto-provisioned "shopping profile" created the first
     * time a SELLER account uses the cart/checkout (see
     * UserService#getOrCreateShoppingProfileForSeller). Null for every
     * normal buyer account that signed up through /auth/user/register.
     * This lets one seller login act as a buyer too - cart/order tables
     * still just point at a User row, they don't need to know it belongs
     * to a seller underneath.
     */
    @Column(name = "linked_seller_id")
    private Integer linkedSellerId;

    public Integer getLinkedSellerId() {
        return linkedSellerId;
    }

    public void setLinkedSellerId(Integer linkedSellerId) {
        this.linkedSellerId = linkedSellerId;
    }

    @OneToOne(mappedBy = "cartUserID", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Cart userCart;

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orderHistory;

    @OneToOne
    @JoinColumn(name = "auth")
    private UserAuth userAuth;

    public UserAuth getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Cart getUserCart() {
        return userCart;
    }

    public void setUserCart(Cart userCart) {
        this.userCart = userCart;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }
}
