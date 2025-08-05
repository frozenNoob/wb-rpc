package com.wb.wbrpc;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashTest {
    @Test
    public void test1(){
        Map<String, List> mp = new HashMap<>();
        List al = new ArrayList<Integer>();
        al.add(2);
        mp.put("c1", al);
        al.remove(0);
        mp.put("c2", al);
        mp.remove("c1");
        mp.remove("c2");
        System.out.println(mp.get("cl"));
        System.out.println(mp);
    }



}
