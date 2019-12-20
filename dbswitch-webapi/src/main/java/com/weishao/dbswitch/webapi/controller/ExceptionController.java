package com.weishao.dbswitch.webapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONException;
import com.weishao.dbswitch.webapi.model.ResponseResult;

/**
 * 全局异常处理
 * 
 * @author tang
 *
 */
@ControllerAdvice
public class ExceptionController {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

	/**
	 * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
	 * 
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
	}

	/**
	 * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
	 * 
	 * @param model
	 */
	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("author", "tang");
	}

	/**
	 * JSON解析异常捕捉处理
	 * 
	 * @param ex
	 * @return
	 */
	@ResponseBody
	@ExceptionHandler(value = JSONException.class)
	public ResponseResult errorHandler(JSONException ex) {
		return ResponseResult.failed(-1, "Invalid JSON format：" + ex.getMessage());
	}
	
	/**
	 * 全局异常捕捉处理
	 * 
	 * @param ex
	 * @return
	 */
	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public ResponseResult errorHandler(Exception ex) {
		logger.error("ERROR:",ex);
		return ResponseResult.failed(-1, "Error information: " + ex.getMessage());
	}

}
