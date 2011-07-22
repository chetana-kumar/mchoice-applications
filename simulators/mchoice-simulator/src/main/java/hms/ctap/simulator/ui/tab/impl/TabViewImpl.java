/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hms.ctap.simulator.ui.tab.impl;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.*;
import hms.ctap.simulator.ui.services.NcsService;
import hms.ctap.simulator.ui.services.NcsUIService;
import hms.ctap.simulator.ui.tab.TabView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author hms
 */
public class TabViewImpl extends TabView {

    private static final int REFRESH_INTERVAL = 4000;

    private ScheduledExecutorService executorService;

    final private Table sentMessageTable;
    final private Table receivedMessageTable;
    final private Button sentMsgClearButton;
    final private Button receiveMsgClearButton;
    final private NcsUIService ncsUIService;
    final private Label phoneImageNumLabel;
    final private Label phoneImageMessageLabel;
    private Refresher refresher;


    public TabViewImpl(NcsUIService ncsUIService) {
        init();
        this.ncsUIService = ncsUIService;
        sentMessageTable = ncsUIService.createSentMessageService();
        receivedMessageTable = ncsUIService.createReceivedMessageService();

        sentMsgClearButton = ncsUIService.createClearSentMessagesButton();
        receiveMsgClearButton = ncsUIService.createClearReceivedMessagesButton();

        phoneImageNumLabel = new Label();
        phoneImageMessageLabel = new Label();
        phoneImageNumLabel.setWidth("98px");
        phoneImageNumLabel.setStyleName("address-display");
        phoneImageMessageLabel.setContentMode(Label.CONTENT_RAW);
        phoneImageMessageLabel.setStyleName("message-display");
        refresher = new Refresher();
        refresher.setRefreshInterval(REFRESH_INTERVAL);
    }

    public void init() {
        super.init();
        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    try {
                        final NcsService ncsService = ncsUIService.getNcsService();
                        List receivedMessages = ncsService.receivedMessages();
                        for (int i = 0, receivedMessagesSize = receivedMessages.size(); i < receivedMessagesSize; i++) {
                            ncsUIService.addElementToReceiveTable(i, receivedMessages.get(i));
                        }
                        if (receivedMessages.size() > 0) {
                            ncsService.updatePhoneView(phoneImageNumLabel, phoneImageMessageLabel, receivedMessages.get(receivedMessages.size() - 1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 4, 4, TimeUnit.SECONDS);
        }
    }

    @Override
    public Button createSendMsgButton() {

        Button sendMsgButton = new Button("Send");
        sendMsgButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                final String address = encryptAddress(getPhoneNoField().getValue().toString());
                final String message = getMessageField().getValue().toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                try {
                    final String myNumber = getUserNumberTextField().getValue().toString();
                    ncsUIService.getNcsService().sendMessage(myNumber, address, message);
                    ncsUIService.addElementToSentTable(dateFormat.format(new Date()), address, message, "Success");
                } catch (Exception e) {
                    ncsUIService.addElementToSentTable(dateFormat.format(new Date()), address, message, "Failed");
                    e.printStackTrace();
                }
            }
        });
        return sendMsgButton;
    }

    /**
     *
     * @param phoneNo
     * @return  the MD5 checksum of the phoneNo
     */
    private String encryptAddress(String phoneNo) {
        String encryptedAddress = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(phoneNo.getBytes());
            for (int i=0; i < bytes.length; i++) { //converting byte array to hex string
                encryptedAddress += Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encryptedAddress;
    }

    @Override
    public Component getTabLayout() {

        VerticalLayout tabLayout = new VerticalLayout();
        tabLayout.setMargin(true);
        tabLayout.setSpacing(true);

        HorizontalLayout tabUpperLayout = new HorizontalLayout();
        tabUpperLayout.setWidth("100%");
        tabUpperLayout.setMargin(true);

        Component mobilePhoneLayout = createMobilePhone();
        tabUpperLayout.addComponent(mobilePhoneLayout);
        tabUpperLayout.setComponentAlignment(mobilePhoneLayout, Alignment.BOTTOM_LEFT);

        Component inputFieldPanel = createInputPanel();
        tabUpperLayout.addComponent(inputFieldPanel);
        tabUpperLayout.setComponentAlignment(inputFieldPanel, Alignment.TOP_RIGHT);
        tabLayout.addComponent(tabUpperLayout);

        VerticalLayout tableLayout = new VerticalLayout();
        tableLayout.setSpacing(true);
        tableLayout.setMargin(true);
        tableLayout.setWidth("100%");

        VerticalLayout receivedOuterLayout = new VerticalLayout();
        receivedOuterLayout.setWidth("100%");
        receivedOuterLayout.setMargin(false);
        receivedOuterLayout.setSpacing(false);
        receivedOuterLayout.setStyleName("table-outer-layout");

        HorizontalLayout receivedMessageTableLayout = new HorizontalLayout();
        receivedMessageTableLayout.setHeight("220px");
        receivedMessageTableLayout.setWidth("100%");
        receivedMessageTableLayout.setMargin(false);
        receivedMessageTableLayout.addComponent(receivedMessageTable);
        receivedOuterLayout.addComponent(receivedMessageTableLayout);

        HorizontalLayout receivedBottomLayout = new HorizontalLayout();
        receivedBottomLayout.setHeight("8px");
        receivedBottomLayout.setWidth("100%");
        receivedBottomLayout.setStyleName("bottom-layout");
        receivedOuterLayout.addComponent(receivedBottomLayout);

        receivedOuterLayout.addComponent(receiveMsgClearButton);

        VerticalLayout sentOuterLayout = new VerticalLayout();
        sentOuterLayout.setWidth("100%");
        sentOuterLayout.setMargin(false);
        sentOuterLayout.setSpacing(false);
        sentOuterLayout.setStyleName("table-outer-layout");

        HorizontalLayout sentMessageTableLayout = new HorizontalLayout();
        sentMessageTableLayout.setHeight("220px");
        sentMessageTableLayout.setWidth("100%");
        sentMessageTableLayout.setMargin(false);
        sentMessageTableLayout.addComponent(sentMessageTable);
        sentOuterLayout.addComponent(sentMessageTableLayout);

        HorizontalLayout sentBottomLayout = new HorizontalLayout();
        sentBottomLayout.setHeight("8px");
        sentBottomLayout.setWidth("100%");
        sentBottomLayout.setStyleName("bottom-layout");
        sentOuterLayout.addComponent(sentBottomLayout);

        sentOuterLayout.addComponent(sentMsgClearButton);

        tableLayout.addComponent(receivedOuterLayout);
        tableLayout.addComponent(sentOuterLayout);
        tableLayout.addComponent(refresher);
        tabLayout.addComponent(tableLayout);

        return tabLayout;
    }


    /**
     * @return a vertical layout containing mobile phone image
     */
    public Component createMobilePhone() {

        VerticalLayout backgroundLayout = new VerticalLayout();
        backgroundLayout.setWidth("119px");
        backgroundLayout.setHeight("236px");
        backgroundLayout.setStyleName("mobile-phone-background");

        VerticalLayout displayLayout = new VerticalLayout();
        displayLayout.setWidth("98px");
        displayLayout.addComponent(phoneImageNumLabel);

        HorizontalLayout messageLayout = new HorizontalLayout();
        messageLayout.setWidth("98px");
        messageLayout.addComponent(phoneImageMessageLabel);
        messageLayout.setExpandRatio(phoneImageMessageLabel,1);

        displayLayout.addComponent(messageLayout);
        displayLayout.addComponent(refresher);
        backgroundLayout.addComponent(displayLayout);
        return backgroundLayout;
    }

}
