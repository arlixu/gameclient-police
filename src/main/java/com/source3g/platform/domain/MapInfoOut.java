package com.source3g.platform.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.source3g.platform.controller.PlayerController;

public class MapInfoOut
{
    public int width;
    public int height;
    public Integer[][] maps;
    public Integer[] shu1Pos;
    public Integer[] shu2Pos;
    public Integer[] shu3Pos;
    public Integer[] shu4Pos;
    public Integer[] caoPos;
    public boolean hasUnknown;
    
    public int[][] cloneMaps()
    {
        int[][] result = new int[height][width];
        for (int i = 0; i < this.width; i++)
        {
            for(int j = 0; j < this.height; j++)
            {
                result[j][i]=maps[j][i].intValue();
            }
        }
        return result;
    }
    
    public Coord getRoleCoordByName(String roleName)
    {
        switch (roleName)
        {
            case "shu1":
                return new Coord(shu1Pos);
            case "shu2":
                return new Coord(shu2Pos);
            case "shu3":
                return new Coord(shu3Pos);
            case "shu4":
                return new Coord(shu4Pos);
            case "cao":
                return new Coord(caoPos);
            default:
                return null;
        }
    }
    
    public boolean isCaoLimited()
    {
        List<Coord> caoCanMoveTo = getNeighbours(new Coord(caoPos));
        List<Coord> shuDefends = new ArrayList<>();
        shuDefends.addAll(getNeighbours(new Coord(shu1Pos)));
        shuDefends.addAll(getNeighbours(new Coord(shu2Pos)));
        shuDefends.addAll(getNeighbours(new Coord(shu3Pos)));
        shuDefends.addAll(getNeighbours(new Coord(shu4Pos)));
        return shuDefends.containsAll(caoCanMoveTo);
    }

    public Coord findNextStepByAStar(Coord current, Coord end)
    {
        Queue<Node> openList = new PriorityQueue<Node>(); // 优先队列(升序)
        List<Node> closeList = new ArrayList<Node>();
        openList.add(new Node(current, null, 0, calcAbsDistance(current, end)));
        while (!openList.isEmpty())
        {
            if (isCoordInClose(end, closeList))
            {
                return chooseNext(current, end, closeList);
            }
            Node currentNode = openList.poll();
            closeList.add(currentNode);
            addNeighborsNodeInOpen(currentNode.coord, end, openList, closeList);
        }
        return null;
    }
    
    public Coord findNextSearchStepByAStar(Coord current, Coord end)
    {
        Queue<Node> openList = new PriorityQueue<Node>(); // 优先队列(升序)
        List<Node> closeList = new ArrayList<Node>();
        openList.add(new Node(current, null, 0, calcAbsDistance(current, end)));
        while (!openList.isEmpty())
        {
            if (isCoordInClose(end, closeList))
            {
                return chooseNext(current, end, closeList);
            }
            Node currentNode = openList.poll();
            closeList.add(currentNode);
            addSearchingNeighborsNodeInOpen(currentNode.coord, end, openList, closeList);
        }
        return null;
    }

    private Node findNode(Coord coord, Collection<Node> list)
    {
        if (coord == null || list.isEmpty())
            return null;
        for (Node node : list)
        {
            if (node.coord.equals(coord))
            {
                return node;
            }
        }
        return null;
    }

    /**
     * 添所有邻结点到open表
     */
    private void addSearchingNeighborsNodeInOpen(Coord current, Coord end, Queue<Node> openList, List<Node> closeList)
    {
        List<Coord> neighbours = getNeighbours(current);
        for (Coord coord : neighbours)
        {
            if (findNode(coord, closeList) == null)
            {
                Node currentNode = findNode(current, closeList);
                int g=1;
                if(currentNode.coord.x==0&currentNode.coord.y==0)
                {
                    g=3;
                }else if(currentNode.coord.x==0||currentNode.coord.y==0)
                {
                    g=2;
                }else if(PlayerController.getViews(currentNode.coord).size()<9)
                {
                    g=2;
                }
                int G = currentNode.G + g;
                //查看扩展节点是否在openList中已经存在,如果存在,看是否需要更新实际值
                Node child = findNode(coord, openList);
                //不存在
                if (child == null)
                {
                    int H = calcAbsDistance(end, coord);
                    child = new Node(coord, currentNode, G, H);
                    openList.add(child);
                }
                else if (child.G > G) //存在
                {
                    child.G = G;
                    child.parent = currentNode;
                    openList.add(child);
                }
            }
        }
    }
    
    /**
     * 添所有邻结点到open表
     */
    private void addNeighborsNodeInOpen(Coord current, Coord end, Queue<Node> openList, List<Node> closeList)
    {
        List<Coord> neighbours = getNeighbours(current);
        for (Coord coord : neighbours)
        {
            if (findNode(coord, closeList) == null)
            {
                Node currentNode = findNode(current, closeList);
                int G = currentNode.G + 1;
                //查看扩展节点是否在openList中已经存在,如果存在,看是否需要更新实际值
                Node child = findNode(coord, openList);
                //不存在
                if (child == null)
                {
                    int H = calcAbsDistance(end, coord);
                    child = new Node(coord, currentNode, G, H);
                    openList.add(child);
                }
                else if (child.G > G) //存在
                {
                    child.G = G;
                    child.parent = currentNode;
                    openList.add(child);
                }
            }
        }
    }

    private int calcAbsDistance(Coord end, Coord coord)
    {
        return Math.abs(end.x - coord.x) + Math.abs(end.y - coord.y);
    }

    private Coord chooseNext(Coord current, Coord end, List<Node> closeList)
    {
        Node next = findNode(end, closeList);
        while (!current.equals(next.parent.coord))
        {
            next = next.parent;
        }
        return next.coord;
    }

    /**
     * 判断坐标是否在close表中
     */
    private boolean isCoordInClose(Coord coord, List<Node> closeList)
    {
        return coord != null && isCoordInClose(coord.x, coord.y, closeList);
    }

    /**
     * 判断坐标是否在close表中
     */
    private boolean isCoordInClose(int x, int y, List<Node> closeList)
    {
        if (closeList.isEmpty())
            return false;
        for (Node node : closeList)
        {
            if (node.coord.x == x && node.coord.y == y)
            {
                return true;
            }
        }
        return false;
    }

    public boolean notAbleToWalk(int x, int y)
    {
        return x < 0 || x >= this.width || y < 0 || y >= this.height || maps[y][x] == 0;
    }

    private List<Coord> getNeighbours(Coord current)
    {
        List<Coord> neighbours = new ArrayList<>();
        int x = current.x;
        int y = current.y;
        //那么 上下左右分别为: x,y-1 | x,y+1|x-1,y|x+1,y|
        if (!this.notAbleToWalk(x, y - 1))
        {
            neighbours.add(new Coord(x, y - 1));
        }
        if (!this.notAbleToWalk(x, y + 1))
        {
            neighbours.add(new Coord(x, y + 1));
        }
        if (!this.notAbleToWalk(x - 1, y))
        {
            neighbours.add(new Coord(x - 1, y));
        }
        if (!this.notAbleToWalk(x + 1, y))
        {
            neighbours.add(new Coord(x + 1, y));
        }
        return neighbours;
    }

    public Map<String,Coord> calcLeastGuards()
    {
        //需要守的点=小偷除stay外可以移动的点
        Map<String,Coord> guards = new HashMap<>();
        List<Coord> needToDefend = getNeighbours(new Coord(this.caoPos));
        List<Coord> shu1Defend=new ArrayList<>();
        List<Coord> shu2Defend=new ArrayList<>();
        List<Coord> shu3Defend=new ArrayList<>();
        List<Coord> shu4Defend=new ArrayList<>();
        for(Coord defendPos:needToDefend)
        {
            if(getNeighbours(new Coord(this.shu1Pos)).contains(defendPos))
            {
                shu1Defend.add(defendPos);
                continue;
            }
            if(getNeighbours(new Coord(this.shu2Pos)).contains(defendPos))
            {
                shu2Defend.add(defendPos);
                continue;
            }
            if(getNeighbours(new Coord(this.shu3Pos)).contains(defendPos))
            {
                shu3Defend.add(defendPos);
                continue;
            }
            if(getNeighbours(new Coord(this.shu4Pos)).contains(defendPos))
            {
                shu4Defend.add(defendPos);
                continue;
            }
        }
        if(!shu1Defend.isEmpty())
        {
            guards.put("shu1",shu1Defend.size()==1? shu1Defend.get(0):new Coord(shu1Pos));
        }
        if(!shu2Defend.isEmpty())
        {
            guards.put("shu2",shu2Defend.size()==1? shu2Defend.get(0):new Coord(shu2Pos));
        }
        if(!shu3Defend.isEmpty())
        {
            guards.put("shu3",shu3Defend.size()==1? shu3Defend.get(0):new Coord(shu3Pos));
        }
        if(!shu4Defend.isEmpty())
        {
            guards.put("shu4",shu4Defend.size()==1? shu4Defend.get(0):new Coord(shu4Pos));
        }
        return guards;
    }

}
