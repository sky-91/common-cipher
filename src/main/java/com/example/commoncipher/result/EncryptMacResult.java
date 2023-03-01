package com.example.commoncipher.result;

/**
 * ClassName: DecryptMacParam <br/>
 * Description: 加密计算摘要结果 <br/>
 * Date: 2022-11-23 <br/>
 */
public class EncryptMacResult {
    public EncryptMacResult(byte[] cipherByte, String mac) {
        this.cipherByte = cipherByte;
        this.mac = mac;
    }

    public EncryptMacResult(String cipherBase64, String mac) {
        this.cipherBase64 = cipherBase64;
        this.mac = mac;
    }

    /**
     * byte[]密文
     */
    private byte[] cipherByte;

    /**
     * base64密文
     */
    private String cipherBase64;

    /**
     * mac摘要
     */
    private String mac;

    public byte[] getCipherByte() {
        return cipherByte;
    }

    public void setCipherByte(byte[] cipherByte) {
        this.cipherByte = cipherByte;
    }

    public String getCipherBase64() {
        return cipherBase64;
    }

    public void setCipherBase64(String cipherBase64) {
        this.cipherBase64 = cipherBase64;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
