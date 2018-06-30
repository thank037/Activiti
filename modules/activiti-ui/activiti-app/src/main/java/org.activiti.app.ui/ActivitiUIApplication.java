package org.activiti.app.ui;

import org.activiti.app.conf.ApplicationConfiguration;
import org.activiti.app.servlet.ApiDispatcherServletConfiguration;
import org.activiti.app.servlet.AppDispatcherServletConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
@Import({ApplicationConfiguration.class})
public class ActivitiUIApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ActivitiUIApplication.class, args);
    }


    /**
     * 指定程序入口
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ActivitiUIApplication.class);
    }


    //

    /**
     * 注册apiDispatcherServlet
     * 注册两个子容器, 可参考{@link org.activiti.app.servlet.WebConfigurer}
     */
    @Bean
    public ServletRegistrationBean apiDispatcher() {
        DispatcherServlet api = new DispatcherServlet();
        // 设置该Servlet启动用的Spring容器实现类
        api.setContextClass(AnnotationConfigWebApplicationContext.class);
        // 设置实现类对应的配置文件
        api.setContextConfigLocation(ApiDispatcherServletConfiguration.class.getName());

        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(api);
        registrationBean.addUrlMappings("/api/*"); //设置url
        registrationBean.setLoadOnStartup(1); //设置优先级
        registrationBean.setAsyncSupported(true);//支持异步
        registrationBean.setName("api");
        return registrationBean;
    }


    /**
     * 注册appDispatcherServlet
     * 注册两个子容器, 可参考{@link org.activiti.app.servlet.WebConfigurer}
     */
    @Bean
    public ServletRegistrationBean appDispatcher() {
        DispatcherServlet app = new DispatcherServlet();
        // 设置该Servlet启动用的Spring容器实现类
        app.setContextClass(AnnotationConfigWebApplicationContext.class);
        // 设置实现类对应的配置文件
        app.setContextConfigLocation(AppDispatcherServletConfiguration.class.getName());

        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        registrationBean.setServlet(app);
        registrationBean.addUrlMappings("/app/*"); //设置url
        registrationBean.setLoadOnStartup(1); //设置优先级
        registrationBean.setAsyncSupported(true);//支持异步
        registrationBean.setName("app");
        return registrationBean;
    }
}
