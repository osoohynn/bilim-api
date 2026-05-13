package com.dgsw.bilimapi.domain.rental.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotRentalParticipantException extends BusinessException {

    public NotRentalParticipantException() {
        super(HttpStatus.FORBIDDEN, "해당 대여에 대한 권한이 없습니다.");
    }
}
