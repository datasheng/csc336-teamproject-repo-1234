package com.campusevents.security;

import com.campusevents.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves @CurrentUser annotated method arguments.
 * 
 * This class extracts the authenticated user from the request attributes
 * (set by JwtAuthFilter) and injects it into controller method parameters.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(User.class);
    }
    
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        
        User currentUser = (User) request.getAttribute("currentUser");
        
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        if (annotation != null && annotation.required() && currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        return currentUser;
    }
}
