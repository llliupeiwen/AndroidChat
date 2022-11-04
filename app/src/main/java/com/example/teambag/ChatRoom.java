package com.example.teambag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teambag.adapter.MsgAdapter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatRoom extends AppCompatActivity implements View.OnClickListener{
    private List<Msg> msgList = new ArrayList<>();
    private MsgAdapter adapter;
    private Button back;                //退回键
    private RecyclerView recyclerView;  //对话显示框
    private EditText input_text;        //输入框
    private String imageId ;               //获取自己的头像
    private String his_imageId;            //获取对方发头像
    private String time;                //获取时间
    private Button send;                //发送按钮
    private String name;                //从主活动获取的名字
    private String hisName;             //对方昵称，从自定义的receive线程中获取，便于和自己的昵称区分
    private String content;             //获取对话内容
    private String ip;                  //获取ip
    private String port;                //获取端口号
    private Socket socketSend;          //套接字，用于绑定ip号和端口号便于计算机之间的传输消息
    private DataInputStream dis;        //码头
    private DataOutputStream dos;       //集装箱
    private String recMsg;
    boolean isRunning = false;          //判断线程是否运行
    boolean isSend = false;             //判断是否发送
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);
        //启动活动，并获取传递过来的信息
        Intent intent = getIntent();
        name=Main.userName;
        /*
        //传入键值得到数据
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        //取出int要指定key，还要设置默认值，当intent中没有该key对应的value时，返回设置的默认值
        imageId = intent.getIntExtra("imageId",0);
         */
        //测试数据
        hisName = intent.getStringExtra("his_name");
        //System.out.println("his_name:  "+hisName);
        ip = "192.168.31.102";
        port = "6666";
        imageId = Main.img;
        System.out.println("img:  "+imageId);
        his_imageId = intent.getStringExtra("his_img");

        //获取实例
        input_text = (EditText) findViewById(R.id.input_text);
        back = (Button) findViewById(R.id.back);
        send = (Button) findViewById(R.id.send);
        //注册监听器
        back.setOnClickListener(this);
        send.setOnClickListener(this);

        //将RecyclerView和list建立联系(建立与适配器关系)
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatRoom.this);
        recyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);

        //如果要连网的话就不能在主线程上操作，所以要另外开启一条线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("号码");
                    socketSend = new Socket(ip, Integer.parseInt(port));
                    isRunning = true;
                    dis = new DataInputStream(socketSend.getInputStream());
                    dos = new DataOutputStream(socketSend.getOutputStream());
                    System.out.println("打开dos");
                    System.out.println("发送定位数据");
                    dos.writeUTF(name);
                    //开一条线程接收服务器传来的信息
                    new Thread(new receive(), "接收线程").start();
                    System.out.println("打开线程");
                } catch (Exception e) {
                    Log.e("TAG",e.toString());
                    e.printStackTrace();
                    //为当前线程准备消息队列
                    Looper.prepare();
                    //Toast只有在主线程中能显示出来
                    Toast.makeText(ChatRoom.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    //开启循环取消息
                    Looper.loop();
                    finish();
                }
            }
        }).start();

    }

    //获取当前时间
    public String getCurrentTime(){
        Date d = new Date();
        //设置显示的时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        //以这种格式显示
        return sdf.format(d);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.send:
                //显示时间
                time = getCurrentTime();
                String content = input_text.getText().toString();
                //显示信息
                StringBuilder sb = new StringBuilder();
                sb.append(content);
                if(!"".equals(content)){  //发送信息不为空
                    isSend = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String content = input_text.getText().toString();
                            Log.d("ttw","发了一条消息");
                            System.out.println("content = "+content);
                            System.out.println("传入发送信息");
                            if(!"".equals(content) && isSend){
                                String date = getCurrentTime();
                                try {
                                    if(!"".equals(date) && !"".equals(name)){
                                        dos.writeUTF(date);
                                        dos.writeUTF(content);
                                        dos.writeUTF(name);
                                        dos.writeUTF(String.valueOf(imageId));
                                        dos.writeUTF(hisName);
                                        System.out.println("发送完了");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                isSend = false;
                            }
                        }
                    }).start();
                    Msg msg = new Msg(content,Msg.TYPE_SENT,time,name,imageId);   //发送消息
                    System.out.println("imageId = "+imageId);
                    msgList.add(msg);
                    //当有新消息时，刷新ListView中的消息
                    adapter.notifyItemInserted(msgList.size()-1);
                    //将LestView定位到最后一行
                    recyclerView.scrollToPosition(msgList.size()-1);
                    input_text.setText("");
                }else{
                    Toast.makeText(ChatRoom.this, "不可发送空信息！", Toast.LENGTH_SHORT).show();
                }
                sb.delete(0,sb.length());
                break;
            default:
                break;
        }
    }

    //子线程与主线程通过Handler来进行通信。子线程可以通过Handler来通知主线程进行UI更新。
    //Handler有两个主要的用途:(1)安排消息和可运行对象在将来的某个时间点执行;(2)将一个要在不同的线程上执行的动作编入队列。
    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(!recMsg.isEmpty()){
                System.out.println("更新消息");
                addNewMessage(content,Msg.TYPE_RECEIVED,time,hisName,his_imageId);  //刷新接收的消息
            }
        }
    };

    public void addNewMessage(String msg,int type,String time,String name,String his_imageId){
        Msg message = new Msg(msg,type,time,name,his_imageId);
        msgList.add(message);
        //当有消息时，通知列表有新的数据插入，刷新recyclerview中消息
        adapter.notifyItemInserted(msgList.size()-1);
        //将消息一直放在显示屏的底部不随意跑上去
        recyclerView.scrollToPosition(msgList.size()-1);
    }

    //接收线程
    class receive implements Runnable{
        @Override
        public void run() {
            recMsg = "";
            while(isRunning){
                System.out.println("开始接收线程receive");
                Msg msg = null;
                try {
                    time = dis.readUTF();
                    content = dis.readUTF();
                    hisName = dis.readUTF();
                    his_imageId = dis.readUTF();
                    System.out.println("接收信息 = "+content);
                    recMsg = hisName + time + content;
                    msg = new Msg(content,Msg.TYPE_RECEIVED,time,hisName,his_imageId);
                } catch (Exception e) {
                    System.out.println("接受失败");
                    e.printStackTrace();
                }
                //判断是否为空字符串
                if(!TextUtils.isEmpty(recMsg)){
                    System.out.println("查看名字"+msg.getName());
                    Message message = new Message();
                    message.obj = msg;
                    handler.sendMessage(message);
                }
            }
        }
    }

}
