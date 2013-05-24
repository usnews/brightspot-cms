package com.psddev.cms.tool.page;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "/toolUserTfa")
@SuppressWarnings("serial")
public class ToolUserTfa extends PageServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();
        boolean verifyError = false;

        if (page.isFormPost() &&
                page.param(String.class, "action-verify") != null) {
            if (!user.verifyTotp(page.param(int.class, "totpCode"))) {
                verifyError = true;

            } else {
                user.setTfaEnabled(!user.isTfaEnabled());
                user.save();

                page.writeStart("script", "type", "text/javascript");
                    page.writeRaw("window.location = window.location;");
                page.writeEnd();
                return;
            }

        } else if (page.param(String.class, "action-image") != null) {
            try {
                StringBuilder keyUri = new StringBuilder("otpauth://totp/");
                String companyName = page.getCmsTool().getCompanyName();

                if (!ObjectUtils.isBlank(companyName)) {
                    keyUri.append(StringUtils.encodeUri(companyName));
                    keyUri.append(StringUtils.encodeUri(" - "));
                }

                keyUri.append(StringUtils.encodeUri(user.getEmail()));
                keyUri.append("?secret=");
                keyUri.append(user.getTotpSecret());

                int size = 200;
                Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();

                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix matrix = writer.encode(keyUri.toString(), BarcodeFormat.QR_CODE, size, size, hintMap);
                BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

                image.createGraphics();

                Graphics2D graphics = (Graphics2D) image.getGraphics();

                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, size, size);
                graphics.setColor(Color.BLACK);

                for (int i = 0; i < size; ++ i) {
                    for (int j = 0; j < size; ++ j) {
                        if (matrix.get(i, j)) {
                            graphics.fillRect(i, j, 1, 1);
                        }
                    }
                }

                HttpServletResponse response = page.getResponse();
                ServletOutputStream output = response.getOutputStream();

                response.setContentType("image/png");
                ImageIO.write(image, "png", output);
                output.flush();

            } catch (Exception error) {
                throw new IllegalStateException(error);
            }

            return;
        }

        if (user.getTotpSecret() == null) {
            byte[] secret = new byte[20];

            RANDOM.nextBytes(secret);
            user.setTotpSecretBytes(secret);
            user.save();
        }

        page.writeHeader();
            page.writeStart("div",
                    "class", "widget",
                    "style", "overflow: hidden;");
                page.writeStart("h1", "class", "icon icon-key");
                    page.writeHtml(user.isTfaEnabled() ? "Disable" : "Enable");
                    page.writeHtml(" Two Factor Authentication");
                page.writeEnd();

                page.writeTag("img",
                        "src", page.url("", "action-image", true),
                        "style", "float: right; height: 200px; margin-left: 10px; width: 200px;");

                page.writeStart("div", "style", "margin-right: 210px;");
                    if (verifyError) {
                        page.writeStart("div", "class", "message message-error");
                            page.writeHtml("Code you've entered isn't valid! Try re-entering the code or re-scan the QR code.");
                        page.writeEnd();

                    } else {
                        page.writeStart("div", "class", "message message-info");
                            if (user.isTfaEnabled()) {
                                page.writeHtml("Enter the displayed code from your Google Authenticator to disable two factor authentication.");

                            } else {
                                page.writeHtml("Scan the QR code with your Google Authenticator and enter the displayed code to enable two factor authentication.");
                            }
                        page.writeEnd();
                    }
                page.writeEnd();

                page.writeStart("form",
                        "method", "post",
                        "action", page.url(""));
                    page.writeStart("div", "class", "inputContainer");
                        page.writeStart("div", "class", "inputLabel");
                            page.writeStart("label", "for", page.createId());
                                page.writeHtml("Code");
                            page.writeEnd();
                        page.writeEnd();

                        page.writeStart("div", "class", "inputSmall");
                            page.writeTag("input",
                                    "type", "text",
                                    "id", page.getId(),
                                    "name", "totpCode");
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("div", "class", "actions");
                        page.writeStart("button",
                                "class", "action icon icon-action-save",
                                "name", "action-verify",
                                "value", true);
                            page.writeHtml("Verify");
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
