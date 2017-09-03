package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.CertificationModel;
import net.glxn.qrgen.QRCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ImageCertificationGenerator implements CertificationGenerator, ImageObserver {
    private static Map<Style, BufferedImage> certificationImages = new HashMap<>();

    private Style style;

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
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
        DEFAULT(new LayoutOffset(0, 0));

        LayoutOffset offset;

        Style(LayoutOffset offset) {
            this.offset = offset;
        }

        public LayoutOffset getOffset() {
            return offset;
        }
    }

    static {
        try {
            BufferedImage bgImage = ImageIO.read(FormServiceImpl.class.getClassLoader().getResourceAsStream("static/cert-bg.jpg"));
            certificationImages.put(Style.DEFAULT, bgImage);
        } catch (IOException e) {}
    }

    public ImageCertificationGenerator(Style style) {
        this.style = style;
    }

    private BufferedImage getBgImage() {
        return certificationImages.get(style);
    }

    private LayoutOffset getOffset() {
        return style.getOffset();
    }

    @Override
    public void generate(CertificationModel model, OutputStream output) throws IOException {
        Graphics2D g2d = (Graphics2D) getBgImage().getGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        drawTitle(model.getTitle(), g2d);

        drawSubTitle(model.getSubTitle(), g2d);

        drawOwner(model.getOwner(), g2d);

        drawBody(model.getContent(), g2d);

        if (model.getCommentContent() != null && model.getCommentLabel() != null) {
            drawCommentLabel(model.getCommentLabel(), g2d);
            drawComment(model.getCommentContent(), g2d);
        }

        drawIssuer(model.getIssuerLabel(), model.getIssuer(), g2d);

        drawDate(model.getDate(), g2d);

        drawQRCode(model.getUrl(), g2d);

        ImageIO.write(getBgImage(), "JPG", output);
    }

    private int getHorizontalCenter(String text, Graphics graphics) {
        return getBgImage().getWidth()/2 - graphics.getFontMetrics().stringWidth(text)/2 + getOffset().getLeft();
    }

    private void drawTitle(String title, Graphics2D g2d) {
        final int fontSize = 30;

        g2d.setFont(new Font("宋体", Font.PLAIN, fontSize));

        g2d.drawString(title, getHorizontalCenter(title, g2d),  120 + getOffset().getTop());
    }

    private void drawSubTitle(String subTitle, Graphics2D g2d) {
        final int fontSize = 20;

        g2d.setFont(new Font("宋体", Font.PLAIN, fontSize));

        g2d.drawString(subTitle, getHorizontalCenter(subTitle, g2d), 150 + getOffset().getTop());
    }

    private void drawOwner(String owner, Graphics2D g2d) {
        final int fontSize = 25;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(owner + ":", 160 + getOffset().getLeft(), 200 + getOffset().getTop());
    }

    private void drawCommentLabel(String label, Graphics2D g2d) {
        final int fontSize = 18;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(label + ":", 160 + getOffset().getLeft(), 400 + getOffset().getTop());
    }

    private void drawComment(String comment, Graphics2D g2d) {
        final int fontSize = 18;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(comment, 160 + getOffset().getLeft(), 450 + getOffset().getTop());
    }

    private void drawIssuer(String label, String issuer, Graphics2D g2d) {
        final int fontSize = 18;

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        final int textWidth = Math.max(g2d.getFontMetrics().stringWidth(label), g2d.getFontMetrics().stringWidth(issuer));

        g2d.drawString(label + ":", getBgImage().getWidth() - textWidth - 200 + getOffset().getLeft(), 400 + getOffset().getTop());

        g2d.drawString(issuer, getBgImage().getWidth() - textWidth - 200 + getOffset().getLeft(), 450 + getOffset().getTop());
    }

    private void drawQRCode(String url, Graphics2D g2d) throws IOException {
        BufferedImage qrCode = ImageIO.read( QRCode.from(url).file());

        g2d.drawImage(qrCode, 150 + getOffset().getLeft(), 520 + getOffset().getTop(), 100, 100, this);
    }

    private void drawDate(Date date, Graphics2D g2d) {
        final int fontSize = 18;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        g2d.setFont(new Font("宋体", Font.BOLD, fontSize));

        g2d.drawString(year + "年 " + month + "月 " + day + "日", 160 + getOffset().getLeft(), 500 + getOffset().getTop());
    }

    private void drawBody(String body, Graphics2D g2d) {
        final int fontSize = 20;

        final int maxLineWidth = new Double(getBgImage().getWidth() * 0.7).intValue();

        final int lineHeight = 30;

        int lineTop = 260 + getOffset().getTop();

        StringBuilder sb = new StringBuilder();

        g2d.setFont(new Font("宋体", Font.PLAIN, fontSize));

        if (g2d.getFontMetrics().stringWidth(body) > maxLineWidth) {
            for (char c : body.toCharArray()) {
                sb.append(c);
                if (g2d.getFontMetrics().stringWidth(sb.toString()) >= maxLineWidth) {
                    g2d.drawString(sb.toString(), 160 + getOffset().getLeft(), lineTop);
                    sb.delete(0, sb.length());
                    lineTop += lineHeight;
                }
            }

            if (sb.length() > 0) {
                g2d.drawString(sb.toString(), 160 + getOffset().getLeft(), lineTop);
            }

        } else {
            g2d.drawString(body, 160 + getOffset().getLeft(), lineTop);
        }
    }
}
