package com.woorifisa.won_invest_channel_server.domain.etf.model.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EtfRiskGrade {

    VERY_LOW("매우 낮은 위험", "가격 변동성이 낮은 ETF"),
    LOW("낮은 위험", "비교적 안정적인 ETF"),
    MEDIUM("보통 위험", "일반적인 수준의 변동성을 가진 ETF"),
    HIGH("높은 위험", "가격 변동성이 큰 ETF"),
    VERY_HIGH("매우 높은 위험", "손실 가능성과 변동성이 매우 큰 ETF"),
    UNKNOWN("위험등급 미분류", "위험등급 정보가 아직 확정되지 않은 ETF");

    private final String label;
    private final String description;
}