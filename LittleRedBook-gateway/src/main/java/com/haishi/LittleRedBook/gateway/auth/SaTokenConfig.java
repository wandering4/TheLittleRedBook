package com.haishi.LittleRedBook.gateway.auth;

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
                    SaRouter.match("/auth/user/logout",r->StpUtil.checkRole("admin"));

/*                    SaRouter.match("/user/**",r->StpUtil.checkPermission("user"));
                    SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));*/

                    // 更多匹配 ...  */


                        }
                )
                // 异常处理方法：每次setAuth函数出现异常时进入
                /*.setError(e -> {
                    return SaResult.error(e.getMessage());
                })*/;
    }
}
