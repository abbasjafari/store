package com.pay.coniq.domain;

import com.pay.coniq.service.dto.StockDTO;
import lombok.Data;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * ACCOUNT entity object
 * see also {@link StockDTO}
 * */
@Entity
@Data
@Audited
public class Stock extends AbstractTimestampEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @NotNull
    private String name;
    @Column
    @NotNull
    private BigDecimal currentPrice;



    /**
     * set name using builder pattern
     * */
    public Stock name(String name) {
        this.name = name;
        return this;
    }
    /**
     * set currentPrice using builder pattern
     * */
    public Stock currentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        return this;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stock)) {
            return false;
        }
        return id != null && id.equals(((Stock) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
