package com.icecream.helplog;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author andre.lan
 */
@Configuration
@ConditionalOnMissingBean(type = {"helpLogConfiguration"})
@ComponentScan(
        basePackages = {"com.icecream.helplog.config"}
)
public class HelpLogConfiguration {
    public HelpLogConfiguration() {}
}
