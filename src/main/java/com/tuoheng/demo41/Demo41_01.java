package com.tuoheng.demo41;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * 这个demo用来演示自动装配的原理， 现在有两个第三方类AutoConfiguration1和AutoConfiguration2, 
 * 通过自定义静态内部类Config通过注解@Import({AutoConfiguration1.class, AutoConfiguration2.class})能够
 * 实现导入.
 * 但是这种硬编码方式不提倡，不利于扩展，希望导入的第三方组件通过外部配置文件的方式引入. 步骤如下:
     * 1.首先, 自定义的导入文件选择器类MyImportSelector实现ImportSelector接口，重写它的
     * String[] selectImports(AnnotationMetadata importingClassMetadata)方法，该方法会从所有依赖jar包中
     * 扫描资源路径下META-INF/spring.factories文件中的键值对, 比如下面这样的键值对:
     *  com.tuoheng.demo41.Demo41_1$MyImportSelector=\
     *  com.tuoheng.demo41.Demo41_1.AutoConfiguration1,\
     *  com.tuoheng.demo41.Demo41_1.AutoConfiguration2
     *  这表示一行,但分行展示,$+类名表示这是内部类，该键值对导入了上面的这两个自动配置类
*      2.其次, 在我的自定义的配置类上添加注解@Import(MyImportSelector.class)
*      3.在主文件中, 通过定义一个GenericApplicationContext对象context以及注册bean对象Config.class, 再
*      注册Bean后置处理器ConfigurationClassPostProcessor.class, 这样在初始化之时，就将包括Config在内的两个第三方
*      自动配置类也导入了spring容器中。
 *  当自定义的配置类和导入的第三方配置类中存在同名的Bean对象，这时候如果是spring程序，后注册的bean对象会覆盖先引入的bean对象.
 *  默认先导入Import注解中引入的类的bean对象,再导入标注该注解的配置类的bean对象.比如下面的Config类中定义了Bean1工厂方法,
 *  AutoConfiguratin1中也定义了Bean1工厂方法, 默认最终注册进入的是前者的Bean对象.
 *  然而SpringBoot默认情况下不会实行覆盖, 这里为了演示SpringBoot的启动过程手动取消覆盖设置
 *  context.getDefaultListableBeanFactory().setAllowBeanDefinitionOverriding(false);
 *  这时候就会无法正常执行spring程序
 *  Exception in thread "main" org.springframework.beans.factory.support.BeanDefinitionOverrideException: Invalid bean definition with name 'bean1' defined 
 *  in com.tuoheng.demo41.Demo41_1$Config: Cannot register bean definition [Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=config; factoryMethodName=bean1; initMethodName=null; destroyMethodName=(inferred); defined in com.tuoheng.demo41.Demo41_1$Config] for bean 'bean1': There is already [Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=com.tuoheng.demo41.Demo41_1$AutoConfiguration1; factoryMethodName=bean1; initMethodName=null; destroyMethodName=(inferred); defined in class path resource [com/tuoheng/demo41/Demo41_1$AutoConfiguration1.class]] bound.
 *  为了使这两个Bean对象配置代码不改变,同时能够正常启动spring程序,使得若本地存在同名Bean对象则注册本地,否则注册第三方的情况,我们
 *  首先需要改变这两个类的导入顺序,前面说默认导入顺序是导入的类>标记@Import的类, 这是因为实现的是ImportSelector接口,将它改成它的子接口
 *  DeferredImportSelector就会实现导入顺序:标记@Import的类>导入的类, 这样一来就会先注册Config类中的Bean1对象,然后在其他类的
 *  Bean1工厂类对象中添加注解@ConditionalOnMissingBean, 这注解表示只有前面没有注册这个名称的bean对象时才会注册这个bean对象
 *  相当于一个if-else条件,这就保证了如果本地有优先注册本地,如果本地没有则注册第三方,也不必改动原配置代码.
 */

public class Demo41_01 {

    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getDefaultListableBeanFactory().setAllowBeanDefinitionOverriding(false);
        context.registerBean("config", Config.class);
        context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();
        
        for(String name : context.getBeanDefinitionNames()){
            System.out.println(name);
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(context.getBean(Bean1.class));
    }
    
    @Configuration
    @Import(MyImportSelector.class)
    static class Config{
//        @Bean
//        public Bean1 bean1(){
//            return new Bean1("本项目");
//        }
    }


//    static class MyImportSelector implements ImportSelector{
//        @Override
//        public String[] selectImports(AnnotationMetadata importingClassMetadata){
//            List<String> names = SpringFactoriesLoader.loadFactoryNames(Demo41_1.MyImportSelector.class, null);
//            return names.toArray(new String[0]);
//        }
    //DeferredImportSelector会改变默认的Bean对象注册顺序
    static class MyImportSelector implements DeferredImportSelector{
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata){
            List<String> names = SpringFactoriesLoader.loadFactoryNames(MyImportSelector.class, null);
            return names.toArray(new String[0]);
        }
        
    }
    
    static class AutoConfiguration1{
        @Bean
        @ConditionalOnMissingBean
        public Bean1 bean1(){
            return new Bean1("第三方");
        }
    }
    
    @Configuration
    static class AutoConfiguration2{
        @Bean
        public Bean2 bean2(){
            return new Bean2();
        }
    }
    
    static class Bean1{
        private String name;
        
        public Bean1(){}
        
        public Bean1(String name){
            this.name = name;
        }
        
        public String toString(){
            return "Bean1{" +
                    "name=" + name + '\'' +
                    '}';
        }
    }
    
    static class Bean2{
        
    }
}
