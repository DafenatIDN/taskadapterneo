package com.taskadapter.web.configeditor;

import com.taskadapter.connector.definition.exceptions.BadConfigException;
import com.taskadapter.connector.definition.exceptions.ConnectorException;
import com.taskadapter.model.NamedKeyedObject;
import com.taskadapter.model.NamedKeyedObjectImpl;
import com.taskadapter.web.ExceptionFormatter;
import com.taskadapter.web.callbacks.DataProvider;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class EditorUtil {
    private final static Logger logger = LoggerFactory.getLogger(EditorUtil.class);

    // TODO 14 - not attached!!
    private static final String errorStyle = ".notification-error-style { "
            + "  color: red;"
            + " }";

    private static void showList(String windowTitle, String listTitle, Collection<String> items, ValueListener valueListener) {
        ListSelectionDialog newWindow = new ListSelectionDialog(windowTitle, listTitle, items, valueListener);
        // TODO 14 show list
//        newWindow.center();
//        newWindow.setModal(true);
//
//        UI.getCurrent().addWindow(newWindow);
//        newWindow.focus();
    }

    public static void show(String caption, Exception e) {
        String errorMessage = getRoot(e).getMessage();
        Div content = new Div();
        content.addClassName("notification-error-style");
        content.setText(caption + " " + errorMessage);

        Notification notification = new Notification(content);
        notification.setDuration(3000);
    }

    // TODO can't move this to ButtonBuilder class right now because it's not accessible from webshared module.
    // TODO zzz raw type
    public static Button createButton(String label, String description, ComponentEventListener clickListener) {
        Button button = new Button(label);
        button.getElement().setProperty("title", description); // tooltip
        button.addClickListener(clickListener);
        return button;
    }

    public static <T> TextField textInput(Binder<T> binder, String propertyName) {
        TextField result = new TextField();
        binder.bind(result, propertyName);
        return result;
    }

    public static <T> Checkbox checkbox(String label, String description, Binder<T> binder, String propertyName) {
        Checkbox field = new Checkbox(label);
        field.getElement().setProperty("title", description); // tooltip
        binder.bind(field, propertyName);
        return field;
    }

    public static <T> PasswordField passwordInput(Binder<T> binder, String propertyName) {
        PasswordField result = new PasswordField();
        binder.bind(result, propertyName);
        return result;
    }

    // TODO review and refactor this. this method is too complex
	public static Button createLookupButton(
            final String buttonLabel,
            String description, final String windowTitle,
            final String listTitle,
            final DataProvider<List<? extends NamedKeyedObject>> operation,
            final ExceptionFormatter errorFormatter,
            Function<NamedKeyedObject, Void> selectionListener) {

        final LookupResultListener listener = new LookupResultListener() {
            @Override
            public void notifyDone(List<? extends NamedKeyedObject> objects) {
                if (!objects.isEmpty()) {
                    showValues(objects);
                }
            }

            private void showValues(List<? extends NamedKeyedObject> objects) {
                final Map<String, String> map = new TreeMap<>();
                for (NamedKeyedObject o : objects) {
                    map.put(o.getName(), o.getKey());
                }

                showList(windowTitle, listTitle, map.keySet(), value -> {
                    String key = map.get(value);
                    selectionListener.apply(new NamedKeyedObjectImpl(key, value));
                });
            }
        };
        Button button = new Button(buttonLabel, event -> {
            final List<? extends NamedKeyedObject> objects;
            try {
                objects = operation.loadData();

                if (objects.isEmpty()) {
                    Notification.show("No objects have been found");
                }
                listener.notifyDone(objects);
            } catch (BadConfigException e) {
                logger.error(e.getMessage());
                Notification.show(errorFormatter.formatError(e));
            } catch (ConnectorException e) {
                logger.error(e.toString());
                Notification.show(errorFormatter.formatError(e));
            } catch (Exception e) {
                logger.error(e.toString());
                EditorUtil.show("Something went wrong", e);
            }
        });
        button.getElement().setProperty("title", description); // tooltip
        return button;
    }
	
    public interface ValueListener {
        void setValue(String value);
    }

    public static Throwable getRoot(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }
}
