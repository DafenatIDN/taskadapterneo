package com.taskadapter.webui;

import com.vaadin.ui.*;

import java.util.List;

public class MessageDialog extends Window implements Button.ClickListener {

    HorizontalLayout layout = new HorizontalLayout();
    Callback callback;

    public MessageDialog(String caption, String question, List<String> answers, Callback callback) {
        super(caption);

        setModal(true);

        this.callback = callback;

        if (question != null) {
            addComponent(new Label(question));
        }

        createButtons(answers);
        addComponent(layout);
//        layout.setComponentAlignment(yes, Alignment.MIDDLE_CENTER);
//        layout.setComponentAlignment(no, Alignment.MIDDLE_CENTER);
    }

    private void createButtons(List<String> answers) {
        for (String answer : answers){
            Button yes = new Button(answer, this);
            layout.addComponent(yes);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        if (getParent() != null) {
            (getParent()).removeWindow(this);
        }
        callback.onDialogResult(((Button) clickEvent.getSource()).getCaption());
    }

    public interface Callback {
        public void onDialogResult(String answer);
    }
}

