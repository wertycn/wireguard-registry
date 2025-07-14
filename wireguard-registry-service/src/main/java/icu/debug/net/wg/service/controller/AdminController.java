package icu.debug.net.wg.service.controller;

import icu.debug.net.wg.core.auth.AdminAuthService;
import icu.debug.net.wg.core.auth.AdminRole;
import icu.debug.net.wg.core.auth.AdminUser;
import icu.debug.net.wg.core.auth.NodeAuthService;
import icu.debug.net.wg.core.auth.TemporaryKey;
import icu.debug.net.wg.service.entity.HttpResult;
import icu.debug.net.wg.service.entity.LoginRequest;
import icu.debug.net.wg.service.entity.CreateUserRequest;
import icu.debug.net.wg.service.entity.ChangePasswordRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 管理控制台控制器
 */
@RestController
@RequestMapping("/v1/admin")
@Slf4j
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final NodeAuthService nodeAuthService;

    public AdminController(AdminAuthService adminAuthService, NodeAuthService nodeAuthService) {
        this.adminAuthService = adminAuthService;
        this.nodeAuthService = nodeAuthService;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public HttpResult<AdminAuthService.LoginResult> login(@RequestBody LoginRequest request) {
        log.info("Admin login attempt: {}", request.getUsername());
        
        AdminAuthService.LoginResult result = adminAuthService.login(
            request.getUsername(), 
            request.getPassword()
        );
        
        if (result.isSuccess()) {
            return HttpResult.success(result);
        } else {
            return HttpResult.error(result.getMessage());
        }
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public HttpResult<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            adminAuthService.logout(token);
        }
        return HttpResult.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public HttpResult<AdminUser> getProfile(HttpServletRequest request) {
        AdminUser user = getCurrentUser(request);
        if (user == null) {
            return HttpResult.error("用户未登录");
        }
        return HttpResult.success(user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public HttpResult<Void> changePassword(@RequestBody ChangePasswordRequest request,
                                          HttpServletRequest httpRequest) {
        AdminUser user = getCurrentUser(httpRequest);
        if (user == null) {
            return HttpResult.error("用户未登录");
        }

        boolean success = adminAuthService.updatePassword(
            user.getUsername(),
            request.getOldPassword(),
            request.getNewPassword()
        );

        if (success) {
            return HttpResult.success();
        } else {
            return HttpResult.error("密码修改失败");
        }
    }

    /**
     * 创建用户（需要超级管理员权限）
     */
    @PostMapping("/users")
    public HttpResult<Void> createUser(@RequestBody CreateUserRequest request,
                                      HttpServletRequest httpRequest) {
        AdminUser currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return HttpResult.error("用户未登录");
        }

        if (!currentUser.hasRole(AdminRole.SUPER_ADMIN)) {
            return HttpResult.error("权限不足");
        }

        boolean success = adminAuthService.createUser(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getRoles()
        );

        if (success) {
            return HttpResult.success();
        } else {
            return HttpResult.error("用户创建失败");
        }
    }

    /**
     * 获取所有用户（需要超级管理员权限）
     */
    @GetMapping("/users")
    public HttpResult<List<AdminUser>> getAllUsers(HttpServletRequest request) {
        AdminUser currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return HttpResult.error("用户未登录");
        }

        if (!currentUser.hasRole(AdminRole.SUPER_ADMIN)) {
            return HttpResult.error("权限不足");
        }

        List<AdminUser> users = adminAuthService.getAllUsers();
        return HttpResult.success(users);
    }

    /**
     * 启用/禁用用户（需要超级管理员权限）
     */
    @PostMapping("/users/{username}/status")
    public HttpResult<Void> setUserStatus(@PathVariable String username,
                                         @RequestParam boolean active,
                                         HttpServletRequest request) {
        AdminUser currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return HttpResult.error("用户未登录");
        }

        if (!currentUser.hasRole(AdminRole.SUPER_ADMIN)) {
            return HttpResult.error("权限不足");
        }

        boolean success = adminAuthService.setUserActive(username, active);
        if (success) {
            return HttpResult.success();
        } else {
            return HttpResult.error("操作失败");
        }
    }

    /**
     * 生成临时密钥（需要网络管理员权限）
     */
    @PostMapping("/temp-keys")
    public HttpResult<TemporaryKey> generateTempKey(@RequestParam String networkId,
                                                   HttpServletRequest request) {
        AdminUser currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return HttpResult.error("用户未登录");
        }

        if (!currentUser.hasRole(AdminRole.NETWORK_ADMIN)) {
            return HttpResult.error("权限不足");
        }

        TemporaryKey tempKey = nodeAuthService.generateTemporaryKey(networkId);
        return HttpResult.success(tempKey);
    }

    /**
     * 获取当前用户
     */
    private AdminUser getCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return null;
        }

        AdminAuthService.TokenValidationResult result = adminAuthService.validateToken(token);
        if (result.isValid()) {
            return result.getUser();
        } else {
            return null;
        }
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}