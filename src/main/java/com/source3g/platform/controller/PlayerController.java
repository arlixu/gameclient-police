package com.source3g.platform.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.source3g.platform.contants.Direct;
import com.source3g.platform.domain.Coord;
import com.source3g.platform.domain.MapInfoOut;
import com.source3g.platform.dto.ClientRes;
import com.source3g.platform.dto.GameMap;
import com.source3g.platform.dto.PosInfo;

/**
 * Created by huhuaiyong on 2017/9/11.
 */
@RestController
@RequestMapping(path = "/player")
public class PlayerController
{
    public final static int BLOCK = 0; // 障碍值
    public final static int ROAD = 1; // 路径
    public final static int UNKNOW = -1; // 未知
    List<Coord> NEXT_STEPS = new ArrayList<>();
    public static MapInfoOut MAP_INFO = null;
    public static int ROUND = 2;
    public static int[][] ROLL_MAPS = null;
    public static Coord CAO_LAST_STEP = null;

    @PostMapping(path = "/start")
    public void start(@RequestBody GameMap gameMap)
    {
        NEXT_STEPS = new ArrayList<>();
        MAP_INFO = null;
        ROUND = 2;
        ROLL_MAPS = null;
        CAO_LAST_STEP = null;
    }

    @GetMapping(path = "/stop")
    public void stop()
    {
        NEXT_STEPS = new ArrayList<>();
        MAP_INFO = null;
        ROUND = 2;
        ROLL_MAPS = null;
        CAO_LAST_STEP = null;
    }

    @PostMapping(path = "/caoMove")
    public ClientRes caoMove(@RequestBody GameMap gameMap)
    {
        ClientRes clientRes = new ClientRes();
        return clientRes;
    }

    @PostMapping(path = "/shuMove")
    public ClientRes shuMove(@RequestBody GameMap gameMap)
    {
        ClientRes clientRes = new ClientRes();
        calMapInfo(gameMap);
        Coord currentShu1 = new Coord(MAP_INFO.shu1Pos);
        Coord currentShu2 = new Coord(MAP_INFO.shu2Pos);
        Coord currentShu3 = new Coord(MAP_INFO.shu3Pos);
        Coord currentShu4 = new Coord(MAP_INFO.shu4Pos);
        if (MAP_INFO.caoPos != null)
        {
            Coord currentCao = new Coord(MAP_INFO.caoPos);
            //该变量仅作为看不到小偷时根据小偷上一步值计算小偷当前位置。
            CAO_LAST_STEP = new Coord(MAP_INFO.caoPos);
            //获取到小偷位置
            System.out.println("将CAO_LAST_STEP设置为" + CAO_LAST_STEP);
            List<Coord> cnb = getNeighbours(currentCao);
            //如果就在边上。直接抓
            if (cnb.contains(currentShu1) || cnb.contains(currentShu2) || cnb.contains(currentShu3) || cnb.contains(currentShu4))
            {
                System.out.println("小偷在移动范围内。");
                Coord shu1NextStep = MAP_INFO.findNextStepByAStar(currentShu1, new Coord(MAP_INFO.caoPos));
                Coord shu2NextStep = MAP_INFO.findNextStepByAStar(currentShu2, new Coord(MAP_INFO.caoPos));
                Coord shu3NextStep = MAP_INFO.findNextStepByAStar(currentShu3, new Coord(MAP_INFO.caoPos));
                Coord shu4NextStep = MAP_INFO.findNextStepByAStar(currentShu4, new Coord(MAP_INFO.caoPos));
                clientRes.setShu1(shu1NextStep.calDirect(currentShu1));
                clientRes.setShu2(shu2NextStep.calDirect(currentShu2));
                clientRes.setShu3(shu3NextStep.calDirect(currentShu3));
                clientRes.setShu4(shu4NextStep.calDirect(currentShu4));
                return clientRes;
            }
            //如果仅仅在视野范围内。
            System.out.println("小偷在斜对角。");
            //其他不在小偷周围的警察，移动至能限制小偷活动的点。
            //在小偷周围的警察，如果已经能限制小偷活动，则刚好能限制小偷活动的棋子保持不动，如果不能限制小偷活动，则移动至能限制小偷活动的点。
            if (MAP_INFO.isCaoLimited())
            {
                //计算最小限制集。
                Map<String, Coord> guards = MAP_INFO.calcLeastGuards();
                System.out.println(guards + "开始站岗");
                //在最小限制集里面的人选择站岗
                for (String guard : guards.keySet())
                {
                    System.out.println(guard + "站岗位置" + MAP_INFO.getRoleCoordByName(guard));
                    clientRes.move(guard, guards.get(guard).calDirect(MAP_INFO.getRoleCoordByName(guard)));
                }
                //其他人抓人
                List<String> others = new ArrayList<String>(Arrays.asList((new String[] { "shu1", "shu2", "shu3", "shu4" })));
                others.removeAll(guards.keySet());
                for (String other : others)
                {
                    Coord roleCoord = MAP_INFO.getRoleCoordByName(other);
                    clientRes.move(other, MAP_INFO.findNextStepByAStar(roleCoord, currentCao).calDirect(roleCoord));
                }
                return clientRes;
            }
            else
            {
                //TODO: 优化点，寻找最近的战略点，当前策略，饿虎扑羊
                System.out.println("饿虎扑羊");
                List<String> all = new ArrayList<String>(Arrays.asList((new String[] { "shu1", "shu2", "shu3", "shu4" })));
                List<Coord> frontLine = getNeighbours(currentCao);
                List<Coord> selectedTargets = new ArrayList<>();
                for (String each : all)
                {
                    Coord roleCoord = MAP_INFO.getRoleCoordByName(each);
                    if (getViews(roleCoord).contains(currentCao))
                    {
                        boolean hasDecided = false;
                        for (Coord frontPos : frontLine)
                        {
                            if (roleCoord.isNearBy(frontPos) && !selectedTargets.contains(frontPos))
                            {
                                clientRes.move(each, frontPos.calDirect(roleCoord));
                                selectedTargets.add(frontPos);
                                hasDecided = true;
                                break;
                            }
                        }
                        if (!hasDecided)
                        {
                            clientRes.move(each, MAP_INFO.findNextStepByAStar(roleCoord, currentCao).calDirect(roleCoord));
                        }
                    }
                    else
                    {
                        List<Coord> forceCircle = getViews(currentCao);
                        Coord nearestOfForceCircle = getNearestOfForceCircle(roleCoord, forceCircle);
                        Coord findNextStepByAStar = MAP_INFO.findNextStepByAStar(roleCoord, nearestOfForceCircle);
                        System.out.println(each + "最近包围圈为" + nearestOfForceCircle);
                        clientRes.move(each, findNextStepByAStar.calDirect(roleCoord));
                    }
                }
                if (clientRes.getShu1() == clientRes.getShu2() && clientRes.getShu1() == clientRes.getShu3() && clientRes.getShu1() == clientRes.getShu4())
                {
                    if (currentShu1.equals(currentShu2) && currentShu1.equals(currentShu3) && currentShu1.equals(currentShu4))
                    {
                        clientRes.setShu1(Direct.UP);
                        clientRes.setShu2(Direct.DOWN);
                        clientRes.setShu3(Direct.LEFT);
                        clientRes.setShu4(Direct.RIGHT);
                    }
                }
                return clientRes;
            }
        }
        else if (CAO_LAST_STEP != null)
        {
            //            CAO_CURRENT_REAL_POS=
            List<Coord> caoCurrentPossible = getNeighbours(CAO_LAST_STEP);
            caoCurrentPossible.add(CAO_LAST_STEP);
            List<Coord> caoCantBe = new ArrayList<>();
            caoCantBe.addAll(getViews(currentShu1));
            caoCantBe.addAll(getViews(currentShu2));
            caoCantBe.addAll(getViews(currentShu3));
            caoCantBe.addAll(getViews(currentShu4));
            caoCurrentPossible.removeAll(caoCantBe);
            System.out.println("计算曹操可能位置" + caoCurrentPossible);
            if (caoCurrentPossible.size() == 1)
            {
                Coord caoCurrentMustBe = caoCurrentPossible.get(0);
                System.out.println("曹操存在唯一位置" + caoCurrentMustBe);

                List<String> all = new ArrayList<String>(Arrays.asList((new String[] { "shu1", "shu2", "shu3", "shu4" })));
                for (String each : all)
                {
                    Coord roleCoord = MAP_INFO.getRoleCoordByName(each);
                    clientRes.move(each, MAP_INFO.findNextStepByAStar(roleCoord, caoCurrentMustBe).calDirect(roleCoord));
                }
                CAO_LAST_STEP = caoCurrentMustBe;
                System.out.println("通过计算将CAO_LAST_STEP设置为" + CAO_LAST_STEP);
                return clientRes;
            }
            else
            {
                //TODO 多个也是线索。
                System.out.println("跟丢了。");
                CAO_LAST_STEP = null;
            }
        }
        else
        {
            System.out.println("曹操未出现过，无法计算曹操位置");
        }

        if (MAP_INFO.hasUnknown)
        {
            NEXT_STEPS = new ArrayList<>();
            List<Coord> targets = new ArrayList<>();
            List<Coord> allCurrents = new ArrayList<>();
            allCurrents.add(currentShu1);
            allCurrents.add(currentShu2);
            allCurrents.add(currentShu3);
            allCurrents.add(currentShu4);
            String shuNamePefix = "shu";
            int i = 1;
            List<Coord> toBeKnown=new ArrayList<>();
            for (Coord shu : allCurrents)
            {
                //shu怎么走
                List<Coord> shuNeighs = getNeighbours(shu);
                sortMostDiscoverFirst(shuNeighs,toBeKnown);
                Coord shuNextStep = shuNeighs.get(0);
                if (noDiscovery(shuNextStep))
                {
                    //寻找最近的未知区域并向其移动
                    Coord target = findNearestUnknown(shu, targets);
                    if (target == null)
                    {
                        System.out.println("已无未知区域");
                    }
                    else
                    {
                        targets.add(target);
                    }
                    shuNextStep = MAP_INFO.findNextSearchStepByAStar(shu, target);
                    if (shuNextStep == null)
                    {
                        System.out.println("A*没算出下一步");
                    }
                }
                clientRes.move(shuNamePefix + i, shuNextStep.calDirect(shu));
                i++;
                targets.add(shuNextStep);
                toBeKnown.addAll(getViews(shuNextStep));
                NEXT_STEPS.add(shuNextStep);
            }
            return clientRes;
        }
        else
        {
            if (ROLL_MAPS == null)
            {
                ROLL_MAPS = MAP_INFO.cloneMaps();
            }
            //贪心算法，循环扫图。
            NEXT_STEPS = new ArrayList<>();
            List<Coord> targets = new ArrayList<>();
            List<Coord> toBeRoll = new ArrayList<>();
            List<Coord> allCurrents = new ArrayList<>();
            allCurrents.add(currentShu1);
            allCurrents.add(currentShu2);
            allCurrents.add(currentShu3);
            allCurrents.add(currentShu4);
            //Set view to ROUND
            setViewToRound(allCurrents);
            //ROUND结束则ROUND++
            if (isRoundEnd())
            {
                System.out.println("第" + ROUND + "轮图已经扫完");
                ROUND++;
                System.out.println("开始第" + ROUND + "轮扫图");
            }

            String shuNamePefix = "shu";
            int i = 1;
            for (Coord shu : allCurrents)
            {
                //shu怎么走
                List<Coord> shuNeighs = getNeighbours(shu);
                sortMostRollFirst(shuNeighs,toBeRoll);
                Coord shuNextStep = shuNeighs.get(0);
                if (noRoll(shuNextStep))
                {
                    //寻找最近的未扫图区域并向其移动
                    Coord target = findNearestUnroll(shu, targets);
                    if (target == null)
                    {
                        System.out.println("第" + ROUND + "轮图已经扫完");
                    }
                    else
                    {
                        targets.add(target);
                    }
                    shuNextStep = MAP_INFO.findNextSearchStepByAStar(shu, target);
                    if (shuNextStep == null)
                    {
                        System.out.println("A*没算出下一步");
                    }
                }
                clientRes.move(shuNamePefix + i, shuNextStep.calDirect(shu));
                i++;
                targets.add(shuNextStep);
                toBeRoll.addAll(getViews(shuNextStep));
                NEXT_STEPS.add(shuNextStep);
            }
            return clientRes;
        }
    }

    private Coord getNearestOfForceCircle(Coord current, List<Coord> forceCircle)
    {
        forceCircle.sort(new Comparator<Coord>()
        {

            @Override
            public int compare(Coord o1, Coord o2)
            {
                return Integer.compare(Math.abs(o1.x - current.x) + Math.abs(o1.y - current.y), Math.abs(o2.x - current.x) + Math.abs(o2.y - current.y));
            }
        });
        return forceCircle.get(0);
    }

    private List<Coord> getNeighbours(Coord current)
    {
        List<Coord> neighbours = new ArrayList<>();
        int x = current.x;
        int y = current.y;
        //上下左右分别为: x,y-1 | x,y+1|x-1,y|x+1,y|
        if (!MAP_INFO.notAbleToWalk(x, y + 1))
        {
            neighbours.add(new Coord(x, y + 1));
        }

        if (!MAP_INFO.notAbleToWalk(x + 1, y))
        {
            neighbours.add(new Coord(x + 1, y));
        }
        if (!MAP_INFO.notAbleToWalk(x - 1, y))
        {
            neighbours.add(new Coord(x - 1, y));
        }

        if (!MAP_INFO.notAbleToWalk(x, y - 1))
        {
            neighbours.add(new Coord(x, y - 1));
        }

        return neighbours;
    }

    private boolean noDiscovery(Coord coord)
    {
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (!MAP_INFO.notAbleToWalk(coord.x + i, coord.y + j) && MAP_INFO.maps[coord.y + j][coord.x + i] == -1)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean noRoll(Coord coord)
    {
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (!MAP_INFO.notAbleToWalk(coord.x + i, coord.y + j) && ROLL_MAPS[coord.y + j][coord.x + i] < ROUND)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void sortMostDiscoverFirst(List<Coord> coords,List<Coord> toBeKnown)
    {
        coords.sort(new Comparator<Coord>()
        {
            @Override
            public int compare(Coord o1, Coord o2)
            {
                //其他警察准备走的路，放在最低优先级里选。
                if (NEXT_STEPS.contains(o1) && !NEXT_STEPS.contains(o2))
                {
                    return 1;
                }
                else if (!NEXT_STEPS.contains(o1) && NEXT_STEPS.contains(o2))
                {
                    return -1;
                }
                //探路越多越好。
                int o1WillOpen = 0;
                int o2WillOpen = 0;
                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        if (!MAP_INFO.notAbleToWalk(o1.x + i, o1.y + j) && MAP_INFO.maps[o1.y + j][o1.x + i] == -1&&!toBeKnown.contains(new Coord(o1.x + i,o1.y+j)))
                        {
                            o1WillOpen++;
                        }
                        if (!MAP_INFO.notAbleToWalk(o2.x + i, o2.y + j) && MAP_INFO.maps[o2.y + j][o2.x + i] == -1&&!toBeKnown.contains(new Coord(o1.x + i,o1.y+j)))
                        {
                            o2WillOpen++;
                        }
                    }
                }
                int compare = Integer.compare(o2WillOpen, o1WillOpen);
                return compare;
            }
        });
    }

    private void sortMostRollFirst(List<Coord> coords, List<Coord> toBeRoll)
    {
        coords.sort(new Comparator<Coord>()
        {
            @Override
            public int compare(Coord o1, Coord o2)
            {
                //其他警察准备走的路，放在最低优先级里选。
                if (NEXT_STEPS.contains(o1) && !NEXT_STEPS.contains(o2))
                {
                    return 1;
                }
                else if (!NEXT_STEPS.contains(o1) && NEXT_STEPS.contains(o2))
                {
                    return -1;
                }
                //扫路越多越好。
                int o1WillRoll = 0;
                int o2WillRoll = 0;
                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        if (!MAP_INFO.notAbleToWalk(o1.x + i, o1.y + j) && ROLL_MAPS[o1.y + j][o1.x + i] < ROUND&&!toBeRoll.contains(new Coord(o1.x + i,o1.y+j)))
                        {
                            o1WillRoll++;
                        }
                        if (!MAP_INFO.notAbleToWalk(o2.x + i, o2.y + j) && ROLL_MAPS[o2.y + j][o2.x + i] < ROUND&&!toBeRoll.contains(new Coord(o1.x + i,o1.y+j)))
                        {
                            o2WillRoll++;
                        }
                    }
                }
                int compare = Integer.compare(o2WillRoll, o1WillRoll);
                return compare;
            }
        });
    }

    public Coord findNearestUnknown(Coord current, List<Coord> targets)
    {
        List<Coord> unknows = new ArrayList<>();
            for (int j = MAP_INFO.height-1; j >=0; j--)
                for (int i = MAP_INFO.width-1; i >=0 ; i--)
            {
                if (MAP_INFO.maps[j][i] == -1)
                {
                    unknows.add(new Coord(i, j));
                }
            }
        unknows.sort(new Comparator<Coord>()
        {
            @Override
            public int compare(Coord o1, Coord o2)
            {
                //在targets周围的坐标需要放在后面
                if (o1.isNotSoFar(targets) && !o2.isNotSoFar(targets))
                {
                    return 1;
                }
                if (o2.isNotSoFar(targets) && !o1.isNotSoFar(targets))
                {
                    return -1;
                }
                //把边角的放在最后
                if ((o1.x == 0 || o1.y == 0) && (o2.x != 0 && o2.y != 0))
                {
                    return 1;
                }
                if ((o2.x == 0 || o2.y == 0) && (o1.x != 0 && o1.y != 0))
                {
                    return -1;
                }
                //比较和current的距离，最小的排在前面。
                return Integer.compare(Math.abs(o1.x - current.x) + Math.abs(o1.y - current.y), Math.abs(o2.x - current.x) + Math.abs(o2.y - current.y));
            }
        });
        if (unknows.isEmpty())
        {
            return null;
        }
        for (Coord coord : unknows)
        {
            List<Coord> neighbours = getNeighbours(coord);
            for (Coord neighb : neighbours)
            {
                if (MAP_INFO.maps[neighb.y][neighb.x] == 1)
                {
                    return coord;
                }
            }
        }
        return null;
    }

    public Coord findNearestUnroll(Coord current, List<Coord> targets)
    {
        List<Coord> unrolls = new ArrayList<>();
        for (int i = 0; i < MAP_INFO.width; i++)
            for (int j = 0; j < MAP_INFO.height; j++)
            {
                if (ROLL_MAPS[j][i] < ROUND && ROLL_MAPS[j][i] > 0)
                {
                    unrolls.add(new Coord(i, j));
                }
            }
        unrolls.sort(new Comparator<Coord>()
        {
            @Override
            public int compare(Coord o1, Coord o2)
            {
                //在targets周围的坐标需要放在后面
                if (o1.isNotSoFar(targets) && !o2.isNotSoFar(targets))
                {
                    return 1;
                }
                if (o2.isNotSoFar(targets) && !o1.isNotSoFar(targets))
                {
                    return -1;
                }
                //把边角的放在最后
                if ((o1.x == 0 || o1.y == 0) && (o2.x != 0 && o2.y != 0))
                {
                    return 1;
                }
                if ((o2.x == 0 || o2.y == 0) && (o1.x != 0 && o1.y != 0))
                {
                    return -1;
                }
                //比较和current的距离，最小的排在前面。
                return Integer.compare(Math.abs(o1.x - current.x) + Math.abs(o1.y - current.y), Math.abs(o2.x - current.x) + Math.abs(o2.y - current.y));
            }
        });
        if (unrolls.isEmpty())
        {
            return null;
        }
        for (Coord coord : unrolls)
        {
            List<Coord> neighbours = getNeighbours(coord);
            for (Coord neighb : neighbours)
            {
                if (MAP_INFO.maps[neighb.y][neighb.x] == 1)
                {
                    return coord;
                }
            }
        }
        return null;
    }

    public boolean isRoundEnd()
    {
        for (int i = 0; i < MAP_INFO.height; i++)
            for (int j = 0; j < MAP_INFO.width; j++)
            {
                if (ROLL_MAPS[i][j] < ROUND && ROLL_MAPS[i][j] > 0)
                    return false;
            }
        return true;
    }

    public void setViewToRound(List<Coord> currents)
    {
        for (Coord coord : currents)
        {
            List<Coord> views = getViews(coord);
            for (Coord view : views)
            {
                ROLL_MAPS[view.y][view.x] = ROUND;
            }
        }
    }

    public static List<Coord> getViews(Coord current)
    {
        List<Coord> views = new ArrayList<>();
        int x = current.x;
        int y = current.y;

        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                if (!MAP_INFO.notAbleToWalk(x + i, y + j))
                {
                    views.add(new Coord(x + i, y + j));
                }
            }
        }
        return views;
    }

    private void calMapInfo(GameMap gameMap)
    {
        MapInfoOut mapInfoOut = new MapInfoOut();
        List<List<PosInfo>> map = gameMap.getMap();
        mapInfoOut.maps = new Integer[gameMap.getColLen()][gameMap.getRowLen()];
        mapInfoOut.height = gameMap.getColLen();
        mapInfoOut.width = gameMap.getRowLen();
        mapInfoOut.hasUnknown = false;
        for (int j = 0; j < map.size(); j++)
        {
            List<PosInfo> posInfos = map.get(j);
            for (int i = 0; i < posInfos.size(); i++)
            {
                PosInfo posInfo = posInfos.get(i);
                if (posInfo.isShu1())
                {
                    mapInfoOut.shu1Pos = new Integer[] { i, j };
                }
                if (posInfo.isShu2())
                {
                    mapInfoOut.shu2Pos = new Integer[] { i, j };
                }
                if (posInfo.isShu3())
                {
                    mapInfoOut.shu3Pos = new Integer[] { i, j };
                }
                if (posInfo.isShu4())
                {
                    mapInfoOut.shu4Pos = new Integer[] { i, j };
                }
                if (posInfo.isCao())
                {
                    mapInfoOut.caoPos = new Integer[] { i, j };
                }
                if (posInfo.isUnknown())
                {
                    mapInfoOut.hasUnknown = true;
                }
                mapInfoOut.maps[j][i] = posInfo.isUnknown() ? -1 : posInfo.isBlock() ? 0 : posInfo.isRoad() ? 1 : -2;
            }
        }
        MAP_INFO = mapInfoOut;
    }

}
