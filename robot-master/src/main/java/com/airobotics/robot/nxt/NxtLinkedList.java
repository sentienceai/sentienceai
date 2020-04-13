package com.airobotics.robot.nxt;

import java.util.Queue;

import com.airobotics.robot.api.IQueue;

@SuppressWarnings("unchecked")
public class NxtLinkedList<E> extends Queue<E> implements IQueue<E> {
	public boolean offer(E e) {
		super.push(e);
		return true;
	}

	public E poll() {
		return (E) super.pop();
	}
	
	public E peek() {
		return (E) super.peek();
	}

	public E remove(int index) {
		E elem = (E) super.elementAt(index);
		super.removeElementAt(index);
		return elem;
	}
}
