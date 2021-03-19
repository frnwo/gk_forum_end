package com.guangke.forum.mapper;

import com.guangke.forum.pojo.Carousel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarouselMapper {
    //num:第几个图 url:图片的url
    int updateImg(int id,int num,String url);
    Carousel selectImg();
}
