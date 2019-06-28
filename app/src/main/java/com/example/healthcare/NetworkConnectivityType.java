package com.example.healthcare;

public enum NetworkConnectivityType {
    NO_NETWORK(0){@Override public String toString(){return "no internet";}}
    ,MOBILE(1){@Override public String toString(){return "mobile";}}
    ,WIFI(2){@Override public String toString() {
            return "wifi";
        }};

    private final int type;
    NetworkConnectivityType(final int type){
        this.type=type;
    }
    public boolean ifHaveInternet(){
        return type>0;
    }
}
