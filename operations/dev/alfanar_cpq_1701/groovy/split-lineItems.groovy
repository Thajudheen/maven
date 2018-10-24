import java.util.List;
import java.util.Map
import java.math.BigDecimal;

import com.imc.iss.groovy.salesitem.SalesItemsTree;
import com.imc.iss.groovy.salesitem.SalesItemNode;


Closure splitItem = {SalesItemNode salesItemNode ->
		
		def quantity = salesItemNode.getQuantity();

		if(quantity > 1) {
			for(int i = 0; i <(quantity-1); i++ ) {
				salesItemNode.split(1);
			}
		}
		
	}
	

	List<SalesItemNode> children = salesItemsTree.getChildren();
	for(int i =0; i < children.size() ; i++)
	{
			splitItem(children.get(i));	
	}

	
return salesItemsTree;