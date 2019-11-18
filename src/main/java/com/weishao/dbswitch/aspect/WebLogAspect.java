package com.weishao.dbswitch.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;

/**
 *  WEB请求日志截面类
 * @author tang
 *
 */
@Aspect
@Component
public class WebLogAspect {

	private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);
	
	@Pointcut("execution(* com.weishao.dbswitch.controller..*.*(..))")
	public void webLog() {
    }

	@Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

		String className = joinPoint.getSignature().getDeclaringTypeName();
		if (!className.equals("com.weishao.dbswitch.controller.ExceptionController")) {
			HashMap<String, String> msg = new HashMap<String, String>();
			msg.put("REQ_URL :", request.getRequestURL().toString());
			msg.put("METHOD  ", request.getMethod());
			msg.put("REMOTE_IP ", request.getRemoteAddr());
			msg.put("CLASS_METHOD ",	className + "." + joinPoint.getSignature().getName());
			msg.put("BODY_CONTENT : ", Arrays.toString(joinPoint.getArgs()));
			logger.info("[Request] {}", JSON.toJSONString(msg));
		}
    }

    @AfterReturning(returning = "ret",pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
    	if(null!=ret) {
    		logger.info("[Response] : " + ret);
    	}
    }
	
}
