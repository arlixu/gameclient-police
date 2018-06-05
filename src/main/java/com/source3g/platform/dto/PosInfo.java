package com.source3g.platform.dto;

import com.source3g.platform.contants.MapType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huhuaiyong on 2017/9/11.
 */
@Data
public class PosInfo {
    private boolean unknown = false;
    private boolean block = false;
    private boolean road = false;
    private boolean cao = false;
    private boolean shu1 = false;
    private boolean shu2 = false;
    private boolean shu3 = false;
    private boolean shu4 = false;

    public PosInfo setMapType(MapType mapType, boolean hasRole){
        switch (mapType){
            case UNKNOWN:
                unknown = hasRole;
                break;
            case BLOCK:
                block = hasRole;
                break;
            case ROAD:
                road = hasRole;
                break;
            case CAO:
                cao = hasRole;
                break;
            case SHU1:
                shu1 = hasRole;
                break;
            case SHU2:
                shu2 = hasRole;
                break;
            case SHU3:
                shu3 = hasRole;
                break;
            case SHU4:
                shu4 = hasRole;
                break;
            default:break;
        }
        return this;
    }
    public String toString(){
        List<Integer> list = new ArrayList<>();
        if (unknown){
            list.add(MapType.UNKNOWN.getIndex());
        }
        if (block){
            list.add(MapType.BLOCK.getIndex());
        }
        if (road){
            list.add(MapType.ROAD.getIndex());
        }
        if (cao){
            list.add(MapType.CAO.getIndex());
        }
        if (shu1){
            list.add(MapType.SHU1.getIndex());
        }
        if (shu2){
            list.add(MapType.SHU2.getIndex());
        }
        if (shu3){
            list.add(MapType.SHU3.getIndex());
        }
        if (shu4){
            list.add(MapType.SHU4.getIndex());
        }
        return list.toString();
    }

    public boolean hasMapType(MapType mapType){
        switch (mapType){
            case UNKNOWN:
                return unknown;
            case BLOCK:
                return block;
            case ROAD:
                return road;
            case CAO:
                return cao;
            case SHU1:
                return shu1;
            case SHU2:
                return shu2;
            case SHU3:
                return shu3;
            case SHU4:
                return shu4;
            default:break;
        }
        return false;
    }
}
