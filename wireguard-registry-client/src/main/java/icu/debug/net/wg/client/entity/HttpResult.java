package icu.debug.net.wg.client.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用HTTP响应对象
 *
 * @author hanjinxiang@debug.icu
 * @date 2024-02-07 21:10
 */
@Getter
@Setter
@ToString
public class HttpResult<T> {

    private Integer status;

    private String msg;

    private T data;

    public static <T> HttpResult<T> success(T data) {
        HttpResult<T> result = new HttpResult<>();
        result.setStatus(0);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> HttpResult<T> success() {
        HttpResult<T> result = new HttpResult<>();
        result.setStatus(0);
        result.setMsg("success");
        return result;
    }

    public static <T> HttpResult<T> error(Integer status, String msg) {
        HttpResult<T> result = new HttpResult<>();
        result.setStatus(status);
        result.setMsg(msg);
        return result;
    }

    public static <T> HttpResult<T> error(String msg) {
        HttpResult<T> result = new HttpResult<>();
        result.setStatus(500);
        result.setMsg(msg);
        return result;
    }

} 