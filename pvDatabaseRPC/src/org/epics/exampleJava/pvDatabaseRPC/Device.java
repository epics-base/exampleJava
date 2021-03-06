/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */

/**
 * @author Dave Hickin
 *
 */
package org.epics.exampleJava.pvDatabaseRPC;

import java.util.ArrayList;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.ThreadReady;


public class Device implements RunnableReady
{
    public enum State {
        IDLE, READY, RUNNING, PAUSED
    }

    public synchronized State getState()
    {
        return state;
    }

    public static interface Callback
    {
        public void update(int flags);

        public final static int SETPOINT_CHANGED = 0x1;
        public final static int READBACK_CHANGED = 0x2;
        public final static int STATE_CHANGED    = 0x4;
        public final static int SCAN_COMPLETE    = 0x8;
    }

    public synchronized void registerCallback(Callback callback)
    {
        for (Callback current : callbacks)
            if (current == callback) return;

        callbacks.add(callback);
    }

    public synchronized boolean unregisterCallback(Callback callback)
    {
        return callbacks.remove(callback);
    }

    synchronized void scanComplete()
    {
        flags |= Device.Callback.SCAN_COMPLETE;
    }

    synchronized void update()
    {
        ArrayList<Callback> callbacks = (ArrayList<Callback>)this.callbacks.clone();

        if (flags != 0)
        {
            for (Callback callback : callbacks)
                callback.update(flags);

            flags = 0;
        }
    }

    public Device()
    {
        threadCreate.create("device",ThreadPriority.getJavaPriority(ThreadPriority.middle), this);
    }

    public void run(ThreadReady threadReady)
    {
        threadReady.ready();
        while (true)
        {
            try {
                Thread.sleep(100);
                synchronized(this) {
                    if (state == State.IDLE || state == State.RUNNING)
                    {
                        if (!positionRB.equals(positionSP))
                        {
                            double dx = positionSP.x - positionRB.x;
                            double dy = positionSP.y - positionRB.y;

                            final double ds = Math.sqrt(dx*dx+dy*dy);
                            final double maxds = 0.01;
                            // avoid very small final steps
                            final double maxds_x = maxds + 1.0e-5;

                            if (ds > maxds_x)
                            {
                                double scale = maxds/ds;
                                dx *= scale;
                                dy *= scale;
                                setReadbackImpl(new Point(
                                    positionRB.x + dx, positionRB.y + dy));
                            }
                            else
                            {
                                setReadbackImpl(positionSP);
                            }
                        }
                    }

                    if (state == State.RUNNING && positionRB.equals(positionSP))
                    {
                        if (index < points.size())
                        {
                            setSetpointImpl(points.get(index));
                            ++index;
                        }
                        else
                        {
                            scanComplete();
                            stop();
                        }
                    }
                }
            }
            catch (Throwable t) { abort(); }
            update();
        }
    }

    synchronized public Point getPositionSetpoint()
    {
        return positionSP;
    }

    synchronized public Point getPositionReadback()
    {
        return positionRB;
    }

    synchronized public void setSetpoint(Point sp)
    {
        if (state != State.IDLE)
        {
            String message = String.format(
                "Cannot set position setpoint unless device is IDLE. State is %1$s",
                 state);
            throw new IllegalOperationException(message);
        }
        setSetpointImpl(sp);
    }

    private void setSetpointImpl(Point sp)
    {
       positionSP = sp;
       flags |= Device.Callback.SETPOINT_CHANGED;
    }

    private void setReadbackImpl(Point rb)
    {
       positionRB = rb;
       flags |= Device.Callback.READBACK_CHANGED;
    }

    private void setStateImpl(State state)
    {
        this.state = state;
        flags |= Device.Callback.STATE_CHANGED;
    }

    synchronized public void abort()
    {
        System.out.println("Abort");
        setStateImpl(State.IDLE);
        points.clear();
        if (!positionSP.equals(positionRB))
            setSetpointImpl(positionRB);
    }

    synchronized public void configure(ArrayList<Point> newPoints)
    {
        if (state != State.IDLE)
        {
            String message = String.format(
                "Cannot configure device unless it is IDLE. State is %1$s",
                 state);
            throw new IllegalOperationException(message);
        }
        System.out.println("Configure");
        setStateImpl(State.READY);
        points = (ArrayList<Point>)newPoints.clone();
        if (!positionSP.equals(positionRB))
            setSetpointImpl(positionRB);
    }

    synchronized public void run()
    {
        if (state != State.READY)
        {
            String message = String.format(
                "Cannot run device unless it is READY. State is %1$s",
                 state);
            throw new IllegalOperationException(message);
        }
        System.out.println("Run");
        setStateImpl(State.RUNNING);
        index = 0;
    }

    synchronized public void pause()
    {
        if (state != State.RUNNING) 
        {
            String message = String.format(
                "Cannot pause device unless it is RUNNING. State is %1$s",
                 state);
            throw new IllegalOperationException(message);
        }
        System.out.println("Pause");
        setStateImpl(State.PAUSED);
    }

    synchronized public void resume()
    {
        if (state != State.PAUSED) 
        {
            String message = String.format(
                "Cannot resume device unless it is PAUSED. State is %1$s",
                 state);
            throw new IllegalOperationException(message);
        }
        System.out.println("Resume");
        setStateImpl(State.RUNNING);
    }

    synchronized public void stop()
    {
        switch (state)
        {
        case RUNNING:
        case PAUSED:
        case READY:
            System.out.println("Stop");
            setStateImpl(State.READY);
            if (!positionSP.equals(positionRB))
                setSetpointImpl(positionRB);
            break;
        default:
            {
                String message = String.format(
                    "Cannot stop device unless it is RUNNING, PAUSED or READY. State is %1$s",
                     state);
                throw new IllegalOperationException(message);
            }
        }
    }

    synchronized public void rewind(int n)
    {
        switch (state)
        {
        case RUNNING:
        case PAUSED:
            if (n < 0)
            {
                String message = String.format(
                    "Rewind argument cannot be negative. Argument is %1$d",
                        n);
                throw new IllegalOperationException(message);
            }
            if (n > 0)
            {
                System.out.println("Rewind(" + n +")");
                index -= n+1;
                if (index < 0)
                    index = 0;
                setSetpointImpl(points.get(index));
                ++index;
            }
            break;
        default:
            {
                String message = String.format(
                    "Cannot rewind device unless it is RUNNING or PAUSED. State is %1$s",
                     state);
                throw new IllegalOperationException(message);
            }
        }
    }

    private State state = State.IDLE;

    private int flags = 0;

    private Point positionSP = new Point();
    private Point positionRB = new Point();

    private ArrayList<Callback> callbacks = new ArrayList<Callback>();

    private int index = 0;
    private ArrayList<Point> points = new ArrayList<Point>();

    private static ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();


}
