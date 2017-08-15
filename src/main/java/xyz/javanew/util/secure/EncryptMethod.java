/**
 * 
 */
package xyz.javanew.util.secure;

/**
 * @Desc 加密方式
 * @author wewenge.yan
 * @Date 2016年8月31日
 * @ClassName EncryptMethod
 */
public enum EncryptMethod {
    /**
     * 无加密
     */
    NONE,
    /**
     * AES(可逆)
     */
    AES,
    /**
     * MD5(不可逆)
     */
    MD5,
    SHA1;
}
