package com.dgsw.bilimapi.domain.reading.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvitationNotFoundException extends BusinessException {

    public InvitationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다.");
    }
}
