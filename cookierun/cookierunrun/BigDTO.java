package cookierunrun;

class BigDTO {
    public int x;
    public int y;
    public int randomIndex;
    public boolean eat = false;

    public BigDTO() {
        randomIndex = (int) (Math.random() * 2) + 1;
    }

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

    public int getRandomIndex() {
        return randomIndex;
    }
}