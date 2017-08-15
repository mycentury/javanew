/**
 * 
 */
package xyz.javanew.util.secure;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2016年8月23日
 * @ClassName MD5Util
 */
public class MD5Util {
    private static MessageDigest md5;
    private final static String[] CHARS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
    static {
        try {
            if (md5 == null) {
                md5 = MessageDigest.getInstance("MD5");
                System.out.println("md5 初始化");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encode(String source) {
        if (source == null) {
            source = "";
        }
        byte[] digest = null;
        for (int i = 0; i < 49; i++) {
            digest = md5.digest(source.getBytes());
        }
        return bytesToHexString(digest);
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] > bytes[i + 1]) {
                byte temp = bytes[i];
                bytes[i] = bytes[i + 1];
                bytes[i + 1] = temp;
            }
        }
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToHexString(bytes[i]));
        }
        return sb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return (new StringBuilder()).append(CHARS[d1]).append(CHARS[d2]).toString();
    }

    public static void main(String[] args) {
        System.out.println(encode("123"));
        System.out.println(new String(md5.digest("123".getBytes())));
    }
}
