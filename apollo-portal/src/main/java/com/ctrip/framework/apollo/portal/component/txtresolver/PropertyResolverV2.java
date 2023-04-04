package com.ctrip.framework.apollo.portal.component.txtresolver;

import com.ctrip.framework.apollo.common.dto.ItemChangeSets;
import com.ctrip.framework.apollo.common.dto.ItemDTO;
import com.ctrip.framework.apollo.common.exception.BadRequestException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * V2版本的{@link PropertyResolver}，用于修复解析整个配置文本时，解析出的结果混乱的问题
 */
@Primary
@Component("propertyResolver")
public class PropertyResolverV2 implements ConfigTextResolver {
	
	// ===== ===== ===== ===== [常量-未分类] ===== ===== ===== ===== //
	
	/** 每个键值对的分隔符 */
	private static final String KV_SEPARATOR = "=";
	
	/** 每个元素的分隔符(即换行符) */
	private static final String ITEM_SEPARATOR = "\n";
	
	/** 按照行号排序的对比器 */
	public static final Comparator<ItemDTOWrapper> LINE_NUM_COMPARATOR = Comparator.comparingInt(wrapper -> wrapper.getItemDTO().getLineNum());
	
	// ===== ===== ===== ===== [重写方法] ===== ===== ===== ===== //
	
	@Override
	public ItemChangeSets resolve(long namespaceId, String configText, List<ItemDTO> baseItems) {
		// STEP 拆分新文本
		String[] newLines = configText.split(ITEM_SEPARATOR);
		
		// STEP 解析新文本为列表
		List<ItemDTOWrapper> newItemWrapperList = this.parseLines(namespaceId, newLines);
		
		// STEP 判断新文本中是否有重复的Key
		Set<String> repeatKeys = this.getRepeatKeys(newItemWrapperList);
		if (! repeatKeys.isEmpty()) {
			throw new BadRequestException("Config text has repeat keys [" + repeatKeys + "], Please check and modify.");
			
		}
		
		// STEP 新增配置项变更信息对象，修改其中的列表类型
		ItemChangeWrapperSets changeWrapperSets = new ItemChangeWrapperSets();
		
		// STEP 转化老配置行的数据类型
		List<ItemDTOWrapper> oldItemWrapperList = baseItems.stream().map(ItemDTOWrapper::new).collect(Collectors.toList());
		
		// STEP 处理老配置行，移除老行中行号重复的数据
		// baseItems = this.removeDuplicatedLineNumItems(baseItems, changeWrapperSets.getDeleteItems());
		oldItemWrapperList = this.normalizeLineNums(oldItemWrapperList, changeWrapperSets.getCreateItems(), changeWrapperSets.getDeleteItems());
		
		// STEP 对新老配置项进行简单对比
		this.doSimpleCompare(oldItemWrapperList, newItemWrapperList, changeWrapperSets);
		
		// STEP 解析对比所得的变更项，将部分变更项进行简化合并(同内容同行号新增又删除的，合并为不修改；同Key新增又删除的，合并为简单修改；)
		this.combineRedundantChanges(changeWrapperSets);
		
		// STEP 返回配置项变更情况集合
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
		
	}
	
	/** 移除配置项列表中行号重复的项，将其添加到重复的配置项列表中，返回处理好的新配置项列表
	 * @deprecated 已被替换为{@link #normalizeLineNums(List, List, List)}
	 */
	@Deprecated
	private List<ItemDTO> removeDuplicatedLineNumItems(List<ItemDTO> srcItems, List<ItemDTO> duplicatedLineNumItems) {
		// STEP 校验传入参数
		List<ItemDTO> result = new ArrayList<>();
		if (isEmpty(srcItems)) { return result; }
		
		// STEP 循环读取每个配置项的行号，如果行号已经存在，则将该配置项放入重复行列表中，否则放入结果列表中
		Set<Integer> lineNumSet = new HashSet<>();
		for (ItemDTO item : srcItems) {
			if (lineNumSet.contains(item.getLineNum())) {
				if (duplicatedLineNumItems != null) {
					duplicatedLineNumItems.add(item);
					
				}
				
			} else {
				result.add(item);
				lineNumSet.add(item.getLineNum());
				
			}
			
		}
		
		// STEP 返回处理后的结果
		return result;
		
	}
	
	/** 规范配置项列表中的行号，移除行号重复的项，补全缺失的行号，将移除和补全的数据添加到参数的配置项列表中，返回处理好的新配置项列表 */
	private List<ItemDTOWrapper> normalizeLineNums(List<ItemDTOWrapper> srcItems, List<ItemDTOWrapper> addedItems, List<ItemDTOWrapper> removedItems) {
		// STEP 校验传入参数
		List<ItemDTOWrapper> result = new ArrayList<>();
		if (isEmpty(srcItems)) { return result; }
		
		// STEP 过滤掉配置项中为null的数据，并且按照行号排序
		List<ItemDTOWrapper> filteredItems = srcItems.stream()
				.filter(Objects::nonNull)
				.sorted(LINE_NUM_COMPARATOR)
				.collect(Collectors.toList())
		;
		
		// STEP 循环读取每个配置项的行号，如果行号已经存在，则将该配置项放入重复行列表中，否则放入结果列表中
		int lineNumTemp = 1;
		Set<Integer> lineNumSet = new HashSet<>();
		for (ItemDTOWrapper wrapper : filteredItems) {
			// BRANCH 如果对象为空，则直接跳过
			if ((wrapper == null) || (wrapper.getItemDTO() == null)) { continue; }
			ItemDTO itemDTO = wrapper.getItemDTO();
			
			// BRANCH 如果行号已经存在，则将该配置项放入重复行列表中
			int lineNum = itemDTO.getLineNum();
			if (lineNumSet.contains(lineNum)) {
				if (removedItems != null) {
					removedItems.add(wrapper);
					
				}
				
			// BRANCH 如果行号不存在
			} else {
				// SUBSTEP 补全缺失的行号
				while (lineNumTemp < (lineNum - 1)) {
					ItemDTOWrapper blankItem = new ItemDTOWrapper(this.buildBlankItem(0L, itemDTO.getNamespaceId(), lineNumTemp));
					result.add(blankItem);
					addedItems.add(blankItem);
					lineNumTemp ++;
					lineNumSet.add(lineNumTemp);
					
				}
				// SUBSTEP 将目标行的配置项放入结果列表中
				result.add(wrapper);
				lineNumSet.add(lineNum);
				
			}
			
			// STEP 为临时行号变量赋值
			lineNumTemp = lineNum;
			
		}
		
		// STEP 返回处理后的结果
		return result;
		
	}
	
	/** 对新老配置项进行简单对比 */
	private void doSimpleCompare(List<ItemDTOWrapper> oldItemWrapperList, List<ItemDTOWrapper> newItemWrapperList, ItemChangeWrapperSets changeWrapperSets) {
		int oldItemCount = oldItemWrapperList.size();
		int newItemCount = newItemWrapperList.size();
		int maxIndex = Math.max(oldItemCount, newItemCount);
		for (int i = 0; i < maxIndex; i ++) {
			// SUBSTEP 获取新老配置项
			ItemDTOWrapper newItem = safelyGetFromList(newItemWrapperList, i, null);
			ItemDTOWrapper oldItem = safelyGetFromList(oldItemWrapperList, i, null);
			
			// SUBSTEP 判断新老配置项是否为空，是否已经超出列表范围
			boolean isNewItemNull = ((newItem == null) || (newItem.getItemDTO() == null));
			boolean isOldItemNull = ((oldItem == null) || (oldItem.getItemDTO() == null));
			boolean isNewItemOverCount = (i >= newItemCount);
			boolean isOldItemOverCount = (i >= oldItemCount);
			if (isNewItemNull && (! isNewItemOverCount)) { throw new BadRequestException("Null value found in new property items"); }
			if (isOldItemNull && (! isOldItemOverCount)) { throw new BadRequestException("Null value found in old property items"); }
			
			// SUBSTEP 若新老配置项都为空，则直接跳过
			if (isOldItemNull && isNewItemNull) { continue; }
			
			// SUBSTEP 若老配置项为空，新配置项不为空，则直接添加一条新增记录
			if (isOldItemNull) {
				changeWrapperSets.addCreateItem(newItem);
				continue;
				
			}
			
			// SUBSTEP 若新配置项为空，老配置项不为空，直接添加一条删除记录
			if (isNewItemNull) {
				changeWrapperSets.addDeleteItem(oldItem);
				continue;
				
			}
			
			// SUBSTEP 若新老配置项行号不一致，则直接抛出异常
			int newLineNum = newItem.getItemDTO().getLineNum();
			int oldLineNum = oldItem.getItemDTO().getLineNum();
			if (newLineNum != oldLineNum) {
				throw new BadRequestException("Line number mismatch between old and new property items, old line number [" 
						+ oldLineNum + "], new line number [" + newLineNum + "]"
				);
				
			}
			
			// SUBSTEP 若新老配置项类型不一致，则添加一条删除记录，一条新增记录，否则添加一条修改记录
			if (newItem.getLineType() != oldItem.getLineType()) {
				changeWrapperSets.addDeleteItem(oldItem);
				changeWrapperSets.addCreateItem(newItem);
				
			} else {
				ItemDTO updateItemDTO = ItemDTOWrapper.clone(newItem.getItemDTO());
				updateItemDTO.setId(oldItem.getItemDTO().getId());
				changeWrapperSets.addUpdateItem(new ItemDTOWrapper(updateItemDTO));
				
			}
			
		}
		
	}
	
	/** 解析对比所得的变更项，将部分变更项进行简化合并(同内容同行号新增又删除的，合并为不修改；同Key新增又删除的，合并为简单修改；) */
	private void combineRedundantChanges(ItemChangeWrapperSets changeWrapperSets) {
		// STEP 提取出新增项、删除项列表
		List<ItemDTOWrapper> createItems = changeWrapperSets.getCreateItems();
		List<ItemDTOWrapper> deleteItems = changeWrapperSets.getDeleteItems();
		
		// STEP 过滤出新增项、删除项列表中的普通配置行，转化为配置Key相关的Map，便于后续查询
		Predicate<ItemDTOWrapper> filter = item -> item.getLineType() == LineTypes.NORMAL;
		Collector<ItemDTOWrapper, ?, Map<String, ItemDTOWrapper>> collector = Collectors.toMap(item -> item.getItemDTO().getKey(), item -> item);
		Map<String, ItemDTOWrapper> keyWrapperCreateMap = createItems.stream().filter(filter).collect(collector);
		Map<String, ItemDTOWrapper> keyWrapperDeleteMap = deleteItems.stream().filter(filter).collect(collector);
		
		// STEP 声明新的新增项、删除项列表，用于存放合并后的结果
		List<ItemDTOWrapper> newCreateItems = new ArrayList<>();
		List<ItemDTOWrapper> newDeleteItems = new ArrayList<>();
		List<ItemDTOWrapper> newUpdateItems = changeWrapperSets.getUpdateItems();
		List<ItemDTOWrapper> ignoredDeleteItems = new ArrayList<>();
		
		// STEP 循环遍历新增项列表，对于每一项新增项，判断是否存在对应的删除项，若存在，则进行合并处理
		for (ItemDTOWrapper createItem : createItems) {
			// SUBSTEP 若新增项不是普通配置行，则跳过
			if (createItem.getLineType() != LineTypes.NORMAL) {
				newCreateItems.add(createItem);
				continue;
				
			}
			
			// SUBSTEP 按照新增项的Key，从删除项列表中查找对应的删除项
			ItemDTOWrapper deleteItem = keyWrapperDeleteMap.get(createItem.getItemDTO().getKey());
			
			// SUBSTEP 若未找到对应的删除项，则跳过
			if (deleteItem == null) {
				newCreateItems.add(createItem);
				continue;
				
			}
			
			// SUBSTEP 若两者的值、注释或者行号不一致，则添加一个修改记录，然后将被忽略的删除项添加到忽略列表中
			if (! Objects.equals(createItem.getItemDTO().getValue(), deleteItem.getItemDTO().getValue())
					|| ! Objects.equals(createItem.getItemDTO().getComment(), deleteItem.getItemDTO().getComment())
					|| (createItem.getItemDTO().getLineNum() != deleteItem.getItemDTO().getLineNum())) {
				// PART 添加一个修改记录
				ItemDTO newUpdateItem = ItemDTOWrapper.clone(createItem.getItemDTO());
				newUpdateItem.setId(createItem.getItemDTO().getId());
				changeWrapperSets.addUpdateItem(new ItemDTOWrapper(newUpdateItem));
				
				// PART 将被忽略的删除项添加到忽略列表中
				ignoredDeleteItems.add(deleteItem);
				
			}
			
		}
		
		// STEP 使用被忽略的删除项列表，过滤出其他的删除项列表
		newDeleteItems = deleteItems.stream().filter(item -> ! ignoredDeleteItems.contains(item)).collect(Collectors.toList());
		
		// STEP 对新的删除项、修改项列表按ID进行去重
		newDeleteItems = newDeleteItems.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> 
				new TreeSet<>(Comparator.comparing(wrapper -> wrapper.getItemDTO().getId()))), ArrayList::new)
		);
		newUpdateItems = changeWrapperSets.getUpdateItems().stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
				new TreeSet<>(Comparator.comparing(wrapper -> wrapper.getItemDTO().getId()))), ArrayList::new)
		);
		
		// STEP 对新的新增项、删除项、修改项列表按行号进行排序
		newCreateItems.sort(LINE_NUM_COMPARATOR);
		newDeleteItems.sort(LINE_NUM_COMPARATOR);
		newUpdateItems.sort(LINE_NUM_COMPARATOR);
		
		// STEP 将新的新增项、删除项列表存入变更项集合中
		changeWrapperSets.setCreateItems(newCreateItems);
		changeWrapperSets.setDeleteItems(newDeleteItems);
		changeWrapperSets.setUpdateItems(newUpdateItems);
		
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
