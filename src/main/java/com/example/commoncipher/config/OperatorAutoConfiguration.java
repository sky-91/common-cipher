package com.example.commoncipher.config;

import com.example.commoncipher.operator.Operator;
import com.example.commoncipher.operator.OperatorRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OperatorAutoConfiguration {

    @Autowired(required = false)
    public void initOperatorRouter(Map<String, OperatorRouter> routerMap, ApplicationContext applicationContext) {
        if (null != routerMap && !routerMap.isEmpty()) {

            routerMap.values().forEach(router -> {
                Class<Operator> operatorClass = router.getOperatorClass();
                Map<String, Operator> beans = applicationContext.getBeansOfType(operatorClass);

                Map<Object, Operator> tempMap = new HashMap<>(8);

                beans.forEach((beanName, operator) -> {
                    router.checkOperator(operator);
                    tempMap.put(operator.getName(), operator);
                });

                router.setOperatorMap(Collections.unmodifiableMap(tempMap));
            });

        }
    }

}
