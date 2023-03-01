package com.example.commoncipher.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SmUtil;
import com.example.commoncipher.param.DecryptMacParam;
import com.example.commoncipher.result.EncryptMacResult;
import com.example.commoncipher.result.ExampleCommonResult;
import com.example.commoncipher.service.EnDecryptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;

/**
 * ClassName: HutoolBaffleServiceImpl <br/>
 * Description: Hutool SM4加密挡板 <br/>
 * Date: 2022-12-01 <br/>
 */
public class HutoolBaffleServiceImpl implements EnDecryptService {

    private static final String ERROR_CODE = "99999999";
    private static final String ERROR_MAC_MSG = "MAC_CHECK_ERROR";

    private static final byte[] KEY = "1234567887654321".getBytes(StandardCharsets.UTF_8);

    //是否打印加密相关调用日志
    @Value("${encrypt.log.info.print:false}")
    public boolean isPrint;

    private static final Logger log = LoggerFactory.getLogger(HutoolBaffleServiceImpl.class);

    private boolean checkByte(byte[] data) {
        return null == data || data.length == 0;
    }

    /**
     * 添加加密前缀
     */
    private String addPrefix(String data) {
        if (CharSequenceUtil.isBlank(data)) return data;

        //直接在string形式的密文前面加前缀
        return ENCRYPT_PREFIX + data;
    }

    /**
     * 去除加密前缀
     */
    private String removePrefix(String data) {
        if (CharSequenceUtil.isBlank(data) || data.length() <= ENCRYPT_PREFIX.length()) return data;

        //已加密，移除前面固定长度的前缀
        if (checkEncrypted(data)) return data.substring(ENCRYPT_PREFIX.length());

        return data;
    }

    private boolean checkEncrypted(String data) {
        ExampleCommonResult<Boolean> checkResult = isEncrypt(data);
        return checkResult.isSuccess() && Boolean.TRUE.equals(checkResult.getData());
    }

    private boolean checkEncrypted(byte[] data) {
        ExampleCommonResult<Boolean> checkResult = isEncrypt(data);
        return checkResult.isSuccess() && Boolean.TRUE.equals(checkResult.getData());
    }

    @Override
    public ExampleCommonResult<byte[]> encryptByte(byte[] plainByte) {
        //非空判断
        if (checkByte(plainByte)) return ExampleCommonResult.success(new byte[0]);
        //加密前检查是否已加密
        if (checkEncrypted(plainByte)) return ExampleCommonResult.success(plainByte);

        //保证encryptBase64方法的入参一定是base64格式
        ExampleCommonResult<String> result = encryptBase64(Base64.encode(plainByte));
        if (result.isSuccess()) {
            //返回的密文进制未知，不能直接转码，getBytes原文存储，避免转码丢失数据
            return ExampleCommonResult.success(result.getData().getBytes(StandardCharsets.UTF_8));
        }

        return ExampleCommonResult.fail(result.getCode(), result.getMessage());
    }

    @Override
    public ExampleCommonResult<String> encryptBase64(String plainBase64) {
        //非空判断
        if (CharSequenceUtil.isBlank(plainBase64)) return ExampleCommonResult.success("");
        //加密前检查是否已加密
        if (checkEncrypted(plainBase64)) return ExampleCommonResult.success(plainBase64);

        if (isPrint) log.info("~~~~~encryptBase64 data:{}", plainBase64);

        return ExampleCommonResult.success(addPrefix(SmUtil.sm4(KEY).encryptBase64(plainBase64)));
    }

    @Override
    public ExampleCommonResult<byte[]> decryptByte(byte[] cipherByte) {
        //非空判断
        if (checkByte(cipherByte)) return ExampleCommonResult.success(new byte[0]);
        //解密前检查是否已加密
        if (!checkEncrypted(cipherByte)) return ExampleCommonResult.success(cipherByte);

        //密文byte[]内容是以getByte方式获取的，转回string使用new String()，不丢数据
        ExampleCommonResult<String> result = decryptBase64(new String(cipherByte));
        if (result.isSuccess()) {
            //decryptBase64方法的出参一定是base64格式，此处可base64.decode得到原始byte[]
            return ExampleCommonResult.success(Base64.decode(result.getData()));
        }

        return ExampleCommonResult.fail(result.getCode(), result.getMessage());
    }

    @Override
    public ExampleCommonResult<String> decryptBase64(String cipherBase64) {
        //非空判断
        if (CharSequenceUtil.isBlank(cipherBase64)) return ExampleCommonResult.success("");
        //解密前检查是否已加密
        if (!checkEncrypted(cipherBase64)) return ExampleCommonResult.success(cipherBase64);

        //已加密的密文，移除前缀
        cipherBase64 = removePrefix(cipherBase64);
        if (isPrint) log.info("~~~~~decryptBase64 data:{}", cipherBase64);

        return ExampleCommonResult.success(SmUtil.sm4(KEY).decryptStr(cipherBase64));
    }

    @Override
    public ExampleCommonResult<String> generateMacByte(byte[] data) {
        //非空判断
        if (checkByte(data)) return ExampleCommonResult.success("");

        //因为是对密文计算mac，密文格式未知，使用new String()，不丢数据
        return generateMacBase64(new String(data));
    }

    @Override
    public ExampleCommonResult<String> generateMacBase64(String base64Data) {
        //非空判断
        //传入的非密文，不予计算mac
        if (CharSequenceUtil.isBlank(base64Data) || !checkEncrypted(base64Data)) return ExampleCommonResult.success("");

        //已加密的密文，移除前缀再计算mac
        base64Data = removePrefix(base64Data);
        //因为是对密文计算mac，string格式的密文编码格式一定与入参格式匹配
        if (isPrint) log.info("~~~~~generateMacBase64 data:{}", base64Data);

        return ExampleCommonResult.success(SmUtil.sm3WithSalt(KEY).digestHex(base64Data));
    }

    @Override
    public ExampleCommonResult<Boolean> verifyMacByte(DecryptMacParam byteParam) {
        //非空判断
        if (checkByte(byteParam.getCipherByte()) || CharSequenceUtil.isBlank(byteParam.getMac()))
            return ExampleCommonResult.success(false);

        //因为是对密文计算mac，密文格式未知，使用new String()，不丢数据
        byteParam.setCipherBase64(new String(byteParam.getCipherByte()));
        return verifyMacBase64(byteParam);
    }

    @Override
    public ExampleCommonResult<Boolean> verifyMacBase64(DecryptMacParam base64Param) {
        String data = base64Param.getCipherBase64();
        String mac = base64Param.getMac();

        //非空判断
        //传入的非密文，校验mac不通过
        if (CharSequenceUtil.isBlank(data) || CharSequenceUtil.isBlank(mac) ||
                !checkEncrypted(data)) return ExampleCommonResult.success(false);

        //已加密的密文，移除前缀
        data = removePrefix(data);

        if (isPrint) log.info("~~~~~verifyMacBase64 data:{}, mac:{}", data, mac);

        String newMac = SmUtil.sm3WithSalt(KEY).digestHex(data);
        return mac.equals(newMac) ? ExampleCommonResult.success(true)
                : ExampleCommonResult.fail(ERROR_CODE, ERROR_MAC_MSG);
    }

    @Override
    public ExampleCommonResult<EncryptMacResult> encryptMacByte(byte[] plainByte) {
        ExampleCommonResult<String> encryptResult = encryptBase64(Base64.encode(plainByte));

        if (encryptResult.isSuccess() && CharSequenceUtil.isNotBlank(encryptResult.getData())) {
            //增加前缀，可判断成密文再去计算mac
            String data = encryptResult.getData();

            ExampleCommonResult<String> macResult = generateMacBase64(data);
            if (macResult.isSuccess() && CharSequenceUtil.isNotEmpty(macResult.getData())) {
                //密文格式未知，不能直接转码，getBytes原文存储，避免转码丢失数据
                return ExampleCommonResult.success(
                        new EncryptMacResult(data.getBytes(StandardCharsets.UTF_8), macResult.getData()));
            }

            return ExampleCommonResult.fail(macResult.getCode(), macResult.getMessage());
        }
        return ExampleCommonResult.fail(encryptResult.getCode(), encryptResult.getMessage());
    }

    @Override
    public ExampleCommonResult<EncryptMacResult> encryptMacBase64(String plainBase64) {
        //非空判断
        ExampleCommonResult<String> encryptResult = encryptBase64(plainBase64);

        if (encryptResult.isSuccess() && CharSequenceUtil.isNotBlank(encryptResult.getData())) {
            String data = encryptResult.getData();

            ExampleCommonResult<String> macResult = generateMacBase64(data);
            if (macResult.isSuccess() && CharSequenceUtil.isNotEmpty(macResult.getData())) {
                //密文格式未知，直接当string存储
                return ExampleCommonResult.success(new EncryptMacResult(data, macResult.getData()));
            }

            return ExampleCommonResult.fail(macResult.getCode(), macResult.getMessage());
        }
        return ExampleCommonResult.fail(encryptResult.getCode(), encryptResult.getMessage());
    }

    @Override
    public ExampleCommonResult<byte[]> decryptMacByte(DecryptMacParam byteParam) {
        //非空判断
        ExampleCommonResult<Boolean> macResult = verifyMacByte(byteParam);

        if (macResult.isSuccess() && Boolean.TRUE.equals(macResult.getData())) {
            return decryptByte(byteParam.getCipherByte());
        }

        return ExampleCommonResult.fail(macResult.getCode(), macResult.getMessage());
    }

    @Override
    public ExampleCommonResult<String> decryptMacBase64(DecryptMacParam base64Param) {
        //非空判断
        ExampleCommonResult<Boolean> macResult = verifyMacBase64(base64Param);

        if (macResult.isSuccess() && Boolean.TRUE.equals(macResult.getData())) {
            return decryptBase64(base64Param.getCipherBase64());
        }

        return ExampleCommonResult.fail(macResult.getCode(), macResult.getMessage());
    }

    @Override
    public ExampleCommonResult<Boolean> isEncrypt(byte[] cipherByte) {
        //非空判断
        if (checkByte(cipherByte) || cipherByte.length < ENCRYPT_PREFIX.getBytes().length)
            return ExampleCommonResult.success(false);

        //判断密文，不能使用任何转换
        return this.isEncrypt(new String(cipherByte));
    }

    @Override
    public ExampleCommonResult<Boolean> isEncrypt(String cipherBase64) {
        //非空判断
        if (CharSequenceUtil.isBlank(cipherBase64)) return ExampleCommonResult.success(false);

        return ExampleCommonResult.success(cipherBase64.startsWith(ENCRYPT_PREFIX));
    }
}
