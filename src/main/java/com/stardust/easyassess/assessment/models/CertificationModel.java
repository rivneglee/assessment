package com.stardust.easyassess.assessment.models;


import com.stardust.easyassess.assessment.models.form.Form;

import java.util.Date;

public class CertificationModel {

    private String title;

    private String subTitle;

    private String owner;

    private String issuer;

    private String issuerLabel;

    private String commentLabel;

    private String commentContent;

    private String content;

    private Date date;

    private String url;

    private String signatureUrl;

    private Form form;

    private final String OSS_BUCKET_ENDPOINT = "http://assess-bucket.oss-cn-beijing.aliyuncs.com";

    public CertificationModel(Form form) {
        String signature = OSS_BUCKET_ENDPOINT + "/ministry-signature/signature_" + form.getAssessment().getOwner() + ".png";
        this.setTitle(form.getAssessment().getCertTitle());
        this.setSignatureUrl(signature);
        this.setSubTitle(form.getAssessment().getCertSubTitle());
        this.setOwner(form.getOwnerName());
        this.setIssuerLabel("颁发机构");
        this.setIssuer(form.getAssessment().getCertIssuer() != null ? form.getAssessment().getCertIssuer() : form.getAssessment().getOwnerName());
        this.setContent(form.getAssessment().getCertContent());
        this.setCommentLabel(form.getAssessment().getCertCommentLabel());
        this.setCommentContent(form.getAssessment().getCertCommentContent());
        this.setDate(form.getAssessment().getEndDate());
        this.setForm(form);
    }

    public CertificationModel() {

    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public String getOSS_BUCKET_ENDPOINT() {
        return OSS_BUCKET_ENDPOINT;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCommentLabel() {
        return commentLabel;
    }

    public void setCommentLabel(String commentLabel) {
        this.commentLabel = commentLabel;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIssuerLabel() {
        return issuerLabel;
    }

    public void setIssuerLabel(String issuerLabel) {
        this.issuerLabel = issuerLabel;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getCertName() {
        return getForm().getAssessment().getOwnerName() + "_" + getForm().getAssessment().getName() + "_" + getForm().getOwnerName() + "_" + getForm().getId() + ".jpg";
    }
}
