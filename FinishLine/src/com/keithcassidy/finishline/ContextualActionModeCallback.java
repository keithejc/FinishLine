package com.keithcassidy.finishline;

public interface ContextualActionModeCallback 
{
	/**
	 * Invoked when an item is selected.
	 * 
	 * @param itemId the context menu item id
	 * @param position the position of the selected row
	 * @param id the id of the selected row, if available
	 */
	public boolean onClick(int itemId, int position, long id);
}
