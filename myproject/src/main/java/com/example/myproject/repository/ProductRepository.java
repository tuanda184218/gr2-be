package com.example.myproject.repository;

import com.example.myproject.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductName(String productName);

    List<Product> findByUserId(Long userId);
}