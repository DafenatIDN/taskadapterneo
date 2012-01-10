package com.taskadapter.connector.msp;

import com.taskadapter.web.FileManager;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author Alexey Skorokhodov
 */
public class RemoteModePanel extends VerticalLayout {
    public static final int MAX_FILE_SIZE = 1000000;  // 1 mb max

    private Label status = new Label("Please select a file to upload");
    private ProgressIndicator pi = new ProgressIndicator();

    private MyReceiver receiver = new MyReceiver();
    private Upload upload = new Upload("", receiver);
    private HorizontalLayout progressLayout = new HorizontalLayout();
    private UploadListener uploadListener;

    public RemoteModePanel(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
        buildUI();
    }

    private void buildUI() {
        // Slow down the upload
        receiver.setSlow(true);

        addComponent(status);
        addComponent(upload);
        addComponent(progressLayout);

        // Make uploading start immediately when file is selected
        upload.setImmediate(true);
        upload.setButtonCaption("Upload");

        progressLayout.setSpacing(true);
        progressLayout.setVisible(false);
        progressLayout.addComponent(pi);
        progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

        final Button cancelProcessing = new Button("Cancel");
        cancelProcessing.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                upload.interruptUpload();
            }
        });
        cancelProcessing.setStyleName("small");
        progressLayout.addComponent(cancelProcessing);

        /**
         * =========== Add needed listener for the upload component: start,
         * progress, finish, success, fail ===========
         */

        upload.addListener(new Upload.StartedListener() {
            public void uploadStarted(StartedEvent event) {
                // This method gets called immediately after upload is started
                upload.setVisible(false);
                progressLayout.setVisible(true);
                pi.setValue(0f);
                pi.setPollingInterval(500);
                status.setValue("Uploading file \"" + event.getFilename()
                        + "\"");
            }
        });

        upload.addListener(new Upload.ProgressListener() {
            public void updateProgress(long readBytes, long contentLength) {
                // This method gets called several times during the update
                pi.setValue(new Float(readBytes / (float) contentLength));
            }

        });

        upload.addListener(new Upload.SucceededListener() {
            public void uploadSucceeded(SucceededEvent event) {
                // This method gets called when the upload finished successfully
                status.setValue("Uploading file \"" + event.getFilename()
                        + "\" succeeded");
                MyReceiver receiver1 = (MyReceiver) event.getUpload().getReceiver();
                saveFile(receiver.getFileName(), receiver1.getBytes());
                uploadListener.fileUploaded(receiver.getFileName());
            }
        });

        upload.addListener(new Upload.FailedListener() {
            public void uploadFailed(FailedEvent event) {
                // This method gets called when the upload failed
                status.setValue("Uploading interrupted");
            }
        });

        upload.addListener(new Upload.FinishedListener() {
            public void uploadFinished(FinishedEvent event) {
                // This method gets called always when the upload finished,
                // either succeeding or failing
                progressLayout.setVisible(false);
                upload.setVisible(true);
                upload.setCaption("Select another file");
            }
        });


    }

    private void saveFile(String fileName, byte[] bytes) {
        try {
            new FileManager().saveFileOnServer(fileName, bytes);
        } catch (IOException e) {
            getWindow().showNotification("Error: "+ e.toString());
        }
    }

    public static class MyReceiver implements Upload.Receiver {

        private String fileName;
        private String mtype;
        private boolean sleep;
        private int total;
        private byte[] bytes = new byte[MAX_FILE_SIZE];

        public OutputStream receiveUpload(String filename, String mimetype) {
            total = 0;
            fileName = filename;
            mtype = mimetype;
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    bytes[total++] = (byte) b;
                    if (sleep && total % 10000 == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (total== MAX_FILE_SIZE) {
                        throw new RuntimeException("Max file size reached: "  + MAX_FILE_SIZE + " bytes");
                    }
                }
            };
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mtype;
        }

        public void setSlow(boolean value) {
            sleep = value;
        }

        public byte[] getBytes() {
            return Arrays.copyOf(bytes, total);
        }
    }

}