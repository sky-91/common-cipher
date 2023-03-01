package com.example.commoncipher.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.example.commoncipher.annotation.EnDecryptField;
import com.example.commoncipher.exception.ServiceException;
import com.example.commoncipher.param.DecryptMacParam;
import com.example.commoncipher.result.EncryptMacResult;
import com.example.commoncipher.result.ExampleCommonResult;
import com.example.commoncipher.service.EnDecryptService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * ClassName: EnDecryptFieldAspect <br/>
 * Description: mapper接口加解密aop实现 <br/>
 * Date: 2022-11-30 <br/>
 */
@Aspect
public class EnDecryptFieldAspect {

    private static final Logger log = LoggerFactory.getLogger(EnDecryptFieldAspect.class);

    private static final Function<Field, String> getMacField = field ->
            field.getAnnotation(EnDecryptField.class).macField();

    //是否打印加密相关调用日志
    @Value("${encrypt.log.info.print:false}")
    public boolean isPrint;

    @Resource
    private EnDecryptService enDecryptService;

    private static List<Field> getEncryptString(Object object) {
        Field[] fields = ReflectUtil.getFields(object.getClass());
        if (fields.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(fields).filter(field -> field.isAnnotationPresent(EnDecryptField.class))
                .filter(field -> field.getType().isAssignableFrom(String.class))
                .toList();
    }

    private static List<Field> getEncryptByte(Object object) {
        Field[] fields = ReflectUtil.getFields(object.getClass());
        if (fields.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(fields).filter(field -> field.isAnnotationPresent(EnDecryptField.class))
                .filter(field -> field.getType().isArray())
                .toList();
    }

    //建议切入点：实现类，方法上加EnDecryptMapperMethod(或者*mapper.class接口)
    @Pointcut(value = "@annotation(com.example.commoncipher.annotation.EnDecryptMapperMethod)")
    public void cipherDecryptPoint() {
    }

    @SuppressWarnings("unchecked")
    @Around("cipherDecryptPoint()")
    public Object after(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed(joinPoint.getArgs());

        Signature signature = joinPoint.getSignature();
        String classMethod = signature.getName();

        if (isPrint) log.info("~~~~~拦截类:{}, 方法:{}", signature.getDeclaringTypeName(), classMethod);

        if (result instanceof List) {
            List<Object> cipherList = (ArrayList<Object>) result;

            result = cipherList.stream().map(this::doReturnDecrypt).toList();
        } else {
            result = doReturnDecrypt(result);
        }

        if (isPrint) log.info("~~~~~类:{}, 方法:{}, 对返回类型:{} 解密成功", signature.getDeclaringTypeName(),
                classMethod, result.getClass());
        return result;
    }

    private Object doReturnDecrypt(Object object) {
        object = enDecryptByte(object, false);
        return enDecryptString(object, false);
    }

    //建议切入点：实现类，方法上加EnDecryptMapperMethod(或者*mapper.class接口)
    @Pointcut(value = "@annotation(com.example.commoncipher.annotation.EnDecryptMapperMethod)")
    public void plainEncryptPoint() {
    }

    @SuppressWarnings("unchecked")
    @Around("plainEncryptPoint()")
    public Object before(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        if (null == args || args.length != 1) {
            //只处理一个参数的方法
            return point.proceed(args);
        }

        Signature signature = point.getSignature();
        String classMethod = signature.getName();

        if (isPrint) log.info("~~~~~拦截类:{}, 方法:{}", signature.getDeclaringTypeName(), classMethod);

        if (args[0] instanceof List) {
            List<Object> plainList = (ArrayList<Object>) args[0];

            args[0] = plainList.stream().map(this::doBeforeEncrypt).toList();
        } else {
            args[0] = doBeforeEncrypt(args[0]);
        }

        if (isPrint) log.info("~~~~~类:{}, 方法:{}, 对参数类型:{} 加密成功", signature.getDeclaringTypeName(),
                classMethod, args[0].getClass());
        return point.proceed(args);
    }

    /**
     * 前置增强处理
     *
     * @param object object
     */
    private Object doBeforeEncrypt(Object object) {
        object = enDecryptByte(object, true);
        return enDecryptString(object, true);
    }

    /**
     * byte[]多字段加密.
     *
     * @param <T>     the type parameter
     * @param t       the t
     * @param encrypt the encrypt
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private <T> T enDecryptByte(T t, boolean encrypt) {
        List<Field> fieldList = getEncryptByte(t);
        if (checkMultiField(fieldList, encrypt, t)) return t;

        try {
            Object returnObj = t.getClass().getDeclaredConstructor().newInstance();
            BeanUtil.copyProperties(t, returnObj);

            for (Field field : fieldList) {
                if (encrypt) {
                    coreEncryptByte(returnObj, field);
                } else {
                    coreDecryptByte(returnObj, field);
                }
            }
            return (T) returnObj;
        } catch (Exception e) {
            log.error("~~~~~enDecryptByte error : " + e.getMessage(), e);
            return t;
        }
    }

    private void coreEncryptByte(Object returnObj, Field field) throws ServiceException {
        byte[] value = (byte[]) ReflectUtil.getFieldValue(returnObj, field);
        if (ObjectUtil.isEmpty(value)) return;

        String macFieldStr = getMacField.apply(field);

        if (CharSequenceUtil.isBlank(macFieldStr)) {
            ExampleCommonResult<byte[]> encryptResult = enDecryptService.encryptByte(value);

            if (encryptResult.isSuccess()) {
                ReflectUtil.setFieldValue(returnObj, field, encryptResult.getData());
            } else {
                throw new ServiceException(encryptResult.getCode(), encryptResult.getMessage());
            }

        } else {
            String mac = (String) ReflectUtil.getFieldValue(returnObj, macFieldStr);
            if (CharSequenceUtil.isBlank(mac)) {
                ExampleCommonResult<EncryptMacResult> encryptResult = enDecryptService.encryptMacByte(value);

                if (encryptResult.isSuccess()) {
                    ReflectUtil.setFieldValue(returnObj, field, encryptResult.getData().getCipherByte());
                    ReflectUtil.setFieldValue(returnObj, macFieldStr, encryptResult.getData().getMac());
                } else {
                    throw new ServiceException(encryptResult.getCode(), encryptResult.getMessage());
                }

            }
        }
    }

    private void coreDecryptByte(Object returnObj, Field field) throws ServiceException {
        byte[] value = (byte[]) ReflectUtil.getFieldValue(returnObj, field);
        if (ObjectUtil.isEmpty(value)) return;

        String macFieldStr = getMacField.apply(field);
        if (CharSequenceUtil.isBlank(macFieldStr)) {
            ExampleCommonResult<byte[]> decryptResult = enDecryptService.decryptByte(value);

            if (decryptResult.isSuccess()) {
                ReflectUtil.setFieldValue(returnObj, field, decryptResult.getData());
            } else {
                throw new ServiceException(decryptResult.getCode(), decryptResult.getMessage());
            }
        } else {
            String mac = (String) ReflectUtil.getFieldValue(returnObj, macFieldStr);
            if (CharSequenceUtil.isNotBlank(mac)) {

                DecryptMacParam param = new DecryptMacParam(value, mac);
                ExampleCommonResult<byte[]> decryptResult = enDecryptService.decryptMacByte(param);

                if (decryptResult.isSuccess()) {
                    ReflectUtil.setFieldValue(returnObj, field, decryptResult.getData());
                    ReflectUtil.setFieldValue(returnObj, macFieldStr, CharSequenceUtil.EMPTY);
                } else {
                    throw new ServiceException(decryptResult.getCode(), decryptResult.getMessage());
                }
            }
        }
    }

    /**
     * String多字段加密.
     *
     * @param <T>     the type parameter
     * @param t       the t
     * @param encrypt the encrypt
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private <T> T enDecryptString(T t, boolean encrypt) {
        List<Field> fieldList = getEncryptString(t);
        if (checkMultiField(fieldList, encrypt, t)) return t;

        try {
            Object returnObj = t.getClass().getDeclaredConstructor().newInstance();
            BeanUtil.copyProperties(t, returnObj);

            for (Field field : fieldList) {
                if (encrypt) {
                    coreEncryptString(returnObj, field);
                } else {
                    coreDecryptString(returnObj, field);
                }
            }
            return (T) returnObj;
        } catch (Exception e) {
            log.error("~~~~~enDecryptString error : " + e.getMessage(), e);
            return t;
        }
    }

    private void coreEncryptString(Object returnObj, Field field) throws ServiceException {
        String value = (String) ReflectUtil.getFieldValue(returnObj, field);
        if (CharSequenceUtil.isBlank(value)) return;

        String macFieldStr = getMacField.apply(field);

        if (CharSequenceUtil.isBlank(macFieldStr)) {
            ExampleCommonResult<String> encryptResult = enDecryptService.encryptBase64(value);

            if (encryptResult.isSuccess()) {
                ReflectUtil.setFieldValue(returnObj, field, encryptResult.getData());
            } else {
                throw new ServiceException(encryptResult.getCode(), encryptResult.getMessage());
            }
        } else {
            String mac = (String) ReflectUtil.getFieldValue(returnObj, macFieldStr);

            if (CharSequenceUtil.isBlank(mac)) {
                ExampleCommonResult<EncryptMacResult> encryptResult = enDecryptService.encryptMacBase64(value);

                if (encryptResult.isSuccess()) {
                    ReflectUtil.setFieldValue(returnObj, field, encryptResult.getData().getCipherBase64());
                    ReflectUtil.setFieldValue(returnObj, macFieldStr, encryptResult.getData().getMac());
                } else {
                    throw new ServiceException(encryptResult.getCode(), encryptResult.getMessage());
                }
            }
        }
    }

    private void coreDecryptString(Object returnObj, Field field) throws ServiceException {
        String value = (String) ReflectUtil.getFieldValue(returnObj, field);
        if (CharSequenceUtil.isBlank(value)) return;

        String macFieldStr = getMacField.apply(field);

        if (CharSequenceUtil.isBlank(macFieldStr)) {
            ExampleCommonResult<String> decryptResult = enDecryptService.decryptBase64(value);

            if (decryptResult.isSuccess()) {
                ReflectUtil.setFieldValue(returnObj, field, decryptResult.getData());
            } else {
                throw new ServiceException(decryptResult.getCode(), decryptResult.getMessage());
            }
        } else {
            String mac = (String) ReflectUtil.getFieldValue(returnObj, macFieldStr);

            if (CharSequenceUtil.isNotBlank(mac)) {
                DecryptMacParam param = new DecryptMacParam(value, mac);
                ExampleCommonResult<String> decryptResult = enDecryptService.decryptMacBase64(param);

                if (decryptResult.isSuccess()) {
                    ReflectUtil.setFieldValue(returnObj, field, decryptResult.getData());
                    ReflectUtil.setFieldValue(returnObj, macFieldStr, CharSequenceUtil.EMPTY);
                } else {
                    throw new ServiceException(decryptResult.getCode(), decryptResult.getMessage());
                }
            }
        }
    }

    private <T> boolean checkMultiField(List<Field> fieldList, boolean encrypt, T t) {
        if (fieldList.isEmpty()) return true;

        return fieldList.stream().allMatch(field -> {
            Object value = ReflectUtil.getFieldValue(t, field);
            String macFieldStr = getMacField.apply(field);

            if (CharSequenceUtil.isBlank(macFieldStr)) return ObjectUtil.isEmpty(value);

            String mac = (String) ReflectUtil.getFieldValue(t, macFieldStr);

            if (encrypt) {
                //做加密，所有字段的mac均不为空，即视为已加密，直接返回
                return ObjectUtil.isNotEmpty(value) && CharSequenceUtil.isNotBlank(mac);
            } else {
                //做解密，所有字段的value不为空，且mac为空，即视为未加密，直接返回
                return ObjectUtil.isNotEmpty(value) && CharSequenceUtil.isBlank(mac);
            }
        });
    }
}
