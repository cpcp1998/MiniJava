package linearscan;

import java.util.Collection;

public class Interval {
    public int left, right;
    public boolean empty;

    public Interval() {
        empty = true;
    }

    public Interval(int left, int right) {
        this.left = left;
        this.right = right;
        empty = false;
    }

    public void addRange(int left, int right) {
        if (empty) {
            empty = false;
            this.left = left;
            this.right = right;
        } else {
            if (left < this.left) {
                this.left = left;
            }
            if (right > this.right) {
                this.right = right;
            }
        }
    }

    public boolean contains(Collection<Integer> points) {
        if (empty) return false;
        for (int point : points) {
            if (left <= point && point < right) return true;
        }
        return false;
    }

    public String toString() {
        if (empty) return "(0, 0)";
        return "[" + left + ", " + right + ")";
    }
}
