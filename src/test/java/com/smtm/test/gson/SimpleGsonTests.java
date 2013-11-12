package com.smtm.test.gson;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: <a href="mailto:simeon.petkov@methodia.com">Simeon Petkov</a>
 * Date: 11/11/13
 * Time: 5:38 PM
 * (c) 2012 Methodia Ltd., Sofia, Bulgaria
 */
public class SimpleGsonTests {

    @Test
    public void testTuples() {

        Object[] inner1 = new Object[]{new Date().getTime(), 12.5d};
        Object[] inner2 = new Object[]{new Date().getTime(), 13.5d};
        Object[] inner3 = new Object[]{new Date().getTime(), 14.5d};

        Object[] outer = new Object[]{inner1, inner2, inner3};

        System.out.println(new Gson().toJson(outer));

    }

}
