package com.thinking.machines.webrock.annotations;
import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})  
@Retention(RetentionPolicy.RUNTIME)    
public @interface InjectRequestScope
{
 Class target() default Void.class;
}