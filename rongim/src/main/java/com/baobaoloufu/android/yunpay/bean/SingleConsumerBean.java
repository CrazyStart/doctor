package com.baobaoloufu.android.yunpay.bean;

public class SingleConsumerBean {

    /**
     * status : 1
     * text : 医生赠送服务中
     * record : 5f2ad4fdb8eeb3331bb36e29
     * remain_count : 1
     * name : 患者顾
     * avatar : https://ent-avatar.oss-cn-hangzhou.aliyuncs.com/defalutAvatar.png
     */

    private int status;
    private String text;
    private String record;
    protected String expiryDate;
    private int remain_count;
    private String name;
    private int type;
    private String avatar;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public int getRemain_count() {
        return remain_count;
    }

    public void setRemain_count(int remain_count) {
        this.remain_count = remain_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
