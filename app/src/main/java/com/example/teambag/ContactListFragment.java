package com.example.teambag;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.teambag.adapter.SortAdapter;
import com.example.teambag.tools.SideBar;
import com.example.teambag.tools.User;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("ValidFragment")
public class ContactListFragment extends Fragment {
    String[] imgUrl;
    String[] name;
    String[] friend;
    String hisNumber;
    private String number; //微信号，通过微信号去查找通讯录
    /* 声明组件*/
    private ListView listView;
    private SideBar sideBar;
    /*声明或创建集合，用于处理数据*/
    private ArrayList<User> list;
    private ArrayList<Integer> list2;
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    //自定义的一个Hander消息机制
    private MyHander myhander = new MyHander();

    @SuppressLint("ValidFragment")
    ContactListFragment(String number) {
        this.number = number;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*开启一个线程，用微信号向服务器请求通讯录数据*/
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                httpUrlConnPost(String.valueOf(number));
            }
        });
        thread1.start();
        /*等待线性处理完成*/
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //获取fragment布局
        View view = inflater.inflate(R.layout.contactlist_fragment, container, false);
        /*初始化组件*/
        listView = (ListView) view.findViewById(R.id.listView);
        sideBar = (SideBar) view.findViewById(R.id.side_bar);
        //初始化数据
        initData();
        sideBar.setOnStrSelectCallBack(new SideBar.ISideBarSelectCallBack() {
            @Override
            public void onSelectStr(int index, String selectStr) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getName() == "新的朋友" || list.get(i).getName() == "群聊" ||
                            list.get(i).getName() == "标签" || list.get(i).getName() == "公众号"  )
                        continue;
                    if (selectStr.equalsIgnoreCase(list.get(i).getFirstLetter())) {
                        listView.setSelection(i); // 选择到首字母出现的位置
                        return;
                    }
                }
            }
        });
        return view;
    }

    private void initData() {
        //把从服务器获取解析的数据添加到map中，方便处理
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < imgUrl.length; i ++) {
            map.put(name[i], imgUrl[i]);
        }
        data.add(map);
        //名字要提取出来在添加到list中，因为要进行字母排序
        list = new ArrayList<>();
        for (int i = 0; i < imgUrl.length; i++) {
            //System.out.println("i=  "+i);
            list.add(new User(name[i],friend[i]));
        }
        Collections.sort(list); // 对list进行排序，需要让User实现Comparable接口重写compareTo方法
        //四个标签排序后再进行添加，好进行条件判断分离出来
        list.add(0,new User("新的朋友"));
        list.add(1,new User("群聊"));
        list.add(2,new User("标签"));
        list.add(3,new User("公众号"));
        //四个标签图片不需要再服务器获取，直接移动端实现即可
        list2 = new ArrayList<>();
        list2.add(R.drawable.newfriend);
        list2.add(R.drawable.groupchat);
        list2.add(R.drawable.sign);
        list2.add(R.drawable.publicnum);
        /*创建自定义适配器，并设置给listview*/
        SortAdapter adapter = new SortAdapter(getActivity().getApplicationContext(), list, list2, data);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i<4)
                    return false;
                hisNumber=list.get(i).getNumber();
                PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.sample_menu,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.showFriend:
                                break;
                            case R.id.deleteFriend:
                                Thread thread2 = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        httpUrlConnPostToDelete(Main.number,hisNumber,i);
                                    }
                                });
                                thread2.start();
                                /*等待线性处理完成*/
                                try {
                                    thread2.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                adapter.notifyDataSetChanged();
                                break;
                            case R.id.chatFriend:
                                break;
                            case R.id.blockFriend:
                                break;
                        }
                        return true;
                    }
                });
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i<4)
                    return;
                String his_name=list.get(i).getName();
                Intent intent = new Intent(getActivity(),ChatRoom.class);
                for(int k =0;k<data.size();k++) {
                    if (data.get(k).get(his_name) != null) {
                        intent.putExtra("his_img", data.get(k).get(his_name));
                        break;
                    }
                }
                intent.putExtra("img",Main.img);
                intent.putExtra("his_name",his_name);
                intent.putExtra("his_number",list.get(i).getNumber());
                //页面跳转到登录界面
                startActivity(intent);
            }
        });
    }

    public void httpUrlConnPostToDelete(String number, String friend,int i){
        HttpURLConnection urlConnection = null;
        URL url;
        try {
            // 请求的URL地地址
            url = new URL(
                    "http://192.168.31.102:8808/AndroidServer_war_exploded/DeleteFriend");
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
            json.put("number", URLEncoder.encode(number, "UTF-8"));// 把数据put进json对象中
            json.put("friend",URLEncoder.encode(friend, "UTF-8"));
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
                boolean result = rjson.getBoolean("j1");// 从rjson对象中得到key值为"json"的数据，这里服务端返回的是一个boolean类型的数据
                //如果服务器端返回的是true，则说明注册成功，否则注册失败
                if (result) {// 判断结果是否正确
                    //在Android中http请求，必须放到线程中去作请求，但是在线程中不可以直接修改UI，只能通过hander机制来完成对UI的操作
                    myhander.sendEmptyMessage(1);
                    list.remove(i);
                    Log.i("用户：", "删除好友成功");
                } else {
                    myhander.sendEmptyMessage(2);
                    Log.i("用户：", "删除好友失败");
                }
            } else {
                myhander.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("aa", e.toString());
            myhander.sendEmptyMessage(2);
        } finally {
            urlConnection.disconnect();// 使用完关闭TCP连接，释放资源
        }
    }

    // 1.编写一个发送请求的方法
    // 发送请求的主要方法
    public void httpUrlConnPost(String number) {
        HttpURLConnection urlConnection = null;
        URL url;
        try {
            // 请求的URL地地址
            url = new URL(
                    "http://192.168.31.102:8808/AndroidServer_war_exploded/Contact");
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
            json.put("number", URLEncoder.encode(number, "UTF-8"));// 把数据put进json对象中
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
                    str1 += rjson.getJSONArray("json").getJSONObject(i).get("img").toString();
                    str1 += "\r\n";
                    str2 += rjson.getJSONArray("json").getJSONObject(i).get("name").toString();
                    str2 += "\r\n";
                    str3 += rjson.getJSONArray("json").getJSONObject(i).get("friend").toString();
                    str3 += "\r\n";
                }
                imgUrl = str1.split("\r\n");
                name = str2.split("\r\n");
                friend = str3.split("\r\n");
                boolean result = rjson.getBoolean("j1");// 从rjson对象中得到key值为"json"的数据，这里服务端返回的是一个boolean类型的数据
                System.out.println("jsonLength:===" + rjson.getJSONArray("json").length());
                //如果服务器端返回的是true，则说明注册成功，否则注册失败
                if (result) {// 判断结果是否正确
                    //在Android中http请求，必须放到线程中去作请求，但是在线程中不可以直接修改UI，只能通过hander机制来完成对UI的操作
                    myhander.sendEmptyMessage(1);
                    Log.i("用户：", "登录成功");
                } else {
                    myhander.sendEmptyMessage(2);
                    System.out.println("222222222222222");
                    Log.i("用户：", "登录失败");
                }
            } else {
                myhander.sendEmptyMessage(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("aa", e.toString());
            System.out.println("11111111111111111");
            myhander.sendEmptyMessage(2);
        } finally {
            urlConnection.disconnect();// 使用完关闭TCP连接，释放资源
        }
    }

    // 在Android中不可以在线程中直接修改UI，只能借助Handler机制来完成对UI的操作
    class MyHander extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //判断hander的内容是什么，如果是1则说明注册成功，如果是2说明注册失败
            switch (msg.what) {
                case 1:
                    Log.i("aa", msg.what + "");
                    break;
                case 2:
                    Log.i("aa", msg.what + "");
            }
        }
    }
}

