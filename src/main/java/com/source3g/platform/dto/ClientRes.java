package com.source3g.platform.dto;

import com.source3g.platform.contants.Direct;
import lombok.Data;

/**
 * Created by huhuaiyong on 2017/9/11.
 */
@Data
public class ClientRes {
    private Direct cao;
    private Direct shu1;
    private Direct shu2;
    private Direct shu3;
    private Direct shu4;
    public void move(String roleName,Direct direct)
    {
        System.out.println(roleName+"移动方向"+direct);
      
        switch (roleName)
        {
            case "shu1":
                shu1=direct;
            break;
            case "shu2":
                shu2=direct;
                break;
            case "shu3":
                shu3=direct;
                break;
            case "shu4":
                shu4=direct;
                break;
            default:
            break;
        }
    }
}
