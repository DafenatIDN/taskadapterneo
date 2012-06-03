package com.taskadapter.web.configeditor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.taskadapter.connector.Priorities;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.ObjectProperty;

/**
 * Properties model.
 * 
 * @author maxkar
 * 
 */
final class PrioritiesModel implements Container,
		Container.ItemSetChangeNotifier, Container.PropertySetChangeNotifier {
	private static final long serialVersionUID = 1L;

	private static final Map<String, Class<?>> PROPTYPES = new HashMap<String, Class<?>>();

	static {
		PROPTYPES.put("text", String.class);
		PROPTYPES.put("value", Integer.class);
	}

	/**
	 * Used priorities.
	 */
	private final Priorities priorities;

	/**
	 * Item mapping.
	 */
	private final Map<Object, Item> items = new LinkedHashMap<Object, Item>();

	/**
	 * Item listeners.
	 */
	private final List<ItemSetChangeListener> itemListeners = new LinkedList<Container.ItemSetChangeListener>();

	PrioritiesModel(Priorities priorities) {
		this.priorities = priorities;
		fillItems(priorities);
	}

	@Override
	public void addListener(PropertySetChangeListener listener) {
		// not used
	}

	@Override
	public void removeListener(PropertySetChangeListener listener) {
		// not used
	}

	@Override
	public void addListener(ItemSetChangeListener listener) {
		itemListeners.add(listener);
	}

	@Override
	public void removeListener(ItemSetChangeListener listener) {
		itemListeners.remove(listener);
	}

	@Override
	public Item getItem(Object itemId) {
		return items.get(itemId);
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return PROPTYPES.keySet();
	}

	@Override
	public Collection<?> getItemIds() {
		return items.keySet();
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		final Item item = getItem(itemId);
		if (item == null)
			return null;
		return item.getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return PROPTYPES.get(propertyId);
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		return items.containsKey(itemId);
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates a content from a map.
	 * 
	 * @param props
	 *            used properties.
	 */
	void updateContent(Priorities props) {
		this.priorities.clear();
		for (String key : props.getAllNames())
			priorities.setPriority(key, props.getPriorityByText(key));
	}

	/**
	 * Fills a property set.
	 * 
	 * @param props
	 *            priorities.
	 */
	void fillItems(Priorities props) {
		items.clear();
		for (String key : props.getAllNames())
			items.put(key, createItem(key));
		final ItemSetChangeEvent evt = new ItemSetChangeEvent() {
			private static final long serialVersionUID = 1L;

			@Override
			public Container getContainer() {
				return PrioritiesModel.this;
			}
		};
		for (ItemSetChangeListener iscl : itemListeners
				.toArray(new ItemSetChangeListener[itemListeners.size()]))
			iscl.containerItemSetChange(evt);
	}

	/**
	 * Creates a new item.
	 * 
	 * @param key
	 *            item key.
	 * @return created item.
	 */
	private Item createItem(String key) {
		final ObjectProperty<String> text = new ObjectProperty<String>(key);
		text.setReadOnly(true);

		final Property value = new MethodProperty<Integer>(Integer.class,
				priorities, "getPriorityByText", "setPriority",
				new Object[] { key }, new Object[] { key, null }, 1);

		final Map<String, Property> propmap = new HashMap<String, Property>();
		propmap.put("text", text);
		propmap.put("value", value);

		return ConstItem.constItem(propmap);
	}

}
