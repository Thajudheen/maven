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

Closure <Integer> getBreakerSize = { Integer nPole, Integer current ->
	if(nPole == 3) {
		if(current <= 250) {
			return 3;
		} else if(current <= 630) {
			return 4;
		}
	} else if(nPole == 4) {
		if(current <= 250) {
			return 4;
		} else if(current <= 630) {
			return 5;
		}
	}
	return null;
}

for(SalesItemNode switchboard : salesItemsTree.filterChildrenByProductType(SWITCHBOARD))
{
	List<SalesItemNode> acbBreakers = switchboard.filterChildrenByDynamicAttribute(MATERIAL_CATEGORY, MATERIAL_CATEGORY_ACB);
	List<SalesItemNode> mccbBreakers = switchboard.filterChildrenByDynamicAttribute(MATERIAL_CATEGORY, MATERIAL_CATEGORY_MCCB);

	// Place each ACB breaker in its own cubicle
	acbBreakers.sort{x,y -> y.getDynamicAttribute(RATED_CURRENT) <=> x.getDynamicAttribute(RATED_CURRENT)}
	for(SalesItemNode breaker : acbBreakers)
	{
		SalesItemNode acbCubicle = switchboard.addChildByProductErpId(ACB_CUBICLE_ERP_ID);
		breaker.changeParent(acbCubicle);
	}

	if(!mccbBreakers.isEmpty())
	{
		mccbBreakers.sort{x,y -> y.getDynamicAttribute(RATED_CURRENT) <=> x.getDynamicAttribute(RATED_CURRENT)}
		SalesItemNode mccbCubicle = switchboard.addChildByProductErpId(MCCB_CUBICLE_ERP_ID);
		int occupiedSlot = 0;
		for(SalesItemNode breaker : mccbBreakers)
		{
			Integer nPole = (Integer)breaker.getDynamicAttribute(NO_OF_POLE);
			Integer current = (Integer)breaker.getDynamicAttribute(RATED_CURRENT);
			Integer size = getBreakerSize(nPole, current);
			
			if(size != null) {
				// if the remaining capacity in the cubicle can't even fit 1 of this breaker, add a new cubicle first
				if(MCCB_CUBICLE_CAPACITY - occupiedSlot < size) {
					mccbCubicle = switchboard.addChildByProductErpId(MCCB_CUBICLE_ERP_ID);
					occupiedSlot = 0;
				}
				
				int totalQty = breaker.getQuantity().intValue();
				// if the current cubicle can take in all of this breaker, just change the parent
				if(MCCB_CUBICLE_CAPACITY - occupiedSlot - (size * totalQty) >= 0) {
					breaker.changeParent(mccbCubicle);
					occupiedSlot += size * totalQty;
				}
				//otherwise, fit in as much as possible in the current cubicle, and put the rest in new cubicle(s)
				else {
					int splitQty = totalQty - Math.floor((MCCB_CUBICLE_CAPACITY - occupiedSlot) / size);
					SalesItemNode splitBreaker = breaker.split(splitQty);
					breaker.changeParent(mccbCubicle);
					totalQty = splitQty;
					SalesItemNode currentBreaker = splitBreaker;
					
					while (totalQty > 0) {
						mccbCubicle = switchboard.addChildByProductErpId(MCCB_CUBICLE_ERP_ID);
						occupiedSlot = 0;
						if(MCCB_CUBICLE_CAPACITY - occupiedSlot - (size * totalQty) >= 0) {
							currentBreaker.changeParent(mccbCubicle);
							occupiedSlot += size * totalQty;
							totalQty = 0;
						} else {
							splitQty = totalQty - Math.floor(MCCB_CUBICLE_CAPACITY / size);
							splitBreaker = currentBreaker.split(splitQty);
							currentBreaker.changeParent(mccbCubicle);
							currentBreaker = splitBreaker;
							totalQty = splitQty;
						}
					}
				}
			}
		}
	}
}

return salesItemsTree;