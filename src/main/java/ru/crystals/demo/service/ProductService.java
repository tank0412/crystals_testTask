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
                .comparing(PricePeriod::getProductCode)
                .thenComparing(PricePeriod::getBegin)
                .thenComparing(PricePeriod::getEnd);
        TreeMap<PricePeriod, Product> productMap = new TreeMap<>(pricePeriodComparator);
        for (Product p : products) {
            productMap.put(new PricePeriod(p.getProductCode(), p.getBegin(), p.getEnd()), p);
        }

        //filter products if there are products which period crosses period of changed products
        for (Product changedProduct : changedProducts) {
            changedProduct.setProductChange(true);
            PricePeriod changedPeriod = new PricePeriod(changedProduct.getProductCode(), changedProduct.getBegin(), changedProduct.getEnd());
            Map.Entry<PricePeriod, Product> lowerEntry;
            do {
                lowerEntry = productMap.higherEntry(changedPeriod);
                if (lowerEntry == null) {
                    //try floor entry (less than or equal to the given key)
                    lowerEntry = productMap.lowerEntry(changedPeriod);
                }
                if (lowerEntry != null) {
                    PricePeriod floorEntryKey = lowerEntry.getKey();
                    Product floorEntryValue = lowerEntry.getValue();
                    if (floorEntryKey.getBegin().compareTo(changedPeriod.getBegin()) >= 0
                            && floorEntryKey.getEnd().compareTo(changedPeriod.getEnd()) <= 0
                            && changedProduct.getProductCode().equals(floorEntryValue.getProductCode())
                            && changedProduct.getDepart().equals(floorEntryValue.getDepart())
                            && changedProduct.getNumber().equals(floorEntryValue.getNumber())) {
                        productMap.remove(floorEntryKey);
                    } else {
                        //so if it did not pass validation - exiting
                        break;
                    }
                }
            }
            while (lowerEntry != null);
        }
        products = new ArrayList<>(productMap.values());


        List<Product> productsFiltered = new ArrayList<>();
        //second condition for a case when we removed all products which cross periods of changedProducts
        if (products.size() > 0 || changedProducts.size() > 0) {
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

                        //check number and department
                        if (nextProductPeriod.getNumber().equals(productPeriod.getNumber())
                                && nextProductPeriod.getDepart().equals(productPeriod.getDepart())) {

                            if (nextProductPeriod.getBegin().compareTo(productPeriod.getBegin()) > 0
                                    && nextProductPeriod.getEnd().compareTo(productPeriod.getEnd()) < 0) {
                                //ProductPeriod - 01.01.2013-31.01.2013
                                //NextProductPeriod - 12.01.2013-13.01.20138
                                Product newProduct = new Product(productPeriod.getId(), productPeriod.getProductCode(),
                                        productPeriod.getNumber(), productPeriod.getDepart(), productPeriod.getBegin(),
                                        productPeriod.getEnd(), productPeriod.getValue(), false);
                                newProduct.setBegin(nextProductPeriod.getEnd());
                                newProducts.add(newProduct);
                                productPeriod.setEnd(nextProductPeriod.getBegin());
                            } else if ((nextProductPeriod.getBegin().compareTo(productPeriod.getBegin()) > 0 &&
                                    //начало у нового периода не должно быть больше, чем конец у старого
                                    nextProductPeriod.getBegin().compareTo(productPeriod.getEnd()) < 0)
                                    && nextProductPeriod.getEnd().compareTo(productPeriod.getEnd()) > 0) {
                                //to fix a situation when change product affect few periods which existed earlier
                                if (nextProductPeriod.getBegin().compareTo(productPeriod.getEnd()) < 0
                                        && !nextProductPeriod.isProductChange()) {
                                    //ProductPeriod - 01.01.2013-15.02.2013 false
                                    //NextProductPeriod - 01.02.2013-28.02.2013 false
                                    nextProductPeriod.setBegin(productPeriod.getEnd());
                                } else {
                                    //ProductPeriod - 01.01.2013-03.03.2013
                                    //NextProductPeriod - 31.01.2013-15.03.2013
                                    productPeriod.setEnd(nextProductPeriod.getBegin());
                                }
                            } else if (nextProductPeriod.getBegin().compareTo(productPeriod.getBegin()) == 0
                                    && nextProductPeriod.getEnd().compareTo(productPeriod.getEnd()) > 0
                                    && nextProductPeriod.getBegin().compareTo(productPeriod.getEnd()) < 0) {
                                //ProductPeriod - 15.02.2013-28.02.2013
                                //NextProductPeriod - 15.02.2013-15.03.2013
                                productsFiltered.set(i, null);
                            }

                        }
                    }
                }
                productsFiltered.addAll(newProducts);
            }
        }
        productsFiltered = productsFiltered.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Product::getBegin)).collect(Collectors.toList());
        return productsFiltered;
    }
}
