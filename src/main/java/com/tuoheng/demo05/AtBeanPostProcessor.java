package com.tuoheng.demo05;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.Set;

/**
 * @Description
 * @Author douzhenjun
 * @DATE 2023/4/21
 **/
public class AtBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        try {
            System.out.println("执行AtBeanPostProcessor");
            //1.读取路径下的Config.class
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
            MetadataReader reader = factory.getMetadataReader(new ClassPathResource("com/tuoheng/demo05/Config.class"));
            //2.获得所有被注解了@Bean的方法对象,用集合存放
            Set<MethodMetadata> methods = reader.getAnnotationMetadata().getAnnotatedMethods(Bean.class.getName());
            for (MethodMetadata method : methods) {
//                System.out.println(">>>>>>");
//                System.out.println(method);
                //3.获得@Bean对象中参数initMethod所指的值,initMethod表示初始化方法,在一个类构造方法执行前执行
                String initMethod = method.getAnnotationAttributes(Bean.class.getName()).get("initMethod").toString();
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
                //4.定义bean工厂对象config,这里配置类就是工厂对象
                builder.setFactoryMethodOnBean(method.getMethodName(), "config");
                //5.如果注释掉这一行则显示SqlSessionFactoryBean对象创建失败,从代码上看是因为它传入的参数DataSource,spring
                //无法识别, 所以下面设置在构造器中对参数做@Autowired以注入
                builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
                if(initMethod.length()>0){
                    builder.setInitMethodName(initMethod);
                }
                //为每个method创建一个BeanDefinition对象,以methodName为bean_id
                AbstractBeanDefinition bd = builder.getBeanDefinition();
                beanDefinitionRegistry.registerBeanDefinition(method.getMethodName(), bd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
