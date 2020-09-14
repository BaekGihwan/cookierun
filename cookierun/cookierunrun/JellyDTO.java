package cookierunrun;

class JellyDTO { // 각각의 젤리에 대한 좌표값을 저장
    public int x, y;
    public boolean eat = false;  //만약 쿠키와 닿으면  true가 되어서 패널위에서 보이지 않는다. 
    public int imageIndex;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}