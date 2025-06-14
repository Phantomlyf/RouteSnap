package com.skymmer.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;


   public static <T> Result<T> success(T data){
        return new Result<>(200,"成功",data);
    }

    public static <T> Result<T> success(){
        return  new Result<>(200,"成功",null);
    }

    public static <T> Result<T> error(Integer code, String msg){
        return new Result<>(code,msg,null);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
