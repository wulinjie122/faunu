package com.zengdaimoney.chaos.annotation;

import com.zengdaimoney.chaos.configuration.TaskHealthConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that chaos support should be enabled.
 * <p>
 * This should be applied to a Spring java config and should have an accompanying '@Configuration' annotation.
 *
 * @author wulj
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({TaskHealthConfig.class})
public @interface EnableChaos {


}
