package com.example.webproject.config;

import com.example.webproject.dtos.mvcDtos.SingletonCommentDto;
import com.example.webproject.models.mvcModels.guestUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;
@Configuration
@EnableTransactionManagement
public class ApplicationConfig {
    private final String dbUrl, dbUsername, dbPassword;

    @Autowired
    public ApplicationConfig(Environment env) {
        dbUrl = env.getProperty("database.url");
        dbUsername = env.getProperty("database.username");
        dbPassword = env.getProperty("database.password");
    }

    @Bean(name="entityManagerFactory")
    public LocalSessionFactoryBean localSessionFactoryBean(){
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.example.webproject.models");
        sessionFactory.setHibernateProperties(properties());
        return sessionFactory;
    }
    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }
    @Bean
    public Properties properties(){
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
        return hibernateProperties;
    }
    @Bean
    @Scope("singleton")
    public guestUser singletonUser() {
        return new guestUser();
    }
    @Bean
    @Scope("singleton")
    public SingletonCommentDto singletonCommentDto() {
        return new SingletonCommentDto();
    }
}