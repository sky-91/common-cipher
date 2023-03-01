package com.example.commoncipher.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.text.CharSequenceUtil;
import com.example.commoncipher.param.DecryptMacParam;
import com.example.commoncipher.result.EncryptMacResult;
import com.example.commoncipher.result.ExampleCommonResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * ClassName: EnDecryptService <br/>
 * Description: 加密抽象接口 <br/>
 * Date: 2022-11-23 <br/>
 */
public interface EnDecryptService {

    String ENCRYPT_PREFIX = "[SM4]";

    /**
     * 用于加密BLOB存储的特征、byte流读取的图片及文件等
     *
     * @param plainByte 待加密的明文
     * @return the common result
     */
    ExampleCommonResult<byte[]> encryptByte(byte[] plainByte);

    /**
     * 用于加密CLOB存储的特征、base64图片及视频等
     *
     * @param plainBase64 待加密的明文
     * @return the common result
     */
    ExampleCommonResult<String> encryptBase64(String plainBase64);

    /**
     * 用于解密BLOB存储的特征、byte流读取的图片及文件等
     *
     * @param cipherByte 待解密的密文
     * @return the common result
     */
    ExampleCommonResult<byte[]> decryptByte(byte[] cipherByte);

    /**
     * 用于解密CLOB存储的特征、base64图片及视频等
     *
     * @param cipherBase64 待解密的密文
     * @return the common result
     */
    ExampleCommonResult<String> decryptBase64(String cipherBase64);

    /**
     * 用于对byte[]数据计算摘要
     *
     * @param data 待算摘要的数据
     * @return the common result
     */
    ExampleCommonResult<String> generateMacByte(byte[] data);

    /**
     * 用于对base64 string数据计算摘要
     *
     * @param base64Data 待算摘要的数据
     * @return the common result
     */
    ExampleCommonResult<String> generateMacBase64(String base64Data);

    /**
     * 用于对byte[]数据验证摘要
     *
     * @param byteParam 待验证摘要的数据
     * @return the common result
     */
    ExampleCommonResult<Boolean> verifyMacByte(DecryptMacParam byteParam);

    /**
     * 用于对base64 string数据验证摘要
     *
     * @param base64Param 待验证摘要的数据
     * @return the common result
     */
    ExampleCommonResult<Boolean> verifyMacBase64(DecryptMacParam base64Param);

    /**
     * 用于加密BLOB存储的特征、byte流读取的图片及文件等并同步计算摘要
     *
     * @param plainByte 待加密的明文
     * @return the common result
     */
    ExampleCommonResult<EncryptMacResult> encryptMacByte(byte[] plainByte);

    /**
     * 用于加密CLOB存储的特征、base64图片及视频等并同步计算摘要
     *
     * @param plainBase64 待加密的明文
     * @return the common result
     */
    ExampleCommonResult<EncryptMacResult> encryptMacBase64(String plainBase64);

    /**
     * 用于解密BLOB存储的特征、byte流读取的图片及文件等并同步验证摘要
     *
     * @param byteParam 待解密的密文 + 摘要
     * @return the common result
     */
    ExampleCommonResult<byte[]> decryptMacByte(DecryptMacParam byteParam);

    /**
     * 用于解密CLOB存储的特征、base64图片及视频等并同步验证摘要
     *
     * @param base64Param 待解密的密文 + 摘要
     * @return the common result
     */
    ExampleCommonResult<String> decryptMacBase64(DecryptMacParam base64Param);

    /**
     * 用于判断传入的byte[]数据是否已加密
     *
     * @param cipherByte 待判断的密文
     * @return the common result
     */
    default ExampleCommonResult<Boolean> isEncrypt(byte[] cipherByte) {
        if (null == cipherByte || cipherByte.length < ENCRYPT_PREFIX.length())
            return ExampleCommonResult.success(false);

        byte[] preBytes = Arrays.copyOfRange(cipherByte, 0, ENCRYPT_PREFIX.length());
        boolean flag = ENCRYPT_PREFIX.equals(new String(preBytes, StandardCharsets.UTF_8));
        return ExampleCommonResult.success(flag);
    }

    /**
     * 用于判断传入的base64数据是否已加密
     *
     * @param cipherBase64 待判断的密文
     * @return the common result
     */
    default ExampleCommonResult<Boolean> isEncrypt(String cipherBase64) {
        if (CharSequenceUtil.isBlank(cipherBase64)) return ExampleCommonResult.success(false);

        return isEncrypt(Base64.decode(cipherBase64));
    }
}
