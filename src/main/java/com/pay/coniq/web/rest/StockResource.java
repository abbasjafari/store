package com.pay.coniq.web.rest;

import com.pay.coniq.service.StockService;
import com.pay.coniq.service.dto.StockDTO;
import com.pay.coniq.web.rest.errors.BadRequestAlertException;
import com.pay.coniq.web.rest.util.HeaderUtil;
import com.pay.coniq.web.rest.util.PaginationUtil;
import com.pay.coniq.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Rest services related to Stock actions
 * some methods are {@link #createStock(StockDTO)}
 * some methods are {@link #updateStock(Long,BigDecimal)}
 * some methods are {@link #getAllStocks(Pageable)}
 * some methods are {@link #getStock(Long)}
 * */
@RestController
@RequestMapping("/api")
public class StockResource {

    private final Logger log = LoggerFactory.getLogger(StockResource.class);

    private static final String ENTITY_NAME = "Stock";

    @Value("${store.clientApp.name}")
    private String applicationName;

    private final StockService stockService;

    public StockResource(StockService stockService) throws URISyntaxException {
        this.stockService = stockService;
    }

    @PostMapping("/stocks")
    public ResponseEntity<StockDTO> createStock(@RequestBody StockDTO stockDTO) throws URISyntaxException {
        log.debug("REST request to save Stock : {}", stockDTO);
        if (stockDTO.getId() != null) {
            throw new BadRequestAlertException("A new stock cannot already have an ID", ENTITY_NAME, "idexists");
        }
        StockDTO result = stockService.save(stockDTO);
        return ResponseEntity.created(new URI("/api/stocks/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/stocks/{id}")
    public ResponseEntity<StockDTO> updateStock(@PathVariable Long id,@RequestParam BigDecimal currentPrice) throws URISyntaxException {
        log.debug("REST request to update currentPrice :", id,currentPrice);
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        StockDTO result = stockService.save(id,currentPrice);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,id.toString()))
                .body(result);
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<StockDTO>> getAllStocks(Pageable pageable) {
        log.debug("REST request to get all Stocks");
        Page<StockDTO> page =  stockService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }


    @GetMapping("/stocks/{id}")
    public ResponseEntity<StockDTO> getStock(@PathVariable Long id) {
        log.debug("REST request to get Stock : {}", id);
        Optional<StockDTO> stockDTO = stockService.findOne(id);
        return ResponseUtil.wrapOrNotFound(stockDTO);
    }


    @DeleteMapping("/stocks/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        log.debug("REST request to delete Stock : {}", id);
        stockService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

}
