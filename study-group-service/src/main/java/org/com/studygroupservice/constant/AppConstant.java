package org.com.studygroupservice.constant;

import org.springframework.stereotype.Component;

@Component
public class AppConstant {
    public final static String serviceName="study-group-service";
    public static final String SUBJECT_KEY = "subject";
    public static final long TTL_IN_SECONDS = 30 * 24 * 60 * 60;
}
