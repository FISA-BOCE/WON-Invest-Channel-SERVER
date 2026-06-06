package com.woorifisa.won_invest_channel_server.global.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID authUserUuid,
        UUID userUuid,
        String jti
) {
}
