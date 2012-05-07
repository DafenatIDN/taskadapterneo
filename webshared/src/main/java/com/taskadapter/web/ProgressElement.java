package com.taskadapter.web;

import com.vaadin.ui.*;

public class ProgressElement extends GridLayout {
    private ProgressIndicator indicator;
    private Label finishLabel;
    private boolean isStarted;

    public ProgressElement() {
        setColumns(1);
        setRows(1);

        setWidth("20px");
        setHeight("20px");

        indicator = new ProgressIndicator();
        indicator.setIndeterminate(true);
        indicator.setPollingInterval(300);
        indicator.setEnabled(false);

        finishLabel = new Label("\u2713");
        finishLabel.setWidth(null);
        finishLabel.addStyleName("progress-label");

        isStarted = false;
    }

    public void start() {
        if (!isStarted) {
            isStarted = true;

            removeAllComponents();
            addComponent(indicator, 0, 0);
            setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);

            ProgressItemThread progressThread = new ProgressItemThread();
            progressThread.start();
            indicator.setEnabled(true);
            indicator.setVisible(true);
        }
    }

    private void showAfterProgress() {
        removeAllComponents();
        addComponent(finishLabel, 0, 0);
        setComponentAlignment(finishLabel, Alignment.MIDDLE_CENTER);

        isStarted = false;
    }

    private class ProgressItemThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (getApplication()) {
                showAfterProgress();
            }
        }
    }
}