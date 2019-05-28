package com.example.mobildonemprojesi;


import java.util.ArrayList;

public class Note {
    private int id;
    private String adr;
    private int priority;
    private String header;
    private String editor;
    private String time;
    private int color;
    private ArrayList<Extra> extras;
    private ArrayList<CharSequence> geo_list;
    private ArrayList<Long> time_list;
    Note(String adr, String header, String editor, int color, ArrayList<Extra> extras, ArrayList<CharSequence> geo_list,ArrayList<Long> time_list){
        this.color = color;
        this.editor = editor;
        this.header = header;
        this.extras = extras;
        this.geo_list = geo_list;
        this.time_list = time_list;
        this.adr = adr;
    }
    Note(){}
    public String getAdr() {
        return adr;
    }

    public void setAdr(String adr) {
        this.adr = adr;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ArrayList<Extra> getExtras() {
        return extras;
    }

    public void setExtras(ArrayList<Extra> extras) {
        this.extras = extras;
    }

    public ArrayList<CharSequence> getGeo_list() {
        return geo_list;
    }

    public void setGeo_list(ArrayList<CharSequence> geo_list) {
        this.geo_list = geo_list;
    }

    public ArrayList<Long> getTime_list() {
        return time_list;
    }

    public void setTime_list(ArrayList<Long> time_list) {
        this.time_list = time_list;
    }

    public static class Extra{
        public String uri;
        public String type;
    }
}
