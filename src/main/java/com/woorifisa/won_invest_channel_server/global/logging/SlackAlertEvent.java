package com.woorifisa.won_invest_channel_server.global.logging;

public record SlackAlertEvent(int status, String method, String uri, long elapsedMs) {
}