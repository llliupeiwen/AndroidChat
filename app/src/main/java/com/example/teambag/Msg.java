package com.example.teambag;

public class Msg {
    public static final int TYPE_RECEIVED = 0;	//标记用于判断是输出消息还是接收消息
    public static final int TYPE_SENT = 1;
    private String content;		//存储消息信息
    private int type;	//存储判断信息
    private String time;
    private String name;
    private String imageId;

    public Msg(String content, int type, String time,String name,String imageId) {
        this.content = content;
        this.type = type;
        this.time = time;
        this.name = name;
        this.imageId = imageId;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getTime(){
        return time;
    }

    public String getName(){
        return name;
    }

    public String getImageId(){
        return imageId;
    }
}

