package ru.crystals.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

//Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class Product {
    private Long id; // идентификатор в БД
    private String productCode; // код товара
    private Integer number; // номер цены
    private Integer depart; // номер отдела
    private Date begin; // начало действия
    private Date end; // конец действия
    private Long value; // значение цены в копейках

}
