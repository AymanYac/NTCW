package model;

import javafx.collections.ObservableListBase;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;


public class ObservableDeque<E> extends ObservableListBase<E> implements Deque<E> {

    private  Deque<E> queue ;


    /**
     * Creates an ObservableQueue backed by the supplied Queue. 
     * Note that manipulations of the underlying queue will not result
     * in notification to listeners.
     * 
     * @param queue
     */
    public ObservableDeque(Deque<E> queue) {
        this.queue = queue ;
    }

    /**
     * Creates an ObservableQueue backed by a LinkedList.
     */
    public ObservableDeque() {
        this(new LinkedList<>());
    }

    @Override
    public boolean offer(E e) {
        beginChange();
        boolean result = queue.offer(e);
        if (result) {
            nextAdd(queue.size()-1, queue.size());
        }
        endChange();
        return result ;
    }

    @Override
    public boolean add(E e) {
        beginChange() ;
        try {
            queue.add(e);
            nextAdd(queue.size()-1, queue.size());
            return true ;
        } finally {
            endChange();
        }
    }
    
    @Override
	public void addFirst(E arg0) {
    	beginChange() ;
        try {
            queue.addFirst(arg0);
            nextAdd(queue.size()-1, queue.size());
        } finally {
            endChange();
        }
	}
    
    @Override
	public void addLast(E arg0) {
    	beginChange() ;
        try {
            queue.addLast(arg0);
            nextAdd(queue.size()-1, queue.size());
        } finally {
            endChange();
        }
	}


    @Override
    public E remove() {
        beginChange();
        try {
            E e = queue.remove();
            nextRemove(0, e);
            return e;
        } finally {
            endChange();
        }
    }
    
    @Override
	public E removeFirst() {
    	beginChange();
        try {
            E e = queue.removeFirst();
            nextRemove(0, e);
            return e;
        } finally {
            endChange();
        }
	}
    
    @Override
	public E removeLast() {
    	beginChange();
        try {
            E e = queue.removeLast();
            nextRemove(0, e);
            return e;
        } finally {
            endChange();
        }
	}

    @Override
    public E poll() {
        beginChange();
        E e = queue.poll();
        if (e != null) {
            nextRemove(0, e);
        }
        endChange();
        return e ;
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public E get(int index) {
        Iterator<E> iterator = queue.iterator();
        for (int i = 0; i < index; i++) iterator.next();
        return iterator.next();
    }

    @Override
    public int size() {
        return queue.size();
    }

	

	

	@Override
	public Iterator<E> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E getFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E getLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean offerFirst(E arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offerLast(E arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E peekFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E peekLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E pollFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E pollLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E pop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void push(E arg0) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public boolean removeFirstOccurrence(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	

	@Override
	public boolean removeLastOccurrence(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}