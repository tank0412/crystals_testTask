package ru.crystals.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.crystals.demo.dto.ProductPeriodChangeDto;
import ru.crystals.demo.entity.Product;
import ru.crystals.demo.service.ProductService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/products", produces = "application/json")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping("/periodChange")
    public List<Product> proceedProductsPeriodChange(@RequestBody ProductPeriodChangeDto productPeriodChangeDto) {
        List<Product> products = productPeriodChangeDto.getProducts();
        List<Product> changedProducts = productPeriodChangeDto.getChangedProducts();

        return productService.proceedProductsPeriodChange(products, changedProducts);
    }
}
