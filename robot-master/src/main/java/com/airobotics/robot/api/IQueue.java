package com.airobotics.robot.api;

// java.util.Queue in lejos is not an interface
// so, a runtime exception happens with the simulator which uses a regular JRE
public interface IQueue<E> {
	public boolean offer(E e);
	public E poll();
	public E peek();
	public boolean isEmpty();
	public Object[] toArray();
	public E remove(int index);
	public int size();
	public void clear();
}
