package com.woorifisa.won_invest_channel_server.domain.etf.dto.sync;

import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;

/**
 * WON해요 서비스 제공 대상으로 사전에 선별한 ETF 후보 DTO
 *
 * KIS 전체 종목 목록을 수집하지 않고,
 * 서비스 기획 기준으로 지정한 ETF만 KIS 상품기본정보 API로 검증하기 위한 입력값
 */

public record CuratedEtfProductCandidate(

        // 외부 API 제공자 - 현재는 KIS 기준으로 사용
        String externalProvider,

        // KIS 상품유형코드 - ex) 512 = 미국 나스닥, 513 = 미국 뉴욕, 529 = 미국 아멕스
        String productTypeCode,

        //상품번호: KIS API 요청의 PDNO로 사용 - ex)VOO, QQQ, SPY
        String ticker,

        //화면 표시용 설명 - invest_chn_etf_product.description에 저장
        String description,

        // 화면 표시용 위험 등급 - invest_chn_etf_product.risk_grade에 저장
        EtfRiskGrade riskGrade,

        // 화면 노출 순서 - invest_chn_etf_product.display_order에 저장
        Integer displayOrder
) {

    public static CuratedEtfProductCandidate kis(
            String productTypeCode,
            String ticker,
            String description,
            EtfRiskGrade riskGrade,
            Integer displayOrder
    ) {
        return new CuratedEtfProductCandidate(
                "KIS",
                productTypeCode,
                ticker,
                description,
                riskGrade,
                displayOrder
        );
    }
}