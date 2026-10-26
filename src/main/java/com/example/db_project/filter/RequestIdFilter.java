package com.example.db_project.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 요청 하나에 짧은 id 를 붙여 MDC 에 넣습니다.
 * 로그와 에러 응답에 같은 id 가 찍히므로, 민원이 들어왔을 때 그 요청만 뽑아볼 수 있습니다.
 *
 * @Component 로 등록된 일반 빈이라 @WebMvcTest 에서는 뜨지 않습니다.
 */
@Component
public class RequestIdFilter implements Filter {

    public static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        MDC.put(REQUEST_ID, UUID.randomUUID().toString().substring(0, 8));
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();   // 스레드가 재사용되므로 반드시 비웁니다
        }
    }
}
