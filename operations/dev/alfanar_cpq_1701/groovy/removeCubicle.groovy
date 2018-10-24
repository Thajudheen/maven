import java.util.List;
import java.util.Map
import java.math.BigDecimal;

import com.imc.iss.groovy.salesitem.SalesItemsTree;
import com.imc.iss.groovy.salesitem.SalesItemNode;

import static Constants.*

class Constants
{
	// schemas
	static final AS = "http://www.inmindcomputing.com/application/application-schema.owl#"
	static final AI = "http://www.inmindcomputing.com/application/application-implementation.owl#"
	static final PI = "http://www.inmindcomputing.com/application/products/products-implementation.owl#"
	
	// product types
	static final SWITCHBOARD = AS + "SWITCHBOARDTYPE"
	static final CUBICLE = AS + "CUBICLE"
	static final BREAKER = AS + "BREAKERTYPE"
	
	// dynamic attributes
	static final MATERIAL_CATEGORY = "Material_Category"
	static final RATED_CURRENT = "Rated_Current_(AF)"
	static final NO_OF_POLE = "No of Pole"
	static final MATERIAL_CATEGORY_ACB = "ACB"
	static final MATERIAL_CATEGORY_MCCB = "MCCB"
	
	// cubicles ERP ID
	static final ACB_CUBICLE_ERP_ID = "LV-CUBICLE-ACB"
	static final MCCB_CUBICLE_ERP_ID = "CUBICLE-MCCB"

	static final def SWITCHBOARD_ERPIDS = ["LV-SB-MF"]
	static final def cubiclesERPIDs = ["LV-CUBICLE-GENERIC", "LV-CUBICLE-ACB", "LV-CUBICLE-HORIZONTAL",  "LV-CUBICLE-MA", "LV-CUBICLE-VERTICAL", "LV-CUBICLE-2-VERTICAL"]
	static final def GROUPING_ERPIDS = ["LV-INCOMER", "LV-OUTGOING", "GP-001"]
}


Closure removeCubicle = {List<SalesItemNode> cubicles, SalesItemNode switchboard  ->
	for (int i = 0; i < cubicles.size(); i++)
		{
			SalesItemNode cubicle = cubicles.get(i);
						
			if(cubiclesERPIDs.contains(cubicle.getProductErpId()))
			{
					List<SalesItemNode> breakers = cubicle.getChildren();
					for (int j = 0; j < breakers.size(); j++)
					{
						SalesItemNode breaker = breakers.get(j);
						breaker.changeParent(switchboard);
						j--;
					}
					cubicle.delete();
			}
		}
}

for(SalesItemNode switchboard : salesItemsTree.filterChildrenByProductERPID(SWITCHBOARD_ERPIDS))
	{
		List<SalesItemNode> groupings = switchboard.filterChildrenByProductERPID(GROUPING_ERPIDS);
		
		if(!groupings.isEmpty()) {
			for(int i=0; i<groupings.size(); i++ ) {
			List<SalesItemNode> childrens = groupings.getAt(i).filterChildrenByProductERPID(cubiclesERPIDs);
			removeCubicle(childrens, groupings.getAt(i) );
			}
		}
		
		List<SalesItemNode> childrens = switchboard.filterChildrenByProductERPID(cubiclesERPIDs);
		removeCubicle(childrens,switchboard );
		
		
		// resort the items
		switchboard.getChildren().sort{x,y -> x.getObjectName() <=> y.getObjectName()}
	}

return salesItemsTree;