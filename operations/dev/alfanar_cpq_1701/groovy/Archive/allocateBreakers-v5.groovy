
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
import com.imc.iss.web.notification.message.MessageDisplayType
import com.imc.iss.web.notification.message.MessageFactory
import com.imc.iss.groovy.salesitem.SalesItemNode;
import static Constants.*;
import static CubicleValidation;
import static AssemblyValidaton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.imc.util.NullUtil;
import static Utils.*;
import static Logger.*;

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
		
		static final boolean ENABLE_LOGGER = true;
		static final boolean ENABLE_DIRECTPRINT = false;

		//list of ERPID to identify the type of product
		static final def SWITCHBOARD_ERPIDS = ["LV-SB-MF"]
		
		static final def INCOMING_ERPID = "LV-INCOMER"
		static final def OUTGOING_ERPID = "LV-OUTGOING"
		static final def GROUPING_ERPIDS = [INCOMING_ERPID, OUTGOING_ERPID]
		
		//MA-Assembly
		static final def MA_ASSEMBLY = "LV-MA-ASSEMBLY"
		static final int MAX_BREAKER_IN_ASSEMBLY = 4;

		//breaker 
		// the name of the Dynamic attributes using by breaker
		static final String BREAKER_DA_MATERIALCATEGORY = "MATERIAL_CATEGORY";
		static final String BREAKER_DA_RATEDCURRENT = "RATED_CURRENT_AF";
		static final String BREAKER_DA_NOOFPOLE = "NUMBER_OF_POLE";
		static final String BREAKER_DA_CAPACITYICU = "RATED_ULT_BREAKING_CAPACIT_ICU"

		static final def MATERIAL_CATEGORY_VALUES = [MaterialCategory.ACB.toString(), MaterialCategory.MCCB.toString(), MaterialCategory.ICCB.toString(), MaterialCategory.CBR.toString()]
		
		static final int DEFAULT_BREAKER_MODULARITY = 3;
		
		//cubicle
		// the ERPID of the cubicles
		static final String GENERIC_CUBICLE_ERP_ID = "LV-CUBICLE-GENERIC"
		static final String ACB_CUBICLE_ERP_ID = "LV-CUBICLE-ACB"
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
		
		static final String CUBUCLE_DA_HBB_I_YD = "LV_CUBICLE_HBB_I_YD"
		
		//to store the highest breaker's rated Current from incomer 
		static String highestRatedCurrentFromIncomer = null
		
		//Threshold of breakers allowed in each type of cubicle
		static final int NumberOfBreakerAllow_MA = 24
		static final int NumberOfBreakerAllow_ACB = 1
		static final int NumberOfBreakerAllow_Vertica = 1
		static final int NumberOfBreakerAllow_2_Vertica = 2
		
		static final int NumberOfAssemblyAllow_MA = 6
		
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
	
	class Logger{
		public static StringBuilder errorLog = new StringBuilder() ;
		public static StringBuilder infoLog = new StringBuilder() ;
		
		public static void addInfo(String msg) {
			infoLog.append(msg).append("\n");
			if(ENABLE_DIRECTPRINT)
				println(msg);
		}
		
		public static void addError(String msg) {
			errorLog.append(msg).append("\n");
			if(ENABLE_DIRECTPRINT)
				println(msg);
		}
		
		public static String getInfoLog() {
			return infoLog.toString();
		}
		
		public static String getErrorLog() {
			return errorLog.toString();
		}
		
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
	
	class AssemblyValidaton{
		public static boolean checkAssembly(SalesItemNode assembly) {
			boolean flag = true;
			
			if(!checkNumberOfBreakerInAssembly(assembly)) {
				Logger.addInfo("==> Max number of breaker in assembly reached!");
				return false;
			}
			
			return flag;
		}
		
		private static boolean checkNumberOfBreakerInAssembly(SalesItemNode assembly) {
			
			List<SalesItemNode> breakers = assembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			println "No of breaker in assembly: " + breakers.size()
			if(breakers.size() > MAX_BREAKER_IN_ASSEMBLY)
				return false;
			else
				return true;				
		}
		
	}
	
	class CubicleValidation{
		
		public static boolean checkCubicle(SalesItemNode cubicle) {
			boolean flag = true;
			
			if(!checkCubicleMaxModuleSize(cubicle)) {
				Logger.addInfo("==> Cubicle Max Module Size reached!");
				return false;
			}			
			
			if(!checkTotalAmperageWithDiversityFactor(cubicle)) {
				Logger.addInfo("==> Total Amperage with Diversity Factor reached!");
				return false;
			}
			
			if(!checkBreakerRatedCurrent(cubicle)) {
				Logger.addInfo("==> Breaker\'s rated current outside the range of rated current supported by the cubicle!");
				return false;
			}
			
			if(!checkNumberOfBreakersAllow(cubicle)) {
				Logger.addInfo("==> Number of breaker exceeded allowed size!");
				return false;
			}
			
			if(!checkNumberOfAssemblyAllowForMA(cubicle)) {
				Logger.addInfo("==> Number of assembly exceeded allowed size!");
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
			List<SalesItemNode> breakers = new ArrayList<SalesItemNode>();
			
			def cubicleERPId = cubicle.getProductErpId();
			
			switch(cubicleERPId) {
				case MCCB_MA_CUBICLE_ERP_ID:
					List<SalesItemNode> assemblies = cubicle.filterChildrenByProductERPID([MA_ASSEMBLY]);
				
					for(SalesItemNode assembly : assemblies) {
						List<SalesItemNode> breakersInAssembly = assembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
						breakers.addAll(breakersInAssembly);
					}
				break;
				
				default: 
				breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			}
		
			
			if(breakers != null)
				totalBreaker = breakers.size();
			
			if(totalBreaker < 2) {
				return true;
			}
			
			SalesItemNode switchBoard = getSwitchBoard(cubicle);
			if(switchBoard == null) {
				Logger.addError("checkTotalAmperageWithDiversityFactor --> No switchboard found.");
			}
			
			def ratedCurrentMainBusBar = Utils.convertToBigDecimal(SB_DA_RATEDCURRENTMAINBUSBAR, switchBoard.getDynamicAttribute(SB_DA_RATEDCURRENTMAINBUSBAR));
			if(ratedCurrentMainBusBar == null) {
				throw new Exception("checkTotalAmperageWithDiversityFactor --> Invalid value ratedCurrentMainBusBar: " + ratedCurrentMainBusBar);
			}
			
			def cubicleHBBRatedCurrent = Utils.convertToBigDecimal(CUBUCLE_DA_HBB_I_YD, cubicle.getDynamicAttribute(CUBUCLE_DA_HBB_I_YD));
			if(cubicleHBBRatedCurrent != null) {
				ratedCurrentMainBusBar = cubicleHBBRatedCurrent;
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
		
			for (SalesItemNode breaker : breakers)
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
				Logger.addInfo("@@@ checkTotalAmperageWithDiversityFactor @@@");
				Logger.addInfo("Sum of Breakers Amperage (with Diversity factor " + diversityFactor + "): " + totalBreakerAmp + " vs Rated Current Main BusBar: " + ratedCurrentMainBusBar);
				if(isSpecial)
					Logger.addInfo(" or Special Rated Current Main BusBar limit: " + specialLimit);
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
				
		private static boolean checkBreakerRatedCurrent(SalesItemNode cubicle)
		{
			int lowerRange =0;
			int upperRange = 0;
			def allowedNoOfPole=[];
			
			String cubicleERPID = cubicle.getProductErpId();
			
			List<SalesItemNode> breakers = new ArrayList<SalesItemNode>();
			
			switch(cubicleERPID) {
				case MCCB_VERTICAL_CUBICLE_ERP_ID:
					lowerRange = 125; upperRange=1600
					allowedNoOfPole = [3,4]
					breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES )
					break;
				
				case MCCB_2_VERTICAL_CUBICLE_ERP_ID:
					lowerRange = 800; upperRange=1600
					allowedNoOfPole = [3,4]
					breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES )
					break;
				
				case MCCB_HORIZONTAL_CUBICLE_ERP_ID:
					lowerRange = 100; upperRange=630;
					allowedNoOfPole = [3,4]
					breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
					break;
				
				case MCCB_MA_CUBICLE_ERP_ID:
					lowerRange = 100; upperRange=250;
					allowedNoOfPole = [3];
				
					List<SalesItemNode> assemblies = cubicle.filterChildrenByProductERPID([MA_ASSEMBLY]);
				
					for(SalesItemNode assembly : assemblies) {
						List<SalesItemNode> breakersInAssembly = assembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
						breakers.addAll(breakersInAssembly);
					}
					break;
							
				default :
					lowerRange = 0; upperRange=9999;
					allowedNoOfPole = [3,4]
					breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
			}		
			
			for (SalesItemNode breaker : breakers) {
				def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))
				if(breakerRatedCurrent == null) {
					breakerRatedCurrent = 0
				}
				BigDecimal breakerNoOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE))
				if(breakerNoOfPole == null) {
					breakerNoOfPole = 0
				}
				
				if((lowerRange <= breakerRatedCurrent && breakerRatedCurrent <= upperRange) && allowedNoOfPole.contains(breakerNoOfPole.intValue())) {
					Logger.addInfo("\n@@@ checkBreakerRatedCurrent - Passed @@@");
					Logger.addInfo("lowerRange: " + lowerRange + "\t upperRange: " + upperRange + "\t breaker Rated Current: " + breakerRatedCurrent + "\n");
					Logger.addInfo("allowedNoOfPole: " + allowedNoOfPole + "\t breaker No Of Pole: " + breakerNoOfPole + "\n");
					
				}else {
					Logger.addInfo("\n@@@ checkBreakerRatedCurrent - Failed @@@");
					Logger.addInfo("lowerRange: " + lowerRange + "\t upperRange: " + upperRange + "\t breaker Rated Current: " + breakerRatedCurrent + "\n");
					Logger.addInfo("allowedNoOfPole: " + allowedNoOfPole + "\t breaker No Of Pole: " + breakerNoOfPole + "\n");
					
					return false
				}
			}
			return true
		}
		
		/*
		 * checkNumberOfAssemblyAllowForMA only for MA-cubicle at the moment
		 */
		private static boolean checkNumberOfAssemblyAllowForMA(SalesItemNode cubicle) {
			int numberOfAssemblyAllow = 0;
			
			def cubicleERPID = cubicle.getProductErpId();
			
			switch(cubicleERPID) {
				case MCCB_MA_CUBICLE_ERP_ID:
					numberOfAssemblyAllow = NumberOfAssemblyAllow_MA;
					break;

				default : return true; //other cubicle return true 
			}
			
			List<SalesItemNode> assemblies = cubicle.filterChildrenByProductERPID([MA_ASSEMBLY]);
			
			if(ENABLE_LOGGER) {
				Logger.addInfo("@@@ NumberOfAssemblyAllow @@@");
				Logger.addInfo("No of Assembly in cubicle: " + assemblies.size() + " vs number of Assembly allowed: " + NumberOfAssemblyAllow_MA + "\n");
			}
			
			if(assemblies.size() <= NumberOfAssemblyAllow_MA)
				return true;
			else
				return false;
			
		}
		
		//only applicable for Cubicle Vertical, 2-Vertical, ACB
		private static boolean checkNumberOfBreakersAllow(SalesItemNode cubicle) {
			
			int numberOfBreakerAllow = 0;
			
			def cubicleERPID = cubicle.getProductErpId();
			switch(cubicleERPID) {
				case MCCB_HORIZONTAL_CUBICLE_ERP_ID:
					return true; // This rule does not apply for Horizontal Cubicle
					break;
				case MCCB_MA_CUBICLE_ERP_ID:
					numberOfBreakerAllow = NumberOfBreakerAllow_MA;
					break;
				case ACB_CUBICLE_ERP_ID:
					numberOfBreakerAllow = NumberOfBreakerAllow_ACB;
					break;
				case MCCB_VERTICAL_CUBICLE_ERP_ID:
					numberOfBreakerAllow = NumberOfBreakerAllow_Vertica;
					break;
				case MCCB_2_VERTICAL_CUBICLE_ERP_ID:
					numberOfBreakerAllow = NumberOfBreakerAllow_2_Vertica;
					break;
	
				default : numberOfBreakerAllow = 9999;
				}
			
			
			List<SalesItemNode> breakers ;
			int numberOfBreaker = 0;
			
			if(cubicleERPID == MCCB_MA_CUBICLE_ERP_ID) {
				List<SalesItemNode> assemblies = cubicle.filterChildrenByProductERPID([MA_ASSEMBLY]);
				
				for(SalesItemNode assembly : assemblies) {
					breakers = assembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
					numberOfBreaker = numberOfBreaker + breakers.size();
				}	
			}
			else {
				breakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
				numberOfBreaker = breakers.size();
			}
			
			if(ENABLE_LOGGER) {
				Logger.addInfo("@@@ checkNumberOfBreakersAllow @@@");
				Logger.addInfo("No of breakers in cubicle: " + numberOfBreaker + " vs number of breaker allowed: " + numberOfBreakerAllow + "\n");
			}
			
			if(numberOfBreaker <= numberOfBreakerAllow)
				return true;
			else
				return false;
		}
		
		//Only for Horizontal , the rest will be handle by CheckNumberOfBreakersAllow()
		private static boolean checkCubicleMaxModuleSize(SalesItemNode cubicle)
		{
			int totalModule = 0;
			int cubicleModularity = 34;
			
			def cubicleERPID = cubicle.getProductErpId();
					
			switch(cubicleERPID) {
				case ACB_CUBICLE_ERP_ID: return true;
				break;
				case MCCB_VERTICAL_CUBICLE_ERP_ID: return true;
				break;
				case MCCB_2_VERTICAL_CUBICLE_ERP_ID: return true; 
				break;
				case MCCB_MA_CUBICLE_ERP_ID: return true; 
				break;
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
				Logger.addInfo("\n@@@ checkCubicleMaxModuleSize @@@");
				Logger.addInfo("Sum of breakers modules: " + totalModule + " vs Cubicle modularity: " + cubicleModularity + "\n");
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
	
	// Closure to populate cubicle' dynamic attributes
	Closure populateCubicleAttributes = {SalesItemNode cubicle ->
		SalesItemNode parentNode = cubicle.getParent();
		def parentERPID = parentNode.getProductErpId();
		
		//set cubicle DA LV_CUBICLE_HBB_I_YD to the highestRatedCurrent from incomer
		if(parentERPID == OUTGOING_ERPID && highestRatedCurrentFromIncomer != null) {
			cubicle.setDynamicAttribute(CUBUCLE_DA_HBB_I_YD, highestRatedCurrentFromIncomer);
		}
		
	}
	
	Closure<SalesItemNode> assignBreakerToCubicle = { SalesItemNode newBreaker , SalesItemNode cubicle ->
		
		def newBreakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, newBreaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
		if(newBreakerRatedCurrent == null) {
			throw new Exception("assignBreakerToCubicle --> newBreakerRatedCurrent cannot be null. ");
		}
		
		String cubicleERPID = cubicle.getProductErpId();
		if(cubicleERPID == null) {
			throw new Exception("assignBreakerToCubicle --> cubicleERPID cannot be null. ");
		}
		switch(cubicleERPID) {
			case MCCB_MA_CUBICLE_ERP_ID:
			
			List<SalesItemNode> ma_assemblies = cubicle.filterChildrenByProductERPID( [MA_ASSEMBLY]);
			
			if(NullUtil.isNullorEmpty(ma_assemblies)) {
				//create new assembly if the MA-cubicle does not have assembly
				SalesItemNode ma_assembly = cubicle.addChildByProductErpId(MA_ASSEMBLY);
				newBreaker.changeParent(ma_assembly);
				
			}else {
				//find all assemblies which sloted breaker with the same rated current
				//if found, check if all fail the validation check
				List<SalesItemNode> sameRatedCurrentAssemblies = new ArrayList<SalesItemNode>();
				
				for(SalesItemNode ma_assembly : ma_assemblies) {
					List<SalesItemNode> breakers = ma_assembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
					if(!NullUtil.isNullorEmpty(breakers)) {
						def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breakers.get(0).getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
						if(breakerRatedCurrent == newBreakerRatedCurrent) {
							sameRatedCurrentAssemblies.add(ma_assembly);
						}
					}
				}
				
				//create a new MA-Assembly and assign breaker since no sameRatedCurrentAssemblies found
				if(NullUtil.isNullorEmpty(sameRatedCurrentAssemblies)) {
					SalesItemNode newMA_assembly = cubicle.addChildByProductErpId(MA_ASSEMBLY);
					newBreaker.changeParent(newMA_assembly);
				}else {
					int countFail = 0;
					int noOfSameRatedCurrentAssembly = sameRatedCurrentAssemblies.size();
					SalesItemNode ma_assemblyNotFull;
					
					for(SalesItemNode ma_assembly : sameRatedCurrentAssemblies) {
						boolean isValid = AssemblyValidaton.checkAssembly(ma_assembly);
						if(!isValid) {
							countFail++;
						}else {
							ma_assemblyNotFull = ma_assembly;
						}
					}
					//create new ma-assembly when all SameRatedCurrentAssembly are full
					if( countFail == noOfSameRatedCurrentAssembly) {
						SalesItemNode newMA_assembly = cubicle.addChildByProductErpId(MA_ASSEMBLY);
						newBreaker.changeParent(newMA_assembly);
					}else {
						newBreaker.changeParent(ma_assemblyNotFull);
						boolean isValid = AssemblyValidaton.checkAssembly(ma_assemblyNotFull);
						if(!isValid) {
							SalesItemNode newMA_assembly = cubicle.addChildByProductErpId(MA_ASSEMBLY);
							newBreaker.changeParent(newMA_assembly);
						}
					}
				}
			}
			
			break;
			
			default:
			newBreaker.changeParent(cubicle);			
		}
		
		return cubicle;
	}
	
	Closure <SalesItemNode> getCubicleByERPID = { SalesItemNode breaker, SalesItemNode switchboard, SalesItemNode parent, int balanceOfBreaker ->
		SalesItemNode newCubicle = null;
		
		if(breaker ==null || switchboard ==null) {
			throw new Exception("getCubicleERPID --> breaker or switchboard cannot be null. ");
		}
		
		def parentERPID = parent.getProductErpId();
		if(parentERPID == null) {
			throw new Exception("getCubicleERPID --> parent '" + parent.getObjectName() + "'s ERPID cannot be null. ");
		}
		
		def materialCategory = breaker.getDynamicAttribute(BREAKER_DA_MATERIALCATEGORY)
		if(materialCategory == null) {
			throw new Exception("getCubicleERPID --> breaker's (" + breaker.getObjectName()  +  ") " + materialCategory + " cannot be null. ");
		}
		
		def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))
		if(breakerRatedCurrent == null) {
			throw new Exception("getCubicleERPID --> breaker's (" + breaker.getObjectName()  +  ") " + BREAKER_DA_RATEDCURRENT + " cannot be null. ");
		}
		
		def breakerNoOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE))
		if(breakerNoOfPole == null) {
			throw new Exception("getCubicleERPID --> breaker's (" + breaker.getObjectName()  +  ") " + BREAKER_DA_NOOFPOLE + " cannot be null. ");
		}
		
		def breakerCapacityICU = Utils.convertToBigDecimal(BREAKER_DA_CAPACITYICU, breaker.getDynamicAttribute(BREAKER_DA_CAPACITYICU))
		if(breakerCapacityICU == null) {
			breakerCapacityICU = 0
		}
		
		def SBRatedCurrentMainBusBar = Utils.convertToBigDecimal(SB_DA_RATEDCURRENTMAINBUSBAR, switchboard.getDynamicAttribute(SB_DA_RATEDCURRENTMAINBUSBAR))
		if(SBRatedCurrentMainBusBar==null) {
			SBRatedCurrentMainBusBar = 0
		}
			
		def SBPreferredArrangement = switchboard.getDynamicAttribute(SB_DA_PREFERRED_ARRANGEMENT)
		if(SBPreferredArrangement == null) {
			SBPreferredArrangement = ''
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
				}
				else if  (breakerRatedCurrent >= 2500 && breakerRatedCurrent <= 3200 && breakerCapacityICU <= 65) {
					newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
				}
				break;
			
			case MaterialCategory.MCCB.toString():
				
				if(SBRatedCurrentMainBusBar <= 4000) {
					if (800 <= breakerRatedCurrent && breakerRatedCurrent <= 1600){
						if ( balanceOfBreaker >= 2)
						{
							newCubicle = parent.addChildByProductErpId(MCCB_2_VERTICAL_CUBICLE_ERP_ID );
							newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_1000mm);
						}else {
							newCubicle = parent.addChildByProductErpId(MCCB_VERTICAL_CUBICLE_ERP_ID );
							newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_600mm);
						}
						
					}else if(breakerRatedCurrent < 800){
						//condition for MA cubicle
						if (breakerRatedCurrent <= 250 && 
							SBPreferredArrangement.contains(SB_DA_PREFERRED_ARRANGEMENT_VALUE_MA) &&
							(SBForm.contains(SB_DA_FORM_VALUE_1) || SBForm.contains(SB_DA_FORM_VALUE_2)) &&
							breakerNoOfPole == 3 &&
							balanceOfBreaker >= 3 &&
							parentERPID == OUTGOING_ERPID)
						{
							newCubicle = parent.addChildByProductErpId(MCCB_MA_CUBICLE_ERP_ID);
							newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_800mm);
						}
						//condition for Horizontal cubicle
						else if (breakerRatedCurrent <= 630 && 
							//SBPreferredArrangement.contains(SB_DA_PREFERRED_ARRANGEMENT_VALUE_HOR) &&						
							balanceOfBreaker >= 3)
						{
							newCubicle = parent.addChildByProductErpId(MCCB_HORIZONTAL_CUBICLE_ERP_ID);
							newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_1000mm);
						}
						else if ((breakerRatedCurrent >= 125 && breakerRatedCurrent <= 630) )
						{
							newCubicle = parent.addChildByProductErpId(MCCB_VERTICAL_CUBICLE_ERP_ID);
							newCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_400mm);
						}
					}
				}else {
					Logger.addError("The script does not support SBRatedCurrentMainBusBar > 4000 for Material Category MCCB");
				}

				break;
				
				case MaterialCategory.ICCB.toString():
					newCubicle = parent.addChildByProductErpId(ICCB_CUBICLE_ERP_ID);
				break;
			default:
			newCubicle = null;
		}
		
		if(newCubicle != null) {
			populateCubicleAttributes(newCubicle);
		}
		
		if(ENABLE_LOGGER && newCubicle != null) {
			Logger.addInfo("\nNew Cubicle: " + newCubicle.getProductErpId() + 
				" with Width: " + newCubicle.getDynamicAttribute(CUBICLE_DA_WIDTH) +
				" with LV_CUBICLE_HBB_I_YD: " + newCubicle.getDynamicAttribute(CUBUCLE_DA_HBB_I_YD) +
				"\n");
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
				if(ENABLE_LOGGER) {
					Logger.addInfo("=========  Assigning for breaker-" + (i+1) + " =========") ;
				}
				SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
				
				if(newCubicle == null) {
					genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
					tempBreaker.changeParent(genericCubicle);
				}else {
					lastCubicle = assignBreakerToCubicle(tempBreaker, newCubicle);
				}
			}
			else
			{
				if(ENABLE_LOGGER) {
					Logger.addInfo("=========  Assigning for breaker-" + (i+1) + " =========") ;
				}
				if (NullUtil.isNullorEmpty(lastCubicle))
				{
					SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
									
					if(newCubicle == null) {
						if(genericCubicle==null)
							genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
						tempBreaker.changeParent(genericCubicle);
					}else {
						lastCubicle = assignBreakerToCubicle(tempBreaker, newCubicle);
					}
				}
				else
				{
					lastCubicle = assignBreakerToCubicle(tempBreaker, lastCubicle);
					boolean flag = CubicleValidation.checkCubicle(lastCubicle);
					
					if(!flag) {
						
						//change mccb 2 verticle cubicle to verticle cubicle if it only contains 1 breaker.
						if(lastCubicle != null && lastCubicle.getProductErpId().contains(MCCB_2_VERTICAL_CUBICLE_ERP_ID) && lastCubicle.getChildren().size() <= 2 ){
									
							SalesItemNode replacementCubicle = parent.addChildByProductErpId(MCCB_VERTICAL_CUBICLE_ERP_ID);
							SalesItemNode breaker = lastCubicle.getChildren().get(0);
							breaker.changeParent(replacementCubicle);
							def breakerRatedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))
							if(breakerRatedCurrent >=800 && breakerRatedCurrent <=1600) {
								replacementCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_600mm);
							}else {
								replacementCubicle.setDynamicAttribute(CUBICLE_DA_WIDTH, CUBICLE_DA_WIDTH_Value_400mm);
							}
							
							SalesItemNode toDeleteCubicle = lastCubicle;
							lastCubicle = replacementCubicle;
							toDeleteCubicle.delete();
						}
						//to remove empty assembly
						if(lastCubicle != null && lastCubicle.getProductErpId().contains(MCCB_MA_CUBICLE_ERP_ID)){
							tempBreaker.changeParent(parent);
							List<SalesItemNode> ma_assemblies = lastCubicle.filterChildrenByProductERPID( [MA_ASSEMBLY]);
							List<SalesItemNode> toDelete_ma_assemblies = new ArrayList<SalesItemNode> ();
							for(int j = 0; j< ma_assemblies.size(); j++) {
								List<SalesItemNode> breakersInAssembly = ma_assemblies.get(j).filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
								if(breakersInAssembly.size() == 0) {
									toDelete_ma_assemblies.add(ma_assemblies.get(j));									
								}
							}
							
							if(!toDelete_ma_assemblies.isEmpty()) {
								for(int j = 0; j< toDelete_ma_assemblies.size(); j++) {
									toDelete_ma_assemblies.get(j).delete();
								}
							}
						}
						
						
						SalesItemNode newCubicle = getCubicleByERPID(tempBreaker, switchboard, parent, balanceOfBreaker);
						if(newCubicle == null) {
							if(genericCubicle==null)
								genericCubicle = parent.addChildByProductErpId(GENERIC_CUBICLE_ERP_ID);
								tempBreaker.changeParent(genericCubicle);
						}else {
							lastCubicle = assignBreakerToCubicle(tempBreaker, newCubicle);
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
	
	Closure setHighestRatedCurrentFromIncomer = { List sortedBreaker ->
		if(sortedBreaker != null && !sortedBreaker.isEmpty()) {
			SalesItemNode breaker = sortedBreaker.get(0);
			def breakerRatedCurrent = breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT);
			
			if(breakerRatedCurrent != null) {
				highestRatedCurrentFromIncomer = String.valueOf(breakerRatedCurrent);
			}
		}
		
	}
	
	Closure processBreakers = {SalesItemNode salesItemNode, SalesItemNode switchboard ->
		
		List<SalesItemNode> listOfBreaker = salesItemNode.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
		
		String ERPIDOfSalesItemNode = salesItemNode.getProductErpId();

		for(int i =0; i < listOfBreaker.size() ; i++) {
			splitItem(listOfBreaker.get(i));
		}
			
		listOfBreaker = salesItemNode.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MATERIAL_CATEGORY_VALUES );
				
		Map<MaterialCategory, List<SalesItemNode>> categorizedBreakers = categoriseBreaker(listOfBreaker);

		SalesItemNode genericCubicle = null;
		
		for (Map.Entry<String, List<SalesItemNode>> entry : categorizedBreakers.entrySet())
		{
			String materialCategory = entry.getKey();
		
			Logger.addInfo('processing breaker type:' + materialCategory) ;			
			
			List<SalesItemNode> breakers = entry.getValue();

			def sortedBreaker = breakers.toSorted {x,y -> Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, y.getDynamicAttribute(BREAKER_DA_RATEDCURRENT)) <=> Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, x.getDynamicAttribute(BREAKER_DA_RATEDCURRENT))}

			//find and set the highest Rated Current of breaker from Incomer
			if(ERPIDOfSalesItemNode.contains(INCOMING_ERPID) && materialCategory.contains(MaterialCategory.ACB.toString())) {
				setHighestRatedCurrentFromIncomer(sortedBreaker);
			}
			
			if(ENABLE_LOGGER) {
				for(SalesItemNode breaker: sortedBreaker) {
					Logger.addInfo('breaker rated current:' + breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT)) ;
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

			if(groupings.isEmpty()) {
				continue;
			}
			
			for(SalesItemNode grouping : groupings)
			{
				processBreakers(grouping, switchboard);
			}
			
		}
		
		
	return salesItemsTree;
	}
	
	SalesItemsTree newQuote = execute();
	String infoLog = Logger.getInfoLog();
	String errorLog = Logger.getErrorLog();
	Map<String, Object> result = new HashMap<String, Object>();
	result.put("result", newQuote);
	result.put("infoLog", infoLog);
	result.put("errorLog", errorLog);
	
	if (StringUtils.isNotBlank(errorLog))
	{
		notificationService.addAndSendMessage(MessageFactory.createErrorMessage(errorLog,
			MessageDisplayType.NORMAL));
	}

	if (LOGGER.isDebugEnabled())
	{
		if (StringUtils.isNotBlank(infoLog))
		{
			LOGGER.debug(infoLog);
		}
	}
	
	return newQuote;



