package com.haishi.LittleRedBook.gateway.auth;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SaToken权限配置类
 */
@Configuration
public class SaTokenConfig {
    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                //拦截地址
                .addInclude("/**")
                .setAuth(o -> {
                    //登录校验，拦截所有路由,并排除/user/doLogin 用于开放登录
                    SaRouter.match("/**")
                            .notMatch("/auth/user/login")
                            .notMatch("/auth/verification/code/send")
                            .check(r->StpUtil.checkLogin());

                    //权限认证
//                    SaRouter.match("/auth/user/logout", r -> StpUtil.checkPermission("user"));
//                    SaRouter.match("/auth/user/logout", r -> StpUtil.checkPermission("app:note:publish"));
//                    SaRouter.match("/auth/user/logout",r->StpUtil.checkRole("admin"));
                            SaRouter.match("/auth/user/logout",r->StpUtil.checkRole("common_user"));

/*                    SaRouter.match("/user/**",r->StpUtil.checkPermission("user"));
                    SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));*/

                    // 更多匹配 ...  */


                        }
                )
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> {
                    // return SaResult.error(e.getMessage());
                    // 手动抛出异常，抛给全局异常处理器
                    if (e instanceof NotLoginException) { // 未登录异常
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) { // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else { // 其他异常，则抛出一个运行时异常
                        throw new RuntimeException(e.getMessage());
                    }
                })
                ;
    }
}