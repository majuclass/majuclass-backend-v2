package com.ssafy.a202.common.roleAop.scenarioPermission;

import com.ssafy.a202.common.roleAop.PermissionAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckScenarioPermission {
    PermissionAction value();
}