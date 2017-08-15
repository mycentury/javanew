package xyz.javanew.util.secure;

public abstract interface IEncryptStrategy {
    /**
     * 加密
     * 
     * @param source
     * @param key 16位
     * @return
     * @throws Exception
     */
    public abstract String encrypt(String source, String key) throws Exception;

    /**
     * 解密
     * 
     * @param source
     * @param key
     * @return
     */
    public abstract String decrypt(String source, String key) throws Exception;

    public abstract EncryptMethod getEncryptMethod();
}