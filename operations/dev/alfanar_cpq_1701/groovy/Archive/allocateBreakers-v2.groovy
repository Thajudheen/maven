import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator
import java.util.List;
import java.util.Map;
import java.util.TreeMap

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets
import com.imc.core.Tree;
import com.imc.datamodel.transferobjects.BusinessObjectDTO;
import com.imc.iss.web.config.Settings;
import com.imc.iss.web.services.commands.quote.GetQuoteSalesItemsTreeForReallocationCommand;
import com.imc.iss.web.services.impl.QuoteServiceImpl
import com.imc.util.BODTOUtil;
import com.imc.util.NullUtil;

/*
 * Global constants for the groovy
 */
	interface ApplicationConstants {
		//breaker
		static final String BREAKER_DA_MATERIALCATEGORY = "MaterialCategory";
		static final String BREAKER_DA_RATEDCURRENT = "LV-Breaker-Rated current-AF";
		static final String BREAKER_DF_MODULARITY_ACB = 34;
		static final String BREAKER_DF_MODULARITY_MCCB = 3;
		
		//cubicle
		


		//SwitchBoard
		static final String SB_DA_RATEDCURRENTMAINBUSBAR = "LV-SB-MF_IBB_U";
		static final String SB_DA_FORM = "LV-SB-MF_SEPERATION_U";

		Map cubicleERPID = [
			"ACB":   "C-ACB-0001",
			"GENERIC":   "1-LV-CUBICLE-GENERIC",
			"LV-CUBICLE-HORIZONTAL": "LV-CUBICLE-HORIZONTAL",
			"LV-CUBICLE-VERTICAL" : "LV-CUBICLE-VERTICAL",
			"LV-CUBICLE-2-VERTICAL": "LV-CUBICLE-2-VERTICAL",
		]
	}
	
	

	public enum MaterialCategory
	{
		ACB(2), MCCB(1), ICCB(3), CBR(4), GENERIC(99);

		int priority;


		MaterialCategory(int priority)
		{
			this.priority = priority;
		}

		public int getPriority()
		{
			return priority;
		}
	}

	/*
	 * local function to get DAValue
	 */
	Closure <Object> getDAValue = { Map<String, Object> configItem, String attribute ->
		String value="";

		if(configItem==null) {
			return "";
		}

		Object result = configItem.get(attribute);

		if(result == null) {
			value = "";
		}
		else if (result instanceof List<?>)
		{
			List<String> values = (List<String>) result;
			if (!values.isEmpty())
			{
				value = String.valueOf(values.get(0));
				if (StringUtils.isBlank(value) || value.equalsIgnoreCase("null"))
					value = "";
			}
		}
		else
		{
			value = String.valueOf(result);
			if (StringUtils.isBlank(value) || value.equalsIgnoreCase("null"))
				value = "";
		}

		return value;
	}

	Closure<Tree<BusinessObjectDTO>> convertToMutableList = { Tree<BusinessObjectDTO> bodto ->
		List newChild  =  new ArrayList<Tree<BusinessObjectDTO>>();
		for (Tree<BusinessObjectDTO> treeObj : bodto.children) {
			newChild.add(treeObj);
		}
		bodto.children = newChild;
		return bodto;
	}

	Closure <MaterialCategory> getTypeOfMaterialCategory = {String materialCategoryKey ->
		MaterialCategory materialCategory = null;

		switch(materialCategoryKey) {
				case "ACB": materialCategory = MaterialCategory.ACB; break;
				case "MCCB": materialCategory = MaterialCategory.MCCB; break;
				case "ICCB": materialCategory = MaterialCategory.ICCB; break;
				case "CBR": materialCategory = MaterialCategory.CBR; break;
				default:
				materialCategory = MaterialCategory.GENERIC;
		}
		return materialCategory;
	}

	Closure< Map<MaterialCategory, List<Tree<BusinessObjectDTO>>> > categoriseBreaker = { List<Tree<BusinessObjectDTO>> salesItems->

		Map<MaterialCategory, List<Tree<BusinessObjectDTO>> > sortedBreakerMap = new TreeMap<MaterialCategory, List<Tree<BusinessObjectDTO>>>();

		for (Tree<BusinessObjectDTO> salesItem : salesItems)
		{
			final BusinessObjectDTO salesItemBODTO = salesItem.retrieveBODTOValue();

			final String breakerMaterialCategory = getDAValue(salesItemBODTO.objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_MATERIALCATEGORY);

			MaterialCategory materialCategoryKey = getTypeOfMaterialCategory(breakerMaterialCategory);

			if (!sortedBreakerMap.containsKey(materialCategoryKey))
			{
				List< Tree<BusinessObjectDTO>> list = new ArrayList< Tree<BusinessObjectDTO>> ();
				list.add(salesItem);

				sortedBreakerMap.put(materialCategoryKey, list);
			}
			else
			{
				sortedBreakerMap.get(materialCategoryKey).add(salesItem);
			}
		}
		return sortedBreakerMap;
	}

	Closure <Tree<BusinessObjectDTO>> getNewCubicle = { Tree<BusinessObjectDTO> switchBoard, Tree<BusinessObjectDTO> breaker ->

		Tree<BusinessObjectDTO> newCubicle = null;
		String matCat = getDAValue(breaker.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_MATERIALCATEGORY);
		MaterialCategory breakerMatCat = getTypeOfMaterialCategory(matCat);

		switch (breakerMatCat)
		{
		case MaterialCategory.ACB:
			newCubicle = new Tree<BusinessObjectDTO>();
			newCubicle.setBODTOValue(new BusinessObjectDTO());
			newCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
			newCubicle.retrieveBODTOValue().objects = new HashMap<>();
			newCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
			newCubicle.retrieveBODTOValue().objects.put("quantity", "1");
			newCubicle.retrieveBODTOValue().objects.put("erpId", ApplicationConstants.cubicleERPID.get('ACB'));
			newCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")));
			break;

		case MaterialCategory.MCCB:

			BigDecimal breakerRatedCurrent =  new BigDecimal(0);
			BigDecimal SBRatedCurrentMainBusBar = new BigDecimal(0);
			String SBForm = "";

			Object breakerRatedCurrentObject = getDAValue(breaker.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_RATEDCURRENT);

			Object SBRatedCurrentMainBusBarObject =  getDAValue(switchBoard.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.SB_DA_RATEDCURRENTMAINBUSBAR);

			Object SBFormObject = getDAValue(switchBoard.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.SB_DA_FORM);

			try
			{
				if(!NullUtil.isNullorEmpty(SBFormObject))
					SBForm = String.valueOf(SBFormObject);
				else
					SBForm=0;

				if(!NullUtil.isNullorEmpty(breakerRatedCurrentObject))
					breakerRatedCurrent = new BigDecimal(breakerRatedCurrentObject);
				else
					breakerRatedCurrent = 0;

				if(!NullUtil.isNullorEmpty(SBRatedCurrentMainBusBarObject))
					SBRatedCurrentMainBusBar = new BigDecimal(SBRatedCurrentMainBusBarObject)
				else
					SBRatedCurrentMainBusBar = 0;
			}
			catch (final NumberFormatException ex)
			{
				println ex
			}

			if ((800 < breakerRatedCurrent && breakerRatedCurrent <= 1600) && SBRatedCurrentMainBusBar <= 4000)
			{
				newCubicle = new Tree<BusinessObjectDTO>();
				newCubicle.setBODTOValue(new BusinessObjectDTO());
				newCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
				newCubicle.retrieveBODTOValue().objects = new HashMap<>();
				newCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
				newCubicle.retrieveBODTOValue().objects.put("quantity", "1");
				newCubicle.retrieveBODTOValue().objects.put("erpId",  ApplicationConstants.cubicleERPID.get('LV-CUBICLE-2-VERTICAL'));
				newCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")))
			}
			else if ((800 < breakerRatedCurrent && breakerRatedCurrent <= 1600) && SBRatedCurrentMainBusBar <= 4000)
			{
				newCubicle = new Tree<BusinessObjectDTO>();
				newCubicle.setBODTOValue(new BusinessObjectDTO());
				newCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
				newCubicle.retrieveBODTOValue().objects = new HashMap<>();
				newCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
				newCubicle.retrieveBODTOValue().objects.put("quantity", "1");
				newCubicle.retrieveBODTOValue().objects.put("erpId",  ApplicationConstants.cubicleERPID.get('LV-CUBICLE-VERTICAL'));
				newCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")))
			}
			else if (breakerRatedCurrent <= 630 && SBRatedCurrentMainBusBar <= 4000 && (SBForm.contains('SB_FORM_3') || SBForm.contains('SB_FORM_4')))
			{
				newCubicle = new Tree<BusinessObjectDTO>();
				newCubicle.setBODTOValue(new BusinessObjectDTO());
				newCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
				newCubicle.retrieveBODTOValue().objects = new HashMap<>();
				newCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
				newCubicle.retrieveBODTOValue().objects.put("quantity", "1");
				newCubicle.retrieveBODTOValue().objects.put("max", "1");
				newCubicle.retrieveBODTOValue().objects.put("erpId",  ApplicationConstants.cubicleERPID.get('LV-CUBICLE-HORIZONTAL'));
				newCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")))
			}
			else
			{
				newCubicle = new Tree<BusinessObjectDTO>();
				newCubicle.setBODTOValue(new BusinessObjectDTO());
				newCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
				newCubicle.retrieveBODTOValue().objects = new HashMap<>();
				newCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
				newCubicle.retrieveBODTOValue().objects.put("quantity", "1");
				newCubicle.retrieveBODTOValue().objects.put("erpId",  ApplicationConstants.cubicleERPID.get('LV-CUBICLE-HORIZONTAL'));
				newCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")))
			}
		break;

		default:
			newCubicle = null;
		}

		return newCubicle;
	}


	Closure <Map<String, Tree<BusinessObjectDTO>> > allocateBreakers = { Tree<BusinessObjectDTO> switchBoard, List<Tree<BusinessObjectDTO>> breakers ->
		Map<String,Tree<BusinessObjectDTO>> result = Maps.newHashMap();

		List<Tree<BusinessObjectDTO>> cubicles = Lists.newArrayList();

		Tree<BusinessObjectDTO> lastCubicle = null;

		Tree<BusinessObjectDTO>	genericCubicle = new Tree<BusinessObjectDTO>();
		genericCubicle.setBODTOValue(new BusinessObjectDTO());
		genericCubicle.retrieveBODTOValue().attributes = Sets.newHashSet();
		genericCubicle.retrieveBODTOValue().objects = new HashMap<>();
		genericCubicle.retrieveBODTOValue().objects.put("flag", "NEW");
		genericCubicle.retrieveBODTOValue().objects.put("quantity", "1");
		genericCubicle.retrieveBODTOValue().objects.put("erpId", ApplicationConstants.cubicleERPID.get('GENERIC'));
		genericCubicle.retrieveBODTOValue().objects.put("hasProductType", new ArrayList<String>(Arrays.asList("Cubicle")));
		genericCubicle.children = Lists.newArrayList();

		int i = 0;
		for (Iterator<Tree<BusinessObjectDTO>> breaker = breakers.iterator(); breaker.hasNext();)
		{
			Tree<BusinessObjectDTO> tempBreaker = breaker.next();

			if (i == 0)
			{
				lastCubicle = getNewCubicle(switchBoard, tempBreaker);

				if (!NullUtil.isNullorEmpty(lastCubicle)) {
					if(lastCubicle.children == null)
						lastCubicle.children = Lists.newArrayList();
					lastCubicle.children.add(tempBreaker);
				}
				else {
					if(genericCubicle.children == null)
						genericCubicle.children = Lists.newArrayList();
					genericCubicle.children.add(tempBreaker);
				}
			}
			else
			{
				if (NullUtil.isNullorEmpty(lastCubicle))
				{
					Tree<BusinessObjectDTO> cubicle = getNewCubicle(switchBoard, tempBreaker);
					if (!NullUtil.isNullorEmpty(cubicle))
					{
						if(cubicle.children == null)
							cubicle.children = Lists.newArrayList();
						cubicle.children.add(tempBreaker);
						lastCubicle = cubicle;
					}
					else
					{
						if(genericCubicle.children == null)
							genericCubicle.children = Lists.newArrayList();
						genericCubicle.children.add(tempBreaker);
					}
				}
				else
				{
					if(lastCubicle.children == null)
						lastCubicle.children = Lists.newArrayList();
					lastCubicle.children.add(tempBreaker);
					boolean flag = CubicleValidation.checkCubicle(lastCubicle);
					
					if(!flag) {
						lastCubicle.children.remove(tempBreaker);
						cubicles.add(lastCubicle);
						
						Tree<BusinessObjectDTO> cubicle = getNewCubicle(switchBoard, tempBreaker);
						if (!NullUtil.isNullorEmpty(cubicle))
						{
							if(cubicle.children == null)
								cubicle.children = Lists.newArrayList();
							cubicle.children.add(tempBreaker);
							lastCubicle = cubicle;
						}
						else
						{
							if(genericCubicle.children == null)
								genericCubicle.children = Lists.newArrayList();
							genericCubicle.children.add(tempBreaker);
						}
						
						lastCubicle = cubicle;
						cubicles.add(lastCubicle);
					}
					
//					boolean flag = true;
//					for (AbstractRule rule : lastCubicle.getRules())
//					{
//						rule.setCubicle(lastCubicle);
//						if (!rule.validate())
//						{
//							flag = false;
//							break;
//						}
//					}
//					if(!flag) {
//						lastCubicle.unassignBreaker(tempBreaker);
//						cubicles.add(lastCubicle);
//
//						Cubicle cubicle = getNewCubicle(switchBoard, tempBreaker);
//						cubicle.assignBreaker(tempBreaker);
//						lastCubicle = cubicle;
//						cubicles.add(lastCubicle);
//					}
				}

			}

			if (i == (breakers.size() - 1) && lastCubicle!=null && !cubicles.contains(lastCubicle))
			{
				cubicles.add(lastCubicle);
			}

			i++;
		}

		if (genericCubicle.children.size() >0)
		{
			cubicles.add(genericCubicle);
		}

		return cubicles;

	}


	public class CubicleValidation{
		static final int CUBICLE_DEFAULT_MODULARITY = 34;
		
		public static boolean checkCubicle(Tree<BusinessObjectDTO> cubicle) {
			boolean flag = true;
			
			flag = checkCubicleMaxModuleSize(cubicle);
	
			return flag;
		}
		
		private static boolean checkCubicleMaxModuleSize(Tree<BusinessObjectDTO> cubicle)
		{
		int totalModule = 0;
		
		List<Tree<BusinessObjectDTO>> breakers = cubicle.children;
		

		for (Tree<BusinessObjectDTO> breaker : breakers)
		{
			int moduleSize = 0;
			String matCat = "";
			
			Map<String,Object> configItem = breaker.retrieveBODTOValue().objects.get("configItemKV");
			if(configItem==null) {
				return false;
			}
	
			Object result = configItem.get(ApplicationConstants.BREAKER_DA_MATERIALCATEGORY);
	
			if(result == null) {
				return false;
			}
			else if (result instanceof List<?>)
			{
				List<String> values = (List<String>) result;
				if (!values.isEmpty())
				{
					matCat = String.valueOf(values.get(0));
					if (StringUtils.isBlank(matCat) || matCat.equalsIgnoreCase("null"))
						matCat = "";
				}
			}
			else
			{
				matCat = String.valueOf(result);
				if (StringUtils.isBlank(matCat) || matCat.equalsIgnoreCase("null"))
					matCat = "";
			}
			
			switch(matCat) {
				case "ACB": moduleSize = 34; break;
				case "MCCB" : moduleSize = 3; break;
				default : moduleSize = 99;
			}
			totalModule += moduleSize;
		}

		if (totalModule <= CUBICLE_DEFAULT_MODULARITY)
			return true;
		else
			return false;
		}

	}


	//-------------------------------------------------------------------------------------



	// First level always is the quote boDTO
	List<Tree<BusinessObjectDTO>> firstLevelSalesItemList = salesItemsTree.children;
	List<Tree<BusinessObjectDTO>> rootNewChild = new ArrayList<Tree<BusinessObjectDTO>>();

	for (Tree<BusinessObjectDTO> firstLevelSalesItem : firstLevelSalesItemList)
	{
		Collection<Tree<BusinessObjectDTO>> tempList = new ArrayList<Tree<BusinessObjectDTO>>();
		Collection<Tree<BusinessObjectDTO>> toRemoveList = new ArrayList<Tree<BusinessObjectDTO>>();
		Collection<Tree<BusinessObjectDTO>> toAddList = new ArrayList<Tree<BusinessObjectDTO>>();

		firstLevelSalesItem = convertToMutableList(firstLevelSalesItem);
		BusinessObjectDTO firstLevelBODTO = firstLevelSalesItem.retrieveBODTOValue();

		List<Object> hasProductType = BODTOUtil.getProductType(firstLevelBODTO);
		Map<String, Object> configItemKV = BODTOUtil.getConfigItems(firstLevelBODTO);

		if (NullUtil.isNullorEmpty(firstLevelSalesItem.retrieveBODTOValue().objects))
		{
			firstLevelSalesItem.retrieveBODTOValue().objects = Maps.newHashMap();
		}
		firstLevelSalesItem.retrieveBODTOValue().objects.put("hasProductType", hasProductType);
		firstLevelSalesItem.retrieveBODTOValue().objects.put("configItemKV", configItemKV);
		firstLevelSalesItem.retrieveBODTOValue().objects.put("flag", "OLD");

		if (hasProductType.contains("SwitchBoard"))
		{
			// get list of breakers
			List<Tree<BusinessObjectDTO>> subSalesItems = firstLevelSalesItem.children;
			List<Tree<BusinessObjectDTO>> subSalesItemsNew = new ArrayList<Tree<BusinessObjectDTO>>();

			for(Tree<BusinessObjectDTO> subSalesItem : subSalesItems) {

				List<Object> hasProductTypeBreaker = BODTOUtil.getProductType(subSalesItem.retrieveBODTOValue());
				Map<String, Object> configItemKVBreaker = BODTOUtil.getConfigItems(subSalesItem.retrieveBODTOValue());

				if (NullUtil.isNullorEmpty(subSalesItem.retrieveBODTOValue().objects))
				{
					subSalesItem.retrieveBODTOValue().objects = Maps.newHashMap();
				}

				subSalesItem.retrieveBODTOValue().objects.put("hasProductType", hasProductTypeBreaker);
				subSalesItem.retrieveBODTOValue().objects.put("configItemKV", configItemKVBreaker);
				subSalesItem.retrieveBODTOValue().objects.put("flag", "OLD");
				subSalesItemsNew.add(subSalesItem);
			}

			Map<String, List<Tree<BusinessObjectDTO>>> categorizedBreakers = categoriseBreaker(subSalesItemsNew);

			for (Map.Entry<MaterialCategory, List<Tree<BusinessObjectDTO>>> entry : categorizedBreakers.entrySet())
			{
				MaterialCategory materialCategory = entry.getKey();

				LOGGER.debug( 'processing breaker type:' + materialCategory );

				List<Tree<BusinessObjectDTO>> breakers = entry.getValue();

				def sortedBreaker = breakers.toSorted { x, y ->  getDAValue(y.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_RATEDCURRENT) <=>  getDAValue(x.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_RATEDCURRENT) }

				if(LOGGER.isDebugEnabled()) {
					for(Tree<BusinessObjectDTO> breaker : sortedBreaker) {
						def ratedCurrent = getDAValue(breaker.retrieveBODTOValue().objects.get("configItemKV"), ApplicationConstants.BREAKER_DA_RATEDCURRENT);
						LOGGER.debug('breaker rated Current:  ' + ratedCurrent);
					}
				}

				List<Tree<BusinessObjectDTO>> cubicles = allocateBreakers(firstLevelSalesItem, sortedBreaker);

				toAddList.addAll(cubicles);
				toRemoveList.addAll(breakers);


			}

			for (Tree<BusinessObjectDTO> treeObj : firstLevelSalesItem.children)
			{
				if (!toRemoveList.contains(treeObj))
				{
					tempList.add(treeObj);
				}
			}
			List newList =  new ArrayList<>();
			newList.addAll(toAddList);
			newList.addAll(tempList);
			firstLevelSalesItem.children = newList;

			rootNewChild.add(firstLevelSalesItem);
		}
		else
		{
			rootNewChild.add(firstLevelSalesItem);
		}

	}
	salesItemsTree.children = new ArrayList<Tree<BusinessObjectDTO>>(rootNewChild);

	Tree<BusinessObjectDTO>  result =  salesItemsTree;
	
	return result;


