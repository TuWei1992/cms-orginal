package com.zving.framework.ui.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.zving.framework.utility.FastStringBuilder;

/**
 * HTML元素容器虚拟类
 * 
 * @author 王育春
 * @mail wyuch@zving.com
 * @date 2014-3-3
 */
abstract class HtmlElementContainer extends HtmlNode {
	protected List<HtmlNode> children = null;

	public List<HtmlNode> getChildren() {
		if (children == null) {
			children = new ArrayList<HtmlNode>(4);
		}
		return children;
	}

	public void addChild(HtmlNode child) {
		if (child instanceof HtmlDocument) {
			throw new HtmlParseException("Can't add HtmlDocument as a child.");
		}
		child.setParent(this);
	}

	public void addText(String text) {
		HtmlText node = new HtmlText(text);
		node.setParent(this);
	}

	public void addComment(String comment) {
		HtmlComment node = new HtmlComment(comment);
		node.setParent(this);
	}

	public void addInstruction(String instruction) {
		HtmlInstruction node = new HtmlInstruction(instruction);
		node.setParent(this);
	}

	public List<HtmlElement> elements() {
		List<HtmlElement> elements = new ArrayList<HtmlElement>();
		for (HtmlNode node : getChildren()) {
			if (node.getType() == HtmlNode.ELEMENT) {
				elements.add((HtmlElement) node);
			}
		}
		return elements;
	}

	public List<HtmlElement> elements(String path) {
		String[] arr = path.split("\\.");
		List<HtmlElement> list = elements();
		for (int i = 0; i < arr.length; i++) {
			String seg = arr[i];
			List<HtmlElement> result = new ArrayList<HtmlElement>();
			for (HtmlElement ele : list) {
				if ("*".equals(seg) || ele.getTagName().equalsIgnoreCase(seg)) {
					result.add(ele);
				}
			}
			list = result;
			if (list.size() == 0) {
				break;
			}
			if (i != arr.length - 1) {
				List<HtmlElement> tmp = new ArrayList<HtmlElement>();
				for (HtmlElement ele : list) {
					tmp.addAll(ele.elements());
				}
				list = tmp;
			}
		}
		return list;
	}

	public HtmlElement element(String path) {
		List<HtmlElement> nodes = elements(path);
		return nodes == null || nodes.size() == 0 ? null : nodes.get(0);
	}

	/**
	 * 考虑到文档有可能常驻内存，则需要调用repack()重新组织字符串以节约内存
	 */
	@Override
	void repack() {
		for (HtmlNode node : getChildren()) {
			node.repack();
		}
	}

	/**
	 * 获得指定标签名的顶层元素（多层嵌套时只取符合指定标签名的最顶级的元素）
	 */
	public List<HtmlElement> getTopElementsByTagName(String tagName) {
		ArrayList<HtmlElement> list = new ArrayList<HtmlElement>();
		for (HtmlNode node : getChildren()) {
			if (node instanceof HtmlElement) {
				HtmlElement ele = (HtmlElement) node;
				getTopElementsByTagName(list, ele, tagName);
			}
		}
		return list;
	}

	/**
	 * 按标签名获得元素列表，本方法会遍所有层级。
	 */
	public List<HtmlElement> getElementsByTagName(String tagName) {// NO_UCD
		ArrayList<HtmlElement> list = new ArrayList<HtmlElement>();
		for (HtmlNode node : getChildren()) {
			if (node instanceof HtmlElement) {
				HtmlElement ele = (HtmlElement) node;
				getElementsByTagName(list, ele, tagName);
			}
		}
		return list;
	}

	private static void getTopElementsByTagName(List<HtmlElement> list, HtmlElement parent, String tagName) {
		if (parent.getTagName().equalsIgnoreCase(tagName)) {
			list.add(parent);
			return;
		} else {
			for (HtmlNode node : parent.getChildren()) {
				if (node instanceof HtmlElement) {
					HtmlElement ele = (HtmlElement) node;
					getTopElementsByTagName(list, ele, tagName);
				}
			}
		}
	}

	private static void getElementsByTagName(List<HtmlElement> list, HtmlElement parent, String tagName) {
		if (parent.getTagName().equalsIgnoreCase(tagName)) {
			list.add(parent);
		}
		for (HtmlNode node : parent.getChildren()) {
			if (node instanceof HtmlElement) {
				HtmlElement ele = (HtmlElement) node;
				getElementsByTagName(list, ele, tagName);
			}
		}
	}

	@Override
	public String getOuterHTML() {
		FastStringBuilder sb = new FastStringBuilder();
		getOuterHTML(sb);
		return sb.toStringAndClose();
	}

	public HtmlNodeIterator iterator() {
		return new HtmlNodeIterator();
	}

	public class HtmlNodeIterator implements Iterator<HtmlNode>, Iterable<HtmlNode> {
		private HtmlNode last;

		private HtmlNode next;

		private HtmlElementContainer parent;

		private int i = 0;

		/**
		 * 以ele为父节点，构造一个遍历器
		 */
		HtmlNodeIterator() {
			parent = HtmlElementContainer.this;
			next = parent.getChildren().size() == 0 ? null : parent.getChildren().get(0);
			i = 1;
		}

		/**
		 * 是否还有下一个节点
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if (next == null) {
				return false;
			}
			return true;
		}

		/**
		 * 获取下一个节点
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public HtmlNode next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			last = next;
			if (next.getType() == HtmlNode.ELEMENT) {
				HtmlElement ele = (HtmlElement) next;
				if (ele.children != null && ele.children.size() > 0) {
					next = ele.children.get(0);
					parent = ele;
					i = 1;
					return last;
				}
			}
			if (i == parent.children.size()) {
				HtmlElementContainer e = parent.getParent();
				HtmlNode c = parent;
				while (true) {
					if (c == HtmlElementContainer.this) {
						next = null;
						break;
					}
					if (e == null) {
						if (HtmlElementContainer.this instanceof HtmlDocument) {
							e = HtmlElementContainer.this;
						} else {
							next = null;
							break;
						}
					}
					i = e.children.indexOf(c);
					if (i == e.children.size() - 1) {
						c = e;
						e = e.getParent();
					} else {
						next = e.children.get(i + 1);
						i += 2;
						parent = e;
						break;
					}
				}
			} else {
				next = parent.children.get(i++);
			}
			return last;
		}

		/**
		 * 删除当前节点
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			if (last == null) {
				throw new IllegalStateException();
			}
			last.parent.getChildren().remove(last);
			last = null;
		}

		@Override
		public Iterator<HtmlNode> iterator() {
			return this;
		}
	}

	public String format() {// NO_UCD
		FastStringBuilder sb = new FastStringBuilder();
		format(sb, "");
		return sb.toStringAndClose();
	}
}
