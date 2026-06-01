package com.v1rex.liftnexus.storagebin.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageBinErrorCode implements ErrorCode {
  STORAGE_BIN_NOT_FOUND("storage_bin_not_found", "Storage Bin Not Found", HttpStatus.NOT_FOUND),

  STORAGE_BIN_CODE_EXISTS(
      "storage_bin_code_already_exists", "Storage Bin Code Already Exists", HttpStatus.CONFLICT);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
