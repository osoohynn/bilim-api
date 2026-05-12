package com.dgsw.bilimapi.domain.friend.exception;

import com.dgsw.bilimapi.commons.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SelfFriendRequestException extends BusinessException {

    public SelfFriendRequestException() {
        super(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
    }
}
