package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.common.OSSBucketAccessor;
import com.stardust.easyassess.assessment.models.CertificationModel;
import com.stardust.easyassess.assessment.models.form.Form;
import net.glxn.qrgen.QRCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.URL;
import java.util.*;

public class ImageCertificationGenerator implements CertificationGenerator, ImageObserver {
    private Map<Style, BufferedImage> certificationImages = new HashMap<>();

    private Style style;

    @Override
    public boolean imageUpdate(Image img, int infoFlags, int x, int y, int width, int height) {
        return true;
    }

    private static class LayoutOffset {
        private int left;

        private int top;

        public LayoutOffset(int left, int top) {
            this.left = left;
            this.top = top;
        }

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }
    }

    public enum Style {
        DEFAULT(new LayoutOffset(30, 60));

        LayoutOffset offset;

        Style(LayoutOffset offset) {
            this.offset = offset;
        }

        public LayoutOffset getOffset() {
            return offset;
        }
    }

    public ImageCertificationGenerator(Style style) throws IOException {
        this.style = style;
        BufferedImage bgImage = ImageIO.read(FormServiceImpl.class.getClassLoader().getResourceAsStream("static/cert-bg.jpg"));
        certificationImages.put(Style.DEFAULT, bgImage);
    }

    private BufferedImage getBgImage() {
        return certificationImages.get(style);
    }

    private LayoutOffset getOffset() {
        return style.getOffset();
    }

    @Override
    public URL generate(CertificationModel model) throws IOException {
        Graphics2D g2d = (Graphics2D) getBgImage().getGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        drawTitle(model.getTitle(), g2d);

        drawSubTitle(model.getSubTitle(), g2d);

        drawOwner(model.getOwner(), g2d);

        drawBody(model.getContent(), g2d);

        if (model.getCommentContent() != null
                && model.getCommentLabel() != null) {
            drawCommentLabel(model.getCommentLabel(), g2d);
            drawComment(model.getCommentContent(), g2d);
        }

        drawIssuer(model.getIssuerLabel(), model.getIssuer(), model.getDate(), model.getSignatureUrl(), g2d);

        if (model.getUrl() != null
                && !model.getUrl().isEmpty()) {
            drawQRCode(model.getUrl(), g2d);
        }

        if ( model.getForm() != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(getBgImage(), "JPG", os);
            InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
            return new URL(new OSSBucketAccessor().put("assess-bucket", "ministry-certs/cert_" + model.getForm().getAssessment().getOwnerName() + "_" + model.getForm().getAssessment().getName() + "_" + model.getForm().getOwnerName() + "_" +model.getForm().getId() + ".jpg", inputStream));
        }

        return null;
    }

    @Override
    public InputStream getCertification(Form form) {
        if (form != null) {
            String certUrl = "http://assess-bucket.oss-cn-beijing.aliyuncs.com/ministry-certs/cert_" + form.getAssessment().getOwnerName() + "_" + form.getAssessment().getName() + "_" + form.getOwnerName() + "_" + form.getId() + ".jpg";
            try {
                return new URL(certUrl).openStream();
            } catch (IOException e) {}
        }

        return null;
    }

    @Override
    public void printCertification(CertificationModel model, OutputStream outputStream) throws IOException {
        InputStream inputStream = getCertification(model.getForm());
        if (inputStream == null) {
            generate(model);
            ImageIO.write(getBgImage(), "JPG" ,outputStream);
        } else {
            ImageIO.write(ImageIO.read(inputStream), "JPG" ,outputStream);
        }
    }

    private int getHorizontalCenter(String text, Graphics graphics) {
        return getBgImage().getWidth()/2 - graphics.getFontMetrics().stringWidth(text)/2 + getOffset().getLeft();
    }

    private void drawTitle(String title, Graphics2D g2d) {
        final int fontSize = 34;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(title, getHorizontalCenter(title, g2d),  140 + getOffset().getTop());
    }

    private void drawSubTitle(String subTitle, Graphics2D g2d) {
        final int fontSize = 24;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(subTitle, getHorizontalCenter(subTitle, g2d), 170 + getOffset().getTop());
    }

    private void drawOwner(String owner, Graphics2D g2d) {
        final int fontSize = 31;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(owner + ":", 160 + getOffset().getLeft(), 220 + getOffset().getTop());
    }

    private void drawCommentLabel(String label, Graphics2D g2d) {
        final int fontSize = 22;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(label + ":", 160 + getOffset().getLeft(), 420 + getOffset().getTop());
    }

    private void drawComment(String comment, Graphics2D g2d) {
        final int fontSize = 22;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(comment, 160 + getOffset().getLeft(), 470 + getOffset().getTop());
    }

    private void drawIssuer(String label, String issuer, Date date, String url, Graphics2D g2d) {
        final int fontSize = 22;

        final int left = getBgImage().getWidth() - 410 + getOffset().getLeft();

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        // g2d.drawString(label + ":", left, 400 + getOffset().getTop());

        final int nextLineTop = drawWrapContent(issuer, g2d, 350, 30, 530 + getOffset().getTop(), left - 100) + 100;

        drawDate(date, g2d, getBgImage().getWidth() - 460, nextLineTop);

        BufferedImage signature;
        try {
            signature = ImageIO.read(new URL(url).openStream());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g2d.drawImage(signature, left - 50, 500 + getOffset().getTop() - 80, 200, 200, this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } catch (IOException e) {
        }
    }

    private void drawQRCode(String url, Graphics2D g2d) throws IOException {
        BufferedImage qrCode = ImageIO.read(QRCode.from(url).file());

        g2d.drawImage(qrCode, 180 + getOffset().getLeft(), 480 + getOffset().getTop(), 160, 160, this);
    }

    private void drawDate(Date date, Graphics2D g2d, int left, int top) {
        final int fontSize = 22;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(year + "年 " + month + "月 " + day + "日", left + getOffset().getTop() , top + getOffset().getTop());
    }

    private int drawWrapContent(String content, Graphics2D g2d, int maxLineWidth, int lineHeight, int lineTop, int lineLeft) {
        StringBuilder sb = new StringBuilder();

        if (g2d.getFontMetrics().stringWidth(content) > maxLineWidth) {
            for (char c : content.toCharArray()) {
                sb.append(c);
                if (g2d.getFontMetrics().stringWidth(sb.toString()) >= maxLineWidth) {
                    g2d.drawString(sb.toString(), lineLeft + getOffset().getLeft(), lineTop);
                    sb.delete(0, sb.length());
                    lineTop += lineHeight;
                }
            }

            if (sb.length() > 0) {
                g2d.drawString(sb.toString(), lineLeft + getOffset().getLeft(), lineTop);
            }

        } else {
            g2d.drawString(content, lineLeft + getOffset().getLeft(), lineTop);
        }

        return lineTop;
    }

    private void drawBody(String body, Graphics2D g2d) {
        final int fontSize = 24;

        final String indent = "       ";

        g2d.setFont(new Font("宋体", Font.PLAIN, fontSize));

        final int maxLineWidth = new Double(getBgImage().getWidth() * 0.7).intValue();

        final int lineHeight = 30;

        int lineTop = 260 + getOffset().getTop();

        drawWrapContent(indent + body, g2d, maxLineWidth, lineHeight, lineTop, 160);

//        StringBuilder sb = new StringBuilder();
//
//        if (g2d.getFontMetrics().stringWidth(body) > maxLineWidth) {
//            for (char c : body.toCharArray()) {
//                sb.append(c);
//                if (g2d.getFontMetrics().stringWidth(sb.toString()) >= maxLineWidth) {
//                    g2d.drawString(sb.toString(), 160 + getOffset().getLeft(), lineTop);
//                    sb.delete(0, sb.length());
//                    lineTop += lineHeight;
//                }
//            }
//
//            if (sb.length() > 0) {
//                g2d.drawString(sb.toString(), 160 + getOffset().getLeft(), lineTop);
//            }
//
//        } else {
//            g2d.drawString(body, 160 + getOffset().getLeft(), lineTop);
//        }
    }
}
