package ru.crystals.demo.service;

import org.springframework.stereotype.Service;
import ru.crystals.demo.dto.PricePeriod;
import ru.crystals.demo.entity.Product;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    public List<Product> proceedProductsPeriodChange(List<Product> products, List<Product> changedProducts) {
        Comparator<PricePeriod> pricePeriodComparator = Comparator
                .comparing(PricePeriod::getBegin)
                .thenComparing(PricePeriod::getEnd);
        TreeMap<PricePeriod, Product> productMap = new TreeMap<>(pricePeriodComparator);
        for (Product p : products) {
//            LocalDate begin = p.getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            LocalDate end = p.getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            productMap.put(new PricePeriod(p.getBegin(), p.getEnd()), p);
        }

        for (Product changedProduct : changedProducts) {
//            LocalDate begin = changedProduct.getBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            LocalDate end = changedProduct.getEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            PricePeriod changedPeriod = new PricePeriod(changedProduct.getBegin(), changedProduct.getEnd());

//            Map.Entry<PricePeriod, Product> ceilingEntry = productMap.ceilingEntry(changedPeriod);
            Map.Entry<PricePeriod, Product> floorEntry = productMap.floorEntry(changedPeriod);
//            Map.Entry<PricePeriod, Product> lowerEntry = productMap.lowerEntry(changedPeriod);
            //FIXME: Проверить на пересечение
            if (floorEntry != null) {
                PricePeriod floorEntryKey = floorEntry.getKey();
                Product floorEntryValue = floorEntry.getValue();
                if (floorEntryKey.getBegin().compareTo(changedPeriod.getBegin()) >= 0
                        && floorEntryKey.getEnd().compareTo(changedPeriod.getEnd()) <= 0
                && changedProduct.getDepart().equals(floorEntryValue.getDepart())
                && changedProduct.getNumber().equals(floorEntryValue.getNumber())
                && changedProduct.getProductCode().equals(floorEntryValue.getProductCode())) {
                    productMap.remove(floorEntryKey);

                    //FIXME: Is this really necessary
//                    Map.Entry<PricePeriod, Product> higherEntry = productMap.higherEntry(changedPeriod);
//                    if (higherEntry != null) {
//                        if (floorEntryKey.getBegin().compareTo(changedPeriod.getBegin()) >= 0
//                        && floorEntryKey.getEnd().compareTo(changedPeriod.getEnd()) <= 0
//                                && changedProduct.getDepart().equals(floorEntryValue.getDepart())
//                                && changedProduct.getNumber().equals(floorEntryValue.getNumber())
//                                && changedProduct.getProductCode().equals(floorEntryValue.getProductCode())) {
//                            productMap.remove(higherEntry.getKey());
//                        }
//                    }
                }
            }
        }
        products = new ArrayList<>(productMap.values());


        List<Product> productsFiltered = new ArrayList<>();
        if (products != null) {
            products.addAll(changedProducts);

            Map<String, List<Product>> productGrouped = products.stream()
                    .filter(product -> product.getProductCode() != null)
                    .sorted(Comparator.comparing(Product::getBegin))
                    .collect(Collectors.groupingBy(Product::getProductCode));

            for (String productCode : productGrouped.keySet()) {
                List<Product> productsByCode = productGrouped.get(productCode);

                //reduce for products with same value and period which can be prolonged
                List<Product> productsByCodeReduced = new ArrayList<>();
                Map<Long, List<Product>> groupedByValue = productsByCode.stream().collect(Collectors.groupingBy(Product::getValue));
                groupedByValue.entrySet().forEach(entrySet -> {
                    List<Product> productWithSameValue = entrySet.getValue();
                    if (productWithSameValue.size() != 1) {
                        Optional<Product> reducedProduct = productWithSameValue.stream()
                                .reduce((p1, p2) -> {
                                    if (p2.getBegin().compareTo(p2.getEnd()) < 0 && p1.getEnd().compareTo(p2.getEnd()) < 0) {
                                        p1.setEnd(p2.getEnd());
                                    }
                                    return p1;
                                });
                        reducedProduct.ifPresent(productsByCodeReduced::add);
                    } else {
                        productsByCodeReduced.addAll(productWithSameValue);
                    }
                });
                productsFiltered.addAll(productsByCodeReduced);

                //proceed situation when product appears with same id but date period affecting current price
                List<Product> productsByCodeCorrected = productsByCodeReduced.stream().sorted(Comparator.comparing(Product::getBegin))
                        .collect(Collectors.toList());
                List<Product> newProducts = new ArrayList<>();
                for (int i = 0; i < productsByCodeCorrected.size(); ++i) {
                    if (i + 1 < productsByCodeCorrected.size()) {
                        Product productPeriod = productsByCodeCorrected.get(i);
                        Product nextProductPeriod = productsByCodeCorrected.get(i + 1);

                        if (nextProductPeriod.getBegin().compareTo(productPeriod.getBegin()) > 0
                                && nextProductPeriod.getEnd().compareTo(productPeriod.getEnd()) < 0
                                && nextProductPeriod.getNumber().equals(productPeriod.getNumber())
                                && nextProductPeriod.getDepart().equals(productPeriod.getDepart())) {

                            Product newProduct = new Product(productPeriod.getId(), productPeriod.getProductCode(),
                                    productPeriod.getNumber(), productPeriod.getDepart(), productPeriod.getBegin(),
                                    productPeriod.getEnd(), productPeriod.getValue());
                            newProduct.setBegin(nextProductPeriod.getEnd());
                            newProducts.add(newProduct);
                            productPeriod.setEnd(nextProductPeriod.getBegin());
                        } else if ((nextProductPeriod.getBegin().compareTo(productPeriod.getBegin()) > 0 &&
                                //начало у нового периода не должно быть больше, чем конец у старого
                                nextProductPeriod.getBegin().compareTo(productPeriod.getEnd()) < 0)
                                && nextProductPeriod.getEnd().compareTo(productPeriod.getEnd()) > 0
                                && nextProductPeriod.getNumber().equals(productPeriod.getNumber())
                                && nextProductPeriod.getDepart().equals(productPeriod.getDepart())) {
                            productPeriod.setEnd(nextProductPeriod.getBegin());
                        }
                    }
                }
                productsFiltered.addAll(newProducts);
            }
        }
        productsFiltered = productsFiltered.stream().sorted(Comparator.comparing(Product::getBegin)).collect(Collectors.toList());
        return productsFiltered;
        //return null;
    }
}
