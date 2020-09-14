package cookierunrun;

import java.util.ArrayList;

// 바닥에 한줄로 쿠기가 가는 길에 뿌려주는 젤리를 생성해주는 클래스. 
//스레드를 통해서 정해진 일정한 시간마다 일정한 간격으로 젤리(코인)가 형성된다. 
//젤리의 최대 생성 개수는 16개로 그 이상이 되면 리스트 안의 이미지를 하나씩 없애줌
class JellyDrop implements Runnable {
    public JellyDrop() {
        Jelly.list = new ArrayList<JellyDTO>();
    }

    public void ThreadStart() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (!Game1Client.gameDie) {
            // 새로운 젤리 셋팅
            if (Jelly.list.size() == 16)
                Jelly.list.remove(0);
            JellyDTO jellyDTO = new JellyDTO();
            jellyDTO.setX(720);
            jellyDTO.setY(330);
            Jelly.list.add(jellyDTO);
            jellyDTO.imageIndex = (int) (Math.random() * 2) + 1; // 1~2의 난수를 생성하여 코인을 올릴지 젤리를 올릴지 결정
            try {
                Thread.sleep(200); // 초단위로 진행됨
            } catch (InterruptedException e) {
            }
            if (Game1Client.gameDie)
                break;
        }
    }
}
