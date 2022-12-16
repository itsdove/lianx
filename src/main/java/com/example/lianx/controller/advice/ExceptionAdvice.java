package com.example.lianx.controller.advice;

import com.example.lianx.util.CommunityUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;


@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        logger.error("服务器异常"+e.getMessage());

        for(StackTraceElement stackTrace:e.getStackTrace()){
            logger.error(stackTrace.toString());
        }
        String header = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(header)){
            httpServletResponse.setContentType("application/plain");
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        }else{
            httpServletResponse.sendRedirect(request.getContextPath()+"/error");
        }
    }


}
