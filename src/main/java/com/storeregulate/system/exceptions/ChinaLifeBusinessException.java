package com.storeregulate.system.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 此类仅仅是为了返回不是5xx的错误，应对nginx 1%错误率返回的告警需求 把 返回错误频率高的异常调用此类
 * 
 * 返回412 异常
 * @author lsf
 *
 */
@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED)
public class ChinaLifeBusinessException extends BusinessException {

	private static final long serialVersionUID = -2892977295043172164L;

	public ChinaLifeBusinessException(String message) {
		super(message);
	}
}
