package com.source3g.platform.domain;

import java.util.List;

import com.source3g.platform.contants.Direct;

public class Coord
{
    public int x;
    public int y;

    public Coord(int x, int y)
    {
        super();
        this.x = x;
        this.y = y;
    }

    public Coord(Integer[] a)
    {
        super();
        x=a[0];
        y=a[1];
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (obj instanceof Coord)
        {
            Coord c = (Coord) obj;
            return x == c.x && y == c.y;
        }
        return false;
    }
    
    public boolean isNotSoFar(List<Coord> targets)
    {
        for (Coord coord : targets)
        {
            if (this.isNotSoFar(coord))
            {
              return true;
            }
        }
        return false;
    }
    
    public boolean isNotSoFar(Coord target)
    {
        return Math.abs(this.x-target.x)+Math.abs(this.y-target.y)<3;
    }
    
    public boolean isNearBy(Coord target)
    {
        return Math.abs(this.x-target.x)+Math.abs(this.y-target.y)<=1;
    }

    public Direct calDirect(Coord latest)
    {
        if (this.x > latest.x)
        {
            return Direct.RIGHT;
        }
        else if (this.x < latest.x)
        {
            return Direct.LEFT;
        }
        else if (this.y > latest.y)
        {
            return Direct.DOWN;
        }
        else if (this.y < latest.y)
        {
            return Direct.UP;
        }
        else
            return Direct.STAY;

    }

    @Override
    public String toString()
    {
        return "Coord [x=" + x + ", y=" + y + "]";
    }

}
