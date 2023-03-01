package com.example.commoncipher.config;

import com.example.commoncipher.aop.EnDecryptFieldAspect;
import com.example.commoncipher.service.impl.DefaultNoCipherServiceImpl;
import com.example.commoncipher.service.impl.HutoolBaffleServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: EnDecryptServiceConfig <br/>
 * Description: 配置注入类-根据配置文件的值注入Bean <br/>
 * Date: 2022-11-29 <br/>
 */
@Configuration
@EnableConfigurationProperties
public class EnDecryptServiceConfig {

    /**
     * 加解密拦截注入，避免使用@ComponentScan
     */
    @Bean(name = "enDecryptFieldAspect")
    public EnDecryptFieldAspect initFieldAspect() {
        return new EnDecryptFieldAspect();
    }

    //以下的实现类，在配置的时候，只能指定其中一个为true

    /**
     * hard.cipher.service 缺省，启用不加密实现
     */
    @Bean(name = "enDecryptService")
    @ConditionalOnProperty(prefix = "hard.cipher", name = "service", havingValue = "default", matchIfMissing = true)
    public DefaultNoCipherServiceImpl getNoCipherService() {
        return new DefaultNoCipherServiceImpl();
    }

    /**
     * hard.cipher.service = hutool_sm，启用hutool-挡板加密机
     */
    @Bean(name = "enDecryptService")
    @ConditionalOnProperty(prefix = "hard.cipher", name = "service", havingValue = "hutool_sm")
    public HutoolBaffleServiceImpl getHutoolBaffle() {
        return new HutoolBaffleServiceImpl();
    }
}
