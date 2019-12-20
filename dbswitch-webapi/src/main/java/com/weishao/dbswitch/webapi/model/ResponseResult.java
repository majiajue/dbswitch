package com.weishao.dbswitch.webapi.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 通用响应类
 * 
 * @author tang
 */
@ApiModel(value = "接口响应对象")
public class ResponseResult implements Serializable {

	private static final long serialVersionUID = 3136839578922385415L;

	public static final Integer SUCCESS_STATUS = 0;

	public static final String SUCCESS = "success";

	@ApiModelProperty(value = "错误码", name = "errcode", required = true)
	private Integer errcode;

	@ApiModelProperty(value = "错误信息", name = "errmsg", required = true)
	private String errmsg;

	@ApiModelProperty(value = "数据信息", name = "data", required = true)
	private Object data;

	/////////////////////////////////////////////////////////////////

	public static ResponseResult success(Object data) {
		return new ResponseResult(SUCCESS, SUCCESS_STATUS, data);
	}

	public static ResponseResult failed(Integer errorCode, String message) {
		return new ResponseResult(message, errorCode);
	}

	/////////////////////////////////////////////////////////////////

	public ResponseResult(String message, Integer code) {
		this.errmsg = message;
		this.errcode = code;
	}

	public ResponseResult(String message, Integer code, Object data) {
		this.errmsg = message;
		this.errcode = code;
		this.data = data;
	}

	public Integer getErrcode() {
		return errcode;
	}

	public void setErrcode(Integer errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
