package ru.crystals.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class PricePeriod/* implements Comparable<PricePeriod>*/ {
    private Date begin; // начало действия

    private Date end; // конец действия

//    @Override
//    public int compareTo(PricePeriod pricePeriod) {
//       return begin.compareTo(pricePeriod.getBegin()) * pricePeriod.getBegin().compareTo(begin);
//    }
}
