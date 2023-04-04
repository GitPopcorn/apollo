package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.portal.AbstractUnitTest;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * {@link PropertyResolverV2}-基础测试类
 * @author ZhouYi
 * @date 2023/4/3 19:01
 * @note note
 */
public class PropertyResolverV2Test extends AbstractUnitTest {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 日志记录器 */
	private static final Logger log = LoggerFactory.getLogger(PropertyResolverV2Test.class);
	
	/** {@link ItemDTO}列表类型 */
	public static final Type ITEM_LIST_TYPE = new TypeToken<List<ItemDTO>>() {}.getType();
	
	/**{@link ItemDTO}列表JSON文本  */
	public static final String ITEM_LIST_JSON = 
			"[\n" +
			"\t{\n" +
			"\t\t\"id\": 7723,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行校验\",\n" +
			"\t\t\"lineNum\": 1,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2022-05-18 21:31:29\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2022-05-18 21:31:29\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28093,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# ===== ===== ===== ===== [开户规则校验] ===== ===== ===== ===== #\",\n" +
			"\t\t\"lineNum\": 1,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28094,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 2,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27649,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# ===== ===== ===== ===== [开户规则校验] ===== ===== ===== ===== #\",\n" +
			"\t\t\"lineNum\": 3,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 03:45:38\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 03:45:38\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27710,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行校验\",\n" +
			"\t\t\"lineNum\": 3,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 03:59:54\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 03:59:54\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 7725,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行数量校验\",\n" +
			"\t\t\"lineNum\": 4,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2022-05-18 21:31:29\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2022-05-18 21:31:29\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 5195,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-validation\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 4,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2021-10-24 22:06:21\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28095,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 5,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 7726,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 6,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2022-05-18 21:31:29\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2022-05-18 21:31:29\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 9155,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 修改开户规则时是否执行数量校验\",\n" +
			"\t\t\"lineNum\": 6,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2022-06-07 03:44:23\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2022-06-07 03:44:23\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27651,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 7,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 03:45:38\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 03:45:38\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 5198,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"enable-balance-control-modification-amount-validation\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"true\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 7,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2021-10-24 22:06:21\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28096,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 8,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28147,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# ===== ===== ===== ===== [项目静态资源版本号相关] ===== ===== ===== ===== #\",\n" +
			"\t\t\"lineNum\": 89,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28148,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 90,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28149,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-开启静态资源版本功能的服务环境\",\n" +
			"\t\t\"lineNum\": 91,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27049,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-enabled-envs\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"dev,test,grey,prd,QQ370\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 92,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-03-31 05:15:31\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28150,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 93,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28151,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-用户手动指定的版本号\",\n" +
			"\t\t\"lineNum\": 94,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26993,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-custom-version\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 95,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-03-31 04:54:06\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28152,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 96,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28153,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-更新版本号的时间间隔(毫秒)，若小于等于0则不生效，若大于0且用户未手动指定版本号，则每次请求都会按时间更新版本号\",\n" +
			"\t\t\"lineNum\": 97,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26996,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-interval-millis\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"0\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 98,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-03-31 04:54:06\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28154,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 99,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28155,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-静态资源URL中，版本号参数的参数键名\",\n" +
			"\t\t\"lineNum\": 100,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 26999,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-param-key\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"rv\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 101,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-03-31 04:54:06\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28156,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 102,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28157,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"# 项目静态资源版本号-需要且可以添加版本号的链接的正则表达式\",\n" +
			"\t\t\"lineNum\": 103,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:54:08\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27002,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"static-resource-version-versionable-link-pattern\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \".*[^.]+\\\\.(js|css|html|json|png|jpg|jpeg).*\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 104,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-03-31 04:54:06\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:54:08\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 27947,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 106,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:34:51\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:34:51\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28009,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 108,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:35:57\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:35:57\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28011,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 111,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:35:57\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:35:57\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28073,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 113,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28075,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 116,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28077,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 119,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28079,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 122,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28019,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 125,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:35:57\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:35:57\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28021,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 128,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:35:57\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:35:57\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28083,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 130,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28085,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 133,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28087,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 136,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28089,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 139,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t},\n" +
			"\t{\n" +
			"\t\t\"id\": 28091,\n" +
			"\t\t\"namespaceId\": 24,\n" +
			"\t\t\"key\": \"\",\n" +
			"\t\t\"type\": 0,\n" +
			"\t\t\"value\": \"\",\n" +
			"\t\t\"comment\": \"\",\n" +
			"\t\t\"lineNum\": 142,\n" +
			"\t\t\"isDeleted\": false,\n" +
			"\t\t\"deletedAt\": 0,\n" +
			"\t\t\"dataChange_CreatedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_CreatedTime\": \"2023-04-03 07:52:41\",\n" +
			"\t\t\"dataChange_LastModifiedBy\": \"apollo\",\n" +
			"\t\t\"dataChange_LastTime\": \"2023-04-03 07:52:41\"\n" +
			"\t}\n" +
			"]"
	;
	
	// ===== ===== ===== ===== [静态变量] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [成员变量] ===== ===== ===== ===== //
	
	/** 属性解析器-V2 */
	@InjectMocks
	private PropertyResolverV2 resolver;
	
	/** GSON对象 */
	private final Gson gson = new Gson();
	
	// ===== ===== ===== ===== [准备方法] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例测试方法] ===== ===== ===== ===== //
	
	@Test
	public void resolveTest01() {
		// STEP 声明变量
		long namespace = 1L;
		String configText = 
				"# ===== ===== ===== ===== [开户规则校验] ===== ===== ===== ===== #\n" +
				"\n" +
				"# 修改开户规则时是否执行校验\n" +
				"enable-balance-control-modification-validation = true\n" +
				"\n" +
				"# 修改开户规则时是否执行数量校验\n" +
				"enable-balance-control-modification-amount-validation = true\n" +
				"\n" +
				"# ===== ===== ===== ===== [项目静态资源版本号相关] ===== ===== ===== ===== #\n" +
				"\n" +
				"# 项目静态资源版本号-开启静态资源版本功能的服务环境\n" +
				"static-resource-version-enabled-envs = dev,test,grey,prd,QQ370\n" +
				"\n" +
				"# 项目静态资源版本号-用户手动指定的版本号\n" +
				"static-resource-version-custom-version = \n" +
				"\n" +
				"# 项目静态资源版本号-更新版本号的时间间隔(毫秒)，若小于等于0则不生效，若大于0且用户未手动指定版本号，则每次请求都会按时间更新版本号\n" +
				"static-resource-version-interval-millis = 0\n" +
				"\n" +
				"# 项目静态资源版本号-静态资源URL中，版本号参数的参数键名\n" +
				"static-resource-version-param-key = rv\n" +
				"\n" +
				"# 项目静态资源版本号-需要且可以添加版本号的链接的正则表达式\n" +
				"static-resource-version-versionable-link-pattern = .*[^.]+\\.(js|css|html|json|png|jpg|jpeg).*\n"
		;
		List<ItemDTO> baseItems = gson.fromJson(ITEM_LIST_JSON, ITEM_LIST_TYPE);
		
		// STEP 执行测试-01-A
		ItemChangeSets changeSets = resolver.resolve(namespace, configText, baseItems);
		log.info("测试结果-01-A：{}", changeSets);
		
		// STEP 执行测试-01-B
		List<ItemDTO> newItems = this.mockPersist(baseItems, changeSets);
		assertEquals("处理完毕后的配置项列表中最大行号与行数量不符", newItems.size(), newItems.stream().mapToInt(ItemDTO::getLineNum).max().orElse(0));
		assertEquals("处理完毕后的配置项列表中存在重复的行号", newItems.size(), (int) newItems.stream().mapToInt(ItemDTO::getLineNum).distinct().count());
		String newConfigText = newItems.stream().map(ItemDTOWrapper::toLine).collect(Collectors.joining("\n"));
		log.info("测试结果-01-B-修改后的配置文本：{}", changeSets);
		safelyWriteTo(newConfigText, "target/test/temp/apollo-config-text-01.txt", StandardCharsets.UTF_8);
		
		// STEP 输出日志
		log.info("测试通过");
		
		// STEP 沉睡一段时间，等待日志输出
		// safelySleep(5000);
		
	}
	
	// ===== ===== ===== ===== [操作方法-未分类] ===== ===== ===== ===== //
	
	
	
	// ===== ===== ===== ===== [实例工具方法-未分类] ===== ===== ===== ===== //
	
	/** 模拟数据库持久化操作 */
	public List<ItemDTO> mockPersist(List<ItemDTO> items, ItemChangeSets changeSets) {
		// STEP 获取删除和更新的配置列表，转化为以Key为键，以ItemDTO为值的Map
		Map<Long, ItemDTO> deletedKeyItemMap = changeSets.getDeleteItems().stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
		Map<Long, ItemDTO> updatedKeyItemMap = changeSets.getUpdateItems().stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
		
		// STEP 获取最大的配置项ID
		Long maxId = items.stream().map(ItemDTO::getId).max(Long::compareTo).orElse(1L);
		
		// STEP 循环处理原有的配置项列表，删除已删除的配置项，更新已更新的配置项，保留未变更的配置项
		// STEP 为新增的配置项设置ID，并添加到新的配置项列表中 
		List<ItemDTO> newItems = new ArrayList<>(items);
		for (ItemDTO item : changeSets.getCreateItems()) {
			item.setId(++ maxId);
			newItems.add(item);
			
		}
		
		// STEP 执行更新操作
		for (ItemDTO item : items) {
			if (updatedKeyItemMap.containsKey(item.getId())) {
				ItemDTOWrapper.copyNonNullProperties(updatedKeyItemMap.get(item.getId()), item);
				
			}
			
		}
		
		// STEP 执行删除操作
		newItems = newItems.stream().filter(item -> ! deletedKeyItemMap.containsKey(item.getId())).collect(Collectors.toList());
		
		// STEP 对处理后的配置项列表进行排序
		newItems.sort(Comparator.comparing(ItemDTO::getLineNum));
		
		// STEP 返回处理后的配置项列表
		return newItems;
		
	}
	
	// ===== ===== ===== ===== [静态工具方法-未分类] ===== ===== ===== ===== //
	
	/** 安全地沉睡指定时长 */
	public static void safelySleep(long millis) {
		try {
			Thread.sleep(millis);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			
		}
		
	}
	
	/** 安全地写出文本到指定文件 */
	@SuppressWarnings("UnstableApiUsage")
	public static void safelyWriteTo(String output, String pathText, Charset charset) {
		try {
			File file = new File(pathText);
			Files.createParentDirs(file);
			Files.write(output, file, charset);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
			
		}
		
	}
	
}
