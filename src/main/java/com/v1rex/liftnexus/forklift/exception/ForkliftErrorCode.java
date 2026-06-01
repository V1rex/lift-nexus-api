package com.v1rex.liftnexus.forklift.exception;

import com.v1rex.liftnexus.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ForkliftErrorCode implements ErrorCode {
  FORKLIFT_NOT_FOUND("forklift_not_found", "Forklift Not Found", HttpStatus.NOT_FOUND),

  FORKLIFT_TYPE_NOT_FOUND(
      "forklift_type_not_found", "Forklift Type Not Found", HttpStatus.NOT_FOUND),

  FORKLIFT_FLEET_NUMBER_EXISTS(
      "forklift_fleet_number_already_exists",
      "Forklift Fleet Number already exists",
      HttpStatus.CONFLICT),

  FORKLIFT_TYPE_NAME_EXISTS(
      "forklift_type_name_already_exists",
      "Forklift Type Name  already exists",
      HttpStatus.CONFLICT);

  private final String code;
  private final String defaultTitle;
  private final HttpStatus status;
}
