/***************************************************************************
 * Copyright (c) 2004 - 2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.net4j.util.ui.views;

import org.eclipse.net4j.internal.util.container.ContainerEventAdapter;
import org.eclipse.net4j.util.container.IContainer;
import org.eclipse.net4j.util.event.EventUtil;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Eike Stepper
 */
public class ContainerItemProvider<CONTAINER extends IContainer<Object>> extends ItemProvider<CONTAINER>
{
  private Map<Object, Node> nodes = new HashMap<Object, Node>();

  private ContainerNode root;

  private IElementFilter rootElementFilter;

  public ContainerItemProvider()
  {
  }

  public ContainerItemProvider(IElementFilter rootElementFilter)
  {
    this.rootElementFilter = rootElementFilter;
  }

  public IElementFilter getRootElementFilter()
  {
    return rootElementFilter;
  }

  @Override
  public boolean hasChildren(Object element)
  {
    try
    {
      Node node = getNode(element);
      return !node.getChildren().isEmpty();
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  public Object[] getChildren(Object element)
  {
    try
    {
      Node node = getNode(element);
      List<Node> children = node.getChildren();
      for (Iterator<Node> it = children.iterator(); it.hasNext();)
      {
        Object child = it.next();
        if (!LifecycleUtil.isActive(child))
        {
          it.remove();
        }
      }

      Object[] result = new Object[children.size()];
      for (int i = 0; i < result.length; i++)
      {
        result[i] = children.get(i).getElement();
      }

      return result;
    }
    catch (RuntimeException ex)
    {
      return NO_ELEMENTS;
    }
  }

  public Object getParent(Object element)
  {
    try
    {
      Node node = getNode(element);
      Node parentNode = node.getParent();
      return parentNode == null ? null : parentNode.getElement();
    }
    catch (RuntimeException ex)
    {
      return null;
    }
  }

  @Override
  protected void connectInput(CONTAINER input)
  {
    root = (ContainerNode)createNode(null, input);
    nodes.put(input, root);
  }

  @Override
  protected void disconnectInput(CONTAINER input)
  {
    root.dispose();
    root = null;
    nodes.clear();
  }

  protected void elementAdded(Object element, Object parent)
  {
  }

  protected void elementRemoved(Object element, Object parent)
  {
  }

  protected ContainerNode getRoot()
  {
    return root;
  }

  protected Map<Object, Node> getNodes()
  {
    return nodes;
  }

  protected Node getNode(Object element)
  {
    Node node = root;
    if (element != getInput())
    {
      node = nodes.get(element);
    }

    if (node == null)
    {
      throw new IllegalStateException("No node for " + element);
    }

    return node;
  }

  protected Node createNode(Node parent, Object element)
  {
    if (element instanceof IContainer)
    {
      return new ContainerNode(parent, (IContainer<Object>)element);
    }

    return new LeafNode(parent, element);
  }

  protected boolean filterRootElement(Object element)
  {
    if (rootElementFilter != null)
    {
      return rootElementFilter.filter(element);
    }

    return true;
  }

  /**
   * @author Eike Stepper
   */
  public interface Node
  {
    public void dispose();

    public Object getElement();

    public Node getParent();

    public List<Node> getChildren();
  }

  /**
   * @author Eike Stepper
   */
  public abstract class AbstractNode implements Node
  {
    private Node parent;

    private List<Node> children;

    public AbstractNode(Node parent)
    {
      this.parent = parent;
    }

    public void dispose()
    {
      parent = null;
      if (children != null)
      {
        for (Node child : children)
        {
          child.dispose();
        }

        children.clear();
        children = null;
      }
    }

    @Override
    public String toString()
    {
      return getElement().toString();
    }

    public final Node getParent()
    {
      return parent;
    }

    public final List<Node> getChildren()
    {
      if (children == null)
      {
        children = createChildren();
      }

      return children;
    }

    protected abstract List<Node> createChildren();
  }

  /**
   * @author Eike Stepper
   */
  public class ContainerNode extends AbstractNode
  {
    private IContainer<Object> container;

    private IListener containerListener = new ContainerEventAdapter<Object>()
    {
      @Override
      protected void onAdded(IContainer<Object> container, Object element)
      {
        if (container == ContainerNode.this.container)
        {
          Node node = addChild(getChildren(), element);
          if (node != null)
          {
            refreshElement(container, true);
            revealElement(element);
            elementAdded(element, container);
          }
        }
      }

      @Override
      protected void onRemoved(IContainer<Object> container, Object element)
      {
        if (container == ContainerNode.this.container)
        {
          Node node = nodes.remove(element);
          if (node != null)
          {
            getChildren().remove(node);
            node.dispose();
            refreshElement(container, true);
            elementRemoved(element, container);
          }
        }
      }

      @Override
      protected void notifyOtherEvent(IEvent event)
      {
        updateLabels(event.getSource());
      }
    };

    public ContainerNode(Node parent, IContainer<Object> container)
    {
      super(parent);
      this.container = container;
    }

    @Override
    public void dispose()
    {
      container.removeListener(containerListener);
      container = null;
      super.dispose();
    }

    public IContainer<Object> getContainer()
    {
      return container;
    }

    public Object getElement()
    {
      return container;
    }

    @Override
    public String toString()
    {
      return container == null ? super.toString() : container.toString();
    }

    @Override
    protected List<Node> createChildren()
    {
      Object[] elements = container.getElements();
      List<Node> children = new ArrayList<Node>(elements.length);
      for (int i = 0; i < elements.length; i++)
      {
        Object element = elements[i];
        addChild(children, element);
      }

      container.addListener(containerListener);
      return children;
    }

    protected Node addChild(List<Node> children, Object element)
    {
      if (nodes.containsKey(element))
      {
        return null;
      }

      if (this != root || filterRootElement(element))
      {
        Node node = createNode(this, element);
        nodes.put(element, node);
        children.add(node);
        return node;
      }

      return null;
    }
  }

  /**
   * @author Eike Stepper
   */
  public class LeafNode implements Node, IListener
  {
    private Node parent;

    private Object element;

    public LeafNode(Node parent, Object element)
    {
      this.parent = parent;
      this.element = element;
      EventUtil.addListener(element, this);
    }

    public void dispose()
    {
      EventUtil.removeListener(element, this);
      element = null;
      parent = null;
    }

    public Node getParent()
    {
      return parent;
    }

    public Object getElement()
    {
      return element;
    }

    public List<Node> getChildren()
    {
      return Collections.emptyList();
    }

    public void notifyEvent(IEvent event)
    {
      updateLabels(event.getSource());
    }

    @Override
    public String toString()
    {
      return element == null ? super.toString() : element.toString();
    }
  }
}
