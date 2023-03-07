package com.mimiter.payment.exception.handler;

import com.mimiter.payment.model.BaseResponse;
import com.mimiter.payment.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理，在service, controller层抛出的未处理的异常都会由这个类处理。
 */
@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandlerAdvice {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public BaseResponse<?> missingParamExceptionHandler(MissingServletRequestParameterException exception) {
        return BaseResponse.error(exception.getParameterName() + "不能为空");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> validExceptionHandler(BindException exception) {
        return BaseResponse.error(exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> validExceptionHandler(MethodArgumentNotValidException exception) {
        return BaseResponse.error(
                exception.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})
    public BaseResponse<?> resourceNotFoundExceptionHandler(Exception exception) {
        return BaseResponse.error(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            MultipartException.class})
    public BaseResponse<?> badRequestExceptionHandler(Exception exception) {
        return BaseResponse.error(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalException.class)
    public BaseResponse<?> internalExceptionHandler(InternalException exception) {
        log.error("InternalException: ", exception);
        return BaseResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务异常，请稍后重试");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public BaseResponse<?> forbiddenExceptionHandler(ForbiddenException exception) {
        return BaseResponse.error(HttpStatus.FORBIDDEN.value(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public BaseResponse<?> internalExceptionHandler(UnauthorizedException exception) {
        return BaseResponse.error(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> unhandledExceptionHandler(Exception exception) {
        log.error("Unhandled InternalException: ", exception);
        return BaseResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务异常，请稍后重试");
    }

}
