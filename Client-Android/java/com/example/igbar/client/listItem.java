package com.example.igbar.client;

/**
 * Created by A.Igbarea on 12/7/2017.
 */

class listItem {
    String type;
    String topic ;
    String message;
    String time;
    String ipAddress;
    Boolean checkBox;
    listItem (String type,String txtname ,String txtsup,String ipAddress,Boolean checkBox,String time)
    {
        this.type=type;
        this.topic=txtname;
        this.message=txtsup;
        this.ipAddress=ipAddress;
        this.checkBox=checkBox;
        this.time=time;

    }
    listItem (String forword)
    {
        String msg[] =forword.split(" ",5);

        this.type=msg[0];
        this.topic=msg[1];
        this.ipAddress=msg[2];
        this.time=msg[3];
        this.message=msg[4];
        this.checkBox=null;
    }


}