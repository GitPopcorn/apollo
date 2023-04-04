package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * V2版本的{@link PropertyResolver}，用于修复解析整个配置文本时，解析出的结果混乱的问题
 */
@Primary
@Component("propertyResolverV2")
public class PropertyResolverV2 implements ConfigTextResolver {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 每个键值对的分隔符 */
	private static final String KV_SEPARATOR = "=";
	
	/** 每个元素的分隔符(即换行符) */
	private static final String ITEM_SEPARATOR = "(\r\n|\r|\n)";
	
	// ===== ===== ===== ===== [重写方法] ===== ===== ===== ===== //
	
	@Override
	public ItemChangeSets resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
		// STEP 拆分新文本
		String[] newLines = configText.split(ITEM_SEPARATOR);
		
		// STEP 解析新文本为配置项封装对象列表，每一行文本都会被解析为一个带类型的配置项封装对象
		List<ItemDTOWrapper> newItemWrapperList = this.parseLines(namespaceId, newLines);
		
		// STEP 判断新配置项列表中是否有重复的Key，若有则抛出异常
		Set<String> repeatKeys = this.getRepeatKeys(newItemWrapperList);
		if (! repeatKeys.isEmpty()) {
			throw new BadRequestException("Config text has repeat keys " + repeatKeys + ", Please check and modify.");
			
		}
		
		// STEP 新增方法返回值对象：配置项变更信息集合
		ItemChangeWrapperSets changeWrapperSets = new ItemChangeWrapperSets();
		
		// STEP 转化老配置项列表，处理成带封装的新对象列表
		List<ItemDTOWrapper> oldItemWrapperList = baseItems.stream().map(ItemDTOWrapper::new).collect(Collectors.toList());
		
		// STEP 对新老配置项进行对比，把对比出的变更信息放入集合对象中
		this.doCompare(oldItemWrapperList, newItemWrapperList, changeWrapperSets);
		
		// STEP 移除配置项变更情况的封装，转化为指定类型并返回
		return changeWrapperSets.toItemChangeSets();
		
	}
	
	// ===== ===== ===== ===== [操作方法-未分类] ===== ===== ===== ===== //
	
	/** 解析多行文本为{@link ItemDTOWrapper}列表 */
	private List<ItemDTOWrapper> parseLines(long namespaceId, String[] lines) {
		// STEP 校验传入参数
		List<ItemDTOWrapper> itemDTOWrappers = new ArrayList<>();
		if (lines == null) { return itemDTOWrappers; }
		
		// STEP 循环解析每行文本
		int lineNum = 0;
		for (String line : lines) {
			// SUBSTEP 行号自增
			lineNum ++;
			
			// SUBSTEP 识别文本
			// BRANCH 空行
			if (ItemDTOWrapper.isBlankLine(line)) {
				itemDTOWrappers.add(new ItemDTOWrapper(this.buildBlankItem(0L, namespaceId, lineNum), line, LineTypes.BLANK));
				continue;
				
			}
			// BRANCH 注释行
			if (ItemDTOWrapper.isCommentLine(line)) {
				itemDTOWrappers.add(new ItemDTOWrapper(this.buildCommentItem(0L, namespaceId, line, lineNum), line, LineTypes.COMMENT));
				continue;
				
			}
			// BRANCH 普通行
			String[] kv = this.parseKeyValue(line, lineNum);
			itemDTOWrappers.add(new ItemDTOWrapper(this.buildNormalItem(0L, namespaceId, kv[0], kv[1], "", lineNum), line, LineTypes.NORMAL));
			
		}
		
		// STEP 返回解析结果
		return itemDTOWrappers;
		
	}
	
	/** 解析KeyValue */
	private String[] parseKeyValue(String line, int lineNum) {
		int kvSeparator = line.indexOf(KV_SEPARATOR);
		if (kvSeparator == -1) {
			throw new BadRequestException("Invalid property line: [" + line + "] at line [" + lineNum + "], must separate by '='");
			
		}
		String[] kv = new String[2];
		kv[0] = line.substring(0, kvSeparator).trim();
		kv[1] = line.substring(kvSeparator + 1).trim();
		return kv;
		
	}
	
	/** 获取重复的配置Key */
	private Set<String> getRepeatKeys(List<ItemDTOWrapper> newItemWrapperList) {
		/*
		return newItemWrapperList.stream()
				.filter(item -> item.getLineType() == LineTypes.NORMAL)
				.map(item -> item.getItemDTO().getKey())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet()
					.stream()
					.filter(entry -> entry.getValue() > 1)
					.map(Map.Entry::getKey)
				.collect(Collectors.toSet())
		;
		*/
		// NOTE 这里不用Stream API，只用常规的增强for循环以提高效率
		// STEP 校验传入参数
		Set<String> repeatKeys = new HashSet<>();
		if (newItemWrapperList == null) { return repeatKeys; }
		
		// STEP 循环遍历新配置项列表，找出重复的Key
		Set<String> keys = new HashSet<>();
		for (ItemDTOWrapper itemWrapper : newItemWrapperList) {
			if (itemWrapper == null) { continue; }
			if (itemWrapper.getLineType() == LineTypes.NORMAL) {
				String key = itemWrapper.getItemDTO().getKey();
				if (keys.contains(key)) {
					repeatKeys.add(key);
					
				}
				keys.add(key);
				
			}
			
		}
		
		// STEP 返回查找结果
		return repeatKeys;
		
	}
	
	/** 对新老配置项进行对比 */
	private void doCompare(List<ItemDTOWrapper> oldItemWrapperList, List<ItemDTOWrapper> newItemWrapperList, ItemChangeWrapperSets changeWrapperSets) {
		// STEP 筛选老配置项，移除其中的null值
		oldItemWrapperList = oldItemWrapperList.stream().filter(ItemDTOWrapper::isItemDTONotNull).collect(Collectors.toList());
		
		// STEP 将老配置项分别按照行号和Key进行分组，如果组内有重复数据，则只取最后一条数据
		Map<Integer, ItemDTOWrapper> lineNumWrapperMap = oldItemWrapperList.stream().collect(Collectors.toMap(
				wrapper -> wrapper.getItemDTO().getLineNum(), Function.identity(), (oldValue, newValue) -> newValue
		));
		Map<String, ItemDTOWrapper> keyWrapperMap = oldItemWrapperList.stream().filter(wrapper -> wrapper.getLineType() == LineTypes.NORMAL)
			.collect(Collectors.toMap(
					wrapper -> wrapper.getItemDTO().getKey(), Function.identity(), (oldValue, newValue) -> newValue
			))
		;
		
		// STEP 对新配置项的每一行进行对比，总结出新增、修改、删除的变更信息
		// NOTE 一切对比严格以新配置项为准，严格保证对比完成并且修改之后，得到的最终结果与新配置项格式严格一致
		Set<Long> handledIdSet = new HashSet<>();
		List<String> commentsTempList = new ArrayList<>();
		for (ItemDTOWrapper newItem : newItemWrapperList) {
			// SUBSTEP 校验是否为空
			if (newItem == null) { continue; }
			
			// BRANCH 若新行是配置行
			if (newItem.getLineType() == LineTypes.NORMAL) {
				// PART 修改新配置行的注释，清空临时注释列表
				newItem.getItemDTO().setComment(String.join("\n", commentsTempList));
				commentsTempList.clear();
				
				// PART 按Key获取第一条对应的老数据行
				ItemDTOWrapper oldItemByKey = keyWrapperMap.get(newItem.getItemDTO().getKey());
				
				// PART 若存在相同Key的老数据行，且数据与新数据不一致(包括注释不一致也算)，则添加修改记录
				if (
						(oldItemByKey != null)
						&& (! ItemDTOWrapper.isPropertiesEqual(oldItemByKey.getItemDTO(), newItem.getItemDTO(), false))
				) {
					ItemDTO updateItemDTO = ItemDTOWrapper.clone(newItem.getItemDTO());
					updateItemDTO.setId(oldItemByKey.getItemDTO().getId());
					changeWrapperSets.addUpdateItem(new ItemDTOWrapper(updateItemDTO));
					handledIdSet.add(oldItemByKey.getItemDTO().getId());
					
				// PART 其余情况，添加一条新增记录
				} else {
					changeWrapperSets.addCreateItem(newItem);
					
				}
				
			// BRANCH 若新行不是配置行
			} else {
				// PART 判断该行是空白行还是注释行
				// SUB-BRANCH 若为空白行，则清空临时注释列表
				if (newItem.getLineType() == LineTypes.BLANK) {
					commentsTempList.clear();
					
				// SUB-BRANCH 若为注释行，则添加到临时注释列表中
				} else {
					commentsTempList.add(newItem.getItemDTO().getComment());
					
				}
				
				// PART 按行号获取第一条对应的老数据行
				ItemDTOWrapper oldItemByLineNum = lineNumWrapperMap.get(newItem.getItemDTO().getLineNum());
				
				// PART 若老数据行存在，并且也不是配置行，并且新行的注释不为空，则添加修改记录
				if ((oldItemByLineNum != null) 
						&& (oldItemByLineNum.getLineType() != LineTypes.NORMAL) 
						&& (StringUtils.isNotBlank(newItem.getItemDTO().getComment()))
				) {
					ItemDTO updateItemDTO = ItemDTOWrapper.clone(newItem.getItemDTO());
					updateItemDTO.setId(oldItemByLineNum.getItemDTO().getId());
					changeWrapperSets.addUpdateItem(new ItemDTOWrapper(updateItemDTO));
					handledIdSet.add(oldItemByLineNum.getItemDTO().getId());
					
					// PART 其他情况，添加新增记录
				} else {
					changeWrapperSets.addCreateItem(newItem);
					
				}
				
			}
			
		}
		
		// STEP 按ID获取剩余的未处理的原数据行，全部添加到删除记录列表中
		oldItemWrapperList.stream()
			.filter(wrapper -> ! handledIdSet.contains(wrapper.getItemDTO().getId()))
			.forEach(changeWrapperSets::addDeleteItem)
		;
		
	}
	
	// ===== ===== ===== ===== [实例工具方法-构筑变更对象] ===== ===== ===== ===== //
	
	/** 构筑注释配置项对象 */
	private ItemDTO buildCommentItem(Long id, Long namespaceId, String comment, int lineNum) {
		return buildNormalItem(id, namespaceId, "", "", comment, lineNum);
		
	}
	
	/** 构筑空行配置项对象 */
	private ItemDTO buildBlankItem(Long id, Long namespaceId, int lineNum) {
		return buildNormalItem(id, namespaceId, "", "", "", lineNum);
		
	}
	
	/** 构筑配置项对象 */
	private ItemDTO buildNormalItem(Long id, Long namespaceId, String key, String value, String comment, int lineNum) {
		ItemDTO item = new ItemDTO(key, value, comment, lineNum);
		item.setId(id);
		item.setNamespaceId(namespaceId);
		return item;
		
	}
	
	// ===== ===== ===== ===== [静态工具方法] ===== ===== ===== ===== //
	
	/** 判断集合是否为空 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null) || collection.isEmpty();
		
	}
	
	/** 安全地从列表中获取元素，若获取不到则返回默认值 */
	public <E> E safelyGetFromList(List<E> list, int index, E defaultValue) {
		if ((list == null) || list.isEmpty() || (index < 0) || (index >= list.size())) { return defaultValue; }
		return list.get(index);
		
	}
	
	// ===== ===== ===== ===== [静态内部类] ===== ===== ===== ===== //
	
	
}
