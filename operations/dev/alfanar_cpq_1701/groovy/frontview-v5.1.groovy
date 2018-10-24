import java.math.BigDecimal;
import java.util.List;

import groovy.transform.Field;

import com.imc.iss.groovy.autocad.AutocadScript;
import com.imc.iss.groovy.autocad.AutocadScript2D;
import com.imc.iss.groovy.salesitem.SalesItemsTree;
import com.imc.iss.groovy.salesitem.SalesItemNode;

import static Constants.*;
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
 */
class Constants {
	static final boolean ENABLE_SPLITBREAKER = true;
	static final boolean ENABLE_LOGGER = true;

	//list of ERPID to identify type of product
	//static final def SWITCHBOARD_ERPIDS = ["LV-SB-MF"]  //to be deleted
	static final def SWITCHBOARD_OBJECTNAME = ["LV-SB-MF"]
	
	//static final def INCOMINDERPID = "LV-INCOMER"
	//static final def OUTGOINGERPID = "LV-OUTGOING"
	//static final def GROUPING_ERPIDS = [INCOMINDERPID, OUTGOINGERPID]  //to be deleted
	
	static final def INCOMING_ObjectName = "LV-Incomer"
	static final def OUTGOING_ObjectName = "LV-Feeder"
	static final def GROUPING_ObjectNames = [INCOMING_ObjectName, OUTGOING_ObjectName]
		
	static final def MA_ASSEMBLY = "LV-MA-ASSEMBLY"

	//breaker
	static final String BREAKER_DA_MATERIALCATEGORY = "MATERIAL_CATEGORY";
	static final String BREAKER_DA_RATEDCURRENT = "RATED_CURRENT_AF";
	static final String BREAKER_DA_NOOFPOLE = "NUMBER_OF_POLE";
	static final String BREAKER_DA_CAPACITYICU = "RATED_ULT_BREAKING_CAPACIT_ICU"

	static final def MATERIAL_CATEGORY_VALUES = [MaterialCategory.ACB.toString(), MaterialCategory.MCCB.toString(), MaterialCategory.ICCB.toString(), MaterialCategory.CBR.toString()]
	
	static final int DEFAULT_BREAKER_MODULARITY = 3;
	
	//cubicle
	static final String GENERIC_CUBICLE_ERP_ID = "LV-CUBICLE-GENERIC"
	static final String ACB_CUBICLE_ERP_ID = "LV-CUBICLE-ACB"
	static final String MCCB_HORIZONTAL_CUBICLE_ERP_ID = "LV-CUBICLE-HORIZONTAL"
	static final String MCCB_MA_CUBICLE_ERP_ID = "LV-CUBICLE-MA"
	static final String MCCB_VERTICAL_CUBICLE_ERP_ID = "LV-CUBICLE-VERTICAL"
	static final String MCCB_2_VERTICAL_CUBICLE_ERP_ID = "LV-CUBICLE-2-VERTICAL"
	static final String ICCB_CUBICLE_ERP_ID = "ICCB-001"
	static final def CUBICLE_ERPIDS = [ACB_CUBICLE_ERP_ID, MCCB_HORIZONTAL_CUBICLE_ERP_ID, MCCB_MA_CUBICLE_ERP_ID, MCCB_VERTICAL_CUBICLE_ERP_ID, MCCB_2_VERTICAL_CUBICLE_ERP_ID]
	
	static final String CUBICLE_DA_WIDTH = "LV-CUBICLE_WIDTH"
	static final String CUBICLE_DA_WIDTH_Value_400mm = "400mm"
	static final String CUBICLE_DA_WIDTH_Value_600mm = "600mm"
	static final String CUBICLE_DA_WIDTH_Value_800mm = "800mm"
	static final String CUBICLE_DA_WIDTH_Value_1000mm = "1000mm"
	static final String CUBICLE_DA_WIDTH_Value_1200mm = "1200mm"

	static final String CUBICLE_BUS_BAR_LOCATION = "LV-SB-MF_BBP_U"
	static final String CUBICLE_BUS_BAR_LOCATION_LOWER = "CUBICLE_LHBB"
	static final String CUBICLE_BUS_BAR_LOCATION_UPPER = "CUBICLE_UHBB"
		
	//SwitchBoard
	static final String SB_DA_PREFERRED_ARRANGEMENT = "Preferred_Arrangement_U"
	static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_MA = "MA"
	static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_HOR = "HOR"
	static final String SB_DA_PREFERRED_ARRANGEMENT_VALUE_VER = "VER"

	static final String SB_DA_SHORT_CIRCUIT = "LV-SB-MF_ICW_U"
	static final String SB_DA_SHORT_CIRCUIT_VALUE_50 = "SB_ICW_50"
	static final String SB_DA_SHORT_CIRCUIT_VALUE_65 = "SB_ICW_65"
	static final String SB_DA_SHORT_CIRCUIT_VALUE_85 = "SB_ICW_85"
	static final String SB_DA_SHORT_CIRCUIT_VALUE_100 = "SB_ICW_100"
	static final String SB_DA_SHORT_CIRCUIT_VALUE_120 = "SB_ICW_120"
	
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

// Declare some global variables
@Field BigDecimal cubicle_x = 0;
@Field BigDecimal cubicle_y = 0;
@Field BigDecimal breaker_x = 0;
@Field BigDecimal breaker_y = 0;
@Field boolean isReverse = false;
@Field def defaultbusBarLocation = null;
@Field def shortCircuit = null;
@Field String tagLetter;
@Field int tagNumber;

void processCubicle(AutocadScript autocadScript, SalesItemNode cubicle)
{
	String busBarLocation = cubicle.getDynamicAttribute(CUBICLE_BUS_BAR_LOCATION);
	if(busBarLocation == null)
	{
		busBarLocation = defaultbusBarLocation;
	}

	switch(cubicle.getProductErpId()) {
		case ACB_CUBICLE_ERP_ID:
			processAcbCubicle(autocadScript, cubicle, busBarLocation);
			break;
		case MCCB_2_VERTICAL_CUBICLE_ERP_ID:
			process2VerticalCubicle(autocadScript, cubicle, busBarLocation);
			break;
		case MCCB_VERTICAL_CUBICLE_ERP_ID:
			processVerticalCubicle(autocadScript, cubicle, busBarLocation);
			break;
		case MCCB_HORIZONTAL_CUBICLE_ERP_ID:
			processMccbCubicle(autocadScript, cubicle, busBarLocation);
			break;
		case MCCB_MA_CUBICLE_ERP_ID:
			processMaCubicle(autocadScript, cubicle, busBarLocation);
			break;
	}
}

void processAcbCubicle(AutocadScript autocadScript, SalesItemNode cubicle, String busBarLocation)
{
	SalesItemNode breaker = cubicle.getChildren().get(0);
	def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
	BigDecimal width = 0;
	if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_65 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent >= 800 && ratedCurrent <= 2000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC65kA - LHBB - 800A to 2000A.dwg", cubicle_x, cubicle_y);
		width = 600;
	}
	else if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_65 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent >= 2500 && ratedCurrent <= 4000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC65kA - LHBB - 2500A to 4000A.dwg", cubicle_x, cubicle_y);
		width = 800;
	}
	else if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_65 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent >= 800 && ratedCurrent <= 2000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC65kA - UHBB - 800A to 2000A.dwg", cubicle_x, cubicle_y);
		width = 600;
	}
	else if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_65 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent >= 2500 && ratedCurrent <= 4000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC65kA - UHBB - 2500A to 4000A.dwg", cubicle_x, cubicle_y);
		width = 800;
	}
	else if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_100 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent >= 800 && ratedCurrent <= 4000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC100kA - LHBB - 800A to 4000A.dwg", cubicle_x, cubicle_y);
		width = 800;
	}
	else if(shortCircuit == SB_DA_SHORT_CIRCUIT_VALUE_100 && busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent >= 800 && ratedCurrent <= 4000)
	{
		autocadScript.insertFileAsBlock("ACBs - D.O - Fixed - SC100kA - UHBB - 800A to 4000A.dwg", cubicle_x, cubicle_y);
		width = 800;
	}
	autocadScript.insertText(tagLetter + tagNumber, cubicle_x + (width/2), cubicle_y + 1245, AutocadScript.TextAnchor.MID_CENTER);
	cubicle_x += width;
	tagNumber++;
}

void process2VerticalCubicle(AutocadScript autocadScript, SalesItemNode cubicle, String busBarLocation)
{
	SalesItemNode[] breaker = [cubicle.getChildren().get(0), cubicle.getChildren().get(1)];
	BigDecimal[] breaker_x = [cubicle_x + 25, cubicle_x + 525];
	BigDecimal breaker_y = cubicle_y + 50;

	autocadScript.insertFileAsBlock("MCCB - 2 x Vertical - Fixed - SC65&100kA - Cell.dwg", cubicle_x, cubicle_y);
	cubicle_x += 1000;
	
	for (int i = 0; i <= 1; i++)
	{
		def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker[i].getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
		if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent == 800)
		{
			autocadScript.insertFileAsBlock("MCCB - 2 x Vertical - Fixed - SC65&100kA - LHBB - 800A.dwg", breaker_x[i], breaker_y);
			autocadScript.insertText(tagLetter + tagNumber, breaker_x[i] + 355, breaker_y + 866);
		}
		else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && (ratedCurrent == 1250 || ratedCurrent == 1600))
		{
			autocadScript.insertFileAsBlock("MCCB - 2 x Vertical - Fixed - SC65&100kA - LHBB - 1250&1600A.dwg", breaker_x[i], breaker_y);
			autocadScript.insertText(tagLetter + tagNumber, breaker_x[i] + 355, breaker_y + 866);
		}
		else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent == 800)
		{
			autocadScript.insertFileAsBlock("MCCB - 2 x Vertical - Fixed - SC65&100kA - UHBB - 800A.dwg", breaker_x[i], breaker_y);
			autocadScript.insertText(tagLetter + tagNumber, breaker_x[i] + 355, breaker_y + 1566);
		}
		else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && (ratedCurrent == 1250 || ratedCurrent == 1600))
		{
			autocadScript.insertFileAsBlock("MCCB - 2 x Vertical - Fixed - SC65&100kA - UHBB - 1250&1600A.dwg", breaker_x[i], breaker_y);
			autocadScript.insertText(tagLetter + tagNumber, breaker_x[i] + 355, breaker_y + 1566);
		}
		tagNumber++;
	}
}

void processVerticalCubicle(AutocadScript autocadScript, SalesItemNode cubicle, String busBarLocation)
{
	SalesItemNode breaker = cubicle.getChildren().get(0);
	def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
	if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent == 125)
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - LHBB - 125A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 270, cubicle_y + 802.5);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && (ratedCurrent == 160 || ratedCurrent == 250))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - LHBB - 160&250A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 277.5, cubicle_y + 807.5);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && (ratedCurrent == 400 || ratedCurrent == 630))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - LHBB - 400&630A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 295, cubicle_y + 855);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && ratedCurrent == 800)
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - LHBB - 800A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 430, cubicle_y + 916);
		cubicle_x += 600;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER && (ratedCurrent == 1250 || ratedCurrent == 1600))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - LHBB - 1250&1600A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 430, cubicle_y + 951);
		cubicle_x += 600;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent == 125)
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - UHBB - 125A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 270, cubicle_y + 1602.5);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && (ratedCurrent == 160 || ratedCurrent == 250))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - UHBB - 160&250A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 277.5, cubicle_y + 1607.5);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && (ratedCurrent == 400 || ratedCurrent == 630))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - UHBB - 400&630A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 295, cubicle_y + 1655);
		cubicle_x += 400;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && ratedCurrent == 800)
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - UHBB - 800A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 430, cubicle_y + 1616);
		cubicle_x += 600;
	}
	else if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_UPPER && (ratedCurrent == 1250 || ratedCurrent == 1600))
	{
		autocadScript.insertFileAsBlock("MCCB - 1 x Vertical - Fixed - SC65&100kA - UHBB - 1250&1600A.dwg", cubicle_x, cubicle_y);
		autocadScript.insertText(tagLetter + tagNumber, cubicle_x + 430, cubicle_y + 1625);
		cubicle_x += 600;
	}
	tagNumber++;
}

void processMccbCubicle(AutocadScript autocadScript, SalesItemNode cubicle, String busBarLocation)
{
	breaker_x = cubicle_x + 225;
	if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER)
	{
		autocadScript.insertFileAsBlock("MCCB - Horizontal - Fixed - SC65kA - LHBB - Cell.dwg", cubicle_x, cubicle_y);
		breaker_y = cubicle_y + 350;
		isReverse = false;
	}
	else
	{
		autocadScript.insertFileAsBlock("MCCB - Horizontal - Fixed - SC65kA - UHBB - Cell.dwg", cubicle_x, cubicle_y);
		breaker_y = cubicle_y + 1900;
		isReverse = true;
	}
	List<SalesItemNode> mccbBreakers = cubicle.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MaterialCategory.MCCB.toString() );
	if(!mccbBreakers.isEmpty())
	{
		for(SalesItemNode mccbBreaker : mccbBreakers)
		{
			processMccbBreaker(autocadScript, mccbBreaker);
		}
	}
	cubicle_x += 1000;
}

void processMccbBreaker(AutocadScript autocadScript, SalesItemNode breaker)
{
	String blockName = null;
	BigDecimal breakerHeight;

	def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
	def numberOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE));
	
	if(numberOfPole == 3 && ratedCurrent == 125)
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 3P - 125A.dwg";
		breakerHeight = 150;
	}
	else if(numberOfPole == 3 && (ratedCurrent == 160 || ratedCurrent == 250))
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 3P - 160&250A.dwg";
		breakerHeight = 150;
	}
	else if(numberOfPole == 3 && (ratedCurrent == 400 || ratedCurrent == 630))
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 3P - 400&630A.dwg";
		breakerHeight = 200;
	}
	else if(numberOfPole == 4 && ratedCurrent == 125)
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 4P - 125A.dwg";
		breakerHeight = 200;
	}
	else if(numberOfPole == 4 && (ratedCurrent == 160 || ratedCurrent == 250))
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 4P - 160&250A.dwg";
		breakerHeight = 200;
	}
	else if(numberOfPole == 4 && (ratedCurrent == 400 || ratedCurrent == 630))
	{
		blockName = "MCCB - Horizontal - Fixed - SC65kA - 4P - 400&630A.dwg";
		breakerHeight = 250;
	}

	if(blockName != null)
	{
		if(isReverse)
		{
			breaker_y -= breakerHeight;
		}
		autocadScript.insertFileAsBlock(blockName, breaker_x, breaker_y);
		autocadScript.insertText(tagLetter + tagNumber, breaker_x + 365, breaker_y + breakerHeight - 20);
		tagNumber++;
		if(!isReverse)
		{
			breaker_y += breakerHeight;
		}
	}
}

void processMaCubicle(AutocadScript autocadScript, SalesItemNode cubicle, String busBarLocation)
{
	breaker_x = cubicle_x + 25;
	if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER)
	{
		autocadScript.insertFileAsBlock("MCCB - MA - Fixed - SC65kA - LHBB - Cell.dwg", cubicle_x, cubicle_y);
		breaker_y = cubicle_y + 350;
		isReverse = false;
	}
	else
	{
		autocadScript.insertFileAsBlock("MCCB - MA - Fixed - SC65kA - UHBB - Cell.dwg", cubicle_x, cubicle_y);
		breaker_y = cubicle_y + 1900;
		isReverse = true;
	}
	List<SalesItemNode> maAssemblies = cubicle.filterChildrenByProductERPID([MA_ASSEMBLY]);
	if(!maAssemblies.isEmpty())
	{
		for(SalesItemNode maAssembly : maAssemblies)
		{
			processMaBreaker(autocadScript, maAssembly, busBarLocation);
		}
	}
	cubicle_x += 800;
}

void processMaBreaker(AutocadScript autocadScript, SalesItemNode maAssembly, String busBarLocation)
{
	if(isReverse)
	{
		breaker_y -= 275;
	}
	//TODO: Is there any difference between mounting for 125A and 160/250A???
	autocadScript.insertFileAsBlock("MCCB - MA - Fixed - SC65kA - 3P - Mounting - 125A x 4.dwg", breaker_x, breaker_y);
	
	List<SalesItemNode> maBreakers = maAssembly.filterChildrenByDynamicAttribute(BREAKER_DA_MATERIALCATEGORY, MaterialCategory.MCCB.toString() );
	if(!maBreakers.isEmpty())
	{
		int i = 0;
		BigDecimal[] breaker_x_offset = [330.5, 419.5, 330.5, 419.5];
		BigDecimal[] tag_x_offset = [75, 620, 75, 620];
		BigDecimal[] y_offset;
		if(busBarLocation == CUBICLE_BUS_BAR_LOCATION_LOWER)
		{
			y_offset = [85, 85, 190, 190];
		}
		else
		{
			y_offset = [190, 190, 85, 85];
		}
		AutocadScript.TextAnchor[] tag_anchor = [AutocadScript.TextAnchor.MID_RIGHT, AutocadScript.TextAnchor.MID_LEFT, AutocadScript.TextAnchor.MID_RIGHT, AutocadScript.TextAnchor.MID_LEFT];
		for(SalesItemNode breaker : maBreakers)
		{
			String blockName = null;
			BigDecimal breakerHeight, breakerWidth;
			
			def ratedCurrent = Utils.convertToBigDecimal(BREAKER_DA_RATEDCURRENT, breaker.getDynamicAttribute(BREAKER_DA_RATEDCURRENT));
			def numberOfPole = Utils.convertToBigDecimal(BREAKER_DA_NOOFPOLE, breaker.getDynamicAttribute(BREAKER_DA_NOOFPOLE));
			if(numberOfPole == 3 && ratedCurrent == 125)
			{
				blockName = "MCCB - MA - Fixed - SC65kA - 3P - CB - 125A.dwg";
				breakerHeight = 90;
				breakerWidth = 155;
			}
			else if(numberOfPole == 3 && (ratedCurrent == 160 || ratedCurrent == 250))
			{
				blockName = "MCCB - MA - Fixed - SC65kA - 3P - CB - 160&250A.dwg";
				breakerHeight = 105;
				breakerWidth = 165;
			}
			
			if(blockName != null)
			{
				BigDecimal x = breaker_x + breaker_x_offset[i] - (i%2 ? 0 : breakerWidth);
				BigDecimal y = breaker_y + y_offset[i] - (breakerHeight/2);
				autocadScript.insertFileAsBlock(blockName, x, y);
				autocadScript.insertText(tagLetter + tagNumber, breaker_x + tag_x_offset[i] , breaker_y + y_offset[i], AutocadScript.TextAnchor.MID_LEFT);
				tagNumber++;
			}
			i++;
		}
	}
	
	if(!isReverse)
	{
		breaker_y += 275;
	}
}

AutocadScript generateScript(AutocadScript autocadScript, SalesItemsTree salesItemsTree)
{
	autocadScript.setDefaultFontSize(30);
	autocadScript.setDefaultTextAnchor(AutocadScript.TextAnchor.TOP_LEFT);

	for(SalesItemNode switchboard : salesItemsTree.filterChildrenByProductName(SWITCHBOARD_OBJECTNAME))
	{
		defaultbusBarLocation = switchboard.getDynamicAttribute(CUBICLE_BUS_BAR_LOCATION);
		if(defaultbusBarLocation == null)
		{
			defaultbusBarLocation = CUBICLE_BUS_BAR_LOCATION_LOWER;
		}
		
		shortCircuit = switchboard.getDynamicAttribute(SB_DA_SHORT_CIRCUIT);
		
		List<SalesItemNode> groupings = switchboard.filterChildrenByProductName(GROUPING_ObjectNames);
		if(!groupings.isEmpty())
		{
			for(SalesItemNode grouping : groupings)
			{
				tagLetter = grouping.getProductObjectName().equals(INCOMING_ObjectName) ? 'Q' : 'F';
				tagNumber = 1;
				List<SalesItemNode> cubicles = grouping.filterChildrenByProductERPID(CUBICLE_ERPIDS);
				if(!cubicles.isEmpty())
				{
					for(SalesItemNode cubicle : cubicles)
					{
						processCubicle(autocadScript, cubicle);
					}
				}
			}
		}
	}
	return autocadScript;
}

return generateScript(new AutocadScript2D(), salesItemsTree);