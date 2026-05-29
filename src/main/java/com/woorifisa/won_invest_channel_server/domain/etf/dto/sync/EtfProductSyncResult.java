package com.woorifisa.won_invest_channel_server.domain.etf.dto.sync;

import java.util.List;

public record EtfProductSyncResult(

        int totalCount,

        int syncedCount,

        int skippedCount,

        int failedCount,

        List<Item> skippedItems,

        List<Item> failedItems
) {

    public EtfProductSyncResult {
        skippedItems = skippedItems == null ? List.of() : List.copyOf(skippedItems);
        failedItems = failedItems == null ? List.of() : List.copyOf(failedItems);
    }

    public record Item(
            String ticker,
            String reason
    ) {
    }
}