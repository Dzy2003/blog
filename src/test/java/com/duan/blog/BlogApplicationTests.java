package com.duan.blog;

import com.duan.blog.Mapper.TagMapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

@SpringBootTest
@Slf4j
class BlogApplicationTests {

    @Autowired
    TagMapper mapper;
    @Test
    public void testRunnable() throws Exception {
        String s = "DADWDAWDAWDAD";
        test(s, (a) -> a.toLowerCase());
    }
    public <T,R> void test(T r,Function<T,R> function){
        R apply = function.apply(r);
        System.out.println(apply);
    }

    }


