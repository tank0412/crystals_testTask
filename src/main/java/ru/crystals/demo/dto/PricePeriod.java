package ru.crystals.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class PricePeriod {
    private String productCode; // код товара

    private Date begin; // начало действия

    private Date end; // конец действия
}
