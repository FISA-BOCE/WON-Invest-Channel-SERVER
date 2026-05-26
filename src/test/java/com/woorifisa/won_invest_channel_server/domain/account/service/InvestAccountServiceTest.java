package com.woorifisa.won_invest_channel_server.domain.account.service;

import com.woorifisa.won_invest_channel_server.domain.account.dto.request.LinkAccountRequest;
import com.woorifisa.won_invest_channel_server.domain.account.dto.response.LinkAccountResponse;
import com.woorifisa.won_invest_channel_server.domain.account.exception.code.InvestAccountErrorCode;
import com.woorifisa.won_invest_channel_server.domain.account.external.CommonMappingApi;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.LinkInvestMappingRequest;
import com.woorifisa.won_invest_channel_server.domain.account.external.dto.MappingStatusResponse;
import com.woorifisa.won_invest_channel_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_channel_server.domain.account.model.InvestChnAccountSummary;
import com.woorifisa.won_invest_channel_server.domain.account.repository.InvestChnAccountSummaryRepository;
import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvestAccountServiceTest {

    @Mock
    private CommonMappingApi commonMappingApi;

    @Mock
    private InvestChnAccountSummaryRepository accountSummaryRepository;

    @InjectMocks
    private InvestAccountService investAccountService;

    @Test
    @DisplayName("정상 연결 성공")
    void linkAccount_success() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID investUserUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.ACTIVE);
        given(accountSummary.getInvestAccountUuid()).willReturn(investAccountUuid);
        given(accountSummary.getInvestUserUuid()).willReturn(investUserUuid);
        given(accountSummary.getAccountNoDisplay()).willReturn("123-456-789");

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when
        LinkAccountResponse response = investAccountService.linkAccount(userUuid, request);

        // then
        assertThat(response.investAccountUuid()).isEqualTo(investAccountUuid);
        assertThat(response.accountNoDisplay()).isEqualTo("123-456-789");
        assertThat(response.investConnectedStatus()).isTrue();
        verify(commonMappingApi).linkInvestMapping(eq(userUuid), eq(new LinkInvestMappingRequest(investUserUuid)));
    }

    @Test
    @DisplayName("이미 연결된 경우 ALREADY_LINKED 예외 발생")
    void linkAccount_alreadyLinked() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(true));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ALREADY_LINKED));
    }

    @Test
    @DisplayName("계좌 없는 경우 ACCOUNT_NOT_FOUND 예외 발생")
    void linkAccount_accountNotFound() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Test
    @DisplayName("본인 계좌가 아닌 경우 NOT_ACCOUNT_OWNER 예외 발생")
    void linkAccount_notAccountOwner() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID otherUserUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(otherUserUuid);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.NOT_ACCOUNT_OWNER));
    }

    @Test
    @DisplayName("getMappingStatus Feign 오류 시 BAD_GATEWAY 예외 발생")
    void linkAccount_getMappingStatus_feignException() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        FeignException getStatusException = mock(FeignException.class);
        given(commonMappingApi.getMappingStatus(userUuid)).willThrow(getStatusException);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("linkInvestMapping Feign 오류 시 BAD_GATEWAY 예외 발생")
    void linkAccount_linkInvestMapping_feignException() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();
        UUID investUserUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.ACTIVE);
        given(accountSummary.getInvestUserUuid()).willReturn(investUserUuid);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));
        FeignException linkMappingException = mock(FeignException.class);
        willThrow(linkMappingException).given(commonMappingApi).linkInvestMapping(eq(userUuid), any());

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("계좌 상태 비정상인 경우 INVALID_ACCOUNT_STATUS 예외 발생")
    void linkAccount_invalidAccountStatus() {
        // given
        UUID userUuid = UUID.randomUUID();
        UUID investAccountUuid = UUID.randomUUID();

        LinkAccountRequest request = new LinkAccountRequest(investAccountUuid);

        MappingStatusResponse mappingStatus = new MappingStatusResponse(new MappingStatusResponse.InvestStatus(false));
        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, mappingStatus);

        InvestChnAccountSummary accountSummary = mock(InvestChnAccountSummary.class);
        given(accountSummary.getUserUuid()).willReturn(userUuid);
        given(accountSummary.getAccountStatus()).willReturn(AccountStatus.INACTIVE);

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);
        given(accountSummaryRepository.findById(investAccountUuid)).willReturn(Optional.of(accountSummary));

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.INVALID_ACCOUNT_STATUS));
    }

    @Test
    @DisplayName("getMappingStatus 응답이 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus 응답의 data가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseDataNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, (MappingStatusResponse) null);
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }

    @Test
    @DisplayName("getMappingStatus 응답의 data.invest()가 null인 경우 BAD_GATEWAY 예외 발생")
    void linkAccount_mappingResponseInvestNull_badGateway() {
        // given
        UUID userUuid = UUID.randomUUID();
        LinkAccountRequest request = new LinkAccountRequest(UUID.randomUUID());

        ApiResponse<MappingStatusResponse> mappingResponse = ApiResponse.of(SuccessStatus.OK, new MappingStatusResponse(null));
        given(commonMappingApi.getMappingStatus(userUuid)).willReturn(mappingResponse);

        // when & then
        assertThatThrownBy(() -> investAccountService.linkAccount(userUuid, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(CommonErrorCode.BAD_GATEWAY));
    }
}
