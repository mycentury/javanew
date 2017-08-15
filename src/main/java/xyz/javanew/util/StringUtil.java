/**
 * 
 */
package xyz.javanew.util;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2016年12月29日
 * @ClassName StringUtil
 */
public class StringUtil {
    
    /**
     * 获取两段文本的差异值,0-1000
     *
     * @param text1
     * @param text2
     * @return 差异值
     */
    public int getTextDifference(String text1 ,String text2){
        if (text1==null||text2==null) {
            return 1000;
        }
        char[] charAry1 = text1.toCharArray();
        char[] charAry2 = text2.toCharArray();
        for (char c1 : charAry1) {
            for (char c2 : charAry2) {
                
            }
        }
        return 0;
    }

	public static String initialToUpper(String source) {
		if (source == null || source.length() <= 0) {
			return source;
		}
		char[] charArray = source.toCharArray();
		if ('a' <= charArray[0] && charArray[0] <= 'z') {
			charArray[0] -= 'a' - 'A';
		}
		return new String(charArray);
	}

	public static String initialToLower(String source) {
		if (source == null || source.length() <= 0) {
			return source;
		}
		char[] charArray = source.toCharArray();
		if ('A' <= charArray[0] && charArray[0] <= 'Z') {
			charArray[0] += 'a' - 'A';
		}
		return new String(charArray);
	}
}
