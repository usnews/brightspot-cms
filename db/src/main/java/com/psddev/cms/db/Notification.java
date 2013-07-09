package com.psddev.cms.db;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.util.MailMessage;
import com.psddev.dari.util.MailProvider;
import com.psddev.dari.util.SmsProvider;

@ToolUi.IconName("object-notification")
public abstract class Notification extends Record {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notification.class);

    protected String createMessage(Object object, ToolUser sender, Date date, ToolUser receiver) {
        return null;
    }

    protected MailMessage createEmail(Object object, ToolUser sender, Date date, ToolUser receiver) {
        String message = createMessage(object, sender, date, receiver);

        if (message == null) {
            return null;

        } else {
            return new MailMessage().
                    subject("[CMS " + getState().getType().getLabel() + "] " + State.getInstance(object).getLabel()).
                    bodyPlain(message);
        }
    }

    protected String createSms(Object object, ToolUser sender, Date date, ToolUser receiver) {
        return createMessage(object, sender, date, receiver);
    }

    public void processNotification(Object object, ToolUser sender, Date date) {
        for (ToolUser receiver : Query.
                from(ToolUser.class).
                where("notifications = ?", this).
                iterable(0)) {
            if (receiver.getNotifyVia() == null) {
                continue;
            }

            try {
                switch (receiver.getNotifyVia()) {
                    case EMAIL :
                        MailMessage email = createEmail(object, sender, date, receiver);

                        email.from("support@perfectsensedigital.com");
                        email.to(receiver.getEmail());
                        MailProvider.Static.getDefault().send(email);
                        break;

                    case SMS :
                        SmsProvider.Static.getDefault().send(
                                null,
                                receiver.getPhoneNumber(),
                                createSms(object, sender, date, receiver));
                        break;
                }

            } catch (RuntimeException error) {
                LOGGER.warn("Can't send notification!", error);
            }
        }
    }
}
