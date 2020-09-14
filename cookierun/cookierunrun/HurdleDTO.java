package cookierunrun;

class HurdleDTO {
    public int x, y;
    public int imageIndex;

    public HurdleDTO() {
    }

    public HurdleDTO(int imageIndex) {
        this.imageIndex = imageIndex;
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

    public int getImageIndex() {
        return imageIndex;
    }
}