package com.juhe.weather.utils;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Rudy Steiner on 2015/12/1.
 */
public class TextSort {

    //按首字母排序，效果不太好
    public static List TextListSort(List<String> list){

        Object[] listToArray=list.toArray();
        Comparator<Object> com= Collator.getInstance(java.util.Locale.CHINA);
        Arrays.sort(listToArray, com);
        return Arrays.asList(listToArray);
    }
}
