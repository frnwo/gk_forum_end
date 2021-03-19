package com.guangke.forum.service;

import com.guangke.forum.mapper.CarouselMapper;
import com.guangke.forum.pojo.Carousel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarouselService {
    @Autowired
    CarouselMapper carouselMapper;
    public int updateImg(int id,int num,String url){
       return carouselMapper.updateImg(id,num,url);
    }
    public Carousel selectImg(){
        return carouselMapper.selectImg();
    }
}
