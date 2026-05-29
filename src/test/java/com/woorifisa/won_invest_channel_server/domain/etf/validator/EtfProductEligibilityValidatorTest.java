package com.woorifisa.won_invest_channel_server.domain.etf.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EtfProductEligibilityValidatorTest {

    private final EtfProductEligibilityValidator validator = new EtfProductEligibilityValidator();

    @Test
    @DisplayName("서비스 제공 조건을 모두 만족하면 true를 반환한다")
    void validate_whenProductIsEligible_returnsTrue() {
        // given
        ExternalEtfProduct product = product(
                true,
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                true,
                true
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("ETF 상품이 아니면 false를 반환한다")
    void validate_whenProductIsNotEtf_returnsFalse() {
        // given
        ExternalEtfProduct product = product(
                false,
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                true,
                true
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("USD 거래 상품이 아니면 false를 반환한다")
    void validate_whenProductCurrencyIsNotUsd_returnsFalse() {
        // given
        ExternalEtfProduct product = product(
                true,
                null,
                EtfProductStatus.ACTIVE,
                true,
                true
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("거래 가능 상품이 아니면 false를 반환한다")
    void validate_whenProductIsNotTradeAvailable_returnsFalse() {
        // given
        ExternalEtfProduct product = product(
                true,
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                true,
                false
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("상품 상태가 비활성 상태이면 -> false를 반환한다")
    void validate_whenProductStatusIsInactive_returnsFalse() {
        // given
        ExternalEtfProduct product = product(
                true,
                EtfCurrency.USD,
                EtfProductStatus.INACTIVE,
                true,
                true
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("소수점 매수 가능 상품이 아니면 false를 반환한다")
    void validate_whenProductIsNotFractionalAvailable_returnsFalse() {
        // given
        ExternalEtfProduct product = product(
                true,
                EtfCurrency.USD,
                EtfProductStatus.ACTIVE,
                false,
                true
        );

        // when
        boolean result = validate(product);

        // then
        assertThat(result).isFalse();
    }

    private ExternalEtfProduct product(
            boolean isEtf,
            EtfCurrency currency,
            EtfProductStatus productStatus,
            boolean isFractionalAvailable,
            boolean isTradeAvailable
    ) {
        return new ExternalEtfProduct(
                "KIS",
                "US78462F1030",
                "SPY",
                "US78462F1030",
                "STATE STREET SPDR S&P 500 ETF",
                "S&P 500 지수를 추종하는 대표 미국 시장 ETF",
                "AMEX",
                currency,
                productStatus,
                EtfRiskGrade.MEDIUM,
                isEtf,
                isFractionalAvailable,
                isTradeAvailable,
                1
        );
    }

    private boolean validate(ExternalEtfProduct product) {
        try {
            Method method = Arrays.stream(EtfProductEligibilityValidator.class.getDeclaredMethods())
                    .filter(candidate -> candidate.getParameterCount() == 1)
                    .filter(candidate -> candidate.getParameterTypes()[0].equals(ExternalEtfProduct.class))
                    .filter(candidate -> candidate.getReturnType().equals(boolean.class)
                            || candidate.getReturnType().equals(Boolean.class))
                    .sorted(Comparator.comparingInt(this::methodPriority))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "ExternalEtfProduct를 인자로 받고 boolean을 반환하는 검증 메서드를 찾지 못했습니다."
                    ));

            method.setAccessible(true);

            Object result = method.invoke(validator, product);

            if (result instanceof Boolean booleanResult) {
                return booleanResult;
            }

            throw new IllegalStateException("지원하지 않는 검증 결과 타입입니다. resultType=" + result.getClass());

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ETF 상품 제공 조건 검증 테스트 실행 중 오류가 발생했습니다.", e);
        }
    }

    private int methodPriority(Method method) {
        String methodName = method.getName();

        if (methodName.equals("isEligible")) {
            return 0;
        }

        if (methodName.equals("validate")) {
            return 1;
        }

        if (methodName.equals("isValid")) {
            return 2;
        }

        if (methodName.equals("isServiceAvailable")) {
            return 3;
        }

        return 10;
    }
}