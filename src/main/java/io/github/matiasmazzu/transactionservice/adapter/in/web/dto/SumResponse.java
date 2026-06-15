package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

import java.math.BigDecimal;

public record SumResponse(double sum) {

    public static SumResponse of(BigDecimal sum) {
        return new SumResponse(sum.doubleValue());
    }
}
