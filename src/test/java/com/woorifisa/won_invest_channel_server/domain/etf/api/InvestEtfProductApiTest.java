package com.woorifisa.won_invest_channel_server.domain.etf.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.response.InvestEtfProductDetailResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import com.woorifisa.won_invest_channel_server.domain.etf.service.InvestEtfProductQueryService;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.security.AuthenticatedUser;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class InvestEtfProductApiTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private InvestEtfProductQueryService investEtfProductQueryService;

    @InjectMocks
    private InvestEtfProductApi investEtfProductApi;

    @Test
    @DisplayName("인증된 요청이면 ETF 상세 조회 결과를 공통 응답 포맷으로 반환한다")
    void getEtfProductDetail_success() {
        InvestEtfProductDetailResponse detailResponse = new InvestEtfProductDetailResponse(
                1L,
                "Vanguard S&P 500 ETF",
                "VOO",
                "AMEX",
                EtfCurrency.USD,
                EtfRiskGrade.MEDIUM
        );

        given(investEtfProductQueryService.getEtfProductDetail(1L)).willReturn(detailResponse);

        ResponseEntity<ApiResponse<InvestEtfProductDetailResponse>> response =
                investEtfProductApi.getEtfProductDetail(
                        new AuthenticatedUser(USER_UUID),
                        "WOORI-FISA-APP-01",
                        "TX-20260604-ETF01",
                        1L
                );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVEST_200_004");
        assertThat(response.getBody().data()).isEqualTo(detailResponse);
    }

    @Test
    @DisplayName("인증 정보 없으면 UNAUTHORIZED 예외 발생")
    void getEtfProductDetail_unauthorized() {
        assertThatThrownBy(() -> investEtfProductApi.getEtfProductDetail(
                null,
                "WOORI-FISA-APP-01",
                "TX-20260604-ETF01",
                1L
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.UNAUTHORIZED));
    }
}
