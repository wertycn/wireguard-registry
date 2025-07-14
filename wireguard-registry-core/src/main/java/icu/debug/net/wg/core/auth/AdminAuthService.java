package icu.debug.net.wg.core.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理控制台认证服务
 * 基于JWT的用户认证机制
 */
@Slf4j
public class AdminAuthService {

    private final Key jwtSecretKey;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, AdminUser> users = new ConcurrentHashMap<>();
    private final Set<String> revokedTokens = new ConcurrentHashMap<String, Boolean>().keySet();

    // JWT 有效期（秒）
    private static final long JWT_EXPIRATION = 3600; // 1小时

    public AdminAuthService(String jwtSecret) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.passwordEncoder = new BCryptPasswordEncoder();
        
        // 初始化默认管理员用户
        initializeDefaultAdmin();
    }

    /**
     * 初始化默认管理员用户
     */
    private void initializeDefaultAdmin() {
        AdminUser admin = new AdminUser(
            "admin",
            passwordEncoder.encode("admin123"),
            "admin@wireguard.local",
            Set.of(AdminRole.SUPER_ADMIN),
            true,
            Instant.now(),
            Instant.now()
        );
        users.put("admin", admin);
        log.info("Default admin user created: admin/admin123");
    }

    /**
     * 用户登录
     */
    public LoginResult login(String username, String password) {
        AdminUser user = users.get(username);
        if (user == null) {
            log.warn("Login failed: user {} not found", username);
            return new LoginResult(false, "用户不存在", null, null);
        }

        if (!user.isActive()) {
            log.warn("Login failed: user {} is disabled", username);
            return new LoginResult(false, "用户已被禁用", null, null);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Login failed: invalid password for user {}", username);
            return new LoginResult(false, "密码错误", null, null);
        }

        // 生成JWT Token
        String token = generateJwtToken(user);
        
        // 更新最后登录时间
        user.setLastLoginAt(Instant.now());
        
        log.info("User {} logged in successfully", username);
        return new LoginResult(true, "登录成功", token, user);
    }

    /**
     * 验证JWT Token
     */
    public TokenValidationResult validateToken(String token) {
        try {
            // 检查是否在撤销列表中
            if (revokedTokens.contains(token)) {
                return new TokenValidationResult(false, "Token已被撤销", null, null);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            AdminUser user = users.get(username);
            
            if (user == null || !user.isActive()) {
                return new TokenValidationResult(false, "用户不存在或已被禁用", null, null);
            }

            return new TokenValidationResult(true, "Token有效", user, claims);

        } catch (Exception e) {
            log.warn("Token validation failed", e);
            return new TokenValidationResult(false, "Token无效", null, null);
        }
    }

    /**
     * 撤销Token
     */
    public void revokeToken(String token) {
        revokedTokens.add(token);
        log.info("Token revoked");
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        revokeToken(token);
        log.info("User logged out");
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(String username, AdminRole requiredRole) {
        AdminUser user = users.get(username);
        if (user == null || !user.isActive()) {
            return false;
        }
        
        return user.getRoles().contains(requiredRole) || 
               user.getRoles().contains(AdminRole.SUPER_ADMIN);
    }

    /**
     * 创建用户
     */
    public boolean createUser(String username, String password, String email, Set<AdminRole> roles) {
        if (users.containsKey(username)) {
            log.warn("User {} already exists", username);
            return false;
        }

        AdminUser user = new AdminUser(
            username,
            passwordEncoder.encode(password),
            email,
            roles,
            true,
            Instant.now(),
            Instant.now()
        );
        
        users.put(username, user);
        log.info("User {} created successfully", username);
        return true;
    }

    /**
     * 更新用户密码
     */
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        AdminUser user = users.get(username);
        if (user == null) {
            return false;
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            log.warn("Password update failed: invalid old password for user {}", username);
            return false;
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        
        log.info("Password updated for user {}", username);
        return true;
    }

    /**
     * 禁用/启用用户
     */
    public boolean setUserActive(String username, boolean active) {
        AdminUser user = users.get(username);
        if (user == null) {
            return false;
        }

        user.setActive(active);
        user.setUpdatedAt(Instant.now());
        
        log.info("User {} {}", username, active ? "activated" : "deactivated");
        return true;
    }

    /**
     * 获取所有用户
     */
    public List<AdminUser> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * 获取用户信息
     */
    public AdminUser getUser(String username) {
        return users.get(username);
    }

    /**
     * 生成JWT Token
     */
    private String generateJwtToken(AdminUser user) {
        Date expiration = Date.from(Instant.now().plusSeconds(JWT_EXPIRATION));
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .claim("roles", user.getRoles())
                .claim("email", user.getEmail())
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 登录结果
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final String token;
        private final AdminUser user;

        public LoginResult(boolean success, String message, String token, AdminUser user) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
        public AdminUser getUser() { return user; }
    }

    /**
     * Token验证结果
     */
    public static class TokenValidationResult {
        private final boolean valid;
        private final String message;
        private final AdminUser user;
        private final Claims claims;

        public TokenValidationResult(boolean valid, String message, AdminUser user, Claims claims) {
            this.valid = valid;
            this.message = message;
            this.user = user;
            this.claims = claims;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public AdminUser getUser() { return user; }
        public Claims getClaims() { return claims; }
    }
}