package com.woorifisa.won_invest_channel_server.domain.etf.dto.response;

import java.util.List;

public record EtfProductListResponse(
        List<EtfProductSummaryResponse> etfs
) {

    public EtfProductListResponse {
        etfs = etfs == null ? List.of() : List.copyOf(etfs);
    }

    public static EtfProductListResponse of(List<EtfProductSummaryResponse> etfs) {
        return new EtfProductListResponse(etfs);
    }
}
