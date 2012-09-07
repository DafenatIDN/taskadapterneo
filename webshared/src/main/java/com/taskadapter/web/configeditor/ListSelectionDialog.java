package com.taskadapter.web.configeditor;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Window;

import java.util.Collection;

public class ListSelectionDialog extends Window {
    private String listTitle;
    private Collection<String> items;
    private EditorUtil.ValueListener valueListener;
    private Button closeButton;

    public ListSelectionDialog(String windowTitle, String listTitle, Collection<String> items, EditorUtil.ValueListener valueListener) {
        this.listTitle = listTitle;
        this.items = items;
        this.valueListener = valueListener;
        buildUI();
        setCaption(windowTitle);
        setCloseShortcut(ShortcutAction.KeyCode.ESCAPE);
    }

    private void buildUI() {
        setWidth("350px");
        final ListSelect listSelect = new ListSelect(listTitle, items);
        listSelect.setNullSelectionAllowed(false);
        listSelect.setWidth("300px");
        listSelect.setImmediate(true);
        listSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                closeButton.setEnabled(true);
            }
        });
        addComponent(listSelect);

        closeButton = new Button("Select");
        closeButton.setEnabled(false);
        closeButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                valueListener.setValue((String) listSelect.getValue());
                close();
            }
        });
        addComponent(closeButton);
    }
}
