package com.woorifisa.won_invest_channel_server.domain.etf.service;

import com.woorifisa.won_invest_channel_server.domain.etf.client.CoreEtfProductClient;
import com.woorifisa.won_invest_channel_server.domain.etf.client.KisOverseasProductInfoClient;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestChnEtfProductService {

    private final CuratedEtfProductCandidateProvider candidateProvider;
    private final KisOverseasProductInfoClient kisOverseasProductInfoClient;
    private final KisEtfProductMapper kisEtfProductMapper;
    private final EtfProductEligibilityValidator etfProductEligibilityValidator;
    private final CoreEtfProductClient coreEtfProductClient;
    private final InvestChnEtfProductCommandService investChnEtfProductCommandService;

    public EtfProductSyncResult syncCuratedEtfProducts() {
        List<CuratedEtfProductCandidate> candidates = candidateProvider.getCandidates();

        int syncedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        List<EtfProductSyncResult.Item> skippedItems = new ArrayList<>();
        List<EtfProductSyncResult.Item> failedItems = new ArrayList<>();

        // 1. 관리자가 입력한 ETF 목록 각각에 대해
        for (CuratedEtfProductCandidate candidate : candidates) {
            try {   // 2. KIS에 정보 조회
                KisOverseasProductInfoResponse kisResponse =
                        kisOverseasProductInfoClient.getProductInfo(
                                candidate.productTypeCode(),
                                candidate.ticker()
                        );

                // 3. 응답을 ExternalEtfProduct 로 반환
                ExternalEtfProduct externalEtfProduct =
                        kisEtfProductMapper.toExternalEtfProduct(candidate, kisResponse);

                // 4. 가능 조건 검사
                List<String> ineligibleReasons =
                        etfProductEligibilityValidator.findIneligibleReasons(externalEtfProduct);

                // 5. 통과하면 -> Core에 저장 요청
                if (!ineligibleReasons.isEmpty()) {
                    skippedCount++;
                    skippedItems.add(new EtfProductSyncResult.Item(
                            candidate.ticker(),
                            String.join(", ", ineligibleReasons)
                    ));
                    continue;
                }

                CoreEtfProductUpsertRequest coreRequest =
                        CoreEtfProductUpsertRequest.from(externalEtfProduct);

                CoreEtfProductUpsertResponse coreResponse =
                        coreEtfProductClient.upsertEtfProduct(coreRequest);

                // 6. 최종 채널 DB upsert
                investChnEtfProductCommandService.upsertFromCoreEtfId(
                        coreResponse.etfId(),
                        externalEtfProduct
                );

                syncedCount++;
            } catch (Exception e) {
                failedCount++;
                failedItems.add(new EtfProductSyncResult.Item(
                        candidate.ticker(),
                        e.getMessage()
                ));
            }
        }

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