package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.external.CoreEtfProductApi;
import com.woorifisa.won_invest_channel_server.domain.etf.external.KisOverseasProductInfoApi;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.request.CoreEtfProductUpsertRequest;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.core.response.CoreEtfProductUpsertResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.kis.response.KisOverseasProductInfoResponse;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.EtfProductSyncResult;
import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.ExternalEtfProduct;
import com.woorifisa.won_invest_channel_server.domain.etf.mapper.KisEtfProductMapper;
import com.woorifisa.won_invest_channel_server.domain.etf.provider.CuratedEtfProductCandidateProvider;
import com.woorifisa.won_invest_channel_server.domain.etf.validator.EtfProductEligibilityValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.woorifisa.won_invest_channel_server.domain.etf.exception.code.EtfErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestChnEtfProductSyncService {

    private final CuratedEtfProductCandidateProvider candidateProvider;
    private final KisOverseasProductInfoApi kisOverseasProductInfoApi;
    private final KisEtfProductMapper kisEtfProductMapper;
    private final EtfProductEligibilityValidator etfProductEligibilityValidator;
    private final CoreEtfProductApi coreEtfProductApi;
    private final InvestChnEtfProductCommandService investChnEtfProductCommandService;


    public EtfProductSyncResult syncCuratedEtfProducts() {
        List<CuratedEtfProductCandidate> candidates = candidateProvider.getCandidates();

        String traceId = UUID.randomUUID().toString().substring(0, 8);

        log.info("[ETF_FLOW] traceId={} | 00_START | totalCandidates={}",
                traceId,
                candidates.size()
        );

        int syncedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        List<EtfProductSyncResult.Item> skippedItems = new ArrayList<>();
        List<EtfProductSyncResult.Item> failedItems = new ArrayList<>();

        // 1. 관리자가 입력한 ETF 목록 각각에 대해
        for (CuratedEtfProductCandidate candidate : candidates) {
            try {   // 2. KIS에 정보 조회

                log.info("[ETF_FLOW] traceId={} | 01_KIS_REQUEST | CHN->KIS | ticker={} | productTypeCode={}",
                        traceId,
                        candidate.ticker(),
                        candidate.productTypeCode()
                );

                KisOverseasProductInfoResponse kisResponse =
                        kisOverseasProductInfoApi.getProductInfo(
                                candidate.productTypeCode(),
                                candidate.ticker()
                        );

                KisOverseasProductInfoResponse.Output output = kisResponse.output();

                log.info("[ETF_FLOW] traceId={} | 02_KIS_RESPONSE | KIS->CHN | ticker={} | stdPdno={} | isin={} | prdtEngName={} | trCrcyCd={} | etfRiskCode={} | lstgYn={} | abolishedYn={} | tradeStopCode={} | mintSvcYn={} | miniStatus={} | fractionalYn={}",
                        traceId,
                        candidate.ticker(),
                        output.stdPdno(),
                        output.isttUsgeIsinCd(),
                        output.prdtEngName(),
                        output.trCrcyCd(),
                        output.ovrsStckEtfRiskDrtpCd(),
                        output.lstgYn(),
                        output.lstgAbolItemYn(),
                        output.ovrsStckTrStopDvsnCd(),
                        output.mintSvcYn(),
                        output.miniStkTrStatDvsnCd(),
                        output.mintDcptTradPsblYn()
                );

                // 3. 응답을 ExternalEtfProduct 로 반환
                ExternalEtfProduct externalEtfProduct =
                        kisEtfProductMapper.toExternalEtfProduct(candidate, kisResponse);

                log.info("[ETF_FLOW] traceId={} | 03_MAPPED | KIS_DTO->CHANNEL_DTO | ticker={} | externalEtfId={} | isin={} | etfName={} | market={} | currency={} | productStatus={} | riskGrade={} | isEtf={} | isTradeAvailable={} | isFractionalAvailable={} | displayOrder={}",
                        traceId,
                        externalEtfProduct.ticker(),
                        externalEtfProduct.externalEtfId(),
                        externalEtfProduct.isin(),
                        externalEtfProduct.etfName(),
                        externalEtfProduct.market(),
                        externalEtfProduct.currency(),
                        externalEtfProduct.productStatus(),
                        externalEtfProduct.riskGrade(),
                        externalEtfProduct.isEtf(),
                        externalEtfProduct.isTradeAvailable(),
                        externalEtfProduct.isFractionalAvailable(),
                        externalEtfProduct.displayOrder()
                );

                // 4. 가능 조건 검사
                List<String> ineligibleReasons =
                        etfProductEligibilityValidator.findIneligibleReasons(externalEtfProduct);

                log.info("[ETF_FLOW] traceId={} | 04_VALIDATED | CHANNEL | ticker={} | eligible={} | reasons={}",
                        traceId,
                        externalEtfProduct.ticker(),
                        ineligibleReasons.isEmpty(),
                        ineligibleReasons
                );


                // 5. 통과하면 -> Core에 저장 요청
                if (!ineligibleReasons.isEmpty()) {
                    skippedCount++;
                    String skipReason = String.join(", ", ineligibleReasons);

                    log.info("[ETF_FLOW] traceId={} | 04_SKIPPED | CHANNEL | ticker={} | reason={}",
                            traceId,
                            externalEtfProduct.ticker(),
                            skipReason
                    );

                    skippedItems.add(new EtfProductSyncResult.Item(
                            candidate.ticker(),
                            String.join(", ", ineligibleReasons)
                    ));
                    continue;
                }

                CoreEtfProductUpsertRequest coreRequest =
                        CoreEtfProductUpsertRequest.from(externalEtfProduct);

                log.info("[ETF_FLOW] traceId={} | 05_CORE_REQUEST | CHN->CORE | ticker={} | externalProvider={} | externalEtfId={} | isin={} | etfName={} | market={} | currency={} | productStatus={} | riskGrade={}",
                        traceId,
                        externalEtfProduct.ticker(),
                        externalEtfProduct.externalProvider(),
                        externalEtfProduct.externalEtfId(),
                        externalEtfProduct.isin(),
                        externalEtfProduct.etfName(),
                        externalEtfProduct.market(),
                        externalEtfProduct.currency(),
                        externalEtfProduct.productStatus(),
                        externalEtfProduct.riskGrade()
                );

                CoreEtfProductUpsertResponse coreResponse =
                        coreEtfProductApi.upsertEtfProduct(coreRequest);

                log.info("[ETF_FLOW] traceId={} | 06_CORE_RESPONSE | CORE->CHN | ticker={} | coreEtfId={}",
                        traceId,
                        externalEtfProduct.ticker(),
                        coreResponse.etfId()
                );

                // 6. 최종 채널 DB upsert
                investChnEtfProductCommandService.upsertFromCoreEtfId(
                        coreResponse.etfId(),
                        externalEtfProduct
                );

                log.info("[ETF_FLOW] traceId={} | 07_CHANNEL_UPSERT | CHN->CHN_DB | ticker={} | etfId={} | externalEtfId={} | displayOrder={}",
                        traceId,
                        externalEtfProduct.ticker(),
                        coreResponse.etfId(),
                        externalEtfProduct.externalEtfId(),
                        externalEtfProduct.displayOrder()
                );

                syncedCount++;
            } catch (Exception e) {
                failedCount++;

                log.error("[ETF_FLOW] traceId={} | 99_FAILED | ticker={} | message={}",
                        traceId,
                        candidate.ticker(),
                        e.getMessage(),
                        e
                );

                failedItems.add(new EtfProductSyncResult.Item(
                        candidate.ticker(),
                        EtfErrorCode.ETF_SYNC_FAILED.getMessage()
                ));
            }
        }

        log.info("[ETF_FLOW] traceId={} | 99_END | totalCount={} | syncedCount={} | skippedCount={} | failedCount={}",
                traceId,
                candidates.size(),
                syncedCount,
                skippedCount,
                failedCount
        );

        return new EtfProductSyncResult(
                candidates.size(),
                syncedCount,
                skippedCount,
                failedCount,
                skippedItems,
                failedItems
        );
    }
}
