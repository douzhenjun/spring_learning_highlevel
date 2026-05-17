package com.tuoheng.demo05;

//import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.tuoheng.demo04.Bean1;
import com.tuoheng.demo05.mapper.Mapper1;
import com.tuoheng.demo05.mapper.Mapper2;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/21
 **/

@Configuration
@ComponentScan("com.tuoheng.demo05.component")
public class Config {
    @Bean
    public Bean1 bean1(){
        return new Bean1();
    }
    
    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource){
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean;
    }
    
    @Bean(initMethod = "init")
    public DruidDataSource dataSource(){
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/my_security");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        return dataSource;
    }
    
    //下面的两个Bean对象用于@Mapper注解的类, Mapper1,Mapper2分别是com.tuoheng.mapper下的两个映射类接口名
//    @Bean
//    public MapperFactoryBean<Mapper1> mapper1(SqlSessionFactory sqlSessionFactory){
//        MapperFactoryBean<Mapper1> factory = new MapperFactoryBean<>(Mapper1.class);
//        factory.setSqlSessionFactory(sqlSessionFactory);
//        return factory;
//    }
//
//    @Bean
//    public MapperFactoryBean<Mapper2> mapper2(SqlSessionFactory sqlSessionFactory){
//        MapperFactoryBean<Mapper2> factory = new MapperFactoryBean<>(Mapper2.class);
//        factory.setSqlSessionFactory(sqlSessionFactory);
//        return factory;
//    }
}
