package com.example.teambag.tools;

public class User implements Comparable<User> {
    private String name; // 姓名
    private String pinyin; // 姓名对应的拼音
    private String firstLetter; // 拼音的首字母
    private String number;
    public User() {    }
    public User(String name) {
        this.name = name;
        pinyin = Cn2Spell.getPinYin(name); // 根据姓名获取拼音
        firstLetter = pinyin.substring(0, 1).toUpperCase(); // 获取拼音首字母并转成大写
        if (!firstLetter.matches("[A-Z]")) { // 如果不在A-Z中则默认为“#”
            firstLetter = "#";        }
    }
    public User(String name,String number) {
        this.name = name;
        this.number = number;
        pinyin = Cn2Spell.getPinYin(name); // 根据姓名获取拼音
        firstLetter = pinyin.substring(0, 1).toUpperCase(); // 获取拼音首字母并转成大写
        if (!firstLetter.matches("[A-Z]")) { // 如果不在A-Z中则默认为“#”
            firstLetter = "#";        }
    }
    public String getName() {
        return name;
    }
    public String getPinyin() {
        return pinyin;
    }
    public String getFirstLetter() {
        return firstLetter;
    }
    public String getNumber() {return number;}
    public void setNumber(String number){this.number=number;}
    @Override
    public int compareTo(com.example.teambag.tools.User another) {
        if (firstLetter.equals("#") && !another.getFirstLetter().equals("#")) {
            return 1;
        }
        else if (!firstLetter.equals("#") && another.getFirstLetter().equals("#")){
            return -1;
        } else {
            return pinyin.compareToIgnoreCase(another.getPinyin());
        }
    }
}
