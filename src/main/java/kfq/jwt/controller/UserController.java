package kfq.jwt.controller;

import kfq.jwt.common.JwtTokenProvider;
import kfq.jwt.entity.User;
import kfq.jwt.repository.UserRepository;
import kfq.jwt.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam("id") String id,
                                                     @RequestParam("password") String password,
                                                     HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();
        User user = (User) customUserDetailsService.loadUserByUsername(id);
        System.out.println("controller: "+ user.getUsername());
        String accessToken = jwtTokenProvider.createToken(user.getUsername());
        String refreshToken = jwtTokenProvider.refreshToken(user.getUsername());
        res.put("userId", user.getId());
        res.put("userEmail", user.getEmail());
        res.put("userName", user.getName());
        res.put("userNickname", user.getNickname());
        res.put("accessToken", accessToken);
        res.put("refreshToken", refreshToken);
        return new ResponseEntity<Map<String, String>>(res, HttpStatus.OK);
    }

    @PostMapping("/userInfo")
    public ResponseEntity<User> userInfo(@RequestParam("id") String id) {
        User user = (User) customUserDetailsService.loadUserByUsername(id);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
}
