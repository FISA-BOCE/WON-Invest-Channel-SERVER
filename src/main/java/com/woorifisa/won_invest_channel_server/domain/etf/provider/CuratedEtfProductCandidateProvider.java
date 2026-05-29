package com.woorifisa.won_invest_channel_server.domain.etf.provider;

import com.woorifisa.won_invest_channel_server.domain.etf.dto.sync.CuratedEtfProductCandidate;
import java.util.List;

public interface CuratedEtfProductCandidateProvider {

    List<CuratedEtfProductCandidate> getCandidates();
}