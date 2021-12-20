package com.pay.coniq.service.mapper;

import com.pay.coniq.domain.Stock;
import com.pay.coniq.service.dto.StockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * StockMapper using Entity mapper
 * see {@link StockDTO} and {@link Stock}
 * */
@Mapper(componentModel = "spring", uses = {})
public interface StockMapper extends EntityMapper<StockDTO, Stock> {


    @Override
    @Mapping(source = "createdDate", target = "createdDate", dateFormat = "yyyy MMM dd HH:mm:ss.SSS")
    @Mapping(source = "modifiedDate", target = "modifiedDate", dateFormat = "yyyy MMM dd HH:mm:ss.SSS")
    StockDTO toDto(Stock entity);

    @Override
    @Mapping(target = "createdDate",ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    Stock toEntity(StockDTO dto);



    default Stock fromId(Long id) {
        if (id == null) {
            return null;
        }
        Stock stock = new Stock();
        stock.setId(id);
        return stock;
    }
}
