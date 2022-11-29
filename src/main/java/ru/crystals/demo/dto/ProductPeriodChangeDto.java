package ru.crystals.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.crystals.demo.entity.Product;

import java.util.List;

@NoArgsConstructor
@Data
public class ProductPeriodChangeDto {
    private List<Product> products;
    private List<Product> changedProducts;
}
