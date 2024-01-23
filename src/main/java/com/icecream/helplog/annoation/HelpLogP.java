package com.icecream.helplog.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author andre.lan
 * 该注解声明的string,请不要随意赋值，容易造成日志错乱
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface HelpLogP {

}
