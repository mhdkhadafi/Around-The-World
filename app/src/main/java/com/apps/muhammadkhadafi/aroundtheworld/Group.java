package com.apps.muhammadkhadafi.aroundtheworld;

/**
 * Created by muhammadkhadafi on 11/29/14.
 */
import java.util.ArrayList;
import java.util.List;

public class Group {

    public String title;
    public String subTitle;
    public final List<String> children = new ArrayList<String>();

    public Group(String title, String subTitle ) {
        this.title = title;
        this.subTitle = subTitle;
    }

}