package com.woorifisa.won_invest_channel_server.domain.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateInvestAccountRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,

        @NotBlank
        String customerName,

        @NotBlank
        @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
        String accountPassword,

        @NotBlank
        @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
        String accountPasswordConfirm,

        @NotBlank
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotEmpty
        List<@NotBlank String> agreedTerms
) {}