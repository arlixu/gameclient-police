package com.source3g.platform.contants;

/**
 * Created by huhuaiyong on 2017/9/11.
 */
public enum MapType {
    UNKNOWN(-1, "未知"),
    BLOCK(0, "障碍物"),
    ROAD(1, "道路"),
    CAO(2, "曹操"),
    SHU1(3, "张飞"),
    SHU2(4, "关羽"),
    SHU3(5, "赵云"),
    SHU4(6, "马超");

    private Integer index;
    private String name;
    MapType(Integer index, String name){
        this.index = index;
        this.name = name;
    }
    public Integer getIndex() {
        return index;
    }
    public String toString(){
        return this.name;
    }

    public static MapType indexOf(Object index){
        MapType[] enumConstants = MapType.class.getEnumConstants();
        for (MapType status : enumConstants)
        {
            if (status.getIndex().equals(Integer.parseInt(index.toString())))
            {
                return status;
            }
        }
        return UNKNOWN;
    }
}
