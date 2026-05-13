package com.dgsw.bilimapi.domain.rental.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RentalNotFoundException extends BusinessException {

    public RentalNotFoundException() {
        super(HttpStatus.NOT_FOUND, "대여 정보를 찾을 수 없습니다.");
    }
}
