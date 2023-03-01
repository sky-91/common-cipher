package com.example.commoncipher.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.example.commoncipher.param.DecryptMacParam;
import com.example.commoncipher.result.EncryptMacResult;
import com.example.commoncipher.result.ExampleCommonResult;
import com.example.commoncipher.service.EnDecryptService;

/**
 * ClassName: DefaultNoCipherServiceImpl <br/>
 * Description: 默认不加密的实现 <br/>
 * Date: 2022-12-02 <br/>
 */
public class DefaultNoCipherServiceImpl implements EnDecryptService {
    @Override
    public ExampleCommonResult<byte[]> encryptByte(byte[] plainByte) {
        return ExampleCommonResult.success(plainByte);
    }

    @Override
    public ExampleCommonResult<String> encryptBase64(String plainBase64) {
        return ExampleCommonResult.success(plainBase64);
    }

    @Override
    public ExampleCommonResult<byte[]> decryptByte(byte[] cipherByte) {
        return ExampleCommonResult.success(cipherByte);
    }

    @Override
    public ExampleCommonResult<String> decryptBase64(String cipherBase64) {
        return ExampleCommonResult.success(cipherBase64);
    }

    @Override
    public ExampleCommonResult<String> generateMacByte(byte[] data) {
        return ExampleCommonResult.success(CharSequenceUtil.EMPTY);
    }

    @Override
    public ExampleCommonResult<String> generateMacBase64(String base64Data) {
        return ExampleCommonResult.success(CharSequenceUtil.EMPTY);
    }

    @Override
    public ExampleCommonResult<Boolean> verifyMacByte(DecryptMacParam byteParam) {
        return ExampleCommonResult.success(true);
    }

    @Override
    public ExampleCommonResult<Boolean> verifyMacBase64(DecryptMacParam base64Param) {
        return ExampleCommonResult.success(true);
    }

    @Override
    public ExampleCommonResult<EncryptMacResult> encryptMacByte(byte[] plainByte) {
        return ExampleCommonResult.success(new EncryptMacResult(plainByte, CharSequenceUtil.EMPTY));
    }

    @Override
    public ExampleCommonResult<EncryptMacResult> encryptMacBase64(String plainBase64) {
        return ExampleCommonResult.success(new EncryptMacResult(plainBase64, CharSequenceUtil.EMPTY));
    }

    @Override
    public ExampleCommonResult<byte[]> decryptMacByte(DecryptMacParam byteParam) {
        return ExampleCommonResult.success(byteParam.getCipherByte());
    }

    @Override
    public ExampleCommonResult<String> decryptMacBase64(DecryptMacParam base64Param) {
        return ExampleCommonResult.success(base64Param.getCipherBase64());
    }

    @Override
    public ExampleCommonResult<Boolean> isEncrypt(byte[] cipherByte) {
        return ExampleCommonResult.success(true);
    }

    @Override
    public ExampleCommonResult<Boolean> isEncrypt(String cipherBase64) {
        return ExampleCommonResult.success(true);
    }
}
