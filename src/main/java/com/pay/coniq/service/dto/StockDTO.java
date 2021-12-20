package com.pay.coniq.service.dto;

import com.pay.coniq.domain.Stock;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO class relative to {@link Stock}
 * */
@Data
public class StockDTO implements Serializable {

    private Long id;
    private String name;
    private BigDecimal currentPrice;
    private String createdDate;
    private String modifiedDate;

    /**
     * set name using builder pattern
     * */
    public StockDTO name(String name) {
        this.name = name;
        return this;
    }
    /**
     * set currentPrice using builder pattern
     * */
    public StockDTO currentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StockDTO stockDTO = (StockDTO) o;
        if (stockDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), stockDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
