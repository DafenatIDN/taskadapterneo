package com.taskadapter.webui.pages;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class LayoutsUtil {
    /**
     * create a horizontal layout with the provided element in the center.
     */
    public static HorizontalLayout centeredLayout(VerticalLayout component, int componentWidth) {
        var layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        // DEBUG - Visually display the  bounds of this layout.
        // layout.getStyle().set("border", "2px dotted DarkOrange");

        component.setWidth(componentWidth + "px");
        layout.add(component);
        return layout;
    }
}
