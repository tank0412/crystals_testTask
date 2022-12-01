package ru.crystals.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.crystals.demo.dto.ProductPeriodChangeDto;
import ru.crystals.demo.entity.Product;

import java.io.File;

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
        testUsingExistingJsonFile("/products/periodChange",
                "static/TwoProductsPeriodChangeRequest.json", "static/TwoProductsPeriodChangeResponse.json");
    }

    @Test
    public void changeProductPeriodAndCoverAllExistingPeriods() throws Exception {
        testUsingExistingJsonFile("/products/periodChange",
                "static/OneProductPeriodChangeRequestWhenItCoversAllExistingPeriodRequest.json",
                "static/OneProductPeriodChangeRequestWhenItCoversAllExistingPeriodResponse.json");
    }

    @Test
    public void changeProductPeriodAndAffectTwoPeriods() throws Exception {
        testUsingExistingJsonFile("/products/periodChange",
                "static/OneProductPeriodChangeRequestWhenItAffectsTwoExistingPeriodsRequest.json",
                "static/OneProductPeriodChangeRequestWhenItAffectsTwoExistingPeriodsResponse.json");
    }

    @Test
    public void changeProductPeriodAndAffectTwoPeriodsAndAbsorbOne() throws Exception {
        testUsingExistingJsonFile("/products/periodChange",
                "static/OneProductPeriodChangeRequestWhenItAffectsTwoExistingPeriodsAndAbsorbOneRequest.json",
                "static/OneProductPeriodChangeRequestWhenItAffectsTwoExistingPeriodsAndAbsorbOneResponse.json");
    }

    private void testUsingExistingJsonFile(String url, String requestFileName, String responseFileName) throws Exception {
        File requestJson = new ClassPathResource(requestFileName).getFile();
        ProductPeriodChangeDto productPeriodChangeDto = super.mapFromJsonFile(requestJson, ProductPeriodChangeDto.class);
        File responseJson = new ClassPathResource(responseFileName).getFile();
        Product[] expectedProductsResponse = super.mapFromJsonFile(responseJson, Product[].class);

        String inputJson = super.mapToJson(productPeriodChangeDto);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(url)
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
