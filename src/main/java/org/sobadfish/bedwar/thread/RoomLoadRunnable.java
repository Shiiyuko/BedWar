package org.sobadfish.bedwar.thread;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import org.sobadfish.bedwar.BedWarMain;
import org.sobadfish.bedwar.entity.ShopVillage;
import org.sobadfish.bedwar.manager.RoomManager;
import org.sobadfish.bedwar.manager.ThreadManager;
import org.sobadfish.bedwar.player.PlayerInfo;
import org.sobadfish.bedwar.room.GameRoom;
import org.sobadfish.bedwar.room.floattext.FloatTextInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomLoadRunnable extends ThreadManager.AbstractBedWarRunnable {

    public LinkedHashMap<String,Long> time = new LinkedHashMap<>();

    @Override
    public GameRoom getRoom() {
        return null;
    }

    @Override
    public String getThreadName() {
        String color = "&a";
        if(isClose){
            color = "&7";
        }
        StringBuilder s = new StringBuilder(color+"房间进程 &7(" + BedWarMain.getRoomManager().getRooms().size() + ")\n");
        for(Map.Entry<String,Long> room: time.entrySet()){
            s.append("     &r").append(room.getKey()).append("  &a").append(room.getValue()).append(" ms\n");

        }
        return s.toString();
    }

    @Override
    public void run() {
        try {
            if (isClose) {
                ThreadManager.cancel(this);
            }

            if (BedWarMain.getBedWarMain().isDisabled()) {
                isClose = true;
                return;
            }
            List<GameRoom> gameRooms = new CopyOnWriteArrayList<>(BedWarMain.getRoomManager().getRooms().values());
            for (GameRoom room : gameRooms) {
                long t1 = System.currentTimeMillis();
                if (BedWarMain.getRoomManager().getRoom(room.getRoomConfig().name) == null) {
                    RoomManager.LOCK_GAME.remove(room.getRoomConfig());
                    BedWarMain.getRoomManager().getRooms().remove(room.getRoomConfig().name);
                    continue;
                }

                if (room.close || room.getWorldInfo().getConfig().getGameWorld() == null) {
                    continue;
                }
                room.onUpdate();
                for (PlayerInfo playerInfo : room.getPlayerInfos()) {
                    if (playerInfo.cancel || playerInfo.isLeave) {
                        playerInfo.removeScoreBoard();

                    } else {
                        playerInfo.onUpdate();
                    }

                }

                if (room.loadTime > 0) {
                    if (!room.getEventControl().hasEvent()) {
                        room.loadTime--;
                    }

                }
                try {
                    if (room.worldInfo != null) {
                        if (!room.worldInfo.isClose()) {
                            //把掉落物啥的扔回主线程
                            Server.getInstance().getScheduler().scheduleTask(BedWarMain.getBedWarMain(),new WorldInfoMasterThread(room,room.worldInfo,BedWarMain.getBedWarMain()));
                        }
                    }
                } catch (Exception ignore) {
                }

                time.put(room.getRoomConfig().name, System.currentTimeMillis() - t1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
