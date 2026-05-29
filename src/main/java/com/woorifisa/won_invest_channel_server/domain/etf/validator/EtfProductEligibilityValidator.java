package com.woorifisa.won_invest_channel_server.domain.etf.validator;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EtfProductEligibilityValidator {

    // 최종 통과/실패
    // 조건 : 1.ETF인지? 2.ACTIVE 인지? 3.USD 인지? 4.거래 가능한지? 5.소수점 매수 가능한지?
    public boolean isEligible(ExternalEtfProduct product) {
        return findIneligibleReasons(product).isEmpty();
    }

    // 실패 - 사유
    public List<String> findIneligibleReasons(ExternalEtfProduct product) {
        if (product == null) {
            return List.of("ETF 상품 정보가 없습니다.");
        }

        List<String> reasons = new ArrayList<>();

        if (!hasText(product.externalProvider())) {
            reasons.add("외부 제공자 정보가 없습니다.");
        }

        if (!hasText(product.ticker())) {
            reasons.add("ETF 티커가 없습니다.");
        }

        if (!hasText(product.etfName())) {
            reasons.add("ETF 상품명이 없습니다.");
        }

        if (!product.isEtf()) {
            reasons.add("ETF 상품이 아닙니다.");
        }

        if (product.productStatus() != EtfProductStatus.ACTIVE) {
            reasons.add("ACTIVE 상태가 아닙니다.");
        }

        if (product.currency() != EtfCurrency.USD) {
            reasons.add("USD 상품이 아닙니다.");
        }

        if (!product.isTradeAvailable()) {
            reasons.add("거래 가능 상품이 아닙니다.");
        }

        if (!product.isFractionalAvailable()) {
            reasons.add("소수점 매수 가능 상품이 아닙니다.");
        }

        return List.copyOf(reasons);
    }

    // 실패 - 예외 주기
    public void validateEligible(ExternalEtfProduct product) {
        List<String> reasons = findIneligibleReasons(product);

        if (!reasons.isEmpty()) {
            String ticker = product == null ? "UNKNOWN" : product.ticker();

            throw new IllegalArgumentException(
                    "서비스 제공 불가 ETF입니다. ticker=" + ticker + ", reasons=" + reasons
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}