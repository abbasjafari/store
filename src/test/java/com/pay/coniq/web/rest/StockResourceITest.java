
package com.pay.coniq.web.rest;

import com.pay.coniq.StoreApp;
import com.pay.coniq.TestUtil;
import com.pay.coniq.domain.Stock;
import com.pay.coniq.repository.StockRepository;
import com.pay.coniq.service.StockService;
import com.pay.coniq.service.dto.StockDTO;
import com.pay.coniq.service.mapper.StockMapper;
import com.pay.coniq.web.rest.errors.ExceptionTranslator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration tests for the {@link StockResource} REST controller.
 */

@SpringBootTest(classes = StoreApp.class)
@ActiveProfiles("dev")
public class StockResourceITest {

    private static final String DEFAULT_NAME = "p1";
    private static final String UPDATED_NAME = "p2";

    private static final BigDecimal DEFAULT_CURRENT_PRICE = new BigDecimal("12.5");
    private static final BigDecimal UPDATED_CURRENT_PRICE = new BigDecimal("5.12");


    @Autowired
    private StockRepository stockRepository;


    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restStockMockMvc;

    private Stock stock;


    @BeforeEach
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        final StockResource stockResource = new StockResource(stockService);
        this.restStockMockMvc = MockMvcBuilders.standaloneSetup(stockResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(TestUtil.createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator).build();
    }


/**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */

    public static Stock createEntity(EntityManager em) {
        Stock stock = new Stock()
                .name(DEFAULT_NAME)
                .currentPrice(DEFAULT_CURRENT_PRICE);
        return stock;
    }



    @BeforeEach
    public void initTest() {
        stockRepository.deleteAll();
        stock = createEntity(em);
    }

    @Test
    @Transactional
    public void createStock() throws Exception {
        int databaseSizeBeforeCreate = stockRepository.findAll().size();

        // Create the Stock
        StockDTO stockDTO = stockMapper.toDto(stock);
        restStockMockMvc.perform(post("/api/stocks")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(stockDTO)))
                .andExpect(status().isCreated());

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeCreate + 1);
        Stock testStock = stockList.get(stockList.size() - 1);
        assertThat(testStock.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createStockWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = stockRepository.findAll().size();

        // Create the Stock with an existing ID
        stock.setId(1L);
        StockDTO stockDTO = stockMapper.toDto(stock);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStockMockMvc.perform(post("/api/stocks")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(stockDTO)))
                .andExpect(status().isBadRequest());

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllStocks() throws Exception {
        // Initialize the database
        stock = stockRepository.saveAndFlush(stock);

        // Get all the stockList
        restStockMockMvc.perform(get("/api/stocks?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].id").value(hasItem(stock.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
                .andExpect(jsonPath("$.[*].currentPrice").value(hasItem(DEFAULT_CURRENT_PRICE.doubleValue())));
    }

    @Test
    @Transactional
    public void getStock() throws Exception {
        // Initialize the database
        stock = stockRepository.saveAndFlush(stock);

        // Get the stock
        restStockMockMvc.perform(get("/api/stocks/{id}", stock.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stock.getId().intValue()))
                .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
                .andExpect(jsonPath("$.currentPrice").value(DEFAULT_CURRENT_PRICE));
    }

    @Test
    @Transactional
    public void getNonExistingStock() throws Exception {
        // Get the stock
        restStockMockMvc.perform(get("/api/stocks/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStock() throws Exception {
        // Initialize the database
        stock = stockRepository.saveAndFlush(stock);

        int databaseSizeBeforeUpdate = stockRepository.findAll().size();

        // Update the stock
        Stock updatedStock = stockRepository.findById(stock.getId()).get();
        // Disconnect from session so that the updates on updatedStock are not directly saved in db
        em.detach(updatedStock);
        updatedStock
                .name(UPDATED_NAME)
                .currentPrice(UPDATED_CURRENT_PRICE);
        StockDTO stockDTO = stockMapper.toDto(updatedStock);

        restStockMockMvc.perform(put("/api/stocks/{id}",stock.getId()).param("currentPrice",stockDTO.getCurrentPrice().toString()))
                .andExpect(status().isOk());

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeUpdate);
        Stock testStock = stockList.get(stockList.size() - 1);
        assertThat(testStock.getCurrentPrice()).isEqualTo(UPDATED_CURRENT_PRICE);
    }

    @Test
    @Transactional
    public void updateNonExistingStock() throws Exception {
        int databaseSizeBeforeUpdate = stockRepository.findAll().size();

        // Create the Stock
        StockDTO stockDTO = stockMapper.toDto(stock);

        // If the entity doesn't have an ID, it will throw MethodNotAllowedException
        restStockMockMvc.perform(put("/api/stocks/{id}",stock.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(stockDTO)))
                .andExpect(status().isMethodNotAllowed());

        // Validate the Stock in the database
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteStock() throws Exception {
        // Initialize the database
        stock = stockRepository.saveAndFlush(stock);

        int databaseSizeBeforeDelete = stockRepository.findAll().size();

        // Delete the stock
        restStockMockMvc.perform(delete("/api/stocks/{id}", stock.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Stock> stockList = stockRepository.findAll();
        assertThat(stockList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Stock.class);
        Stock stock1 = new Stock();
        stock1.setId(1L);
        Stock stock2 = new Stock();
        stock2.setId(stock1.getId());
        assertThat(stock1).isEqualTo(stock2);
        stock2.setId(2L);
        assertThat(stock1).isNotEqualTo(stock2);
        stock1.setId(null);
        assertThat(stock1).isNotEqualTo(stock2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(StockDTO.class);
        StockDTO stockDTO1 = new StockDTO();
        stockDTO1.setId(1L);
        StockDTO stockDTO2 = new StockDTO();
        assertThat(stockDTO1).isNotEqualTo(stockDTO2);
        stockDTO2.setId(stockDTO1.getId());
        assertThat(stockDTO1).isEqualTo(stockDTO2);
        stockDTO2.setId(2L);
        assertThat(stockDTO1).isNotEqualTo(stockDTO2);
        stockDTO1.setId(null);
        assertThat(stockDTO1).isNotEqualTo(stockDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        Assertions.assertThat(stockMapper.fromId(42L).getId()).isEqualTo(42);
        Assertions.assertThat(stockMapper.fromId(null)).isNull();
    }
}

