import java.util.List;
import java.util.Map
import java.math.BigDecimal;

import com.imc.iss.groovy.salesitem.SalesItemsTree;
import com.imc.iss.groovy.salesitem.SalesItemNode;

import static Constants.*

class Constants
{
	// schemas
	static final String AS = "http://www.inmindcomputing.com/application/application-schema.owl#"
	static final String AI = "http://www.inmindcomputing.com/application/application-implementation.owl#"
	static final String PI = "http://www.inmindcomputing.com/application/products/products-implementation.owl#"

	// product types
	static final String SWITCHBOARD = AS + "SWITCHBOARDTYPE"
	static final String CUBICLE = AS + "CUBICLE"
	static final String BREAKER = AS + "BREAKERTYPE"

	// dynamic attributes
	static final String MATERIAL_CATEGORY = "Material_Category"
	static final String MATERIAL_CATEGORY_ACB = "ACB"
	static final String MATERIAL_CATEGORY_MCCB = "MCCB"
	static final String RATED_CURRENT = "Rated_Current"
	static final String NO_OF_POLE = "No_of_Pole"
	
	// cubicles ERP ID
	static final String ACB_CUBICLE_ERP_ID = "CUBICLE_ACB"
	static final String MCCB_CUBICLE_ERP_ID = "CUBICLE_MCCB"

	// other constants
	static final int MCCB_CUBICLE_CAPACITY = 34
}

for(SalesItemNode switchboard : salesItemsTree.filterChildrenByProductType(SWITCHBOARD))
{
	// to avoid ConcurrentModificationException error, have to use the basic for loop here 
	List<SalesItemNode> cubicles = switchboard.getChildren();
	for (int i = 0; i < cubicles.size(); i++)
	{
		SalesItemNode cubicle = cubicles.get(i);
		if(cubicle.getProductTypeURI().equals(CUBICLE))
		{
			List<SalesItemNode> breakers = cubicle.getChildren();
			for (int j = 0; j < breakers.size(); j++)
			{
				SalesItemNode breaker = breakers.get(j);
				breaker.changeParent(switchboard);
				j--; // the breaker has been removed from the list
			}
			cubicle.delete();
			i--; // the cubicle has been removed from the list
		}
	}
	
	// resort the items
	switchboard.getChildren().sort{x,y -> x.getObjectName() <=> y.getObjectName()}
	
	// loop through once more to merge any mccbs that were split
	List<SalesItemNode> mccbBreakers = switchboard.filterChildrenByDynamicAttribute(MATERIAL_CATEGORY, MATERIAL_CATEGORY_MCCB);
	SalesItemNode previousBreaker = null;
	for (int i = 0; i < mccbBreakers.size(); i++)
	{
		SalesItemNode currentBreaker = mccbBreakers.get(i);
		if(previousBreaker != null && previousBreaker.getObjectName().equals(currentBreaker.getObjectName())
			&& previousBreaker.getDynamicAttribute(RATED_CURRENT).equals(currentBreaker.getDynamicAttribute(RATED_CURRENT))
			&& previousBreaker.getDynamicAttribute(NO_OF_POLE).equals(currentBreaker.getDynamicAttribute(NO_OF_POLE)))
		{
			previousBreaker.changeQuantity(previousBreaker.getQuantity().add(currentBreaker.getQuantity()));
			currentBreaker.delete();
		} else {
			previousBreaker = currentBreaker;
		}
	}
}

return salesItemsTree;