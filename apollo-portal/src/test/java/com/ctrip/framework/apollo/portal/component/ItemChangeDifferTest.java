package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import com.ctrip.framework.apollo.portal.component.txtresolver.ItemDTOWrapper;
import com.ctrip.framework.apollo.portal.component.txtresolver.PropertyResolverV2TestUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ctrip.framework.apollo.portal.component.txtresolver.PropertyResolverV2TestUtils.*;
import static org.junit.Assert.assertEquals;

/**
 * {@link ItemChangeDiffer}-基础测试类
 * @author ZhouYi
 * @date 2023/4/4 14:23
 * @note note
 */
public class ItemChangeDifferTest extends AbstractUnitTest {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(ItemChangeDifferTest.class);
	
	/** 当前版本的配置KeyValue映射表的JSON文本  */
	public static final String RELEASE_KEY_VALUE_JSON = 
			"{\n" +
			"\t\"enable-balance-control-modification-validation\": \"true\",\n" +
			"\t\"enable-balance-control-modification-amount-validation\": \"true\",\n" +
			"\t\"static-resource-version-enabled-envs\": \"dev,test,grey,prd,QQ370\",\n" +
			"\t\"static-resource-version-custom-version\": \"\",\n" +
			"\t\"static-resource-version-interval-millis\": \"0\",\n" +
			"\t\"static-resource-version-param-key\": \"rv\",\n" +
			"\t\"static-resource-version-versionable-link-pattern\": \".*[^.]+\\\\.(js|css|html|json|png|jpg|jpeg).*\"\n" +
			"}"
	;
	
	/** 基础{@link ItemDTO}列表JSON文本  */
	public static final String BASE_ITEM_LIST_JSON = 
			"[\n" +
			"\t{\n" +
			"\t\t\"id\": 28689,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# ===== ===== ===== ===== [开户规则校验] ===== ===== ===== ===== #\",\n" +
			"\t\t\"lineNum\": 1,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28690,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 2,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28159,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行校验\",\n" +
			"\t\t\"lineNum\": 3,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:14:52\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:17:32\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28507,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-validation\",\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行校验\",\n" +
			"\t\t\"lineNum\": 4,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28691,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 5,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28573,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行数量校验\",\n" +
			"\t\t\"lineNum\": 6,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:54:06\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28509,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-amount-validation\",\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行数量校验\",\n" +
			"\t\t\"lineNum\": 7,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28692,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 8,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28408,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# ===== ===== ===== ===== [项目静态资源版本号相关] ===== ===== ===== ===== #\",\n" +
			"\t\t\"lineNum\": 9,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:19:31\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:50:13\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28722,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 10,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28460,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-开启静态资源版本功能的服务环境\",\n" +
			"\t\t\"lineNum\": 11,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:50:13\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:50:13\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28562,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-enabled-envs\",\n" +
			"\t\t\"value\": \"dev,test,grey,prd,QQ370\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-开启静态资源版本功能的服务环境\",\n" +
			"\t\t\"lineNum\": 12,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28723,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 13,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28410,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-用户手动指定的版本号\",\n" +
			"\t\t\"lineNum\": 14,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:19:31\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:50:13\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28564,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-custom-version\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-用户手动指定的版本号\",\n" +
			"\t\t\"lineNum\": 15,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28724,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 16,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28412,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-更新版本号的时间间隔(毫秒)，若小于等于0则不生效，若大于0且用户未手动指定版本号，则每次请求都会按时间更新版本号\",\n" +
			"\t\t\"lineNum\": 17,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:19:31\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:50:13\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28566,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-interval-millis\",\n" +
			"\t\t\"value\": \"0\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-更新版本号的时间间隔(毫秒)，若小于等于0则不生效，若大于0且用户未手动指定版本号，则每次请求都会按时间更新版本号\",\n" +
			"\t\t\"lineNum\": 18,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28725,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 19,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28414,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-静态资源URL中，版本号参数的参数键名\",\n" +
			"\t\t\"lineNum\": 20,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:19:31\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:50:13\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28568,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-param-key\",\n" +
			"\t\t\"value\": \"rv\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-静态资源URL中，版本号参数的参数键名\",\n" +
			"\t\t\"lineNum\": 21,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28726,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 22,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28727,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-需要且可以添加版本号的链接的正则表达式\",\n" +
			"\t\t\"lineNum\": 23,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 14:53:41\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28570,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-versionable-link-pattern\",\n" +
			"\t\t\"value\": \".*[^.]+\\\\.(js|css|html|json|png|jpg|jpeg).*\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-需要且可以添加版本号的链接的正则表达式\",\n" +
			"\t\t\"lineNum\": 24,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-04-04 11:51:49\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 14:53:41\"\n" +
			"\t}\n" +
			"]"
	;
	
	/** 被删除的{@link ItemDTO}列表JSON文本  */
	public static final String DELETED_ITEM_LIST_JSON = 
			"[\n" +
			"\t{\n" +
			"\t\t\"id\": 5195,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-validation\",\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行校验\",\n" +
			"\t\t\"lineNum\": 4,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2021-10-25 06:06:21\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 5198,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-amount-validation\",\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行数量校验\",\n" +
			"\t\t\"lineNum\": 7,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2021-10-25 06:06:21\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27049,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-enabled-envs\",\n" +
			"\t\t\"value\": \"dev,test,grey,prd,QQ370\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-开启静态资源版本功能的服务环境\",\n" +
			"\t\t\"lineNum\": 92,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 13:15:31\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26993,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-custom-version\",\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-用户手动指定的版本号\",\n" +
			"\t\t\"lineNum\": 95,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 12:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26996,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-interval-millis\",\n" +
			"\t\t\"value\": \"0\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-更新版本号的时间间隔(毫秒)，若小于等于0则不生效，若大于0且用户未手动指定版本号，则每次请求都会按时间更新版本号\",\n" +
			"\t\t\"lineNum\": 98,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 12:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26999,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-param-key\",\n" +
			"\t\t\"value\": \"rv\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-静态资源URL中，版本号参数的参数键名\",\n" +
			"\t\t\"lineNum\": 101,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 12:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27002,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-versionable-link-pattern\",\n" +
			"\t\t\"value\": \".*[^.]+\\\\.(js|css|html|json|png|jpg|jpeg).*\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-需要且可以添加版本号的链接的正则表达式\",\n" +
			"\t\t\"lineNum\": 104,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 12:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-04-04 11:51:49\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26990,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-versioning-enabled\",\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 146,\n" +
			"\t\t\"dataChangeCreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeLastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChangeCreatedTime\": \"2023-03-31 17:54:06\",\n" +
			"\t\t\"dataChangeLastModifiedTime\": \"2023-03-31 18:15:31\"\n" +
			"\t}\n" +
			"]"
	;
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [静态变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [成员变量] ===== ===== ===== ===== //
	
	/** 属性解析器-V2 */
	@InjectMocks
	private ItemChangeDiffer differ;
	
	// ===== ===== ===== ===== [准备方法] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例测试方法] ===== ===== ===== ===== //
	
	@Test
	public void diffItemChangeSetsForRevokingTest01() {
		// STEP 声明变量
		long namespaceId = 24L;
		String format = ConfigFileFormat.Properties.getValue();
		Map<String, String> releaseItemKeyValue = GSON.fromJson(RELEASE_KEY_VALUE_JSON, STRING_MAP_TYPE);
		List<ItemDTO> currBaseItems = GSON.fromJson(BASE_ITEM_LIST_JSON, ITEM_LIST_TYPE);
		List<ItemDTO> currDeletedItems = GSON.fromJson(DELETED_ITEM_LIST_JSON, ITEM_LIST_TYPE);
		
		// STEP 执行测试-01-A
		ItemChangeSets changeSets = differ.diffItemChangeSetsForRevoking(namespaceId, format, releaseItemKeyValue, currBaseItems, currDeletedItems);
		log.info("测试结果-01-A：{}", changeSets);
		
		// STEP 执行测试-01-B
		List<ItemDTO> newItems = PropertyResolverV2TestUtils.mockPersist(currBaseItems, changeSets);
		assertEquals("处理完毕后的配置项列表中最大行号与行数量不符", newItems.size(), newItems.stream().mapToInt(ItemDTO::getLineNum).max().orElse(0));
		assertEquals("处理完毕后的配置项列表中存在重复的行号", newItems.size(), (int) newItems.stream().mapToInt(ItemDTO::getLineNum).distinct().count());
		String newConfigText = newItems.stream().map(ItemDTOWrapper::toLine).collect(Collectors.joining("\n"));
		log.info("测试结果-01-B-修改后的配置文本：{}", changeSets);
		PropertyResolverV2TestUtils.safelyWriteTo(newConfigText, "target/test/temp/apollo-config-text-02.txt", StandardCharsets.UTF_8);
		
		// STEP 输出日志
		log.info("测试通过");
		
	}
	
	// ===== ===== ===== ===== [操作方法-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例工具方法-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [静态工具方法-未分类] ===== ===== ===== ===== //
	
	
	
	
}