package com.pay.coniq.service;

import com.pay.coniq.domain.Stock;
import com.pay.coniq.repository.StockRepository;
import com.pay.coniq.service.dto.StockDTO;
import com.pay.coniq.service.mapper.StockMapper;
import com.pay.coniq.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockService {

    private final Logger log = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;

    private final StockMapper stockMapper;
    private static final String ENTITY_NAME = "Stock";


    public StockService(StockRepository stockRepository, StockMapper stockMapper) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
    }


    /**
     * saving stock
     * see also {@link StockDTO}
     */
    public StockDTO save(StockDTO stockDTO) {
        log.debug("Request to save Stock : {}", stockDTO);
        Stock stock = stockMapper.toEntity(stockDTO);
        stock = stockRepository.save(stock);
        StockDTO result = stockMapper.toDto(stock);
        return result;
    }
    /**
     * update stock
     * see also {@link StockDTO}
     */
    public StockDTO save(Long id, BigDecimal currentPrice) {
        log.debug("Request to update Stock : ", id,currentPrice);
        Optional<Stock> stockOptional = stockRepository.findById(id);
        Stock stock ;
        if (stockOptional.isPresent()) {
            stock=stockOptional.get();
            stock.setCurrentPrice(currentPrice);

        }else {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, id.toString());
        }
        stock = stockRepository.save(stock);
        StockDTO result = stockMapper.toDto(stock);
        return result;
    }
        /**
         * getting all stocks converted to StockDTO
         * see also {@link StockDTO}
         */
    @Transactional(readOnly = true)
    public Page<StockDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Stocks");
        return stockRepository.findAll(pageable).map(stockMapper::toDto);
    }

    /**
     * finding stock base on stock id
     */
    @Transactional(readOnly = true)
    public Optional<StockDTO> findOne(Long id) {
        log.debug("Request to get Stock : {}", id);
        return stockRepository.findById(id)
                .map(stockMapper::toDto);
    }

    /**
     * deleting stock base on stock id
     */
    public void delete(Long id) {
        log.debug("Request to delete Stock : {}", id);
        stockRepository.deleteById(id);
    }


}

