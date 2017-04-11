/**
 * 
 */
package xyz.javanew.interceptor;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2017年4月11日
 * @ClassName PerformenceMonitor
 */
@Component
public class MethodInterceptor {
	private static final Logger logger = Logger.getLogger(MethodInterceptor.class);

	/**
	 * 监控com.henry.advertising.web.service包及其子包的所有public方法、 execution(modifiers-pattern? ret-type-pattern
	 * declaring-type-pattern? name-pattern(param-pattern)throws-pattern?)
	 */
	public void doBefore(JoinPoint jp) throws Throwable {
		logger.info("[" + jp.getTarget().getClass() + "]执行" + jp.getSignature().getName() + "(" + Arrays.toString(jp.getArgs()) + ")START");
	}

	public void doAfter(JoinPoint jp) throws Throwable {
		logger.info("[" + jp.getTarget().getClass() + "]执行" + jp.getSignature().getName() + "(" + Arrays.toString(jp.getArgs()) + ")END");
	}

	public void doAfterReturn(JoinPoint jp) throws Throwable {
		logger.info("[" + jp.getTarget().getClass() + "]执行" + jp.getSignature().getName() + "(" + Arrays.toString(jp.getArgs()) + ")doAfterReturn");
	}

	// 声明环绕通知
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		long begin = System.nanoTime();
		Object obj = pjp.proceed();
		long end = System.nanoTime();
		logger.info("[" + pjp.getTarget().getClass() + "]执行" + pjp.getSignature().getName() + "(" + Arrays.toString(pjp.getArgs()) + ")耗时："
				+ (end - begin) / 1000 + "秒");
		return obj;
	}

	public void doAfterThrow(JoinPoint jp) throws Throwable {
		logger.info("[" + jp.getTarget().getClass() + "]执行" + jp.getSignature().getName() + "(" + Arrays.toString(jp.getArgs()) + ")doAfterThrow");
	}
}
