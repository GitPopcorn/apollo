package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ItemDTOWrapper}-基础测试类
 * @author ZhouYi
 * @date 2023/4/4 11:10
 * @note note
 */
public class ItemDTOWrapperTest extends AbstractUnitTest {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(ItemDTOWrapperTest.class);
	
	// ===== ===== ===== ===== [静态变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [成员变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [准备方法] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例测试方法] ===== ===== ===== ===== //
	
	@Test
	public void testIsCommentLineTest01() {
		// STEP 声明变量
		String line01 = "# 注释行";
		
		// STEP 执行测试-01
		boolean result01 = ItemDTOWrapper.isCommentLine(line01);
		log.info("测试结果-01：{}", result01);
		Assert.assertTrue(result01);
		
		// STEP 输出日志
		log.info("测试通过");
		
	}
	
	// ===== ===== ===== ===== [操作方法-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例工具方法-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [静态工具方法-未分类] ===== ===== ===== ===== //
	
	
	
	
}