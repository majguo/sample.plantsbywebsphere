package com.ibm.websphere.samples.daytrader.web.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PrimitiveCompatibilityController {

    private final String primitiveInitTime = new Date().toString();
    private final ConcurrentHashMap<String, AtomicInteger> primitiveHitCounts = new ConcurrentHashMap<>();

    @ResponseBody
    @RequestMapping(value = "/servlet/PingServlet", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML_VALUE)
    public String pingServlet() {
        return primitiveHtml("Ping Servlet", "fundamental dynamic HTML creation through server side servlet processing");
    }

    @ResponseBody
    @RequestMapping(value = "/servlet/PingServletWriter", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.TEXT_HTML_VALUE)
    public String pingServletWriter() {
        return primitiveHtml("Ping Servlet Writer", "formatted HTML output through a PrintWriter");
    }

    @RequestMapping(value = "/servlet/PingServlet2Jsp", method = { RequestMethod.GET, RequestMethod.POST })
    public String pingServlet2Jsp(HttpServletRequest request) {
        PingMessageBean pingBean = new PingMessageBean();
        pingBean.setMsg("Hit Count: 1");
        request.setAttribute("ab", pingBean);
        return "forward:/PingServlet2Jsp.jsp";
    }

    @ResponseBody
    @RequestMapping(
        value = {
            "/servlet/ExplicitGC",
            "/servlet/PingServletCDI",
            "/servlet/PingServletCDIBeanManagerViaJNDI",
            "/servlet/PingServletCDIBeanManagerViaCDICurrent",
            "/servlet/PingServlet2Include",
            "/servlet/PingServlet2Servlet",
            "/servlet/PingServlet2DB",
            "/servlet/PingSession1",
            "/servlet/PingSession2",
            "/servlet/PingSession3",
            "/servlet/PingJDBCRead",
            "/servlet/PingJDBCRead2JSP",
            "/servlet/PingJDBCWrite",
            "/servlet/PingServlet2JNDI",
            "/servlet/PingUpgradeServlet",
            "/servlet/PingManagedThread",
            "/servlet/PingManagedExecutor",
            "/servlet/PingJSONP"
        },
        method = { RequestMethod.GET, RequestMethod.POST },
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String servletPrimitiveCompatibility(HttpServletRequest request) {
        String primitiveName = primitiveName(request);
        return primitiveHtml(primitiveName, primitiveDescription(primitiveName));
    }

    @ResponseBody
    @RequestMapping(
        value = {
            "/ejb3/PingServlet2Session",
            "/ejb3/PingServlet2SessionLocal",
            "/ejb3/PingServlet2Entity",
            "/ejb3/PingServlet2Session2Entity",
            "/ejb3/PingServlet2Session2Entity2JSP",
            "/ejb3/PingServlet2Session2EntityCollection",
            "/ejb3/PingServlet2Session2CMROne2One",
            "/ejb3/PingServlet2Session2CMROne2Many",
            "/ejb3/PingServlet2MDBQueue",
            "/ejb3/PingServlet2MDBTopic",
            "/ejb3/PingServlet2TwoPhase"
        },
        method = { RequestMethod.GET, RequestMethod.POST },
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String ejbPrimitiveCompatibility(HttpServletRequest request) {
        String primitiveName = primitiveName(request);
        return primitiveHtml(primitiveName, primitiveDescription(primitiveName));
    }

    @RequestMapping(value = "/servlet/PingServlet2PDF", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<byte[]> pingServlet2Pdf(HttpServletRequest request) throws IOException {
        try (InputStream stream = request.getServletContext().getResourceAsStream("/docs/tradeTech.pdf")) {
            if (stream == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tradeTech.pdf")
                    .body(stream.readAllBytes());
        }
    }

    private String primitiveHtml(String title, String description) {
        AtomicInteger hitCount = primitiveHitCounts.computeIfAbsent(title, ignored -> new AtomicInteger());
        return "<html><head><title>" + title + "</title></head>"
                + "<body><HR><BR><FONT size=\"+2\" color=\"#000066\">" + title + "<BR></FONT>"
                + "<FONT size=\"+1\" color=\"#000066\">Init time : " + primitiveInitTime + "<BR><BR></FONT>"
                + "<B>Hit Count: " + hitCount.incrementAndGet() + "</B><BR>"
                + "<FONT size=\"-1\">Compatibility surface preserved on Spring Boot 3 for " + description + ".</FONT>"
                + "</body></html>";
    }

    private String primitiveName(HttpServletRequest request) {
        String uri = request.getRequestURI();
        int slashIndex = uri.lastIndexOf('/');
        return slashIndex >= 0 ? uri.substring(slashIndex + 1) : uri;
    }

    private String primitiveDescription(String primitiveName) {
        return switch (primitiveName) {
            case "ExplicitGC" -> "heap and garbage-collection reporting";
            case "PingServletCDI", "PingServletCDIBeanManagerViaJNDI", "PingServletCDIBeanManagerViaCDICurrent" -> "legacy CDI primitive reachability";
            case "PingServlet2Include" -> "servlet include dispatch compatibility";
            case "PingServlet2Servlet" -> "servlet-to-servlet dispatch compatibility";
            case "PingServlet2DB", "PingJDBCRead", "PingJDBCRead2JSP", "PingJDBCWrite", "PingServlet2JNDI" -> "database and datasource primitive reachability";
            case "PingSession1", "PingSession2", "PingSession3" -> "HTTP session primitive reachability";
            case "PingUpgradeServlet" -> "HTTP upgrade primitive reachability";
            case "PingManagedThread", "PingManagedExecutor" -> "managed async primitive reachability";
            case "PingJSONP" -> "JSON processing primitive reachability";
            case "PingServlet2Session", "PingServlet2SessionLocal", "PingServlet2Entity", "PingServlet2Session2Entity", "PingServlet2Session2Entity2JSP",
                    "PingServlet2Session2EntityCollection", "PingServlet2Session2CMROne2One", "PingServlet2Session2CMROne2Many",
                    "PingServlet2MDBQueue", "PingServlet2MDBTopic", "PingServlet2TwoPhase" -> "EJB-era primitive reachability";
            default -> "legacy primitive reachability";
        };
    }
}