package com.woorifisa.won_invest_channel_server.domain.account.model;

import com.woorifisa.won_invest_channel_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Table(name = "invest_chn_account_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnAccountSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invest_account_uuid", nullable = false, unique = true)
    private UUID investAccountUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "invest_user_uuid", nullable = false, unique = true)
    private UUID investUserUuid;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @Column(name = "account_no_display", nullable = false)
    private String accountNoDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;
}
