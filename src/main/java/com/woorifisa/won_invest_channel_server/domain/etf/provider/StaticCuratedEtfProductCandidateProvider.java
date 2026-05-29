package com.woorifisa.won_invest_channel_server.domain.etf.provider;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import com.woorifisa.won_invest_channel_server.domain.etf.model.type.EtfRiskGrade;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StaticCuratedEtfProductCandidateProvider implements CuratedEtfProductCandidateProvider {

    private static final String NASDAQ = "512";
    private static final String NYSE = "513";
    private static final String AMEX = "529";

    @Override
    public List<CuratedEtfProductCandidate> getCandidates() {
        return List.of(
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "SPY",
                        "S&P 500 지수를 추종하는 대표 미국 시장 ETF",
                        EtfRiskGrade.MEDIUM,
                        1
                ),
                CuratedEtfProductCandidate.kis(
                        NASDAQ,
                        "QQQ",
                        "나스닥100 지수를 추종하는 대표 성장형 ETF",
                        EtfRiskGrade.HIGH,
                        2
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "SCHD",
                        "미국 배당주 중심의 대표 배당 ETF",
                        EtfRiskGrade.MEDIUM,
                        3
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "VOO",
                        "S&P 500 지수를 추종하는 장기 투자형 ETF",
                        EtfRiskGrade.MEDIUM,
                        4
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "XLP",
                        "필수소비재 섹터에 투자하는 방어형 ETF",
                        EtfRiskGrade.LOW,
                        5
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "XLK",
                        "미국 대형 기술주 섹터에 투자하는 ETF",
                        EtfRiskGrade.HIGH,
                        6
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "XLE",
                        "미국 에너지 섹터에 투자하는 ETF",
                        EtfRiskGrade.HIGH,
                        7
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "XLV",
                        "미국 헬스케어 섹터에 투자하는 ETF",
                        EtfRiskGrade.MEDIUM,
                        8
                ),
                CuratedEtfProductCandidate.kis(
                        AMEX,
                        "XLY",
                        "미국 임의소비재 섹터에 투자하는 ETF",
                        EtfRiskGrade.HIGH,
                        9
                )
        );
    }
}
