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
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teambag.adapter.MsgAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
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
    private String hisNumber;           //对方账号，用于存储历史消息
    private String content;             //获取对话内容
    private String ip;                  //获取ip
    private String port;                //获取端口号
    private Socket socketSend;          //套接字，用于绑定ip号和端口号便于计算机之间的传输消息
    private DataInputStream dis;        //码头
    private DataOutputStream dos;       //集装箱
    private String recMsg;
    private String[] targetList;
    private String[] contentList;
    private String[] timeList;

    private int i;

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
        hisNumber = intent.getStringExtra("his_number");

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
                    httpUrlConnPost(String.valueOf(hisNumber),String.valueOf(Main.number));
                    socketSend = new Socket(ip, Integer.parseInt(port));
                    isRunning = true;
                    dis = new DataInputStream(socketSend.getInputStream());
                    dos = new DataOutputStream(socketSend.getOutputStream());
                    System.out.println("打开dos");
                    System.out.println("发送定位数据");
                    dos.writeUTF(Main.number);
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

    public void httpUrlConnPost(String target,String user) {
        HttpURLConnection urlConnection = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        URL url;
        try {
            // 请求的URL地地址
            url = new URL(
                    "http://192.168.31.102:8808/AndroidServer_war_exploded/ChatMessage");
            urlConnection = (HttpURLConnection) url.openConnection();// 打开http连接
            urlConnection.setConnectTimeout(3000);// 连接的超时时间
            urlConnection.setUseCaches(false);// 不使用缓存
            // urlConnection.setFollowRedirects(false);是static函数，作用于所有的URLConnection对象。
            urlConnection.setInstanceFollowRedirects(true);// 是成员函数，仅作用于当前函数,设置这个连接是否可以被重定向
            urlConnection.setReadTimeout(3000);// 响应的超时时间
            urlConnection.setDoInput(true);// 设置这个连接是否可以写入数据
            urlConnection.setDoOutput(true);// 设置这个连接是否可以输出数据
            urlConnection.setRequestMethod("POST");// 设置请求的方式
            urlConnection.setRequestProperty("Content-Type",
                    "application/json;charset=UTF-8");// 设置消息的类型
            urlConnection.connect();// 连接，从上述至此的配置必须要在connect之前完成，实际上它只是建立了一个与服务器的TCP连接
            JSONObject json = new JSONObject();// 创建json对象
            //json.put("title", URLEncoder.encode(title, "UTF-8"));// 使用URLEncoder.encode对特殊和不可见字符进行编码
            json.put("target", URLEncoder.encode(target, "UTF-8"));// 把数据put进json对象中
            json.put("user", URLEncoder.encode(user, "UTF-8"));// 把数据put进json对象中
            String jsonstr = json.toString();// 把JSON对象按JSON的编码格式转换为字符串
            // ------------字符流写入数据------------
            OutputStream out = urlConnection.getOutputStream();// 输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));// 创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
            bw.write(jsonstr);// 把json字符串写入缓冲区中
            bw.flush();// 刷新缓冲区，把数据发送出去，这步很重要
            out.close();
            bw.close();// 使用完关闭
            Log.i("aa", urlConnection.getResponseCode()+"");
            //以下判斷是否訪問成功，如果返回的状态码是200则说明访问成功
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {// 得到服务端的返回码是否连接成功
                // ------------字符流读取服务端返回的数据------------
                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in));
                String str = null;
                StringBuffer buffer = new StringBuffer();
                while ((str = br.readLine()) != null) {// BufferedReader特有功能，一次读取一行数据
                    System.out.println("测试：" + str);
                    buffer.append(str);
                }
                in.close();
                br.close();
                JSONObject rjson = new JSONObject(buffer.toString());
                String str1=new String();
                String str2=new String();
                String str3=new String();
                for(int i=0;i<rjson.getJSONArray("json").length();i++){
                    str1 += rjson.getJSONArray("json").getJSONObject(i).get("target").toString();
                    str1 += "\r\n";
                    str2 += rjson.getJSONArray("json").getJSONObject(i).get("content").toString();
                    str2 += "\r\n";
                    str3 += new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rjson.getJSONArray("json").getJSONObject(i).get("time").toString()));
                    str3 += "\r\n";
                }
                targetList = str1.split("\r\n");
                contentList = str2.split("\r\n");
                timeList = str3.split("\r\n");
                boolean result = rjson.getBoolean("j1");// 从rjson对象中得到key值为"json"的数据，这里服务端返回的是一个boolean类型的数据
                System.out.println("json:===" + result);
                //如果服务器端返回的是true，则说明成功，否则失败
                if (result) {// 判断结果是否正确
                    for(i=0;i< targetList.length;i++){
                        System.out.println("TargetNumber:  "+targetList[i]);
                        Msg msg;
                        if(targetList[i].equals(Main.number)){
                            System.out.println("历史收到的信息");
                            msg=new Msg(contentList[i],Msg.TYPE_RECEIVED,timeList[i],hisName,his_imageId);
                        }else{
                            System.out.println("历史发送的信息");
                            msg=new Msg(contentList[i],Msg.TYPE_SENT,timeList[i],name,imageId);
                        }
                        msgList.add(msg);
                    }
                    h.sendMessage(Message.obtain());
                } else {
                    System.out.println("查询失败");
                    Log.i("用户：", "登录失败");
                }
            } else {
                System.out.println("连接失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("aa", e.toString());
            System.out.println("11111111111111111");
        } finally {
            urlConnection.disconnect();// 使用完关闭TCP连接，释放资源
        }
    }

    //获取当前时间
    public String getCurrentTime(){
        Date d = new Date();
        //设置显示的时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
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
                                        dos.writeUTF(hisNumber);
                                        dos.writeUTF(Main.number);
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
                    //System.out.println("imageId = "+imageId);
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
    private Handler h = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            for(int j=0;j<=i;j++){
                //当有消息时，通知列表有新的数据插入，刷新recyclerview中消息
                adapter.notifyItemInserted(msgList.size()-1-j);
                //将消息一直放在显示屏的底部不随意跑上去
                recyclerView.scrollToPosition(msgList.size()-1-j);
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果是返回按钮
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
