package com.ibm.websphere.samples.daytrader.web.mvc;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ibm.websphere.samples.daytrader.web.prims.PingBean;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/servlet")
public class PrimitiveCompatibilityController {

    private final String pingServletInitTime = new Date().toString();
    private final String pingServletWriterInitTime = new Date().toString();
    private final AtomicInteger pingServletHitCount = new AtomicInteger();
    private final AtomicInteger pingServletWriterHitCount = new AtomicInteger();

    @ResponseBody
    @RequestMapping(value = "/PingServlet", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML_VALUE)
    public String pingServlet() {
        return "<html><head><title>Ping Servlet</title></head>"
                + "<body><HR><BR><FONT size=\"+2\" color=\"#000066\">Ping Servlet<BR></FONT>"
                + "<FONT size=\"+1\" color=\"#000066\">Init time : " + pingServletInitTime
                + "<BR><BR></FONT>  <B>Hit Count: " + pingServletHitCount.incrementAndGet() + "</B></body></html>";
    }

    @ResponseBody
    @RequestMapping(value = "/PingServletWriter", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML_VALUE)
    public String pingServletWriter() {
        return "<html><head><title>Ping Servlet Writer</title></head>"
                + "<body><HR><BR><FONT size=\"+2\" color=\"#000066\">Ping Servlet Writer:<BR></FONT>"
                + "<FONT size=\"+1\" color=\"#000066\">Init time : " + pingServletWriterInitTime
                + "<BR><BR></FONT>  <B>Hit Count: " + pingServletWriterHitCount.incrementAndGet() + "</B></body></html>";
    }

    @RequestMapping(value = "/PingServlet2Jsp", method = { RequestMethod.GET, RequestMethod.POST })
    public String pingServlet2Jsp(HttpServletRequest request) {
        PingBean pingBean = new PingBean();
        pingBean.setMsg("Hit Count: 1");
        request.setAttribute("ab", pingBean);
        return "forward:/PingServlet2Jsp.jsp";
    }
}