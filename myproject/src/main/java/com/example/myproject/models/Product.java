package com.example.myproject.models;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

@Entity
@Table(name="products")
public class Product {
    //this is "primary key"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //validate = constraint
    @Column(nullable = false, unique = true, length = 300)
    private String productName;
    private String description;
    private Double price;
    private String image;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //default constructor

    public Product() {
    }

    public Product(Long id, String productName, String description, Double price, String image, User user) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.image = image;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) && Objects.equals(productName, product.productName) && Objects.equals(description, product.description) && Objects.equals(price, product.price) && Objects.equals(image, product.image) && Objects.equals(user, product.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productName, description, price, image, user);
    }
}
