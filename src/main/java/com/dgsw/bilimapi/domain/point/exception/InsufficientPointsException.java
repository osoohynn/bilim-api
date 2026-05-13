package com.dgsw.bilimapi.domain.point.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InsufficientPointsException extends BusinessException {

    public InsufficientPointsException() {
        super(HttpStatus.PAYMENT_REQUIRED, "포인트가 부족합니다.");
    }
}
