/**
 * 
 */
package xyz.javanew.util.secure;

import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import xyz.javanew.util.SpringContextUtil;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2016年8月31日
 * @ClassName EncryptUtil
 */
public class EncryptUtil {

    public static String encrypt(String str, String key, EncryptMethod method) {
        Map<String, IEncryptStrategy> beans = SpringContextUtil.getBeans(IEncryptStrategy.class);
        for (IEncryptStrategy strategy : beans.values()) {
            if (strategy.getEncryptMethod().equals(method)) {
                try {
                    return strategy.encrypt(str, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }

    public static String decrypt(String str, String key, EncryptMethod method) {
        Map<String, IEncryptStrategy> beans = SpringContextUtil.getBeans(IEncryptStrategy.class);
        for (IEncryptStrategy strategy : beans.values()) {
            if (strategy.getEncryptMethod().equals(method)) {
                try {
                    return strategy.decrypt(str, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return str;
    }

    public static void main(String[] args) {
        String[] locations = { "classpath:spring/spring-context.xml", "classpath:spring/springmvc-servlet.xml",
                "classpath:spring/spring-rabbitmq.xml", "classpath:spring/cxf-client.xml", "classpath:spring/hessian-client.xml" };
        BeanFactory factory = new ClassPathXmlApplicationContext(locations);
        String key = "1234567890123456";
        String enctypt = EncryptUtil.encrypt("abc123", key, EncryptMethod.AES);
        System.out.println(enctypt);
        String decrypt = EncryptUtil.encrypt(enctypt, key, EncryptMethod.AES);
        System.out.println(decrypt);
        System.exit(0);
    }
}
