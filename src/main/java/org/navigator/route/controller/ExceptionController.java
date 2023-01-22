package org.navigator.route.controller;

import org.navigator.route.exception.BaseException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

  @ExceptionHandler({BaseException.class})
  public ResponseEntity<String> handleInvalidParametersException(BaseException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatusCode.valueOf(400));
  }

}
