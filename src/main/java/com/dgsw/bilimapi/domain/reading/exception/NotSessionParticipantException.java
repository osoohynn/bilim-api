package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotSessionParticipantException extends BusinessException {

    public NotSessionParticipantException() {
        super(HttpStatus.FORBIDDEN, "세션 참가자가 아닙니다.");
    }
}
