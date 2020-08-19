package com.baobaoloufu.android.yunpay.bean;

public class PriceBean {

    /**
     * priceInfo : {"price":50,"asked_price":20,"oneonone_price":50}
     */

    private PriceInfoBean priceInfo;

    public PriceInfoBean getPriceInfo() {
        return priceInfo;
    }

    public void setPriceInfo(PriceInfoBean priceInfo) {
        this.priceInfo = priceInfo;
    }

    public static class PriceInfoBean {
        /**
         * price : 50.0
         * asked_price : 20.0
         * oneonone_price : 50.0
         */

        private double price;
        private double asked_price;
        private double oneonone_price;

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getAsked_price() {
            return asked_price;
        }

        public void setAsked_price(double asked_price) {
            this.asked_price = asked_price;
        }

        public double getOneonone_price() {
            return oneonone_price;
        }

        public void setOneonone_price(double oneonone_price) {
            this.oneonone_price = oneonone_price;
        }
    }
}
