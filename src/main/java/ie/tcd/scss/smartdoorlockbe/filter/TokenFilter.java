//package ie.tcd.scss.smartdoorlockbe.filter;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.PathMatcher;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//public class TokenFilter implements Filter {
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired
//    private ObjectMapper objectMapper;  //Jackson
//    @Value("${filter.config.excludeUrls}")
//    private List<String> excludeUrls;
//    @Value("${filter.config.includeUrls}")
//    private List<String> includeUrls;
//
//    //    @Override
/// /    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
/// /        HttpServletRequest servletRequest = (HttpServletRequest) request;
/// /        String requestUrl = servletRequest.getRequestURI();
/// /        PathMatcher matcher = new AntPathMatcher();
/// /        //不是白名单
/// /        if (excludeUrls.stream().noneMatch(excludeUrl -> matcher.match(excludeUrl, requestUrl))) {
/// /            String token = servletRequest.getHeader("Authorization");
/// /            //token过期
/// /            if (!jwtUtil.validateToken(token)) {
/// /                //返回错误信息
/// /                DataResult result = DataResult.fail(ResponseCode.TOKEN_ERROR.getCode(), ResponseCode.TOKEN_ERROR.getMessage());
/// /                response.getOutputStream().write(objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8)); //将DataResult对象转换为JSON字符串，并写入到响应的输出流中
/// /                response.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置响应的内容类型为JSON
/// /                return;
/// /            }
/// /        }
/// /        //在白名单中，或者令牌有效，调用FilterChain的doFilter方法，将请求和响应传递给下一个过滤器或Servlet
/// /        filterChain.doFilter(request, response);
/// /    }
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest servletRequest = (HttpServletRequest) request;
//        String requestUrl = servletRequest.getRequestURI();
//        PathMatcher matcher = new AntPathMatcher();
//        //是黑名单includeUrl中的Url
//        if (includeUrls.stream().anyMatch(includeUrl -> matcher.match(includeUrl, requestUrl))) {
//            String token = servletRequest.getHeader("Authorization");
//            //token过期
//            if (!jwtUtil.validateToken(token)) {
//                //返回错误信息
//                DataResult result = DataResult.fail(ResponseCode.TOKEN_ERROR.getCode(), ResponseCode.TOKEN_ERROR.getMessage());
//                response.getOutputStream().write(objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8)); //将DataResult对象转换为JSON字符串，并写入到响应的输出流中
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置响应的内容类型为JSON
//                return;
//            }
//        }
//        //在白名单中，或者令牌有效，调用FilterChain的doFilter方法，将请求和响应传递给下一个过滤器或Servlet
//        filterChain.doFilter(request, response);
//    }
//}
