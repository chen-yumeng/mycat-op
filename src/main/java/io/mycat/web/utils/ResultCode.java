package io.mycat.web.utils;

/**
 * @program: mycat->ResultCode
 * @description:
 * @author: cg
 * @create: 2020-06-13 17:09
 **/
public class ResultCode {

    private int code;
    private Object data;

    public ResultCode() {
    }

    public ResultCode(int code, Object data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
