package com.source3g.platform.dto;

import com.source3g.platform.contants.MapType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huhuaiyon 2017/9/11.
 */
@Data
public class GameMap {
    private String name;
    private int colLen = 0;
    private int rowLen = 0;
    private List<List<PosInfo>> map;

    public GameMap(){

    }

    public GameMap(String name, int colLen, int rowLen){
        this.name = name;
        this.colLen = colLen;
        this.rowLen = rowLen;
        map = new ArrayList<>();
        for (int i=0; i < colLen; i++) {
            List<PosInfo> row = new ArrayList<>();
            for (int j = 0; j < rowLen; j++) {
                PosInfo posInfo = new PosInfo();
                posInfo.setMapType(MapType.UNKNOWN, true);
                row.add(posInfo);
            }
            map.add(row);
        }
    }
}
