package org.epics.exampleJava.pvDatabaseRPC;


public class Point
{
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Point()
    {
        this(0.0,0.0);
    }

    public Point(Point p)
    {
        this(p.x, p.y);
    }

    public String toString()
    {
         return "(" + x + "," + y + ")";
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Point))
            return false;

        Point p = (Point)o;
        return x == p.x && y == p.y;
    }

    public final double x;
    public final double y;
}
