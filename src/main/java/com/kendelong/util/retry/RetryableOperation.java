package com.kendelong.util.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryableOperation
{
	int msToFirstRetry() default 100;
	
	int maxRetries() default 2;

}
