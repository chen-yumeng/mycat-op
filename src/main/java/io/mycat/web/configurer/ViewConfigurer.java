package io.mycat.web.configurer;

import io.mycat.web.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: Mycat->ViewConfigurer
 * @description:
 * @author: cg
 * @create: 2020-06-20 11:55
 **/
@Configuration
public class ViewConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册TestInterceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(new LoginInterceptor());
        //所有路径都被拦截
        registration.addPathPatterns("/**");
        //添加不拦截路径
        registration.excludePathPatterns("/login", "/mycat/login", "/**/*.html", "/**/*.js", "/**/*.css", "/**/*.woff", "/**/*.ttf", "/**/*.jpg", "/**/*.png");
    }

}
