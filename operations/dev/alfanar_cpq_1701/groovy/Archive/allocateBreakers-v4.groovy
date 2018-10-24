
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator
import java.util.List;
import java.util.Map;
import java.util.TreeMap


import java.math.BigDecimal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets
import com.imc.util.NullUtil;

import com.imc.iss.groovy.salesitem.SalesItemsTree;
import com.imc.iss.web.controller.QuoteController
import com.imc.iss.groovy.salesitem.SalesItemNode;
import static Constants.*;
import static CubicleValidation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.imc.util.NullUtil;
import static Utils.*;

	enum MaterialCategory {
		ACB(1), MCCB(2), ICCB(3), CBR(4)
		
		MaterialCategory(int priority) {
			this.priority = priority
		}
		
		private final int priority
		int getPriority() {
			priority
		}
	}
	
	/*
	 * Global constants for the groovy
	 *  @@@@@@@@@@@@@@@@@ Update the constants below based on your environment! @@@@@@@@@@@@@@@@@@@@@@
	 */
	class Constants {
		
		static final boolean ENABLE_SPLITBREAKER = true;
		static final boolean ENABLE_LOGGER = true;

		//list of ERPID to identify type of product
		static final def SWITCHBOARD_ERPIDS = ["LV-SB-MF"]
		static final def GROUPING_ERPIDS = ["LV-INCOMER", "LV-OUTGOING", "GP-001"]

		//breaker
		static final String BREAKER_DA_MATERIALCATEGORY = "MATERIAL_CATEGORY";
		static final String BREAKER_DA_RATEDCURRENT = "RATED_CURRENT_AF";
		static final String BREAKER_DA_NOOFPOLE = "NUMBER_OF_POLE";
		static final String BREAKER_DA_CAPACITYICU = "RATED_ULT_BREAKING_CAPACIT_ICU"
	
		static final def MATERIAL_CATEGORY_VALUES = [MaterialCategory.ACB.toString(), MaterialCategory.MCCB.toString(), MaterialCategory.ICCB.toString(), MaterialCategory.CBR.toString()]
		
		static final int DEFAULT_BREAKER_MODULARITY = 3;
		
		//cubicle
		static final String GENERIC_CUBICLE_ERP_ID = "1-LV-CUBICLE-GENERIC1"
		static final String ACB_CUBICLE_ERP_ID = "C-ACB-0001"
		static final String MCCB_HORIZONTAL_CUBICLE_ERP_ID = "LV-CUBICLE-HORIZONTAL"
		static final String MCCB_MA_CUBICLE_ERP_ID = "LV-CUBICLE-MA"
		static final String MCCB_VERTICAL_CUBICLE_ERP_ID = "LV-CUBICLE-VERTICAL"
		static final String MCCB_2_VERTICAL_CUBICLE_ERP_ID = "LV-CUBICLE-2-VERTICAL"
		static final String ICCB_CUBICLE_ERP_ID = "ICCB-001"
		
		static final String CUBICLE_DA_WIDTH = "LV-CUBICLE_WIDTH"
		static final String CUBICLE_DA_WIDTH_Value_400mm = "400mm"
		static final String CUBICLE_DA_WIDTH_Value_600mm = "600mm"
		static final String CUBICLE_DA_WIDTH_Value_800mm = "800mm"
		static final String CUBICLE_DA_WIDTH_Value_1000mm = "1000mm"
		static final String CUBICLE_DA_WIDTH_Value_1200mm = "1200mm"
		
		//SwitchBoard
		static final String SB_DA_PREFERRED_ARRANGEMENT = "Preferred_Arrangement_U"
		static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_MA = "MA"
		static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_HOR = "HOR"
		static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_VER = "VER"


		static final String SB_DA_RATEDCURRENTMAINBUSBAR = "LV_CUBICLE_HBB_I_YD";
		
		static final String SB_DA_FORM = "LV-CUBICLE_FORM";
		static final String SB_DA_FORM_VALUE_1 = "CUBICLE_FORM1"
		static final String SB_DA_FORM_VALUE_2 = "CUBICLE_FORM2"
		static final String SB_DA_FORM_VALUE_3 = "CUBICLE_FORM3"
		static final String SB_DA_FORM_VALUE_4 = "CUBICLE_FORM4"
	}
	
	class Utils{
		public static BigDecimal convertToBigDecimal(String field, def value){
			if(value != null) {			
				try {
					value = new BigDecimal(value);
				}
				catch(Exception e){
				   throw new Exception("convertToBigDecimal --> Casting Error on " + field + ": " + value + '. ' + e.getMessage());
				}
			}
			return value;
		}
	}
	
	
	class CubicleValidation{		
		
		public static boolean checkCubicle(SalesItemNode cubicle) {
			boolean flag = true;
			
			if(!checkCubicleMaxModuleSize(cubicle)) {
				println '==> Cubicle Max Module Size reached!'
				return false;
			}
			
//			if(!checkCubicleRuleTotalAmperage(cubicle)) {
//				println 'Cubicle Total Amperage reached'
//				return false;
//			}
			
			if(!checkTotalAmperageWithDiversityFactor(cubicle)) {
				println '==> Total Amperage with Diversity Factor reached!'
				return false;
			}
			
			if(!checkBreakerRatedCurrent(cubicle)) {
				println '==> Breaker\'s rated current outside the range of rated current supported by the cubicle!'
				return false;
			}
			
			if(!checkNumberOfBreakersAllow(cubicle)) {
				println '==> Number of breakers exceeded allowed size!'
				return false;
			}
			
			return flag;
		}
		
		private static SalesItemNode getSwitchBoard(SalesItemNode salesItemNode) {
			SalesItemNode parentNode = salesItemNode.getParent();
			def productERPID = parentNode.getProductErpId();
			
			if(SWITCHBOARD_ERPIDS.contains(productERPID))
				return parentNode;
			else if(StringUtils.isBlank(productERPID)) 
				return null;
			else
				getSwitchBoard(parentNode);		
		}
		
		private static boolean checkTotalAmperageWithDiversityFactor(SalesItemNode cubicle)
		{
			double totalBreakerAmp = 0;
			final int totalBreaker = 0;
			double specialLimit = 0;
			boolean isSpecial = false;
			
			def cubicleERPId = cubicle.getProductErpId();
			
			List<SalesItemNode> breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			
			if(breakers == null) {
				println 'No breaker found in the cubicle'
			}else
				totalBreaker = breakers.size();
			
			if(totalBreaker < 2) {
				return true;
			}
			
			SalesItemNode switchBoard = getSwitchBoard(cubicle);			
			if(switchBoard == null) {
				throw new Exception("checkTotalAmperageWithDiversityFactor --> No switchboard found.");
			}
			
			def ratedCurrentMainBusBar = Utils.convertToBigDecimal(SB_DA_RATEDCURRENTMAINBUSBAR, switchBoard.getDynamicAttribute(SB_DA_RATEDCURRENTMAINBUSBAR));
			if(ratedCurrentMainBusBar == null) {
				throw new Exception("checkTotalAmperageWithDiversityFactor --> Invalid value ratedCurrentMainBusBar: " + ratedCurrentMainBusBar);
			}
			
			double diversityFactor = 1;
			if(totalBreaker == 2 || totalBreaker == 3) {
				diversityFactor = 0.9
			}else if(totalBreaker == 4 || totalBreaker == 5) {
				diversityFactor = 0.8
			}else if( 6 <= totalBreaker && totalBreaker <= 9 ) {
				diversityFactor = 0.7
			}else if( totalBreaker >= 10 ) {
				diversityFactor = 0.6
			}
						
			//ratedCurrentMainBusBar = ratedCurrentMainBusBar * diversityFactor;			
			
			for (SalesItemNode breaker : cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES ))
			{
				BigDecimal ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
				if(ratedCurrent==null) {
					throw new Exception("CheckCubicleRuleTotalAmperate --> rated Current of breaker should not be null");
				}
				totalBreakerAmp += ratedCurrent;
			}
			totalBreakerAmp = totalBreakerAmp * diversityFactor;
				
			switch(cubicleERPId) {
				case MCCB_MA_CUBICLE_ERP_ID:
					isSpecial = true;
					specialLimit = 1600;
				break;
				
				case MCCB_HORIZONTAL_CUBICLE_ERP_ID:
					isSpecial = true;
					specialLimit = 2500;
				break;
					
				default:
					isSpecial = false;
			}
			
			if(ENABLE_LOGGER) {
				println '@@@ checkTotalAmperageWithDiversityFactor @@@'
				print 'Sum of Breakers Amperage (with Diversity factor ' + diversityFactor + '): ' + totalBreakerAmp + ' vs Rated Current Main BusBar: ' + ratedCurrentMainBusBar;
				if(isSpecial)
					print ' or Special Rated Current Main BusBar limit: ' + specialLimit;
				println '\n';
			}
			
			if(isSpecial) {
				if (totalBreakerAmp <= ratedCurrentMainBusBar && (totalBreakerAmp <= specialLimit))
					return true;
				else
					return false;
			}else {
				if (totalBreakerAmp <= ratedCurrentMainBusBar)
					return true;
				else
					return false;
			}			
		}
		
		
		private static boolean checkCubicleRuleTotalAmperage(SalesItemNode cubicle)
		{
			double totalBreakerAmp = 0;

			SalesItemNode switchBoard = getSwitchBoard(cubicle);
			if(switchBoard == null) {
				throw new Exception("checkCubicleRuleTotalAmperage --> No switchboard found.");
			}
				
			def ratedCurrentMainBusBar = Utils.convertToBigDecimal(SB_DA_RATEDCURRENTMAINBUSBAR, switchBoard.getDynamicAttribute(SB_DA_RATEDCURRENTMAINBUSBAR));			
			if(ratedCurrentMainBusBar == null) {
				throw new Exception("CheckCubicleRuleTotalAmperate --> Invalid value ratedCurrentMainBusBar: " + ratedCurrentMainBusBar);
			}
			
			for (SalesItemNode breaker : cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES ))
			{
				def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
				if(ratedCurrent==null) {
					throw new Exception("CheckCubicleRuleTotalAmperate --> Unable to get the rated Current of breaker ");
				}
				totalBreakerAmp += ratedCurrent;
			}
			if(ENABLE_LOGGER) {
				println '\n@@@ checkCubicleRuleTotalAmperage @@@'
				println 'Sum of Breakers Amperage: ' + totalBreakerAmp + ' vs Rated Current Main BusBar: ' + ratedCurrentMainBusBar + '\n';
			}
			if (totalBreakerAmp <= ratedCurrentMainBusBar)
				return true;
			else
				return false;
		}
		
		private static boolean checkBreakerRatedCurrent(SalesItemNode cubicle)
		{
			int lowerRange =0;
			int upperRange = 0;
			def allowedNoOfPole=[];
			
			String cubicleERPID = cubicle.getProductErpId();
			
			switch(cubicleERPID) {
				case MCCB_VERTICAL_CUBICLE_ERP_ID: 
				lowerRange = 250; upperRange=1600;
				allowedNoOfPole = [3,4]
				break;
				
				case MCCB_2_VERTICAL_CUBICLE_ERP_ID:
				lowerRange = 800; upperRange=1600;
				allowedNoOfPole = [3,4]
				break;
				
				case MCCB_HORIZONTAL_CUBICLE_ERP_ID:
				lowerRange = 0; upperRange=630;
				allowedNoOfPole = [3,4]
				break;
				
				case MCCB_MA_CUBICLE_ERP_ID:
				lowerRange = 125; upperRange=250;
				allowedNoOfPole = [3];
				break;
							
				default : 
				lowerRange = 0; upperRange=9999;
				allowedNoOfPole = [3,4]
			}
			
			List<SalesItemNode> breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			for (SalesItemNode breaker : breakers) {
				def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))				
				if(breakerRatedCurrent == null) {
					breakerRatedCurrent = 0
				}
				def breakerNoOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE))
				if(breakerNoOfPole == null) {
					breakerNoOfPole = 0
				}
				
				if(!(lowerRange <= breakerRatedCurrent && breakerRatedCurrent<= upperRange) && !(allowedNoOfPole.contains(breakerNoOfPole))) {
					println '\n@@@ checkBreakerRatedCurrent - Failed @@@';
					println 'lowerRange: ' + lowerRange + '\t upperRange: ' + upperRange + '\t breaker Rated Current: ' + breakerRatedCurrent + '\n';
					println 'allowedNoOfPole: ' + allowedNoOfPole + '\t breaker No Of Pole: ' + breakerNoOfPole + '\n';
					return false	
				}else {
					println '\n@@@ checkBreakerRatedCurrent - Passed @@@';
					println 'lowerRange: ' + lowerRange + '\t upperRange: ' + upperRange + '\t breaker Rated Current: ' + breakerRatedCurrent + '\n';
					println 'allowedNoOfPole: ' + allowedNoOfPole + '\t breaker No Of Pole: ' + breakerNoOfPole + '\n';
				}
			}
			return true		
		}
		
		private static boolean checkNumberOfBreakersAllow(SalesItemNode cubicle) {
			
			int numberOfBreakerAllow = 0;
			
			def cubicleERPID = cubicle.getProductErpId();
			List<SalesItemNode> breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			int numberOfBreaker = breakers.size();
			
			switch(cubicleERPID) {
			case ACB_CUBICLE_ERP_ID:
				numberOfBreakerAllow = 1;
				break;		
			case MCCB_VERTICAL_CUBICLE_ERP_ID: 
				numberOfBreakerAllow = 1; 
				break;
			case MCCB_2_VERTICAL_CUBICLE_ERP_ID: 
				numberOfBreakerAllow = 2; 
				break;
//			no max number of breaker is mentioned for horizontal
//				case MCCB_HORIZONTAL_CUBICLE_ERP_ID:	
//				numberOfBreakerAllow = 2;
//				break;
			case MCCB_MA_CUBICLE_ERP_ID:
				numberOfBreakerAllow = 24;
				break;
			default : numberOfBreakerAllow = 9999;
			}
			
			if(ENABLE_LOGGER) {
				println '@@@ checkNumberOfBreakersAllow @@@'
				println 'No of breakers in cubicle: ' + numberOfBreaker + ' vs number of breaker allowed: ' + numberOfBreakerAllow + '\n';
			}
			
			if(numberOfBreaker <= numberOfBreakerAllow)
				return true;
			else
				return false;		
		}
		
		private static boolean checkCubicleMaxModuleSize(SalesItemNode cubicle)
		{
			int totalModule = 0;
			int cubicleModularity = 34;
			
			def cubicleERPID = cubicle.getProductErpId();
					
			switch(cubicleERPID) {
				//case MCCB_VERTICAL_CUBICLE_ERP_ID: cubicleModularity = 3; break;
				//case MCCB_2_VERTICAL_CUBICLE_ERP_ID: cubicleModularity = 6; break;
				case MCCB_MA_CUBICLE_ERP_ID: cubicleModularity = 72; break;
				default : cubicleModularity = 34;
			}
			
			List<SalesItemNode> breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			
			for (SalesItemNode breaker : breakers)
			{
				int breakerModularity = 0;
				String matCat = breaker.getDynamicAttribute(BREAKER_DA_MATERIALCATEGORY);
				def nPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE));
				def current = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));

				switch(matCat) {
					case MaterialCategory.ACB.toString(): 
						breakerModularity = 34; 
						break;
					
					case MaterialCategory.MCCB.toString() : 
						if(nPole == 3) {
							if(current <= 250) 
								breakerModularity = 3;
							else if(current <= 630) 
								breakerModularity = 4;
							else
								breakerModularity = 4;
								
						}else if(nPole == 4) {
							if(current <= 250) 
								breakerModularity = 4;
							else if(current <= 630) 
								breakerModularity = 5;
							else
								breakerModularity = 5;
						}
					break;
					default : breakerModularity = DEFAULT_BREAKER_MODULARITY;
				}
				totalModule += breakerModularity;
			}
	
			if(ENABLE_LOGGER) {
				println '\n@@@ checkCubicleMaxModuleSize @@@'
				println 'Sum of breakers modules: ' + totalModule + ' vs Cubicle modularity: ' + cubicleModularity + '\n';
			}
			if (totalModule <= cubicleModularity)
				return true;
			else
				return false;
		}
	}
	
	
	Closure< Map<MaterialCategory, List<SalesItemNode>>> categoriseBreaker = { List<SalesItemNode> salesItems->
		
		Map<MaterialCategory, List<SalesItemNode> > breakerMap = new HashMap<MaterialCategory, List<SalesItemNode>>();
		MaterialCategory matCat;
		
		for (SalesItemNode salesItem : salesItems)
		{
			final String breakerMaterialCategory = salesItem.getDynamicAttribute(BREAKER_DA_MATERIALCATEGORY)
			
			switch(breakerMaterialCategory) {
				case 'ACB': matCat = MaterialCategory.ACB; 
				break;
				case 'MCCB': matCat = MaterialCategory.MCCB;
				break;
				case 'ICCB': matCat = MaterialCategory.ICCB;
				break;
				case 'CBR': matCat = MaterialCategory.CBR;
				break;
			}
		
			if (!breakerMap.containsKey(matCat))
			{
				List<SalesItemNode> list = new ArrayList<SalesItemNode> ();
				list.add(salesItem);
		
				breakerMap.put(matCat, list);
			}
			else
			{
				breakerMap.get(matCat).add(salesItem);			
			}
		}
		
		Map sortedBreakerMap = breakerMap.sort { a, b -> a.key.getPriority() <=> b.key.getPriority()}

		return sortedBreakerMap;
	}
	
	
	Closure <SalesItemNode> getCubicleByERPID = { SalesItemNode breaker, SalesItemNode switchboard, SalesItemNode parent, int balanceOfBreaker ->
		SalesItemNode newCubicle = null;
		
		if(breaker ==null || switchboard ==null) {
			throw new Exception("getCubicleERPID --> breaker or switchboard cannot be null. ");
		}
		
		def materialCategory = breaker.getDynamicAttribute(BREAKER_DA_MATERIALCATEGORY)
		if(materialCategory == null) {
			throw new Exception("getCubicleERPID --> breaker's " + materialCategory + " cannot be null. ");
		}
		
		def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))	
		if(breakerRatedCurrent == null) {
			throw new Exception("getCubicleERPID --> breaker's " + BREAKER_DA_RATEDCURRENT + " cannot be null. ");
		}
		
		def breakerNoOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE))
		if(breakerNoOfPole == null) {
			throw new Exception("getCubicleERPID --> breaker's " + BREAKER_DA_NOOFPOLE + " cannot be null. ");
		}
		
		def breakerCapacityICU = Utils.convertToBigDecimal(BREAKER_DA_CAPACITYICU, breaker.getDynamicAttribute(BREAKER_DA_CAPACITYICU))
		if(breakerCapacityICU == null) {
			breakerCapacityICU = 0
		}
		
		def SBRatedCurrentMainBusBar = Utils.convertToBigDecimal(SB_DA_RATEDCURRENTMAINBUSBAR, switchboard.getDynamicAttribute(SB_DA_RATEDCURRENTMAINBUSBAR))
		if(SBRatedCurrentMainBusBar==null) {
			SBRatedCurrentMainBusBar = 0
		}
			
		def SBForm = switchboard.getDynamicAttribute(SB_DA_FORM)
		if(SBForm == null) {
			SBForm = ''
		}
		
		switch (materialCategory)
		{
			case MaterialCategory.ACB.toString():
				newCubicle = parent.addChildByProductErpId(ACB_CUBICLE_ERP_ID);
				
				if (breakerRatedCurrent <= 2000 && breakerCapacityICU <= 65) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_600mm);
				}else if  (breakerRatedCurrent <= 2000 && breakerCapacityICU > 65 && breakerCapacityICU <= 80) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
				}else if  (breakerRatedCurrent >= 2500 && breakerRatedCurrent <= 3200 && breakerCapacityICU <= 85) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
				}else if  (breakerRatedCurrent == 4000 && breakerCapacityICU <= 100) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
				}else if  (breakerRatedCurrent == 4000 && breakerCapacityICU > 100) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_1200mm);
				}else if  (breakerRatedCurrent > 4000 && breakerRatedCurrent <= 6300) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_1200mm);
				}else {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
				}						
				break;
			
			case MaterialCategory.MCCB.toString():
				
				if ((800<= breakerRatedCurrent && breakerRatedCurrent <= 1600) && SBRatedCurrentMainBusBar <= 4000 && balanceOfBreaker == 2)
				{
					newCubicle = parent.addChildByProductErpId(MCCB_2_VERTICAL_CUBICLE_ERP_ID );
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, '1000');
				}
				else if ((250 <= breakerRatedCurrent && breakerRatedCurrent <= 1600) && SBRatedCurrentMainBusBar <= 4000 && balanceOfBreaker == 1)
				{
					newCubicle = parent.addChildByProductErpId(MCCB_VERTICAL_CUBICLE_ERP_ID);
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, '600');
				}
				else if (breakerRatedCurrent <= 250 && SBRatedCurrentMainBusBar <= 4000 && (SBForm.contains(SB_DA_FORM_VALUE_1) || SBForm.contains(SB_DA_FORM_VALUE_2)) && breakerNoOfPole == 3)
				{
					newCubicle = parent.addChildByProductErpId(MCCB_MA_CUBICLE_ERP_ID);
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, '800');
				}	
				else if (breakerRatedCurrent <= 630 && SBRatedCurrentMainBusBar <= 4000)
					{
						newCubicle = parent.addChildByProductErpId(MCCB_HORIZONTAL_CUBICLE_ERP_ID);
						newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, '1000');
					}
				else
				{
					newCubicle = parent.addChildByProductErpId(MCCB_2_VERTICAL_CUBICLE_ERP_ID);
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, '1000');
				}						
				break;
				
				case MaterialCategory.ICCB.toString():
					newCubicle = parent.addChildByProductErpId(ICCB_CUBICLE_ERP_ID);
				break;
			default:
			newCubicle = null;
		}
		if(ENABLE_LOGGER && newCubicle != null) {
			println '\nNew Cubicle: ' + newCubicle.getProductErpId() + ' with Width: ' + newCubicle.getDynamicAttribute(CUBICLE_DA_WIDTH) + '\n';
		}
		return newCubicle;
	}
	
	
	Closure <Map<String, SalesItemNode> > allocateBreakers = {SalesItemNode switchboard, SalesItemNode parent, List<SalesItemNode> breakers ->

		SalesItemNode lastCubicle = null;

		SalesItemNode genericCubicle = null;
		
		int balanceOfBreaker = breakers.size();
		
		for (int i = 0; i < breakers.size(); i++)
		{
			SalesItemNode tempBreaker = breakers.get(i)

			if (i == 0)
			{
				SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
				
				if(newCubicle == null) {
					genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
					tempBreaker.changeParent(genericCubicle);
				}else {
					tempBreaker.changeParent(newCubicle);
					lastCubicle = newCubicle;
				}	
			}
			else
			{
				if (NullUtil.isNullorEmpty(lastCubicle))
				{			
					SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
					
					if(newCubicle == null) {
						if(genericCubicle==null)
							genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
						tempBreaker.changeParent(genericCubicle);
					}else {
						tempBreaker.changeParent(newCubicle);
						lastCubicle = newCubicle;
					}
				}
				else
				{
					tempBreaker.changeParent(lastCubicle);
					println '=========  Cubicle Validation for breaker-' + (i+2) + ' ========='
					boolean flag = CubicleValidation.checkCubicle(lastCubicle);
					
					if(!flag) {
						
						//change mccb 2 verticle cubicle to verticle cubicle due to only one breaker is allocated.
						if(lastCubicle.getProductErpId().contains(MCCB_2_VERTICAL_CUBICLE_ERP_ID) && lastCubicle.getChildren().size()==2){
									
							SalesItemNode replacementCubicle = parent.addChildByProductErpId(MCCB_VERTICAL_CUBICLE_ERP_ID);
							List<SalesItemNode> tempbreakers = lastCubicle.getChildren();
							for(int j=0; j<tempbreakers.size(); j++) {
								tempbreakers.get(j).changeParent(replacementCubicle);
								j--;
							}
							SalesItemNode toDeleteCubicle = lastCubicle;
							lastCubicle = replacementCubicle;
							toDeleteCubicle.delete();
						}
						
						SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
						if(newCubicle == null) {
							if(genericCubicle==null)
								genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
							tempBreaker.changeParent(genericCubicle);
						}else {
							tempBreaker.changeParent(newCubicle);
							lastCubicle = newCubicle;
						}
					}
				}
			}
			balanceOfBreaker--;
		}
		return genericCubicle;
	}
	
	
	Closure splitItem = {SalesItemNode salesItemNode ->
		
		def quantity = salesItemNode.getQuantity();

		if(quantity > 1) {
			for(int i = 0; i <(quantity-1); i++ ) {
				salesItemNode.split(1);
			}
		}
		
	}
	
	
	Closure processBreakers = {SalesItemNode salesItemNode, SalesItemNode switchboard ->
		
		List<SalesItemNode> listOfBreaker = salesItemNode.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
		
		if(ENABLE_SPLITBREAKER) {
			for(int i =0; i < listOfBreaker.size() ; i++) {
				splitItem(listOfBreaker.get(i));
			}
			
			listOfBreaker = salesItemNode.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
		}
		
		Map<MaterialCategory, List<SalesItemNode>> categorizedBreakers = categoriseBreaker(listOfBreaker);
		
		SalesItemNode genericCubicle = null;
		
		for (Map.Entry<String, List<SalesItemNode>> entry : categorizedBreakers.entrySet())
		{
			String materialCategory = entry.getKey();

			if(ENABLE_LOGGER) {
				println 'processing breaker type:' + materialCategory ;
			}
			
			List<SalesItemNode> breakers = entry.getValue();

			def sortedBreaker = breakers.toSorted {x,y -> Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, y.getDynamicAttribute(BREAKER_DA_RATEDCURRENT)) <=> Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, x.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))}

			if(!ENABLE_LOGGER) {
				for(SalesItemNode breaker: sortedBreaker) {
					println breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT)
				}
			}
			SalesItemNode tempGenericCubicle = allocateBreakers(switchboard, salesItemNode, sortedBreaker);
			
			if (tempGenericCubicle !=null) {
				
				if(genericCubicle ==null) {
					genericCubicle = salesItemNode.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
				}

				List<SalesItemNode> genericBreakers = tempGenericCubicle.getChildren();
				for(int i=0; i < genericBreakers.size(); i++) {
					genericBreakers.get(i).changeParent(genericCubicle);
					i--;
				}
				tempGenericCubicle.delete();
			}
				
		}
		
		return null;
	}
	
	//----------------------------------Main Method ----------------------------------------

	
	
	Closure <SalesItemsTree> execute = { ->

		for(SalesItemNode switchboard : salesItemsTree.filterChildrenByProductERPID(SWITCHBOARD_ERPIDS))
		{
			List<SalesItemNode> groupings = switchboard.filterChildrenByProductERPID(GROUPING_ERPIDS);
			List<SalesItemNode> breakers = switchboard.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			
			if(!groupings.isEmpty()) {
				for(SalesItemNode grouping : groupings)
				{
					processBreakers(grouping, switchboard);
				}
			}
			
			if(!breakers.isEmpty()) {
				processBreakers(switchboard, switchboard);			
			}
		}

	return salesItemsTree;
	}
	
	SalesItemsTree result = execute();
	return result;



