/**
 * Project Name:javanew
 * File Name:Test.java
 * Package Name:xyz.javanew.service
 * Date: 2017年7月25日
 * Time: 下午2:53:42
 *
*/

package xyz.javanew.service;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName:Test <br/>
 * TODO ADD DESCRIPTION
 * Date: 2017年7月25日
 * Time: 下午2:53:42
 * @author   yanwg 	 
 */
public class Test {

    @org.junit.Test
    public void test() {
       Map<String,String> map = new HashMap<String, String>();
       map.put("key", "\"value\"");
    }

}

