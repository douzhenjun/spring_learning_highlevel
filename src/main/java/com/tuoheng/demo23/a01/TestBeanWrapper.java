package com.tuoheng.demo23.a01;

import org.springframework.beans.BeanWrapperImpl;

import java.util.Date;

public class TestBeanWrapper {

    public static void main(String[] args) {
        //利用反射原理, 为bean的属性赋值
        MyBean target = new MyBean();
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(target);
        beanWrapper.setPropertyValue("a", "10");
        beanWrapper.setPropertyValue("b", "hello");
        beanWrapper.setPropertyValue("c", "1999/03/04");
        System.out.println(target);
    }
    
    static class MyBean{
        private int a;
        private String b;
        private Date c;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public Date getC() {
            return c;
        }

        public void setC(Date c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "MyBean{" +
                    "a=" + a +
                    ", b='" + b + '\'' +
                    ", c=" + c +
                    '}';
        }
    }
}
