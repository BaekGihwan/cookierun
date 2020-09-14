package cookierunrun;

class GomJellyDTO {
    public int x;
    public int y;
    public boolean eat = false; // 젤리를 먹었을 떄 true로 바뀐다.

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setEat(boolean eat) {
        this.eat = eat;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}