package com.campusevents.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the currently authenticated user into controller methods.
 * 
 * Usage:
 * <pre>
 * @GetMapping("/profile")
 * public ResponseEntity<?> getProfile(@CurrentUser User user) {
 *     return ResponseEntity.ok(user);
 * }
 * </pre>
 * 
 * If the user is not authenticated, null will be injected.
 * Use the 'required' attribute to make authentication mandatory.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    /**
     * Whether authentication is required.
     * If true and user is not authenticated, a 401 response will be returned.
     */
    boolean required() default true;
}
