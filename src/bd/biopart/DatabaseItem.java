package bd.biopart;


/**
 * A database item. Each item has a name that uniquely identifies an item.
 * @author Bastiaan van den Berg
 */
public interface DatabaseItem {	
	
	/**
	 * @return The name of the database item.
	 */
	public String getName();
}