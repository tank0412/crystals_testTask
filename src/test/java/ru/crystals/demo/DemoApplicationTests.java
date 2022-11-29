package ru.crystals.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ResourceUtils;
import ru.crystals.demo.dto.ProductPeriodChangeDto;
import ru.crystals.demo.entity.Product;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DemoApplicationTests extends AbstractTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void changeProductPeriod() throws Exception {
        String uri = "/products/periodChange";

        File requestJson = new ClassPathResource("static/TwoProductsPeriodChangeRequest.json").getFile();
        ProductPeriodChangeDto productPeriodChangeDto = super.mapFromJsonFile(requestJson, ProductPeriodChangeDto.class);
        File responseJson = new ClassPathResource("static/TwoProductsPeriodChangeResponse.json").getFile();
        Product[] expectedProductsResponse = super.mapFromJsonFile(responseJson, Product[].class);


        String inputJson = super.mapToJson(productPeriodChangeDto);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);

        String content = mvcResult.getResponse().getContentAsString();
        Product[] productsInResponse = super.mapFromJson(content, Product[].class);
        for (int i = 0; i < expectedProductsResponse.length; ++i) {
            Product expectedProduct = expectedProductsResponse[i];
            assertEquals(expectedProduct, productsInResponse[i]);
        }
    }

}
