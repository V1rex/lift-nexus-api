package com.v1rex.liftnexus.common.exception;


import java.time.LocalDateTime;

public record ApiError(
        String message,
        int status,
        LocalDateTime timestamp,
        String path
) {}
