
package com.source3g.platform.domain;

import com.source3g.platform.contants.Direct;

public class Node implements Comparable<Node>
{
    public Coord coord; // 坐标
    public Node parent; // 父结点
    public int G; // G：是个准确的值，是起点到当前结点的代价
    public int H; // H：是个估值，当前结点到目的结点的估计代价

    public Node(int x, int y)
    {
        this.coord = new Coord(x, y);
    }

    public Node(Coord current)
    {
        this.coord = new Coord(current.x, current.y);
    }


    public Node(Coord coord, Node parent, int g, int h)
    {
        this.coord = coord;
        this.parent = parent;
        G = g;
        H = h;
    }

    public Direct calDirect(Coord latest)
    {
        if (coord.x > latest.x)
        {
            return Direct.RIGHT;
        }
        else if (coord.x < latest.x)
        {
            return Direct.LEFT;
        }
        else if (coord.y > latest.y)
        {
            return Direct.DOWN;
        }
        else if (coord.y < latest.y)
        {
            return Direct.UP;
        }
        else
            return Direct.STAY;

    }

    @Override
    public int compareTo(Node o)
    {
        return Integer.compare(G + H, o.G + o.H);
    }

    @Override
    public String toString()
    {
        return "Node [coord=" + coord + ", parent=" + parent + ", G=" + G + ", H=" + H + "]";
    }
    
}
