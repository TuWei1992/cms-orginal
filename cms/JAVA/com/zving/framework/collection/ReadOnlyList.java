package com.zving.framework.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.zving.framework.core.FrameworkException;

/**
 * 只读的、线程安全的List，调用涉及到修改List的方法时会抛出异常。
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-4-7
 */
public class ReadOnlyList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;

	List<? extends E> data;

	public ReadOnlyList(List<? extends E> data) {
		if (data == null) {
			throw new NullPointerException();
		}
		this.data = data;
	}

	public ReadOnlyList(Collection<? extends E> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		ArrayList<E> list = new ArrayList<E>(c.size());
		for (E e : c) {
			list.add(e);
		}
		this.data = list;
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}

	@Override
	public boolean add(E o) {
		throw new ListReadOnlyException();
	}

	@Override
	public boolean remove(Object o) {
		throw new ListReadOnlyException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new ListReadOnlyException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new ListReadOnlyException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new ListReadOnlyException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new ListReadOnlyException();
	}

	@Override
	public void clear() {
		throw new ListReadOnlyException();
	}

	@Override
	public E get(int index) {
		return data.get(index);
	}

	@Override
	public E set(int index, E element) {
		throw new ListReadOnlyException();
	}

	@Override
	public void add(int index, E element) {
		throw new ListReadOnlyException();
	}

	@Override
	public E remove(int index) {
		throw new ListReadOnlyException();
	}

	@Override
	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new ListReadOnlyException("ReadOnlyList's subList is not supported!");
	}

	public static class ListReadOnlyException extends FrameworkException {
		private static final long serialVersionUID = 1L;

		public ListReadOnlyException() {
			super("Can't modify a ReadOnlyList!");
		}

		public ListReadOnlyException(String message) {
			super(message);
		}
	}

	private class ListItr extends Itr implements ListIterator<E> {
		ListItr(int index) {
			cursor = index;
		}

		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		@Override
		public E previous() {
			try {
				int i = cursor - 1;
				E previous = get(i);
				return previous;
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void set(E o) {
			throw new ListReadOnlyException();
		}

		@Override
		public void add(E o) {
			throw new ListReadOnlyException();
		}
	}

	private class Itr implements Iterator<E> {
		int cursor = 0;

		@Override
		public boolean hasNext() {
			return cursor != size();
		}

		@Override
		public E next() {
			try {
				E next = get(cursor++);
				return next;
			} catch (IndexOutOfBoundsException e) {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new ListReadOnlyException();

		}
	}
}
