package com.weishao.dbswitch.controller;

import java.util.HashMap;
import java.util.Map;

public class BaseController {

	/**
	 * 成功响应
	 * 
	 * @param data
	 * @return
	 */
	protected Map<String, Object> success(Object data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("errcode", 0);
		map.put("errmsg", "success");
		if (null != data) {
			map.put("data", data);
		}
		return map;
	}

	/**
	 * 失败响应
	 * 
	 * @param errno  失败的错误码
	 * @param reason 失败的原因
	 * @return
	 */
	protected Map<String, Object> failed(long errno, String reason) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("errcode", errno);
		map.put("errmsg", reason);
		return map;
	}
}
