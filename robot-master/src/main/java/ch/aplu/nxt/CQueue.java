// CQueue.java

/*
This software is part of the NxtJLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/

/* Implementation details:
 * Buffer is a an array of CQueueData, thus its size is fixed at creation time
 * and does not grow dynamically.
 *
 * Two indices are used:
 * 'head' points to the element that is entered last, next element is entered
 * at head + 1 (modulo size)
 * 'tail' points to the element that is entered first, this is the element that will
 * be retrieved next.
 *
 * head and tail are wrapped (value 0..size-1) so that the buffer becomes circular
 *
 * # of elements:
 *  head - tail + 1, for head >= tail
 *  empty: size - emtpy, empty = tail - head - 1, for head < tail
 *
 * special case: empty buffer (0 elements): tail = -1, head = -1, next element
 * entered at head = 0. tail will be set to 0. This is also the initial condition.
 */

package ch.aplu.nxt;

/**
 * Implementation of a circular queue of fixed size (FIFO buffer) containing elements
 * of class CQueueData.
 * @see CQueueData
 */
public class CQueue
{
  private CQueueData[] buf;
  private int tail;
  private int head;
  private int size;

  /**
   * Creates a queue with given maximal number of elements.
   * For size <= 0 no buffer is available.
   * @param size the size of the queue (maximal number of elements before overrun)
   */
  public CQueue(int size)
  {
    if (size <= 0)  // no buffer available
      size = 0;
    else
    {
      buf = new CQueueData[size];
      init();
    }
    this.size = size;
  }

  /**
   * Puts given given element at the next position of the queue.
   * @return true, if successful; false, if overrun or no buffer available
   */
  public boolean put(CQueueData data)
  {
    if (size == 0)
      return false;
    synchronized (buf)
    {
      incHead();
      buf[head] = data;
      if (head == tail)  // overrun
      {
        incTail();
        return false;
      }
      if (tail == -1)  // empty
        tail = 0;
      return true;
    }
  }

  /**
   * Returns the next element from the queue.
   * @return a reference to a clone of the next available element or null,
   * if queue is empty or no buffer available
   */
  public CQueueData get()
  {
    if (size == 0)  // no buf
      return null;
    synchronized (buf)
    {
      if (tail == -1)  // empty
        return null;
      CQueueData tmp = new CQueueData(buf[tail]);
      if (head == tail)
        init();
      else
        incTail();
      return tmp;
    }
  }

  /**
   * Returns the current number of data elements in the queue.
   * @return the number of elements or -1 if no buffer available
   */
  public int available()
  {
    if (size == 0)  // no buf
      return -1;
    synchronized (buf)
    {
      if (tail == -1)
        return 0;
      if (head >= tail)
        return head - tail + 1;
      else
        return size - (tail - head - 1);
    }
  }

  private void incHead()
  {
    head++;
    if (head == size)  // wrap
      head = 0;
  }

  private void incTail()
  {
    tail++;
    if (tail == size) // wrap
      tail = 0;
  }

  private void init()
  {
    tail = -1;
    head = -1;
  }
}
