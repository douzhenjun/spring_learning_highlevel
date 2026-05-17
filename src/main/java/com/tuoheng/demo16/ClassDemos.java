package com.tuoheng.demo16;

import org.springframework.transaction.annotation.Transactional;

/**
 * @Description 案例类
 * @Author douzhenjun
 * @DATE 2023/4/26
 **/
public class ClassDemos {

    static class T1 {
        @Transactional
        public void foo() {
        }

        public void bar() {
        }
    }

    @Transactional
    static class T2 {
        public void foo() {
        }
    }

    @Transactional
    interface I3 {
        void foo();
    }

    static class T3 implements I3 {
        public void foo() {
        }
    }
}
