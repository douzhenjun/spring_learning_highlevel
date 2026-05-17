package com.tuoheng.demo23.a01;

import com.ctc.wstx.shaded.msv_core.grammar.xmlschema.XMLSchemaSchema;
import org.springframework.beans.SimpleTypeConverter;

import java.util.Date;

public class TestSimpleConverter {
    public static void main(String[] args) {
        //仅有类型转换的功能
        SimpleTypeConverter converter = new SimpleTypeConverter();
        Integer number = converter.convertIfNecessary("13", int.class);
        Date date = converter.convertIfNecessary("1999/02/12", Date.class);
        System.out.println(number);
        System.out.println(date);
    }
}
