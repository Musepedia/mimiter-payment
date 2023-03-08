package com.mimiter.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 统一接口返回数据类型。
 *
 * @param <T> 任意数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    /**
     * 返回码，与http状态码一致。
     */
    private Integer code;

    /**
     * 返回信息，抛出异常时会附带异常信息，正常返回任意值（"ok", null）皆可。
     */
    private String message;

    /**
     * 返回数据，正常返回时返回的数据，异常时返回null。
     */
    private T data;

    /**
     * 返回成功，不带数据。
     *
     * @param <T> /
     * @return /
     */
    public static <T> BaseResponse<T> ok() {
        return ok("ok", null);
    }

    /**
     * 如果返回数据的泛型为{@link String}，请使用{@link #ok(String, Object)}。<br/>
     * <code>
     * BaseResponse&lt;String&gt; resp = BaseResponse.ok("ok", str);
     * </code>
     *
     * @param data /
     * @param <T>  /
     * @return /
     */
    public static <T> BaseResponse<T> ok(T data) {
        return ok("ok", data);
    }

    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>(HttpStatus.OK.value(), message, data);
    }

    /**
     * {@inheritDoc}
     *
     * @see #ok(Object)
     */
    public static <T> BaseResponse<T> ok(String message) {
        return BaseResponse.ok(message, null);
    }

    /**
     * 请求异常返回，{@link #code}为400。
     *
     * @param message 异常信息
     * @param <T>     /
     * @return /
     */
    public static <T> BaseResponse<T> error(String message) {
        return error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 请求异常返回。
     *
     * @param status  http状态码
     * @param message 异常信息
     * @param <T>     /
     * @return /
     */
    public static <T> BaseResponse<T> error(int status, String message) {
        return new BaseResponse<>(status, message, null);
    }
}
