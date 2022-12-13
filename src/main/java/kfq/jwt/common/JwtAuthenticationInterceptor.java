package kfq.jwt.common;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = jwtTokenProvider.resolveToken(request); // request header에서 토큰 가져옴
        System.out.println("token: " + token);
        if (token == null) {
            return true; // 1. 토큰이 비어있을 때: 토큰 생성 전이기 때문에 로그인 처리로 넘김
        }
        String[] tokens = token.split(",");
        if (jwtTokenProvider.validateToken(tokens[0])) { // 2. accessToken이 유효한 경우 정상처리
            System.out.println("2. accessToken 유효");
            // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옴
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // Security Context에 Authentication 객체를 저장함
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } else if (tokens.length == 1) { // 3. accessToken만 가져왔는데 만료됐을 경우, refreshToken 요청
            System.out.println("3. refreshToken 요청");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"rescode\":100}");
            response.getWriter().flush();
        } else if (jwtTokenProvider.validateToken(tokens[1])) { // 4. refreshToken 유효함, 새로운 두개의 토큰 재발급
            System.out.println("4. refreshToken 유효, 두개의 토큰 재발급");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String userId = jwtTokenProvider.getUserId(tokens[1]);
            String accessToken = jwtTokenProvider.createToken(userId);
            String refreshToken = jwtTokenProvider.refreshToken(userId);
            response.getWriter().write("{\"rescode\":101,\"accessToken\":\"" + accessToken + "\"," +
                    "\"refreshToken\":\"" + refreshToken + "\"}");
            response.getWriter().flush();
        } else { // 5. refresh 토큰 만료됨, 재로그인 요청
            System.out.println("5. 토큰 만료, 재로그인");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"rescode\":102}");
            response.getWriter().flush();
        }
        return false;
    }
}
