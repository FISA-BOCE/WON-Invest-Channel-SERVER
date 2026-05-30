package com.woorifisa.won_invest_channel_server.domain.etf.mapper;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfProductStatus;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import com.woorifisa.won_invest_channel_server.domain.etf.exception.EtfSyncException;
import org.springframework.stereotype.Component;

@Component
public class KisEtfProductMapper {

    private static final String YES = "Y";
    private static final String NO = "N";

    private static final String ETF_CODE = "03";
    private static final String ETF_RISK_CODE = "001";
    private static final String NORMAL_TRADING_STATUS_CODE = "01";
    private static final String MINI_STOCK_NORMAL_STATUS_CODE = "01";
    private static final String USD = "USD";

    public ExternalEtfProduct toExternalEtfProduct(
            // 입력 정보, KIS 응답 받음
            CuratedEtfProductCandidate candidate,
            KisOverseasProductInfoResponse response
    ) {
        validate(candidate, response);

        KisOverseasProductInfoResponse.Output output = response.output();


        boolean isEtf = isEtf(output);                                  // ETF 여부 확인
        boolean isTradeAvailable = isTradeAvailable(output);            // 거래 가능 여부 계산
        boolean isFractionalAvailable = isFractionalAvailable(output);  // 소수점 거래 가능 여부 계산

        EtfProductStatus productStatus = isTradeAvailable
                ? EtfProductStatus.ACTIVE
                : EtfProductStatus.INACTIVE;

        return new ExternalEtfProduct(
                candidate.externalProvider(),
                firstNonBlank(output.stdPdno(), candidate.ticker()),
                candidate.ticker(),
                output.isttUsgeIsinCd(),
                firstNonBlank(output.prdtEngName(), output.prdtName(), candidate.ticker()),
                candidate.description(),
                firstNonBlank(output.ovrsExcgCd(), output.ovrsExcgName(), output.trMketName()),
                toCurrency(output.trCrcyCd()),
                productStatus,
                candidate.riskGrade(),
                isEtf,
                isFractionalAvailable,
                isTradeAvailable,
                candidate.displayOrder()
        );
    }

    private void validate(
            CuratedEtfProductCandidate candidate,
            KisOverseasProductInfoResponse response
    ) {
        if (candidate == null) {
            throw new EtfSyncException(EtfErrorCode.ETF_CANDIDATE_EMPTY);
        }

        if (response == null || response.output() == null) {
            throw new EtfSyncException(EtfErrorCode.KIS_PRODUCT_RESPONSE_EMPTY);
        }
    }

    private boolean isEtf(KisOverseasProductInfoResponse.Output output) {
        return equalsCode(output.ovrsStckDvsnCd(), ETF_CODE)
                || equalsCode(output.ovrsStckEtfRiskDrtpCd(), ETF_RISK_CODE)
                || containsIgnoreCase(output.prdtClsfName(), "ETF")
                || containsIgnoreCase(output.prdtEngName(), "ETF")
                || containsIgnoreCase(output.prdtName(), "ETF");
    }

    private boolean isTradeAvailable(KisOverseasProductInfoResponse.Output output) {
        return equalsCode(output.lstgYn(), YES)
                && equalsCode(output.lstgAbolItemYn(), NO)
                && equalsCode(output.ovrsStckTrStopDvsnCd(), NORMAL_TRADING_STATUS_CODE);
    }

    private boolean isFractionalAvailable(KisOverseasProductInfoResponse.Output output) {
        return equalsCode(output.mintSvcYn(), YES)
                && equalsCode(output.miniStkTrStatDvsnCd(), MINI_STOCK_NORMAL_STATUS_CODE)
                && equalsCode(output.mintDcptTradPsblYn(), YES);
    }

    private boolean equalsCode(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private EtfCurrency toCurrency(String value) {
        if (!hasText(value)) {
            return null;
        }

        try {
            return EtfCurrency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }

        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
